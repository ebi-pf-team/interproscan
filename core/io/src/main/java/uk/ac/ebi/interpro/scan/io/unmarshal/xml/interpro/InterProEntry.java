package uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Lightweight model to hold details of an InterPro Entry,
 * sufficient only to allow basics of an InterPro entry
 * to be output with match data, limited to Entry accession,
 * description and optionally a Set of GoTerm objects.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public final class InterProEntry implements Serializable {

    private final String entryAccession;

    private final String description;

    private final Set<GoTerm> goTerms;

    InterProEntry(String entryAccession, String description, Set<GoTerm> goTerms) {
        if (entryAccession == null || description == null) {
            throw new IllegalArgumentException("entryAccession and description parameters may be null when instantiating a new InterProEntry object. entryAccession: " +
                    entryAccession + ", description: " + description);
        }
        this.entryAccession = entryAccession;
        this.description = description;
        if (goTerms == null) {
            this.goTerms = Collections.emptySet();
        } else {
            this.goTerms = goTerms;
        }
    }

    public String getEntryAccession() {
        return entryAccession;
    }

    public String getDescription() {
        return description;
    }

    public Set<GoTerm> getGoTerms() {
        return goTerms;
    }

    @Override
    public String toString() {
        return entryAccession + " (" +
                description + ") GO: " + goTerms +
                "\n"
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterProEntry that = (InterProEntry) o;

        if (!entryAccession.equals(that.entryAccession)) return false;
        if (!description.equals(that.description)) return false;
        if (!goTerms.equals(that.goTerms)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entryAccession.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + goTerms.hashCode();
        return result;
    }
}
