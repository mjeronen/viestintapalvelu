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

import fi.vm.sade.viestintapalvelu.common.exception.ExternalInterfaceException;
import fi.vm.sade.viestintapalvelu.externalinterface.RyhmasahkopostiRestClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
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

    private final EmailResource ryhmasahkopostiRestClient;

    @Autowired
    private EmailBuilder emailBuilder;

    @Autowired
    public EmailComponent(RyhmasahkopostiRestClient ryhmasahkopostiRestClient) {
        this.ryhmasahkopostiRestClient = ryhmasahkopostiRestClient;
    }

    public boolean sendEmail(EmailData data) {
        try {
            ryhmasahkopostiRestClient.sendEmail(data);
            return true;
        } catch (Exception e) {
            LOGGER.error("Email sending request failed for data " + data + ", reason: " + e.getMessage(), e);
            return false;
        }
    }

    public String getPreview(EmailData data) {
        try {
            LOGGER.warn("Calling external interface EmailResource.getPreview");
            HttpResponse response = ryhmasahkopostiRestClient.getPreview(data);
            InputStream stream = response.getEntity().getContent();
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

        try {
            ryhmasahkopostiRestClient.sendEmail(emailData);
            return true;
        } catch (Exception e) {
            LOGGER.error("Email sending request failed for letter " + source + ", reason: " + e.getMessage(), e);
            return false;
        }
    }
}
