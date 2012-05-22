package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.XmlWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.*;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.ProteinMatchesHTMLResultWriter;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.TarArchiveBuilder;
import uk.ac.ebi.interpro.scan.model.IMatchesHolder;
import uk.ac.ebi.interpro.scan.model.NucleicAcidMatchesHolder;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinMatchesHolder;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Writes all matches for a slice of proteins to a file.
 * <p/>
 * Should be run once analysis is complete.
 *
 * @author ?
 * @author Maxim Scheremetjew
 */

public class WriteOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(WriteOutputStep.class.getName());

    private ProteinDAO proteinDAO;

    private boolean deleteWorkingDirectoryOnCompletion;

    private XmlWriter xmlWriter;

    private ProteinMatchesHTMLResultWriter htmlResultWriter;

    private boolean compressHtmlOutput;

    @Required
    public void setCompressHtmlOutput(boolean compressHtmlOutput) {
        this.compressHtmlOutput = compressHtmlOutput;
    }

    @Required
    public void setHtmlResultWriter(ProteinMatchesHTMLResultWriter htmlResultWriter) {
        this.htmlResultWriter = htmlResultWriter;
    }

    @Required
    public void setXmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Required
    public void setDeleteWorkingDirectoryOnCompletion(String deleteWorkingDirectoryOnCompletion) {
        this.deleteWorkingDirectoryOnCompletion = "true".equalsIgnoreCase(deleteWorkingDirectoryOnCompletion);
    }

    public static final String OUTPUT_FILE_PATH_KEY = "OUTPUT_PATH";
    public static final String OUTPUT_FILE_FORMATS = "OUTPUT_FORMATS";
    public static final String MAP_TO_INTERPRO_ENTRIES = "MAP_TO_INTERPRO_ENTRIES";
    public static final String MAP_TO_GO = "MAP_TO_GO";
    public static final String MAP_TO_PATHWAY = "MAP_TO_PATHWAY";
    public static final String SEQUENCE_TYPE = "SEQUENCE_TYPE";

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting step with Id " + this.getId());
        }
        final Map<String, String> parameters = stepInstance.getParameters();
        final String outputFormatStr = parameters.get(OUTPUT_FILE_FORMATS);
        final Set<FileOutputFormat> outputFormats = FileOutputFormat.stringToFileOutputFormats(outputFormatStr);
        final String filePathName = parameters.get(OUTPUT_FILE_PATH_KEY);

        for (FileOutputFormat outputFormat : outputFormats) {
            Integer counter = null;
            boolean pathAvailable = false;
            File outputFile = null;

            // Try to use the file name provided. If the file already exists, append a bracketed number (Chrome style).
            // but using an underscore rather than a space (pah!)
            while (!pathAvailable) {
                final StringBuilder candidateFileName = new StringBuilder(filePathName);
                if (counter == null) {
                    counter = 1;
                } else {
                    // E.g. Output file name could become "test_proteins.fasta_1.tsv"
                    candidateFileName
                            .append("_")
                            .append(counter++);
                }
                candidateFileName
                        .append('.')
                        .append(outputFormat.getFileExtension());
                if (outputFormat.getFileExtension().equals("html")) {
                    outputFile = new File(buildTarArchiveName(candidateFileName.toString(), compressHtmlOutput));
                } else {
                    outputFile = new File(candidateFileName.toString());
                }
                pathAvailable = !outputFile.exists();
            }
            try {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Writing out " + outputFormat + " file");
                }
                final String sequenceType = parameters.get(SEQUENCE_TYPE);
                switch (outputFormat) {
                    case TSV:
                        outputToTSV(outputFile, stepInstance);
                        break;
                    case XML:
                        outputToXML(outputFile, stepInstance, sequenceType);
                        break;
                    case GFF3:
                        outputToGFF(outputFile, stepInstance, sequenceType);
                        break;
                    case HTML:
                        //Replace the default temp dir with the user specified one
                        if (temporaryFileDirectory != null) {
                            htmlResultWriter.setTempDirectory(temporaryFileDirectory);
                        }
                        outputToHTML(outputFile, stepInstance);
                        break;
                    default:
                        LOGGER.warn("Unrecognised output format " + outputFormat + " - cannot write the output file.");
                }
            } catch (IOException ioe) {
                throw new IllegalStateException("IOException thrown when attempting to writeComment output from InterProScan", ioe);
            }
        }


        if (deleteWorkingDirectoryOnCompletion) {
            // Clean up empty working directory.
            final String workingDirectory = temporaryFileDirectory.substring(0, temporaryFileDirectory.lastIndexOf('/'));
            File file = new File(workingDirectory);
            if (file.exists()) {
                for (File subDir : file.listFiles()) {
                    if (!subDir.delete()) {
                        LOGGER.warn("At run completion, unable to delete temporary directory " + subDir.getAbsolutePath());
                    }
                }
            }
            if (!file.delete()) {
                LOGGER.warn("At run completion, unable to delete temporary directory " + file.getAbsolutePath());
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Step with Id " + this.getId() + " finished.");
        }
    }

    private void outputToXML(File outputFile, StepInstance stepInstance, String sequenceType) throws IOException {
        final List<Protein> proteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        IMatchesHolder matchesHolder;
        if (sequenceType.equalsIgnoreCase("n")) {
            matchesHolder = new NucleicAcidMatchesHolder();
        } else {
            matchesHolder = new ProteinMatchesHolder();
        }
        matchesHolder.addProteins(proteins);
        xmlWriter.writeMatches(outputFile, matchesHolder);
    }

    private void outputToTSV(File file, StepInstance stepInstance) throws IOException {
        ProteinMatchesTSVResultWriter writer = null;
        try {
            writer = new ProteinMatchesTSVResultWriter(file);
            writeProteinMatches(writer, stepInstance);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void outputToGFF(File file, StepInstance stepInstance, String sequenceType) throws IOException {
        ProteinMatchesGFFResultWriter writer = null;
        try {
            if (sequenceType.equalsIgnoreCase("n")) {
                writer = new GFFResultWriterForNucSeqs(file);
            }//Default tsvWriter for proteins
            else {
                writer = new GFFResultWriterForProtSeqs(file);
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

    private void outputToHTML(File file, StepInstance stepInstance) throws IOException {
        //TODO: Think about how to add cross-references to protein object!
        List<Protein> proteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        if (proteins != null && proteins.size() > 0) {
            for (Protein protein : proteins) {
                htmlResultWriter.write(protein);
            }
            List<File> resultFiles = htmlResultWriter.getResultFiles();
            TarArchiveBuilder tarArchiveBuilder = new TarArchiveBuilder(resultFiles, file, compressHtmlOutput);
            tarArchiveBuilder.buildTarArchive();
            //Delete result files in the temp directory at the end
            for (File resultFile : resultFiles) {
                //Only delete HTML files, but not the resource directory which is also part of the result files list
                if (resultFile.isFile()) {
                    boolean isDeleted = resultFile.delete();
                    if (LOGGER.isEnabledFor(Level.WARN)) {
                        if (!isDeleted) {
                            LOGGER.warn("Couldn't delete file " + resultFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * Builds a sensible tarball file name.<br>
     * e.g. for a compressed file it would be: file-name.tar.gz
     * <p/>
     * The expected file format would be <file-name>.<extension>
     *
     * @param fileName           Input filename without extension
     * @param compressHtmlOutput If TRUE,do compress tarball as well.
     * @return Tarball filename with extension added
     */
    private String buildTarArchiveName(String fileName, boolean compressHtmlOutput) {
        if (fileName == null) {
            throw new IllegalStateException("HTML output file name was NULL");
        } else if (fileName.length() == 0) {
            throw new IllegalStateException("HTML output file name was empty");
        }
        String fileExtension = (compressHtmlOutput ? ".tar.gz" : ".tar");
        if (fileName.endsWith(fileExtension)) {
            return fileName;
        }

        return fileName + fileExtension;
    }


    private void writeFASTASequences(ProteinMatchesGFFResultWriter writer) throws IOException {
        Map<String, String> identifierToSeqMap = writer.getIdentifierToSeqMap();
        for (String key : identifierToSeqMap.keySet()) {
            writer.writeFASTASequence(key, identifierToSeqMap.get(key));
        }
    }

    private void writeProteinMatches(ProteinMatchesResultWriter writer, StepInstance stepInstance) throws IOException {
        final Map<String, String> parameters = stepInstance.getParameters();
        final boolean mapToPathway = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_PATHWAY));
        final boolean mapToGO = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_GO));
        final boolean mapToInterProEntries = mapToPathway || mapToGO || Boolean.TRUE.toString().equals(parameters.get(MAP_TO_INTERPRO_ENTRIES));
        final List<Protein> proteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        writer.setMapToInterProEntries(mapToInterProEntries);
        writer.setMapToGo(mapToGO);
        writer.setMapToPathway(mapToPathway);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Writing output:" + writer.getClass().getCanonicalName());
        }
        int locationCount = 0;
        if (proteins != null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Loaded " + proteins.size() + " proteins...");
            }
            for (Protein protein : proteins) {
                locationCount += writer.write(protein);
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Written out " + locationCount + " locations (should equal rows in TSV).");
        }
    }
}
