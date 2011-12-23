package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.XmlWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.*;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.MatchesHolder;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Writes all matches for a slice of proteins to a file.
 * <p/>
 * Should be run once analysis is complete.
 */

public class WriteOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(WriteOutputStep.class.getName());

    private ProteinDAO proteinDAO;

    private boolean deleteWorkingDirectoryOnCompletion;

    private XmlWriter xmlWriter;

    @Required
    public void setXmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Required
    public void setDeleteWorkingDirectoryOnCompletion(String deleteWorkingDirectoryOnCompletion) {
        this.deleteWorkingDirectoryOnCompletion = "true".equalsIgnoreCase(deleteWorkingDirectoryOnCompletion);
    }

    public static final String OUTPUT_FILE_PATH_KEY = "OUTPUT_PATH";
    public static final String OUTPUT_FILE_FORMAT = "OUTPUT_FORMAT";
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
        LOGGER.info("Starting step with Id " + this.getId());
        final Map<String, String> parameters = stepInstance.getParameters();
        final String outputFormat = parameters.get(OUTPUT_FILE_FORMAT);
        final File outputFile = new File(parameters.get(OUTPUT_FILE_PATH_KEY));
        if (outputFile.exists()) {
            if (!outputFile.delete()) {
                throw new IllegalStateException("The output file already exists and cannot be overwritten.");
            }
        }
        try {
            if ("tsv".equalsIgnoreCase(outputFormat)) {
                LOGGER.info("Writing out TSV file");
                outputToTSV(outputFile, stepInstance);
            } else if ("xml".equalsIgnoreCase(outputFormat)) {
                LOGGER.info("Writing out XML file");
                outputToXML(outputFile, stepInstance);
            } else if ("gff".equalsIgnoreCase(outputFormat)) {
                LOGGER.info("Writing out GFF file (version 3)");
                final String sequenceType = parameters.get(SEQUENCE_TYPE);
                outputToGFF(outputFile, stepInstance, sequenceType);
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("IOException thrown when attempting to writeComment output from InterProScan", ioe);
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
        LOGGER.info("Step with Id " + this.getId() + " finished.");
    }

    private void outputToXML(File outputFile, StepInstance stepInstance) throws IOException {
        final List<Protein> proteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        MatchesHolder matches = new MatchesHolder();
        matches.addProteins(proteins);
        xmlWriter.writeMatches(outputFile, matches);
    }

    private void outputToTSV(File file, StepInstance stepInstance) throws IOException {
        ProteinMatchesTSVResultWriter writer = new ProteinMatchesTSVResultWriter(file);
        try {
            writeProteinMatches(writer, stepInstance);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void outputToGFF(File file, StepInstance stepInstance, String sequenceType) throws IOException {
        ProteinMatchesGFFResultWriter writer;
        if (sequenceType.equalsIgnoreCase("n")) {
            writer = new GFFResultWriterForNucSeqs(file);
        }//Default tsvWriter for proteins
        else {
            writer = new GFFResultWriterForProtSeqs(file);
        }
        try {
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
        LOGGER.info("Writing output:" + writer.getClass().getCanonicalName());
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