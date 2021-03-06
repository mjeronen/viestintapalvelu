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
package fi.vm.sade.viestintapalvelu.letter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import fi.vm.sade.viestintapalvelu.letter.dto.LetterBatchDetails;
import fi.vm.sade.viestintapalvelu.template.Template;

@ApiModel(value = "Kerralla muodostettavien kirjeiden joukko")
public class LetterBatch implements LetterBatchDetails {
    @ApiModelProperty(value = "Kerralla muodostettavien kirjeiden joukko, (1-n)", required = true)
    private List<Letter> letters;

    @ApiModelProperty(value = "Kirjepohja")
    private Template template;

    @ApiModelProperty(value = "Kirjepohjan tunniste")
    private Long templateId;

    @ApiModelProperty(value = "Kirjeen yleiset personointikentät", required = false, notes = "")
    private Map<String, Object> templateReplacements;

    @ApiModelProperty(value = "Kirjepohjan tunniste/nimi")
    private String templateName;

    @ApiModelProperty(value = "Kielikoodi ISO 639-1, default = 'FI'")
    private String languageCode;

    @ApiModelProperty(value = "Tallentajan Oid")
    private String storingOid;

    @ApiModelProperty(value = "Organisaatio Oid")
    private String organizationOid;

    @ApiModelProperty(value = "Haku")
    private String applicationPeriod;

    @ApiModelProperty(value = "Hakukohde id")
    private String fetchTarget;

    @ApiModelProperty(value = "Vapaa teksti tunniste")
    private String tag;

    @ApiModelProperty(value = "Onko iposti-tyyppinen oletuksena ei iposti", required = false)
    private boolean iposti = false;

    @ApiModelProperty(value = "Ohitetaanko dokumentin tallennus dokumenttipalveluun", required = false)
    private boolean skipDokumenttipalvelu = false;

    private Map<String, byte[]> iPostiData = new LinkedHashMap<>();

    public Map<String, Object> getTemplateReplacements() {
        return templateReplacements;
    }

    public void setTemplateReplacements(Map<String, Object> templateReplacements) {
        this.templateReplacements = templateReplacements;
    }

    @Override
    public boolean isIposti() {
        return this.iposti;
    }

    public void setLetters(List<Letter> letters) {
        this.letters = letters;
    }

    public LetterBatch() {
    }

    public LetterBatch(List<Letter> letters) {
        this.letters = letters;
    }

    public List<Letter> getLetters() {
        return letters;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean isSkipDokumenttipalvelu() {
        return skipDokumenttipalvelu;
    }

    public void setSkipDokumenttipalvelu(boolean skipDokumenttipalvelu) {
        this.skipDokumenttipalvelu = skipDokumenttipalvelu;
    }

    public List<LetterBatch> split(int limit) {
        List<LetterBatch> batches = new ArrayList<>();
        split(letters, batches, limit);
        return batches;
    }

    private LetterBatch createSubBatch(List<Letter> lettersOfSubBatch) {
        LetterBatch result = new LetterBatch(lettersOfSubBatch);
        result.setLanguageCode(languageCode);
        result.setApplicationPeriod(applicationPeriod);
        result.setFetchTarget(fetchTarget);
        result.setOrganizationOid(organizationOid);
        result.setStoringOid(storingOid);
        result.setTemplate(template);
        result.setTemplateId(templateId);
        result.setTemplateName(templateName);
        result.setTemplateReplacements(templateReplacements);
        result.setTag(tag);
        return result;
    }

    private void split(List<Letter> remaining, List<LetterBatch> batches, int limit) {
        if (limit >= remaining.size()) {
            batches.add(createSubBatch(new ArrayList<>(remaining)));
        } else {
            batches.add(createSubBatch(new ArrayList<>(remaining.subList(0, limit))));
            split(remaining.subList(limit, remaining.size()), batches, limit);
        }
    }

    @Override
    public String toString() {
        return "LetterBatch [letters=" + letters + ", template=" + template + ", templateId=" + templateId + ", templateReplacements=" + templateReplacements
                + ", templateName=" + templateName + ", languageCode=" + languageCode + ", storingOid=" + storingOid + ", organizationOid=" + organizationOid
                + ", applicationPeriod=" + applicationPeriod + ", fetchTarget=" + fetchTarget + ", tag=" + tag + ", skipDokumenttipalvelu=" + skipDokumenttipalvelu + "]";
    }

    public Map<String, byte[]> getIPostiData() {
        return iPostiData;
    }

    public void addIPostiData(String name, byte[] content) {
        iPostiData.put(name, content);
    }

    public int getLetterCount() {
        return 0;
    }

    public int getSentLetterCount() {
        return 0;
    }
}
