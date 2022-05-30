package uk.ac.ebi.interpro.scan.io.panther;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

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
 * @author Matthias Blum
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
            if (modelFile.exists()) {
                File file = new File(modelFile.getFile() + "/" + this.getNamesTabFileStr());
                Map<String, String> id2name = parseNames(file);
                Map<String, List<String>> parent2children = new HashMap<>();

                for (String id: id2name.keySet()) {
                    String parentId;
                    boolean isSubFamily;

                    if (id.contains(":SF")) {
                        parentId = id.split(":")[0];
                        isSubFamily = true;
                    } else {
                        parentId = id;
                        isSubFamily = false;
                    }

                    List<String> children = parent2children.get(parentId);
                    if (children == null ){
                        children = new ArrayList<>();
                    }

                    if (isSubFamily) {
                        children.add(id);
                    }

                    parent2children.put(parentId, children);
                }

                for (String parentId: parent2children.keySet()) {
                    Set<Model> models = new HashSet<>();
                    models.add(new Model(parentId, id2name.get(parentId), null));

                    List<String> children = parent2children.get(parentId);
                    for (String childId: children) {
                        models.add(new Model(childId, id2name.get(childId), null));
                    }

                    release.addSignature(new Signature(parentId, id2name.get(parentId), null, null, null, release, models));
                }
            }
        }
        return release;
    }

    /**
     * Parses signature accessions and names out of the specified tab separated file.
     *
     * @param file Tab separated file with 2 columns.
     * @return A map of signature accessions and names.
     */
    private Map<String, String> parseNames(File file) throws IOException {
        Map<String, String> result = new HashMap<>();

        try (FileInputStream is = new FileInputStream(file);
             InputStreamReader sr = new InputStreamReader(is);
             BufferedReader br =  new BufferedReader(sr)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("PTHR")) {
                    String[] columns = line.split("\t", 2);  //so that we have at most two strings
                    String familyId = columns[0].trim();
                    familyId = familyId.replace(".mod", "");
                    if (familyId.contains(".mag")) {
                        // familyId is a family
                        familyId = familyId.replace(".mag", "");
                    } else {
                        // familyId is a subfamily
                        familyId = familyId.replace(".", ":");
                    }

                    String familyName = "";
                    if (columns.length == 2) {
                        //we also have a family name
                        familyName = columns[1].trim();
                    }

                    result.put(familyId, familyName);
                }
            }
        }

        return result;
    }
}
