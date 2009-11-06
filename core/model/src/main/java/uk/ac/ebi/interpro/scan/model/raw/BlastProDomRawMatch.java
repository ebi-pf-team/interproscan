package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Column;
import java.io.Serializable;

/**
 * TODO: Add class description
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
@Entity
//public final class BlastProDomRawMatch extends RawMatch implements Serializable {
public class BlastProDomRawMatch extends RawMatch implements Serializable {
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
