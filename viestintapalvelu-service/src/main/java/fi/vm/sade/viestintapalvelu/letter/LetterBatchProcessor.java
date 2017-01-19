/**
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 **/
package fi.vm.sade.viestintapalvelu.letter;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

import fi.vm.sade.viestintapalvelu.letter.LetterService.LetterBatchProcess;
import fi.vm.sade.viestintapalvelu.letter.dto.LetterBatchSplitedIpostDto;
import fi.vm.sade.viestintapalvelu.letter.impl.LetterServiceImpl;
import fi.vm.sade.viestintapalvelu.letter.processing.*;

@Component
@Singleton
public class LetterBatchProcessor {
    private static final Logger logger = LoggerFactory.getLogger(LetterBatchProcessor.class);

    @Resource(name = "batchJobExecutorService")
    private ExecutorService batchJobExecutorService;
    @Resource(name = "letterReceiverExecutorService")
    private ExecutorService letterReceiverExecutorService;
    @Value("#{poolSizes['threadsPerBatchJob'] != null ? poolSizes['threadsPerBatchJob'] : 4}")
    private int letterBatchJobThreadCount = 4;
    @Value("#{poolSizes['threadsPerBatchJob'] != null ? poolSizes['threadsPerBatchJob'] : 4}")
    private int iPostZipProcessingJobThreadCount = 4;

    private volatile Set<Job<?>> jobsBeingProcessed = new HashSet<>();

    @Autowired
    private LetterService letterService;

    public LetterBatchProcessor() {
    }

    public LetterBatchProcessor(ExecutorService batchJobExecutorService, ExecutorService letterReceiverExecutorService, LetterServiceImpl letterService) {
        this.batchJobExecutorService = batchJobExecutorService;
        this.letterReceiverExecutorService = letterReceiverExecutorService;
        this.letterService = letterService;
    }

    public Future<Boolean> processLetterBatch(long letterBatchId) {
        LetterReceiverJob job = new LetterReceiverJob(letterBatchId);
        reserveJob(job);
        BatchJob<LetterReceiverProcessable> batchJob = new BatchJob<>(new JobDescription<>(job,
                LetterReceiverProcessable.forIds(letterService.findUnprocessedLetterReceiverIdsByBatch(letterBatchId)), letterBatchJobThreadCount));
        return batchJobExecutorService.submit(batchJob);
    }

    // / For testing:
    public boolean isProcessingLetterBatch(long letterBatchId) {
        return jobsBeingProcessed.contains(new LetterReceiverJob(letterBatchId));
    }

    private synchronized void reserveJob(Job<?> job) {
        if (!jobsBeingProcessed.add(job)) {
            throw new ConcurrentModificationException("Trying to process same job " + job + " more than once");
        }
        logger.info("Reserved job={}", job);
    }

    /**
     * LETTER type job
     */
    public class LetterReceiverJob implements Job<LetterReceiverProcessable> {
        private long letterBatchId;

        public LetterReceiverJob(long letterBatchId) {
            this.letterBatchId = letterBatchId;
        }

        public long getLetterBatchId() {
            return letterBatchId;
        }

        @Override
        public void start(JobDescription<LetterReceiverProcessable> description) {
            logger.info("BatchID={}. Starting to process letter batch. Generating {} letters", letterBatchId, description.getProcessables().size());
            letterService.updateBatchProcessingStarted(letterBatchId, LetterBatchProcess.LETTER);
        }

        @Override
        public void handleJobStartedFailure(Exception e, JobDescription<LetterReceiverProcessable> description) {
            // Ignore at this point. Handling not started.
        }

        @Override
        public void process(LetterReceiverProcessable letterReceiverProcessable) throws Exception {
            logger.debug("BatchID={}. Processing next letter generation with ID={}", letterBatchId, letterReceiverProcessable.getId());
            letterService.processLetterReceiver(letterReceiverProcessable.getId());
        }

        @Override
        public void handleFailure(Exception e, LetterReceiverProcessable processable) {
            logger.error("BatchID=" + letterBatchId + ". Unable to start job for letter generation.", e);
            letterService.saveBatchErrorForReceiver(processable.getId(), e.getMessage());
        }

