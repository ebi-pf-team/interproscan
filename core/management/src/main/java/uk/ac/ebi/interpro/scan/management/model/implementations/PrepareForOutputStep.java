package uk.ac.ebi.interpro.scan.management.model.implementations;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.match.writer.ProteinMatchesXMLJAXBFragmentsResultWriter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.EntryKVDAO;
import uk.ac.ebi.interpro.scan.persistence.MatchDAO;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrepareForOutputStep extends Step {

    private static final Logger LOGGER = LogManager.getLogger(PrepareForOutputStep.class.getName());
    private static final int MAX_NUM_DOMAINS_BY_GROUP = 20;
    private static final double DOMAIN_OVERLAP_THRESHOLD = 0.3;

    //DAOs
    private ProteinDAO proteinDAO;
    private MatchDAO matchDAO;
    private EntryKVDAO entryKVDAO;

    private String entryKVPath;

    private NucleotideSequenceDAO nucleotideSequenceDAO;

    final ConcurrentHashMap<Long, Long> allNucleotideSequenceIds = new ConcurrentHashMap<>();
    final ConcurrentHashMap<Long, Boolean> processedNucleotideSequences = new ConcurrentHashMap<>();
    final Set<String> processesReadyForXMLMarshalling = new HashSet<>();

    private Map<String, Collection<GoXref>> entry2GoXrefsMap;
    private Map<String, Collection<PathwayXref>> entry2PathwayXrefsMap;

    private ConcurrentHashMap<String, List<String>> gotermsMap;
    private String goVersion;
    private ConcurrentHashMap<String, List<String>> entry2GoTermsMap;
    private ConcurrentHashMap<String, List<String>> entry2PathwayMap;
    private ConcurrentHashMap<String, List<String>> pathwayMap;
    private ConcurrentHashMap<String, String> typesMap;
    private ConcurrentHashMap<String, Integer> domainsMap;
    private ConcurrentHashMap<String, Integer> familiesMap;

    Random random = new Random();

    public static final String SEQUENCE_TYPE = "SEQUENCE_TYPE";

    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    public void setMatchDAO(MatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    public void setEntryKVDAO(EntryKVDAO entryKVDAO) {
        this.entryKVDAO = entryKVDAO;
    }

    public void setNucleotideSequenceDAO(NucleotideSequenceDAO nucleotideSequenceDAO) {
        this.nucleotideSequenceDAO = nucleotideSequenceDAO;
    }

    public String getEntryKVPath() {
        return entryKVPath;
    }

    public void setEntryKVPath(String entryKVPath) {
        this.entryKVPath = entryKVPath;
    }

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();

        final Set<Long> nucleotideSequenceIds = new HashSet<>();

        String proteinRange = "[" + stepInstance.getBottomProtein() + "_" + stepInstance.getTopProtein() + "]";
        Utilities.verboseLog(1100, "Starting PrepareForOutputStep :" + proteinRange);

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        Utilities.verboseLog(110, " PrepareForOutputStep :  There are " + (topProteinId - bottomProteinId) + " proteins.");

        Long totalNucleotideSequences = nucleotideSequenceDAO.count();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting step with Id " + this.getId());
        }

        Utilities.verboseLog(110, "temporaryFileDirectory: " + temporaryFileDirectory);

        Set<String> signatureLibraryNames = new HashSet<>();

        for (SignatureLibrary sig : SignatureLibrary.values()) {
            signatureLibraryNames.add(sig.getName());
        }

        if (bottomProteinId == 1) {
            Utilities.printMemoryUsage("at start of preparing  [" + proteinRange + " proteins");
        }

        //exponentioanl backoff setup for failure
        int proteinSetFactor = topProteinId.intValue() * 2000 / 1000; //use the input range size
        int randomInt =  random.nextInt(60 - 30) + 30;  // random number betwen 60 and 120 seconds

        int randomMilliSeconds = proteinSetFactor + (randomInt *  1000);
        int exponentionalBackOffTime = randomMilliSeconds * ( stepInstance.getExecutions().size()  - 1);
        Utilities.verboseLog(10, "How many executions  of this Step have we done before including this Step: "
                + stepInstance.getExecutions().size());
        if (exponentionalBackOffTime > 0) {
            Utilities.verboseLog(10, "randomMilliSeconds: " + randomMilliSeconds + " ExponentionalBackOffTime: " + exponentionalBackOffTime);
            Utilities.sleep(exponentionalBackOffTime);
            Utilities.verboseLog(10, "Backed off for : " + exponentionalBackOffTime);
        }

        if (entryKVDAO == null) {
            Utilities.verboseLog(30, "entryKVDAO is null ");
        }else {
            Utilities.verboseLog(30, entryKVDAO.toString());
            if (entryKVDAO.getKvStoreEntry() != null) {
                Utilities.verboseLog(30,  "dbname :" + entryKVDAO.getKvStoreEntry().getDbName());
            }else {
                Utilities.verboseLog(30, "entryKVDAO.getKvStoreEntry() is null  ");
            }
        }

        final Map<String, String> parameters = stepInstance.getParameters();
        final boolean mapToGO = Boolean.TRUE.toString().equals(parameters.get("MAP_TO_GO"));
        final boolean mapToPathway = Boolean.TRUE.toString().equals(parameters.get("MAP_TO_PATHWAY"));
        // Note: MAP_TO_INTERPRO_ENTRIES is always true (iprlookup hard-coded to true in Run.java)
        final boolean mapToInterPro = mapToGO || mapToPathway || Boolean.TRUE.toString().equals(parameters.get("MAP_TO_INTERPRO_ENTRIES"));

        try {
            if (mapToPathway) {
                getPathwayMap();
                getEntry2PathwayMap();
            }

            if (mapToGO) {
                loadGoTerms();
                loadInterPro2Go();
            }

            getEntriesMaps();

            //proceed to rest of functionality
            Utilities.verboseLog(1100, "Pre-marshall the proteins ...");
            simulateMarshalling(stepInstance, "p", temporaryFileDirectory, mapToGO, mapToInterPro);
            Utilities.verboseLog(1100, "Pre-marshall the nucleotide sequences ...");

            final String sequenceType = parameters.get(SEQUENCE_TYPE);
            if (sequenceType.equalsIgnoreCase("n")) {
                Utilities.verboseLog(1100, "Dealing with nucleotide sequences ... , so pre-marshalling required");
                if (bottomProteinId == 1) {
                    Utilities.printMemoryUsage("Start nucleotide sequences - preparing  [" + proteinRange + " proteins");
                }
                processNucleotideSequences(stepInstance, totalNucleotideSequences, temporaryFileDirectory);
            } else {
                Utilities.verboseLog(1100, "Dealing with proteins  ... , so pre-marshalling already done");
            }
            Utilities.verboseLog(1100, "Completed prepraring the protein range  ..." + proteinRange);
        } catch (IOException e) {
            LOGGER.warn("Problem (IOException on marshalling)  in the  Prepare proteins for output step " + proteinRange);
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage()); //Exception(e.getMessage());
        } catch (Exception e){
            LOGGER.warn("Error in Prepare proteins for output step - " + proteinRange);
            e.printStackTrace();
            throw e;
        }
    }


    private void processNucleotideSequences(StepInstance stepInstance, Long totalNucleotideSequences, String temporaryFileDirectory) {
        //
        //should we deal with nucleotides here
        //Utilities.verboseLog(1100, "proteinWithXref: \n" +  proteinWithXref.toString());
        Utilities.verboseLog(30, "Start processNucleotideSequences - total size expected : " + totalNucleotideSequences);
        final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();

        final Set<Long> nucleotideSequenceIds = new HashSet<>();

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();


        int proteinCount = 0;

        Set<String> signatureLibraryNames = new HashSet<>();

        for (SignatureLibrary sig : SignatureLibrary.values()) {
            signatureLibraryNames.add(sig.getName());
        }
        Long proteinsConsidered = topProteinId - bottomProteinId;

        String proteinRange = "[" + stepInstance.getBottomProtein() + "_" + stepInstance.getTopProtein() + "]";
        List<Long> proteinBreakPoints = new ArrayList<>();
        proteinBreakPoints.add(10l);
        for (Long breakIndex = 2000l; breakIndex <= proteinsConsidered; breakIndex += 2000) {
            proteinBreakPoints.add(breakIndex);
        }

        int maxTryCount = 12;
        int totalWaitTime = 0;

        ArrayList<Pair<Integer, Integer>> observedTryCounts = new ArrayList<>();

        for (Long proteinIndex = bottomProteinId; proteinIndex <= topProteinId; proteinIndex++) {
            String proteinKey = Long.toString(proteinIndex);
            //
            //deal with the case of updated sst files
            int tryCount = 0;
            Protein protein = null;
            while (tryCount <= maxTryCount) {
                try {
                    protein = proteinDAO.getProteinAndCrossReferencesByProteinId(proteinIndex);
                    break;
                } catch (Exception exception) {
                    //dont recover but sleep for a few seconds and try again
                    Utilities.verboseLog(100, "Exception type: " + exception.getClass());
                    //how long to wait for files to be available ??
                    int waitTime = (proteinsConsidered.intValue() / 2000) * 2 * 1000;
                    String errorMessage = "Failed to get the associated proteinobject in processNucleotideSequences: + " + proteinKey;
                    totalWaitTime = handleKVStoreExceptions(tryCount, maxTryCount, waitTime, totalWaitTime, exception, errorMessage);
                }
                tryCount++;
            }
            observedTryCounts.add(Pair.of(tryCount, totalWaitTime));
            //Protein protein = proteinDAO.getProtein(proteinKey);
            if (protein == null) {
                continue;
            }
            proteinCount++;

            for (OpenReadingFrame orf : protein.getOpenReadingFrames()) {
                //Utilities.verboseLog(1100, "OpenReadingFrame: \n" +  orf.toString());
                NucleotideSequence seq = orf.getNucleotideSequence();
                //Utilities.verboseLog(1100, "NucleotideSequence: \n" +  seq.toString());

                if (seq != null) {
                    Long seqId = seq.getId();
                    //nucleotideSequences.add(seq);
                    nucleotideSequenceIds.add(seqId); //store the Id
                    allNucleotideSequenceIds.put(seqId, seqId);
                    //                    Hibernate.initialize(seq);
                    //                    nucleotideSequenceDAO.persist(key, seq);
                }
            }
            //help garbage collection??
            if (bottomProteinId == 1 && proteinBreakPoints.contains(proteinIndex)) {
                Utilities.printMemoryUsage("processNucleotideSequences: GC scheduled at breakIndex = " + proteinIndex);
            }
            if (proteinCount % 500 == 0) {
                Utilities.verboseLog(30, proteinRange + " processed " + proteinCount + " proteins of "
                        + proteinsConsidered + " proteins from "
                        + " nucleotideSequenceIds: " + nucleotideSequenceIds.size());
            }

        }
        Utilities.verboseLog(30, proteinRange + " Completed creating the nucleotideSequences and resp. " +
                "nucleotideSequenceIds: " + nucleotideSequenceIds.size()
                + " allNucleotideSequenceIds: " + allNucleotideSequenceIds.size());
        int expectedPrepareJobCount = Utilities.prepareOutputInstances;

        Utilities.verboseLog(30, proteinRange + " Start to prepare XML for nucleotideSequences - "
                + " cpu count: " + Utilities.cpuCount
                + " expectedPrepareJobCount " + expectedPrepareJobCount);
        //maybe wait
        //some nucleotides dont have ORFs??

        processesReadyForXMLMarshalling.add(proteinRange);
        try {
            //set a break clause in case something amiss happens
            while (processesReadyForXMLMarshalling.size() < expectedPrepareJobCount) {
                Utilities.verboseLog(30, proteinRange + " processesReadyForXMLMarshalling: " + processesReadyForXMLMarshalling.size()
                        + " expectedPrepareJobCount: " + expectedPrepareJobCount);
                Thread.sleep(30 * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Utilities.verboseLog(30, proteinRange + " after waiting - " +
                " processesReadyForXMLMarshalling : " + processesReadyForXMLMarshalling.size() +
                " nucleotideSequenceIds: " + nucleotideSequenceIds.size()
                + " allNucleotideSequenceIds: " + allNucleotideSequenceIds.size()
                + " totalNucleotideSequences: " + totalNucleotideSequences);

        if (nucleotideSequenceIds.size() > 0) {
            try {
                //outputNTToXML(stepInstance, "n", nucleotideSequences);
                outputToXML(stepInstance, "n", nucleotideSequenceIds, temporaryFileDirectory);
                //outputToJSON(stepInstance, "n", nucleotideSequences);
            } catch (IOException e) {
                LOGGER.error("Error writing to xml");
                e.printStackTrace();
            }
        }
        Utilities.verboseLog(30, proteinRange + "Completed marshalling to XML for nucleotideSequences size: " +
                "nucleotideSequenceIds: " + nucleotideSequenceIds.size()
                + " allNucleotideSequenceIds: " + allNucleotideSequenceIds.size());
    }


    private void simulateMarshalling(StepInstance stepInstance, String sequenceType, String temporaryFileDirectory,
                                     boolean mapToGo, boolean mapToInterPro) throws IOException {
        if (!sequenceType.equalsIgnoreCase("p")) {
            //maybe we should simulate for all types
            //return;
        }
        final boolean isSlimOutput = false;
        //final String interProScanVersion = "5-34";

        Path outputPath = getFinalPath(stepInstance, temporaryFileDirectory, FileOutputFormat.XML);

        Utilities.verboseLog(110, " Prepare For OutputStep - prepare to output proteins for XML: " + outputPath);

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();
        Long proteinsConsidered = topProteinId - bottomProteinId;

        String proteinRange = "[" + stepInstance.getBottomProtein() + "_" + stepInstance.getTopProtein() + "]";

        List<Long> proteinBreakPoints = new ArrayList<>();
        proteinBreakPoints.add(10l);
        for (Long breakIndex = 2000l; breakIndex <= proteinsConsidered; breakIndex += 2000) {
            proteinBreakPoints.add(breakIndex);
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Load " + topProteinId + " proteins from the db.");
        }
        int count = 0;
        int proteinCount = 0;
        int matchCount = 0;

        //try (ProteinMatchesXMLJAXBFragmentsResultWriter writer = new ProteinMatchesXMLJAXBFragmentsResultWriter(outputPath, Protein.class, isSlimOutput)) {
        //writer.header(interProScanVersion);
        //writer.header(interProScanVersion,   "protein-matches");
        Set<String> signatureLibraryNames = new HashSet<>();

        for (SignatureLibrary sig : SignatureLibrary.values()) {
            signatureLibraryNames.add(sig.getName());
        }

        Long middleProtein = topProteinId / 2;
        int maxTryCount = 12;

        int totalWaitTime = 0;
        ArrayList<Pair<Integer, Integer>> observedTryCounts4Proteins = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> observedTryCounts4Matches = new ArrayList<>();

        for (Long proteinIndex = bottomProteinId; proteinIndex <= topProteinId; proteinIndex++) {
            String proteinKey = Long.toString(proteinIndex);
            //Protein protein  = proteinDAO.getProteinAndCrossReferencesByProteinId(proteinIndex);
            int tryCount = 0;
            Protein protein = null;
            totalWaitTime = 0;
            while (tryCount <= maxTryCount) {
                try {
                    //int randomInt =  random.nextInt(3 - 1) + 1;  //TODO remove test
                    //if (randomInt == 1) {
                    //    throw new IllegalStateException("Root cause of this error is not known... but proteinindex :" + proteinIndex);
                    //}
                    protein = proteinDAO.getProtein(proteinKey);
                    break; //otherwise we have an infinite loop
                } catch (Exception exception) {
                    //dont recover but sleep for a few seconds and try again
                    Utilities.verboseLog(1100, "Exception type: " + exception.getClass());
                    int waitTime = (proteinsConsidered.intValue() / 1000) * 1 * 1000;
                    String errorMessage = "Proteing get: There is a problem processing results for protein internal Identifier: " + proteinIndex +
                            ". You may not get the matches for this protein in the final results. You should report the errors.";
                    totalWaitTime = handleKVStoreExceptions(tryCount, maxTryCount, waitTime, totalWaitTime, exception, errorMessage);
                }
                tryCount++;
            }
            if (totalWaitTime > 0) {
                Utilities.verboseLog(20, "  Prepare for output - Total wait time : " + totalWaitTime
                        + " millis, average wait time " + totalWaitTime / tryCount + " millis, " +
                        " retries : " + tryCount);
                observedTryCounts4Proteins.add(Pair.of(tryCount, totalWaitTime));
            }

            if (protein != null) {
                proteinCount++;
            } else {
                LOGGER.warn("Failed to get match results for protein identifier: + " + proteinKey);
                continue;
            }

            totalWaitTime = 0;
            ArrayList<Domain> domains = new ArrayList<>();
            ArrayList<Domain> families = new ArrayList<>();
            for (String signatureLibraryName : signatureLibraryNames) {
                final String dbKey = proteinKey + signatureLibraryName;

                Set<Match> matches = null;
                //try this say three times
                tryCount = 0;
                totalWaitTime = 0;
                while (tryCount <= maxTryCount) {
                    try {
                        matches = matchDAO.getMatchSet(dbKey);
                        break;
                    } catch (Exception exception) {
                        //dont recover but sleep for a few seconds and try again
                        Utilities.verboseLog(1100, "Exception type: " + exception.getClass());
                        int waitTime = (proteinsConsidered.intValue() / 1000) * 1 * 1000;
                        String errorMessage = "Match get: There is a problem processing results for protein: " + protein.getCrossReferences().iterator().next().getIdentifier() +
                                ". You may not get the matches for this protein in the final results. You should report the errors.";
                        totalWaitTime = handleKVStoreExceptions(tryCount, maxTryCount, waitTime, totalWaitTime, exception, errorMessage);
                    }
                    tryCount++;
                }
                if (totalWaitTime > 0) {
                    Utilities.verboseLog(20, "  Prepare for output - Total wait time : " + totalWaitTime
                            + " millis, average wait time " + totalWaitTime / tryCount + " millis, " +
                            " retries : " + tryCount);
                    observedTryCounts4Matches.add(Pair.of(tryCount, totalWaitTime));
                }

                if (matches != null) {
                    for (Match match : matches) {
                        if (match instanceof PantherMatch) {
                            PantherMatch pantherMatch = (PantherMatch) match;
                            Set<GoXref> goXrefs = new HashSet<>();

                            if (mapToGo) {
                                pantherMatch.getGoXRefs().forEach(
                                        goXref -> {
                                            String goId = goXref.getIdentifier();
                                            List<String> goLine = gotermsMap.get(goId);

                                            if (goLine != null) {
                                                String goName = goLine.get(0);
                                                String goCategoryCode = goLine.get(1);
                                                GoCategory category = GoCategory.parseNameCode(goCategoryCode);
                                                goXrefs.add(new GoXref(goId, goName, category));
                                            }
                                        }
                                );
                            }

                            pantherMatch.setGoXRefs(goXrefs);
                        }

                        if (this.typesMap.containsKey(match.getSignature().getAccession())) {
                            String type = (String) this.typesMap.get(match.getSignature().getAccession());
                            match.getSignature().setType(type);
                        }

                        if (this.domainsMap.containsKey(match.getSignature().getAccession())) {
                            int databaseRank = this.domainsMap.get(match.getSignature().getAccession());
                            Set<Location> locations = match.getLocations();
                            if (locations != null) {
                                for (Location location: locations) {
                                    domains.add(new Domain(location, databaseRank));
                                }
                            }
                        }

                        if (this.familiesMap.containsKey(match.getSignature().getAccession())) {
                            int databaseRank = this.familiesMap.get(match.getSignature().getAccession());
                            Set<Location> locations = match.getLocations();
                            if (locations != null) {
                                for (Location location: locations) {
                                    families.add(new Domain(location, databaseRank));
                                }
                            }
                        }

                        Entry simpleEntry = match.getSignature().getEntry();
                        if (simpleEntry != null) {
                            if (mapToInterPro) {
                                String entryAc = simpleEntry.getAccession();

                                try {
                                    Entry entry =  updateEntryXrefs(simpleEntry);
                                    match.getSignature().setEntry(entry);
                                } catch (Exception e) {
                                    LOGGER.warn("Could get the entry in the kvstore " + entryAc);
                                    e.printStackTrace();
                                }
                            } else {
                                match.getSignature().setEntry(null);
                            }
                        }

                        protein.addMatch(match);
                        matchCount++;
                    }
                }
            }

            if (domains.size() > 0) {
                selectRepresentative(domains);
            }

            if (families.size() > 0) {
                selectRepresentative(families);
            }

            //TODO Temp check what breaks if you dont do pre-marshalling
            //String xmlProtein = writer.marshal(protein);

            //try to persist three times and then abort
            int persistTries = 0;
            int maxPersistTries = 4;
            int persistTotalWaitTime = 0;
            while (persistTries <= maxPersistTries) {
                try {
                    proteinDAO.persist(proteinKey, protein);
                    break; //otherwise we have an infinite loop
                } catch (Exception exception) {
                    int persistWaitTime = 1000; //1000 millis is okay
                    String errorMessage = "Persist: There is a problem processing results for protein: " + protein.getCrossReferences().iterator().next().getIdentifier() +
                            ". You may not get the matches for this protein in the final results. You should report the errors.";
                    persistTotalWaitTime = handleKVStoreExceptions(persistTries, maxPersistTries, persistWaitTime, persistTotalWaitTime, exception, errorMessage);
                }
                persistTries++;
            }
            //help garbage collection??
            if (bottomProteinId == 1 && proteinBreakPoints.contains(proteinIndex)) {
                Utilities.printMemoryUsage("after GC scheduled at breakIndex = " + proteinIndex);
            }
        }

        //if (observedTryCounts4Proteins.size() > 0) {
        String kvGetStats4Proteins = getTryCountStats(observedTryCounts4Proteins);
        Utilities.verboseLog(10, " Info: " +
                proteinRange + " pcounts " + kvGetStats4Proteins
        );
        //}
        //if (observedTryCounts4Matches.size() > 0) {
        String kvGetStats4Matches = getTryCountStats(observedTryCounts4Matches);
        Utilities.verboseLog(10," Info: " +
                proteinRange + " mcounts " + kvGetStats4Matches
        );
        //}
        deleteTmpMarshallingFile(outputPath);

        //int randomInt =  random.nextInt(3 - 1) + 1;  //TODO remove test
        //if (randomInt == 1) {
        //    LOGGER.warn("Exception will be thrown... here for test");
        //    throw new ParseException("Testing Root cause of ... ... ... but proteins range is : " + proteinRange +
        //            " \n How many times have we run this step before : " + stepInstance.getExecutions().size() );
        //}
    }

    /**
     *  if the kvstore is returning some exception, its mostly due to file system problems,
     *  so allow to try again
     *
     * @param tryCount
     * @param maxTryCount
     * @param waitTime
     * @param totalWaitTime
     * @param exc
     * @param errorMessage
     * @return
     */
    private int handleKVStoreExceptions(int tryCount, int maxTryCount, int waitTime, int totalWaitTime, Exception exc, String errorMessage) {
        if (tryCount < maxTryCount - 1) {
            int randomInt =  random.nextInt(6000 - 400) + 400;  //get random number and use to calculate wait time
            waitTime = (randomInt * tryCount) + waitTime;
            if (getKvStoreDelayMilliseconds() < waitTime) {
                if (waitTime > 90 * 1000) {
                    waitTime = 6 * 1000;
                }
                Utilities.sleep(waitTime);
            } else {
                delayForKVStore();
                waitTime = getKvStoreDelayMilliseconds();
            }
            totalWaitTime += waitTime;
            Utilities.verboseLog(110, "  Prepare for output - Slept for at least " + waitTime + " millis");
        } else {
            exc.printStackTrace();
            LOGGER.warn(errorMessage);
        }

        return totalWaitTime;
    }

    private void outputToXML(StepInstance stepInstance, String sequenceType, Set<Long> nucleotideSequenceIds, String temporaryFileDirectory) throws IOException {
        if (!sequenceType.equalsIgnoreCase("n")) {
            return;
        }
        final boolean isSlimOutput = false;
        final String interProScanVersion = "5-34";

        Path outputPath = getFinalPath(stepInstance, temporaryFileDirectory, FileOutputFormat.XML);

        Utilities.verboseLog(110, " Prepare For OutputStep - output Nucleotide sequences to XML: " + outputPath);

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        Long proteinsConsidered = topProteinId - bottomProteinId;
        int nucleotideCount = nucleotideSequenceIds.size();

        String proteinRange = "[" + stepInstance.getBottomProtein() + "_" + stepInstance.getTopProtein() + "]";

        List<Long> proteinBreakPoints = new ArrayList<>();

        for (Long breakIndex = 500l; breakIndex <= nucleotideCount; breakIndex += 500) {
            proteinBreakPoints.add(breakIndex);
        }
        if (bottomProteinId == 1) {
            Utilities.printMemoryUsage("outputToXML: start proessing the nucleotides for xml" + proteinRange
                    + " nucleotideCount: " + nucleotideCount);
        }

        try (ProteinMatchesXMLJAXBFragmentsResultWriter writer = new ProteinMatchesXMLJAXBFragmentsResultWriter(outputPath, NucleotideSequence.class, isSlimOutput)) {
            //writer.header(interProScanVersion);
            if (!nucleotideSequenceIds.isEmpty()) {
                Utilities.verboseLog(110, " nucleotideSequenceIds  : " + nucleotideSequenceIds.size());

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Load " + topProteinId + " proteins from the db.");
                }
                Long orfCount = 0l;
                Long count = 0l;
                writer.header(interProScanVersion, "nucleotide-sequence-matches");
                //final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();
                for (Long nucleotideSequenceId : nucleotideSequenceIds) {
                    if (processedNucleotideSequences.contains(nucleotideSequenceId)) {
                        continue;
                    }
                    //else claim
                    processedNucleotideSequences.put(nucleotideSequenceId, true);

                    count++;
                    Utilities.verboseLog(120, "#: " + count + "nucleotideSequenceId  : " + nucleotideSequenceId);

                    NucleotideSequence nucleotideSequenceInH2 = nucleotideSequenceDAO.getNucleotideSequence(nucleotideSequenceId);
                    String nucleotideSequenceKey = nucleotideSequenceInH2.getMd5();
                    Set<NucleotideSequenceXref> nucleotideSequenceCrossReferences = nucleotideSequenceInH2.getCrossReferences();
                    if (Utilities.verboseLogLevel >= 120) {
                        for (NucleotideSequenceXref nucleotideXref : nucleotideSequenceCrossReferences) {
                            String nucleotideXrefIdentifier = nucleotideXref.getIdentifier();
                            Utilities.verboseLog(120, count + " nucleotideXrefIdentifier: " + nucleotideXrefIdentifier);
                        }
                    }
                    //nucleotideSequenceDAO.persist(nucleotideSequenceKey, nucleotideSequenceInH2);
                    //NucleotideSequence  nucleotideSequence = nucleotideSequenceDAO.get(nucleotideSequenceKey);

                    int orfCountPerSequence = 0;
                    for (OpenReadingFrame orf : nucleotideSequenceInH2.getOpenReadingFrames()) {
                        Protein protein = orf.getProtein();
                        orfCount++;
                        orfCountPerSequence++;
                        // String proteinKey = Long.toString(protein.getId());
                        //Protein proteinMarshalled = proteinDAO.getProtein(proteinKey);
                        //protein = proteinMarshalled;
                        //orf.setProtein(proteinMarshalled);
                    }
                    Utilities.verboseLog(110, "\n#" + count + " nucleotideSequenceInH2: " + nucleotideSequenceId + " orfCount: " + orfCountPerSequence); // + nucleotideSequenceInH2.toString());

                    String xmlNucleotideSequence = writer.marshal(nucleotideSequenceInH2);
                    if (Utilities.verboseLogLevel > 120) {
                        Utilities.verboseLog(120, "\n#" + count + " xmlNucleotideSequence: " + xmlNucleotideSequence);
                    }
                    //String key = nucleotideSequence.getMd5();
                    nucleotideSequenceDAO.persist(nucleotideSequenceKey, nucleotideSequenceInH2);
                    //Utilities.verboseLog(1100, "Prepae OutPut xmlNucleotideSequence : " + nucleotideSequenceId + " -- "); // +  xmlNucleotideSequence);
                    //break;
                    if (bottomProteinId == 1 && proteinBreakPoints.contains(orfCount)) {
                        Utilities.printMemoryUsage("outputToXML " + proteinRange + " scheduled at orfcount breakIndex = "
                                + orfCount + " ns count: " + count + " of " + nucleotideSequenceIds.size()
                                + " All processedNucleotideSequences: " + processedNucleotideSequences.size());
                    }
                }
                Utilities.verboseLog(30, "PrepareforOutPut completed xml marshalling for orfcount count: " + orfCount
                        + " processed nucleotideSequences: " + count
                        + " of nucleotideSequenceIds size: " + nucleotideSequenceIds.size());

                Utilities.verboseLog(30, "PrepareforOutPut nucleotideSequenceIds size: " + nucleotideSequenceIds.size());

            }

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        //delete the tmp xml file
        deleteTmpMarshallingFile(outputPath);
    }


    Path getFinalPath(StepInstance stepInstance, String temporaryFileDirectory, FileOutputFormat fileOutputFormat) {
        //stepInstance.buildFullyQualifiedFilePath();

        final String OUTPUT_EXPLICIT_FILE_PATH_KEY = "EXPLICIT_OUTPUT_FILE_PATH";
        final Map<String, String> parameters = stepInstance.getParameters();
        final boolean explicitPath = false; //parameters.containsKey(OUTPUT_EXPLICIT_FILE_PATH_KEY);

        final String OUTPUT_FILE_FORMATS = "OUTPUT_FORMATS";

        final String outputFormatStr = parameters.get(OUTPUT_FILE_FORMATS);
        final Set<FileOutputFormat> outputFormats = FileOutputFormat.stringToFileOutputFormats(outputFormatStr);

        final String OUTPUT_FILE_PATH_KEY = "OUTPUT_PATH";
        String filePathName = (explicitPath)
                ? parameters.get(OUTPUT_EXPLICIT_FILE_PATH_KEY)
                : parameters.get(OUTPUT_FILE_PATH_KEY);

        Utilities.verboseLog(1100, "filePathName: " + filePathName);

        String sequenceLabel = "prot";
        final String sequenceType = parameters.get(SEQUENCE_TYPE);
        if (sequenceType.equalsIgnoreCase("n")) {
            sequenceLabel = "nt";
        }

        filePathName = temporaryFileDirectory + File.separator + sequenceLabel + ".prepare"; //overwite the filepath as these files are temp so they should be in the temp folder

        String proteinRange = stepInstance.getBottomProtein() + "_" + stepInstance.getTopProtein();
        filePathName = filePathName + "." + proteinRange + ".tmp";

        Path outputPath = getPathName(explicitPath, filePathName, fileOutputFormat);
        return outputPath;
    }

    private Path getPathName(final boolean explicitPath,
                             final String filePathName,
                             final FileOutputFormat outputFormat) {
        // E.g. for "-b OUT" filePathName = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT"
        Path outputPath = null;
        boolean archiveSVGOutput = false;  //TEMP

        if (explicitPath) {
            outputPath = Paths.get(filePathName);
            if (Files.exists(outputPath)) {
                try {
                    Files.delete(outputPath);
                } catch (IOException e) {
                    final String p = outputPath.toAbsolutePath().toString();
                    System.out.println("Unable to overwrite file " + p + ".  Please check file permissions.");
                    System.exit(101);
                }
            }
        } else {
            // Try to use the file name provided. If the file already exists, append a bracketed number (Chrome style).
            // but using an underscore rather than a space (pah!)
            Integer counter = null;
            int ioCounter = 0;
            boolean pathAvailable = false;
            while (!pathAvailable) {
                final StringBuilder candidateFileName = new StringBuilder(filePathName);
                if (counter == null) {
                    counter = 1;
                } else {
                    // E.g. Output file name could become "test_proteins.fasta_1.tsv"
                    candidateFileName
                            .append('_')
                            .append(counter++);
                }
                candidateFileName
                        .append('.')
                        .append(outputFormat.getFileExtension());
                //Extend file name by tar (tar.gz) extension if HTML or SVG
                if (outputFormat.equals(FileOutputFormat.HTML) || outputFormat.equals(FileOutputFormat.SVG)) {
                    //outputPath = Paths.get(TarArchiveBuilder.buildTarArchiveName(candidateFileName.toString(), archiveSVGOutput, compressHtmlAndSVGOutput, outputFormat));
                } else {
                    outputPath = Paths.get(candidateFileName.toString());
                }
                pathAvailable = !Files.exists(outputPath);
                if (pathAvailable) {
                    try {
                        // Start creating the empty output file now, while the path is still available
                        if (outputFormat.equals(FileOutputFormat.SVG) && !archiveSVGOutput) {
                            outputPath = Files.createDirectories(outputPath);
                        } else {
                            outputPath = Files.createFile(outputPath);
                        }
                    } catch (IOException e) {
                        pathAvailable = false; // Nope, that path has probably just been taken (e.g. by another copy of InterProScan writing to the same output directory)
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Path " + candidateFileName.toString() + " was available for writing to, but I/O exception thrown");
                        }
                        ioCounter++;
                        if (ioCounter > 2000) {
                            // Stop possible infinite loop!
                            throw new IllegalStateException("Path " + candidateFileName.toString() + " was available, but I/O exception thrown on file creation");
                        }
                    }
                }
            }

        }
        return outputPath;
    }

    public  void getPathwayMap() {
        if (pathwayMap != null){
            return;
        }

        try {
            File file = new File(this.getEntryKVPath() + "/pathways.json");
            FileInputStream is = new FileInputStream(file);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            Map<String, List<String>> jsonMap;
            jsonMap = mapper.readValue(is, new TypeReference<>() {});
            pathwayMap = new ConcurrentHashMap<> (jsonMap);

            for (String key : jsonMap.keySet()) {
                List<String> pathwayLine = jsonMap.get(key);
                Utilities.verboseLog(30," pathwayLine: " + key +  " -" + pathwayLine);
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public  void getEntry2PathwayMap() {
        if (entry2PathwayMap != null){
            return;
        }
        try {
            File file = new File(this.getEntryKVPath() + "/pathways.ipr.json");
            FileInputStream is = new FileInputStream(file);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            Map<String, List<String>> jsonMap;
            jsonMap = mapper.readValue(is, new TypeReference<>() {});
            entry2PathwayMap = new ConcurrentHashMap<> (jsonMap);

            for (String key : jsonMap.keySet()) {
                List<String> pathwayLine = jsonMap.get(key);
                Utilities.verboseLog(30," entry2pathway: " + key + " = " + pathwayLine);
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void loadGoTerms() {
        if (gotermsMap != null){
            return;
        }
        try {
            File file = new File(this.getEntryKVPath() + "/goterms.json");
            FileInputStream is = new FileInputStream(file);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            Map<String, Object> jsonMap = mapper.readValue(is, new TypeReference<>() {});
            goVersion = (String) jsonMap.get("version");
            Map<String, List<String>> terms = (Map<String, List<String>>) jsonMap.get("terms");
            gotermsMap = new ConcurrentHashMap<>(terms);

            for (String key : gotermsMap.keySet()) {
                List<String> gotermLine = gotermsMap.get(key);
                Utilities.verboseLog(30," gotermLine: " + key +  " -" + gotermLine);
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadInterPro2Go(){
        if (entry2GoTermsMap != null){
            return;
        }

        try {
            File file = new File(this.getEntryKVPath() + "/goterms.ipr.json");
            FileInputStream is = new FileInputStream(file);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            Map<String, List<String>> jsonMap;
            jsonMap = mapper.readValue(is, new TypeReference<>() {});
            entry2GoTermsMap = new ConcurrentHashMap<> (jsonMap);

            for (String key : entry2GoTermsMap.keySet()) {
                List<String> gotermLine = entry2GoTermsMap.get(key);
                Utilities.verboseLog(30," entry2GoTermsMap: " + key + " = " + gotermLine);
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getEntriesMaps() {
        if (typesMap != null){
            return;
        }

        try {
            File file = new File(this.getEntryKVPath() + "/entries.json");
            FileInputStream is = new FileInputStream(file);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            Map<String, Object> jsonMap = mapper.readValue(is, new TypeReference<>() {});
            Map<String, Map<String, Object>> entries = (Map<String, Map<String, Object>>) jsonMap.get("entries");
            typesMap = new ConcurrentHashMap<>();
            domainsMap = new ConcurrentHashMap<>();
            familiesMap = new ConcurrentHashMap<>();

            for (Map.Entry<String, Map<String, Object>> entry : entries.entrySet()) {
                String accession = entry.getKey();
                Map<String, Object> value = entry.getValue();
                String type = (String) value.get("type");

                if (type != null) {
                    typesMap.put(accession, type.toUpperCase());
                }

                Map<String, Object> representative = (Map<String, Object>) value.get("representative");
                String rep_type = (String) representative.get("type");
                Integer index = (Integer) representative.get("index");

                if (rep_type != null && rep_type.equals("domain")) {
                    domainsMap.put(accession, index);
                }
                if (rep_type != null && rep_type.equals("family")) {
                    familiesMap.put(accession, index);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Entry updateEntryXrefs(Entry entry) {
        String entryAc = entry.getAccession();
        Set<GoXref> goXrefs = (Set<GoXref>) getGoXrefsByEntryAc(entryAc); //entry2GoXrefsMap.get(entryAc);
        Set<PathwayXref> pathwayXrefs =  (Set<PathwayXref>) getPathwayXrefsByEntryAc(entryAc); //(Set<PathwayXref>) entry2PathwayXrefsMap.get(entryAc);

        return new Entry.Builder(entryAc)
                .goCrossReferences(goXrefs)
                .pathwayCrossReferences(pathwayXrefs)
                .updateXrefs(entry);
    }

    public Collection<GoXref> getGoXrefsByEntryAc(String entryAc) {
        Set<GoXref> result = new HashSet<>();
        try {
            if (entry2GoTermsMap == null || !entry2GoTermsMap.containsKey(entryAc)) {
                Utilities.verboseLog(30, "pathway list for  " + entryAc + ": 0" );
                return result;
            }
            List<String> goIDs = entry2GoTermsMap.get(entryAc);
            Utilities.verboseLog(30, "Go-terms list for  " + entryAc + ": " + goIDs.size());
            for (String goId: goIDs) {
                List<String> goLine = gotermsMap.get(goId);
                if (goLine != null) {
                    String goName = goLine.get(0);
                    String goCategoryCode = goLine.get(1);
                    GoCategory category = GoCategory.parseNameCode(goCategoryCode);
                    result.add(new GoXref(goId, goName, category));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;

    }


    //FIXME calling this in multiple threads might cause a problem
    /*
    public Map<String, Collection<GoXref>> getEntry2GoXrefsMap() {
        if (entry2GoXrefsMap == null) {
            if (checkIfDAOAreUsable()) {
                LOGGER.info("Loading entry to go xrefs...");
                entry2GoXrefsMap = entry2GoDAO.getAllGoXrefs();
                if (entry2GoXrefsMap == null) {
                    throw new RuntimeException("Could not load any entry to go mappings from external database!");
                }
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(entry2GoXrefsMap.size() + " mappings loaded.");
                }
                Utilities.printMemoryUsage("PrepareForOutPut");
            }
        }
        return entry2GoXrefsMap;
    }


     */

    /*
    void test() {
        result = this.jdbcTemplate
                .query("SELECT i2g.entry_ac, g.go_id, g.name, g.category from INTERPRO.INTERPRO2GO i2g JOIN go.terms@GOAPRO g ON i2g.go_id = g.go_id WHERE i2g.entry_ac = :entry_ac",
                        namedParameters,
                        new RowMapper<GoXref>() {
                            public GoXref mapRow(ResultSet rs, int rowNum) throws SQLException {
                                String identifier = rs.getString("go_id");
                                String name = rs.getString("name");
                                String nameCode = rs.getString("category");
                                GoCategory category = GoCategory.parseNameCode(nameCode);
                                return new GoXref(identifier, name, category);
                            }
                        });
    }


     */
    public Collection<PathwayXref> getPathwayXrefsByEntryAc(String entryAc) {
        Set<PathwayXref> result = new HashSet<>();
        try {
            if (entry2PathwayMap == null || !entry2PathwayMap.containsKey(entryAc)) {
                Utilities.verboseLog(30, "pathway list for  " + entryAc + ": 0" );
                return result;
            }
            List<String> pathwayIDs = entry2PathwayMap.get(entryAc);
            Utilities.verboseLog(30, "pathway list for  " + entryAc + ": " + pathwayIDs.size());
            for (String pathwayId: pathwayIDs) {
                List<String> pathwayLine = pathwayMap.get(pathwayId);
                String dbcode = pathwayLine.get(0);
                String dbName = decodeDbCode(dbcode);
                String desc = pathwayLine.get(1);

                PathwayXref pxref = new PathwayXref(pathwayId, desc, dbName);
                result.add(pxref);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    /*
    public Map<String, Collection<PathwayXref>> getEntry2PathwayXrefsMap() {
        if (entry2PathwayXrefsMap == null) {
            if (checkIfDAOAreUsable()) {
                LOGGER.info("Loading entry to pathway xrefs...");
                entry2PathwayXrefsMap = entry2PathwayDAO.getAllPathwayXrefs();
                if (entry2PathwayXrefsMap == null) {
                    throw new RuntimeException("Could not load any entry to pathway mappings from external database!");
                }
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(entry2PathwayXrefsMap.size() + " mappings loaded.");
                }
                Utilities.printMemoryUsage("PrepareForOutPut");
            }
        }
        return entry2PathwayXrefsMap;
    }

     */

    private String decodeDbCode(String dbCode) {
        if (dbCode != null && dbCode.length() > 0) {
            return PathwayXref.PathwayDatabase.parseDatabaseCode(dbCode.charAt(0)).toString();
        }
        return null;
    }


    /**
     * get the proteins in range - expensive in terms of memeory usage, so need benachmarking
     *
     * @param bottomProteinId
     * @param topProteinId
     * @return
     */
    private Set<Protein> getProteinInRange(Long bottomProteinId, Long topProteinId) {
        Set<Protein> proteinsInRange = new HashSet<>();
        for (Long proteinIndex = bottomProteinId; proteinIndex <= topProteinId; proteinIndex++) {
            String proteinKey = Long.toString(proteinIndex);
            Protein protein = proteinDAO.getProteinAndCrossReferencesByProteinId(proteinIndex);
            //Protein protein = proteinDAO.getProtein(proteinKey);
            if (protein == null) {
                continue;
            }
            proteinsInRange.add(protein);
        }

        return proteinsInRange;
    }

    private void deleteTmpMarshallingFile(Path outputPath) {
        final String outputFilePathName = outputPath.toAbsolutePath().toString();
        Utilities.verboseLog(120, "Deleting (or attempting to) temp xml file:  " + outputFilePathName);

        File file = new File(outputFilePathName);
        if (file.exists()) {
            return;
        }
        if (file.exists()) {
            LOGGER.warn("PrepareForOutput File is located at " + outputFilePathName);
            if (!file.delete()) {
                LOGGER.error("Unable to delete the file located at " + outputFilePathName);
                throw new IllegalStateException("Unable to delete the file located at " + outputFilePathName);
            }
        } else {
            LOGGER.warn("File not found, file located at " + outputFilePathName);
        }
    }

    private String getTryCountStats(ArrayList<Pair<Integer, Integer>> observedTryCounts) {
        String tryCountStats = "";
        int tryCount = observedTryCounts.size();
        int maxTryCount = 0;
        int maxtotalWaitTime = 0;
        for (Pair<Integer, Integer> tryCountEntry : observedTryCounts) {
            if (maxTryCount < tryCountEntry.getLeft()) {
                maxTryCount = tryCountEntry.getLeft();
            }
            if (maxtotalWaitTime < tryCountEntry.getRight()) {
                maxtotalWaitTime = tryCountEntry.getRight();
            }
        }

        return " tryCounts:" + tryCount + " maxTryCount:" + maxTryCount + " maxtotalWaitTime: " + maxtotalWaitTime;
    }

    private void selectRepresentative(ArrayList<Domain> domains) {
        domains.sort(new Comparator<Domain>() {
            @Override
            public int compare(Domain d1, Domain d2) {
                int delta = d1.getLocation().getStart() - d2.getLocation().getStart();
                return delta != 0 ? delta : d1.getLocation().getEnd() - d2.getLocation().getEnd();
            }
        });

        ArrayList<ArrayList<Domain>> groups = groupDomains(domains);

        for (ArrayList<Domain> allDomainsInGroup : groups) {
            allDomainsInGroup.sort(new Comparator<Domain>() {
                @Override
                public int compare(Domain d1, Domain d2) {
                    int delta = d2.getResidues().size() - d1.getResidues().size();
                    if (delta != 0) {
                        return delta;
                    }
                    return d1.getDatabaseRank() - d2.getDatabaseRank();
                }
            });

            List<Domain> bestDomainsInGroup = allDomainsInGroup.subList(0, Math.min(MAX_NUM_DOMAINS_BY_GROUP, allDomainsInGroup.size()));

            Map<Integer, Set<Integer>> graph = new HashMap<>();
            for (int i = 0; i < bestDomainsInGroup.size(); i++) {
                Set<Integer> edges = new HashSet<>();

                for (int j = 0; j < bestDomainsInGroup.size(); j++) {
                    if (i != j) {
                        edges.add(j);
                    }
                }

                graph.put(i, edges);
            }

            for (int i = 0; i < bestDomainsInGroup.size(); i++) {
                Domain domainA = bestDomainsInGroup.get(i);

                for (int j = i + 1; j < bestDomainsInGroup.size(); j++) {
                    Domain domainB = bestDomainsInGroup.get(j);

                    if (domainA.overlaps(domainB, DOMAIN_OVERLAP_THRESHOLD)) {
                        graph.get(i).remove(j);
                        graph.get(j).remove(i);
                    }
                }
            }

            List<Set<Integer>> subgroups = new DomainResolver(graph).resolve();
            int maxCoverage = 0;
            int maxPfams = 0;
            List<Domain> bestSubgroup = null;
            for (Set<Integer> subgroup: subgroups) {
                Set<Integer> coverage = new HashSet<>();
                int numPfams = 0;
                List<Domain> candidate = new ArrayList<>();

                for (int i: subgroup) {
                    Domain domain = bestDomainsInGroup.get(i);
                    coverage.addAll(domain.getResidues());
                    if (domain.getDatabaseRank() == 0) {
                        numPfams++;
                    }

                    candidate.add(domain);
                }

                int sizeCoverage = coverage.size();
                if (sizeCoverage > maxCoverage || (sizeCoverage == maxCoverage && numPfams > maxPfams)) {
                    maxCoverage = sizeCoverage;
                    maxPfams = numPfams;
                    bestSubgroup = candidate;
                }
            }

            if (bestSubgroup != null) {
                for (Domain domain: bestSubgroup) {
                    domain.getLocation().setRepresentative(true);
                }
            }
        }
    }

    private ArrayList<ArrayList<Domain>> groupDomains(ArrayList<Domain> domains) {
        ArrayList<ArrayList<Domain>> groups = new ArrayList<>();
        ArrayList<Domain> group = new ArrayList<>();
        Domain domain = domains.get(0);
        group.add(domain);
        int stop = domain.getLocation().getEnd();

        for (int i = 1; i < domains.size(); i++) {
            domain = domains.get(i);
            int start = domain.getLocation().getStart();

            if (start <= stop) {
                group.add(domain);
                stop = Math.max(stop, domain.getLocation().getEnd());
            } else {
                groups.add(group);
                group = new ArrayList<>();
                group.add(domain);
                stop = domain.getLocation().getEnd();
            }
        }

        groups.add(group);
        return groups;
    }
}
