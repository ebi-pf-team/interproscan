package uk.ac.ebi.interpro.scan.model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

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
public class PathwayXref extends Xref implements Serializable {

    @ManyToMany
    private Entry entry;

    /**
     * Zero arguments constructor just for Hibernate.
     */
    protected PathwayXref() {
    }

    public PathwayXref(String databaseName, String identifier, String name) {
        super(databaseName, identifier, name);
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
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
                if (databaseCode == database.getDatabaseCode() || databaseCode.toString().equalsIgnoreCase(database.getDatabaseCode().toString())) {
                    return database;
                }
            }
            throw new IllegalArgumentException("Unrecognised database code: " + databaseCode);
        }
    }
}
