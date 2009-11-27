package uk.ac.ebi.interpro.scan.io.match;

import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Set;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.IOException;

/**
 * Parser for HMMER2 and HMMER3 output.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class HmmerMatchParser implements MatchParser {

    // Sequence section of output, for example "Query sequence: UPI0000000030"
    private static final String KEY_SEQUENCE  = "Query sequence: ";

    private static final String ROW_NO_HITS = "[no hits above thresholds]";
    private static final String EOF         = "//";

    // TODO: Use same regexs for hmmsearch and hmmpfam
    
    // Match sequence lines ("?" in ".+?" means non-greedy match -- only works if score is "#.#"), for example:
    // PF01391.9 Collagen triple helix repeat (20 copies)       242.2    1.1e-69   7
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("^(\\S+)\\s+(.+?)\\s+(\\d+\\.\\d+)\\s+(\\S+)");

    // Match domain lines (only works if score is "#.#"), for example:
    // PF00092.19   1/2      21   162 ..    42   195 .]    69.1  1.7e-21
    private static final Pattern DOMAIN_PATTERN   =
            Pattern.compile("^(\\S+)\\s+\\S+\\s+(\\d+)\\s+(\\d+)\\s+\\S+\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\d+\\.\\d+)\\s+(\\S+)");

    /**
     * Parse HMMER2 or HMMER3 output. 
     *
     * @param   is  Output from HMMER2 or HMMER3.
     * @return  Protein(s) with associated matches.
     * @throws  IOException if problems reading input stream.
     */
    // TODO: Throw unchecked exceptions as recommended by Spring Framework
    public Set<RawProtein> parse(InputStream is) throws IOException {
        Scanner scanner = null;
        try {
            scanner = new Scanner(is);
            // Get HMMER binary name, for example "hmmpfam - search one or more sequences against HMM database"
            String binary   = scanner.nextLine();
            // Get HMMER version, for example "HMMER 2.3.2 (Oct 2003)"
            String version  = scanner.nextLine();
            boolean isHmmer2 = (version.startsWith("HMMER 2.3.2"));
            if (binary.startsWith("hmmpfam"))  {
//                return parseHmmPfam(scanner);
                return null;
            }
            else if (binary.startsWith("hmmsearch"))  {
                //return parseHmmSearch(scanner);
                return null;
            }
            else    {
                throw new IllegalStateException("Unrecognised binary: " + binary);
            }
        }
        finally	{
            if (scanner != null)	{
                scanner.close();
            }
        }
    }

    /**
     * Parse hmmpfam output (one protein sequence, one or more HMMs)
     *
     * @param   scanner
     * @return  Protein(s) with associated matches.
     */
    // TODO: Consider storing other parts of output:
    /*
    hmmpfam - search one or more sequences against HMM database
    HMMER 2.3.2 (Oct 2003)    
    ...
    HMM file:                 /ebi/sp/pro1/interpro/data/members/pfam/22/Pfam_fs.bin
    Sequence file:            data/5k.fasta
    ...
    Accession:      [none]
    Description:    [none]
    */
