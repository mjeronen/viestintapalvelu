package fi.vm.sade.ryhmasahkoposti.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.authentication.model.OrganisaatioHenkilo;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.ryhmasahkoposti.api.constants.GroupEmailConstants;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailMessageDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipientDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.OrganizationDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.PagingAndSortingDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedMessageDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedMessagesDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.SendingStatusDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.query.ReportedMessageQueryDTO;
import fi.vm.sade.ryhmasahkoposti.converter.EmailMessageDTOConverter;
import fi.vm.sade.ryhmasahkoposti.converter.EmailRecipientDTOConverter;
import fi.vm.sade.ryhmasahkoposti.converter.ReportedAttachmentConverter;
import fi.vm.sade.ryhmasahkoposti.converter.ReportedMessageConverter;
import fi.vm.sade.ryhmasahkoposti.converter.ReportedMessageDTOConverter;
import fi.vm.sade.ryhmasahkoposti.converter.ReportedRecipientConverter;
import fi.vm.sade.ryhmasahkoposti.externalinterface.component.CurrentUserComponent;
import fi.vm.sade.ryhmasahkoposti.externalinterface.component.OrganizationComponent;
import fi.vm.sade.ryhmasahkoposti.model.ReportedAttachment;
import fi.vm.sade.ryhmasahkoposti.model.ReportedMessage;
import fi.vm.sade.ryhmasahkoposti.model.ReportedRecipient;
import fi.vm.sade.ryhmasahkoposti.service.GroupEmailReportingService;
import fi.vm.sade.ryhmasahkoposti.service.ReportedAttachmentService;
import fi.vm.sade.ryhmasahkoposti.service.ReportedMessageAttachmentService;
import fi.vm.sade.ryhmasahkoposti.service.ReportedMessageService;
import fi.vm.sade.ryhmasahkoposti.service.ReportedRecipientService;

@Service
@Transactional(readOnly=true)
public class GroupEmailReportingServiceImpl implements GroupEmailReportingService {
    private static Logger LOGGER = LoggerFactory.getLogger(GroupEmailReportingServiceImpl.class);
	private ReportedMessageService reportedMessageService;
	private ReportedRecipientService reportedRecipientService;
	private ReportedAttachmentService reportedAttachmentService;
	private ReportedMessageAttachmentService reportedMessageAttachmentService;
	private ReportedMessageConverter reportedMessageConverter;
	private ReportedRecipientConverter reportedRecipientConverter;
	private ReportedAttachmentConverter reportedAttachmentConverter;
	private EmailMessageDTOConverter emailMessageDTOConverter;
	private EmailRecipientDTOConverter emailRecipientDTOConverter;
	private ReportedMessageDTOConverter reportedMessageDTOConverter;
	private CurrentUserComponent currentUserComponent;
	private OrganizationComponent organizationComponent;

