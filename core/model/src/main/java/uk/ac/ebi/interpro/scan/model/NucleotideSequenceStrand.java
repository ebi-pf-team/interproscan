package uk.ac.ebi.interpro.scan.model;

import javax.xml.bind.annotation.XmlType;

/**
 * Represents strand on nucleotide sequence.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@XmlType(name="NucleotideSequenceStrandType")
public enum NucleotideSequenceStrand {

    SENSE,
    ANTISENSE

}
