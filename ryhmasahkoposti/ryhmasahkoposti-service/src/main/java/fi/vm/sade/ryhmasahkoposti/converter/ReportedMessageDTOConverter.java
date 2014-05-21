package fi.vm.sade.ryhmasahkoposti.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import fi.vm.sade.ryhmasahkoposti.api.dto.EmailAttachment;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailAttachmentDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipientDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedMessageDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.SendingStatusDTO;
import fi.vm.sade.ryhmasahkoposti.common.util.MessageUtil;
import fi.vm.sade.ryhmasahkoposti.model.ReportedAttachment;
import fi.vm.sade.ryhmasahkoposti.model.ReportedMessage;
import fi.vm.sade.ryhmasahkoposti.model.ReportedRecipient;

@Component
public class ReportedMessageDTOConverter {

	public List<ReportedMessageDTO> convert(List<ReportedMessage> reportedMessages) {
		List<ReportedMessageDTO> reportedMessageDTOs = new ArrayList<ReportedMessageDTO>();
		
		for (ReportedMessage reportedMessage : reportedMessages) {
			ReportedMessageDTO reportedMessageDTO = new ReportedMessageDTO();			
			convert(reportedMessageDTO, reportedMessage);
			reportedMessageDTOs.add(reportedMessageDTO);
		}
		
		return reportedMessageDTOs;
	}

	public List<ReportedMessageDTO> convert(List<ReportedMessage> reportedMessages, 
		Map<Long, SendingStatusDTO> sendingStatuses) {
		List<ReportedMessageDTO> reportedMessageDTOs = new ArrayList<ReportedMessageDTO>();
		
		for (ReportedMessage reportedMessage : reportedMessages) {
			ReportedMessageDTO reportedMessageDTO = new ReportedMessageDTO();			
			
			convert(reportedMessageDTO, reportedMessage);
			SendingStatusDTO sendingStatus = sendingStatuses.get(reportedMessage.getId());
			reportedMessageDTO.setSendingStatus(sendingStatus);
			setStatusReport(reportedMessageDTO, sendingStatus.getNumberOfFailedSendings());
			
			reportedMessageDTOs.add(reportedMessageDTO);
		}
		
		return reportedMessageDTOs;
	}
	
	public ReportedMessageDTO convert(ReportedMessage reportedMessage, 
		List<ReportedAttachment> reportedAttachments, SendingStatusDTO sendingStatusDTO) {
		ReportedMessageDTO reportedMessageDTO = new ReportedMessageDTO();
		
		convert(reportedMessageDTO, reportedMessage);
		
		List<ReportedRecipient> reportedRecipients = 
		    new ArrayList<ReportedRecipient>(reportedMessage.getReportedRecipients());
		reportedMessageDTO.setEmailRecipients(convertEmailRecipientDTO(reportedRecipients));
		
		reportedMessageDTO.setAttachments(convertEmailAttachmentDTO(reportedAttachments));
		reportedMessageDTO.setSendingStatus(sendingStatusDTO);
		
		setSendingReport(reportedMessageDTO, sendingStatusDTO);
		setStatusReport(reportedMessageDTO, sendingStatusDTO.getNumberOfFailedSendings());
				
		return reportedMessageDTO;
	}

   public ReportedMessageDTO convert(ReportedMessage reportedMessage, List<ReportedRecipient> reportedRecipients, 
        List<ReportedAttachment> reportedAttachments, SendingStatusDTO sendingStatusDTO) {
        ReportedMessageDTO reportedMessageDTO = new ReportedMessageDTO();
        
        convert(reportedMessageDTO, reportedMessage);
        reportedMessageDTO.setEmailRecipients(convertEmailRecipientDTO(reportedRecipients));
        reportedMessageDTO.setAttachments(convertEmailAttachmentDTO(reportedAttachments));
        reportedMessageDTO.setSendingStatus(sendingStatusDTO);
        
        setSendingReport(reportedMessageDTO, sendingStatusDTO);
        setStatusReport(reportedMessageDTO, sendingStatusDTO.getNumberOfFailedSendings());
                
        return reportedMessageDTO;
    }

