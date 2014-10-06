package fi.vm.sade.viestintapalvelu.externalinterface.component;

import java.io.InputStream;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.viestintapalvelu.email.EmailBuilder;
import fi.vm.sade.viestintapalvelu.email.EmailSourceData;
import fi.vm.sade.viestintapalvelu.exception.ExternalInterfaceException;
import fi.vm.sade.viestintapalvelu.externalinterface.api.EmailResource;

@Component
public class EmailComponent {
    private static Logger LOGGER = LoggerFactory.getLogger(EmailComponent.class);

    @Resource
    private EmailResource emailResourceClient;

    @Autowired
    private EmailBuilder emailBuilder;


    public boolean sendEmail(EmailData data) {
         return checkResponse(emailResourceClient.sendEmail(data));
    }

    public String getPreview(EmailData data) {
        try {
            InputStream stream = (InputStream)emailResourceClient.getPreview(data).getEntity();
            return IOUtils.toString(stream);
        } catch(Exception e) {
            LOGGER.error("Could make preview for email " + data + ". Reason: " + e.getMessage(), e);
            throw  new ExternalInterfaceException("Email preview failed", e);
        }
    }

    public boolean sendEmail(EmailSourceData source) {
        LOGGER.debug("Handling email for letter " + source);
        EmailData emailData;
        try {
            emailData = emailBuilder.buildEmailData(source);
            LOGGER.debug("Got emaildata "+ emailData +  " for letter "+ source);
        } catch (Exception e) {
            LOGGER.error("Could not make email data for letter " + source + " reason " + e.getMessage(), e);
            return false;
        }
        return checkResponse(emailResourceClient.sendEmail(emailData));
    }
    
    private boolean checkResponse(Response response) {
        // if response is ok.. 
        LOGGER.debug("Got email response: " + response.toString() + " " + response.getStatus());
        return true;
    }
}
