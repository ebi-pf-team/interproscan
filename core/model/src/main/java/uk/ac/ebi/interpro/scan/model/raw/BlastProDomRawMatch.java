package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
//public final class BlastProDomRawMatch extends RawMatch implements Serializable {
public class BlastProDomRawMatch extends RawMatch implements Serializable {

    // TODO: Algorithm classes should be abstract so can sub-class based on member DB name
    // TODO: eg. ProDomRawMatch, PfamRawMatch, TigrFamRawMatch ...etc
    // TODO: No need for member DBs where algorithm is unique to them, eg. PRINTS, Panther

    //@Column(name="SCORE")
    private double score;   // location.score
    
    public BlastProDomRawMatch() { }
    
     public BlastProDomRawMatch(String seqIdentifier, String model,String dbname,String dbversion, String generator, long start, long end,double score) {
        super(seqIdentifier,model,dbname,dbversion,generator,start,end);
        setScore(score);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
