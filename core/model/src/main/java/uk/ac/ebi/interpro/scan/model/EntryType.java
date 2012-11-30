/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.interpro.scan.model;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * InterPro entry type.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@XmlType(name="EntryTypeType")
public enum EntryType implements Serializable {

    // TODO: Should this be an enum, or read from the database?

    // select upper(abbrev)||'("'||code||'", "'||abbrev||'", "'||description||'"),'
    // from interpro.cv_entry_type order by code;

    /** Active site */
    ACTIVE_SITE('A',
            "Active site",
            "Active sites are best known as the catalytic pockets of enzymes " +
            "where a substrate is bound and converted to a product, which is then released. Distant parts of a " +
            "protein's primary structure may be involved in the formation of the catalytic pocket. " +
            "Therefore, to describe an active site, different signatures may well be needed to cover all " +
            "the active site residues. In InterPro active sites are defined by PROSITE patterns and the " +
            "amino acids involved in the catalytic reaction must be described and mutational inactivation " +
            "studies reported."),

    /** Binding site */
    BINDING_SITE('B',
            "Binding site",
            "An InterPro Binding site binds chemical compounds, which themselves are not substrates " +
            "for a reaction and where the binding is reversible. The compound, which is bound, may be a " +
            "required co-factor for a chemical reaction, be involved in electron transport or be involved " +
            "in protein structure modification. In InterPro binding sites are defined by PROSITE patterns " +
            "where the amino acid(s) involved in the binding have been described."),

    /** Conserved site */
    CONSERVED_SITE('C',
            "Conserved site",
            "A Conserved site is a protein motif that is define by a PROSITE pattern. " +
            "These PROSITE patterns are intended to identify proteins containing a characteristic fingerprint " +
            "of a protein domain or where they are members of a protein family. In InterPro these are not " +
            "defined as 'Binding Sites', 'Active Sites' or 'PTMs'."),

    /** Domain */
    DOMAIN('D',
            "Domain",
            "In InterPro a 'Domain' can be an evolutionary conserved sequence defining a known biological " +
            "domain or a region of unknown function. Its length is not defined but it must have adjacent member " +
            "database signatures."),

    /** Family */
    FAMILY('F',
            "Family",
            "Group of evolutionarily related proteins that may share one or more features in common, " +
            "which in InterPro are defined by a member database signature with no adjacent signatures."),

    /** Post-translational modification */
    PTM('P',
            "PTM",
            "Posttranslational modification (PTM) is the chemical modification of a protein after its translation. " +
            "PTMs involve the addition of functional groups, addition of other proteins or peptides, " +
            "changing the chemical nature of amino acids or changing the primary structure of the protein itself.  " +
            "Sequence motifs defining the modification sites are generally, but not always, " +
            "defined by PROSITE patterns."),

    /** Repeat */
    REPEAT('R',
            "Repeat",
            "Repeats are regions that are not expected to fold into a globular domain on their own. " +
            "For example 6-8 copies of the WD40 repeat are needed to form a single globular domain. " +
            "There also many other short repeat motifs that probably do not form a globular fold."),

    /** Unknown */
    UNKNOWN('U',
            "Unknown",
            "Placeholder for undecided cases. There should not be any in a release.");


    private final char code;
    private final String name;
    private final String description;

    private EntryType(char code, String name, String description) {
        this.code        = code;
        this.name        = name;
        this.description = description;
    }

    public char getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override public String toString() {
        return name;
    }

    public static EntryType parseCode(char code)  {
        for (EntryType m : EntryType.values()) {
            if (code == m.getCode())   {
                return m;
            }
        }
        throw new IllegalArgumentException("Unrecognised code: " + code);
    }

    public static EntryType parseName(String name)  {
        for (EntryType m : EntryType.values()) {
            if (name.equals(m.getName()))   {
                return m;
            }
        }
        throw new IllegalArgumentException("Unrecognised name: " + name);
    }

    /**
     * Map EntryType to and from XML representation
     */
    /*
    @XmlTransient
    static final class EntryTypeAdapter extends XmlAdapter<String, EntryType> {
        // Map Java to XML type
        @Override public String marshal(EntryType entryType) {
            return entryType.getName();
        }
        // Map XML type to Java
        @Override public EntryType unmarshal(String name) {
            return EntryType.parseName(name);
        }
    }
    */

}
