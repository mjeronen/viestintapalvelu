package fi.vm.sade.ryhmasahkoposti.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.ryhmasahkoposti.api.dto.Draft;
import fi.vm.sade.ryhmasahkoposti.converter.DraftConverter;
import fi.vm.sade.ryhmasahkoposti.dao.DraftDAO;
import fi.vm.sade.ryhmasahkoposti.model.DraftModel;
import fi.vm.sade.ryhmasahkoposti.service.DraftService;

@Service
public class DraftServiceImpl implements DraftService {

    final Logger logger = LoggerFactory.getLogger(DraftServiceImpl.class);
    
    @Autowired
    private DraftDAO draftDao;
    
    @Autowired
    private DraftConverter draftConverter;
    
    @Override
    @Transactional
    public Draft getDraft(Long id) {
        DraftModel draftModel = draftDao.getDraft(id);
        return draftConverter.convert(draftModel);
    }

    @Override
    public List<Draft> getAllDrafts() {
        List<DraftModel> draftModels = draftDao.getAllDrafts();
        List<Draft> drafts = new ArrayList<Draft>();
        for(DraftModel draftModel : draftModels) {
            drafts.add(draftConverter.convert(draftModel));
        }
        return drafts;
    }

    @Override
    public Long getCount() {
        return draftDao.getCount();
    }

    @Override
    public Draft deleteDraft(Long id) {
        DraftModel draftModel = draftDao.deleteDraft(id);
        return draftConverter.convert(draftModel);
    }

    @Override
    @Transactional
    public String saveDraft(Draft draft) {
        DraftModel draftModel = draftConverter.convert(draft);
        try{
            draftDao.saveDraft(draftModel);
        } catch(Exception e){
            return "Error: " + e.toString();
        }
        return "Success";
    }
}