	@Autowired
	public GroupEmailReportingServiceImpl(ReportedMessageService reportedMessageService,
		ReportedRecipientService reportedRecipientService, ReportedAttachmentService reportedAttachmentService, 
		ReportedMessageAttachmentService reportedMessageAttachmentService, 
		ReportedMessageConverter reportedMessageConverter, ReportedRecipientConverter reportedRecipientConverter,
		ReportedAttachmentConverter reportedAttachmentConverter, EmailMessageDTOConverter emailMessageDTOConverter, 
		EmailRecipientDTOConverter emailRecipientDTOConverter, ReportedMessageDTOConverter reportedMessageDTOConverter, 
		CurrentUserComponent currentUserComponent, OrganizationComponent organizationComponent) {
		this.reportedMessageService = reportedMessageService;
		this.reportedRecipientService = reportedRecipientService;
		this.reportedAttachmentService = reportedAttachmentService;
		this.reportedMessageAttachmentService = reportedMessageAttachmentService;
		this.reportedMessageConverter = reportedMessageConverter;
		this.reportedRecipientConverter = reportedRecipientConverter;
		this.reportedAttachmentConverter = reportedAttachmentConverter;
		this.emailMessageDTOConverter = emailMessageDTOConverter;
		this.emailRecipientDTOConverter = emailRecipientDTOConverter;
		this.reportedMessageDTOConverter = reportedMessageDTOConverter;
		this.currentUserComponent = currentUserComponent;
		this.organizationComponent = organizationComponent;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public Long addSendingGroupEmail(EmailData emailData) throws IOException {
	    LOGGER.info("addSendingGroupEmail called");
	    
		ReportedMessage reportedMessage = reportedMessageConverter.convert(emailData.getEmail());			
		ReportedMessage savedReportedMessage = reportedMessageService.saveReportedMessage(reportedMessage);
	
		List<ReportedAttachment> reportedAttachments = reportedAttachmentService.getReportedAttachments(
			emailData.getEmail().getAttachInfo());		
		reportedMessageAttachmentService.saveReportedMessageAttachments(savedReportedMessage, reportedAttachments);
	
		Set<ReportedRecipient> reportedRecipients = reportedRecipientConverter.convert(
			savedReportedMessage, emailData.getRecipient());
		reportedRecipientService.saveReportedRecipients(reportedRecipients);		
		
		return savedReportedMessage.getId();
	}

    @Override
	public EmailMessageDTO getMessage(Long messageID) {
	    LOGGER.info("getMessage(" + messageID + ") called");

		ReportedMessage reportedMessage = reportedMessageService.getReportedMessage(messageID);
		List<ReportedAttachment> reportedAttachments = 
			reportedAttachmentService.getReportedAttachments(reportedMessage.getReportedMessageAttachments());
		return emailMessageDTOConverter.convert(reportedMessage, reportedAttachments);
	}

    @Override
	public ReportedMessageDTO getReportedMessage(Long messageID) {
        LOGGER.info("getReportedMessage(" + messageID + ") called");
	    
		ReportedMessage reportedMessage = reportedMessageService.getReportedMessage(messageID);
				
		SendingStatusDTO sendingStatus = reportedRecipientService.getSendingStatusOfRecipients(messageID);
		
		List<ReportedAttachment> reportedAttachments = 
			reportedAttachmentService.getReportedAttachments(reportedMessage.getReportedMessageAttachments());
		
		ReportedMessageDTO reportedMessageDTO = 
			reportedMessageDTOConverter.convert(reportedMessage, reportedAttachments, sendingStatus);
		reportedMessageDTO.setEndTime(sendingStatus.getSendingEnded());
		
		return reportedMessageDTO;
	}
	
	@Override
    public ReportedMessageDTO getReportedMessageAndRecipients(Long messageID, PagingAndSortingDTO pagingAndSorting) {
        LOGGER.info("getReportedMessageAndRecipients(" + messageID + ") called");

        ReportedMessage reportedMessage = reportedMessageService.getReportedMessage(messageID);
        
        List<ReportedRecipient> reportedRecipients = reportedRecipientService.getReportedRecipients(
            messageID, pagingAndSorting);
        
        SendingStatusDTO sendingStatus = reportedRecipientService.getSendingStatusOfRecipients(messageID);
        
        List<ReportedAttachment> reportedAttachments = 
            reportedAttachmentService.getReportedAttachments(reportedMessage.getReportedMessageAttachments());
        
        ReportedMessageDTO reportedMessageDTO = reportedMessageDTOConverter.convert(reportedMessage, reportedRecipients, 
            reportedAttachments, sendingStatus);
        reportedMessageDTO.setEndTime(sendingStatus.getSendingEnded());
        
        return reportedMessageDTO;
    }

	@Override
    public ReportedMessageDTO getReportedMessageAndRecipientsSendingUnsuccesful(Long messageID, 
        PagingAndSortingDTO pagingAndSorting) {
        LOGGER.info("getReportedMessageAndRecipientsSendingUnsuccesful(" + messageID + ") called");

        ReportedMessage reportedMessage = reportedMessageService.getReportedMessage(messageID);
        
        List<ReportedRecipient> reportedRecipients = 
            reportedRecipientService.getReportedRecipientsByStatusSendingUnsuccesful(messageID, pagingAndSorting);
        
        SendingStatusDTO sendingStatus = reportedRecipientService.getSendingStatusOfRecipients(messageID);
        
        List<ReportedAttachment> reportedAttachments = 
            reportedAttachmentService.getReportedAttachments(reportedMessage.getReportedMessageAttachments());
        
        ReportedMessageDTO reportedMessageDTO = reportedMessageDTOConverter.convert(reportedMessage, reportedRecipients, 
            reportedAttachments, sendingStatus);
        reportedMessageDTO.setEndTime(sendingStatus.getSendingEnded());
        
        return reportedMessageDTO;        
    }
    
	@Override
	public ReportedMessagesDTO getReportedMessagesByOrganizationOid(String organizationOid, 
	    PagingAndSortingDTO pagingAndSorting) {
        LOGGER.info("getReportedMessagesByOrganizationOid(String, PagingAndSortingDTO) called");

	    ReportedMessagesDTO reportedMessagesDTO = new ReportedMessagesDTO();

		List<ReportedMessage> reportedMessages = reportedMessageService.getReportedMessages(organizationOid, pagingAndSorting);
		Long numberOfReportedMessages = reportedMessageService.getNumberOfReportedMessages(organizationOid);
		
		Map<Long, SendingStatusDTO> sendingStatuses = new HashMap<Long, SendingStatusDTO>();
		for (ReportedMessage reportedMessage : reportedMessages) {
			SendingStatusDTO sendingStatus = reportedRecipientService.getSendingStatusOfRecipients(reportedMessage.getId());
			sendingStatuses.put(reportedMessage.getId(), sendingStatus);
		}
		
		List<ReportedMessageDTO> listOfReportedMessageDTO = 
		    reportedMessageDTOConverter.convert(reportedMessages, sendingStatuses);
		
		reportedMessagesDTO.setReportedMessages(listOfReportedMessageDTO);
		reportedMessagesDTO.setNumberOfReportedMessages(numberOfReportedMessages);

		return reportedMessagesDTO;
	}

	@Override
	public ReportedMessagesDTO getReportedMessages(ReportedMessageQueryDTO query, PagingAndSortingDTO pagingAndSorting) {
        LOGGER.info("getReportedMessages(ReportedMessageQueryDTO query, PagingAndSortingDTO pagingAndSorting) called");
	    ReportedMessagesDTO reportedMessagesDTO = new ReportedMessagesDTO();
	       
		List<ReportedMessage> reportedMessages = reportedMessageService.getReportedMessages(query, pagingAndSorting);
		Long numberOfReportedMessages = reportedMessageService.getNumberOfReportedMessages(query);
		
        Map<Long, SendingStatusDTO> sendingStatuses = new HashMap<Long, SendingStatusDTO>();
        for (ReportedMessage reportedMessage : reportedMessages) {
            SendingStatusDTO sendingStatus = reportedRecipientService.getSendingStatusOfRecipients(reportedMessage.getId());
            sendingStatuses.put(reportedMessage.getId(), sendingStatus);
        }
        
        List<ReportedMessageDTO> listOfReportedMessageDTO = 
            reportedMessageDTOConverter.convert(reportedMessages, sendingStatuses);
        
        reportedMessagesDTO.setReportedMessages(listOfReportedMessageDTO);
        reportedMessagesDTO.setNumberOfReportedMessages(numberOfReportedMessages);

        return reportedMessagesDTO;        
	}

	@Override
	public SendingStatusDTO getSendingStatus(Long messageID) {
        LOGGER.info("getSendingStatus(" + messageID + ") called");

		ReportedMessage reportedMessage = reportedMessageService.getReportedMessage(messageID);
				
		SendingStatusDTO sendingStatus = reportedRecipientService.getSendingStatusOfRecipients(messageID);
		sendingStatus.setMessageID(messageID);
		sendingStatus.setSendingStarted(reportedMessage.getSendingStarted());

		return sendingStatus;
	}
	
	@Override
	public List<EmailRecipientDTO> getUnhandledMessageRecipients(int listSize) {
        LOGGER.info("getUnhandledMessageRecipients(" + listSize + ") called");

		List<ReportedRecipient> reportedRecipients = reportedRecipientService.getUnhandledReportedRecipients(listSize);
		return emailRecipientDTOConverter.convert(reportedRecipients);
	}

	@Override
	public List<OrganizationDTO> getUserOrganizations() {
        List<OrganizationDTO> organizations = new ArrayList<OrganizationDTO>();
        List<OrganisaatioHenkilo> organisaatioHenkiloList = currentUserComponent.getCurrentUserOrganizations();
        
        for (OrganisaatioHenkilo organisaatioHenkilo : organisaatioHenkiloList) {
            OrganizationDTO organization = new OrganizationDTO();
            
            OrganisaatioRDTO organisaatioRDTO = 
                organizationComponent.getOrganization(organisaatioHenkilo.getOrganisaatioOid());
            String organizationName = organizationComponent.getNameOfOrganisation(organisaatioRDTO);
            
            organization.setOid(organisaatioRDTO.getOid());
            organization.setName(organizationName);
            
            organizations.add(organization);            
        }
        
        return organizations;
    }

    @Override
	@Transactional(propagation=Propagation.REQUIRED)
	public boolean recipientHandledFailure(EmailRecipientDTO recipient, String result) {
        LOGGER.info("recipientHandledFailure(" + recipient.getRecipientID() + ") called");

		ReportedRecipient reportedRecipient = 
			reportedRecipientService.getReportedRecipient(recipient.getRecipientID());
		reportedRecipient.setFailureReason(result);
		reportedRecipient.setSendingSuccesful(GroupEmailConstants.SENDING_FAILED);
		reportedRecipient.setSendingEnded(new Date());
		reportedRecipientService.updateReportedRecipient(reportedRecipient);
		return true;
	}
    
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public boolean recipientHandledSuccess(EmailRecipientDTO recipient, String result) {
        LOGGER.info("recipientHandledSuccess(" + recipient.getRecipientID() + ") called");

	    ReportedRecipient reportedRecipient = 
			reportedRecipientService.getReportedRecipient(recipient.getRecipientID());
		reportedRecipient.setSendingSuccesful(GroupEmailConstants.SENDING_SUCCESFUL);
		reportedRecipient.setSendingEnded(new Date());
		
		reportedRecipientService.updateReportedRecipient(reportedRecipient);
		
		return true;
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public Long saveAttachment(FileItem fileItem) throws IOException {
        LOGGER.info("saveAttachment(" + fileItem.getName() + ") called");

		ReportedAttachment reportedAttachment = reportedAttachmentConverter.convert(fileItem);
		return reportedAttachmentService.saveReportedAttachment(reportedAttachment);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public boolean startSending(EmailRecipientDTO recipient) {
        LOGGER.info("startSending(" + recipient.getEmail() + ") called");

	    ReportedRecipient reportedRecipient = 
			reportedRecipientService.getReportedRecipient(recipient.getRecipientID());

		if (reportedRecipient.getSendingStarted() != null) {
			return false;
		}

		reportedRecipient.setSendingStarted(new Date());
		reportedRecipientService.updateReportedRecipient(reportedRecipient);
		
		return true;
	}

}