package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model;

import java.io.Serializable;

/**
 * Domain class representing a PFAM Clan.
 * Hmm.  Probably don't need this class at all - little more than a String.
 * But typed.
 *
 * @author Phil Jones
 * @version $Id: PfamClan.java,v 1.3 2009/10/26 13:46:33 pjones Exp $
 * @since 1.0
 */
public class PfamClan implements Serializable {

    /**
     * The accession of the clan.
     */
    private String id;

    public PfamClan(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PfamClan)) return false;

        PfamClan pfamClan = (PfamClan) o;

        return id.equals(pfamClan.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "PfamClan{" +
                "id='" + id + '\'' +
                '}';
    }
}
