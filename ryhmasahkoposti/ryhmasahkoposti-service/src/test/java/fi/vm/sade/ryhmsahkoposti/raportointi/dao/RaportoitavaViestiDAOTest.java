package fi.vm.sade.ryhmsahkoposti.raportointi.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.ryhmasahkoposti.raportointi.dao.RaportoitavaVastaanottajaDAO;
import fi.vm.sade.ryhmasahkoposti.raportointi.dao.RaportoitavaViestiDAO;
import fi.vm.sade.ryhmasahkoposti.raportointi.model.RaportoitavaVastaanottaja;
import fi.vm.sade.ryhmasahkoposti.raportointi.model.RaportoitavaViesti;
import fi.vm.sade.ryhmsahkoposti.raportointi.testdata.RaportointipalveluTestData;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-bundle-context.xml")
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, 
	DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class})
@Transactional(readOnly=true)
public class RaportoitavaViestiDAOTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Autowired
	private RaportoitavaViestiDAO raportoitavaViestiDAO;
	@Autowired
	private RaportoitavaVastaanottajaDAO raportoitavaVastaanottajaDAO;

	@Test
	public void testRaportoitavanViestinLisaysOnnistuu() {
		RaportoitavaViesti raportoitavaViesti = RaportointipalveluTestData.getRaportoitavaViesti();
		RaportoitavaViesti tallennettuRaportoitavaViesti = raportoitavaViestiDAO.insert(raportoitavaViesti);
		
		assertNotNull(tallennettuRaportoitavaViesti);
		assertNotNull(tallennettuRaportoitavaViesti.getId());
		assertNotNull(tallennettuRaportoitavaViesti.getVersion());
	}

	@Test
	public void testRaportoitaviaViestejäLoytyy() {
		RaportoitavaViesti raportoitavaViesti = RaportointipalveluTestData.getRaportoitavaViesti();
		raportoitavaViestiDAO.insert(raportoitavaViesti);

		List<RaportoitavaViesti> raportoitavatViestit = raportoitavaViestiDAO.findAll();
		
		assertNotNull(raportoitavatViestit);
		assertNotEquals(0, raportoitavatViestit.size());
	}
	
	@Test
	public void testRaportoitavaViestiLoytyyLahetystunnuksella() {
		RaportoitavaViesti raportoitavaViesti = RaportointipalveluTestData.getRaportoitavaViesti();
		RaportoitavaViesti tallennettuRaportoitavaViesti = raportoitavaViestiDAO.insert(raportoitavaViesti);

		Long id = tallennettuRaportoitavaViesti.getId();
		RaportoitavaViesti haettuRaportoitavaViesti = raportoitavaViestiDAO.read(id);
		
		assertNotNull(haettuRaportoitavaViesti);
		assertEquals(tallennettuRaportoitavaViesti.getId(), haettuRaportoitavaViesti.getId());
	}
	
	@Test
	public void testRaportoitavanViestinPaivitysOnnistuu() {
		RaportoitavaViesti raportoitavaViesti = RaportointipalveluTestData.getRaportoitavaViesti();
		RaportoitavaViesti tallennettuRaportoitavaViesti = raportoitavaViestiDAO.insert(raportoitavaViesti);
		
		assertEquals(new Long(0), tallennettuRaportoitavaViesti.getVersion());
		
		tallennettuRaportoitavaViesti.setLahetysPaattyi(new Date());		
		raportoitavaViestiDAO.update(tallennettuRaportoitavaViesti);
		
		assertEquals(new Long(1), raportoitavaViesti.getVersion());
	}

	@Test
	public void testRaportoitavaaViestiaEiLoydyLahetystunnuksella() {
		Long viestiID = new Long(2013121452);
		RaportoitavaViesti raportoitavaViesti = raportoitavaViestiDAO.read(viestiID);
		
		assertNull(raportoitavaViesti);
	}
	
	@Test
	public void testRaportoitvienViestienLukumaara() {
		RaportoitavaViesti raportoitavaViesti = RaportointipalveluTestData.getRaportoitavaViesti();
		raportoitavaViestiDAO.insert(raportoitavaViesti);
		
		Long lkm = raportoitavaViestiDAO.findRaportoitavienViestienLukumaara();
		
		assertNotNull(lkm);
		assertNotEquals(new Long(0), lkm);
	}
	
	@Test
	public void testViestinlahettajilleLoytyyRaportoitavatViestit() {
		List<RaportoitavaViesti> raportoitavatViestit = RaportointipalveluTestData.getRaportoitavaViestiLista();
		
		for (RaportoitavaViesti raportoitavaViesti : raportoitavatViestit) {
			RaportoitavaViesti tallennettuRaportoitavaViesti = raportoitavaViestiDAO.insert(raportoitavaViesti);
			RaportoitavaVastaanottaja raportoitavaVastaanottaja = 
				RaportointipalveluTestData.getRaportoitavaVastaanottaja(tallennettuRaportoitavaViesti);
			raportoitavaVastaanottajaDAO.insert(raportoitavaVastaanottaja);
		}
		
		List<String> lahettajanOidList = new ArrayList<String>();
		lahettajanOidList.add("102030405010");
		lahettajanOidList.add("102030405020");
		
		List<RaportoitavaViesti> haetutRaportoitavatViestit = 
			raportoitavaViestiDAO.findLahettajanRaportoitavatViestit(lahettajanOidList);
		
		assertNotNull(haetutRaportoitavatViestit);
		assertEquals(2, haetutRaportoitavatViestit.size());
	}
	
	@Test
	public void testViestinlahettajilleEiLoydyRaportoitavatViestit() {
		List<String> lahettajanOidList = new ArrayList<String>();
		lahettajanOidList.add("102030405998");
		lahettajanOidList.add("102030405999");
		
		List<RaportoitavaViesti> haetutRaportoitavatViestit = 
			raportoitavaViestiDAO.findLahettajanRaportoitavatViestit(lahettajanOidList);
		
		assertNotNull(haetutRaportoitavatViestit);
		assertEquals(0, haetutRaportoitavatViestit.size());
	}
}