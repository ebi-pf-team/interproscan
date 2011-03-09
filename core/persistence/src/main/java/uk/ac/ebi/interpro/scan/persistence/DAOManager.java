package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.raw.PfamHmmer3RawMatchDAO;

/**
 * Convenience class to hold DAO objects.
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: Dec 1, 2009
 * Time: 11:02:26 AM
 */
public class DAOManager {

    private ProteinDAO proteinDAO;

    private SignatureDAO signatureDAO;

    private GenericDAO<Model, Long> modelDAO;

    private GenericDAO<SignatureLibraryRelease, Long> SignatureLibraryReleaseDAO;

    private PfamHmmer3RawMatchDAO pfamRawMatchDAO;

    private FilteredMatchDAO pfamFilteredMatchDAO;


    public ProteinDAO getProteinDAO() {
        return proteinDAO;
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    public SignatureDAO getSignatureDAO() {
        return signatureDAO;
    }

    @Required
    public void setSignatureDAO(SignatureDAO signatureDAO) {
        this.signatureDAO = signatureDAO;
    }

    public GenericDAO<Model, Long> getModelDAO() {
        return modelDAO;
    }

    @Required
    public void setModelDAO(GenericDAO<Model, Long> modelDAO) {
        this.modelDAO = modelDAO;
    }

    public GenericDAO<SignatureLibraryRelease, Long> getSignatureLibraryReleaseDAO() {
        return SignatureLibraryReleaseDAO;
    }

    @Required
    public void setSignatureLibraryReleaseDAO(GenericDAO<SignatureLibraryRelease, Long> signatureLibraryReleaseDAO) {
        SignatureLibraryReleaseDAO = signatureLibraryReleaseDAO;
    }

    public PfamHmmer3RawMatchDAO getPfamRawMatchDAO() {
        return pfamRawMatchDAO;
    }

    @Required
    public void setPfamRawMatchDAO(PfamHmmer3RawMatchDAO pfamRawMatchDAO) {
        this.pfamRawMatchDAO = pfamRawMatchDAO;
    }

    public FilteredMatchDAO getPfamFilteredMatchDAO() {
        return pfamFilteredMatchDAO;
    }

    @Required
    public void setPfamFilteredMatchDAO(FilteredMatchDAO pfamFilteredMatchDAO) {
        this.pfamFilteredMatchDAO = pfamFilteredMatchDAO;
    }
}
