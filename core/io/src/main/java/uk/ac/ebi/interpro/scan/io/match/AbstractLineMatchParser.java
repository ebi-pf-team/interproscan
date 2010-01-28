package uk.ac.ebi.interpro.scan.io.match;

import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This implementation parses match data on a line-by-line basis, and can be used to parse,
 * for example, ProDom and Phobius results.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
abstract class AbstractLineMatchParser<T extends RawMatch> implements MatchParser<T> {

    private final String signatureLibraryName;
    private final String signatureLibraryRelease;

    private AbstractLineMatchParser() {
        this.signatureLibraryName    = null;
        this.signatureLibraryRelease = null;
    }

    protected AbstractLineMatchParser(String signatureLibraryName, String signatureLibraryRelease) {
        this.signatureLibraryName    = signatureLibraryName;
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Override public String getSignatureLibraryName() {
        return signatureLibraryName;
    }

    @Override public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Override public Set<RawProtein<T>> parse(InputStream is) throws IOException {
        if (is == null) {
            throw new NullPointerException("InputStream is null");
        }
        Map<String, RawProtein<T>> proteins = new HashMap<String, RawProtein<T>>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(is));
            while (reader.ready()) {
                T match = createMatch(signatureLibraryName, signatureLibraryRelease, reader.readLine());
                if (match != null)  {
                    String id = match.getSequenceIdentifier();
                    RawProtein<T> protein;
                    if (proteins.containsKey(id))   {
                        protein = proteins.get(id);
                    }
                    else    {
                        protein = new RawProtein<T>(id);
                        proteins.put(id, new RawProtein<T>(id));
                    }
                    protein.addMatch(match);
                }
            }
        }
        finally {
            if (reader != null){
                reader.close();
            }
        }
        return new HashSet<RawProtein<T>>(proteins.values());
    }

    /**
     * Returns {@link uk.ac.ebi.interpro.scan.model.raw.RawMatch} instance using values from parameters.
     *
     * @param signatureLibraryName      Corresponds to {@link uk.ac.ebi.interpro.scan.model.SignatureLibrary#getName()}
     * @param signatureLibraryRelease   Corresponds to {@link uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease#getVersion()}
     * @param line                      Line read from input file.
     * @return {@link uk.ac.ebi.interpro.scan.model.raw.RawMatch} instance using values from parameters
     */
    protected abstract T createMatch(String signatureLibraryName,
                                     String signatureLibraryRelease,
                                     String line);

}