package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.XmlWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.ProteinMatchTSVWriter;
import uk.ac.ebi.interpro.scan.io.serialization.ObjectSerializerDeserializer;
import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.SignatureLibraryIntegratedMethods;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.MatchesHolder;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
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

    private ObjectSerializerDeserializer<Map<SignatureLibrary, SignatureLibraryIntegratedMethods>> serializerDeserializer;

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

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setSerializerDeserializer(ObjectSerializerDeserializer<Map<SignatureLibrary, SignatureLibraryIntegratedMethods>> serializerDeserializer) {
        this.serializerDeserializer = serializerDeserializer;
    }


    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        final Map<String, String> parameters = stepInstance.getParameters();
        final String outputFormat = parameters.get(OUTPUT_FILE_FORMAT);
        final File outputFile = new File(parameters.get(OUTPUT_FILE_PATH_KEY));
        try {
            if ("tsv".equalsIgnoreCase(outputFormat)) {
                LOGGER.info("Writing out TSV file");
                outputToTSV(outputFile, stepInstance);
            } else if ("xml".equalsIgnoreCase(outputFormat)) {
                LOGGER.info("Writing out XML file");
                outputToXML(outputFile, stepInstance);
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("IOException thrown when attempting to write output from InterProScan", ioe);
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
    }

    private void outputToXML(File outputFile, StepInstance stepInstance) throws IOException {
        final List<Protein> proteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        MatchesHolder matches = new MatchesHolder();
        matches.addProteins(proteins);
        xmlWriter.writeMatches(outputFile, matches);
    }

    private void outputToTSV(File file, StepInstance stepInstance) throws IOException {
        ProteinMatchTSVWriter writer = null;
        try {
            writer = new ProteinMatchTSVWriter(file);
            final Map<String, String> parameters = stepInstance.getParameters();
            final boolean mapToGO = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_GO));
            final boolean mapToInterProEntries = mapToGO || Boolean.TRUE.toString().equals(parameters.get(MAP_TO_INTERPRO_ENTRIES));
            if (mapToInterProEntries || mapToGO) {
                writer.setInterProGoMapping(serializerDeserializer.deserialize());
            }
            final List<Protein> proteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            writer.setMapToInterProEntries(mapToInterProEntries);
            writer.setMapToGo(mapToGO);
            LOGGER.info("Writing output:" + writer.getClass().getCanonicalName());
            if (proteins != null) {
                LOGGER.info("Loaded " + proteins.size() + " proteins...");
                if (proteins.size() > 0 && proteins.get(0).getMatches().size() == 0) {
                    LOGGER.info("Couldn't load protein matches!");
                }
            }
            for (Protein protein : proteins) {
                writer.write(protein);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
