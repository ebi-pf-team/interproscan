package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.XmlWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.*;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.GraphicalOutputResultWriter;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.ProteinMatchesHTMLResultWriter;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.ProteinMatchesSVGResultWriter;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.TarArchiveBuilder;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.MatchDAO;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinXrefDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Writes all matches for a slice of proteins to a file.
 * <p/>
 * Should be run once analysis is complete.
 *
 * @author David Binns
 * @author Maxim Scheremetjew
 * @author Phil Jones
 */

public class WriteOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(WriteOutputStep.class.getName());

    //DAOs
    private ProteinDAO proteinDAO;

    private ProteinXrefDAO proteinXrefDAO;

    private NucleotideSequenceDAO nucleotideSequenceDAO;

    private MatchDAO matchDAO;

    //Output writer
    private XmlWriter xmlWriter;
    private ProteinMatchesHTMLResultWriter htmlResultWriter;
    private ProteinMatchesSVGResultWriter svgResultWriter;

    //Misc
    private boolean deleteWorkingDirectoryOnCompletion = true;

    /* Boolean flag for the HTML and SVG output generation. If TRUE, the generated tar archives will be compress (gzipped) as well */
    private boolean compressHtmlAndSVGOutput;

    /* Not required. If TRUE (default), it will archive all SVG output files into a single archive.*/
    private boolean archiveSVGOutput = true;

    private boolean excludeSites;

    private EntryHierarchy entryHierarchy;

    private String interProScanVersion;

    public static final String OUTPUT_EXPLICIT_FILE_PATH_KEY = "EXPLICIT_OUTPUT_FILE_PATH";

    public static final String OUTPUT_FILE_PATH_KEY = "OUTPUT_PATH";
    public static final String OUTPUT_FILE_FORMATS = "OUTPUT_FORMATS";
    public static final String INCL_TSV_VERSION = "INCL_TSV_VERSION";
    public static final String MAP_TO_INTERPRO_ENTRIES = "MAP_TO_INTERPRO_ENTRIES";
    public static final String MAP_TO_GO = "MAP_TO_GO";
    public static final String MAP_TO_PATHWAY = "MAP_TO_PATHWAY";
    public static final String SEQUENCE_TYPE = "SEQUENCE_TYPE";

    public void setArchiveSVGOutput(boolean archiveSVGOutput) {
        this.archiveSVGOutput = archiveSVGOutput;
    }

    @Required
    public void setCompressHtmlAndSVGOutput(boolean compressHtmlAndSVGOutput) {
        this.compressHtmlAndSVGOutput = compressHtmlAndSVGOutput;
    }

    @Required
    public void setHtmlResultWriter(ProteinMatchesHTMLResultWriter htmlResultWriter) {
        this.htmlResultWriter = htmlResultWriter;
    }

    @Required
    public void setSvgResultWriter(ProteinMatchesSVGResultWriter svgResultWriter) {
        this.svgResultWriter = svgResultWriter;
    }

    @Required
    public void setXmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Required
    public void setDeleteWorkingDirectoryOnCompletion(String deleteWorkingDirectoryOnCompletion) {
        this.deleteWorkingDirectoryOnCompletion = "true".equalsIgnoreCase(deleteWorkingDirectoryOnCompletion);
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setXrefDao(ProteinXrefDAO proteinXrefDAO) {
        this.proteinXrefDAO = proteinXrefDAO;
    }

    @Required
    public void setNucleotideSequenceDAO(NucleotideSequenceDAO nucleotideSequenceDAO) {
        this.nucleotideSequenceDAO = nucleotideSequenceDAO;
    }

    public void setMatchDAO(MatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    @Required
    public void setExcludeSites(boolean excludeSites) {
        this.excludeSites = excludeSites;
    }

    @Required
    public void setInterProScanVersion(String interProScanVersion) {
        this.interProScanVersion = interProScanVersion;
    }

    @Required
    public void setEntryHierarchy(EntryHierarchy entryHierarchy) {
        this.entryHierarchy = entryHierarchy;
    }

    /**
     * Sets/persists new unique protein xref identifiers in cases where they are non unique (same ID, different sequences).
     */
    private void setUniqueXrefs() {
        if (proteinXrefDAO == null) {
            throw new IllegalStateException("Protein Xref database accession object is NULL. Unexpected state. Cannot go on.");
        }
        Collection<ProteinXref> updates = new HashSet<>();
        Collection<String> nonUniqueIdentifiers = proteinXrefDAO.getNonUniqueXrefs();
        if (nonUniqueIdentifiers != null && nonUniqueIdentifiers.size() > 0) {
            System.out.println("Found " + nonUniqueIdentifiers.size() + " non unique identifier(s). These identifiers do have different sequences, within the FASTA protein sequence input file.");
            System.out.println("InterProScan will make them unique by adding '_sequential number' in the order of their appearance (e.g. P11111 will be P11111_1 for the first protein sequence).");
            System.out.println("Please find below a list of detected identifiers:");
            for (String nonUniqueIdentifier : nonUniqueIdentifiers) {
                System.out.println(nonUniqueIdentifier);
            }
        }
        for (String identifier : nonUniqueIdentifiers) {
            List<ProteinXref> proteinXrefs = proteinXrefDAO.getXrefAndProteinByProteinXrefIdentifier(identifier);
            if (proteinXrefs.size() < 2) {
                LOGGER.warn("Unexpected databases query result. The size of the result set should be > 1.");
            }
            int counter = 0;
            for (ProteinXref xref : proteinXrefs) {
                counter++;
                xref.setIdentifier(xref.getIdentifier() + "_" + counter);
                updates.add(xref);
            }
        }
        proteinXrefDAO.updateAll(updates);
    }


    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting step with Id " + this.getId());
        }
        final Map<String, String> parameters = stepInstance.getParameters();
        final String outputFormatStr = parameters.get(OUTPUT_FILE_FORMATS);
        final Set<FileOutputFormat> outputFormats = FileOutputFormat.stringToFileOutputFormats(outputFormatStr);
        final boolean explicitPath = parameters.containsKey(OUTPUT_EXPLICIT_FILE_PATH_KEY);
        final String filePathName = (explicitPath)
                ? parameters.get(OUTPUT_EXPLICIT_FILE_PATH_KEY)
                : parameters.get(OUTPUT_FILE_PATH_KEY);


        int waitTimeFactor = 1;  //check what is the average time it takes to get raw results
        if (! Utilities.isRunningInSingleSeqMode()) {
            //use loge to get wait time
            waitTimeFactor = Utilities.getWaitTimeFactorLogE(20 * Utilities.getSequenceCount()).intValue();
        }
        Utilities.sleep(waitTimeFactor * 1000);                 //1000 milliseconds is one second.
        Utilities.verboseLog(10, " WriteOutputStep - get proteins, waitTime - " + waitTimeFactor + " seconds");

        final String sequenceType = parameters.get(SEQUENCE_TYPE);
        if (sequenceType.equalsIgnoreCase("p")) {
            LOGGER.debug("Setting unique protein cross references (Please note this function is only performed if the input sequences are proteins)...");
            setUniqueXrefs();
        }

        for (FileOutputFormat outputFormat : outputFormats) {
            Path outputPath = getPathName(explicitPath, filePathName, outputFormat);
            try {
                Utilities.verboseLog("Writing out " + outputPath.toString());
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Writing out " + outputFormat + " file");
                }
                switch (outputFormat) {
                    case TSV:
                        outputToTSV(outputPath, stepInstance, sequenceType);
                        break;
                    case TSV_PRO:
                        outputToTSVPRO(outputPath, stepInstance);
                        break;
                    case XML:
                        outputToXML(outputPath, stepInstance, sequenceType, false);
                        break;
                    case XML_SLIM:
                        outputToXML(outputPath, stepInstance, sequenceType, true);
                        break;
                    case JSON:
                        outputToJSON(outputPath, stepInstance, sequenceType, false);
                        break;
                    case JSON_SLIM:
                        outputToJSON(outputPath, stepInstance, sequenceType, true);
                        break;
                    case GFF3:
                        outputToGFF(outputPath, stepInstance, sequenceType);
                        break;
                    case GFF3_PARTIAL:
                        outputToGFFPartial(outputPath, stepInstance);
                        break;
                    case HTML:
                        //Replace the default temp dir with the user specified one
                        if (temporaryFileDirectory != null) {
                            if (htmlResultWriter == null){
                                throw new IllegalStateException("htmlResultWriter is null ");
                            }
                            htmlResultWriter.setTempDirectory(temporaryFileDirectory);
                        }
                        outputToHTML(outputPath, stepInstance);
                        break;
                    case SVG:
                        //Replace the default temp dir with the user specified one
                        if (temporaryFileDirectory != null) {
                            svgResultWriter.setTempDirectory(temporaryFileDirectory);
                        }
                        outputToSVG(outputPath, stepInstance);
                        break;
                    default:
                        LOGGER.warn("Unrecognised output format " + outputFormat + " - cannot write the output file.");
                }
            } catch (IOException ioe) {
                final String p = outputPath.toAbsolutePath().toString();
                throw new IllegalStateException("IOException thrown when attempting to writeComment output from InterProScan to path: " + p, ioe);
            }
        }

        //close the kvStores

        nucleotideSequenceDAO.getDbStore().close();

        proteinDAO.closeKVDBStores();

        matchDAO.getDbStore().close();

        cleanUpWorkingDir(temporaryFileDirectory);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Step with Id " + this.getId() + " finished.");
        }
        //set that this process has finished
        Utilities.verboseLog("WriteOutput Step with Id " + this.getId() + " finished.");
        Utilities.setWriteOutputStepCompleted(true);
        Utilities.verboseLog("writeOutputStepCompleted: " + Utilities.isWriteOutputStepCompleted());
    }

    private void cleanUpWorkingDir(final String temporaryFileDirectory) {

        LOGGER.debug("deleteWorkingDirectoryOnCompletion: " + deleteWorkingDirectoryOnCompletion);

        if (deleteWorkingDirectoryOnCompletion) {
            // Clean up empty working directory.
            final String workingDirectory = temporaryFileDirectory.substring(0, temporaryFileDirectory.lastIndexOf(File.separatorChar));
            File file = new File(workingDirectory);
            try {
                if (file.exists()) {
                    Utilities.verboseLog(10, "temporaryFileDirectory exists, so delete: ");
                    //Collection<File> filesToDelete = new HashSet<>(FileUtils.listFiles(file,null,true));
                    //Set <String> filenames = new HashSet<>();
                    //for (File fileToDelete : filesToDelete){
                    //    filenames.add(fileToDelete.getName());
                    //}
                    //System.out.println("To delete the following files: ");
                    //for (String filename : filenames){
                    //    System.out.println(filename);
                    //}
                    delayForNfs();
                    // FileUtils.deleteDirectory(file);
                    FileUtils.forceDelete(file);
                }
            } catch (IOException e) {
                LOGGER.warn("At write output completion, unable to delete temporary directory " + file.getAbsolutePath());
                Utilities.verboseLog(20,"WriteOutPut - ExceptionMessage: " + e.getMessage());
                //e.printStackTrace();
            }
        } else {
            LOGGER.debug("Files in temporaryFileDirectory not deleted since  delete.working.directory.on.completion =  " + deleteWorkingDirectoryOnCompletion);
        }
    }

    private Path getPathName(final boolean explicitPath,
                             final String filePathName,
                             final FileOutputFormat outputFormat) {
        // E.g. for "-b OUT" filePathName = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT"
        Path outputPath = null;

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
                    outputPath = Paths.get(TarArchiveBuilder.buildTarArchiveName(candidateFileName.toString(), archiveSVGOutput, compressHtmlAndSVGOutput, outputFormat));
                } else {
                    outputPath = Paths.get(candidateFileName.toString());
                }
                pathAvailable = !Files.exists(outputPath);
                if (pathAvailable) {
                    try {
                        // Start creating the empty output file now, while the path is still available
                        if (outputFormat.equals(FileOutputFormat.SVG) && !archiveSVGOutput) {
                            outputPath = Files.createDirectories(outputPath);
                        }
                        else {
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


    private void outputToXML(Path outputPath, StepInstance stepInstance, String sequenceType, boolean isSlimOutput) throws IOException {
        Utilities.verboseLog(10, " WriteOutputStep - outputToXML " );
        if (! sequenceType.equalsIgnoreCase("p")){
            outputNTToXML(outputPath, stepInstance, sequenceType, isSlimOutput);
            return;
        }
        IMatchesHolder matchesHolder = getMatchesHolder(stepInstance, sequenceType, isSlimOutput);

        //Utilities.verboseLog(10, " WriteOutputStep - outputToXML xml-slim? " + isSlimOutput);
        //xmlWriter.writeMatches(outputPath, matchesHolder);

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        try (ProteinMatchesXMLJAXBFragmentsResultWriter writer = new ProteinMatchesXMLJAXBFragmentsResultWriter(outputPath, Protein.class, isSlimOutput)) {
            //writer.header(interProScanVersion);
            if (bottomProteinId != null && topProteinId != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Load " + topProteinId + " proteins from the db.");
                }
                Utilities.verboseLog(10, " WriteOutputStep -XML new " + " There are " + topProteinId + " proteins.");
                int count = 0;
                writer.header(interProScanVersion, "protein-matches");
                final Set<NucleotideSequence> nucleotideSequences = new HashSet<>();
                for (Long proteinIndex = bottomProteinId; proteinIndex <= topProteinId; proteinIndex++) {
                    String proteinKey = Long.toString(proteinIndex);
                    Protein protein = proteinDAO.getProtein(proteinKey);
                    if (protein == null ) {
                        LOGGER.warn("protein with id  " + proteinIndex + " was null");
                        continue;
                        //something is wrong as we should get a null protein
                    }
                    if (isSlimOutput && protein.getMatches().isEmpty()) {
                        //dont display proteins that dont have matches
                        continue;
                    }
                    Set<Match> matches = protein.getMatches();

                    for (Match match : matches){
                        StringBuilder matchBuilder = new StringBuilder();
                        matchBuilder.append(protein.getId()).append(" ")
                                .append(protein.getMd5()).append(" ")
                                .append(match.getSignature().getSignatureLibraryRelease().getLibrary().getName()).append(" ");
                        Entry matchEntry = match.getSignature().getEntry();
                        if(matchEntry!= null){
                            //check goterms 
                              //check pathways
                            matchBuilder.append("-- entry: ").append(matchEntry.getAccession());
                            matchEntry.getGoXRefs();
                            if(matchEntry.getGoXRefs() != null) {
                                matchEntry.getGoXRefs().size();
                            }
                            matchEntry.getPathwayXRefs();
                            if(matchEntry.getPathwayXRefs() != null) {
                                matchEntry.getPathwayXRefs().size();
                            }
                        }else{
                            matchBuilder.append("-- entry i NULL");
                        }
                        //System.out.println("matchBuilder:  "  + matchBuilder );

                    }
                    writer.write(protein, sequenceType, isSlimOutput);
                    count++;
                    if (count < proteinIndex) {
                        writer.write(","); // More proteins/nucleotide sequences to follow
                    }
                    for (OpenReadingFrame orf : protein.getOpenReadingFrames()) {
                        Utilities.verboseLog(20, "OpenReadingFrame: " +  orf.getId() + " --  " + orf.getStart() + "-" + orf.getEnd());
                        NucleotideSequence seq = orf.getNucleotideSequence();
                        //Utilities.verboseLog("NucleotideSequence: \n" +  seq.toString());
                        if (seq != null) {
                            nucleotideSequences.add(seq);
                            writer.write(seq, sequenceType, isSlimOutput);
                        }
                    }

                    //print one protein then break
                    //break;
                }
                Utilities.verboseLog("WriteOutPut nucleotideSequences size: " +  nucleotideSequences.size());
            }
            writer.close();
        }catch (JAXBException e){
            e.printStackTrace();
        }catch (XMLStreamException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void outputNTToXML(Path outputPath, StepInstance stepInstance, String sequenceType, boolean isSlimOutput) throws IOException {
        if (! sequenceType.equalsIgnoreCase("n")){
            return;
        }
        Utilities.verboseLog(10, " WriteOutputStep - output NucleotideSequence  to XML " );
        IMatchesHolder matchesHolder = getMatchesHolder(stepInstance, sequenceType, isSlimOutput);

        //Utilities.verboseLog(10, " WriteOutputStep - outputToXML xml-slim? " + isSlimOutput);
        //xmlWriter.writeMatches(outputPath, matchesHolder);

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        try (ProteinMatchesXMLJAXBFragmentsResultWriter writer = new ProteinMatchesXMLJAXBFragmentsResultWriter(outputPath, NucleotideSequence.class, isSlimOutput)) {
            //writer.header(interProScanVersion);
            if (bottomProteinId != null && topProteinId != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Load " + topProteinId + " proteins from the db.");
                }
                Utilities.verboseLog(10, " WriteOutputStep nucleotideSequences -XML new " + " There are " + topProteinId + " proteins.");
                int count = 0;
                writer.header(interProScanVersion, "nucleotide-sequence-matches");

                final Set<NucleotideSequence> nucleotideSequences = nucleotideSequenceDAO.getNucleotideSequences();
                for(NucleotideSequence  nucleotideSequence : nucleotideSequences ){
                    count ++;
                    for (OpenReadingFrame orf: nucleotideSequence.getOpenReadingFrames()) {
                        Protein protein = orf.getProtein();
                        String proteinKey = Long.toString(protein.getId());
                        Protein proteinMarshalled = proteinDAO.getProtein(proteinKey);
                        //protein = proteinMarshalled;
                        orf.setProtein(proteinMarshalled);
                    }
                    //Utilities.verboseLog("\n#" + count + " nucleotideSequence: " + nucleotideSequence.toString());
                    writer.write(nucleotideSequence, sequenceType, isSlimOutput);
                }

                Utilities.verboseLog("WriteOutPut nucleotideSequences size: " +  nucleotideSequences.size() + " and count : " + count);
            }
            writer.close();
        }catch (JAXBException e){
            e.printStackTrace();
        }catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }


    private void outputToJSON(Path outputPath, StepInstance stepInstance, String sequenceType, boolean isSlimOutput) throws IOException {
        Utilities.verboseLog(10, " WriteOutputStep - outputToJSON " );
        IMatchesHolder matchesHolder = getMatchesHolder(stepInstance, sequenceType, isSlimOutput);

        Utilities.verboseLog(10, " WriteOutputStep - outputToJSON json-slim? " + isSlimOutput);
        try (ProteinMatchesJSONResultWriter writer = new ProteinMatchesJSONResultWriter(outputPath, isSlimOutput)) {
            //old way??
            //writer.write(matchesHolder, proteinDAO, sequenceType, isSlimOutput);

        }
        //Try writing to JSOn from this module

        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        final Map<String, String> parameters = stepInstance.getParameters();
        final boolean mapToPathway = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_PATHWAY));
        final boolean mapToGO = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_GO));
        final boolean mapToInterProEntries = mapToPathway || mapToGO || Boolean.TRUE.toString().equals(parameters.get(MAP_TO_INTERPRO_ENTRIES));
        //writer.setMapToInterProEntries(mapToInterProEntries);
        //writer.setMapToGO(mapToGO);
       // writer.setMapToPathway(mapToPathway);
        if (sequenceType.equalsIgnoreCase("p")){
            try (ProteinMatchesJSONResultWriter writer = new ProteinMatchesJSONResultWriter(outputPath, isSlimOutput)) {
                writer.header(interProScanVersion);
                if (bottomProteinId != null && topProteinId != null) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Load " + topProteinId + " proteins from the db.");
                    }
                    Utilities.verboseLog(10, " WriteOutputStep -JSON new " + " There are " + topProteinId + " proteins.");
                    int count = 0;
                    for (Long proteinIndex = bottomProteinId; proteinIndex <= topProteinId; proteinIndex++) {
                        String proteinKey = Long.toString(proteinIndex);
                        Protein protein = proteinDAO.getProtein(proteinKey);
                        if (protein == null) {
                            LOGGER.warn("protein with id  " + proteinIndex + " was null");
                            continue;
                        }
                        writer.write(protein);
                        count++;
                        if (count < topProteinId) {
                            writer.write(","); // More proteins/nucleotide sequences to follow
                        }
                    }
                }
                writer.footer();
            }
        }
        if ( sequenceType.equalsIgnoreCase("n")){
            try (ProteinMatchesJSONResultWriter writer = new ProteinMatchesJSONResultWriter(outputPath, isSlimOutput)) {
                writer.header(interProScanVersion);
                if (bottomProteinId != null && topProteinId != null) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Load " + topProteinId + " proteins from the db.");
                    }
                    final Set<NucleotideSequence> nucleotideSequences = nucleotideSequenceDAO.getNucleotideSequences();
                    Utilities.verboseLog(10, " WriteOutputStep - JSON  NucleotideSequence " + " There are " + nucleotideSequences.size() + " nucleotides.");
                    int count = 0;

                    for (NucleotideSequence nucleotideSequence :nucleotideSequences){
                        count++;
                        for (OpenReadingFrame orf: nucleotideSequence.getOpenReadingFrames()) {
                            Protein protein = orf.getProtein();
                            String proteinKey = Long.toString(protein.getId());
                            Protein proteinMarshalled = proteinDAO.getProtein(proteinKey);
                            //protein = proteinMarshalled;
                            orf.setProtein(proteinMarshalled);
                        }
                        writer.write(nucleotideSequence);
                        if (count < nucleotideSequences.size()) {
                            writer.write(","); // More proteins/nucleotide sequences to follow
                        }
                    }

                    Utilities.verboseLog("WriteOutPut nucleotideSequences size: " +  nucleotideSequences.size());
                }
                writer.close();
            }
        }

    }

    private IMatchesHolder getMatchesHolder(StepInstance stepInstance, String sequenceType, boolean isSlimOutput) {
        IMatchesHolder matchesHolder = null;
        /*
        if (sequenceType.equalsIgnoreCase("n")) {
            matchesHolder = new NucleicAcidMatchesHolder(interProScanVersion);
        } else {
            matchesHolder = new ProteinMatchesHolder(interProScanVersion);
        }

        final Map<String, String> parameters = stepInstance.getParameters();
        final boolean excludeSites = Boolean.TRUE.toString().equals(parameters.get(StepInstanceCreatingStep.EXCLUDE_SITES));
        if (excludeSites || this.excludeSites) { // Command line argument takes preference over proprties file config
            removeSites(proteins, true);
        }
        else if (isSlimOutput) {
            removeSites(proteins, false);
        }

        if (isSlimOutput) {
            // Only include a protein in the output if it has at least one match
            for (Protein protein : proteins) {
                Set<Match> matches = protein.getMatches();
                if (matches != null && matches.size() > 0) {
                    matchesHolder.addProtein(protein);
                }
            }
        } else {
            // Include all proteins in the output, whether they have any matches or not
            matchesHolder.addProteins(proteins);
        }
        */
        return matchesHolder;
    }

    private void outputToTSV(final Path path,
                             final StepInstance stepInstance , String sequenceType ) throws IOException {
        try (ProteinMatchesTSVResultWriter writer = new ProteinMatchesTSVResultWriter(path, sequenceType.equalsIgnoreCase("p"))) {
            writeProteinMatches(writer, stepInstance);
        }
        //write the site tsv production output
        //only for CDD and SFLD

        final Map<String, String> parameters = stepInstance.getParameters();
        String analysisJobNames = parameters.get(StepInstanceCreatingStep.ANALYSIS_JOB_NAMES_KEY);
        if (analysisJobNames == null ||
                analysisJobNames.toLowerCase().contains("cdd") ||
                analysisJobNames.toLowerCase().contains("sfld")) {
            final boolean includeTsvSites = Boolean.TRUE.toString().equals(parameters.get(StepInstanceCreatingStep.INCLUDE_TSV_SITES));
            if (includeTsvSites) {
                Path tsvProSitesPath = Paths.get(path.toString() + ".sites");
                Utilities.verboseLog("tsv site path: " + tsvProSitesPath.getFileName().toString());
                try (ProteinSiteMatchesTSVResultWriter tsvSitesWriter = new ProteinSiteMatchesTSVResultWriter(tsvProSitesPath)) {
                    writeProteinMatches(tsvSitesWriter, stepInstance);
                }
            }
        }

//        // Include accompanying TSV version file? If filename already exists it will get replaced
        final boolean inclTSVVersion = Boolean.TRUE.toString().equals(parameters.get(INCL_TSV_VERSION));
        if (inclTSVVersion) {
            final String tsvVersionFilename = path.toString() + ".version";
            final Path tsvVersionPath = Paths.get(tsvVersionFilename);
            if (Files.exists(tsvVersionPath)) {
                System.out.println("Warning: Overwriting existing TSV version output file " + tsvVersionFilename);
            }
            try (BufferedWriter v = Files.newBufferedWriter(tsvVersionPath, Charset.defaultCharset())) {
                v.write(interProScanVersion);
            }
            catch (Exception e) {
                // If we fail to write the TSV version file just report the issue and continue - not worth stopping execution for that!
                System.out.println("Unable to write TSV version file " + tsvVersionFilename + " due to exception: ");
                e.printStackTrace();

            }

        }
    }

    private void outputToTSVPRO(final Path path,
                             final StepInstance stepInstance) throws IOException {
        //first write the tsv production output
        try (ProteinMatchesTSVProResultWriter writer = new ProteinMatchesTSVProResultWriter(path)) {
            writeProteinMatches(writer, stepInstance);
        }
        //write the site tsv production output
        //only for CDD and SFLD
        final Map<String, String> parameters = stepInstance.getParameters();
        String analysisJobNames = parameters.get(StepInstanceCreatingStep.ANALYSIS_JOB_NAMES_KEY);
        if (analysisJobNames == null ||
                analysisJobNames.toLowerCase().contains("cdd") ||
                analysisJobNames.toLowerCase().contains("sfld")) {
            final boolean excludeSites = Boolean.TRUE.toString().equals(parameters.get(StepInstanceCreatingStep.EXCLUDE_SITES));
            if (!excludeSites) {
                Path tsvProSitesPath = Paths.get(path.toString() + ".sites");
                Utilities.verboseLog("tsv site path: " + tsvProSitesPath.getFileName().toString());
                try (ProteinSiteMatchesTSVResultWriter tsvSitesWriter = new ProteinSiteMatchesTSVResultWriter(tsvProSitesPath)) {

                    writeProteinMatches(tsvSitesWriter, stepInstance);
                }
            }
        }
    }

    private void outputToGFF(Path path, StepInstance stepInstance, String sequenceType) throws IOException {
        ProteinMatchesGFFResultWriter writer = null;
        try {
            if (sequenceType.equalsIgnoreCase("n")) {
                writer = new GFFResultWriterForNucSeqs(path, interProScanVersion, false);
            }//Default tsvWriter for proteins
            else {
                writer = new GFFResultWriterForProtSeqs(path, interProScanVersion, true, true);
            }

            //This step writes features (protein matches) into the GFF file
            writeProteinMatches(writer, stepInstance);
            //This step writes FASTA sequence at the end of the GFF file
            writeFASTASequences(writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void outputToGFFPartial(Path path, StepInstance stepInstance) throws IOException {
        try (ProteinMatchesGFFResultWriter writer = new GFFResultWriterForProtSeqs(path, interProScanVersion, false, false)) {
            writeProteinMatches(writer, stepInstance);
        }
    }


    private void outputToHTML(final Path path, StepInstance stepInstance) throws IOException {
        // E.g. for "-b OUT" file = "/home/matthew/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT.html.tar.gz"
        writeGraphicalProteinMatches(htmlResultWriter, stepInstance);
        List<Path> resultFiles = htmlResultWriter.getResultFiles();
            // E.g. resultFiles =
            // - data/freemarker/resources
            //   - data/freemarker/resources/images
            //     - data/freemarker/resources/images/ico_type_family_small.png
            //     ...
            //   - data/freemarker/resources/javascript
            //   ...
            // - ~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/my-computer-name_20160301_141713605_ivyx/jobWriteOutput/P22298.html
            // - ~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/my-computer-name_20160301_141713605_ivyx/jobWriteOutput/P02939.html
            // ...

            buildTarArchive(path, resultFiles);
    }

    /**
     * This method renders the SVG output files.
     * <p/>
     * Please note:
     * <p/>
     * When the SVG files aren't archived, they are written to the output file directory.
     * Otherwise they will be written into the default outputDirectory of the {@link ProteinMatchesSVGResultWriter).
     *
     * @param outputDir Output directory (this file object contains the output path).
     * @param proteins  Set of result proteins.
     * @throws IOException
     */
    private void outputToSVG(final Path path, StepInstance stepInstance) throws IOException {
        // E.g. for "-b OUT" outputDir = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT.svg.tar.gz"
            //If the archive mode is switched off single SVG files should be written to the global output directory
            if (!archiveSVGOutput) {
                final String outputDirPath = path.toAbsolutePath().toString();
                svgResultWriter.setTempDirectory(outputDirPath);
            }
        writeGraphicalProteinMatches(svgResultWriter, stepInstance);
            if (archiveSVGOutput) {
                List<Path> resultFiles = svgResultWriter.getResultFiles();
                // E.g. resultFiles =
                // - ~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/my-computer-name_20160301_141713605_ivyx/jobWriteOutput/P22298.svg
                // - ~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/my-computer-name_20160301_141713605_ivyx/jobWriteOutput/P02939.svg
                // ...

                buildTarArchive(path, resultFiles);
            }
    }

    private void buildTarArchive(Path path, List<Path> resultFiles) throws IOException {
        TarArchiveBuilder tarArchiveBuilder = new TarArchiveBuilder(resultFiles, path, compressHtmlAndSVGOutput);
        tarArchiveBuilder.buildTarArchive();
        //Delete result files in the temp directory at the end
        for (Path resultFile : resultFiles) {
            //Only delete HTML/SVG files, but not the resource directory which is also part of the result files list
            if (Files.isRegularFile(resultFile)) {
                try {
                    Files.delete(resultFile);
                } catch (IOException e) {
                    if (LOGGER.isEnabledFor(Level.WARN)) {
                        final String r = resultFile.toAbsolutePath().toString();
                        LOGGER.warn("Couldn't delete file " + r);
                    }
                }
            }
        }
    }

    private void writeFASTASequences(ProteinMatchesGFFResultWriter writer) throws IOException {
        writer.writeFASTADirective();
        Map<String, String> identifierToSeqMap = writer.getIdentifierToSeqMap();
        for (String key : identifierToSeqMap.keySet()) {
            writer.writeFASTASequence(key, identifierToSeqMap.get(key));
        }
    }

    private void writeProteinMatches(ProteinMatchesResultWriter writer, StepInstance stepInstance) throws IOException {
        Utilities.verboseLog(10, " WriteOutputStep - outputToTSV-etc ");
        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        final Map<String, String> parameters = stepInstance.getParameters();
        final boolean mapToPathway = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_PATHWAY));
        final boolean mapToGO = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_GO));
        final boolean mapToInterProEntries = mapToPathway || mapToGO || Boolean.TRUE.toString().equals(parameters.get(MAP_TO_INTERPRO_ENTRIES));
        writer.setMapToInterProEntries(mapToInterProEntries);
        writer.setMapToGO(mapToGO);
        writer.setMapToPathway(mapToPathway);
        if (bottomProteinId != null && topProteinId != null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Load " + topProteinId + " proteins from the db.");
            }
            Utilities.verboseLog(10, " WriteOutputStep -tsv-etc " + " There are " + topProteinId + " proteins.");
            int count = 0;
            for (Long proteinIndex= bottomProteinId;proteinIndex <= topProteinId; proteinIndex ++){
                String proteinKey = Long.toString(proteinIndex);
                Protein protein = proteinDAO.getProtein(proteinKey);
                if(protein == null || protein.getMatches().isEmpty()){
                    continue;
                }
                writer.write(protein);
                count++;
                if (count % 40000 == 0) {
                    Utilities.verboseLog(10, " WriteOutout - wrote out matches for " + count + " proteins");
                }
            }
        }
    }

    private void writeGraphicalProteinMatches(GraphicalOutputResultWriter writer, StepInstance stepInstance) throws IOException {
        Utilities.verboseLog(10, " WriteOutputStep - outputToTSV-etc ");
        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        if (bottomProteinId != null && topProteinId != null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Load " + topProteinId + " proteins from the db.");
            }
            Utilities.verboseLog(10, " WriteOutputStep -tsv-etc " + " There are " + topProteinId + " proteins.");
            int count = 0;
            for (Long proteinIndex= bottomProteinId;proteinIndex <= topProteinId; proteinIndex ++){
                String proteinKey = Long.toString(proteinIndex);
                Protein protein = proteinDAO.getProtein(proteinKey);
                if(protein == null || protein.getMatches().isEmpty()){
                    continue;
                }
                writer.write(protein, entryHierarchy);
                count++;
                if (count % 40000 == 0) {
                    Utilities.verboseLog(10, " WriteOutout - wrote out matches for " + count + " proteins");
                }
            }
        }
    }

    /**
     * Remove sites from any protein match locations (make sites NULL so they don't appear at all in the XML output)
     * @param proteins The proteins
     * @param all Remove all site data (not just empty sites)?
     */
    private void removeSites(List<Protein> proteins, boolean all) {
        for (Protein protein : proteins) {
            Set<Match> matches = protein.getMatches();
            if (matches != null && matches.size() > 0) {
                for (Match match : matches) {
                    Set<Location> locations = match.getLocations();
                    if (locations != null && locations.size() > 0) {
                        for (Location location : locations) {
                            if (location instanceof LocationWithSites) {
                                LocationWithSites l = (LocationWithSites) location;
                                Set<Site> sites = l.getSites();
                                if (sites != null) {
                                    if (all || sites.size() < 1) {
                                        l.setSites(null);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}