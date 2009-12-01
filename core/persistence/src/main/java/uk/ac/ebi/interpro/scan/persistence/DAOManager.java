package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.persistence.raw.PfamHmmer3RawMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;

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

    private GenericDAO modelDAO;

    private BlastProDomLocationDAO blastProdomLocationDAO;

    private PfamHmmer3RawMatchDAO pfamRawMatchDAO;

    private RawMatchDAO rawMatchDAO;

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setSignatureDAO(SignatureDAO signatureDAO) {
        this.signatureDAO = signatureDAO;
    }

    @Required
    public void setModelDAO(GenericDAO modelDAO) {
        this.modelDAO = modelDAO;
    }

    @Required
    public void setBlastProdomLocationDAO(BlastProDomLocationDAO blastProdomLocationDAO) {
        this.blastProdomLocationDAO = blastProdomLocationDAO;
    }

    @Required
    public void setPfamRawMatchDAO(PfamHmmer3RawMatchDAO pfamRawMatchDAO) {
        this.pfamRawMatchDAO = pfamRawMatchDAO;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    public ProteinDAO getProteinDAO() {
        return proteinDAO;
    }

    public SignatureDAO getSignatureDAO() {
        return signatureDAO;
    }

    public GenericDAO getModelDAO() {
        return modelDAO;
    }

    public BlastProDomLocationDAO getBlastProdomLocationDAO() {
        return blastProdomLocationDAO;
    }

    public PfamHmmer3RawMatchDAO getPfamRawMatchDAO() {
        return pfamRawMatchDAO;
    }

    public RawMatchDAO getRawMatchDAO() {
        return rawMatchDAO;
    }
}
