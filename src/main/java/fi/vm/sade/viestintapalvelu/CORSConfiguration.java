package fi.vm.sade.viestintapalvelu;

import com.google.inject.Inject;

/**
 * Configuration for cross-origin resource sharing in Jersey server
 */
public class CORSConfiguration {
    // CORS configuration
    private String origin = "*";

    private boolean allowCredentials = true;

    private String allowedMethods = "GET, POST, PUT, DELETE, OPTIONS, ACCEPTED";

    @Inject
    public CORSConfiguration() {
    }

    public String getOrigin() {
        return this.origin;
    }

    public boolean allowCredentials() {
        return this.allowCredentials;
    }

    public String getAllowedMethods() {
        return this.allowedMethods;
    }
}