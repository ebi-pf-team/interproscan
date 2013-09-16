package uk.ac.ebi.interpro.scan.business.sequence;

import uk.ac.ebi.interpro.scan.model.Protein;

import java.io.IOException;
import java.util.Set;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface PrecalculatedProteinLookup {

    Protein getPrecalculated(Protein protein, String analysisJobNames);

    Set<Protein> getPrecalculated(Set<Protein> proteins, String analysisJobNames);

    boolean isConfigured();

    /**
     *   If the client and the server are based on the same version of interproscan
     *   return true, otherwise return false
     */
    boolean isSynchronised() throws IOException;



}
