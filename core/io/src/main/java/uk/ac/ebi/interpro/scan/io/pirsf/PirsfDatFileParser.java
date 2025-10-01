package uk.ac.ebi.interpro.scan.io.pirsf;

import org.springframework.beans.factory.annotation.Required;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern PIRSF_DAT_PATTERN_FAM = Pattern.compile("^>(PIRSF\\d{6})");
    private static final Pattern PIRSF_DAT_PATTERN_CHILD = Pattern.compile("child:\\s*(.+)$");
    private static final Pattern PIRSF_DAT_PATTERN_VALUES = Pattern.compile("^(\\d+\\.?\\d*)\\s+(\\d+\\.?\\d*)\\s+(\\d+\\.?\\d*)\\s+(\\d+\\.?\\d*)\\s+(\\d+\\.?\\d*)$");
    private static final Object PIRSF_DAT_LOCK = new Object();
    private Map<String, PirsfDatRecord> records;
    private String datFile;

    @Required
    public void setDatFile(String datFile) {
        this.datFile = datFile;
    }

    public Map<String, PirsfDatRecord> getRecords() throws IOException {
        if (records == null) {
            synchronized (PIRSF_DAT_LOCK) {
                this.loadRecords();
            }
        }

        return records;
    }

    private void loadRecords() throws IOException {
        records = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(datFile))) {
            String line;
            PirsfDatRecord pirsfDatRecord = null;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = PIRSF_DAT_PATTERN_FAM.matcher(line);
                if (matcher.find()) {
                    if (pirsfDatRecord != null) {
                        records.put(pirsfDatRecord.getModelAccession(), pirsfDatRecord);
                    }

                    final String modelAccession = matcher.group(1);
                    pirsfDatRecord = new PirsfDatRecord(modelAccession);

                    matcher = PIRSF_DAT_PATTERN_CHILD.matcher(line);
                    if (matcher.find()) {
                        String[] chunks  = matcher.group(1).split("\\s+");
                        for (String chunk : chunks) {
                            pirsfDatRecord.addSubFamily(chunk);
                        }
                    }

                    continue;
                }

                matcher = PIRSF_DAT_PATTERN_VALUES.matcher(line);
                if (matcher.find()) {
                    pirsfDatRecord.setValues(matcher);
                }
            }

            if (pirsfDatRecord != null) {
                records.put(pirsfDatRecord.getModelAccession(), pirsfDatRecord);
            }
        }
    }
}