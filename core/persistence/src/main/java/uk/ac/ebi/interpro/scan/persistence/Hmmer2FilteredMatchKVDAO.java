package uk.ac.ebi.interpro.scan.persistence;

/**
 * @author maslen
 * @author Gift Nuka
 */

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

abstract class Hmmer2FilteredMatchKVDAO<T extends Hmmer2RawMatch>
        extends FilteredMatchKVDAOImpl<Hmmer2Match, T>
        implements FilteredMatchKVDAO<Hmmer2Match, T> {

    public Hmmer2FilteredMatchKVDAO() {
        super(Hmmer2Match.class);
    }


    @Override
    @Transactional
    public void persist(Collection<RawProtein<T>> filteredProteins, final Map<String, Signature> modelAccessionToSignatureMap, final Map<String, Protein> proteinIdToProteinMap) {
        // Add matches to protein
        SignatureLibrary signatureLibraryRep = null;

        for (RawProtein<T> rp : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rp.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rp.getProteinIdentifier() + "]");

            }
            if (signatureLibraryRep == null) {
                Hmmer2RawMatch repRawMatch = (Hmmer2RawMatch) new ArrayList(rp.getMatches()).get(0);
                signatureLibraryRep = repRawMatch.getSignatureLibrary();
            }
            // Convert raw matches to filtered matches
            Collection<Hmmer2Match> filteredMatches =
                    Hmmer2RawMatch.getMatches(rp.getMatches(), new RawMatch.Listener() {
                                @Override
                                public Signature getSignature(String modelAccession,
                                                              SignatureLibrary signatureLibrary,
                                                              String signatureLibraryRelease) {
                                    return modelAccessionToSignatureMap.get(modelAccession);
                                }
                            }
                    );
            //persist the matches
            String key = Long.toString(protein.getId()) + signatureLibraryRep.getName();
            //String key = ((String) pair.getKey()).replace(signatureLibrary.getName(), "").trim();
            byte[] byteKey = SerializationUtils.serialize(key);
            byte[] byteMatches = SerializationUtils.serialize((HashSet<Hmmer2Match>) filteredMatches);
            //byteKeyToMatchMap.put(byteKey,byteMatches);
            persist(byteKey, byteMatches);

        }
        if (filteredProteins.size() > 0){
       	    addSignatureLibraryName(signatureLibraryRep.getName());
        }

    }
}
