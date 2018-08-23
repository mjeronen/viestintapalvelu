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

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.vm.sade.ryhmasahkoposti.api.dto.EmailAttachment;
import fi.vm.sade.ryhmasahkoposti.externalinterface.api.AttachmentResource;
import fi.vm.sade.ryhmasahkoposti.externalinterface.api.UrisContainerDto;
import fi.vm.sade.viestintapalvelu.common.exception.ExternalInterfaceException;

/**
 * User: ratamaa
 * Date: 24.9.2014
 * Time: 18:13
 */
@Component
public class AttachmentComponent {
    private static Logger logger = LoggerFactory.getLogger(AttachmentComponent.class);

    @Resource
    private AttachmentResource attachmentResourceClient;

    /**
     * @param uri to lookup from Viestintapalvelu's AttacmentResource
     * @return the attachment with contents for the URI or null
     */
    public EmailAttachment getEmailAttachmentByUri(String uri) {
        try {
            logger.warn("Calling external interface AttachmentResource.downloadByUri");
            return attachmentResourceClient.downloadByUri(uri);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ExternalInterfaceException("AttachmentComponent.getEmailAttachmentByUri(uri="+uri+") failed.", e);
        }
    }

    /**
     * @param uris to mark downloaded (delete them from database)
     */
    public void markDownloaded(List<String> uris) {
        try {
            attachmentResourceClient.deleteByUris(new UrisContainerDto(uris));
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ExternalInterfaceException("AttachmentComponent.markDownloaded(uris="+uris+") failed.", e);
        }
    }

    public void setAttachmentResource(AttachmentResource attachmentResource) {
        this.attachmentResourceClient = attachmentResource;
    }
}
