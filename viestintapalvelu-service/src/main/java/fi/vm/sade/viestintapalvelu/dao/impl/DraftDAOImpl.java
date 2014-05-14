package fi.vm.sade.viestintapalvelu.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.expr.BooleanExpression;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.viestintapalvelu.dao.DraftDAO;
import fi.vm.sade.viestintapalvelu.model.Draft;
import fi.vm.sade.viestintapalvelu.model.QDraft;

@Repository
public class DraftDAOImpl extends AbstractJpaDAOImpl<Draft, Long> implements DraftDAO {

	public Draft findDraftByNameOrgTag(	String templateName, String templateLanguage, String organizationOid, 
										String applicationPeriod, String fetchTarget, String tag) {
		EntityManager em = getEntityManager();		
		
		String findDraft = "SELECT a FROM Draft a WHERE "
													+ "a.templateName = :templateName AND "
													+ "a.templateLanguage = :templateLanguage AND "
													+ "a.organizationOid = :organizationOid AND "
													
													+ "a.applicationPeriod = :applicationPeriod AND "
													+ "a.fetchTarget = :fetchTarget AND "
													+ "a.tag = :tag "
								+ "ORDER BY a.timestamp DESC";
		
		TypedQuery<Draft> query = em.createQuery(findDraft, Draft.class);
		query.setParameter("templateName", templateName);
		query.setParameter("templateLanguage", templateLanguage);
        query.setParameter("organizationOid", organizationOid);		        
        query.setParameter("applicationPeriod", applicationPeriod);		        
        query.setParameter("fetchTarget", fetchTarget);		                
		query.setParameter("tag", tag);		
		query.setFirstResult(0);	// LIMIT 1
		query.setMaxResults(1);  	//
			
		Draft draft = new Draft();
		try {
			draft = query.getSingleResult();
		} catch (Exception e) {
			draft = null;
		}
		
		return draft;
	}

	public Draft findDraftByNameOrgTag2(String templateName, String templateLanguage, String organizationOid, 
										String applicationPeriod, String fetchTarget, String tag) {
        QDraft draft = QDraft.draft;
        
        BooleanExpression whereExpression = draft.templateName.eq(templateName)
        									.and(draft.templateLanguage.eq(templateLanguage)
        									.and(draft.organizationOid.eq(organizationOid)));
        
        if (!"".equals(applicationPeriod)) {        	
        	whereExpression = whereExpression.and(draft.applicationPeriod.eq(applicationPeriod));
        }
        if (!"".equals(fetchTarget)) {        	
        	whereExpression = whereExpression.and(draft.fetchTarget.eq(fetchTarget));
        }
        if (!"".equals(tag)) {        	
        	whereExpression = whereExpression.and(draft.tag.eq(tag));
        }
//        OrderSpecifier<?> orderBy = orderBy(draft.timestamp);
        
        
        		
		
		
		return new Draft();
	}
	
	protected JPAQuery from(EntityPath<?>... o) {
        return new JPAQuery(getEntityManager()).from(o);
    }
	
	
}