package uk.ac.ebi.interpro.scan.io.match.prosite;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.raw.PfScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses the GFF output format from ps_scan.pl
 * <p/>
 * the GFF format (version 2) is specified here: http://www.sanger.ac.uk/resources/software/gff/spec.html
 * <p/>
 * Column description (general GFF) summarised from the page above.:
 * <p/>
 * seqname: The id of the protein sequence.
 * source: The source of this feature: in this case the software / version number.
 * feature: The HAMAP or Prosite accession number
 * start: on the protein sequence
 * end: on the protein sequence
 * score: A floating point value. When there is no score
 * strand: NOT USED hence '.'
 * frame: NOT USED hence '.'
 * attributes: key value structure with semicolon separators. keys must be standard identifiers ([A-Za-z][A-Za-z0-9_]*).
 * Free text values must be quoted with double quotes.
 * Note: Non-printing chars ANSI C representation (eg newlines as '\n', tabs as '\t').
 * Multiple values can follow a specific key.
 * <p/>
 * For specific member databases, attributes are:
 * <p/>
 * HAMAP:             Name, Level, RawScore, FeatureFrom, FeatureTo, Sequence, SequenceDescription
 * Prosite Profiles:  Name, Level, RawScore, FeatureFrom, FeatureTo, Sequence, SequenceDescription, KnownFalsePos
 * Prosite Patterns:  Name, LevelTag, Sequence, SequenceDescription, KnownFalsePos
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PrositeMatchParser<T extends PfScanRawMatch> {

    private static final Logger LOGGER = Logger.getLogger(PrositeMatchParser.class.getName());

    public Set<RawProtein<T>> parse(InputStream is, String fileName, String signatureReleaseLibrary) throws IOException {
        Set<RawProtein<T>> rawMatches = new HashSet<RawProtein<T>>();

        return rawMatches;
    }
}
