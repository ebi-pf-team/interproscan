package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoader;

import java.io.InputStream;
import java.io.Serializable;

/**
 * TODO Description of class...
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public interface LoadFastaFile extends Serializable {
    @Required
    void setProteinLoader(ProteinLoader proteinLoader);

    @Transactional
    void loadSequences(InputStream fastaFileInputStream, ProteinLoadListener proteinLoadListener);
}
