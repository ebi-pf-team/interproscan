package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;

import javax.persistence.Query;
import java.util.*;

/**
 * HMMER3 filtered match data access object.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 */
abstract class Hmmer3FilteredMatchDAO<T extends Hmmer3RawMatch> 
        extends GenericDAOImpl<Hmmer3Match, Long>
        implements FilteredMatchDAO<T, Hmmer3Match> {

    public Hmmer3FilteredMatchDAO() {
        super(Hmmer3Match.class);
    }

    @Override public void persist(Collection<RawProtein<T>> rawProteins) {
        final Map<String, Signature> signatureMap = new HashMap<String, Signature>();
        // Get proteins
        Map<String, Protein> proteinMap =  getProteins(rawProteins);
        // Add matches to protein
        for (RawProtein<T> rp : rawProteins)    {
            Protein protein = proteinMap.get(rp.getProteinIdentifier());
            if (protein == null)  {
                throw new IllegalStateException ("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rp.getProteinIdentifier() + "]");

            }
            // Convert raw matches to filtered matches
            Collection<Hmmer3Match> filteredMatches = 
                    Hmmer3RawMatch.getMatches(rp.getMatches(), new RawMatch.Listener()  {
                @Override public Signature getSignature(String modelAccession,
                                                        SignatureLibrary signatureLibrary,
                                                        String signatureLibraryRelease) {
                    if (signatureMap.isEmpty())   {
                        // Assumes all results are from same signature library release
                        signatureMap.putAll(getSignaturesByModelAc(signatureLibrary, signatureLibraryRelease));
                    }
                    return signatureMap.get(modelAccession);
                }
            }
            );
            // Add matches to protein
            for (Hmmer3Match m : filteredMatches)   {
                protein.addMatch(m);
            }
            // Store
            entityManager.persist(protein);
        }
    }

    /**
     * Returns {@link Protein}s corresponding to given set of {@link RawProtein}s.
     *
     * @param  rawProteins Raw proteins
     * @return {@link Protein}s corresponding to given set of {@link RawProtein}s
     */
    private Map<String, Protein> getProteins(Collection<RawProtein<T>> rawProteins){
        // TODO: This method can be used for all member databases, not just HMMER3 
        // Get list of protein IDs.
        final List<Long> proteinIds = new ArrayList<Long>(rawProteins.size());
        for (RawProtein<T> rp : rawProteins){
            proteinIds.add (new Long (rp.getProteinIdentifier()));
        }

        final Map<String, Protein> map = new HashMap<String, Protein>(rawProteins.size());
        for (int start = 0; start < proteinIds.size(); start += MAXIMUM_IN_CLAUSE_SIZE){
            int end = start + MAXIMUM_IN_CLAUSE_SIZE;
            if (end > proteinIds.size()){
                end = proteinIds.size();
            }
            final List<Long> proteinIdSlice = proteinIds.subList(start, end);
            final Query query = entityManager.createQuery(
                    "select p from Protein p where p.id in (:proteinId)"
            );
            query.setParameter("proteinId", proteinIdSlice);
            @SuppressWarnings("unchecked") List<Protein> proteins = query.getResultList();
            for (Protein protein : proteins){
                map.put(protein.getId().toString(), protein);
            }
        }
        return map;
    }

    /**
     * Returns signatures mapped by model accession.
     *
     * @param signatureLibrary          Signature library
     * @param signatureLibraryRelease   Signature library release
     * @return Signatures mapped by model accession.
     */
    private Map<String, Signature> getSignaturesByModelAc(SignatureLibrary signatureLibrary,
                                                          String signatureLibraryRelease) {
        // TODO: This method can be used for all member databases, not just HMMER3
        final Query query =
                entityManager.createQuery(
                        "select r from SignatureLibraryRelease r " +
                        "where r.version = :version " +
                        "and r.library = :signatureLibrary");
        query.setParameter("signatureLibrary", signatureLibrary);
        query.setParameter("version", signatureLibraryRelease);
        @SuppressWarnings("unchecked") List<SignatureLibraryRelease> releaseList = query.getResultList();
        if (releaseList.size() == 0){
            throw new IllegalStateException ("No SignatureLibraryRelease found for " 
                    + signatureLibrary.getName() + " " + signatureLibraryRelease);
        }
        if (releaseList.size() > 1){
            throw new IllegalStateException ("More than one SignatureLibraryRelease found for " 
                    + signatureLibrary.getName() + " " + signatureLibraryRelease);
        }
        Set<Signature> signatures = releaseList.get(0).getSignatures();
        Map<String, Signature> map = new HashMap<String, Signature>(signatures.size());
        for (Signature s : signatures)  {
            for (Model m : s.getModels().values())   {
                map.put(m.getAccession(), s);
            }
        }
        return map;
    }
    
}
