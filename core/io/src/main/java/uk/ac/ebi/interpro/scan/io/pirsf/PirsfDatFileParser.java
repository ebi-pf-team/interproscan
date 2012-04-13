package uk.ac.ebi.interpro.scan.io.pirsf;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to read in the pirsf.dat file.
 *
 * @author Matthew Fraser
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfDatFileParser implements Serializable {

    /*
     * Example file content (superfamily only):
     * >PIRSF000077
     * Thioredoxin
     * 110.136452241715 9.11541109440914 20.3 167.482261208577 57.6586203540026
     * BLAST: No
     *
     * Example file content (subfamilies included):
     * >PIRSF016158 child: PIRSF500165 PIRSF500166
     * Methenyltetrahydromethanopterin dehydrogenase, Hmd type
     * 343.416666666667 12.4422910294134 550.9 678.941666666667 75.8024760729346
     * BLAST: No
     *
     * [where "110.136452241715 9.11541109440914 20.3 167.482261208577 57.6586203540026" is
     * lm=mean(L); lsd=Std(L); smin=min(S); sm=mean(S); ssd=Std(S)]
     *
     * [where L is the query sequence length and S is the query HMM score.
     * mean(L) and std(L) are for the length of the family
     * mean(S), std(S) and min(S) are for the HMM score of the family]
     */

    private static final Logger LOGGER = Logger.getLogger(PirsfDatFileParser.class.getName());

    //Pattern for superfamily only
    private static final Pattern PIRSF_DAT_PATTERN_SUPERFAM = Pattern.compile("^>PIRSF[0-9]{6}$");
    //Pattern for superfamily and subfamilies
    private static final Pattern PIRSF_DAT_PATTERN_SUBFAM = Pattern.compile("^>PIRSF[0-9]{6}[a-zA-Z0-9 :]+$");

    private int row = 1;

    private String modelName;

    private String[] values;

    private boolean isBlastRequired = false;

    private String modelAccession;

    private Set<String> subfamilies;


    public Map<String, PirsfDatRecord> parse(Resource pirsfDatFileResource) throws IOException {
        if (pirsfDatFileResource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!pirsfDatFileResource.exists()) {
            throw new IllegalStateException(pirsfDatFileResource.getFilename() + " does not exist");
        }
        if (!pirsfDatFileResource.isReadable()) {
            throw new IllegalStateException(pirsfDatFileResource.getFilename() + " is not readable");
        }
        final Map<String, PirsfDatRecord> data = new HashMap<String, PirsfDatRecord>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(pirsfDatFileResource.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher modelStart = PIRSF_DAT_PATTERN_SUPERFAM.matcher(line);
                if (modelStart.find()) {
                    // New accession without sub families
                    setUpNewAccession();
                    modelAccession = line.substring(1);
                } else if (row == 2) {
                    // Model name
                    modelName = line;
                } else if (row == 3) {
                    values = line.split("\\s+");
                } else if (row == 4 && line.startsWith("BLAST: ")) {
                    int index = line.indexOf(":");
                    if (index > -1 && line.length() >= index + 1) {
                        line = line.substring(index + 1).trim();
                    }
                    isBlastRequired = line.equalsIgnoreCase("YES");
                    data.put(modelAccession, new PirsfDatRecord(modelAccession, modelName, values, isBlastRequired, subfamilies));
                } else {
                    modelStart = PIRSF_DAT_PATTERN_SUBFAM.matcher(line);
                    if (modelStart.find()) {
                        // New accession with sub families
                        setUpNewAccession();
                        getModelAccessionAndSubFamilies(line, subfamilies);
                    } else {
                        LOGGER.warn("Unexpected line in pirsf.dat: " + line);
                    }
                }
                row++;
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return data;
    }

    private void getModelAccessionAndSubFamilies(String line, Set<String> subfamilies) {
        String[] chunks = line.split(" ");
        for (int i = 0; i < chunks.length; i++) {
            if (i == 0) {
                if (chunks[i].length() > 1) {
                    modelAccession = chunks[i].substring(1);
                }
            } else if (i > 1) {
                subfamilies.add(chunks[i]);
            }
        }
    }

    private void setUpNewAccession() {
        row = 1;
        modelName = null;
        values = null;
        isBlastRequired = false;
        subfamilies = new HashSet<String>();
    }
}