        @Override
        public Optional<? extends JobDescription<?>> jobFinished(JobDescription<LetterReceiverProcessable> description) throws Exception {
            logger.info("BatchID={}. Finishing processing of letter batch. Letters are ready.", letterBatchId);
            Optional<LetterBatchProcess> nextToDo = letterService.updateBatchProcessingFinished(letterBatchId, LetterBatchProcess.LETTER);
            if (nextToDo.isPresent()) {
                switch (nextToDo.get()) {
                case IPOSTI:
                    IPostJob job = new IPostJob(this.letterBatchId);
                    reserveJob(job);
                    LetterBatchSplitedIpostDto splitted = letterService.splitBatchForIpostProcessing(letterBatchId);
                    if (splitted.getProcessables().isEmpty()) {
                        logger.info("BatchID={}. No IPosti found for processing. Skipping IPosti.", letterBatchId);
                        return Optional.absent();
                    }
                    logger.info("BatchID={}. Initializing {} IPosti zip generation for processing.", letterBatchId, splitted.getProcessables().size());
                    JobDescription<IPostiProcessable> jobDescription = new JobDescription<>(job, splitted.getProcessables(),
                            iPostZipProcessingJobThreadCount);
                    return Optional.of(jobDescription);
                default:
                    throw new IllegalStateException(this + " can not start next job " + nextToDo.get());
                }
            }
            return Optional.absent();
        }

        @Override
        public void handleJobFinnishedFailure(Exception e, JobDescription<LetterReceiverProcessable> description) {
            logger.error("BatchID=" + letterBatchId + ". Letter generation job failed", e);
            letterService.errorProcessingBatch(letterBatchId, e);
        }

