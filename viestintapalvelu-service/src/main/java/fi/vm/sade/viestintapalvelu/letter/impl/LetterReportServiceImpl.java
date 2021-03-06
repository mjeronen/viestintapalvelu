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
package fi.vm.sade.viestintapalvelu.letter.impl;

import fi.vm.sade.dto.HenkiloDto;
import fi.vm.sade.dto.OrganisaatioHenkiloDto;
import java.util.ArrayList;
import java.util.List;

import fi.vm.sade.dto.PagingAndSortingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.viestintapalvelu.dao.IPostiDAO;
import fi.vm.sade.viestintapalvelu.dao.LetterBatchDAO;
import fi.vm.sade.viestintapalvelu.dao.LetterReceiverLetterDAO;
import fi.vm.sade.viestintapalvelu.dao.LetterReceiversDAO;
import fi.vm.sade.viestintapalvelu.dto.OrganizationDTO;
import fi.vm.sade.viestintapalvelu.dto.iposti.IPostiDTO;
import fi.vm.sade.viestintapalvelu.dto.letter.LetterBatchReportDTO;
import fi.vm.sade.viestintapalvelu.dto.letter.LetterBatchesReportDTO;
import fi.vm.sade.viestintapalvelu.dto.letter.LetterReceiverDTO;
import fi.vm.sade.viestintapalvelu.dto.query.LetterReportQueryDTO;
import fi.vm.sade.viestintapalvelu.externalinterface.component.CurrentUserComponent;
import fi.vm.sade.viestintapalvelu.externalinterface.component.HenkiloComponent;
import fi.vm.sade.viestintapalvelu.externalinterface.component.OrganizationComponent;
import fi.vm.sade.viestintapalvelu.externalinterface.organisaatio.OrganisaatioService;
import fi.vm.sade.viestintapalvelu.letter.LetterReportService;
import fi.vm.sade.viestintapalvelu.model.IPosti;
import fi.vm.sade.viestintapalvelu.model.LetterBatch;
import fi.vm.sade.viestintapalvelu.model.LetterReceivers;
import fi.vm.sade.viestintapalvelu.model.types.ContentStructureType;
import fi.vm.sade.viestintapalvelu.template.Template;
import fi.vm.sade.viestintapalvelu.template.TemplateService;

@Transactional(readOnly = true)
@Service
public class LetterReportServiceImpl implements LetterReportService {
    public static final long MAX_COUNT_FOR_LETTER_BATCH_SEARCH = 1000L;
    private LetterBatchDAO letterBatchDAO;
    private LetterReceiversDAO letterReceiversDAO;
    private LetterReceiverLetterDAO letterReceiverLetterDAO;
    private IPostiDAO iPostiDAO;
    private TemplateService templateService;
    private CurrentUserComponent currentUserComponent;
    private OrganizationComponent organizationComponent;
    private HenkiloComponent henkiloComponent;
    private OrganisaatioService organisaatioService;

    @Value("${viestintapalvelu.rekisterinpitajaOID}")
    private String rekisterinpitajaOID;

    @Autowired
    public LetterReportServiceImpl(LetterBatchDAO letterBatchDAO, LetterReceiversDAO letterReceiversDAO, LetterReceiverLetterDAO letterReceiverLetterDAO,
            IPostiDAO iPostiDAO, TemplateService templateService, CurrentUserComponent currentUserComponent, OrganizationComponent organizationComponent,
            HenkiloComponent henkiloComponent, OrganisaatioService organisaatioService) {
        this.letterBatchDAO = letterBatchDAO;
        this.letterReceiversDAO = letterReceiversDAO;
        this.letterReceiverLetterDAO = letterReceiverLetterDAO;
        this.iPostiDAO = iPostiDAO;
        this.templateService = templateService;
        this.currentUserComponent = currentUserComponent;
        this.organizationComponent = organizationComponent;
        this.henkiloComponent = henkiloComponent;
        this.organisaatioService = organisaatioService;
    }

