package fi.vm.sade.viestintapalvelu;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * Filter to handle cross-origin resource sharing.
 */
public class CORSFilter implements ContainerResponseFilter {
    private static final String ACAOHEADER = "Access-Control-Allow-Origin";
    private static final String ACRHHEADER = "Access-Control-Request-Headers";
    private static final String ACAHHEADER = "Access-Control-Allow-Headers";
    private static final String ACAMHEADER = "Access-Control-Allow-Methods";
    private static final String ACACHEADER = "Access-Control-Allow-Credentials";

    private final transient CORSConfiguration configuration = new CORSConfiguration();

    public CORSFilter() {
    }

    @Override
    public ContainerResponse filter(final ContainerRequest request, final ContainerResponse response) {
        // TODO Tighten up security and configuration options
        // final String requestOrigin = request.getHeaderValue(ORIGINHEADER);
        response.getHttpHeaders().add(ACAOHEADER, this.configuration.getOrigin());

        final String requestHeaders = request.getHeaderValue(ACRHHEADER);
        response.getHttpHeaders().add(ACAHHEADER, requestHeaders);

        response.getHttpHeaders().add(ACAMHEADER, this.configuration.getAllowedMethods());

        response.getHttpHeaders().add(ACACHEADER, this.configuration.allowCredentials());

        return response;
    }
}