package fi.vm.sade.externalinterface;

import fi.vm.sade.dto.HenkiloDto;
import fi.vm.sade.generic.rest.CachingRestClient;

import java.io.IOException;

public class OppijanumeroRekisteriRestClient extends CachingRestClient {
    private final String baseUrl;

    public OppijanumeroRekisteriRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public HenkiloDto getHenkilo(String henkiloOid) throws IOException {
        String url = baseUrl + "/henkilo/" + henkiloOid;
        return this.get(url, HenkiloDto.class);
    }
}