    @Override
    public LetterBatchReportDTO getLetterBatchReport(Long letterBatchID, PagingAndSortingDTO pagingAndSorting, String query) {
        List<LetterReceivers> letterReceiverList = letterReceiversDAO.findLetterReceiversByLetterBatchID(letterBatchID, pagingAndSorting, query);

        Long numberOfReceivers = letterReceiversDAO.findNumberOfReciversByLetterBatchID(letterBatchID, query);

        if (numberOfReceivers == 0) {
            letterReceiverList = letterReceiversDAO.findLetterReceiversByLetterBatchID(letterBatchID, pagingAndSorting, null);
        }

        final LetterBatch letterBatch;
        if (letterReceiverList.size() > 0) {
            LetterReceivers letterReceivers = letterReceiverList.get(0);
            letterBatch = letterReceivers.getLetterBatch();
        } else {
            letterBatch = letterBatchDAO.read(letterBatchID);
        }
        LetterBatchReportDTO letterBatchReport = convertLetterBatchReport(letterBatch);

        letterBatchReport.setNumberOfReceivers(numberOfReceivers);
        if (numberOfReceivers > 0) {
            List<LetterReceiverDTO> letterReceiverDTOs = convertLetterReceiver(letterBatch, letterReceiverList);
            letterBatchReport.setLetterReceivers(letterReceiverDTOs);
        } else {
            letterBatchReport.setLetterReceivers(new ArrayList<LetterReceiverDTO>());
        }

        List<IPosti> iPostis = iPostiDAO.findByLetterBatchId(letterBatchID);
        List<IPostiDTO> iPostiDTOs = getListOfIPostiDTO(iPostis);
        letterBatchReport.setiPostis(iPostiDTOs);

        if (letterBatch.getStoringOid() != null) {
            HenkiloDto henkilo = henkiloComponent.getHenkilo(letterBatch.getStoringOid());
            letterBatchReport.setCreatorName(henkilo.getSukunimi() + ", " + henkilo.getEtunimet());
        } else {
            letterBatchReport.setCreatorName("" + ", " + "");
        }
        return letterBatchReport;
    }

    @Override
    public LetterBatchesReportDTO getLetterBatchesReport(LetterReportQueryDTO query, PagingAndSortingDTO pagingAndSorting) {
        List<LetterBatchReportDTO> letterBatches = letterBatchDAO.findLetterBatchesBySearchArgument(query, pagingAndSorting);

        LetterBatchesReportDTO letterBatchesReport = new LetterBatchesReportDTO();
        letterBatchesReport.setLetterBatchReports(letterBatches);
        letterBatchesReport.setMaxNumber(Math.max(MAX_COUNT_FOR_LETTER_BATCH_SEARCH, pagingAndSorting.getFromIndex() + pagingAndSorting.getNumberOfRows()));
        letterBatchesReport.setNumberOfLetterBatches(letterBatchDAO.findNumberOfLetterBatchesBySearchArgument(query, letterBatchesReport.getMaxNumber()));

        return letterBatchesReport;
    }

    @Override
    public LetterBatchesReportDTO getLetterBatchesReport(String organizationOID, PagingAndSortingDTO pagingAndSorting) {
        final List<LetterBatch> letterBatches;
        final long numberOfLetterBatches;
        if (organizationOID != null && organizationOID.equals(rekisterinpitajaOID)) {
            letterBatches = letterBatchDAO.findAll(pagingAndSorting);
            numberOfLetterBatches = letterBatchDAO.findNumberOfLetterBatches();
        } else {
            List<String> oids = organisaatioService.findHierarchyOids(organizationOID);
            letterBatches = letterBatchDAO.findLetterBatchesByOrganizationOid(oids, pagingAndSorting);
            numberOfLetterBatches = letterBatchDAO.findNumberOfLetterBatches(oids);
        }

        LetterBatchesReportDTO letterBatchesReport = convertLetterBatchesReport(letterBatches);
        letterBatchesReport.setNumberOfLetterBatches(numberOfLetterBatches);
        return letterBatchesReport;
    }

    @Override
    public List<OrganizationDTO> getUserOrganizations() {
        List<OrganizationDTO> organizations = new ArrayList<>();
        List<OrganisaatioHenkiloDto> organisaatioHenkiloList = currentUserComponent.getCurrentUserOrganizations();

        for (OrganisaatioHenkiloDto organisaatioHenkilo : organisaatioHenkiloList) {
            if (organisaatioHenkilo.isPassivoitu()) {
                continue;
            }
            OrganizationDTO organization = new OrganizationDTO();

            OrganisaatioRDTO organisaatioRDTO = organizationComponent.getOrganization(organisaatioHenkilo.getOrganisaatioOid());
            String organizationName = organizationComponent.getNameOfOrganisation(organisaatioRDTO);

            organization.setOid(organisaatioRDTO.getOid());
            organization.setName(organizationName);

            organizations.add(organization);
        }

        return organizations;
    }

