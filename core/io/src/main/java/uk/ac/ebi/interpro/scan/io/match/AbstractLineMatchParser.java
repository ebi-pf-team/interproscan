package uk.ac.ebi.interpro.scan.io.match;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This implementation parses match data, where one record is on a single line.
 * <p/>
 * This is useful for (for example) Prosite & HAMAP GFF output and any other binaries
 * that output TSV, CSV output such as ProDom.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
public abstract class AbstractLineMatchParser<T extends RawMatch> implements MatchParser<T> {

    private static final Logger LOGGER = Logger.getLogger(AbstractLineMatchParser.class.getName());

    private final SignatureLibrary signatureLibrary;
    private final String signatureLibraryRelease;

    private AbstractLineMatchParser() {
        this.signatureLibrary = null;
        this.signatureLibraryRelease = null;
    }

    protected AbstractLineMatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        this.signatureLibrary = signatureLibrary;
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Override
    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    @Override
    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Override public Set<RawProtein<T>> parse(InputStream is) throws IOException {
        if (is == null) {
            throw new NullPointerException("InputStream is null");
        }
        final Map<String, RawProtein<T>> proteins = new HashMap<String, RawProtein<T>>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Line to parse: " + line);
                T match = createMatch(line);
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Resulting match: " + match);
                if (match != null) {
                    final String sequenceIdentifier = match.getSequenceIdentifier();
                    RawProtein<T> protein;
                    if (proteins.containsKey(sequenceIdentifier)) {
                        protein = proteins.get(sequenceIdentifier);
                    } else {
                        protein = new RawProtein<T>(sequenceIdentifier);
                        proteins.put(sequenceIdentifier, protein);
                    }
                    protein.addMatch(match);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return new HashSet<RawProtein<T>>(proteins.values());
    }

    /**
     * Returns {@link uk.ac.ebi.interpro.scan.model.raw.RawMatch} instance using values from parameters.
     *
     * @param line Line read from input file.   @return {@link uk.ac.ebi.interpro.scan.model.raw.RawMatch} instance using values from parameters
     */
    protected abstract T createMatch(String line);

}
