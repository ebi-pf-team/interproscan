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
    @Column(name="SCORE")
    private double score;   // location.score
    
    public BlastProDomRawMatch() { }
    
     public BlastProDomRawMatch(String identifier, String model, String dbn, String dbver, String gen, long start, long end,double score) {
        super(identifier,model,dbver,gen,start,end);
        setScore(score);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
