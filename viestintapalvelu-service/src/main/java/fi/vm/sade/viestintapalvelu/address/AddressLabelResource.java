package fi.vm.sade.viestintapalvelu.address;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Service;

import com.lowagie.text.DocumentException;

import fi.vm.sade.viestintapalvelu.AsynchronousResource;
import fi.vm.sade.viestintapalvelu.Urls;
import fi.vm.sade.viestintapalvelu.download.Download;
import fi.vm.sade.viestintapalvelu.download.DownloadCache;

@Service
@Singleton
@Path(Urls.ADDRESS_LABEL_RESOURCE_PATH)
public class AddressLabelResource extends AsynchronousResource {
    private DownloadCache downloadCache;
    private AddressLabelBuilder labelBuilder;

    @Inject
    public AddressLabelResource(AddressLabelBuilder labelBuilder, DownloadCache downloadCache) {
        this.labelBuilder = labelBuilder;
        this.downloadCache = downloadCache;
    }

    @POST
    @Consumes("application/json")
    @Produces("text/plain")
    @Path("pdf")
    public Response pdf(final AddressLabelBatch input, @Context HttpServletRequest request) throws IOException,
            DocumentException {
        final String documentId = UUID.randomUUID().toString();
        byte[] pdf = labelBuilder.printPDF(input);
        downloadCache.addDocument(new Download("application/pdf;charset=utf-8", "addresslabels.pdf", pdf), documentId);
        return createResponse(request, documentId);
    }

    @POST
    @Consumes("application/json")
    @Produces("text/plain")
    @Path("xls")
    public Response xls(AddressLabelBatch input, @Context HttpServletRequest request) throws IOException,
            DocumentException {
        byte[] csv = labelBuilder.printCSV(input);
        String documentId = downloadCache
                .addDocument(new Download("application/vnd.ms-excel", "addresslabels.xls", csv));
        return createResponse(request, documentId);
    }
}