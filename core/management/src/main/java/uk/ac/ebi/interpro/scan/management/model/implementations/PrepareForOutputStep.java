package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.match.writer.ProteinMatchesJSONResultWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.ProteinMatchesWithNucleotidesXMLJAXBFragmentsResultWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.ProteinMatchesXMLJAXBFragmentsResultWriter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.TarArchiveBuilder;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.MatchDAO;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PrepareForOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PrepareForOutputStep.class.getName());

    //DAOs
    private ProteinDAO proteinDAO;
    private MatchDAO matchDAO;

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

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting step with Id " + this.getId());
        }

        Utilities.verboseLog(110, "temporaryFileDirectory: " + temporaryFileDirectory);

        int proteinCount = 0;
        int matchCount = 0;

        Set<String> signatureLibraryNames = new HashSet<>();

        for (SignatureLibrary sig : SignatureLibrary.values()) {
            signatureLibraryNames.add(sig.getName());
        }

        int proteinRawCount = 0;
        Protein exampleProtein = null;

        printMemoryUsage("at start of preparing  [" + proteinRange + " proteins");


        try {
            Utilities.verboseLog(1100, "Pre-marshall the proteins ...");
            simulateMarshalling(stepInstance, "p", temporaryFileDirectory);
            Utilities.verboseLog(1100, "Pre-marshall the nucleotide sequences ...");
            final Map<String, String> parameters = stepInstance.getParameters();
            final String sequenceType = parameters.get(SEQUENCE_TYPE);
            if (sequenceType.equalsIgnoreCase("n")) {
                Utilities.verboseLog(1100, "Dealing with nucleotide sequences ... , so pre-marshalling required");
                processNucleotideSequences(stepInstance, temporaryFileDirectory);
            } else {
                Utilities.verboseLog(1100, "Dealing with proteins  ... , so pre-marshalling already done");
            }
            Utilities.verboseLog(1100, "Completed prepraring the protein range  ..." + proteinRange);
        } catch (IOException e) {
            LOGGER.warn("Problem with marshalling in the  Prepare proteins for output step");
            e.printStackTrace();
        }

        /*
        for (Long proteinIndex= bottomProteinId;proteinIndex <= topProteinId; proteinIndex ++){
            proteinRawCount ++;
            String proteinKey = Long.toString(proteinIndex);
            Protein protein = proteinDAO.getProtein(proteinKey);


            if(protein != null){
                proteinCount ++;
            }

            for(String signatureLibraryName: signatureLibraryNames){
                final String dbKey = proteinKey + signatureLibraryName;
                Set<Match> matches = matchDAO.getMatchSet(dbKey);
                if (matches != null){
                    //Utilities.verboseLog(1100, "Get matches for protein  id: " + protein.getId() +  " dbKey (matchKey): " + dbKey);
                    for(Match match: matches){
                        match.getSignature().getCrossReferences();
                        match.getSignature().getEntry();
                        protein.addMatch(match);
                        matchCount ++;
                    }
                }
            }
            if (exampleProtein == null && protein.getMatches().size() > 1) {
                exampleProtein = protein;
            }
            if (proteinIndex % 4000 == 0){
                Utilities.verboseLog(proteinRange + " - Of possible  " + proteinRawCount + " proteins, processed  " + proteinCount + " with  total matches : " + matchCount);
            }

            Protein proteinWithXref = proteinDAO.getProteinAndCrossReferencesByProteinId(proteinIndex);
            //Protein protein = proteinWithXref;

            //Utilities.verboseLog(1100, "proteinWithXref: \n" +  proteinWithXref.toString());
            for (OpenReadingFrame orf : proteinWithXref.getOpenReadingFrames()) {
                //Utilities.verboseLog(1100, "OpenReadingFrame: \n" +  orf.toString());
                NucleotideSequence seq = orf.getNucleotideSequence();
                //Utilities.verboseLog(1100, "NucleotideSequence: \n" +  seq.toString());
                if (seq != null) {
                    nucleotideSequences.add(seq);
                    nucleotideSequenceIds.add(seq.getId()); //store the Id
//                    Hibernate.initialize(seq);
//                    nucleotideSequenceDAO.persist(key, seq);
                }
            }

            //store protein back in kv store
            proteinDAO.persist(proteinKey, protein);



            //keyToProteinMap.put(key, protein);
        }


        if(nucleotideSequences.size() > 0) {
            //Utilities.verboseLog(1100, "nucleotideSequences : \n" + nucleotideSequences.iterator().next());
            try {
                //outputNTToXML(stepInstance, "n", nucleotideSequences);
                outputToXML(stepInstance, "n", nucleotideSequenceIds);
                //outputToJSON(stepInstance, "n", nucleotideSequences);
            } catch (IOException e) {
                LOGGER.error("Error writing to xml");
                e.printStackTrace();
            }
        }



        Utilities.verboseLog(1100, "nucleotideSequences size: " +  nucleotideSequences.size());


       // Map<String, Set<Match>> matchesForEachProtein = matchDAO.getMatchesForEachProtein();

        //Utilities.verboseLog(1100, "matchesForEachProtein: " + matchesForEachProtein.size());

        //for (String matchKey : matchesForEachProtein.keySet()){
        //    Set<Match> matches = matchesForEachProtein.get(matchKey);
            //Utilities.verboseLog(1100, "matchKey: " + matchKey + " match count: " + matches.size());
        //}


        //Set<Match> allMatches = matchDAO.getMatches();
        //matchCount = allMatches.size();

        //        Match testMatch = (Match) matchDAO.get("test1");


        Utilities.verboseLog(1100, "Total proteins in range " + proteinRange + " with matches :  " + proteinCount + " Total matches : " + matchCount);
        //Protein exampleProtein = proteinsinRange.get(1);
        if(exampleProtein != null) {
            //dont prin the example protein for now
//            Utilities.verboseLog(1100, "exampleProtein: " + exampleProtein.toString());
            int matchsize = 0;
            if (! exampleProtein.getMatches().isEmpty()){
                matchsize =  exampleProtein.getMatches().size();
            }
            Utilities.verboseLog(1100, "exampleProtein: " + exampleProtein.getId() + " matches: " + matchsize);
        }
    }

    List<String>   getProteinsBetweenIds(Long bottom, Long top){
        Long timestart = System.currentTimeMillis();
        List<String> proteinsinRange = new ArrayList<>();
        for (Long index= bottom;index <= top; index ++){
            String proteinKey = Long.toString(index);
            Protein protein = proteinDAO.getProtein(proteinKey);
            if(protein != null){
                proteinsinRange.add(proteinKey);
            }
        }
        Long timeTaken = System.currentTimeMillis() - timestart;
        int timeTakenSeconds = timeTaken.intValue() / 1000;
        Utilities.verboseLog(1100, "timeTakenSeconds to get range: [" + bottom + "-" + top + "] = " + timeTakenSeconds + " seconds ");

        return proteinsinRange;

        */
    }


    private void processNucleotideSequences(StepInstance stepInstance, String temporaryFileDirectory) {
        //
        //should we deal with nucleotides here
        //Utilities.verboseLog(1100, "proteinWithXref: \n" +  proteinWithXref.toString());
        final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();

        final Set<Long> nucleotideSequenceIds = new HashSet<>();

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        int proteinCount = 0;

        Set<String> signatureLibraryNames = new HashSet<>();

        for (SignatureLibrary sig : SignatureLibrary.values()) {
            signatureLibraryNames.add(sig.getName());
        }

        for (Long proteinIndex = bottomProteinId; proteinIndex <= topProteinId; proteinIndex++) {
            String proteinKey = Long.toString(proteinIndex);
            Protein protein = proteinDAO.getProteinAndCrossReferencesByProteinId(proteinIndex);
            //Protein protein = proteinDAO.getProtein(proteinKey);
            if (protein != null) {
                proteinCount++;
            }
            for (OpenReadingFrame orf : protein.getOpenReadingFrames()) {
                //Utilities.verboseLog(1100, "OpenReadingFrame: \n" +  orf.toString());
                NucleotideSequence seq = orf.getNucleotideSequence();
                //Utilities.verboseLog(1100, "NucleotideSequence: \n" +  seq.toString());

                if (seq != null) {
                    nucleotideSequences.add(seq);
                    nucleotideSequenceIds.add(seq.getId()); //store the Id
                    //                    Hibernate.initialize(seq);
                    //                    nucleotideSequenceDAO.persist(key, seq);
                }
            }
        }

        if (nucleotideSequences.size() > 0) {
            try {
                //outputNTToXML(stepInstance, "n", nucleotideSequences);
                outputToXML(stepInstance, "n", nucleotideSequenceIds, temporaryFileDirectory);
                //outputToJSON(stepInstance, "n", nucleotideSequences);
            } catch (IOException e) {
                LOGGER.error("Error writing to xml");
                e.printStackTrace();
            }
        }
        Utilities.verboseLog(1100, "nucleotideSequences size: " + nucleotideSequences.size());

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

        //final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();


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

        for (Long proteinIndex = bottomProteinId; proteinIndex <= topProteinId; proteinIndex++) {
            String proteinKey = Long.toString(proteinIndex);
            //Protein protein  = proteinDAO.getProteinAndCrossReferencesByProteinId(proteinIndex);
            Protein protein = proteinDAO.getProtein(proteinKey);
            if (protein != null) {
                proteinCount++;
            }


            if (proteinIndex == middleProtein) {
                printMemoryUsage("at midpoint preparing  " + proteinRange + " proteins");
            }

            for (String signatureLibraryName : signatureLibraryNames) {
                final String dbKey = proteinKey + signatureLibraryName;

                Set<Match> matches = null;
                //try this say three times
                int tryCount = 0;
                while (tryCount <= 3) {
                    try {
                        matches = matchDAO.getMatchSet(dbKey);
                    } catch (Exception exception) {
                        //dont recover but sleep for a few seconds and try again

                        Utilities.verboseLog(1100, "Exception type: " + exception.getClass());

                        exception.printStackTrace();
                        if (tryCount >= 3) {
                            throw new IllegalStateException("Failed to get matches from the DB for key " + dbKey);
                        }
                        //how long to wait for files to be available ??
                        int waitTime = (proteinsConsidered.intValue() / 8000) * 60 * 1000;
                        if (getNfsDelayMilliseconds() < waitTime) {
                            if (waitTime > 120 * 1000) {
                                waitTime = 120 * 1000;
                            }
                            Utilities.sleep(waitTime);
                        } else {
                            delayForNfs();
                        }

                        Utilities.verboseLog(110, "  Prepare for output - Slept for at least " + waitTime + " millis");

                    }
                    tryCount++;
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

            //TDO Temp check what breaks if you dont do pre-marshalling
            //String xmlProtein = writer.marshal(protein);

            protein.getOpenReadingFrames().size();

            for (Match i5Match : protein.getMatches()) {
                //try update with cross refs etc
                updateMatch(i5Match);
            }

            proteinDAO.persist(proteinKey, protein);
            //help garbage collection??
            if (proteinBreakPoints.contains(proteinIndex)){
                System.gc();
                printMemoryUsage("after GC scheduled at breakIndex = " + proteinIndex);
            }
        }
        //}catch (JAXBException e){
        //    e.printStackTrace();
        //}catch (XMLStreamException e) {
        //    e.printStackTrace();
        //}

        //remove the temp xmls file
        deleteTmpMarshallingFile(outputPath);
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

        try (ProteinMatchesXMLJAXBFragmentsResultWriter writer = new ProteinMatchesXMLJAXBFragmentsResultWriter(outputPath, NucleotideSequence.class, isSlimOutput)) {
            //writer.header(interProScanVersion);
            if (!nucleotideSequenceIds.isEmpty()) {
                Utilities.verboseLog(110, " nucleotideSequenceIds  : " + nucleotideSequenceIds.size());

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Load " + topProteinId + " proteins from the db.");
                }

                int count = 0;
                writer.header(interProScanVersion, "nucleotide-sequence-matches");
                //final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();
                for (Long nucleotideSequenceId : nucleotideSequenceIds) {
                    count++;
                    Utilities.verboseLog(120, " nucleotideSequenceId  : " + nucleotideSequenceId);

                    NucleotideSequence nucleotideSequenceInH2 = nucleotideSequenceDAO.getNucleotideSequence(nucleotideSequenceId);
                    String nucleotideSequenceKey = nucleotideSequenceInH2.getMd5();
                    Set<NucleotideSequenceXref> nucleotideSequenceCrossReferences = nucleotideSequenceInH2.getCrossReferences();
                    if (Utilities.verboseLogLevel >= 20) {
                        for (NucleotideSequenceXref nucleotideXref : nucleotideSequenceCrossReferences) {
                            String nucleotideXrefIdentifier = nucleotideXref.getIdentifier();
                            Utilities.verboseLog(count + " nucleotideXrefIdentifier: " + nucleotideXrefIdentifier);
                        }
                    }
                    //nucleotideSequenceDAO.persist(nucleotideSequenceKey, nucleotideSequenceInH2);
                    //NucleotideSequence  nucleotideSequence = nucleotideSequenceDAO.get(nucleotideSequenceKey);

                    Utilities.verboseLog(110, "\n#" + count + " nucleotideSequenceInH2: " + nucleotideSequenceId + " : "); // + nucleotideSequenceInH2.toString());

                    for (OpenReadingFrame orf : nucleotideSequenceInH2.getOpenReadingFrames()) {
                        Protein protein = orf.getProtein();
                        // String proteinKey = Long.toString(protein.getId());
                        //Protein proteinMarshalled = proteinDAO.getProtein(proteinKey);
                        //protein = proteinMarshalled;
                        //orf.setProtein(proteinMarshalled);
                    }
                    String xmlNucleotideSequence = writer.marshal(nucleotideSequenceInH2);
                    if (Utilities.verboseLogLevel > 0) {
                        Utilities.verboseLog(1100, "\n#" + count + " xmlNucleotideSequence: " + xmlNucleotideSequence);
                    }
                    //String key = nucleotideSequence.getMd5();
                    nucleotideSequenceDAO.persist(nucleotideSequenceKey, nucleotideSequenceInH2);
                    //Utilities.verboseLog(1100, "Prepae OutPut xmlNucleotideSequence : " + nucleotideSequenceId + " -- "); // +  xmlNucleotideSequence);
                    //break;
                }

                Utilities.verboseLog(1100, "PrepareforOutPut nucleotideSequenceIds size: " + nucleotideSequenceIds.size());

            }

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        //delete the tmp xml file
        deleteTmpMarshallingFile(outputPath);
    }


    private void outputNTToXML(StepInstance stepInstance, String sequenceType, Set<NucleotideSequence> nucleotideSequences, String temporaryFileDirectory) throws IOException {
        if (!sequenceType.equalsIgnoreCase("n")) {
            return;
        }
        final boolean isSlimOutput = false;
        final String interProScanVersion = "5-34";

        Path outputPath = getFinalPath(stepInstance, temporaryFileDirectory, FileOutputFormat.XML);
        Utilities.verboseLog(110, " Prepare For OutputStep - outputNTToXML: " + outputPath);

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        try (ProteinMatchesWithNucleotidesXMLJAXBFragmentsResultWriter writer = new ProteinMatchesWithNucleotidesXMLJAXBFragmentsResultWriter(outputPath, isSlimOutput)) {
            //writer.header(interProScanVersion);
            if (bottomProteinId != null && topProteinId != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Load " + topProteinId + " proteins from the db.");
                }
                Utilities.verboseLog(110, " WriteOutputStep -XML new " + " There are " + topProteinId + " proteins.");
                int count = 0;
                writer.header(interProScanVersion);
                //final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();
                for (NucleotideSequence nucleotideSequence : nucleotideSequences) {
                    //writer.write(nucleotideSequence, sequenceType, isSlimOutput);
                    String xmlNucleotideSequence = writer.marshal(nucleotideSequence);
                    //writer.write(",");

                    String key = nucleotideSequence.getMd5();
                    Hibernate.initialize(nucleotideSequence);
                    nucleotideSequenceDAO.getMaximumPrimaryKey();

                    nucleotideSequenceDAO.persist(key, nucleotideSequence);
                    if (Utilities.verboseLogLevel > 0) {
                        Utilities.verboseLog(1100, "Prepae OutPut xmlNucleotideSequence : " + xmlNucleotideSequence);
                    }
                }

                Utilities.verboseLog(1100, "WriteOutPut nucleotideSequences size: " + nucleotideSequences.size());


            }
            writer.close();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }


    private void outputToJSON(StepInstance stepInstance, String sequenceType, Set<NucleotideSequence> nucleotideSequences, String temporaryFileDirectory) throws IOException {
        Utilities.verboseLog(110, " WriteOutputStep - outputToJSON ");

        boolean isSlimOutput = false;

        Utilities.verboseLog(110, " WriteOutputStep - outputToJSON json-slim? " + isSlimOutput);
//        try (ProteinMatchesJSONResultWriter writer = new ProteinMatchesJSONResultWriter(outputPath, isSlimOutput)) {
//            //old way??
//            //writer.write(matchesHolder, proteinDAO, sequenceType, isSlimOutput);
//
//        }
        //Try writing to JSOn from this module

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        Path outputPath = getFinalPath(stepInstance, temporaryFileDirectory, FileOutputFormat.JSON);

        final Map<String, String> parameters = stepInstance.getParameters();
        //final boolean mapToPathway = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_PATHWAY));
        //final boolean mapToGO = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_GO));
        //final boolean mapToInterProEntries = mapToPathway || mapToGO || Boolean.TRUE.toString().equals(parameters.get(MAP_TO_INTERPRO_ENTRIES));
        //writer.setMapToInterProEntries(mapToInterProEntries);
        //writer.setMapToGO(mapToGO);
        // writer.setMapToPathway(mapToPathway);
        try (ProteinMatchesJSONResultWriter writer = new ProteinMatchesJSONResultWriter(outputPath, isSlimOutput)) {
            final String interProScanVersion = "5-34";
            writer.header(interProScanVersion);
            if (bottomProteinId != null && topProteinId != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Load " + topProteinId + " proteins from the db.");
                }
                Utilities.verboseLog(110, " WriteOutputStep -JSON new " + " There are " + topProteinId + " proteins.");
                int count = 0;
                for (NucleotideSequence nucleotideSequence : nucleotideSequences) {

                    writer.write(nucleotideSequence);
                    count++;
                    if (count < nucleotideSequences.size()) {
                        writer.write(","); // More proteins/nucleotide sequences to follow
                    }

                }
            }
            writer.footer();
        }

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

    private void printMemoryUsage(String stepName){
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        System.out.println("##### Heap utilization statistics [MB]  at " + stepName + " #####");

        //Print used memory
        System.out.println("Used Memory:"
                + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        //Print free memory
        System.out.println("Free Memory:"
                + runtime.freeMemory() / mb);

        //Print total available memory
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);

        //Print Maximum available memory
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
    }

}
