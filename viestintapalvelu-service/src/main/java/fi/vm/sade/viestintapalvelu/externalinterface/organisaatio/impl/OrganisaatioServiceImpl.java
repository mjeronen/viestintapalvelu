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
package fi.vm.sade.viestintapalvelu.externalinterface.organisaatio.impl;

import static org.joda.time.DateTime.now;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.viestintapalvelu.externalinterface.api.dto.OrganisaatioHierarchyDto;
import fi.vm.sade.viestintapalvelu.externalinterface.component.OrganizationComponent;
import fi.vm.sade.viestintapalvelu.externalinterface.organisaatio.OrganisaatioService;
import fi.vm.sade.viestintapalvelu.recovery.Recoverer;
import fi.vm.sade.viestintapalvelu.recovery.RecovererPriority;

/**
 * User: ratamaa
 * Date: 29.10.2014
 * Time: 14:52
 */
@Service
@Singleton
@RecovererPriority(100)
public class OrganisaatioServiceImpl implements OrganisaatioService ,Recoverer {
    private static final Logger logger = LoggerFactory.getLogger(OrganisaatioServiceImpl.class);

    @Autowired
    private OrganizationComponent organizationComponent;

    private Period cacheValid = Period.days(1);
    private final AtomicInteger sync = new AtomicInteger(1);
    private volatile DateTime cacheCreatedAt;
    private volatile Map<String,OrganisaatioHierarchyDto> hierarchyByOids;
    private volatile OrganisaatioHierarchyDto root;
    private static final HierarchyVisitor<String> EXTRACT_OID = new HierarchyVisitor<String>() {
        public String visit(OrganisaatioHierarchyDto dto, OrganisaatioHierarchyDto parent) {
            return dto.getOid();
        }
    };

    public OrganisaatioHierarchyDto getOrganizationHierarchy(String organizationOid) {
        ensureCacheFresh();
        OrganisaatioHierarchyDto hierarchyDto = this.hierarchyByOids.get(organizationOid);
        if(hierarchyDto == null)
            return null;
        /*
        Be sure to return a copy in case the dto is somehow modified later on.
        This prevents corruption of the cache.
         */
        return new OrganisaatioHierarchyDto(hierarchyDto);
    }

    @Override
    public List<String> findHierarchyOids(OrganisaatioHierarchyDto hierarchyDto) {
        ensureCacheFresh();
        return visitDown(hierarchyDto, null, EXTRACT_OID, new ArrayList<String>());
    }

    @Override
    public List<String> findHierarchyOids(String organisaatioOid) {
        ensureCacheFresh();
        return visitDown(this.hierarchyByOids.get(organisaatioOid), null, EXTRACT_OID,
                new ArrayList<String>());
    }

    @Override
    public List<String> findAllChildrenOids(String organisaatioOid) {
        ensureCacheFresh();
        return visitChildren(this.hierarchyByOids.get(organisaatioOid), EXTRACT_OID,
                new ArrayList<String>());
    }

    @Override
    public List<String> findParentOids(String organisaatioOid) {
        ensureCacheFresh();
        List<String> results = new ArrayList<>();
        visitParents(this.hierarchyByOids.get(organisaatioOid), EXTRACT_OID, results);
        Collections.reverse(results); // the root first
        return results;
    }
    
    @Override
    public String getOrganizationName(String organisaatioOid, String languageCode) {
        OrganisaatioRDTO organization = organizationComponent.getOrganization(organisaatioOid);
        if (StringUtils.isBlank(organization.getNimi().get(languageCode.toLowerCase()))) {
            return organizationComponent.getNameOfOrganisation(organization);           
        }
        return organization.getNimi().get(languageCode.toLowerCase());
    }

    public void ensureCacheFresh() {
        DateTime now = now();
        // volatile double locking:
        if (cacheCreatedAt == null || cacheCreatedAt.plus(cacheValid).isBefore(now)) {
            synchronized (sync) {
                if (cacheCreatedAt == null || cacheCreatedAt.plus(cacheValid).isBefore(now)) {
                    // only one process at a time should get here (but without extra synchronization)
                    refrechCache();
                }
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * MON-FRI")
    public void scheduledRefresh() {
        refrechCache();
    }

    // Run at application startup
    @Override
    public Runnable getTask() {
        return new Runnable() {
            @Override
            public void run() {
                ensureCacheFresh();
            }
        };
    }

    private void refrechCache() {
        logger.info("Refreshing organisaatiohierachy cache.");
        this.root = organizationComponent.getOrganizationHierarchy();
        this.hierarchyByOids = new HashMap<>();
        visitDown(this.root, null, new HierarchyVisitor<Void>() {
            public Void visit(OrganisaatioHierarchyDto dto, OrganisaatioHierarchyDto parent) {
                dto.setParent(parent);
                hierarchyByOids.put(dto.getOid(), dto);
                return null;
            }
        }, new ArrayList<Void>());
        this.cacheCreatedAt = new DateTime();
        this.sync.incrementAndGet();
        logger.info("Organisaatiohierachy cache refreshed with {} OIDs.", this.hierarchyByOids.size());
    }

    private interface HierarchyVisitor<T> {
        T visit(OrganisaatioHierarchyDto dto, OrganisaatioHierarchyDto parent);
    }

    protected<T, C extends Collection<T>> C visitDown(OrganisaatioHierarchyDto item,
                                                      OrganisaatioHierarchyDto parent,
                                                      HierarchyVisitor<T> visitor, C results) {
        T result = visitor.visit(item, parent);
        if (result != null) {
            results.add(result);
        }
        visitChildren(item, visitor, results);
        return results;
    }

    protected <T, C extends Collection<T>> C visitChildren(OrganisaatioHierarchyDto item,
                                                           HierarchyVisitor<T> visitor, C results) {
        for (OrganisaatioHierarchyDto child : item.getChildren()) {
            if (child != null) {
                visitDown(child, item, visitor, results);
            }
        }
        return results;
    }

    protected <T, C extends Collection<T>> C visitUp(OrganisaatioHierarchyDto item,
                                                     HierarchyVisitor<T> visitor, C results) {
        T result = visitor.visit(item, item.getParent());
        if (result != null) {
            results.add(result);
        }
        visitParents(item, visitor, results);
        return results;
    }

    protected <T, C extends Collection<T>> C visitParents(OrganisaatioHierarchyDto item,
                                                          HierarchyVisitor<T> visitor, C results) {
        if (item.getParent() != null) {
            visitUp(item.getParent(), visitor, results);
        }
        return results;
    }

}
