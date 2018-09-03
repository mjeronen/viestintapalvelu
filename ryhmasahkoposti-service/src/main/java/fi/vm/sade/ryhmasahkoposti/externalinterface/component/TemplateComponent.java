/**
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
 **/
package fi.vm.sade.ryhmasahkoposti.externalinterface.component;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.vm.sade.ryhmasahkoposti.api.dto.TemplateDTO;
import fi.vm.sade.ryhmasahkoposti.externalinterface.api.TemplateResource;
import fi.vm.sade.viestintapalvelu.common.exception.ExternalInterfaceException;

/**
 * Template component
 * 
 * @author ovmol1
 *
 */
@Component
public class TemplateComponent {

    private static Logger LOGGER = LoggerFactory.getLogger(TemplateComponent.class);

    @Resource
    private TemplateResource viestintapalveluRestClient;

    /**
     * Get template content
     * 
     * @param templateName
     * @param languageCode
     * @param type
     * @return
     */
    public TemplateDTO getTemplateContent(String templateName, String languageCode, String type, String applicationPeriod) {
        try {
            languageCode = languageCode != null ? languageCode.toUpperCase() : languageCode;
            if (applicationPeriod != null && !applicationPeriod.isEmpty()) {
                return viestintapalveluRestClient.getTemplateContent("" + templateName, "" + languageCode, "" + type, "" + applicationPeriod);
            } else {
                return viestintapalveluRestClient.getTemplateContent("" + templateName, "" + languageCode, "" + type);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ExternalInterfaceException("error.msg.gettingTemplateDataFailed", e);
        }
    }

    public TemplateDTO getTemplateContent(String templateId) {
        try {
            return viestintapalveluRestClient.getTemplateByID(templateId, "email");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ExternalInterfaceException("error.msg.gettingTemplateDataFailed", e);
        }
    }

    public void setTemplateResourceClient(TemplateResource templateResourceClient) {
        this.viestintapalveluRestClient = templateResourceClient;
    }
}
