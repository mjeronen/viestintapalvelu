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
package fi.vm.sade.viestintapalvelu.model;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import fi.vm.sade.generic.model.BaseEntity;

/**
 * @author migar1
 */
@Table(name = "kirjelahetys", schema = "kirjeet")
@Entity(name = "LetterBatch")
@NamedQueries({ @NamedQuery(name = "letterBatchStatus", query = "select new fi.vm.sade.viestintapalvelu.dao.dto.LetterBatchStatusDto(lb.id, "
        + " (select count(ltr1.id) from LetterBatch batch1 " + "       inner join batch1.letterReceivers receiver1"
        + "       inner join receiver1.letterReceiverLetter ltr1" + "       with ltr1.letter != null" + "   where batch1.id = lb.id), "
        + " (select count(ltr2.id) from LetterBatch batch2 " + "       inner join batch2.letterReceivers receiver2"
        + "       inner join receiver2.letterReceiverLetter ltr2" + "   where batch2.id = lb.id)," + " lb.batchStatus,"
        + " (select count(ltr3.id) from LetterBatch batch3 " + "       inner join batch3.letterReceivers receiver3 "
        + "       inner join receiver3.letterReceiverLetter ltr3 " + "               with ltr3.letter != null"
        + "   where batch3.id = lb.id and length(receiver3.emailAddress) > 0 ))" + "from LetterBatch lb " + "where lb.id = :batchId ") })
public class LetterBatch extends BaseEntity {
    public enum Status {
        created, processing, waiting_for_ipost_processing, processing_ipost, ready, // possible
                                                                                    // iPost
                                                                                    // ready
                                                                                    // and
                                                                                    // document
                                                                                    // in
                                                                                    // dokumenttipalvelu
        error
    }

