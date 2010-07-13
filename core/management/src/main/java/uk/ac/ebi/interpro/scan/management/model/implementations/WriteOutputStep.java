package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.writer.ProteinMatchTSVWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.ProteinWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.ZipWriter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
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

    enum Format {
        TSV {
            public ProteinWriter makeWriter(File file) throws IOException {
                return new ProteinMatchTSVWriter(file);
            }
        },
        ZIP {
            @Override
            public ProteinWriter makeWriter(File file) throws IOException {
                return new ZipWriter(file);
            }
        };


        public abstract ProteinWriter makeWriter(File file) throws IOException;

    }

    public static final String OUTPUT_FILE_PATH_KEY = "OUTPUT_PATH";
    public static final String OUTPUT_FILE_FORMAT = "OUTPUT_FORMAT";

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    ProteinDAO proteinDAO;


    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        Map<String, String> stepParameters = stepInstance.getStepParameters();
        final String outputFilePathName = stepParameters.get(OUTPUT_FILE_PATH_KEY);

        Format format = Format.valueOf(stepParameters.get(OUTPUT_FILE_FORMAT).toUpperCase());


        //David says: this might be more efficient, but doesn't work at the moment
        List<Protein> proteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        //List<Protein> proteins = proteinDAO.getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());

        try {
            ProteinWriter writer = format.makeWriter(new File(outputFilePathName));
            LOGGER.info("Writing output:" + writer.getClass().getCanonicalName());
            for (Protein protein : proteins) {
                writer.write(protein);
            }
            writer.close();

        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to write a fasta file to " + outputFilePathName, e);
        }

    }
}
