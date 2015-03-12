package uk.ac.ebi.interpro.scan.io.panther;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to parse PANTHER model directory, so that Signatures / Models can be loaded into I5.
 * <p/>
 * The directory is structured like this:
 * <p/>
 * ../model/books
 * ../model/books/PTHR10000/
 * ../model/books/PTHR10000/SF0
 * ../model/books/PTHR10003/
 * ../model/books/PTHR10003/SF10
 * ../model/books/PTHR10003/SF11
 * ../model/books/PTHR10003/SF27
 * ../model/books/PTHR10004/SF0
 * etc.
 * ../model/globals/
 * <p/>
 * So each model signature / Panther family has its own directory and even the sub families have their own directory,
 * which contains the HMMER model..
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PantherModelDirectoryParser extends AbstractModelFileParser {

    private static final Logger LOGGER = Logger.getLogger(PantherModelDirectoryParser.class.getName());

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

                File booksDir = new File(modelFile.getFile().getPath() + "/books");
                if (booksDir.exists() && booksDir.getAbsoluteFile() != null) {
                    //TODO: Implement a file filter for a more save memory implementation
                    String[] children = booksDir.getAbsoluteFile().list();
                    if (children != null) {
                        for (String signatureAcc : children) {
                            String signatureName = familyIdFamilyNameMap.get(signatureAcc);
                            //Create super family signatures
                            release.addSignature(createSignature(signatureAcc, signatureName, release));
                            //Create sub family signatures
                            createSubFamilySignatures(signatureAcc, familyIdFamilyNameMap, release);
                        }
                    } else {
                        LOGGER.debug("Either dir does not exist or is not a directory.");
                    }
                }
            }
        }
        return release;
    }

    /**
     * Creates sub family signatures.
     *
     * @throws IOException
     */
    private void createSubFamilySignatures(String dirName, Map<String, String> familyIdFamilyNameMap,
                                           SignatureLibraryRelease release) throws IOException {
        for (Resource modelFile : modelFiles) {
            File subFamilyDir = new File(modelFile.getFile().getPath() + "/books/" + dirName);
            if (subFamilyDir.exists() && subFamilyDir.getAbsoluteFile() != null) {
                //TODO: Implement a file filter for a more memory save implementation
                String[] children = subFamilyDir.getAbsoluteFile().list(new DirectoryFilenameFilter());
                if (children != null) {
                    for (String signatureAcc : children) {
                        signatureAcc = dirName + ":" + signatureAcc;
                        String signatureName = familyIdFamilyNameMap.get(signatureAcc);
                        //Create super family signatures
                        release.addSignature(createSignature(signatureAcc, signatureName, release));
                    }
                } else {
                    LOGGER.debug("Either dir does not exist or is not a directory.");
                }
            }
        }
    }

    /**
     * This filter only returns directories
     */
    class DirectoryFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.startsWith("SF");
        }
    }

    /**
     * Handles parsing process of the specified file resource.
     *
     * @param modelFile Tab separated file resource with 2 columns (headers: accession, names).
     * @return A map of signature accessions and names.
     * @throws IOException
     */
    private Map<String, String> readInPantherFamilyNames(Resource modelFile) throws IOException {
        Map<String, String> result = null;
        File globalsDir = new File(modelFile.getFile().getPath() + "/globals");
        if (globalsDir.exists()) {
            File namesTabFile = new File(globalsDir.getPath() + "/" + namesTabFileStr);
            result = parseTabFile(namesTabFile);
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
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(namesTabFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                lineNumber++;
                if (line.length() > 0 && line.startsWith("PTHR")) {
                    String[] columns = line.split("\t");
                    if (columns != null && columns.length == 2) {
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
                        String familyName = columns[1];
                        result.put(familyId, familyName);
                    } else {
                        LOGGER.warn("Columns is Null OR unexpected splitting of line. Line is splitted into " +
                                (columns == null ? 0 : columns.length) + "columns!");
                    }
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
        LOGGER.info(lineNumber + " lines parsed.");
        LOGGER.info(result.size() + " entries created in the map.");
        return result;
    }

    /**
     * Creates and returns an instance of signature.
     */
    private Signature createSignature(String accession, String name, SignatureLibraryRelease release) {
        Model model = new Model(accession, name, null);
        return new Signature(accession, name, null, null, null, release, Collections.singleton(model));
    }
}
