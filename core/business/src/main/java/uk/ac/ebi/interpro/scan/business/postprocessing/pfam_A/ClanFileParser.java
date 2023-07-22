package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClan;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClanData;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamModel;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Pfam Clan file.  Builds a PfamClanData object model of the contents of
 * the file, which captures clan and nesting information for models.
 * <p/>
 * On testing, an entire typical Pfam clan file uses 4.3MB of heap space to store, so
 * sticking this in memory is fine.
 *
 * @author Phil Jones
 * @version $Id: ClanFileParser.java,v 1.3 2009/10/26 17:33:17 pjones Exp $
 * @since 1.0
 */
public class ClanFileParser implements Serializable {

    /*
    Pfam-A.seed

# STOCKHOLM 1.0
#=GF ID   UCH
#=GF AC   PF00443.22
#=GF DE   Ubiquitin carboxyl-terminal hydrolase
#=GF PI   UCH-2;
#=GF AU   Finn RD, Bateman A
#=GF SE   Prosite
#=GF GA   20.50 20.50;
#=GF TC   20.50 20.70;
#=GF NC   20.40 20.30;
#=GF BM   hmmbuild  --handHMM.ann SEED.ann
#=GF SM   hmmsearch -Z 9421015 -E 1000 HMM pfamseq
#=GF TP   Family
#=GF NE   PF02338;
#=GF NL   O76364.2/329-422
#=GF NE   PF00627;
#=GF NL   Q92995.2/652-693
#=GF NE   PF00627;
#=GF NL   Q92995.2/727-767
#=GF NE   PF02148;
#=GF NL   O44787.2/396-470
#=GF NE   PF02809;
#=GF NL   Q8C0R0.1/703-720
#=GF NE   PF02809;
#=GF NL   Q8C0R0.1/805-822
#=GF NE   PF02809;
#=GF NL   Q8C0R0.1/827-844
#=GF NE   PF01753;
#=GF NL   Q19132.1/716-756
#=GF DR   INTERPRO; IPR001394;
#=GF DR   MEROPS; C19;
#=GF DR   PROSITE; PDOC00750;
#=GF DR   SCOP; 1nb8; fa;
#=GF SQ   58

    Pfam-C

# STOCKHOLM 1.0
#=GF ID   2H
#=GF AC   CL0247.6
#=GF DE   2H phosphoesterase superfamily
#=GF AU   Bateman A
#=GF RN   [1]
#=GF RM   12466548
#=GF RT   Detection of novel members, structure-function analysis and
#=GF RT   evolutionary classification of the 2H phosphoesterase superfamily.
#=GF RA   Mazumder R, Iyer LM, Vasudevan S, Aravind L;
#=GF RL   Nucleic Acids Res 2002;30:5229-5243.
#=GF CC   This clan includes a number of phosphoesterases that contain an
#=GF CC   internal duplication.
#=GF MB   PF02834;
#=GF MB   PF08975;
#=GF MB   PF07823;
#=GF MB   PF09749;
#=GF MB   PF06299;
#=GF MB   PF05881;
//

     */

    private static final String RECORD_END = "//";
    private static final String ACCESSION_LINE = "#=GF AC";
    private static final String CLAN_MEMBER_MODEL_AC_LINE = "#=GF MB";
    private static final String NESTING_LINE = "#=GF NE";     // Points to nested (child) model entry.

    private static final Pattern ACCESSION_EXTRACTOR_PATTERN = Pattern.compile("^\\#=GF\\s+[A-Z]{2}\\s+([A-Z0-9]+).*$");

    private String pfamASeedFile;

    private String pfamCFile;

    private PfamClanData clanData;

    private static final Object CLAN_LOCK = new Object();

    @Required
    public void setPfamASeedFile(String pfamASeedFile) {
        this.pfamASeedFile = pfamASeedFile;
    }

    @Required
    public void setPfamCFile(String pfamCFile) {
        this.pfamCFile = pfamCFile;
    }

    /**
     * Lazy-loads and returns the PfamClanData object that contains
     * all the details of Pfam clans and nesting relationships
     * between models.
     *
     * @return the PfamClanData object that contains
     *         all the details of Pfam clans and nesting relationships
     *         between models.
     * @throws IOException in the event of a problem reading the
     *                     clan data file.
     */
    public PfamClanData getClanData() throws IOException {
        if (clanData == null) {
            synchronized (CLAN_LOCK) {
                if (clanData == null) {
                    buildClanAndModelModel();
                }
            }
        }
        return clanData;
    }

    /**
     * Does the job of parsing the file - retrieves the relevant
     * data from each record and stores it into a RecordHolder object.
     * This object is then used to build the proper data model, held
     * in PfamClanData.  The RecordHolder object is then discarded.
     *
     * @throws IOException in the event of a problem reading the file.
     */
    private void buildClanAndModelModel() throws IOException {
        clanData = new PfamClanData();
        parsePfamASeed(); //think of an easier way to get this info as the rest of the alignment info is not used
        parsePfamC();
    }

