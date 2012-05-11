package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoader;

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
    void setSequenceLoader(SequenceLoader sequenceLoader);

    @Transactional
    void loadSequences(InputStream fastaFileInputStream, SequenceLoadListener sequenceLoadListener, String analysisJobNames);
}
