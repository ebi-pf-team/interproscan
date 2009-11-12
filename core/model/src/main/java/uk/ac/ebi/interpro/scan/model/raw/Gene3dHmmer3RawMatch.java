package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public class Gene3dHmmer3RawMatch extends Hmmer3RawMatch {

    // Sequence alignment in CIGAR format
    private String cigarAlignment;

    public String getCigarAlignment() {
        return cigarAlignment;
    }

    public void setCigarAlignment(String cigarAlignment) {
        this.cigarAlignment = cigarAlignment;
    }    

}