    private static final long serialVersionUID = 1L;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "haku")
    private String applicationPeriod;

    @Column(name = "hakukohde")
    private String fetchTarget;

    @Column(name = "tunniste")
    private String tag;

    @Column(name = "aikaleima", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "kielikoodi", nullable = false)
    private String language;

    @Column(name = "oid_tallentaja", nullable = true)
    private String storingOid;

    @Column(name = "oid_organisaatio", nullable = true)
    private String organizationOid;

    @Column(name = "iposti")
    private boolean iposti;

    @Column(name = "kasittelyn_tila")
    @Enumerated(EnumType.STRING)
    private Status batchStatus = Status.created;

    @Column(name = "kasittely_aloitettu")
    @Temporal(TemporalType.TIMESTAMP)
    private Date handlingStarted;

    @Column(name = "kasittely_valmis")
    @Temporal(TemporalType.TIMESTAMP)
    private Date handlingFinished;

    @Column(name = "ipost_kasittely_aloitettu")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ipostHandlingStarted;

    @Column(name = "ipost_kasittely_valmis")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ipostHandlingFinished;

    @Column(name = "email_kasittely_aloitettu")
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailHandlingStarted;

    @Column(name = "email_kasittely_valmis")
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailHandlingFinished;

    @Column(name = "ohita_dokumenttipalvelu")
    private boolean skipDokumenttipalvelu;

    @OneToMany(mappedBy = "letterBatch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<LetterReplacement> letterReplacements;

    @OneToMany(mappedBy = "letterBatch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<LetterBatchProcessingError> processingErrors = new HashSet<>();

    @OneToMany(mappedBy = "letterBatch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<LetterReceivers> letterReceivers;

    @OneToMany(mappedBy = "letterBatch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<IPosti> iposts = new ArrayList<>();

    @OneToMany(mappedBy = "letterBatch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UsedTemplate> usedTemplates = new HashSet<>();

    public void setIposti(boolean iposti) {
        this.iposti = iposti;
    }

    public boolean isIposti() {
        return iposti;
    }

    public List<IPosti> getIposti() {
        return iposts;
    }

    public void addIPosti(IPosti iposti) {
        iposts.add(iposti);
    }

    public Set<UsedTemplate> getUsedTemplates() {
        return usedTemplates;
    }

    public void addUsedTemplate(UsedTemplate template) {
        usedTemplates.add(template);
    }

    public void setUsedTemplates(Set<UsedTemplate> usedTemplates) {
        this.usedTemplates = usedTemplates;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getApplicationPeriod() {
        return applicationPeriod;
    }

    public void setApplicationPeriod(String applicationPeriod) {
        this.applicationPeriod = applicationPeriod;
    }

    public String getFetchTarget() {
        return fetchTarget;
    }

    public void setFetchTarget(String fetchTarget) {
        this.fetchTarget = fetchTarget;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStoringOid() {
        return storingOid;
    }

    public void setStoringOid(String storingOid) {
        this.storingOid = storingOid;
    }

    public String getOrganizationOid() {
        return organizationOid;
    }

    public void setOrganizationOid(String organizationOid) {
        this.organizationOid = organizationOid;
    }

    public Set<LetterReplacement> getLetterReplacements() {
        return letterReplacements;
    }

    public void setLetterReplacements(Set<LetterReplacement> letterReplacements) {
        this.letterReplacements = letterReplacements;
    }

    public Set<LetterReceivers> getLetterReceivers() {
        return letterReceivers;
    }

    public void setLetterReceivers(Set<LetterReceivers> letterReceivers) {
        this.letterReceivers = letterReceivers;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public Date getHandlingFinished() {
        return handlingFinished;
    }

    public void setHandlingFinished(Date handlingFinished) {
        this.handlingFinished = handlingFinished;
    }

    public Date getHandlingStarted() {
        return handlingStarted;
    }

    public void setHandlingStarted(Date handlingStarted) {
        this.handlingStarted = handlingStarted;
    }

    public Date getEmailHandlingStarted() {
        return emailHandlingStarted;
    }

    public void setEmailHandlingStarted(Date emailHandlingStarted) {
        this.emailHandlingStarted = emailHandlingStarted;
    }

    public Date getEmailHandlingFinished() {
        return emailHandlingFinished;
    }

    public void setEmailHandlingFinished(Date emailHandlingFinished) {
        this.emailHandlingFinished = emailHandlingFinished;
    }

    public Status getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(Status batchStatus) {
        this.batchStatus = batchStatus;
    }

    public Set<LetterBatchProcessingError> getProcessingErrors() {
        return processingErrors;
    }

    protected void setProcessingErrors(Set<LetterBatchProcessingError> processingErrors) {
        this.processingErrors = processingErrors;
    }

    public void addProcessingErrors(LetterBatchProcessingError processingError) {
        processingErrors.add(processingError);
    }

    public Date getIpostHandlingStarted() {
        return ipostHandlingStarted;
    }

    public void setIpostHandlingStarted(Date ipostHandlingStarted) {
        this.ipostHandlingStarted = ipostHandlingStarted;
    }

    public Date getIpostHandlingFinished() {
        return ipostHandlingFinished;
    }

    public void setIpostHandlingFinished(Date ipostHandlingFinished) {
        this.ipostHandlingFinished = ipostHandlingFinished;
    }

    public boolean getSkipDokumenttipalvelu() {
        return skipDokumenttipalvelu;
    }

    public void setSkipDokumenttipalvelu(boolean skipDokumenttipalvelu) {
        this.skipDokumenttipalvelu = skipDokumenttipalvelu;
    }

    @Override
    public String toString() {
        return "LetterBatch [templateId=" + templateId + ", templateName=" + templateName + ", applicationPeriod=" + applicationPeriod + ", fetchTarget="
                + fetchTarget + ", timestamp=" + timestamp + ", language=" + language + ", storingOid=" + storingOid + ", organizationOid=" + organizationOid
                + ", skipDokumenttipalvelu=" + skipDokumenttipalvelu + "]";
    }

    public String toStringForLogging() {
        return "haku=" + applicationPeriod + ", hakukohde=" + fetchTarget + ", kieli=" + language
                + ", pohjan nimi=" + templateName + ", kirjeitä=" + ( null == letterReceivers ? 0 : letterReceivers.size() ) + " kpl";
    }
}
