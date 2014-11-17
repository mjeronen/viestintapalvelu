package fi.vm.sade.viestintapalvelu.letter;

import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wordnik.swagger.annotations.ApiParam;

import fi.vm.sade.valinta.dokumenttipalvelu.resource.DokumenttiResource;
import fi.vm.sade.viestintapalvelu.AsynchronousResource;
import fi.vm.sade.viestintapalvelu.dao.dto.LetterBatchStatusDto;
import fi.vm.sade.viestintapalvelu.letter.dto.AsyncLetterBatchDto;
import fi.vm.sade.viestintapalvelu.validator.LetterBatchValidator;
import fi.vm.sade.viestintapalvelu.validator.UserRightsValidator;

public abstract class AbstractLetterResource extends AsynchronousResource {
    private final Logger LOG = LoggerFactory.getLogger(LetterResource.class);

    @Autowired
    protected LetterBuilder letterBuilder;

    @Resource
    protected DokumenttiResource dokumenttipalveluRestClient;

    @Resource(name = "otherAsyncResourceJobsExecutorService")
    protected ExecutorService executor;

    @Autowired
    protected LetterService letterService;

    @Autowired
    protected UserRightsValidator userRightsValidator;

    @Autowired
    protected LetterBatchProcessor letterPDFProcessor;

    @Autowired
    protected LetterEmailService letterEmailService;

    @Autowired
    protected DokumenttiIdProvider dokumenttiIdProvider;

    protected Response createAsyncLetter(@ApiParam(value = "Muodostettavien kirjeiden tiedot (1-n)", required = true) final AsyncLetterBatchDto input, 
            boolean anonymousRequest) {
        try {
            LetterBatchValidator.validate(input);
        } catch (Exception e) {
            LOG.error("Validation error", e);
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            letterBuilder.initTemplateId(input);
            fi.vm.sade.viestintapalvelu.model.LetterBatch letter = letterService.createLetter(input, anonymousRequest);
            Long id = letter.getId();
            letterPDFProcessor.processLetterBatch(id);
            return Response.status(Status.OK).entity(getPrefixedLetterBatchID(id, input.isIposti())).build();
        } catch (Exception e) {
            LOG.error("Letter async failed", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response getLetterBatchStatus(@PathParam("letterBatchId") @ApiParam(value = "Kirjelähetyksen id")  String prefixedLetterBatchId) {
        long letterBatchId = getLetterBatchId(prefixedLetterBatchId);
        LetterBatchStatusDto status = letterService.getBatchStatus(letterBatchId);
        if(status == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        return Response.status(Status.OK).entity(status).build();
    }

    protected long getLetterBatchId(String id) {
        // Expect format: VIES-<tyyppi>-id-<HASH(suola+"VIES-"+tyyppi+id+tallentaja-oid)>
        if (id.startsWith(LetterService.DOKUMENTTI_ID_PREFIX_PDF)) {
            return dokumenttiIdProvider.parseLetterBatchIdByDokumenttiId(id, LetterService.DOKUMENTTI_ID_PREFIX_PDF);
        } else if (id.startsWith(LetterService.DOKUMENTTI_ID_PREFIX_ZIP)) {
            return dokumenttiIdProvider.parseLetterBatchIdByDokumenttiId(id, LetterService.DOKUMENTTI_ID_PREFIX_ZIP);
        } else {
            return Long.parseLong(id);
        }
    }

    protected String getPrefixedLetterBatchID(long id, boolean isZip) {
        if (isZip) {
            return dokumenttiIdProvider.generateDocumentIdForLetterBatchId(id, LetterService.DOKUMENTTI_ID_PREFIX_ZIP);
        } else {
            return dokumenttiIdProvider.generateDocumentIdForLetterBatchId(id, LetterService.DOKUMENTTI_ID_PREFIX_PDF);
        }
    }
}