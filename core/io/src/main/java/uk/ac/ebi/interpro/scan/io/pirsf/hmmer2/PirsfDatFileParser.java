package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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

    private static final Logger LOGGER = LogManager.getLogger(PirsfDatFileParser.class.getName());

    //Pattern for superfamily accessions only (PIRSF037506)
    private static final Pattern PIRSF_DAT_PATTERN_SUPERFAM = Pattern.compile("^>PIRSF[0-9]{6}$");
    //Pattern for superfamily and subfamilies (PIRSF016158 child: PIRSF500165 PIRSF500166)
    private static final Pattern PIRSF_DAT_PATTERN_SUBFAM = Pattern.compile("^>PIRSF[0-9]{6}[a-zA-Z0-9 :]+$");


    public static Map<String, PirsfDatRecord> parse(final Resource pirsfDatFileResource) throws IOException {
        LOGGER.debug("Running PIRSF data file parser...");
        if (pirsfDatFileResource == null) {
            throw new NullPointerException("Resource to the PIRSF dat file is null");
        }
        if (!pirsfDatFileResource.exists()) {
            throw new IllegalStateException(pirsfDatFileResource.getFilename() + " does not exist");
        }
        if (!pirsfDatFileResource.isReadable()) {
            throw new IllegalStateException(pirsfDatFileResource.getFilename() + " is not readable");
        }
        //Result map
        final Map<String, PirsfDatRecord> data = new HashMap<String, PirsfDatRecord>();
        BufferedReader reader = null;
        try {
            //Read input file line by line
            reader = new BufferedReader(new InputStreamReader(pirsfDatFileResource.getInputStream()));
            String line;
            PirsfDatRecord pirsfDatRecord = null;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                Matcher modelStart = PIRSF_DAT_PATTERN_SUPERFAM.matcher(line);
                if (modelStart.find()) {
                    //New accession without sub families
                    final String modelAccession = line.substring(1);
                    pirsfDatRecord = new PirsfDatRecord(modelAccession);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Found a new model accession without sub families: " + modelAccession);
                    }
                    //Reset row attributes
                    row = 1;
                } else if (row == 2) {
                    // Model name
                    final String modelName = line;
                    pirsfDatRecord.setModelName(modelName);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Set model name to " + modelName);
                    }
                } else if (row == 3) {
                    final String[] values = line.split("\\s+");
                    pirsfDatRecord.setValues(values);
                } else if (row == 4 && line.startsWith("BLAST: ")) {
                    int index = line.indexOf(":");
                    if (index > -1 && line.length() >= index + 1) {
                        line = line.substring(index + 1).trim();
                    }
                    final boolean isBlastRequired = line.equalsIgnoreCase("YES");
                    pirsfDatRecord.setBlastRequired(isBlastRequired);
                    data.put(pirsfDatRecord.getModelAccession(), pirsfDatRecord);
                } else {
                    modelStart = PIRSF_DAT_PATTERN_SUBFAM.matcher(line);
                    if (modelStart.find()) {
                        // New accession with sub families
                        row = 1;
                        pirsfDatRecord = parseSubFamilyLine(line);
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

    private static PirsfDatRecord parseSubFamilyLine(String line) {
        PirsfDatRecord instance = null;
        String[] chunks = line.split(" ");
        for (int i = 0; i < chunks.length; i++) {
            if (i == 0) {
                if (chunks[i].length() > 1) {
                    final String modelAccession = chunks[i].substring(1);
                    instance = new PirsfDatRecord(modelAccession);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Found a new model accession with sub families: " + modelAccession);
                    }
                }
            } else if (i > 1) {
                final String subfamily = chunks[i];
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found a new subfamily named " + subfamily + " for model accession: " + instance.getModelAccession());
                }
                instance.addSubFamily(subfamily);
            }
        }
        return instance;
    }
}