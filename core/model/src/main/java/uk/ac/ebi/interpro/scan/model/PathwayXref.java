package uk.ac.ebi.interpro.scan.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a cross reference to a external pathway entry.</br>
 * E.g. UPA00098 Amino-acid biosynthesis; L-proline biosynthesis; L-glutamate 5-semialdehyde from L-glutamate: step 2/2
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@XmlType(name = "PathwayXrefType")
@Table(indexes = { @Index(columnList = "IDENTIFIER") })
public class PathwayXref extends Xref implements Serializable {

    @ManyToMany(mappedBy = "pathwayXRefs",
            targetEntity = Entry.class)
    @JsonBackReference
    private Set<Entry> entries = new HashSet<Entry>();

    /**
     * Zero arguments constructor just for Hibernate.
     */
    protected PathwayXref() {
    }

    public PathwayXref(String identifier, String name, String databaseName) {
        super(databaseName, identifier, name);
    }

    protected void addEntry(Entry entry) {
        if (this.entries == null) {
            this.entries = new HashSet<Entry>();
        }
        entries.add(entry);
    }

    public void removeEntry(Entry entry) {
        if (entries != null) {
            entries.remove(entry);
        }
    }

    @XmlTransient
    public Set<Entry> getEntries() {
        return (entries == null ? new HashSet<Entry>() : entries);
    }

    public void setEntries(Set<Entry> entries) {
        for (Entry e : entries) {
            addEntry(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PathwayXref))
            return false;
        final PathwayXref p = (PathwayXref) o;
        return new EqualsBuilder()
                .append(getId(), p.getId())
                .append(getIdentifier(), p.getIdentifier())
                .append(getName(), p.getName())
                .append(getDatabaseName(), p.getDatabaseName())
                .append(getEntries(), p.getEntries())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(73, 39)
                .append(getId())
                .append(getIdentifier())
                .append(getName())
                .append(getDatabaseName())
                .append(getEntries())
                .toHashCode();
    }

    public enum PathwayDatabase {

        META_CYC('t', "MetaCyc"),
        UNI_PATHWAY('w', "UniPathway"),
        KEGG('k', "KEGG"),
        REACTOME('r', "Reactome");


        private Character databaseCode;

        private String databaseName;

        PathwayDatabase(Character databaseCode, String databaseName) {
            this.databaseCode = databaseCode;
            this.databaseName = databaseName;
        }

        public Character getDatabaseCode() {
            return databaseCode;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        @Override
        public String toString() {
            return databaseName;
        }

        public static PathwayDatabase parseDatabaseCode(Character databaseCode) {
            for (PathwayDatabase database : PathwayDatabase.values()) {
                if (databaseCode.equals(database.getDatabaseCode()) || databaseCode.toString().equalsIgnoreCase(database.getDatabaseCode().toString())) {
                    return database;
                }
            }
            throw new IllegalArgumentException("Unrecognised database code: " + databaseCode);
        }
    }
}