    private List<IPostiDTO> getListOfIPostiDTO(List<IPosti> iPostis) {
        List<IPostiDTO> iPostiDTOList = new ArrayList<>();

        for (IPosti iPosti : iPostis) {
            IPostiDTO iPostiDTO = new IPostiDTO();
            iPostiDTO.setContent(iPosti.getContent());
            iPostiDTO.setContentName(iPosti.getContentName());
            iPostiDTO.setContentType(iPosti.getContentType());
            iPostiDTO.setId(iPosti.getId());
            iPostiDTO.setSentDate(iPosti.getSentDate());

            iPostiDTOList.add(iPostiDTO);
        }

        return iPostiDTOList;
    }

    private LetterBatchesReportDTO convertLetterBatchesReport(List<LetterBatch> letterBatches) {
        LetterBatchesReportDTO letterBatchesReport = new LetterBatchesReportDTO();

        List<LetterBatchReportDTO> letterBatchReports = new ArrayList<>();

        for (LetterBatch letterBatch : letterBatches) {
            LetterBatchReportDTO letterBatchReport = convertLetterBatchReport(letterBatch);
            letterBatchReports.add(letterBatchReport);
        }

        letterBatchesReport.setLetterBatchReports(letterBatchReports);

        return letterBatchesReport;
    }

    private LetterBatchReportDTO convertLetterBatchReport(LetterBatch letterBatch) {
        LetterBatchReportDTO letterBatchReport = new LetterBatchReportDTO();

        letterBatchReport.setTemplateName(letterBatch.getTemplateName());
        letterBatchReport.setApplicationPeriod(letterBatch.getApplicationPeriod());
        letterBatchReport.setDeliveryTypeIPosti(letterBatch.isIposti());
        letterBatchReport.setFetchTarget(letterBatch.getFetchTarget());
        letterBatchReport.setLetterBatchID(letterBatch.getId());
        letterBatchReport.setTag(letterBatch.getTag());
        letterBatchReport.setTimestamp(letterBatch.getTimestamp());
        letterBatchReport.setOrganisaatioOid(letterBatch.getOrganizationOid());

        Template template = templateService.findByIdAndState(letterBatch.getTemplateId(), ContentStructureType.letter, null);
        letterBatchReport.setTemplate(template);
        if (template != null) {
            letterBatchReport.setTemplateName(template.getName());
        }
        if (letterBatch.getBatchStatus() != null) {
            letterBatchReport.setStatus(letterBatch.getBatchStatus().name());
        }
        return letterBatchReport;
    }

    private List<LetterReceiverDTO> convertLetterReceiver(LetterBatch letterBatch, List<LetterReceivers> letterReceiverList) {
        List<LetterReceiverDTO> letterReceiverDTOs = new ArrayList<>();

        for (LetterReceivers letterReceivers : letterReceiverList) {
            LetterReceiverDTO letterReceiverDTO = new LetterReceiverDTO();

            letterReceiverDTO.setAddress1(letterReceivers.getLetterReceiverAddress().getAddressline());
            letterReceiverDTO.setAddress2(letterReceivers.getLetterReceiverAddress().getAddressline2());
            letterReceiverDTO.setCity(letterReceivers.getLetterReceiverAddress().getCity());
            letterReceiverDTO.setContentType(letterReceivers.getLetterReceiverLetter().getContentType());
            letterReceiverDTO.setCountry(letterReceivers.getLetterReceiverAddress().getCountry());
            letterReceiverDTO.setId(letterReceivers.getId());
            letterReceiverDTO.setLetterReceiverLetterID(letterReceivers.getLetterReceiverLetter().getId());
            letterReceiverDTO.setName(letterReceivers.getLetterReceiverAddress().getLastName() + ", "
                    + letterReceivers.getLetterReceiverAddress().getFirstName());
            letterReceiverDTO.setPostalCode(letterReceivers.getLetterReceiverAddress().getPostalCode());
            letterReceiverDTO.setRegion(letterReceivers.getLetterReceiverAddress().getRegion());
            letterReceiverDTO.setTemplateName(letterBatch.getTemplateName());

            letterReceiverDTOs.add(letterReceiverDTO);
        }
        return letterReceiverDTOs;
    }

}
