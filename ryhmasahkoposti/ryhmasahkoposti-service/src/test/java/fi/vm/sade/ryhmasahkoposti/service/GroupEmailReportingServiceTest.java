package fi.vm.sade.ryhmasahkoposti.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import fi.vm.sade.authentication.model.OrganisaatioHenkilo;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.AttachmentResponse;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailAttachment;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailMessage;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailMessageDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipient;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipientDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.OrganizationDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.PagingAndSortingDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedMessageDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedMessagesDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.SendingStatusDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.query.ReportedMessageQueryDTO;
import fi.vm.sade.ryhmasahkoposti.common.util.MessageUtil;
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
import fi.vm.sade.ryhmasahkoposti.service.impl.GroupEmailReportingServiceImpl;
import fi.vm.sade.ryhmasahkoposti.testdata.RaportointipalveluTestData;


@RunWith(PowerMockRunner.class)
@ContextConfiguration("/test-bundle-context.xml")
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, 
	DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class})
@PrepareForTest({MessageUtil.class, ReportedMessageConverter.class, ReportedRecipientConverter.class})
public class GroupEmailReportingServiceTest {
	private GroupEmailReportingService groupEmailReportingService;
	@Mock
	FileItem mockedFileItem;
	@Mock
	private ReportedMessageService mockedReportedMessageService;
	@Mock
	private ReportedRecipientService mockedReportedRecipientService;
	@Mock
	private ReportedAttachmentService mockedReportedAttachmentService;
	@Mock
	private ReportedMessageAttachmentService mockedReportedMessageAttachmentService;
	@Mock
	private ReportedMessageConverter mockedReportedMessageConverter;
	@Mock
	private ReportedRecipientConverter mockedReportedRecipientConverter;
	@Mock
	private ReportedAttachmentConverter mockedReportedAttachmentConverter;
	@Mock
	private EmailMessageDTOConverter mockedEmailMessageDTOConverter;
	@Mock
	private EmailRecipientDTOConverter mockedEmailRecipientDTOConverter;
	@Mock
	private ReportedMessageDTOConverter mockedReportedMessageDTOConverter;
	@Mock
	private CurrentUserComponent mockedCurrentUserComponent;
	@Mock
	private OrganizationComponent mockedOrganizationComponent;
	