        @Override
        public String toString() {
            return "LetterBatch=" + this.letterBatchId + " LETTER processsing";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LetterReceiverJob)) {
                return false;
            }
            LetterReceiverJob that = (LetterReceiverJob) o;
            return letterBatchId == that.letterBatchId;
        }

        @Override
        public int hashCode() {
            return (int) (letterBatchId ^ (letterBatchId >>> 32));
        }
    }

    /**
     * IPOSTI type job
     */
    public class IPostJob implements Job<IPostiProcessable> {
        private long letterBatchId;

        public IPostJob(long letterBatchId) {
            this.letterBatchId = letterBatchId;
        }

        public long getLetterBatchId() {
            return letterBatchId;
        }

        @Override
        public void start(JobDescription<IPostiProcessable> description) {
            logger.info("BatchID={}. Starting to process IPosti for letter batch. Generating {} IPosti zips.", letterBatchId, description.getProcessables().size());
            letterService.updateBatchProcessingStarted(letterBatchId, LetterBatchProcess.IPOSTI);
        }

        @Override
        public void handleJobStartedFailure(Exception e, JobDescription<IPostiProcessable> description) {
            logger.error("BatchID=" + letterBatchId + ". Unable to start job for IPosti zip generation.", e);
            letterService.errorProcessingBatch(letterBatchId, e);
        }

        @Override
        public void process(IPostiProcessable processable) throws Exception {
            logger.info("BatchID={}. Processing {}th IPosti zip generation with {} receivers.",
                    letterBatchId, processable.getOrderNumber(), processable.getLetterReceiverIds().size());
            letterService.processIposti(processable);
        }

        @Override
        public void handleFailure(Exception e, IPostiProcessable processable) {
            logger.error("BatchID=" + letterBatchId + ". IPosti zip generation job failed", e);
            letterService.handleIpostError(processable, e);
        }

        @Override
        public Optional<? extends JobDescription<?>> jobFinished(JobDescription<IPostiProcessable> description) throws Exception {
            logger.info("BatchID={}. Finishing processing of IPosti for letter batch. IPosti zips are ready.", letterBatchId);
            Optional<LetterBatchProcess> nextToDo = letterService.updateBatchProcessingFinished(letterBatchId, LetterBatchProcess.IPOSTI);
            if (nextToDo.isPresent()) {
                throw new IllegalStateException(this + " don't know how to start next job " + nextToDo.get());
            }
            return Optional.absent();
        }

        @Override
        public void handleJobFinnishedFailure(Exception e, JobDescription<IPostiProcessable> description) {
            letterService.errorProcessingBatch(letterBatchId, e);
        }

        @Override
        public String toString() {
            return "LetterBatch=" + this.letterBatchId + " IPOSTI";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IPostJob)) {
                return false;
            }
            IPostJob iPostJob = (IPostJob) o;
            return letterBatchId == iPostJob.letterBatchId;
        }

        @Override
        public int hashCode() {
            return (int) (letterBatchId ^ (letterBatchId >>> 32));
        }
    }

    /**
     * Queue per job
     */
    protected class BatchJob<T extends Processable> implements Callable<Boolean> {
        private final int threads;
        private final JobDescription<T> jobDescription;
        private volatile ConcurrentLinkedQueue<T> unprocessed;
        private volatile AtomicBoolean okState = new AtomicBoolean(true);

        public BatchJob(JobDescription<T> jobDescription) {
            this.jobDescription = jobDescription;
            this.threads = Math.max(1, Math.min(jobDescription.getThreads(), jobDescription.getProcessables().size()));
            this.unprocessed = new ConcurrentLinkedQueue<>();
            this.unprocessed.addAll(jobDescription.getProcessables());
        }

        @Override
        public Boolean call() throws Exception {
            logger.info("{} starting", this.jobDescription);
            try {
                this.jobDescription.getJob().start(this.jobDescription);
            } catch (Exception e) {
                logger.error("Error during start with " + this.jobDescription + ". Error: " + e.getMessage(), e);
                this.jobDescription.getJob().handleJobStartedFailure(e, this.jobDescription);
                this.okState.set(false);
            }

            if (this.okState.get()) {
                final CountDownLatch latch = new CountDownLatch(threads);
                for (int i = 0; i < threads; i++) {
                    letterReceiverExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            T nextProcessable = unprocessed.poll();
                            while (okState.get() && nextProcessable != null) {
                                // To ensure that no id is processed twice, you
                                // could verify the output of:
                                // logger.debug("Thread : " +
                                // Thread.currentThread().getId() +
                                // " processing: " + nextId);
                                try {
                                    if (okState.get()) {
                                        jobDescription.getJob().process(nextProcessable);
                                    }
                                } catch (Exception e) {
                                    logger.error("Error processing processable " + nextProcessable + " in job " + jobDescription.getJob(), e);
                                    try {
                                        jobDescription.getJob().handleFailure(e, nextProcessable);
                                    } catch (Exception e2) {
                                        logger.error("handleFailure error", e2);
                                    }
                                    okState.set(false);
                                }
                                nextProcessable = unprocessed.poll();
                            }
                            latch.countDown();
                        }
                    });
                }
                // Sync, wait for all processors finnish:
                latch.await();
            }

            if (okState.get()) {
                logger.info("Job ended successfully: {}", jobDescription.getJob());
                try {
                    Optional<? extends JobDescription<?>> nextJob = jobDescription.getJob().jobFinished(this.jobDescription);
                    if (nextJob.isPresent()) {
                        JobDescription<?> job = nextJob.get();
                        logger.info("Next processing: {}", job);
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        BatchJob<?> batchJob = new BatchJob(job);
                        batchJobExecutorService.submit(batchJob);
                    }
                } catch (Exception e) {
                    logger.error("Error during jobFinished with " + this.jobDescription + ". Error: " + e.getMessage(), e);
                    this.jobDescription.getJob().handleJobFinnishedFailure(e, this.jobDescription);
                    this.okState.set(false);
                }
            } else {
                logger.error("Job " + jobDescription.getJob() + " procsessing ended due to an error.");
            }

            if (!jobsBeingProcessed.remove(jobDescription.getJob())) {
                logger.warn("Did not remove processing state of job={}", jobDescription.getJob());
            }

            logger.info("{} ended", this.jobDescription);
            return okState.get();
        }
    }

    public void setLetterBatchJobThreadCount(int letterBatchJobThreadCount) {
        this.letterBatchJobThreadCount = letterBatchJobThreadCount;
    }

    public int getLetterBatchJobThreadCount() {
        return letterBatchJobThreadCount;
    }
}
