/*
 * Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.ajastuspalvelu.service.external.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import fi.vm.sade.ajastuspalvelu.service.external.api.dto.HakuDetailsDto;

/**
 * User: ratamaa
 * Date: 31.10.2014
 * Time: 11:05
 */
@Path("options")
public interface ViestintapalveluOptionsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/hakus")
    @ApiOperation(value="Palauttaa julkaistut haut",
            responseContainer = "List", response = HakuDetailsDto.class)
    List<HakuDetailsDto> listHakus(
            @ApiParam("Pakota päivitys (vapaaehtoinen, ilman tätä vastaus voidaan ladata kätköstä)")
            @QueryParam("forceRefresh") Boolean forceRefresh);

}