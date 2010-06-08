package uk.ac.ebi.interpro.scan.io.prints;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Parser for pval files that contain PRINTS models. (Models in PRINTS parlance!)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PvalParser implements Serializable {

    // /ebi/production/interpro/data/members/prints/40.0/print.pval

    private static final Logger LOGGER = Logger.getLogger(PvalParser.class);

    private static final String LINE_SIG_NAME = "gc";
    private static final String LINE_SIG_ACCESSION = "gx";
    private static final String LINE_SIG_DESCRIPTION = "gi";
    private static final String LINE_SIG_MODEL_COUNT = "gn";
    private static final String LINE_MODEL_ACCESSION = "mx";
    private static final String LINE_MODEL_NAME = "mi";

    private String releaseVersion;

    public PvalParser(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    @Transactional
    public SignatureLibraryRelease parse(Map<String, String> kdatFileData, String printsPvalFilePath) throws IOException {
        SignatureLibraryRelease release = new SignatureLibraryRelease(SignatureLibrary.PRINTS, releaseVersion);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(printsPvalFilePath));
            PartialModel partialModelUnderConstruction = null;
            final List<PartialModel> models = new ArrayList<PartialModel>();
            String line, sigAcc = null, sigName = null, sigDescription = null;
            Integer modelCount = null;
            while ((line = reader.readLine()) != null) {
                if (LOGGER.isDebugEnabled()) {
                    if (line.indexOf(';') > -1) {
                        LOGGER.debug(line);
                    }
                }
                if (line.startsWith(LINE_SIG_NAME)) {
                    createSignature(release, kdatFileData, sigAcc, sigName, sigDescription, models, modelCount);

                    // Now clear / reset all local variables.
                    sigName = extractLineContent(line);
                    sigAcc = null;
                    sigDescription = null;
                    partialModelUnderConstruction = null;
                    modelCount = null;
                    models.clear();
                } else if (line.startsWith(LINE_SIG_ACCESSION)) {
                    sigAcc = extractLineContent(line);
                } else if (line.startsWith(LINE_SIG_MODEL_COUNT)) {
                    String modelCountString = extractLineContent(line);
                    modelCount = Integer.parseInt(modelCountString);
                } else if (line.startsWith(LINE_SIG_DESCRIPTION)) {
                    sigDescription = extractLineContent(line);
                } else if (line.startsWith(LINE_MODEL_ACCESSION)) {
                    partialModelUnderConstruction = new PartialModel(extractLineContent(line));
                    models.add(partialModelUnderConstruction);
                } else if (line.startsWith(LINE_MODEL_NAME)) {
                    partialModelUnderConstruction.setName(extractLineContent(line));
                }
            }
            createSignature(release, kdatFileData, sigAcc, sigName, sigDescription, models, modelCount);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return release;
    }

    private String extractLineContent(String line) {
        return line.substring(3).trim();
    }

    private void createSignature(final SignatureLibraryRelease release, Map<String, String> kdatFileData, final String sigAcc, final String sigName, final String sigDescription, final List<PartialModel> partialModels, final Integer modelCount) {
        if (sigName != null) {   // If sigName is null, this is the very first one in the file, so nothing to do.
            if (modelCount == null || modelCount != partialModels.size()) {
                throw new ParseException("The number of models found for " + sigAcc + " does not match the value given on the gn; line. (gn: " + modelCount + ", number of models: " + partialModels.size());
            }

            final String sigAbstract = kdatFileData.get(sigAcc);

            if (sigAbstract == null) {
                throw new ParseException("There is no corresponding entry in the kdat file for fingerprint " + sigAcc);
            }

            final Set<Model> models = new HashSet<Model>();
            // Build the Models
            for (PartialModel partialModel : partialModels) {
                models.add(new Model(partialModel.getAccession(), partialModel.getName(), null));
            }

            release.addSignature(
                    new Signature(sigAcc, sigName, null, sigDescription, sigAbstract, release, models)
            );
        }
    }

    /**
     * Simple bean to allow model data to be parsed out - temporary object.
     */
    private class PartialModel {

        private final String accession;

        private String name;

        private PartialModel(String accession) {
            this.accession = accession;
        }

        public String getAccession() {
            return accession;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            if (this.name != null) {
                throw new IllegalStateException("Attempting to set the name of this PartialModel (PRINTS parsing object), but this value has already been set.");
            }
            this.name = name;
        }
    }

}
