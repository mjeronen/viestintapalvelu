package fi.vm.sade.viestintapalvelu.externalinterface;

import fi.vm.sade.externalinterface.common.ObjectMapperProvider;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.viestintapalvelu.externalinterface.api.EmailResource;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class RyhmasahkopostiRestClient extends CachingRestClient implements EmailResource {
    private final String baseUrl;
    private final ObjectMapperProvider objectMapperProvider;


    public RyhmasahkopostiRestClient(String baseUrl, ObjectMapperProvider objectMapperProvider) {
        this.baseUrl = baseUrl;
        this.objectMapperProvider = objectMapperProvider;
        setClientSubSystemCode("fi.vm.sade.viestintapalvelu");
    }

    @Override
    public HttpResponse sendEmail(EmailData emailData) throws IOException {
        String url = baseUrl + "/email";
        String postBody = objectMapperProvider.getContext(EmailData.class).writeValueAsString(emailData);

        logger.warn("Calling url " + url);
        return post(url, "application/json", postBody);
    }

    @Override
    public HttpResponse getPreview(EmailData emailData) throws Exception {
        String url = baseUrl + "/email/preview";
        String postBody = objectMapperProvider.getContext(EmailData.class).writeValueAsString(emailData);

        logger.warn("Calling url " + url);
        return post(url, "application/json", postBody);
    }
}