    private void parsePfamASeed() throws IOException {

        BufferedReader reader = null;
        // Stick the nesting information into a Map for the moment,
        // will be added to the domain model after parsing the file.
        Map<String, List<String>> modelAccessionNestsModelAccession = new HashMap<String, List<String>>();

        //Maybe We shouldnt parse the seed file as it is not being used at the moment

        try {
            reader = new BufferedReader(new FileReader(new File(pfamASeedFile)));
            RecordHolder record = new RecordHolder();
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.startsWith(RECORD_END)) {
                    buildModel(record, modelAccessionNestsModelAccession);
                    record = new RecordHolder();
                } else if (line.startsWith(ACCESSION_LINE)) {
                    Matcher acLineMatcher = ACCESSION_EXTRACTOR_PATTERN.matcher(line);
                    if (acLineMatcher.find()) {
                        record.setModelAc(acLineMatcher.group(1));
                    }
                } else if (line.startsWith(NESTING_LINE)) {
                    Matcher nestingAccessionMatcher = ACCESSION_EXTRACTOR_PATTERN.matcher(line);
                    if (nestingAccessionMatcher.find()) {
                        record.addNestedDomainAc(nestingAccessionMatcher.group(1));
                    } else {
                        throw new IllegalStateException("Line: " + line + " appears to be a nesting line, but does not match the regex to retrieve the accession number.");
                    }
                }
            }
            // Just in case there is no final end of record marker.
            buildModel(record, modelAccessionNestsModelAccession);

            // Last of all, add Nesting information to the model.
            addNestingInformation(modelAccessionNestsModelAccession);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void parsePfamC() throws IOException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(new File(pfamCFile)));
            PfamClan clan = null;
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.startsWith(RECORD_END)) {
                    clan = null;
                } else if (line.startsWith(ACCESSION_LINE)) {
                    Matcher acLineMatcher = ACCESSION_EXTRACTOR_PATTERN.matcher(line);
                    if (acLineMatcher.find()) {
                        clan = new PfamClan(acLineMatcher.group(1));
                    }
                } else if (line.startsWith(CLAN_MEMBER_MODEL_AC_LINE)) {
                    if (clan == null) {
                        throw new IllegalStateException("Found an entry in file " + pfamCFile + " where there appears to be no clan accession.");
                    }
                    Matcher clanAccessionMatcher = ACCESSION_EXTRACTOR_PATTERN.matcher(line);
                    if (clanAccessionMatcher.find()) {
                        String modelAccession = clanAccessionMatcher.group(1);
                        PfamModel model = clanData.getModelByModelAccession(modelAccession);
                        if (model != null) {
                            model.setClan(clan);
                        }

                    } else {
                        throw new IllegalArgumentException("Looks like a nesting line, but can't parse out the accession.  Line = " + line);
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Goes through the Map of nesting to nested model IDs and adds
     * these relationships to the Models stored in the PfamClanData object.
     *
     * @param modelAccessionNestsModelAccession
     *         mapping nesting model IDs to nested model IDs.
     */
    private void addNestingInformation(Map<String, List<String>> modelAccessionNestsModelAccession) {
        for (String nestingModelAc : modelAccessionNestsModelAccession.keySet()) {
            for (String nestedModelAc : modelAccessionNestsModelAccession.get(nestingModelAc)) {
                PfamModel nestingModel = clanData.getModelByModelAccession(nestingModelAc);
                PfamModel nestedModel = clanData.getModelByModelAccession(nestedModelAc);
                if (nestingModel == null) {
                    throw new IllegalStateException("Attempting to update PfamModel with AC " + nestingModelAc + " with nesting information, but cannot find it!");
                }
                if (nestedModel == null) {
                    throw new IllegalStateException("Attempting to update PfamModel with AC " + nestedModelAc + " with nesting information, but cannot find it!");
                }
		        // Move check for circular relationships to PfamModel.isNestedIn()
                //if (nestingModel.isNestedIn(nestedModel)) {
                //    throw new IllegalStateException("Circularity detected in Clan file nesting relationship between model ACs " + nestedModelAc + " and " + nestingModelAc + '.');
                //}

                // Add the nesting relationship.
                nestedModel.addModelThisIsNestedIn(nestingModel);
            }
        }
    }

    /**
     * For a single record parsed from the flat file, builds the required objects and stores
     * them in the PfamClanData object.
     *
     * @param record containing the relevant information from a single record in the flat file.
     * @param modelAccessionNestsModelAccession
     *               mapping nesting model IDs to nested model IDs.
     */
    private void buildModel(RecordHolder record, Map<String, List<String>> modelAccessionNestsModelAccession) {
        if (record.getModelAc() != null) {
            clanData.addModel(record.getModelAc());
            if (record.getNestedDomains().size() > 0) {
                for (String nestedDomain : record.getNestedDomains()) {
                    List<String> nestedDomains = modelAccessionNestsModelAccession.get(record.getModelAc());
                    if (nestedDomains == null) {
                        nestedDomains = new ArrayList<String>();
                    }
                    nestedDomains.add(nestedDomain);
                    modelAccessionNestsModelAccession.put(record.getModelAc(), nestedDomains);
                }
            }
        }
    }

    /**
     * Inner class used to hold the details of a single Pfam-A.seed record.
     * Used for convenience only - the data is then transferred into the more rich data structure
     * provided by the PfamClanData, PfamModel and PfamClan classes.
     */
    private class RecordHolder {
        private String modelAc;
        private final List<String> nestedDomains = new ArrayList<String>();

        public String getModelAc() {
            return modelAc;
        }

        public void setModelAc(String modelAc) {
            this.modelAc = modelAc;
        }

        public List<String> getNestedDomains() {
            return nestedDomains;
        }

        public void addNestedDomainAc(String nestedDomainAc) {
            this.nestedDomains.add(nestedDomainAc);
        }
    }

}
