package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @author  Manjula Thimma
 * @version $Id$
 */
@Entity
public class PfamHmmer3RawMatch extends Hmmer3RawMatch {
    public PfamHmmer3RawMatch() { }
    public PfamHmmer3RawMatch(String seqIdentifier, String model,String signatureLibraryName, String signatureLibraryRelease, String generator,long hmmStart, long hmmEnd) {
           super.setSequenceIdentifier(seqIdentifier);
        super.setModel(model);
        super.setSignatureLibraryName(signatureLibraryName);
        super.setSignatureLibraryRelease(signatureLibraryRelease);
        super.setGenerator(generator);
        super.setHmmEnd(hmmStart);
        super.setHmmEnd(hmmEnd);


    }

}
