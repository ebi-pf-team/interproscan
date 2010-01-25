package uk.ac.ebi.interpro.scan.io.match.domainfinder;

import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 08-Dec-2009
 * Time: 13:49:11
 * To change this template use File | Settings | File Templates.
 *  This class generates a ssf file which is the input file for Domain Finder. The Domain
 *  Finder parses this ssf file and generates the post processed result to be stored in Intepro table
 *  for Gene3D 3.3 member database.
 */
public class DomainFinderInputWriter {

    private File outputFile = null;

    public DomainFinderInputWriter(File ssfFileName) {
         if (ssfFileName!=null) {
           this.outputFile = ssfFileName;
         } else {
             System.out.println("DomainFinderInputWriter is not given with inputfilename!!!");
         }

    }


    public void writeMatchToFile(Gene3dHmmer3RawMatch rawMatch, BufferedWriter bw) throws IOException {

        if (rawMatch!=null) {

            StringBuilder sb = new StringBuilder();
            sb.append(rawMatch.getSequenceIdentifier()).append("\t");
            sb.append(rawMatch.getModel()).append("\t");
            sb.append("1000\t"); //seqIdentifier length  //TODO: length of input sequence, that matches the hmm model.
            sb.append("178"+ "\t" );  //model length  //TODO : length of input hmm model
            sb.append(rawMatch.getLocationStart()).append("\t");
            sb.append(rawMatch.getLocationEnd()).append("\t");
            sb.append(rawMatch.getHmmStart()).append("\t");
            sb.append(rawMatch.getHmmEnd()).append("\t");
            sb.append(Math.pow(10,rawMatch.getDomainIeValue())).append("\t");
            sb.append(rawMatch.getLocationScore()).append("\t");
            sb.append(rawMatch.getLocationScore()).append("\t");
            String s = rawMatch.getCigarAlignment();
            String[] segments = this.getSegmentAndBoundaries(s,rawMatch.getLocationStart()).split(",");
            if (segments!=null && segments.length==2) {
                sb.append(segments[0]).append("\t"); //number of segments
                sb.append(segments[1]); // segment boundary
            }

            sb.append("\n");
            bw.write(sb.toString());



        }

    }
    public void writeGene3dRawMatchToSsfFile(List<Gene3dHmmer3RawMatch> matches) {
        BufferedWriter bw = null;
        if (matches!=null) {
            try {
              bw = new BufferedWriter(new FileWriter(outputFile));
               for (Gene3dHmmer3RawMatch m : matches) {
                      this.writeMatchToFile(m,bw);
               }
            }catch(IOException ioe) {
                System.out.println("File Writing error for Domain Finder" + ioe.getMessage() );
            } finally {
                if (bw!=null){
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
        }
        }
    }


    //documentation for segment calcuation in cigaraligment format and from hmmsearch file.
    /*
    * The algorithm for splitting a domain into segments is this:

    *(1) Get the string of the sequence as aligned to the model (i.e. 'AIHNMPGRAAGDMAFRTGAamassdrvtitlrgvgghgamphfardpmsaagsiivalqtivareidaqqsavitvgsvqagetfnvipetvvmklsvralnadvrallaqriealakgqaqsfgieadvqydygYPVLV'

    *(2) Step through the sequence residue-by-residue

    *(3) If a lower case letter is reached open a gap; if an upper case letter is reached close a gap. '.'s and '-'s are ignored (i.e. they don't open or close gaps and are ignored when calculating the gap length).

    *(4) At the point of closing a gap, if the gap was >= 30 residues then the sequence is split into two segments around it. So the end of the first segment is the last residue before the gap opens, while the beginning of the second is the first after the gap closes.

    *(5) Continue on down the sequence ...

     */

    public String getSegmentAndBoundaries(String cigarAlignment, int aliFrom) {

        int residueLength=aliFrom;
        int startOfMatch=0,endOfMatch=0;
        int segmentCounter=1;
        int insertCounter=0;
        int residueCounter;
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();



        for (int i=0; i<cigarAlignment.length(); i++) {

            char c = cigarAlignment.charAt(i);

             if(Character.isUpperCase(c)) {
                 residueCounter=Integer.parseInt(sb1.toString());
                 sb1.append(c);
                 sb1.setLength(0);
                 residueLength+=residueCounter;
                 //System.out.println("residue length ::: " + residueLength);
                 switch(c) {
                     case 'M':
                          endOfMatch = residueLength;
                          insertCounter=0;
                          if(startOfMatch==0)
                             startOfMatch =residueLength-residueCounter;  //to have a good start of match always
                          break;

                     case 'I':
                           insertCounter+=residueCounter;  //this is to handle two insert segment followed by each other
                           if (insertCounter >=30 && endOfMatch > startOfMatch ) {
                               sb.append(startOfMatch).append(":").append(endOfMatch - 1).append(":");
                               startOfMatch=residueLength;
                               segmentCounter++;
                           }
                           break;

                     case 'D':
                         residueLength-=residueCounter;
                         break;
                 }
             } else {
                 sb1.append(c);
             } //end of if

        } //end of for
        if( endOfMatch > startOfMatch )
            sb.append(startOfMatch).append(":").append(endOfMatch - 1).append(":");  //not to missout any trailing segment

        String s = segmentCounter + "," +  sb.toString().substring(0,sb.toString().length()-1);
        //System.out.println("Given alignment String has " + segmentCounter + " segments!" );
        //System.out.println("Segment boundaries " + sb.toString().substring(0,sb.toString().length()-1));
        return s;
    }
}


//////////////////////////////////////////////////
/*package uk.ac.ebi.interpro.scan.io.match.domainfinder;

import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 08-Dec-2009
 * Time: 13:49:11
 * To change this template use File | Settings | File Templates.
 *  This class generates a ssf file which is the input file for Domain Finder. The Domain
 *  Finder parses this ssf file and generates the post processed result to be stored in Intepro table
 *  for Gene3D 3.3 member database.

public class DomainFinderInputWriter {

    private File outputFile = null;

    public DomainFinderInputWriter(File ssfFileName) {
         if (ssfFileName!=null) {
           this.outputFile = ssfFileName;
         } else {
             System.out.println("DomainFinderInputWriter is not given with inputfilename!!!");
         }
     
    }

    
    public void writeMatchToFile(Gene3dHmmer3RawMatch rawMatch, BufferedWriter bw) throws IOException {

        if (rawMatch!=null) {

            StringBuilder sb = new StringBuilder();
            sb.append(rawMatch.getSequenceIdentifier()).append("\t");
            sb.append(rawMatch.getModel()).append("\t");
            sb.append("1000\t"); //seqIdentifier length  //TODO: length of input sequence, that matches the hmm model.
            sb.append("178"+ "\t" );  //model length  //TODO : length of input hmm model
            sb.append(rawMatch.getLocationStart()).append("\t");
            sb.append(rawMatch.getLocationEnd()).append("\t");
            sb.append(rawMatch.getHmmStart()).append("\t");
            sb.append(rawMatch.getHmmEnd()).append("\t");
            sb.append(Math.pow(10,rawMatch.getDomainIeValue())).append("\t");
            sb.append(rawMatch.getLocationScore()).append("\t");
            sb.append(rawMatch.getLocationScore()).append("\t");
            String s = rawMatch.getCigarAlignment();
            String[] segments = this.getSegmentAndBoundaries(s,rawMatch.getLocationStart()).split(",");
            if (segments!=null && segments.length==2) {
                sb.append(segments[0]).append("\t"); //number of segments
                sb.append(segments[1]); // segment boundary
            }

            sb.append("\n");
            //System.out.println(sb.toString());
            bw.write(sb.toString());



        }
        
    }
    public void writeGene3dRawMatchToSsfFile(List<Gene3dHmmer3RawMatch> matches) {
        BufferedWriter bw = null;
        if (matches!=null) {
            try {
              bw = new BufferedWriter(new FileWriter(outputFile));
               for (Gene3dHmmer3RawMatch m : matches) {
                      this.writeMatchToFile(m,bw);
               }
            }catch(IOException ioe) {
                System.out.println("File Writing error for Domain Finder" + ioe.getMessage() );
            } finally {
                if (bw!=null){
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
        }
        }
    }   */


