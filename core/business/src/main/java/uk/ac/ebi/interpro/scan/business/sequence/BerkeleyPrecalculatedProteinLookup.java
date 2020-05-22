package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.model.OpenReadingFrame;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupStoreToI5ModelDAO;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntryXML;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;
import uk.ac.ebi.interpro.scan.precalc.client.MatchHttpClient;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Looks up precalculated matches from the Berkeley WebService.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BerkeleyPrecalculatedProteinLookup implements PrecalculatedProteinLookup, Runnable {

    Logger LOGGER = Logger.getLogger(BerkeleyPrecalculatedProteinLookup.class.getName());

    /**
     * This client is used to check for existing matches
     * from a web service.  This web service will be provided directly
     * from the EBI, but can also be easily installed locally.
     * (Runs from a BerkeleyDB on Jetty, so can be run from a single
     * command).
     * <p>
     * Essentially, the client will be used if it is available.
     */
    private MatchHttpClient preCalcMatchClient;

    private LookupStoreToI5ModelDAO lookupStoreToI5ModelDAO;

    private ProteinDAO proteinDAO;

    private List<Protein> proteins;

//    private  Set<Protein> proteinsNotInLookup = new HashSet<>();
//
//    private Set<Protein> proteinsWithoutLookupHit;

    private Map<String, Long> proteinRanges;

    private int proteinInsertBatchSize = 200;

    private int proteinInsertBatchSizeNoLookup;

    private int proteinPrecalcLookupBatchSize = 100;

    private Map<String, SignatureLibraryRelease> analysisJobMap;

    private String interproscanVersion;

    private Long timeLookupError = null;

    private Long timeLookupSynchronisationError = null;

    private int totalLookedup = 0;

    public BerkeleyPrecalculatedProteinLookup() {


    }

    public BerkeleyPrecalculatedProteinLookup(List<Protein> proteins, Map<String, Long> proteinRanges, ProteinDAO proteinDAO, LookupStoreToI5ModelDAO lookupStoreToI5ModelDAO, MatchHttpClient preCalcMatchClient) {
        this.proteins = proteins;
        this.proteinRanges = proteinRanges;
        this.proteinDAO = proteinDAO;
        this.lookupStoreToI5ModelDAO = lookupStoreToI5ModelDAO;
        this.preCalcMatchClient = preCalcMatchClient;
    }

    @Required
    public void setInterproscanVersion(String interproscanVersion) {
        Assert.notNull(interproscanVersion, "Interproscan version cannot be null");
        this.interproscanVersion = interproscanVersion;
    }

    public ProteinDAO getProteinDAO() {
        return proteinDAO;
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setLookupStoreToI5ModelDAO(LookupStoreToI5ModelDAO lookupStoreToI5ModelDAO) {
        this.lookupStoreToI5ModelDAO = lookupStoreToI5ModelDAO;
    }

    /**
     * This client is used to check for existing matches
     * from a web service.  This web service will be provided directly
     * from the EBI, but can also be easily installed locally.
     * (Runs from a BerkeleyDB on Jetty, so can be run from a single
     * command).
     * <p>
     * Essentially, the client will be used if it is available.
     */
    @Required
    public void setPreCalcMatchClient(MatchHttpClient preCalcMatchClient) {
        this.preCalcMatchClient = preCalcMatchClient;
    }

    public void setAnalysisJobMap(Map<String, SignatureLibraryRelease> analysisJobMap) {
        this.analysisJobMap = analysisJobMap;
    }

    @Override
    public void run() {

        String proteinRange = "[" + proteinRanges.get("bottom") + "-" + proteinRanges.get("top") + "]";

        Utilities.verboseLog(1100, "LookupV2 Processing  " + proteins.size() + " range: " + proteinRange);
        int count = 0;
        int batchCount = 0;
        if ( proteinRanges.get("bottom") == 1l) {
            printMemoryUsage("Start of Match lookup Processing " + proteins.size() + " range: " + proteinRange);
            System.gc();
            printMemoryUsage("After first gc in procesing " + proteins.size() + " range: " + proteinRange);
        }

        final Set<Protein> proteinsAwaitingPrecalcLookup = new HashSet<>();
        final Set<Protein> precalculatedProteins = new HashSet<>();
        int proteinsCount = proteins.size();

        int proteinNotInLookupCount = 0;
        //check the kv stores in proteinDAO
        proteinDAO.checkKVDBStores();

        int oldProgressMeter = 0;
        for (Protein protein : proteins) {
            count++;
            if (proteinsAwaitingPrecalcLookup == null) {
                Utilities.verboseLog(proteinRange + "proteinsAwaitingPrecalcLookup is null -- " + proteinsAwaitingPrecalcLookup);
            }
            proteinsAwaitingPrecalcLookup.add(protein);
            if ((proteinsAwaitingPrecalcLookup.size() >= proteinPrecalcLookupBatchSize) || (count >= proteinsCount)) {
                batchCount++;
                Utilities.verboseLog(proteinRange + " lookup up protein batch no. " + batchCount);
                final Set<Protein> localPrecalculatedProteins = getPrecalculated(proteinsAwaitingPrecalcLookup, analysisJobMap);
                boolean printedProteinKeyRep = false;
                if (localPrecalculatedProteins != null) {
                    Utilities.verboseLog(proteinRange + " We have precalculated proteins: " + localPrecalculatedProteins.size());
                    final Map<String, Protein> md5ToPrecalcProtein = new HashMap<>(localPrecalculatedProteins.size());
                    for (Protein precalc : localPrecalculatedProteins) {
                        md5ToPrecalcProtein.put(precalc.getMd5(), precalc);
                    }

                    for (Protein proteinAwaitingPrecalcLookup : proteinsAwaitingPrecalcLookup) {
                        if (md5ToPrecalcProtein.keySet().contains(proteinAwaitingPrecalcLookup.getMd5())) {
                            precalculatedProteins.add(md5ToPrecalcProtein.get(proteinAwaitingPrecalcLookup.getMd5()));
                        } else {
                            //addProteinToBatch(proteinAwaitingPrecalcLookup);
                            String proteinKey = String.valueOf(proteinAwaitingPrecalcLookup.getId());
                            if (!printedProteinKeyRep) {
                                Utilities.verboseLog(1100, "md5ToPrecalcProtein does NOT contain proteinKey Rep: " + proteinKey);
                                printedProteinKeyRep = true;
                            }
                            if (proteinDAO.getLevelDBStore() == null) {
                                LOGGER.error("Something wrong witht the kv store: proteinsNotInLookupDB");
                            }
                            proteinDAO.insertProteinNotInLookup(proteinKey, proteinAwaitingPrecalcLookup);
                        }
                    }
                } else {
                    //there are no matches or we are not using the lookup match service
                    Utilities.verboseLog(proteinRange + " There are NO matches for these proteins: " + proteinsAwaitingPrecalcLookup.size());
                    for (Protein proteinAwaitingPrecalcLookup : proteinsAwaitingPrecalcLookup) {
                        String proteinKey = String.valueOf(proteinAwaitingPrecalcLookup.getId());
                        proteinDAO.insertProteinNotInLookup(proteinKey, proteinAwaitingPrecalcLookup);
                        proteinNotInLookupCount++;
                        //addProteinToBatch(proteinAwaitingPrecalcLookup);
                    }
                }
                precalculatedProteins.addAll(proteinsAwaitingPrecalcLookup);

                // All dealt with, so clear.
                proteinsAwaitingPrecalcLookup.clear();
            }
            int progressMeter = count * 100 / proteinsCount;
            if (progressMeter % 5 == 0 && progressMeter != oldProgressMeter) {
                if ( proteinRanges.get("bottom") == 1l) {
                    if (progressMeter % 5 == 0) {
                        Utilities.verboseLog(0, " LookupProgress " + proteinRange + " : " + progressMeter + "%");
                    }
                    if (progressMeter % 10 == 0) {
                        printMemoryUsage("in lookup " + progressMeter + " % of " + proteinRange);
                    }
                }
                if (progressMeter % 40 == 0) {
                    Utilities.verboseLog(110, " LookupProgress " + proteinRange + " : " + progressMeter + "%");
                } else {

                    Utilities.verboseLog(120, "LookupProgress " + proteinRange + " : " + progressMeter + "%");
                }
                oldProgressMeter = progressMeter;
            }

        }

        //add all the proteins not in the lookup to the concurrent set
        //proteinsWithoutLookupHit.addAll(proteinsNotInLookup);
        //TODO the following lines until end of the method is for testing only, afterwards remove
        List<Protein> proteinsNotInLookup = null;
        try {
            proteinsNotInLookup = proteinDAO.getProteinsNotInLookup();
        } catch (Exception e) {
            e.printStackTrace();
        }


        int proteinsNotInLookupCount = proteinsNotInLookup.size();
        Utilities.verboseLog(110, "1. proteinsNotInLookupCount :  " + proteinsNotInLookupCount);


        Utilities.verboseLog(1100, "2. Precalculated Proteins " + proteinRange + "  size: " + precalculatedProteins.size());
        Utilities.verboseLog(1100, "2. Proteing not in LookUp Service (proteinDAO.proteinsNotInLookupCount) " + proteinRange + "  size: " + proteinsNotInLookupCount);

        //Get all the proteins without a lookup hit


        Utilities.verboseLog(110, " 2. total proteinsNotInLookup   " + proteinRange + " size: " + proteinsNotInLookup.size());
        Utilities.verboseLog(1100, "2. LookupV2 Processing range: " + proteinRange + " completed");

        if ( proteinRanges.get("bottom") == 1l) {
            printMemoryUsage("End of  of Match lookup Processing " + proteins.size() + " range: " + proteinRange);
            System.gc();
            printMemoryUsage("After second gc in procesing " + proteins.size() + " range: " + proteinRange);
        }
    }

    /**
     * Adds a protein to the batch of proteins to be persisted.  If the maximum
     * batch size is reached, store all these proteins (by calling persistBatch().)
     *
     * @param protein being the protein to be stored.
     */
    private void addProteinToBatch(Protein protein) {
        //how do we deal with is method inthe new approach
        //Utilities.verboseLog(1100, "check-protein: " + protein.getId());
        String proteinKey = String.valueOf(protein.getId());
        proteinDAO.insertProteinNotInLookup(proteinKey, protein);
        //proteinsNotInLookup.add(protein);
    }

    private void createAndPersistNewORFs(final ProteinDAO.PersistedProteins persistedProteins) {
        //Holder for new ORFs which should be persisted
        Set<OpenReadingFrame> orfsAwaitingPersistence = new HashSet<>();

        Set<Protein> newProteins = persistedProteins.getNewProteins();
    }

    /**
     * Note - this method returns null if there are no precalculated results.
     *
     * @param protein
     * @return
     */
    @Override
    public Protein getPrecalculated(Protein protein, Map<String, SignatureLibraryRelease> analysisJobMap) {
        // Check if the precalc service is configure and available.
        Utilities.verboseLog(110, "Start getPrecalculated for 1 proteins: " + protein.getId());
        if (!preCalcMatchClient.isConfigured()) {
            return null;
        }

        // Check if the MD5 needs to be reanalyzed
        String lookupMessageStatus = "Checking lookup client and server are in sync";
        try {
            // Only proceed if the lookup client and server are in sync
            if (!isSynchronised()) {
                return null;
            }
            final String upperMD5 = protein.getMd5().toUpperCase();

            lookupMessageStatus = "Check MD5s of proteins analysed previously";

            if (!preCalcMatchClient.getMD5sOfProteinsAlreadyAnalysed(upperMD5).contains(upperMD5)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Protein with MD5 " + upperMD5 + " has not been analysed previously, so the analysis needs to be run.");
                }
                return null;  // Needs to be analysed.
            }
            // Now retrieve the Matches and add to the protein.
            Long startTime = null;
            if (LOGGER.isDebugEnabled()) {
                startTime = System.nanoTime();
            }
            lookupMessageStatus = "Get matches of proteins analysed previously";
//            final KVSequenceEntryXML kvSequenceEntryXML = preCalcMatchClient.getMatches(upperMD5);
            final KVSequenceEntryXML kvSequenceEntryXML = getMatchesFromLookup(upperMD5);
            if (kvSequenceEntryXML == null) {
                Utilities.verboseLog(110, "For this batch, calculate the matches locally - md5 =  " + upperMD5);
                return null;
            }

            long timetaken = System.nanoTime() - startTime;
            long lookupTimeMillis = 0;
            if (timetaken > 0) {
                lookupTimeMillis = timetaken / 1000000;
            }

            Utilities.verboseLog(110, "Time to lookup " + kvSequenceEntryXML.getMatches().size() + " matches for one protein: " + lookupTimeMillis + " millis");

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Time to lookup " + kvSequenceEntryXML.getMatches().size() + " matches for one protein: " + timetaken + "ns");
            }
            if (kvSequenceEntryXML != null) {
                boolean includeCDDorSFLD = includeCDDorSFLD(analysisJobMap);
                KVSequenceEntryXML kvSitesSequenceEntryXML = null;
                if (includeCDDorSFLD) {
                    Utilities.verboseLog(130, "lookup Sites ... ");
                    kvSitesSequenceEntryXML = getSitesFromLookup(upperMD5);
                    //Utilities.verboseLog(1100, "lookup Sites XML:" + kvSitesSequenceEntryXML.toString());
                    Utilities.verboseLog(130, "lookup Sites XML:" + kvSitesSequenceEntryXML.getMatches().size() + " -- " + kvSitesSequenceEntryXML.getMatches().toString());
                }
                lookupStoreToI5ModelDAO.populateProteinMatches(protein, kvSequenceEntryXML.getMatches(), kvSitesSequenceEntryXML.getMatches(), analysisJobMap, includeCDDorSFLD);
            }

            return protein;
        } catch (Exception e) {
            hostAvailabilityCheck(preCalcMatchClient.getUrl());
            displayLookupError(e, lookupMessageStatus);
            return null;
        }

    }

    @Override
    public Set<Protein> getPrecalculated(Set<Protein> proteins, Map<String, SignatureLibraryRelease> analysisJobMap) {
        // Check if the precalc service is configure and available.
        Utilities.verboseLog(110, "Start getPrecalculated for " + proteins.size() + " proteins");
        if (!preCalcMatchClient.isConfigured()) {
            Utilities.verboseLog(110, " preCalcMatchClient is NULL ...");
            return null;
        }

        String lookupMessageStatus = "First checking lookup client and server are in sync";
        Utilities.verboseLog(110, lookupMessageStatus);

        //check if server has been updated
        Double interproscanDataVersion = 0.0;
        Double serverDataVersion = 99.0;
        String lookupServerVersion = "";
        try{
            int finalDashIndex = interproscanVersion.lastIndexOf("-");
            interproscanDataVersion = Double.parseDouble(interproscanVersion.substring(finalDashIndex).replace("-", ""));
            ;
            String serverVersion = preCalcMatchClient.getServerVersion();
            lookupServerVersion = serverVersion;
            int finalDashIndeXForServerVersion = serverVersion.lastIndexOf("-");
            serverDataVersion = Double.parseDouble(serverVersion.substring(finalDashIndeXForServerVersion).replace("-", ""));

            Utilities.verboseLog(110, "interproDataVersion: " +  interproscanDataVersion + " serverDataVersion: " + serverDataVersion);
            if (interproscanDataVersion > serverDataVersion) {
                Utilities.verboseLog(1100, "Lookup service not yet UPDATED - interproDataVersion: " + interproscanDataVersion + " serverVersion:" + serverVersion);
                return null;
            } else {
                Utilities.verboseLog(1100, "Lookup service UPDATED - continue ... " + interproscanDataVersion + ":" + serverDataVersion);
            }
            Utilities.verboseLog(1100, "Lookup isSynchronised? interproDataVersion: " + interproscanDataVersion + " serverVersion:" + serverVersion);

        }catch (Exception e){
            LOGGER.error("Lookup version check failed ... interproscanVersion: " +  interproscanVersion + ":vs Lookup version: " + lookupServerVersion);
            e.printStackTrace();
        }

        try {
            // Only proceed if the lookup client and server are in sync
   if (!isSynchronised()) {
                Utilities.verboseLog(110, "TESTING only: The server and the client DO NOT have the same version or some other errror ");
                return null;
            } else {
                Utilities.verboseLog(110, "TESTING only: The server and the client HAVE the same version");
            }
            // Then, check if the MD5s have been precalculated
            String[] md5s = new String[proteins.size()]; //should this be final
            // Map for looking up proteins by MD5 efficiently.
            final Map<String, Protein> md5ToProteinMap = new HashMap<String, Protein>(proteins.size());
            int i = 0;
            // Both populate the lookup map and also create the array of MD5s to query the service.
            for (Protein protein : proteins) {
                md5ToProteinMap.put(protein.getMd5().toUpperCase(), protein);
                md5s[i++] = protein.getMd5().toUpperCase();
            }
            lookupMessageStatus = "Check MD5s of proteins analysed previously";
            final List<String> analysedMd5s = preCalcMatchClient.getMD5sOfProteinsAlreadyAnalysed(md5s);

            Utilities.verboseLog(110, "GOt MD5sOfProteinsAlreadyAnalysed :" + analysedMd5s.size());
            // Check if NONE have been pre-calculated - if so, return empty set.
            if (analysedMd5s == null || analysedMd5s.size() == 0) {
                return Collections.emptySet();
            }

            // Create a Set of proteins that have been precalculated - this is what will end up being returned.
            final Set<Protein> precalculatedProteins = new HashSet<Protein>(analysedMd5s.size());

            // For the MD5s of proteins that have been pre-calculated, retrieve match data and populate the proteins.
            md5s = new String[analysedMd5s.size()];
            i = 0;
            for (String md5 : analysedMd5s) {
                final String md5Upper = md5.toUpperCase();
                md5s[i++] = md5Upper;
                precalculatedProteins.add(md5ToProteinMap.get(md5Upper));
            }
//            Utilities.verboseLog(110, "precalculatedProteins: "+ precalculatedProteins.toString());
            Long startTime = null;
            startTime = System.nanoTime();

            lookupMessageStatus = "Get matches of proteins analysed previously";
//            final KVSequenceEntryXML kvSequenceEntryXML = preCalcMatchClient.getMatches(md5s);
            Utilities.verboseLog(110, "getMatchesFromLookup .. ");
            final KVSequenceEntryXML kvSequenceEntryXML = getMatchesFromLookup(md5s);
            Utilities.verboseLog(120, "berkeleyMatchXML: " + kvSequenceEntryXML.getMatches().toString());

            //if null is returned from the lookupmatch then may need to be calculated
            if (kvSequenceEntryXML == null) {
                Utilities.verboseLog(120, "For this batch, calculate the matches locally - analysedMd5s.size =  " + analysedMd5s.size());
                Utilities.verboseLog(120, "totalLookedup though: " + totalLookedup);
                return Collections.emptySet();
            }

            totalLookedup = totalLookedup + analysedMd5s.size();
            Utilities.verboseLog(110, "TotalLookedup: " + totalLookedup);
            long timetaken = System.nanoTime() - startTime;
            long lookupTimeMillis = 0;
            if (timetaken > 0) {
                lookupTimeMillis = timetaken / 1000000;
            }


            Utilities.verboseLog(110, "Time to lookup " + kvSequenceEntryXML.getMatches().size() + " matches for " + md5s.length + " proteins: " + lookupTimeMillis + " millis");

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Time to lookup " + kvSequenceEntryXML.getMatches().size() + " matches for " + md5s.length + " proteins: " + lookupTimeMillis + " millis");
            }
            startTime = System.nanoTime();
            // Check if the analysis versions are consistent and then proceed
            int precalculatedProteinsCount = precalculatedProteins.size();
            Utilities.verboseLog(110, "Now check the version consistency : for " + precalculatedProteinsCount + " precalculatedProteins");
            //should we get CDD or SFLD sites
            boolean includeCDDorSFLD = includeCDDorSFLD(analysisJobMap);
            Utilities.verboseLog(1100, "include CDD or SFLD:  ... " + includeCDDorSFLD);
            KVSequenceEntryXML kvSitesSequenceEntryXML = null;

            //Avoid null lists and go for empty lists
            List<KVSequenceEntry> kvSequenceEntrySites = new ArrayList<>();
            if (includeCDDorSFLD) {
                Utilities.verboseLog(130, "Now lookup Sites ... ");
                kvSitesSequenceEntryXML = getSitesFromLookup(md5s);
                if (kvSitesSequenceEntryXML != null) {
                    kvSequenceEntrySites = kvSitesSequenceEntryXML.getMatches();
                }
                Utilities.verboseLog(130, "lookup Sites XML:" + kvSequenceEntrySites.size() + " -- " + kvSequenceEntrySites.toString());
            }

            List<KVSequenceEntry> kvSequenceEntryMatches = new ArrayList<>();
            ;

            if (kvSequenceEntryXML != null) {
                kvSequenceEntryMatches = kvSequenceEntryXML.getMatches();
            }

            if (isAnalysisVersionConsistent(precalculatedProteins, kvSequenceEntryXML.getMatches(), analysisJobMap)) {
                if (kvSequenceEntryMatches != null && kvSequenceEntrySites != null) {
                    Utilities.verboseLog(110, "Analysis versions ARE Consistent ..  populateProteinMatches : kvSequenceEntryMatches " + kvSequenceEntryMatches.size() +
                            " kvSequenceEntrySites: " + kvSequenceEntrySites.size());
                }
                lookupStoreToI5ModelDAO.populateProteinMatches(precalculatedProteins, kvSequenceEntryMatches, kvSequenceEntrySites, analysisJobMap, includeCDDorSFLD);
                Utilities.verboseLog(110, "Completed Populate precalculated Protein Matches:  " + precalculatedProteins.size());
            } else {
                // If the member database version at lookupmatch service is different  from the analysis version in
                // interproscan, then disable the lookup match service for this batch (return null precalculatedProteins )
                Utilities.verboseLog(110, "Analysis versions NOT Consistent");
                return null;
            }
            timetaken = System.nanoTime() - startTime;
            lookupTimeMillis = 0;
            if (timetaken > 0) {
                lookupTimeMillis = timetaken / 1000000;
            }
            Utilities.verboseLog(110, "Time to convert to i5 matches " + kvSequenceEntryXML.getMatches().size() + " matches for " + md5s.length + " proteins: " + lookupTimeMillis + " millis");

            return precalculatedProteins;

        } catch (Exception e) {
            hostAvailabilityCheck(preCalcMatchClient.getUrl());
            displayLookupError(e, lookupMessageStatus);
            return null;
        }

    }

    public KVSequenceEntryXML getMatchesFromLookup(String... md5s) throws InterruptedException {
        int count = 0;
        int maxTries = 4;
        while (true) {
            try {
                KVSequenceEntryXML kvSequenceEntryXML = preCalcMatchClient.getMatches(md5s);
                return kvSequenceEntryXML;
            } catch (UnmarshallingFailureException e) {  //    also covers    UnmarshalException (JAXBException e) {
                // handle exception
                try {
                    Thread.sleep(10 * 1000);  //wait for 10 seconds before trying again
                } catch (InterruptedException exc) {
                    throw exc;
                }
                if (++count == maxTries) {
                    return null;
                }
            } catch (IOException e) {
                // handle exception
                if (++count == maxTries) break;
            } catch (Exception e) {
                if (e instanceof JAXBException) {
                    try {
                        Thread.sleep(10 * 1000);  //wait for 10 seconds before trying again
                    } catch (InterruptedException exc) {
                        throw exc;
                    }
                    if (++count == maxTries) break;
                } else {
                    LOGGER.warn("Lookupmatch server: encountered an unspecific error while getting matches ");
                    throw e;
                }
            }
        }
        return null;
    }


    public KVSequenceEntryXML getSitesFromLookup(String... md5s) throws InterruptedException {
        int count = 0;
        int maxTries = 4;
        while (true) {
            try {
                KVSequenceEntryXML kvSiteSequenceEntryXML = preCalcMatchClient.getSites(md5s);
                return kvSiteSequenceEntryXML;
            } catch (UnmarshallingFailureException e) {  //    also covers    UnmarshalException (JAXBException e) {
                // handle exception
                try {
                    Thread.sleep(10 * 1000);  //wait for 10 seconds before trying again
                } catch (InterruptedException exc) {
                    throw exc;
                }
                if (++count == maxTries) {
                    return null;
                }
            } catch (IOException e) {
                // handle exception
                if (++count == maxTries) break;
            } catch (Exception e) {
                if (e instanceof JAXBException) {
                    try {
                        Thread.sleep(10 * 1000);  //wait for 10 seconds before trying again
                    } catch (InterruptedException exc) {
                        throw exc;
                    }
                    if (++count == maxTries) break;
                } else {
                    LOGGER.warn("Lookupmatch server: encountered an unspecific error while getting matches ");
                    throw e;
                }
            }
        }
        return null;
    }

    /**
     * Utility method to confirm if this service is working.
     *
     * @return true if the service is working.
     */
    public boolean isConfigured() {
        return preCalcMatchClient.isConfigured();
    }

    /**
     * If the client and the server are based on the same version of interproscan
     * return true, otherwise return false
     */
    public boolean isSynchronised() throws IOException {
        // checks if the interpro data version is the same
        // codes version differences are ignored
        // TODO make this more robust - currently assumes everyhting after the last dash is the interpro version
        String serverVersion = preCalcMatchClient.getServerVersion();
        int finalDashIndex = interproscanVersion.lastIndexOf("-");
        String interproDataVersion = interproscanVersion.substring(finalDashIndex);
        Utilities.verboseLog(1100, "Lookup isSynchronised? interproDataVersion: " + interproDataVersion + " serverVersion:" + serverVersion);
        if (!(serverVersion.endsWith(interproDataVersion))) {
            displayLookupSynchronisationError(interproscanVersion, serverVersion);
            return false;
        }

        return true;

    }

    /**
     * If the member database version at lookupmatch service is different  from the analysis version in
     * interproscan, then disable the lookup match service for this batch
     *
     * @param preCalculatedProteins
     * @param kvSequenceEntries
     * @param analysisJobMap
     * @return
     */
    public boolean isAnalysisVersionConsistent(Set<Protein> preCalculatedProteins, List<KVSequenceEntry> kvSequenceEntries, Map<String, SignatureLibraryRelease> analysisJobMap) {
        // Collection of BerkeleyMatches of different kinds.
        Utilities.verboseLog(1100, "Check if AnalysisVersion Consistent  ...");
        Map<String, String> lookupAnalysesMap = new HashMap<String, String>();
        for (KVSequenceEntry kvSequenceEntry : kvSequenceEntries) {
            String proteinMD5 = kvSequenceEntry.getProteinMD5();
            Set<String> sequenceHits = kvSequenceEntry.getSequenceHits();
            for (String sequenceHit : sequenceHits) {
                LOGGER.debug("csvMatch:" + sequenceHit);
                SimpleLookupMatch simpleMatch = new SimpleLookupMatch(proteinMD5, sequenceHit);
                LOGGER.debug("simpleMatch " + simpleMatch.toString());
                String signatureLibraryReleaseVersion = simpleMatch.getSigLibRelease();
                final SignatureLibrary sigLib = SignatureLibraryLookup.lookupSignatureLibrary(simpleMatch.getSignatureLibraryName());
                lookupAnalysesMap.put(sigLib.getName().toUpperCase(), signatureLibraryReleaseVersion);
            }
        }

        //Utilities.verboseLog(1100, "lookupAnalysesMap   ..." + lookupAnalysesMap.keySet());

//        if (analysisJobMap == null){
//            Utilities.verboseLog(1100, "analysisJobMap is null   ..." + analysisJobMap.keySet());
//        }else{
//            Utilities.verboseLog(1100, "analysisJobMap   ..." + analysisJobMap.keySet());
//        }
        for (String analysisJobName : analysisJobMap.keySet()) {
            if (lookupAnalysesMap.containsKey(analysisJobName.toUpperCase())) {
                String lookUpMatchAnalaysVersion = lookupAnalysesMap.get(analysisJobName.toUpperCase());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("analysis: " + analysisJobName + " lookUpMatchAnalaysiVersion: "
                            + lookUpMatchAnalaysVersion + " analysisJobName: " + analysisJobName + " analysisJobVersion: " + analysisJobMap.get(analysisJobName).getVersion());
                }
                if (!lookUpMatchAnalaysVersion.equals(analysisJobMap.get(analysisJobName).getVersion())) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Different versions of  " + analysisJobName + " running ");
                    }
                    Utilities.verboseLog(1100, "Different versions of  " + analysisJobName + " running ");

                    return false;
                }
            }
        }
        Utilities.verboseLog(1100, "Analysis Version is Consistent  ...");
        return true;
    }

    /**
     * include fetching CDD or SFLD sites
     *
     * @param analysisJobMap
     * @return
     */
    private boolean includeCDDorSFLD(Map<String, SignatureLibraryRelease> analysisJobMap) {
        for (SignatureLibraryRelease sigLibrelease : analysisJobMap.values()) {
            if (sigLibrelease.getLibrary().getName().startsWith("CDD") ||
                    sigLibrelease.getLibrary().getName().startsWith("SFLD")) {
                return true;
            }
        }

        return false;
    }

    private void displayLookupError(Exception e, String lookupMessageStatus) {
        /* Barf out - the user wants pre-calculated, but this is not available - tell them what action to take. */

        if (timeLookupError != null) {
            if (!fixedTimeLapsed(timeLookupError)) {
                return;
            }
        }

        timeLookupError = System.currentTimeMillis();

//        LOGGER.warn(e);
//        e.printStackTrace();
//        LOGGER.warn(e.toString());
        LOGGER.warn("Problem with lookup service while on the step: " + lookupMessageStatus);

        LOGGER.warn("\n\n" +
                "The following problem was encountered by the pre-calculated match lookup service:\n" +
                e.getMessage() + "\n" +
                "Pre-calculated match lookup service failed - analysis proceeding to run locally\n" +
                "============================================================\n\n" +
                "The pre-calculated match lookup service has been configured in the interproscan.properties file.  \n" +
                "  precalculated match lookup service url : " + preCalcMatchClient.getUrl() + "\n" +
                "  precalculated match lookup service proxy host : " + preCalcMatchClient.getProxyHost() + "  proxy port : " + preCalcMatchClient.getProxyPort() + "\n\n" +
                "Unfortunately the web service has failed. Check the configuration of this service\n" +
                "in the interproscan.properties file and, if necessary, set the following property to look like this:\n\n" +
                "precalculated.match.lookup.service.url=\n\n" +
                "If the problem persists, check if this is a firewall or proxy issue. If it is a proxy issue, then setting \n" +
                "the following property in the interproscan.properties file should work:\n\n" +
                "precalculated.match.lookup.service.proxy.host=\n" +
                "precalculated.match.lookup.service.proxy.port=\n\n" +
                "If this still does not work please inform the InterPro team of this error\n" +
                "by sending an email to:\n\ninterhelp@ebi.ac.uk\n\n" +
                "In the meantime, the analysis will continue to run locally.\n\n");


    }

    private void displayLookupSynchronisationError(String clientVersion, String serverVersion) {

        if (timeLookupSynchronisationError != null) {
            if (!fixedTimeLapsed(timeLookupSynchronisationError)) {
                return;
            }
        }

        timeLookupSynchronisationError = System.currentTimeMillis();

        if (!Utilities.lookupMatchVersionProblemMessageDisplayed) {
            LOGGER.warn(
                    "\n\nThe version of InterProScan you are using is " + clientVersion + "\n" +
                            "The version of the lookup service you are using is " + serverVersion + "\n" +
                            "As the data in these versions is not the same, you cannot use this match lookup service.\n" +
                            "InterProScan will now run locally\n" +
                            "If you would like to use the match lookup service, you have the following options:\n" +
                            "i) Download the newest version of InterProScan5 from our FTP site by following the instructions on:\n" +
                            "   https://www.ebi.ac.uk/interpro/interproscan.html\n" +
                            "ii) Download the match lookup service for your version of InterProScan from our FTP site and install it locally.\n" +
                            "    You will then need to edit the following property in your configuration file to point to your local installation:\n" +
                            "    precalculated.match.lookup.service.url=\n\n" +
                            "In the meantime, the analysis will continue to run locally.\n\n");

            Utilities.lookupMatchVersionProblemMessageDisplayed = true;
        }
    }

    private Boolean fixedTimeLapsed(Long previousTime) {
        Long fixedTimeBetweenDisplays = 10L;
        Long hoursSince = null;
        Long timeLapse = System.currentTimeMillis() - previousTime;
        if (timeLapse > 0) {
            hoursSince = timeLapse / (60 * 60 * 1000);
            //default is display error message every 10 hours
            if (hoursSince > fixedTimeBetweenDisplays) {
                return true;
            }
        }

        return false;
    }

    public boolean hostAvailabilityCheck(String SERVER_ADDRESS) {
        boolean available = true;
        String hostAvailabilityMessage = "";
        Boolean usingProxy = false;
        URL url = null;
        HttpURLConnection httpConn = null;
        try {
            url = new URL(SERVER_ADDRESS);
            try {
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setInstanceFollowRedirects(false);
                httpConn.setRequestMethod("HEAD");
                usingProxy = httpConn.usingProxy();
                httpConn.connect();
                hostAvailabilityMessage = "accessible - code: " + httpConn.getResponseCode();
            } catch (NoRouteToHostException e) {
                available = false;
                hostAvailabilityMessage = "not avaliable, NoRouteToHostException : " + e.getMessage();
            } catch (ConnectException e) {
                available = false;
                hostAvailabilityMessage = "not avaliable, ConnectException : " + e.getMessage();
            } catch (IOException e) { // io exception, service probably not running
                available = false;
                hostAvailabilityMessage = "not avaliable, IOException : " + e.getMessage();
            } catch (Exception e) { // exception, service probably not running
                available = false;
                hostAvailabilityMessage = "not avaliable, Exception : " + e.getMessage();
            } finally {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
            }
        } catch (MalformedURLException e) {
            available = false;
            hostAvailabilityMessage = " not avaliable, MalformedURLException : " + e.getMessage();
        }

        hostAvailabilityMessage = "lookupUp service at " + SERVER_ADDRESS + " is " + hostAvailabilityMessage + ",  using proxy: " + usingProxy;

        LOGGER.warn(hostAvailabilityMessage);
        return available;
    }

    public boolean hostAvailabilityCheck2(String SERVER_ADDRESS, int TCP_SERVER_PORT) {
        boolean available = true;
        String hostAvailabilityMessage = "";
        try {
            Socket lookupSocket = new Socket(SERVER_ADDRESS, TCP_SERVER_PORT);
            if (lookupSocket.isConnected()) {
                lookupSocket.close();
                hostAvailabilityMessage = "lookupUp service is available and accessible";
            }
        } catch (UnknownHostException e) { // unknown host
            available = false;
            hostAvailabilityMessage = "lookupUp service is not avaliable, UnknownHostException : " + e.getMessage();
        } catch (IOException e) { // io exception, service probably not running
            available = false;
            hostAvailabilityMessage = "lookupUp service is not avaliable, IOException : " + e.getMessage();
        } catch (NullPointerException e) {
            available = false;
            hostAvailabilityMessage = "lookupUp service is not avaliable, NullPointerException : " + e.getMessage();
        }

        return available;
    }

    private void printMemoryUsage(String stepName){
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        System.out.println("##### Heap utilization statistics [MB]  at " + stepName + " #####");

        System.out.println("Used Memory:"
                + (runtime.totalMemory() - runtime.freeMemory()) / mb
                + "\t Free Memory:"
                + runtime.freeMemory() / mb
                + "\t Total Memory:" + runtime.totalMemory() / mb
                + "\t Max Memory:" + runtime.maxMemory() / mb);
    }


}
