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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
