package uk.ac.ebi.interpro.scan.io.match.domainfinder;

import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;

import java.io.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 04-Jan-2010
 * Time: 11:00:16
 * To change this template use File | Settings | File Templates.
 */
public class DomainFinderOutputParser {

     private File inputFile = null;

    public DomainFinderOutputParser(File inFile) {

            this.inputFile = inFile;
            if(this.inputFile!=null)
              this.readGene3dMatchFromDF3OutputFile(); 

    }

    //public void writeMethodToFile(HmmsearchOutputMethod method, String seqId, String domainNum) {
    public void storeDF3OutputToMatch(String[] df3FileLine) {
        Gene3dHmmer3RawMatch m;
        List<Gene3dHmmer3RawMatch> matches=null;

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
                                            100, //envelope start
                                            100, //envelope end
                                             0.0, //expected Accuracy
                                              0.9, // full sequence bias
                                             0.1, //domainCEvalue
                                            0.2, //domainIEvalue
                                            0.3, //domainBias
                                            "unknown string","Domain Finder 3.0");
            System.out.println("Printing Gene3dRawMatch from DF3 output file ....." + m.toString());
            try {
              matches.add(m);
            }catch(NullPointerException e) {
              e.printStackTrace();
            }
        }

    }
    public void readGene3dMatchFromDF3OutputFile() {

        BufferedReader br = null;
        if (this.inputFile!=null) {
            try {
              br = new BufferedReader(new FileReader(inputFile));
              String line;
              while (( line = br.readLine()) != null){
                  
                  String[] tokens = line.split("\\t");
                  if(tokens!=null) {
                      this.storeDF3OutputToMatch(tokens);//store the tokens into list of Gene3DMatches
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