    //documentation for segment calcuation in cigaraligment format and from hmmsearch file.
    /*
    * The algorithm for splitting a domain into segments is this:

    *(1) Get the string of the sequence as aligned to the model (i.e. 'AIHNMPGRAAGDMAFRTGAamassdrvtitlrgvgghgamphfardpmsaagsiivalqtivareidaqqsavitvgsvqagetfnvipetvvmklsvralnadvrallaqriealakgqaqsfgieadvqydygYPVLV'

    *(2) Step through the sequence residue-by-residue

    *(3) If a lower case letter is reached open a gap; if an upper case letter is reached close a gap. '.'s and '-'s are ignored (i.e. they don't open or close gaps and are ignored when calculating the gap length).

    *(4) At the point of closing a gap, if the gap was >= 30 residues then the sequence is split into two segments around it. So the end of the first segment is the last residue before the gap opens, while the beginning of the second is the first after the gap closes.

    *(5) Continue on down the sequence ...



    public String getSegmentAndBoundaries(String cigarAlignment, int aliFrom) {

        int residueLength=aliFrom;
        int startOfMatch=0,endOfMatch=0;
        int segmentCounter=1;
        int insertCounter=0;
        int residueCounter;
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();



        for (int i=0; i<cigarAlignment.length(); i++) {

            char c = cigarAlignment.charAt(i);

             if(Character.isUpperCase(c)) {
                 residueCounter=Integer.parseInt(sb1.toString());
                 sb1.append(c);
                 //System.out.println(sb1.toString());
                 sb1.setLength(0);
                 residueLength+=residueCounter;
                 //System.out.println("residue length ::: " + residueLength);
                 switch(c) {
                     case 'M':
                          endOfMatch = residueLength;
                          insertCounter=0;
                          if(startOfMatch==0)
                             startOfMatch =residueLength-residueCounter;  //to have a good start of match always
                          break;

                     case 'I':
                           insertCounter+=residueCounter;  //this is to handle two insert segment followed by each other
                           if (insertCounter >=30 && endOfMatch > startOfMatch ) {
                               sb.append(startOfMatch).append(":").append(endOfMatch - 1).append(":");
                               startOfMatch=residueLength;
                               segmentCounter++;
                           }
                           break;

                     case 'D':
                         residueLength-=residueCounter;
                         break;
                 }
             } else {
                 sb1.append(c);
             } //end of if

        } //end of for
        if( endOfMatch > startOfMatch )
            sb.append(startOfMatch).append(":").append(endOfMatch - 1).append(":");  //not to missout any trailing segment

        String s = segmentCounter + "," +  sb.toString().substring(0,sb.toString().length()-1);
        //System.out.println("Given alignment String has " + segmentCounter + " segments!" );
        //System.out.println("Segment boundaries " + sb.toString().substring(0,sb.toString().length()-1));
        return s;
    }
} */
///////////////////////////////////////////////////////////////