	private void convert(ReportedMessageDTO reportedMessageDTO, ReportedMessage reportedMessage) {
		reportedMessageDTO.setMessageID(reportedMessage.getId());
		reportedMessageDTO.setSubject(reportedMessage.getSubject());
		reportedMessageDTO.setSenderName(reportedMessage.getSenderName());
		reportedMessageDTO.setFrom(reportedMessage.getSenderEmail());
		reportedMessageDTO.setStartTime(reportedMessage.getSendingStarted());
		reportedMessageDTO.setEndTime(reportedMessage.getSendingEnded());
		reportedMessageDTO.setCallingProcess(reportedMessage.getProcess());
		reportedMessageDTO.setReplyTo(reportedMessage.getReplyToEmail());
		reportedMessageDTO.setBody(reportedMessage.getMessage());
	}

	private List<EmailRecipientDTO> convertEmailRecipientDTO(List<ReportedRecipient> reportedRecipients) {
		List<EmailRecipientDTO> recipients = new ArrayList<EmailRecipientDTO>();
		
		for (ReportedRecipient reportedRecipient : reportedRecipients) {
			EmailRecipientDTO emailRecipientDTO = new EmailRecipientDTO();
			
			emailRecipientDTO.setRecipientID(reportedRecipient.getId());			
			emailRecipientDTO.setSendSuccessfull(reportedRecipient.getSendingSuccesful());
			emailRecipientDTO.setOid(reportedRecipient.getRecipientOid());
			emailRecipientDTO.setEmail(reportedRecipient.getRecipientEmail());
			setRecipientName(emailRecipientDTO, reportedRecipient);
			
			recipients.add(emailRecipientDTO);
		}
		
		return recipients;
	}

	private List<EmailAttachment> convertEmailAttachmentDTO(List<ReportedAttachment> reportedAttachments) {
		List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
		
		for (ReportedAttachment reportedAttachment : reportedAttachments) {
			EmailAttachmentDTO attachmentDTO = new EmailAttachmentDTO();
			attachmentDTO.setAttachmentID(reportedAttachment.getId());
			attachmentDTO.setName(reportedAttachment.getAttachmentName());
			attachmentDTO.setData(reportedAttachment.getAttachment());
			attachmentDTO.setContentType(reportedAttachment.getContentType());
			attachments.add(attachmentDTO);
		}
			
		return attachments;
	}

	private void setSendingReport(ReportedMessageDTO reportedMessageDTO, SendingStatusDTO sendingStatusDTO) {
		Long numberOfSuccesfulSendings = new Long(0);
		if (sendingStatusDTO.getNumberOfSuccesfulSendings() != null) {
			numberOfSuccesfulSendings = sendingStatusDTO.getNumberOfSuccesfulSendings();
		}
		
		Long numberOfFailedSendings = new Long(0);
		if (sendingStatusDTO.getNumberOfFailedSendings() != null) {
			numberOfFailedSendings = sendingStatusDTO.getNumberOfFailedSendings();
		}

		Object[] parameters = {numberOfSuccesfulSendings, numberOfFailedSendings};
		reportedMessageDTO.setSendingReport(
			MessageUtil.getMessage("ryhmasahkoposti.lahetys_raportti", parameters));
	}

	private void setStatusReport(ReportedMessageDTO reportedMessageDTO, Long numberOfFailed) {
		if (numberOfFailed != null && numberOfFailed.compareTo(new Long(0)) > 0) {
			Object[] parameters = {numberOfFailed};
			reportedMessageDTO.setStatusReport(
				MessageUtil.getMessage("ryhmasahkoposti.lahetys_epaonnistui", parameters));
			return;
		}
		
		if (reportedMessageDTO.getStartTime() != null && reportedMessageDTO.getEndTime() == null) {
			reportedMessageDTO.setStatusReport(MessageUtil.getMessage("ryhmasahkoposti.lahetys_kesken"));
			return;
		}
		
		if (reportedMessageDTO.getStartTime() != null && reportedMessageDTO.getEndTime() != null) {
			reportedMessageDTO.setStatusReport(MessageUtil.getMessage("ryhmasahkoposti.lahetys_onnistui"));
		}
		  
		return;
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
