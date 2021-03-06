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
package fi.vm.sade.ryhmasahkoposti.converter;

import fi.vm.sade.ryhmasahkoposti.api.dto.*;
import fi.vm.sade.ryhmasahkoposti.common.util.MessageUtil;
import fi.vm.sade.ryhmasahkoposti.model.ReportedAttachment;
import fi.vm.sade.ryhmasahkoposti.model.ReportedMessage;
import fi.vm.sade.ryhmasahkoposti.model.ReportedRecipient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ReportedMessageDTOConverter {

    public List<ReportedMessageDTO> convert(List<ReportedMessage> reportedMessages) {
        List<ReportedMessageDTO> reportedMessageDTOs = new ArrayList<>();

        for (ReportedMessage reportedMessage : reportedMessages) {
            ReportedMessageDTO reportedMessageDTO = new ReportedMessageDTO();
            convert(reportedMessageDTO, reportedMessage);
            reportedMessageDTOs.add(reportedMessageDTO);
        }

        return reportedMessageDTOs;
    }

    public List<ReportedMessageDTO> convert(List<ReportedMessage> reportedMessages, Map<Long, SendingStatusDTO> sendingStatuses) {
        List<ReportedMessageDTO> reportedMessageDTOs = new ArrayList<>();

        for (ReportedMessage reportedMessage : reportedMessages) {
            ReportedMessageDTO reportedMessageDTO = new ReportedMessageDTO();

            convert(reportedMessageDTO, reportedMessage);
            SendingStatusDTO sendingStatus = sendingStatuses.get(reportedMessage.getId());
            reportedMessageDTO.setSendingStatus(sendingStatus);
            setStatusReport(reportedMessageDTO, sendingStatus);

            reportedMessageDTOs.add(reportedMessageDTO);
        }

        return reportedMessageDTOs;
    }

    public ReportedMessageDTO convert(ReportedMessage reportedMessage, List<ReportedAttachment> reportedAttachments, SendingStatusDTO sendingStatusDTO) {
        List<ReportedRecipient> reportedRecipients = new ArrayList<>(reportedMessage.getReportedRecipients());
        return createReportedMessageDTO(reportedMessage, reportedRecipients, reportedAttachments, sendingStatusDTO);
    }

    public ReportedMessageDTO convert(ReportedMessage reportedMessage, List<ReportedRecipient> reportedRecipients,
            List<ReportedAttachment> reportedAttachments, SendingStatusDTO sendingStatusDTO) {
        return createReportedMessageDTO(reportedMessage, reportedRecipients, reportedAttachments, sendingStatusDTO);
    }

    private ReportedMessageDTO createReportedMessageDTO(ReportedMessage reportedMessage, List<ReportedRecipient> reportedRecipients, List<ReportedAttachment> reportedAttachments, SendingStatusDTO sendingStatusDTO) {
        ReportedMessageDTO reportedMessageDTO = new ReportedMessageDTO();
        convert(reportedMessageDTO, reportedMessage);
        reportedMessageDTO.setEmailRecipients(convertEmailRecipientDTO(reportedRecipients));
        reportedMessageDTO.setAttachments(convertEmailAttachmentDTO(reportedAttachments));
        reportedMessageDTO.setSendingStatus(sendingStatusDTO);
        setSendingReport(reportedMessageDTO, sendingStatusDTO);
        setStatusReport(reportedMessageDTO, sendingStatusDTO);
        return reportedMessageDTO;
    }

    private void convert(ReportedMessageDTO reportedMessageDTO, ReportedMessage reportedMessage) {
        reportedMessageDTO.setMessageID(reportedMessage.getId());
        reportedMessageDTO.setSubject(reportedMessage.getSubject());
        reportedMessageDTO.setSenderName(reportedMessage.getSenderDisplayText());
        reportedMessageDTO.setFrom(reportedMessage.getSenderEmail());
        reportedMessageDTO.setStartTime(reportedMessage.getSendingStarted());
        reportedMessageDTO.setEndTime(reportedMessage.getSendingEnded());
        reportedMessageDTO.setCallingProcess(reportedMessage.getProcess());
        reportedMessageDTO.setReplyTo(reportedMessage.getReplyToEmail());
        reportedMessageDTO.setBody(reportedMessage.getMessage());
    }

    private List<EmailRecipientDTO> convertEmailRecipientDTO(List<ReportedRecipient> reportedRecipients) {
        List<EmailRecipientDTO> recipients = new ArrayList<>();

        for (ReportedRecipient reportedRecipient : reportedRecipients) {
            EmailRecipientDTO emailRecipientDTO = EmailRecipientDTOConverter.initEmailRecipientDTO(reportedRecipient);
            emailRecipientDTO.setSendSuccessful(reportedRecipient.getSendingSuccessful());
            setRecipientName(emailRecipientDTO, reportedRecipient);
            recipients.add(emailRecipientDTO);
        }

        return recipients;
    }

    private List<EmailAttachment> convertEmailAttachmentDTO(List<ReportedAttachment> reportedAttachments) {
        List<EmailAttachment> attachments = new ArrayList<>();

        for (ReportedAttachment reportedAttachment : reportedAttachments) {
            EmailAttachmentDTO attachmentDTO = new EmailAttachmentDTO();
            EmailMessageDTOConverter.initEmailAttachmentDTO(reportedAttachment, attachmentDTO);
            attachments.add(attachmentDTO);
        }
        return attachments;
    }

    private void setSendingReport(ReportedMessageDTO reportedMessageDTO, SendingStatusDTO sendingStatusDTO) {
        Long numberOfSuccessfulSendings = 0L;
        if (sendingStatusDTO.getNumberOfSuccessfulSendings() != null) {
            numberOfSuccessfulSendings = sendingStatusDTO.getNumberOfSuccessfulSendings();
        }

        Long numberOfFailedSendings = 0L;
        if (sendingStatusDTO.getNumberOfFailedSendings() != null) {
            numberOfFailedSendings = sendingStatusDTO.getNumberOfFailedSendings();
        }

        Long numberOfBouncedSendings = 0L;
        if (sendingStatusDTO.getNumberOfBouncedSendings() != null) {
            numberOfBouncedSendings = sendingStatusDTO.getNumberOfBouncedSendings();
        }

        Object[] parameters = { numberOfSuccessfulSendings, numberOfFailedSendings, numberOfBouncedSendings };
        reportedMessageDTO.setSendingReport(MessageUtil.getMessage("ryhmasahkoposti.lahetys_raportti", parameters));
    }

    private void setStatusReport(ReportedMessageDTO reportedMessageDTO, SendingStatusDTO sendingStatus) {
        if (sendingStatus.getNumberOfRecipients() != null && sendingStatus.getNumberOfSuccessfulSendings() != null) {
            if (sendingStatus.getNumberOfRecipients().compareTo(sendingStatus.getNumberOfSuccessfulSendings()) == 0) {
                reportedMessageDTO.setStatusReport(MessageUtil.getMessage("ryhmasahkoposti.lahetys_onnistui"));
                return;
            }
        }

        if (sendingStatus.getNumberOfRecipients() != null && sendingStatus.getNumberOfSuccessfulSendings() != null) {
            if (sendingStatus.getNumberOfRecipients().compareTo(sendingStatus.getNumberOfSuccessfulSendings()) > 0) {
                reportedMessageDTO.setStatusReport(MessageUtil.getMessage("ryhmasahkoposti.lahetys_kesken"));
                return;
            }
        }

        if (sendingStatus.getNumberOfFailedSendings() != null && sendingStatus.getNumberOfFailedSendings().compareTo(0L) > 0) {
            Object[] parameters = { sendingStatus.getNumberOfFailedSendings() };
            reportedMessageDTO.setStatusReport(MessageUtil.getMessage("ryhmasahkoposti.lahetys_epaonnistui", parameters));
        }
    }

    private void setRecipientName(EmailRecipientDTO emailRecipient, ReportedRecipient reportedRecipient) {
        int position = reportedRecipient.getSearchName().indexOf(",");

        if (position == -1) {
            emailRecipient.setLastName(reportedRecipient.getSearchName());
            emailRecipient.setFirstName("");

            return;
        }

        emailRecipient.setLastName(reportedRecipient.getSearchName().substring(0, position));
        emailRecipient.setFirstName(reportedRecipient.getSearchName().substring(++position));
    }
}
