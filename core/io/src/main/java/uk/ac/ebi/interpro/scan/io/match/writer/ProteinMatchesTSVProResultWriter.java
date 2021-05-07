package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.io.match.panther.PantherMatchParser;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.IOException;
import java.nio.DoubleBuffer;
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
        List<String> proteinAcs = getProteinAccessions(protein, proteinSequence);
        int length = protein.getSequenceLength();
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());

        Set<Match> matches = protein.getMatches();

        for (String proteinAc: proteinAcs) {
//            Utilities.verboseLog(1100, "sequence mapping: " + proteinAc + " -> " + protein.getId() + "  length: " +  protein.getSequenceLength() ) ;

            for (Match match : matches) {
                final Signature signature = match.getSignature();
                final String signatureAc = signature.getAccession();
                final String signatureModels = match.getSignatureModels();
                final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
                final String analysis = signatureLibrary.getName();
                final String version = signature.getSignatureLibraryRelease().getVersion();

                final String description = signature.getDescription();


                Set<Location> locations = match.getLocations();
                if (locations != null) {
                    locationCount += locations.size();
                    for (Location location : locations) {
                        final List<String> mappingFields = new ArrayList<>();
                        mappingFields.add(analysis);
                        mappingFields.add(getReleaseMajorMinor(version)[0]);
                        mappingFields.add(getReleaseMajorMinor(version)[1]);
                        mappingFields.add(proteinAc);
                        mappingFields.add(signatureAc);
                        mappingFields.add(signatureModels);
                        mappingFields.add(Integer.toString(location.getStart()));
                        mappingFields.add(Integer.toString(location.getEnd()));

                        //deal with fragments
                        Set<LocationFragment> locationFragments = location.getLocationFragments();
                        StringBuilder listOfLocationFragments = new StringBuilder();
                        String prefix = "";
                        for (LocationFragment locationFragment: locationFragments){
                            listOfLocationFragments.append(prefix);
                            listOfLocationFragments.append(getDomainRegion(locationFragment));
                            prefix = ",";
                        }
                        mappingFields.add(listOfLocationFragments.toString());

                        //Default seq score
                        String seqScore = getSeqScore(match,location);
                        if(seqScore != null) {
                            mappingFields.add(seqScore);
                        }
                        //get the seqEvalue if present
                        String seqEvalue = getSeqEvalue(match,location);
                        if(seqEvalue != null) {
                            mappingFields.add(seqEvalue);
                        }
                        if (match instanceof FingerPrintsMatch) {
                            FingerPrintsMatch.FingerPrintsLocation printsLocation = (FingerPrintsMatch.FingerPrintsLocation) location;
                            mappingFields.add(Integer.toString(printsLocation.getMotifNumber()));
                            mappingFields.add(Double.toString(printsLocation.getPvalue()));
                            mappingFields.add(((FingerPrintsMatch) match).getGraphscan());
                        } else if (location instanceof ProfileScanMatch.ProfileScanLocation) {
                            // Location score for Prosite Profiles/HAMAP already taken care of by getSeqScore method
                            // mappingFields.add (Double.toString( ((ProfileScanMatch.ProfileScanLocation) location).getScore()));
                            mappingFields.add (((ProfileScanMatch.ProfileScanLocation) location).getCigarAlignment());
                        } else if (location instanceof PatternScanMatch.PatternScanLocation) {
                            mappingFields.add(((PatternScanMatch.PatternScanLocation) location).getLevel().getTagNumber());
                            mappingFields.add (((PatternScanMatch.PatternScanLocation) location).getCigarAlignment());
                        } else if (location instanceof PantherMatch.PantherLocation) {
                            PantherMatch.PantherLocation pantherLocation = (PantherMatch.PantherLocation) location;
                            mappingFields.add(pantherLocation.getHmmBounds().getSymbol());
                            mappingFields.add(Integer.toString(pantherLocation.getHmmStart()));
                            mappingFields.add(Integer.toString(pantherLocation.getHmmEnd()));
                            mappingFields.add(Integer.toString(pantherLocation.getHmmLength()));
                            mappingFields.add(Integer.toString(pantherLocation.getEnvelopeStart()));
                            mappingFields.add(Integer.toString(pantherLocation.getEnvelopeEnd()));
                        } else if (location instanceof SuperFamilyHmmer3Match.SuperFamilyHmmer3Location) {
                            mappingFields.add(Integer.toString(((SuperFamilyHmmer3Match.SuperFamilyHmmer3Location) location).getHmmLength()));
                        } else if (location instanceof MobiDBMatch.MobiDBLocation){
                            mappingFields.add(((MobiDBMatch.MobiDBLocation) location).getSequenceFeature());
                        }

                        if (location instanceof Hmmer3Match.Hmmer3Location) {
                            Hmmer3Match.Hmmer3Location hmmer3Location = (Hmmer3Match.Hmmer3Location) location;
                            mappingFields.add(hmmer3Location.getHmmBounds().getSymbol());
                            mappingFields.add(Integer.toString(hmmer3Location.getHmmStart()));
                            mappingFields.add(Integer.toString(hmmer3Location.getHmmEnd()));
                            mappingFields.add(Integer.toString(hmmer3Location.getHmmLength()));
                            mappingFields.add(Integer.toString(hmmer3Location.getEnvelopeStart()));
                            mappingFields.add(Integer.toString(hmmer3Location.getEnvelopeEnd()));
                            mappingFields.add(Double.toString(hmmer3Location.getScore()));
                            mappingFields.add(Double.toString(hmmer3Location.getEvalue()));
                        }
                        if (location instanceof Hmmer2Match.Hmmer2Location) {
                            Hmmer2Match.Hmmer2Location hmmerLocation = (Hmmer2Match.Hmmer2Location) location;
                            mappingFields.add(hmmerLocation.getHmmBounds().getSymbol());
                            mappingFields.add(Integer.toString(hmmerLocation.getHmmStart()));
                            mappingFields.add(Integer.toString(hmmerLocation.getHmmEnd()));
                            mappingFields.add(Integer.toString(hmmerLocation.getHmmLength()));
                            mappingFields.add(Double.toString(hmmerLocation.getScore()));
                            mappingFields.add(Double.toString(hmmerLocation.getEvalue()));
                        }
                        if (location instanceof Hmmer3MatchWithSites.Hmmer3LocationWithSites) {
                            Hmmer3MatchWithSites.Hmmer3LocationWithSites hmmer3LocationWithSite = (Hmmer3MatchWithSites.Hmmer3LocationWithSites) location;
                            mappingFields.add(hmmer3LocationWithSite.getHmmBounds().getSymbol());
                            mappingFields.add(Integer.toString(hmmer3LocationWithSite.getHmmStart()));
                            mappingFields.add(Integer.toString(hmmer3LocationWithSite.getHmmEnd()));
                            mappingFields.add(Integer.toString(hmmer3LocationWithSite.getHmmLength()));
                            mappingFields.add(Integer.toString(hmmer3LocationWithSite.getEnvelopeStart()));
                            mappingFields.add(Integer.toString(hmmer3LocationWithSite.getEnvelopeEnd()));
                            mappingFields.add(Double.toString(hmmer3LocationWithSite.getScore()));
                            mappingFields.add(Double.toString(hmmer3LocationWithSite.getEvalue()));
                        }


//                        Set<LocationFragment> locationFragments = location.getLocationFragments();
//                        StringBuilder listOfLocationFragments = new StringBuilder();
//                        String prefix = "";
//                        for (LocationFragment locationFragment: locationFragments){
//                            listOfLocationFragments.append(prefix);
//                            listOfLocationFragments.append(getDomainRegion(locationFragment));
//                            prefix = ",";
//                        }
//                        mappingFields.add(listOfLocationFragments.toString());
                        //add the Panther annotations details
                        if (match instanceof PantherMatch) {
                            PantherMatch pantherMatch = (PantherMatch) match;
                            mappingFields.add(pantherMatch.getAnnotationsNodeId());
                            //disable anot as they can be processed from the data file and would just use up space in the db for each protein
                            //mappingFields.add(pantherMatch.getAnnotations());
                        }

                        this.tsvWriter.write(mappingFields);
                    }
                }
            }
        }
        return locationCount;
    }

    private String getDomainRegion(LocationFragment locationFragment){
        String domainRegion = locationFragment.getStart() + "-" + locationFragment.getEnd() + "-" + locationFragment.getDcStatus().getSymbol();
        return domainRegion;
    }


    private String getSeqScore(Match match, Location location){
        String seqScore = null;

        if (match instanceof PantherMatch) {
            seqScore = Double.toString( ((PantherMatch) match).getScore());
        }  else if (match instanceof Hmmer3Match) {
            seqScore = Double.toString( ((Hmmer3Match) match).getScore());
        } else if (match instanceof Hmmer2Match) {
            seqScore = Double.toString(((Hmmer2Match) match).getScore());
        } else if (match instanceof Hmmer3MatchWithSites) {
            seqScore = Double.toString(((Hmmer3MatchWithSites) match).getScore());
        } else if (location instanceof RPSBlastMatch.RPSBlastLocation) {
            seqScore = Double.toString(((RPSBlastMatch.RPSBlastLocation) location).getScore());
        } else  if (location instanceof BlastProDomMatch.BlastProDomLocation) {
            seqScore = Double.toString(((BlastProDomMatch.BlastProDomLocation) location).getScore());
        } else if (location instanceof FingerPrintsMatch.FingerPrintsLocation) {
            seqScore = Double.toString( ((FingerPrintsMatch.FingerPrintsLocation) location).getScore());
        } else if (location instanceof ProfileScanMatch.ProfileScanLocation)  {
            seqScore = Double.toString( ((ProfileScanMatch.ProfileScanLocation) location).getScore());
        } else if (location instanceof TMHMMMatch.TMHMMLocation) {
            seqScore = Double.toString( ((TMHMMMatch.TMHMMLocation) location).getScore());
        } else if (location instanceof SignalPMatch.SignalPLocation) {
            seqScore = Double.toString( ((SignalPMatch.SignalPLocation) location).getScore());
        }

        return seqScore;
    }

    private String getSeqEvalue(Match match, Location location) {
        //get the seevalue
        String seqEvalue = null;
        if (match instanceof PantherMatch) {
            seqEvalue = Double.toString(((PantherMatch) match).getEvalue());
        } else if (match instanceof Hmmer3Match) {
            seqEvalue = Double.toString(((Hmmer3Match) match).getEvalue());
        } else if (match instanceof Hmmer2Match) {
            seqEvalue = Double.toString(((Hmmer2Match) match).getEvalue());
        } else if (match instanceof Hmmer3MatchWithSites) {
            seqEvalue = Double.toString(((Hmmer3MatchWithSites) match).getEvalue());
        } else if (location instanceof RPSBlastMatch.RPSBlastLocation) {
            seqEvalue = Double.toString(((RPSBlastMatch.RPSBlastLocation) location).getEvalue());
        } else  if (location instanceof BlastProDomMatch.BlastProDomLocation) {
            seqEvalue = Double.toString(((BlastProDomMatch.BlastProDomLocation) location).getEvalue());
        } else if (match instanceof FingerPrintsMatch) {
            seqEvalue = Double.toString(((FingerPrintsMatch) match).getEvalue());
        } else if (match instanceof SuperFamilyHmmer3Match) {
            seqEvalue = Double.toString(((SuperFamilyHmmer3Match) match).getEvalue());
        }

        return seqEvalue;
    }
    private String getScore(Location location){
        // get the score
        String score = null;

        //In other cases we have to take the value from the location
        if (location instanceof HmmerLocation) {
            score = Double.toString(((HmmerLocation) location).getScore());
        }
        return score;
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




