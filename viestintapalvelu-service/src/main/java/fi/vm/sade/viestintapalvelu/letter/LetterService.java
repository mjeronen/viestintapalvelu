package fi.vm.sade.viestintapalvelu.letter;

import fi.vm.sade.viestintapalvelu.dao.LetterBatchStatusDto;
import fi.vm.sade.viestintapalvelu.letter.dto.AsyncLetterBatchDto;
import fi.vm.sade.viestintapalvelu.model.LetterBatch;
import fi.vm.sade.viestintapalvelu.model.LetterReceiverLetter;
import fi.vm.sade.viestintapalvelu.model.LetterReceivers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Rajapinta kirjeiden liiketoimtakäsittelyä varten
 *  
 * @author vehei1
 *
 */
public interface LetterService {



    public enum LetterBatchProcess {
        EMAIL, LETTER
    }

    /**
     * Luo kirjelähetyksen
     *
     * @param letterBatch Annetun kirjelähetyksen tiedot
     * @return Luodun kirjelähetyksen tiedot
     */
    LetterBatch createLetter(AsyncLetterBatchDto letterBatch);

    /**
     * Luo kirjelähetyksen
     * 
     * @param letterBatch Annetun kirjelähetyksen tiedot
     * @return Luodun kirjelähetyksen tiedot
     */
    public LetterBatch createLetter(fi.vm.sade.viestintapalvelu.letter.LetterBatch letterBatch);

    /**
     * Hakee kirjelähetyksen tiedot annetun avaimen perusteella
     * 
     * @param id Kirjelähetyksen avain
     * @return Kirjelähetyksen tiedot
     */
    public fi.vm.sade.viestintapalvelu.letter.LetterBatch findById(long id);

    /**
     * Hakee annettujen hakuparametrien mukaiset kirjelähetyksen tiedot
     * 
     * @param templateName Kirjepohjan nimi
     * @param languageCode Kielikoodi
     * @param organizationOid Organisaation OID
     * @param tag Tunniste
     * @return Kirjelähetyksen tiedot
     */
    public fi.vm.sade.viestintapalvelu.letter.LetterBatch findLetterBatchByNameOrgTag(String templateName,
        String languageCode, String organizationOid, String tag);

    /**
     * Hakee annettujen hakuparametrien mukaiset korvauskentien tiedot
     * 
     * @param templateName Kirjepohjan nimi
     * @param languageCode Kielikoodi
     * @param organizationOid organisaation OID
     * @param tag Tunniste
     * @return Lista korvauskenttien tietoja
     */
    public List<fi.vm.sade.viestintapalvelu.template.Replacement> findReplacementByNameOrgTag(String templateName,
        String languageCode, String organizationOid, String tag);

    /**
     * Hakee vastaanottajan kirjeen sisällön
     * 
     * @param id Vastaanottajan kirjeen avain
     * @return Kirjeen sisällön tiedot
     */
    public fi.vm.sade.viestintapalvelu.letter.LetterContent getLetter(long id);
    
    /**
     * Hakee kirjelähetyksen kirjeiden sisällöt ja yhdistää ne yhdeksi PDF-dokumentiksi
     * 
     * @param letterBatchID Kirjelähetyksen avain
     * @return Kirjelähetyksen kirjeiden sisällöt
     * @throws Exception
     */
    public byte[] getLetterContentsByLetterBatchID(Long letterBatchID) throws Exception;

    void updateBatchProcessingStarted(long id, LetterBatchProcess process);

    @Transactional
    void processLetterReceiver(long receiverId) throws Exception;

    void updateBatchProcessingFinished(long id, LetterBatchProcess process);

    LetterBatch fetchById(long id);

    List<LetterReceiverLetter> getReceiverLetters (Set<LetterReceivers> letterReceivers);

    LetterBatchStatusDto getBatchStatus(long batchId);

    void updateLetter(LetterReceiverLetter letter);

    List<Long> findUnprocessedLetterReceiverIdsByBatch(long batchId);

    void saveBatchErrorForReceiver(Long letterReceiverId, String message);
}