	@Before
	public void setup() {	    
		groupEmailReportingService = new GroupEmailReportingServiceImpl(mockedReportedMessageService, 
		    mockedReportedRecipientService,	mockedReportedAttachmentService, mockedReportedMessageAttachmentService, 
		    mockedReportedMessageConverter, mockedReportedRecipientConverter, mockedReportedAttachmentConverter, 
		    mockedEmailMessageDTOConverter, mockedEmailRecipientDTOConverter, mockedReportedMessageDTOConverter, 
		    mockedCurrentUserComponent, mockedOrganizationComponent);	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddSendingGroupEmail() throws IOException {
	    when(mockedReportedMessageConverter.convert(any(EmailMessage.class))).thenReturn(new ReportedMessage());
	    
		ReportedMessage savedReportedMessage = RaportointipalveluTestData.getReportedMessage();
		savedReportedMessage.setId(new Long(2));
		savedReportedMessage.setVersion(new Long(0));
		when(mockedReportedMessageService.saveReportedMessage(any(ReportedMessage.class))).thenReturn(savedReportedMessage);
		
		List<ReportedAttachment> reportedAttachments = new ArrayList<ReportedAttachment>();
		ReportedAttachment reportedAttachment = RaportointipalveluTestData.getReportedAttachment();
		reportedAttachment.setId(new Long(3));
		reportedAttachments.add(reportedAttachment);
		when(mockedReportedAttachmentService.getReportedAttachments(any(List.class))).thenReturn(reportedAttachments);
		
		doAnswer(new Answer<Object>() {
		    public Object answer(InvocationOnMock invocation) {
		        @SuppressWarnings("unused")
				Object[] args = invocation.getArguments();
		        return null;
		    }})
		.when(mockedReportedMessageAttachmentService).saveReportedMessageAttachments(
			any(ReportedMessage.class), any(List.class));
				
		doAnswer(new Answer<Object>() {
		    public Object answer(InvocationOnMock invocation) {
		        @SuppressWarnings("unused")
				Object[] args = invocation.getArguments();
		        return null;
		    }})
		.when(mockedReportedRecipientService).saveReportedRecipients(any(Set.class));

		when(mockedReportedRecipientConverter.convert(any(ReportedMessage.class), 
		    eq(new ArrayList<EmailRecipient>()))).thenReturn(new HashSet<ReportedRecipient>());
		
		EmailData emailData = RaportointipalveluTestData.getEmailData();
		
		EmailMessage emailMessage = RaportointipalveluTestData.getEmailMessage();
	
		List<EmailRecipient> emailRecipients = new ArrayList<EmailRecipient>();
		EmailRecipient emailRecipient = RaportointipalveluTestData.getEmailRecipient();
		emailRecipient.setEmail("testLahetyksenTulosVastaaRaportoitujatietoja@sposti.fi");
		emailRecipients.add(emailRecipient);
		
		List<EmailAttachment> emailAttachments = new ArrayList<EmailAttachment>();
		EmailAttachment emailAttachment = RaportointipalveluTestData.getEmailAttachment();
		emailAttachments.add(emailAttachment);
		
		List<AttachmentResponse> attachmentResponses = new ArrayList<AttachmentResponse>();
		AttachmentResponse attachmentResponse = 
			RaportointipalveluTestData.getAttachmentResponse(new Long(1), mockedFileItem);
		attachmentResponses.add(attachmentResponse);
		
		emailMessage.setAttachments(emailAttachments);
		emailMessage.setAttachInfo(attachmentResponses);
		
		emailData.setEmail(emailMessage);
		emailData.setRecipient(emailRecipients);

		Long messageID = groupEmailReportingService.addSendingGroupEmail(emailData);		
		
		assertNotNull(messageID);
	}

	@Test
	public void testSaveAttachment() throws IOException {
		byte[] sisalto = {'k', 'o', 'e', 'k', 'u', 't', 's', 'u'};
		
		when(mockedFileItem.getName()).thenReturn("Koekutsu");
		when(mockedFileItem.getContentType()).thenReturn("application/pdf");
		when(mockedFileItem.get()).thenReturn(sisalto);
		
		when(mockedReportedAttachmentService.saveReportedAttachment(any(ReportedAttachment.class))).thenReturn(new Long(1));
		
		Long liiteID = groupEmailReportingService.saveAttachment(mockedFileItem);
		
		assertNotNull(liiteID);
		assertTrue(liiteID.longValue() > 0);
	}

	@Test
	public void testStartSending() {
		ReportedRecipient reportedRecipient = RaportointipalveluTestData.getReportedRecipient();		
		when(mockedReportedRecipientService.getReportedRecipient(any(Long.class))).thenReturn(reportedRecipient);
		
		doAnswer(new Answer<Object>() {
		    public Object answer(InvocationOnMock invocation) {
		        @SuppressWarnings("unused")
				Object[] args = invocation.getArguments();
		        return null;
		    }})
		.when(mockedReportedRecipientService).updateReportedRecipient(any(ReportedRecipient.class));
		
		EmailRecipientDTO recipient = RaportointipalveluTestData.getEmailRecipientDTO();
		boolean succesful = groupEmailReportingService.startSending(recipient);
		
		assertTrue(succesful);
	}

	@Test
	public void testRecipientHandledSuccess() {
		ReportedRecipient reportedRecipient = RaportointipalveluTestData.getReportedRecipient();		
		when(mockedReportedRecipientService.getReportedRecipient(any(Long.class))).thenReturn(reportedRecipient);
		
		doAnswer(new Answer<Object>() {
		    public Object answer(InvocationOnMock invocation) {
		        @SuppressWarnings("unused")
				Object[] args = invocation.getArguments();
		        return null;
		    }})
		.when(mockedReportedRecipientService).updateReportedRecipient(any(ReportedRecipient.class));

		EmailRecipientDTO emailRecipientDTO = RaportointipalveluTestData.getEmailRecipientDTO();
		groupEmailReportingService.recipientHandledSuccess(emailRecipientDTO, "Lähetys ok");
	}
	
	@Test
	public void testGetUnhandledMessageRecipients() {
		ReportedMessage reportedMessage = RaportointipalveluTestData.getReportedMessage();
		reportedMessage.setId(new Long(1));
		reportedMessage.setVersion(new Long(0));
		
		List<ReportedRecipient> reportedRecipients = new ArrayList<ReportedRecipient>();
		ReportedRecipient reportedRecipient = RaportointipalveluTestData.getReportedRecipient();
		reportedRecipient.setReportedMessage(reportedMessage);
		reportedRecipients.add(reportedRecipient);
		
		List<EmailRecipientDTO> mockedEmailRecipientDTOs = new ArrayList<EmailRecipientDTO>();
		mockedEmailRecipientDTOs.add(RaportointipalveluTestData.getEmailRecipientDTO());
				
		when(mockedReportedRecipientService.getUnhandledReportedRecipients(1)).thenReturn(reportedRecipients);
		when(mockedEmailRecipientDTOConverter.convert(reportedRecipients)).thenReturn(mockedEmailRecipientDTOs);
		
		List<EmailRecipientDTO> emailRecipientDTOs = groupEmailReportingService.getUnhandledMessageRecipients(1);
		
		assertNotNull(emailRecipientDTOs);
		assertTrue(emailRecipientDTOs.size() == 1);
	}
	
    @Test
    @SuppressWarnings("unchecked")
	public void testGetMessage() {
		ReportedMessage reportedMessage = RaportointipalveluTestData.getReportedMessage();
		reportedMessage.setId(new Long(1));
		reportedMessage.setVersion(new Long(0));
		
		Set<ReportedRecipient> reportedRecipients = new HashSet<ReportedRecipient>();
		ReportedRecipient reportedRecipient = RaportointipalveluTestData.getReportedRecipient();
		reportedRecipient.setReportedMessage(reportedMessage);
		reportedRecipients.add(reportedRecipient);
		
		reportedMessage.setReportedRecipients(reportedRecipients);
		
		List<ReportedAttachment> reportedAttachments = new ArrayList<ReportedAttachment>();
		reportedAttachments.add(RaportointipalveluTestData.getReportedAttachment());
		
		EmailMessageDTO mockedEmailMessageDTO = RaportointipalveluTestData.getEmailMessageDTO();
		
		when(mockedReportedMessageService.getReportedMessage(any(Long.class))).thenReturn(reportedMessage);
		when(mockedEmailMessageDTOConverter.convert(any(ReportedMessage.class), any(List.class))).thenReturn(
		    mockedEmailMessageDTO);
		
		EmailMessageDTO emailMessageDTO = groupEmailReportingService.getMessage(new Long(1));
		
		assertNotNull(emailMessageDTO);
		assertNotNull(emailMessageDTO.getMessageID());
		assertTrue(emailMessageDTO.getMessageID().equals(new Long(1)));
	}
	
	@Test
	public void testGetSendingStatus() {	
		ReportedMessage reportedMessage = RaportointipalveluTestData.getReportedMessage();
		reportedMessage.setId(new Long(1));
		
		SendingStatusDTO sendingStatusDTO = RaportointipalveluTestData.getSendingStatusDTO();
		
		when(mockedReportedMessageService.getReportedMessage(any(Long.class))).thenReturn(reportedMessage);
		when(mockedReportedRecipientService.getSendingStatusOfRecipients(any(Long.class))).thenReturn(sendingStatusDTO);
		
		SendingStatusDTO sendingStatus = groupEmailReportingService.getSendingStatus(new Long(1));

		assertNotNull(sendingStatus);
		assertNotNull(sendingStatus.getMessageID());
		assertNotNull(sendingStatus.getNumberOfReciepients());
		assertTrue(sendingStatus.getNumberOfReciepients().equals(new Long(10)));
		assertNotNull(sendingStatus.getNumberOfSuccesfulSendings());
		assertTrue(sendingStatus.getNumberOfSuccesfulSendings().equals(new Long(5)));
		assertNotNull(sendingStatus.getNumberOfFailedSendings());
		assertTrue(sendingStatus.getNumberOfFailedSendings().equals(new Long(2)));
		assertNull(sendingStatus.getSendingEnded());
	}
	
	@Test
	public void testGetSendingStatusWhenSendingHasEnded() {	
		ReportedMessage reportedMessage = RaportointipalveluTestData.getReportedMessage();
		reportedMessage.setId(new Long(1));
		
		SendingStatusDTO sendingStatusDTO = RaportointipalveluTestData.getSendingStatusDTO();
		sendingStatusDTO.setNumberOfSuccesfulSendings(new Long(8));
		sendingStatusDTO.setSendingEnded(new Date());
		
		when(mockedReportedMessageService.getReportedMessage(any(Long.class))).thenReturn(reportedMessage);
		when(mockedReportedRecipientService.getSendingStatusOfRecipients(any(Long.class))).thenReturn(sendingStatusDTO);
		when(mockedReportedRecipientService.getLatestReportedRecipientsSendingEndedDate(any(Long.class))).thenReturn(new Date());
		
		SendingStatusDTO sendingStatus = groupEmailReportingService.getSendingStatus(new Long(1));

		assertNotNull(sendingStatus);
		assertNotNull(sendingStatus.getMessageID());
		assertNotNull(sendingStatus.getNumberOfReciepients());
		assertTrue(sendingStatus.getNumberOfReciepients().equals(new Long(10)));
		assertNotNull(sendingStatus.getNumberOfSuccesfulSendings());
		assertTrue(sendingStatus.getNumberOfSuccesfulSendings().equals(new Long(8)));
		assertNotNull(sendingStatus.getNumberOfFailedSendings());
		assertTrue(sendingStatus.getNumberOfFailedSendings().equals(new Long(2)));
		assertNotNull(sendingStatus.getSendingEnded());
	}
	
    @SuppressWarnings("unchecked")
    @Test
	public void testGetReportedMessagesByOrganizationOid() {       
	    PowerMockito.mockStatic(MessageUtil.class);
        PowerMockito.when(MessageUtil.getMessage("ryhmasahkoposti.lahetys_epaonnistui", 
            new Object[] {new Long(2)})).thenReturn("Lahetyksiä epäonnistui");
		
		List<ReportedMessage> mockedReportedMessages = new ArrayList<ReportedMessage>();
		ReportedMessage reportedMessage = RaportointipalveluTestData.getReportedMessage();
		reportedMessage.setId(new Long(1));
		reportedMessage.setVersion(new Long(0));
		
		Set<ReportedRecipient> reportedRecipients = new HashSet<ReportedRecipient>();
		ReportedRecipient reportedRecipient = RaportointipalveluTestData.getReportedRecipient();
		reportedRecipient.setReportedMessage(reportedMessage);
		reportedRecipients.add(reportedRecipient);

		reportedMessage.setReportedRecipients(reportedRecipients);
		mockedReportedMessages.add(reportedMessage);
		
        PagingAndSortingDTO pagingAndSorting = RaportointipalveluTestData.getPagingAndSortingDTO();
        pagingAndSorting.setSortedBy("sendingStarted");
		
		when(mockedReportedMessageService.getReportedMessages(any(String.class), 
		    any(PagingAndSortingDTO.class))).thenReturn(mockedReportedMessages);
		
		SendingStatusDTO sendingStatus = RaportointipalveluTestData.getSendingStatusDTO();
		when(mockedReportedRecipientService.getSendingStatusOfRecipients(any(Long.class))).thenReturn(sendingStatus);
			
		List<ReportedMessageDTO> mockedReportedMessageDTOs = new ArrayList<ReportedMessageDTO>();
		mockedReportedMessageDTOs.add(RaportointipalveluTestData.getReportedMessageDTO());
		when(mockedReportedMessageDTOConverter.convert(any(List.class), any(Map.class))).thenReturn(mockedReportedMessageDTOs);

		ReportedMessagesDTO reportedMessagesDTO = 
		    groupEmailReportingService.getReportedMessagesByOrganizationOid("1.2.246.562.10.00000000001", pagingAndSorting);
		
		assertNotNull(reportedMessagesDTO);
		assertTrue(reportedMessagesDTO.getReportedMessages().size() == 1);
		assertNotNull(reportedMessagesDTO.getReportedMessages().get(0).getStatusReport());
		assertTrue(reportedMessagesDTO.getReportedMessages().get(0).getStatusReport().equalsIgnoreCase("Lahetyksiä epäonnistui"));
	}
	
	@SuppressWarnings("unchecked")
    @Test
	public void testGetReportedMessagesBySearchArgument() {
	    ReportedMessageQueryDTO query = RaportointipalveluTestData.getReportedMessageQueryDTO();
	    
        PowerMockito.mockStatic(MessageUtil.class);
        when(MessageUtil.getMessage(any(String.class))).thenReturn("lahetys kesken");

		List<ReportedMessage> mockedReportedMessages = new ArrayList<ReportedMessage>();
		ReportedMessage reportedMessage = RaportointipalveluTestData.getReportedMessage();
		reportedMessage.setId(new Long(1));
		reportedMessage.setVersion(new Long(0));
		
		Set<ReportedRecipient> reportedRecipients = new HashSet<ReportedRecipient>();
		ReportedRecipient reportedRecipient = RaportointipalveluTestData.getReportedRecipient();
		reportedRecipient.setReportedMessage(reportedMessage);
		reportedRecipients.add(reportedRecipient);

		reportedMessage.setReportedRecipients(reportedRecipients);
		mockedReportedMessages.add(reportedMessage);
		
		when(mockedReportedMessageService.getReportedMessages(
			any(ReportedMessageQueryDTO.class), any(PagingAndSortingDTO.class))).thenReturn(mockedReportedMessages);

        SendingStatusDTO sendingStatus = RaportointipalveluTestData.getSendingStatusDTO();
        when(mockedReportedRecipientService.getSendingStatusOfRecipients(any(Long.class))).thenReturn(sendingStatus);
	    
        PagingAndSortingDTO pagingAndSorting = RaportointipalveluTestData.getPagingAndSortingDTO();
        pagingAndSorting.setSortedBy("sendingStarted");

        List<ReportedMessageDTO> mockedReportedMessageDTOs = new ArrayList<ReportedMessageDTO>();
        mockedReportedMessageDTOs.add(RaportointipalveluTestData.getReportedMessageDTO());
        when(mockedReportedMessageDTOConverter.convert(any(List.class), any(Map.class))).thenReturn(mockedReportedMessageDTOs);

		ReportedMessagesDTO reportedMessagesDTO = 
			groupEmailReportingService.getReportedMessages(query, pagingAndSorting);
		
		assertNotNull(reportedMessagesDTO);
		assertTrue(reportedMessagesDTO.getReportedMessages().size() == 1);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetReportedMessageWithReports() {		
		ReportedMessage reportedMessage = RaportointipalveluTestData.getReportedMessage();
		reportedMessage.setId(new Long(1));
		reportedMessage.setVersion(new Long(0));
		
		Set<ReportedRecipient> reportedRecipients = new HashSet<ReportedRecipient>();
		ReportedRecipient reportedRecipient = RaportointipalveluTestData.getReportedRecipient();
		reportedRecipient.setReportedMessage(reportedMessage);
		reportedRecipients.add(reportedRecipient);

		reportedMessage.setReportedRecipients(reportedRecipients);

		when(mockedReportedMessageService.getReportedMessage(
			any(Long.class))).thenReturn(reportedMessage);

		SendingStatusDTO sendingStatusDTO = RaportointipalveluTestData.getSendingStatusDTO();
		when(mockedReportedRecipientService.getSendingStatusOfRecipients(any(Long.class))).thenReturn(
			sendingStatusDTO);
		
		PowerMockito.mockStatic(MessageUtil.class);
		PowerMockito.when(MessageUtil.getMessage("ryhmasahkoposti.lahetys_raportti", 
			new Object[]{new Long(5), new Long(2)})).thenReturn("2 lahetystä epäonnistui");
		PowerMockito.when(MessageUtil.getMessage("ryhmasahkoposti.lahetys_epaonnistui", 
			new Object[] {new Long(2)})).thenReturn("Lahetyksiä epäonnistui");
		
		when(mockedReportedAttachmentService.getReportedAttachments(any(Set.class))).thenReturn(
			new ArrayList<ReportedAttachment>());
		
		ReportedMessageDTO mockedReportedMessageDTO = RaportointipalveluTestData.getReportedMessageDTO();
		when(mockedReportedMessageDTOConverter.convert(any(ReportedMessage.class), any(List.class), 
		    any(SendingStatusDTO.class))).thenReturn(mockedReportedMessageDTO);
		
		ReportedMessageDTO reportedMessageDTO = 
			groupEmailReportingService.getReportedMessage(new Long(1));
		
		assertNotNull(reportedMessageDTO);
		assertTrue(reportedMessageDTO.getMessageID().equals(new Long(1)));
		assertNotNull(reportedMessageDTO.getStatusReport());
		assertNotNull(reportedMessageDTO.getSendingReport());
	}
	
	@Test
	public void testGetUserOrganization() {
	    List<OrganisaatioHenkilo> henkilonOrganisaatiot = RaportointipalveluTestData.getHenkilonOrganisaatiot();
	    when(mockedCurrentUserComponent.getCurrentUserOrganizations()).thenReturn(henkilonOrganisaatiot);	    
	    when(mockedOrganizationComponent.getOrganization(any(String.class))).thenReturn(
	        RaportointipalveluTestData.getOrganisaatioRDTO());
	    when(mockedOrganizationComponent.getNameOfOrganisation(any(OrganisaatioRDTO.class))).thenReturn("OPH");
	    
	    List<OrganizationDTO> organizationDTOs = groupEmailReportingService.getUserOrganizations();
	    
	    assertNotNull(organizationDTOs);
	    assertTrue(organizationDTOs.size() == 1);
	    assertTrue(organizationDTOs.get(0).getOid().equals("1.2.246.562.10.00000000001"));
	    assertTrue(organizationDTOs.get(0).getName().equalsIgnoreCase("OPH"));
	}
}