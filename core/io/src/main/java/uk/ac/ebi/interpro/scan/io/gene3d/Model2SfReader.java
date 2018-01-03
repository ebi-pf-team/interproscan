package uk.ac.ebi.interpro.scan.io.gene3d;

import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Reads model2sf files.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class Model2SfReader extends AbstractModelFileParser {

    private String prefix = "G3DSA:";

    /**
     * Optional bean method, should the prefix be different to the default value.
     *
     * @param prefix being the prefix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Method to parse a model file and return a SignatureLibraryRelease.
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Override
    public SignatureLibraryRelease parse() throws IOException {
        final Map<String, String> records = parseFileToMap();

        // Create signatures
        final Map<String, Signature> signatureMap = new HashMap<>();
        for (String modelData : records.keySet()) {
            String[] model = modelData.split("#");
            if (model.length != 2) {
                throw new IllegalStateException("Model data key not in expected 'modelAc#hmmLength' format: " + modelData);
            }
            String modelAc = model[0];
            int hmmLength = Integer.parseInt(model[1]);
            String signatureAc = records.get(modelAc);
            Signature signature;
            if (signatureMap.containsKey(signatureAc)) {
                signature = signatureMap.get(signatureAc);
            } else {
                signature = new Signature(signatureAc);
                signatureMap.put(signatureAc, signature);
            }
            signature.addModel(new Model(modelAc, null, null, hmmLength));
        }

        // Create release
        return new SignatureLibraryRelease(library, releaseVersion, new HashSet<>(signatureMap.values()));
    }

    public Map<String, String> parseFileToMap() throws IOException {
        final Map<String, String> records = new HashMap<>();

        // Some example lines to parse:
        // 1q14A01 3.40.50.1220  50
        // 1vhnA02 1.10.1200.80  112


        for (Resource modelFile : modelFiles) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(modelFile.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] splitLine = line.split("\\s+");
                    if (splitLine.length != 3) {
                        throw new IllegalStateException("Unexpected format on line: " + line);
                    }

                    String model = splitLine[0];
                    String signature = splitLine[1];
                    int hmmLength = Integer.parseInt(splitLine[2]);

                    records.put(model + '#' + hmmLength, prefix + signature);  // model#hmmLength -> signature
                }
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        return records;
    }
}
