package uk.ac.ebi.interpro.scan.io.match.domainfinder;

import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 04-Jan-2010
 * Time: 11:00:16
 * To change this template use File | Settings | File Templates.
 */
public class DomainFinderOutputParser {

     private File inputFile = null;

    //DUMMY values for certain arguments, since they are not available for raw matches at this
    // stage of parsing DF3 output.
     private static final int DUMMY_ENVELOPE_START =100;
    private static final int DUMMY_ENVELOPE_END =100;
    private static final double DUMMY_EXPECTED_ACCURACY=0.0;
    private static final double DUMMY_FULL_SEQUENCE_BIAS=0.9;
    private static final double DUMMY_DOMAIN_CEVALUE=0.1; //domainCEvalue
    private static final double DUMMY_DOMAIN_IEVALUE=0.2; //domainIEvalue
    private static final double DUMMY_DOMAIN_BIAS=0.3; //domainBias

    public DomainFinderOutputParser(File inFile) {

            this.inputFile = inFile;
            if(this.inputFile!=null)
              this.readGene3dMatchFromDF3OutputFile(); 

    }

    public List<Gene3dHmmer3RawMatch> storeDF3OutputToMatch(String[] df3FileLine, List<Gene3dHmmer3RawMatch> matches) {
        Gene3dHmmer3RawMatch m;
        //List<Gene3dHmmer3RawMatch> matches=null;
         //First 5 fields in the DF3 output file has important field for
        //raw/post-processed matches. Hence the condition > 5
        //this can be changed later depending on the requirements
        if (df3FileLine!=null && df3FileLine.length > 5) {
            //TODO store DF3 file output into post-processed Gene3D match collection
            m = new Gene3dHmmer3RawMatch(df3FileLine[0],df3FileLine[1],"Gene3D","3.3",Integer.parseInt(df3FileLine[4]),//locationtart
                                          Integer.parseInt(df3FileLine[5]),   //locationend
                                          Double.parseDouble(df3FileLine[8]),//evalue
                                          Double.parseDouble(df3FileLine[9]),   //score
                                          Integer.parseInt(df3FileLine[6]),  //hmmstart
                                          Integer.parseInt(df3FileLine[7]),  //hmmend
                                                  "hmm bounds",
                                          Double.parseDouble(df3FileLine[9]),   //locationScore
                                            DUMMY_ENVELOPE_START, //envelope start     
                                            DUMMY_ENVELOPE_END, //envelope end
                                            DUMMY_EXPECTED_ACCURACY, //expected Accuracy
                                            DUMMY_FULL_SEQUENCE_BIAS, // full sequence bias
                                            DUMMY_DOMAIN_CEVALUE, //domainCEvalue
                                            DUMMY_DOMAIN_IEVALUE, //domainIEvalue
                                            DUMMY_DOMAIN_BIAS, //domainBias
                                            "unknown string","Domain Finder 3.0");
            System.out.println("Printing Gene3dRawMatch from DF3 output file ....." + m.toString());
            try {
                matches.add(m);

            }catch(NullPointerException e) {
              e.printStackTrace();
            }
        }

        return matches;

    }
    public void readGene3dMatchFromDF3OutputFile() {

        BufferedReader br = null;
        List<Gene3dHmmer3RawMatch> matches= new ArrayList<Gene3dHmmer3RawMatch>();
        List<Gene3dHmmer3RawMatch> retmatches;
        if (this.inputFile!=null) {
            try {
              br = new BufferedReader(new FileReader(inputFile));
              String line;
              while (( line = br.readLine()) != null){
                  String[] tokens = line.split("\\t");
                  if(tokens!=null) {
                      retmatches = this.storeDF3OutputToMatch(tokens,matches);//store the tokens into list of Gene3DMatches
                  }
              }// end of while reading file lines

            }catch(IOException ioe) {
                System.out.println("File Reading error for Domain Finder Output Parser " + ioe.getMessage() );
            } finally {
                if (br!=null){
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.getMessage();
                    }
                }
        }
        }
    }


}
