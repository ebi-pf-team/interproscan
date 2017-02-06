package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.io.match.panther.PantherMatchParser;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Write matches as output for InterProScan user.
 *
 * @author David Binns, EMBL-EBI, InterPro
 * @author Phil Jones, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Gift Nuka, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchesTSVProResultWriter extends ProteinMatchesResultWriter {

    private TSVWriter tsvWriter;

    public ProteinMatchesTSVProResultWriter(Path path) throws IOException {
        super(path);
        this.tsvWriter = new TSVWriter(super.fileWriter);
    }


    /**
     * Writes out a Protein object to a TSV file
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws IOException in the event of I/O problem writing out the file.
     */
    public int write(Protein protein) throws IOException {
        int locationCount = 0;
        List<String> proteinAcs = getProteinAccessions(protein);
        int length = protein.getSequenceLength();
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());

        Set<Match> matches = protein.getMatches();

        for (String proteinAc: proteinAcs) {
//            Utilities.verboseLog("sequence mapping: " + proteinAc + " -> " + protein.getId() + "  length: " +  protein.getSequenceLength() ) ;

            for (Match match : matches) {
                final Signature signature = match.getSignature();
                final String signatureAc = signature.getAccession();
                final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
                final String analysis = signatureLibrary.getName();
                final String version = signature.getSignatureLibraryRelease().getVersion();

                final String description = signature.getDescription();

                Set<Location> locations = match.getLocations();
                if (locations != null) {
                    locationCount += locations.size();
                    for (Location location : locations) {
                        //Default score
                        String score = "-";
                        String status = "T";

                        //Default seq score
                        String seqScore = "-";


                        if (match instanceof PantherMatch) {
                            seqScore = Double.toString( ((PantherMatch) match).getScore());
                        } else if (match instanceof FingerPrintsMatch) {
                            seqScore = Double.toString(((FingerPrintsMatch) match).getEvalue());
                        } else if (match instanceof Hmmer3Match) {
                            seqScore = Double.toString( ((Hmmer3Match) match).getScore());
                        } else if (match instanceof Hmmer2Match) {
                            seqScore = Double.toString(((Hmmer2Match) match).getScore());
                        }


                        //get the seqEvalue


                        // get the score


                        //In other cases we have to take the value from the location
                        if (location instanceof HmmerLocation) {
                            score = Double.toString(((HmmerLocation) location).getScore());
                        } else if (location instanceof BlastProDomMatch.BlastProDomLocation) {
                            score = Double.toString( ((BlastProDomMatch.BlastProDomLocation) location).getScore() );
                        }  else if (location instanceof ProfileScanMatch.ProfileScanLocation)  {
                            score = Double.toString( ((ProfileScanMatch.ProfileScanLocation) location).getScore() );
                        } else if (location instanceof RPSBlastMatch.RPSBlastLocation) {
                            score = Double.toString( ((RPSBlastMatch.RPSBlastLocation) location).getScore() );
                        } else if (location instanceof FingerPrintsMatch.FingerPrintsLocation) {
                            score = Double.toString( ((FingerPrintsMatch.FingerPrintsLocation) location).getScore() );
                        } else if (location instanceof ProfileScanMatch.ProfileScanLocation) {
                            score = Double.toString( ((ProfileScanMatch.ProfileScanLocation) location).getScore());
                        } else if (location instanceof TMHMMMatch.TMHMMLocation) {
                            score = Double.toString( ((TMHMMMatch.TMHMMLocation) location).getScore() );
//                        } else if (location instanceof PatternScanMatch.PatternScanLocation) {
//                            score = Double.toString( ((PatternScanMatch.PatternScanLocation) location).getLevel(). );
                        } else if (location instanceof SignalPMatch.SignalPLocation) {
                            score = Double.toString( ((SignalPMatch.SignalPLocation) location).getScore() );
                        }


                        //how do we deal with duplicates, as we dont want duplicates
                        final List<String> mappingFields = new ArrayList<>();
                        mappingFields.add(analysis);
                        mappingFields.add(getReleaseMajorMinor(version)[0]);
                        mappingFields.add(getReleaseMajorMinor(version)[1]);
                        mappingFields.add(proteinAc);
//                        mappingFields.add(md5);
//                        mappingFields.add(Integer.toString(length));
                        mappingFields.add(signatureAc);
                        mappingFields.add(Integer.toString(location.getStart()));
                        mappingFields.add(Integer.toString(location.getEnd()));
                        mappingFields.add(seqScore);

                        this.tsvWriter.write(mappingFields);
                    }
                }
            }
        }
        return locationCount;
    }

    /**
     * get major release and minor release numbers from version
     * @param version
     * @return
     */
    private String[] getReleaseMajorMinor(String version){
        String releaseMajor = version;
        String releaseMinor = "0";
        Pattern pattern = Pattern.compile("\\. *");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            releaseMajor = version.substring(0, matcher.start());
            releaseMinor = version.substring(matcher.end());
        }

        return new String[] {releaseMajor, releaseMinor};
    }
}




