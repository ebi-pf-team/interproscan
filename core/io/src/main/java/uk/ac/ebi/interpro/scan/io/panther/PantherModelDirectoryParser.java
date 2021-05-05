package uk.ac.ebi.interpro.scan.io.panther;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.util.*;

/**
 * Class to parse PANTHER model directory, so that Signatures / Models can be loaded into I5.
 * <p/>
 * The models are loaded from names.tab
 * <p/>
 *
 * @author Maxim Scheremetjew
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PantherModelDirectoryParser extends AbstractModelFileParser {

    private static final Logger LOGGER = LogManager.getLogger(PantherModelDirectoryParser.class.getName());

    private String namesTabFileStr;

    @Required
    public void setNamesTabFile(String namesTabFile) {
        this.namesTabFileStr = namesTabFile;
    }

    public String getNamesTabFileStr() {
        return namesTabFileStr;
    }

    /**
     * Method to parse a model file and return a SignatureLibraryRelease.
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Override
    public SignatureLibraryRelease parse() throws IOException {
        LOGGER.debug("Starting to parse hmm file.");
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);

        for (Resource modelFile : modelFiles) {
            if (modelFile.exists() && modelFile.getFile() != null) {
                Map<String, String> familyIdFamilyNameMap = readInPantherFamilyNames(modelFile);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("number of panther families: " + familyIdFamilyNameMap.keySet().size());
                }
                Map<String, List<String>> pantherParentChildMap = getPantherParentChildMap(familyIdFamilyNameMap.keySet());
                for (String parent : pantherParentChildMap.keySet()) {
                    String signatureAcc = parent;
                    String signatureName = familyIdFamilyNameMap.get(signatureAcc);
                    release.addSignature(createSignature(signatureAcc, signatureName, release));
                    List<String> children = pantherParentChildMap.get(parent);
                    for (String childSignatureAcc : children) {
                        String childSignatureName = familyIdFamilyNameMap.get(childSignatureAcc);
                        release.addSignature(createSignature(childSignatureAcc, childSignatureName, release));
                    }
                }
            }
        }
        return release;
    }

    /**
     * get the subfamilies and map them to their parents
     * @param pantherFamilyNames
     * @return
     */
    private Map<String, List<String>> getPantherParentChildMap(Set<String> pantherFamilyNames){
        Map<String, List<String>> parentChildFamilyMap = new HashMap<>();

        for (String family: pantherFamilyNames) {
            String parent = getparentFamilyId(family);
            List<String> parentChildren = parentChildFamilyMap.get(parent);
            if (parentChildren == null ){
                parentChildren = new ArrayList<>();
            }
            if (family.contains(":SF")) {
                // add this child to the parent
                parentChildren.add(family);
            }
            parentChildFamilyMap.put(parent, parentChildren);
        }
        return parentChildFamilyMap;
    }

    /**
     * get parent Family ID from the familyId
     * @param familyId
     * @return
     */
    private String getparentFamilyId(String familyId){
        String parent = null;
        if (! familyId.contains(":SF")) {
            //this is a parent
            parent = familyId;
        }else {
            //this is a child
            parent = familyId.split(":")[0];
        }
        return parent;
    }

    /**
     * Handles parsing process of the specified file resource.
     *
     * param namesTabPath Tab separated file resource with 2 columns (headers: accession, names).
     * @return A map of signature accessions and names.
     * @throws IOException
     */
    private Map<String, String> readInPantherFamilyNames(Resource modelFile) throws IOException {
        Map<String, String> result = null;
        try {
            String namesTabPath = modelFile.getFile().getPath() + "/" + namesTabFileStr;
            File namesTabFile = new File(namesTabPath);
            if (namesTabFile.exists()) {
                result = parseTabFile(namesTabFile);
            } else {
                LOGGER.error("names tab file not found");
                throw new IOException("names tab file not found");
            }
            LOGGER.debug(namesTabPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("names tab file not found ...");
        }

        return result;
    }

    /**
     * Parses signature accessions and names out of the specified tab separated file.
     *
     * @param namesTabFile Tab separated file with 2 columns.
     * @return A map of signature accessions and names.
     */
    private Map<String, String> parseTabFile(File namesTabFile) {
        Map<String, String> result = new HashMap<String, String>();

        BufferedReader reader = null;
        int lineNumber = 0;
        int pantherFamilies = 0;
//        System.out.println("names tab is: " + namesTabFile.getAbsolutePath());
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(namesTabFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                lineNumber++;
                if (line.length() > 0 && line.startsWith("PTHR")) {
                    String[] columns = line.split("\t", 2);  //so that we have at most two strings
                    String familyId = columns[0];
                    familyId = familyId.replace(".mod", "");
                    //family Id is a super family
                    if (familyId.contains(".mag")) {
                        familyId = familyId.replace(".mag", "");
                    }
                    //family Id is a sub family
                    else {
                        familyId = familyId.replace(".", ":");
                    }

                    String familyName = "";
                    if (columns.length == 2) {  //we also have a family name
                        familyName = columns[1];
                    } else {  //we don't have a family name
                        LOGGER.warn("Columns is Null OR unexpected splitting of line. Line is splitted into " + columns.length + " columns!" + "columns [0]: " + columns[0]);
                    }
                    pantherFamilies ++;
                    result.put(familyId, familyName);
                    Utilities.verboseLog(1100, "familyId: " +familyId + " familyName: " + familyName);
                } else {
                    LOGGER.warn("Unexpected start of line: " + line);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Couldn't parse tab separated file with family names!", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.warn("Couldn't close buffered reader correctly!", e);
                }
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(lineNumber + " lines parsed.");
            LOGGER.info(result.size() + " entries created in the map.");
        }

        Utilities.verboseLog(1100, "pantherFamilies #: " + pantherFamilies);
        return result;
    }

    /**
     * Creates and returns an instance of signature.
     */
    private Signature createSignature(String accession, String name, SignatureLibraryRelease release) {
        Model model = new Model(accession, name, null); //TODO Also populate hmmLength from the panther.hmm now? Maybe for PANTHER 13.0+
        return new Signature(accession, name, null, null, null, release, Collections.singleton(model));
    }
}