/*    private Set<Protein> parseHmmPfam(Scanner scanner)    {
        Set<Protein> seqIds = new LinkedHashSet<Protein>();
        SequenceIdentifier sequenceIdentifier = null;
        while (scanner.hasNextLine())	{
            String line = scanner.nextLine();
            if (line.startsWith(KEY_SEQUENCE))	{
                // Get protein ID from "Query sequence:" line
                String id = line.split(KEY_SEQUENCE)[1];
                sequenceIdentifier = SequenceIdentifier.Factory.createSequenceIdentifier(id);
            }
            else if (line.equals(EOF)) {
                // We've readched the end of the output, so add this protein to the collection
                seqIds.add(sequenceIdentifier);
                sequenceIdentifier = null;
                // TODO: Should we make defensive copy of protein before adding to collection?
                // ... then we can set protein to null so data not overwritten if we miss out a "Query sequence" line
            }
            else {
                // Look for domain line
                Matcher domainMatcher = DOMAIN_PATTERN.matcher(line);
                if (domainMatcher.find())  {
                    String modelAccession = domainMatcher.group(1);
                    int start      = Integer.parseInt(domainMatcher.group(2));
                    int end        = Integer.parseInt(domainMatcher.group(3));
                    int hmmStart   = Integer.parseInt(domainMatcher.group(4));
                    int hmmEnd     = Integer.parseInt(domainMatcher.group(5));
                    HmmLocation.HmmBounds hmmBounds = HmmLocation.HmmBounds.parseSymbol(domainMatcher.group(6));
                    double score    = Double.parseDouble(domainMatcher.group(7));
                    double evalue   = Double.parseDouble(domainMatcher.group(8));
                    // TODO: Find match for given modelAccession, add locations and bounds
                    HmmLocation location =
                            new HmmLocation(start, end, score, evalue, hmmStart, hmmEnd, hmmBounds);
                    checkSequenceIdentifier(sequenceIdentifier);
                }
                else    {
                    // Look for sequence line (comes after domain matcher because sequence regex is less strict)
                    Matcher sequenceMatcher = SEQUENCE_PATTERN.matcher(line);
                    if (sequenceMatcher.find())  {
                        // Get model data
                        String modelAccession = sequenceMatcher.group(1);
                        Model model           = new Model(modelAccession, null, sequenceMatcher.group(2));
                        // Get match data
                        double score    = Double.parseDouble(sequenceMatcher.group(3));
                        double evalue   = Double.parseDouble(sequenceMatcher.group(4));
                        // Add match
                        checkSequenceIdentifier(sequenceIdentifier);
                        sequenceIdentifier.addRawMatch(new RawHmmMatch(model, score, evalue));
                    }
                    else    {
                        // We're not interested in anything else
                    }
                }
            }
        }
        return seqIds;
    }*/

    /**
     * Check we've created sequenceIdentifier ("Query sequence" line may be missing)
     *
     * @param protein
     * @throws NullPointerException if sequenceIdentifier is null
     */
    private void checkSequenceIdentifier(Protein protein)  {
        if (protein == null) {
            throw new NullPointerException("'protein' - is '" + KEY_SEQUENCE + "' missing from record?");
        }
    }

    
    /*
    Accession:   TIGR00002

    ...

    Scores for complete sequences (score includes all domains):
    Sequence      Description                               Score    E-value  N
    --------      -----------                               -----    ------- ---
    UPI000000200E                                           123.2    8.1e-34   1
    UPI00000014B1                                            84.4      4e-22   1

    Parsed for domains:
    Sequence      Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
    --------      ------- ----- -----    ----- -----      -----  -------
    UPI000000200E   1/1       2    77 ..     1    81 []   123.2  8.1e-34
    UPI00000014B1   1/1      18    98 ..     1    81 []    84.4    4e-22
    */

    /**
     * Parse hmmsearch output (one or more protein sequences, one HMM)
     *
     * @param scanner
     * @return
     */
    /*
    private Set<Protein> parseHmmSearch(Scanner scanner)    {
        // Sections in HMMER output file
        final String KEY_ACCESSION           = "Accession:";
        final String SECTION_SEQUENCE_SCORES = "Scores for complete sequences (score includes all domains):";
        final String SECTION_DOMAIN_SCORES   = "Parsed for domains:";
        final String COLUMN_SEQUENCE         = "Sequence";
        final String COLUMN_UNDERLINE        = "---";
        Set<Protein> proteins = new LinkedHashSet<Protein>();
        Model model = null;
        boolean isSequenceScores = false;
        boolean isDomainScores   = false;
        while (scanner.hasNextLine())	{
            String line = scanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            if (line.startsWith(KEY_ACCESSION))	{
                lineScanner.next();     // Skip "Accession:"
                String modelAccession = lineScanner.next();
                model = new Model(modelAccession);
            }
            if (line.startsWith(SECTION_SEQUENCE_SCORES)) {
                isSequenceScores = true;
            }
            else if (line.startsWith(SECTION_DOMAIN_SCORES))  {
                isDomainScores   = true;
            }
            if (isSequenceScores || isDomainScores)	{
                if (!(line.startsWith(COLUMN_SEQUENCE)  ||
                      line.startsWith(COLUMN_UNDERLINE) ||
                      line.startsWith(SECTION_SEQUENCE_SCORES)  ||
                      line.startsWith(SECTION_DOMAIN_SCORES)))    {
                    Scanner s = new Scanner(line);
                    if (line.contains(ROW_NO_HITS))	{
                        break;  // No results, so stop
                    }
                    else    {
                        if (s.hasNext())    {
                            String proteinAccession = s.next();
                            if (isSequenceScores)   {
                                double score  = s.nextDouble();
                                double evalue = s.nextDouble();
                                Protein protein = new Protein(proteinAccession);
                                proteins.add(protein);
                                protein.addMatch(new HmmMatch(model, score, evalue));
                            }
                            else    {
                                // Find protein
                                Protein protein = null;
                                for (Protein p : proteins)  {
                                    if (p.getAccession().equals(proteinAccession))  {
                                        protein = p;
                                        break;
                                    }
                                }
                                // Get match
                                HmmMatch match = (HmmMatch)protein.getMatches().iterator().next();
                                // Add location
                                s.next();   // Ignore "Domain" column
                                int start = s.nextInt();
                                int end   = s.nextInt();
                                s.next(); // TODO: what's this? (".." for example)
                                int hmmStart = s.nextInt();
                                int hmmEnd   = s.nextInt();
                                HmmMatch.HmmBounds hmmBounds = HmmMatch.HmmBounds.parseSymbol(s.next());
                                double score  = s.nextDouble();
                                // Following give same answer, slightly different from table
                                // TODO: run same calculation in Java
                                double evalue = (Math.log(s.nextDouble())/Math.log(10.0));
                                //m.domainevalue = Consts.log10(s.nextDouble());
                                //m.domainevalue = Consts.log10(Double.parseDouble(s.next()));
                                match.addLocation(new HmmMatch.HmmLocation(start, end, hmmStart, hmmEnd, hmmBounds, evalue, score));
                            }
                        }
                        else    {
                            if (isSequenceScores)   {
                                isSequenceScores = false;   // We've finished parsing sequence scores
                            }
                            else    {
                                break;                      // We've finished parsing domain scores, so stop
                            }
                        }
                    }
                }
            }
        }
        return proteins;
    }
  */  

}
