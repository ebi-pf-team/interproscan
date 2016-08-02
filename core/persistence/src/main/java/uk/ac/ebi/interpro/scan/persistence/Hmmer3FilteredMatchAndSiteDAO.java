package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawSite;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.*;

/**
 * HMMER3 filtered match and site data access object.
 */
abstract class Hmmer3FilteredMatchAndSiteDAO<T extends Hmmer3RawMatch, E extends Hmmer3RawSite>
        extends FilteredMatchAndSiteDAOImpl<T, Hmmer3MatchWithSites, E, Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site> {

    private static final Logger LOGGER = Logger.getLogger(Hmmer3FilteredMatchAndSiteDAO.class.getName());

    public Hmmer3FilteredMatchAndSiteDAO() {
        super(Hmmer3MatchWithSites.class);
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins             being the Collection of filtered RawProtein objects to persist
     * @param modelAccessionToSignatureMap a Map of model accessions to Signature objects.
     * @param proteinIdToProteinMap        a Map of Protein IDs to Protein objects
     */
    @Transactional
    public void persist(Collection<RawProtein<T>> filteredProteins, Collection<E> rawSites, final Map<String, Signature> modelAccessionToSignatureMap, final Map<String, Protein> proteinIdToProteinMap) {

        // Map seqId to raw sites for that sequence
        Map<String, List<E>> seqIdToRawSitesMap = new HashMap<>();
        if (rawSites != null) {
            for (E rawSite : rawSites) {
                String seqId = rawSite.getSequenceIdentifier();
                if (seqIdToRawSitesMap.containsKey(seqId)) {
                    seqIdToRawSitesMap.get(seqId).add(rawSite);
                } else {
                    List<E> s = new ArrayList<>();
                    s.add(rawSite);
                    seqIdToRawSitesMap.put(seqId, s);
                }
            }
        }

        // Add matches to protein
        for (RawProtein<T> rp : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rp.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rp.getProteinIdentifier() + "]");

            }

            // Convert raw matches to filtered matches
            Collection<Hmmer3MatchWithSites> filteredMatches =
                    getMatchesWithSites(rp.getMatches(), seqIdToRawSitesMap.get(rp.getProteinIdentifier()), new RawMatch.Listener() {
                                @Override
                                public Signature getSignature(String modelAccession,
                                                              SignatureLibrary signatureLibrary,
                                                              String signatureLibraryRelease) {
                                    Signature signature = modelAccessionToSignatureMap.get(modelAccession);
                                    if (signature == null) {
                                        throw new IllegalStateException("Attempting to persist a match to " + modelAccession + " however this has not been found in the database.");
                                    }
                                    return modelAccessionToSignatureMap.get(modelAccession);
                                }
                            }
                    );

            int matchLocationCount = 0;
            for (Hmmer3MatchWithSites match : filteredMatches) {
                protein.addMatch(match); // Adds protein to match (yes, I know it doesn't look that way!)
                entityManager.persist(match);
                matchLocationCount += match.getLocations().size();
            }
            //TODO use a different utitlity function
            //System.out.println(" Filtered Match locations size : - " + matchLocationCount);
        }
    }

    private Set<HmmerLocationWithSites.HmmerSite> getSites(T rawMatch, Collection<E> rawSites){
        Set<HmmerLocationWithSites.HmmerSite> hmmerSites = new HashSet<>();
        if (rawSites != null) {
            for (E rawSite : rawSites) {
                if (rawMatch.getModelId().equalsIgnoreCase(rawSite.getModelId())) {
                    if (siteInLocationRange(rawMatch, rawSite)) {
                        final String siteTitle = rawSite.getTitle();
                        final String[] residueCoordinateList = rawSite.getResidues().split(",");
                        Set<SiteLocation> siteLocations = new HashSet<>();
                        for (String residueAnnot : residueCoordinateList) {
                            residueAnnot = residueAnnot.trim();
                            String residue = residueAnnot.substring(0, 1);
                            int position = Integer.parseInt(residueAnnot.substring(1));
                            SiteLocation siteLocation = new SiteLocation(residue, position, position);
                            siteLocations.add(siteLocation);
                        }
                        Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site site = new Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site(siteTitle, siteLocations);

                        hmmerSites.add(site);
                    }
                }
            }
        }
        return hmmerSites;
    }


    public Collection<Hmmer3MatchWithSites> getMatchesWithSites(Collection<T> rawMatches,
                                                              List<E> rawSites,
                                                              RawMatch.Listener rawMatchListener) {
        Collection<Hmmer3MatchWithSites> matches = new HashSet<>();
        // Get a list of unique model IDs
        SignatureLibrary signatureLibrary = null;
        String signatureLibraryRelease = null;

        Map<String, Set<T>> matchesByModel = new HashMap<>();
        for (T m : rawMatches) {
            // Get signature library name and release
            if (signatureLibrary == null) {
                signatureLibrary = m.getSignatureLibrary();
                signatureLibraryRelease = m.getSignatureLibraryRelease();
            } else if (!signatureLibrary.equals(m.getSignatureLibrary()) ||
                    !signatureLibraryRelease.equals(m.getSignatureLibraryRelease())) {
                throw new IllegalArgumentException("Filtered matches are from different signature library versions " +
                        "(more than one library version found)");
            }
            // Get unique list of model IDs
            String modelId = m.getModelId();
            if (matchesByModel.containsKey(modelId)) {
                matchesByModel.get(modelId).add(m);
            } else {
                Set<T> set = new HashSet<>();
                set.add(m);
                matchesByModel.put(modelId, set);
            }
        }
        // Find the location(s) for each match and create a Match instance
        for (String key : matchesByModel.keySet()) {
            Signature signature = rawMatchListener.getSignature(key, signatureLibrary, signatureLibraryRelease);
            matches.add(getMatch(signature, key, matchesByModel, rawSites));
        }
        // Next step would be to link this with protein...
        return matches;

    }

    private Hmmer3MatchWithSites getMatch(Signature signature, String modelId, Map<String, Set<T>> matchesByModel, List<E> rawSites) {
        Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites> locations = new HashSet<>();
        double score = 0, evalue = 0;
        for (T m : matchesByModel.get(modelId)) {
            // Score and evalue should be the same (repeated for each location)
            score = m.getScore();
            evalue = m.getEvalue();
            locations.add(getLocation(m, rawSites));
        }
        return new Hmmer3MatchWithSites(signature, score, evalue, locations);
    }

    private Hmmer3MatchWithSites.Hmmer3LocationWithSites getLocation(T m, List<E> rawSites) {
        return new Hmmer3MatchWithSites.Hmmer3LocationWithSites(
                m.getLocationStart(),
                m.getLocationEnd(),
                m.getLocationScore(),
                m.getDomainIeValue(),
                m.getHmmStart(),
                m.getHmmEnd(),
                HmmBounds.parseSymbol(m.getHmmBounds()),
                m.getEnvelopeStart(),
                m.getEnvelopeEnd(),
                getSites(m, rawSites)
        );
    }




}
