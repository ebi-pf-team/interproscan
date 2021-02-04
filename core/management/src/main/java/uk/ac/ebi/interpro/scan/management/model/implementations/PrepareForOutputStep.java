package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.match.writer.ProteinMatchesXMLJAXBFragmentsResultWriter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.MatchDAO;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrepareForOutputStep extends Step {

    private static final Logger LOGGER = LogManager.getLogger(PrepareForOutputStep.class.getName());

    //DAOs
    private ProteinDAO proteinDAO;
    private MatchDAO matchDAO;

    final ConcurrentHashMap<Long, Long> allNucleotideSequenceIds = new ConcurrentHashMap<>();
    final ConcurrentHashMap<Long, Boolean> processedNucleotideSequences = new ConcurrentHashMap<>();
    final Set<String> processesReadyForXMLMarshalling = new HashSet<>();

    private NucleotideSequenceDAO nucleotideSequenceDAO;

    public static final String SEQUENCE_TYPE = "SEQUENCE_TYPE";

    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    public void setMatchDAO(MatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    public void setNucleotideSequenceDAO(NucleotideSequenceDAO nucleotideSequenceDAO) {
        this.nucleotideSequenceDAO = nucleotideSequenceDAO;
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


        try {
            Utilities.verboseLog(1100, "Pre-marshall the proteins ...");
            simulateMarshalling(stepInstance, "p", temporaryFileDirectory);
            Utilities.verboseLog(1100, "Pre-marshall the nucleotide sequences ...");
            final Map<String, String> parameters = stepInstance.getParameters();
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
            LOGGER.warn("Problem with marshalling in the  Prepare proteins for output step");
            e.printStackTrace();
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
                    int waitTime = (proteinsConsidered.intValue() / 8000) * 60 * 1000;
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


    private void simulateMarshalling(StepInstance stepInstance, String sequenceType, String temporaryFileDirectory) throws IOException {
        if (!sequenceType.equalsIgnoreCase("p")) {
            //maybe we should simulate for all types
            //return;
        }
        final boolean isSlimOutput = false;
        //final String interProScanVersion = "5-34";

        Path outputPath = getFinalPath(stepInstance, temporaryFileDirectory, FileOutputFormat.XML);

        Utilities.verboseLog(110, " Prepare For OutputStep - prepare to output proteins for XML: " + outputPath);

        //LOGGER.warn(" Prepare For OutputStep - prepare to output proteins for XML: " + outputPath);

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
                    protein = proteinDAO.getProtein(proteinKey);
                    break; //otherwise we have an infinite loop
                } catch (Exception exception) {
                    //dont recover but sleep for a few seconds and try again
                    Utilities.verboseLog(1100, "Exception type: " + exception.getClass());
                    int waitTime = (proteinsConsidered.intValue() / 8000) * 60 * 1000;
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
                        int waitTime = (proteinsConsidered.intValue() / 8000) * 60 * 1000;
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
                    //Utilities.verboseLog(1100, "Get matches for protein  id: " + protein.getId() +  " dbKey (matchKey): " + dbKey);
                    for (Match match : matches) {
                        String accession = match.getSignature().getAccession();
                        Utilities.verboseLog(120, "dbKey :" + dbKey + " - " + accession); //+ " - match: " + match.getLocations()) ;

                        match.getSignature().getCrossReferences();
                        //match.getSignature().getEntry();
                        //try update with cross refs etc
                        //updateMatch(match);
                        protein.addMatch(match);
                        matchCount++;
                    }
                }
            }


            //TODO Temp check what breaks if you dont do pre-marshalling
            //String xmlProtein = writer.marshal(protein);

            protein.getOpenReadingFrames().size();

            for (Match i5Match : protein.getMatches()) {
                //try update with cross refs etc
                updateMatch(i5Match);
            }

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
        System.out.println(Utilities.getTimeNow() + " Info: " +
                proteinRange + " pcounts " + kvGetStats4Proteins
        );
        //}
        //if (observedTryCounts4Matches.size() > 0) {
        String kvGetStats4Matches = getTryCountStats(observedTryCounts4Matches);
        System.out.println(Utilities.getTimeNow() + " Info: " +
                proteinRange + " mcounts " + kvGetStats4Matches
        );
        //}
        deleteTmpMarshallingFile(outputPath);
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
            if (getKvStoreDelayMilliseconds() < waitTime) {
                if (waitTime > 120 * 1000) {
                    waitTime = 120 * 1000;
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

    public void updateMatch(Match match) {
        Entry matchEntry = match.getSignature().getEntry();
        if (matchEntry != null) {
            //check goterms
            //check pathways
            matchEntry.getGoXRefs();
            if (matchEntry.getGoXRefs() != null) {
                matchEntry.getGoXRefs().size();
            }
            matchEntry.getPathwayXRefs();
            if (matchEntry.getPathwayXRefs() != null) {
                matchEntry.getPathwayXRefs().size();
            }
        }
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

}
