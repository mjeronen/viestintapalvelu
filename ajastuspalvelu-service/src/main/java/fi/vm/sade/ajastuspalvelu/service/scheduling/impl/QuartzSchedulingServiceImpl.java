/*
 * Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.ajastuspalvelu.service.scheduling.impl;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.sade.ajastuspalvelu.service.scheduling.QuartzSchedulingService;
import fi.vm.sade.ajastuspalvelu.service.scheduling.Schedule;
import fi.vm.sade.ajastuspalvelu.util.DateHelper;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * User: ratamaa
 * Date: 20.10.2014
 * Time: 17:10
 */
@Service
public class QuartzSchedulingServiceImpl implements QuartzSchedulingService {
    private static final Logger logger = LoggerFactory.getLogger(QuartzSchedulingServiceImpl.class);

    public static final String QVARTZ_GROUP_NAME = "ScheduledTask";

    @Autowired
    private Scheduler scheduler;

    @Override
    public void scheduleJob(Long scheduledTaskId, Schedule schedule) throws SchedulerException {
        JobKey key = createKey(scheduledTaskId);
        JobDetail job = scheduler.getJobDetail(key);
        if (job == null) {
            job = newJob(QuartzTriggeredJobDelegator.class)
                    .withIdentity(key)
                    .requestRecovery().storeDurably()
                    .build();
            scheduler.addJob(job, false);
            logger.info("Added job {}", job);
        } else {
            removeAllTriggersFromGivenJob(key);
        }
        if (schedule.isValid()) {
            // And schedule with a new trigger:
            TriggerBuilder<? extends Trigger> builder = newTrigger()
                    .withIdentity("trigger." + key.getName(), QVARTZ_GROUP_NAME)
                    .forJob(key)
                    .withSchedule(cronSchedule(schedule.getCron()));
            if (schedule.getActiveBegin().isPresent()) {
                builder = builder.startAt(schedule.getActiveBegin().transform(DateHelper.TO_DATE).get());
            }
            if (schedule.getActiveEnd().isPresent()) {
                builder = builder.endAt(schedule.getActiveEnd().transform(DateHelper.TO_DATE).get());
            }
            Trigger trigger = builder.build();
            scheduler.scheduleJob(trigger);
            logger.info("Added trigger {} for scheduledTaskId={}", trigger, scheduledTaskId);
        } else {
            logger.info("Schedule {} invalid (in past). Ignoring trigger.", schedule);
        }
    }

    private void removeAllTriggersFromGivenJob(JobKey key) throws SchedulerException {
        for (Trigger trigger : scheduler.getTriggersOfJob(key)) {
            scheduler.unscheduleJob(trigger.getKey());
            logger.info("Removed trigger {}", trigger);
        }
    }

    private JobKey createKey(Long scheduledTaskId) {
        return new JobKey(""+ scheduledTaskId, QVARTZ_GROUP_NAME);
    }

    @Override
    public void unscheduleJob(Long scheduledTaskId) throws SchedulerException {
        JobKey key = createKey(scheduledTaskId);
        JobDetail job = scheduler.getJobDetail(key);
        if (job != null) {
            removeAllTriggersFromGivenJob(key);
            scheduler.deleteJob(key);
            logger.info("Removed job {}", job);
        }
    }

}
