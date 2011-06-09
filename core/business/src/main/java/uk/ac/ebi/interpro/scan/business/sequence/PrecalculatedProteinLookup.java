package uk.ac.ebi.interpro.scan.business.sequence;

import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.Set;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface PrecalculatedProteinLookup {

    Protein getPrecalculated(Protein protein);

    Set<Protein> getPrecalculated(Set<Protein> proteins);

}
