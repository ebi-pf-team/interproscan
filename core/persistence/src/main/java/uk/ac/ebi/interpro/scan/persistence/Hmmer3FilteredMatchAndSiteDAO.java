package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawSite;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

/**
 * HMMER3 filtered match data access object.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
abstract class Hmmer3FilteredMatchAndSiteDAO<T extends Hmmer3RawMatch, U extends SFLDHmmer3RawSite>
        extends FilteredMatchAndSiteDAOImpl<T, Hmmer3Match, U, Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site> {

    private static final Logger LOGGER = Logger.getLogger(Hmmer3FilteredMatchAndSiteDAO.class.getName());

    public Hmmer3FilteredMatchAndSiteDAO() {
        super(Hmmer3Match.class);
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
    public void persist(Collection<RawProtein<T>> filteredProteins, Collection<U> rawSites, final Map<String, Signature> modelAccessionToSignatureMap, final Map<String, Protein> proteinIdToProteinMap) {

        // Map seqId to raw sites for that sequence
        Map<String, List<U>> seqIdToRawSitesMap = new HashMap<>();
        if (rawSites != null) {
            for (U rawSite : rawSites) {
                String seqId = rawSite.getSequenceIdentifier();
                if (seqIdToRawSitesMap.containsKey(seqId)) {
                    seqIdToRawSitesMap.get(seqId).add(rawSite);
                } else {
                    List<U> s = new ArrayList<>();
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

            Collection<T> rawMatches = rp.getMatches();
            for (T rawMatch : rawMatches) {
                // For this match find the sites
                Set<HmmerLocationWithSites.HmmerSite> hmmerSites = getSites(rawMatch, seqIdToRawSitesMap.get(rawMatch.getSequenceIdentifier()));
                Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites> locations = new HashSet<>();
                Utilities.verboseLog("filtered sites: " + hmmerSites);
                locations.add(
                        new Hmmer3MatchWithSites.Hmmer3LocationWithSites(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd(),
                                rawMatch.getScore(),
                                rawMatch.getEvalue(),
                                rawMatch.getHmmStart(),
                                rawMatch.getHmmEnd(),
                                HmmBounds.parseSymbol(rawMatch.getHmmBounds()),
                                rawMatch.getEnvelopeStart(),
                                rawMatch.getEnvelopeEnd(),
                                hmmerSites
                        )
                );
            }

            // Convert raw matches to filtered matches
            Collection<Hmmer3Match> filteredMatches =
                    Hmmer3RawMatch.getMatches(rp.getMatches(), new RawMatch.Listener() {
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
            for (Hmmer3Match match : filteredMatches) {
                protein.addMatch(match); // Adds protein to match (yes, I know it doesn't look that way!)
                entityManager.persist(match);
                matchLocationCount += match.getLocations().size();
            }
            //TODO use a different utitlity function
            //System.out.println(" Filtered Match locations size : - " + matchLocationCount);
        }
    }

    private Set<HmmerLocationWithSites.HmmerSite> getSites(T rawMatch, Collection<U> rawSites){
        Set<HmmerLocationWithSites.HmmerSite> hmmerSites = new HashSet<>();
        if (rawSites != null) {
            for (U rawSite : rawSites) {
                if (rawMatch.getModelId().equalsIgnoreCase(rawSite.getModelId())) {
                    if (siteInLocationRange(rawMatch, rawSite)) {
                        final String siteTitle = rawSite.getTitle();
                        final String[] residueCoordinateList = rawSite.getResidues().split(",");
                        Set<SiteLocation> siteLocations = new HashSet<>();
                        for (String residueAnnot : residueCoordinateList) {
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

}
