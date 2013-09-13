package fi.vm.sade.viestintapalvelu;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import fi.vm.sade.viestintapalvelu.download.DownloadResource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.ExecutorService;

public class AsynchronousResource {
    @Inject
    private ExecutorService executorService;

    public void executeAsynchronously(Runnable task) {
        executorService.execute(task);
    }

    protected Response createResponse(HttpServletRequest request,
                                      String documentId) {
        String resultUrl = urlTo(request, DownloadResource.class);
        URI contentLocation = URI.create(resultUrl + "/" + documentId);
        return Response.status(Status.ACCEPTED)
                .contentLocation(contentLocation)
                .entity(contentLocation.toString()).build();
    }

    String urlTo(HttpServletRequest request,
                 Class<DownloadResource> resourceClass) {
        String path = chompSlashes(request.getContextPath().trim().equals("") ? ""
                : "/" + request.getContextPath())
                + "/"
                + chompSlashes((request.getServletPath().trim().equals("") ? ""
                : "/" + request.getServletPath()))
                + "/"
                + chompSlashes((UriBuilder.fromResource(resourceClass).build()
                .toString()));
        return UriBuilder.fromUri(request.getRequestURL().toString())
                .replacePath(path).build().toString();
    }

    private static String chompSlashes(final String input) {
        return Joiner.on("/").skipNulls().join(Splitter.on('/').omitEmptyStrings().trimResults().split(input));
    }
}