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
package fi.vm.sade.ryhmasahkoposti.config;

import fi.vm.sade.dto.HenkiloDto;
import fi.vm.sade.dto.OrganisaatioHenkiloDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.apache.http.HttpResponse;
import org.dom4j.DocumentException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailAttachment;
import fi.vm.sade.ryhmasahkoposti.api.dto.TemplateDTO;
import fi.vm.sade.ryhmasahkoposti.externalinterface.api.*;
import fi.vm.sade.ryhmasahkoposti.externalinterface.api.dto.OrganisaatioHierarchyResultsDto;
import fi.vm.sade.ryhmasahkoposti.service.DailyTaskRunner;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

/**
 * User: ratamaa
 * Date: 25.9.2014
 * Time: 12:38
 */
@Configuration
@ImportResource(value = {"classpath:test-bundle-context.xml"})
public class IntegrationTestConfig {

    public static class MailerStatus {
        private List<Message> messages = new ArrayList<Message>();

        public MailerStatus() {
        }

        public void addSentMessage(Message message) {
            this.messages.add(message);
        }

        public List<Message> getMessages() {
            return messages;
        }
    }

    private static MailerStatus mailerStatus = new MailerStatus();

    @Bean
    DailyTaskRunner systemStartupTasksService() {
        return new DailyTaskRunner() {
            @Override
            public void runDailyTasks() {

            }
        };
    }

    @Bean
    MailerStatus mailerStatus() {
        // TODO: Find a way to do this or similar thing (overriding static method) with newer
        // JMockito wihtout it's runner or PwoerMockito without it's runner (seems not possible)
        // (using SpringJUnit4ClassRunner runner)
        Mockit.setUpMock(TrasnportMock.class);
        return mailerStatus;
    }

    @MockClass(realClass = Transport.class)
    public static class TrasnportMock{
        @Mock
        public static void send(Message msg) throws MessagingException {
            mailerStatus.addSentMessage(msg);
        }

        @Mock
        public static void send(Message msg, Address[] addresses)
                throws MessagingException {
            mailerStatus.addSentMessage(msg);
        }
    }

    @Bean
    OrganisaatioResource organisaatioResourceStub() {
        return new OrganisaatioResource() {
            @Override
            public OrganisaatioRDTO getOrganisaatioByOID(String oid) {
                throw new IllegalStateException("Please mock me when needed!");
            }

            @Override
            public OrganisaatioHierarchyResultsDto hierarchy(boolean vainAktiiviset) throws Exception {
                throw new IllegalStateException("Please mock me when needed!");
            }
        };
    }

    @Bean
    AttachmentResource attachmentResourceStub() {
        return new AttachmentResource() {
            @Override
            public EmailAttachment downloadByUri(String uri) {
                throw new IllegalStateException("Please mock me when needed!");
            }

            @Override
            public HttpResponse deleteByUris(UrisContainerDto urisContainerDto) {
                throw new IllegalStateException("Please mock me when needed!");
            }
        };
    }

    @Bean
    TemplateResource templateResourceStub() {
        return new TemplateResource() {
            @Override
            public TemplateDTO getTemplateContent(String templateName, String languageCode, String type, String applicationPeriod) throws IOException, DocumentException {
                throw new IllegalStateException("Please mock me when needed!");
            }

            @Override
            public TemplateDTO getTemplateContent(String templateName, String languageCode, String type) throws IOException, DocumentException {
                throw new IllegalStateException("Please mock me when needed!");
            }
            
            @Override
            public TemplateDTO getTemplateByID(String templateId, String type) {
                throw new IllegalStateException("Please mock me when needed!");
            }
        };
    }

    @Bean
    ThreadPoolTaskExecutor emailExecutor() {
        return new ThreadPoolTaskExecutor();
    }
}
