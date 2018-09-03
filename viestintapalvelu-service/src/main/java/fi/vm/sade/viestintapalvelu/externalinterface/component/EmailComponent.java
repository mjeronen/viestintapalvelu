/*
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
 */
package fi.vm.sade.viestintapalvelu.externalinterface.component;

import java.io.InputStream;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;

import fi.vm.sade.viestintapalvelu.common.exception.ExternalInterfaceException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.viestintapalvelu.email.EmailBuilder;
import fi.vm.sade.viestintapalvelu.email.EmailSourceData;
import fi.vm.sade.viestintapalvelu.externalinterface.api.EmailResource;

@Component
public class EmailComponent {
    private static Logger LOGGER = LoggerFactory.getLogger(EmailComponent.class);

    @Resource
    private EmailResource emailResourceClient;

    @Autowired
    private EmailBuilder emailBuilder;

    public boolean sendEmail(EmailData data) {
        LOGGER.warn("Calling external interface EmailResource.sendEmail");
        Response response = emailResourceClient.sendEmail(data);
        return checkResponse(response);
    }

    public String getPreview(EmailData data) {
        try {
            LOGGER.warn("Calling external interface EmailResource.getPreview");
            InputStream stream = (InputStream) emailResourceClient.getPreview(data).getEntity();
            return IOUtils.toString(stream);
        } catch (Exception e) {
            LOGGER.error("Could not make preview for email " + data + ". Reason: " + e.getMessage(), e);
            throw new ExternalInterfaceException("Email preview failed", e);
        }
    }

    public boolean sendEmail(EmailSourceData source) {
        LOGGER.debug("Handling email for letter " + source);
        EmailData emailData;
        try {
            emailData = emailBuilder.buildEmailData(source);
            LOGGER.debug("Got emaildata " + emailData + " for letter " + source);
        } catch (Exception e) {
            LOGGER.error("Could not make email data for letter " + source + " reason " + e.getMessage(), e);
            return false;
        }
        LOGGER.warn("Calling external interface EmailResource.sendEmail");
        Response response = emailResourceClient.sendEmail(emailData);
        return checkResponse(response);
    }

    private boolean checkResponse(Response response) {
        LOGGER.debug("Got email response: " + response.toString() + " " + response.getStatus());
        return true;
    }
}
