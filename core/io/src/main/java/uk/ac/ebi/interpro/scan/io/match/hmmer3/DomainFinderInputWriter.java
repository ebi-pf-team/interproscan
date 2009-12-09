package uk.ac.ebi.interpro.scan.io.match.hmmer3;

import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.HmmsearchOutputMethod;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.DomainMatch;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


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

    public DomainFinderInputWriter() {

            this.outputFile = new File("C:\\Manjula\\input_for_df3.txt");
            this.outputFile.delete();  //to clear out contents written previously.
     
    }

    public void writeMethodToFile(HmmsearchOutputMethod method, String seqId, String domainNum) {
        BufferedWriter bw = null;
        if (method!=null) {
            try {
                bw = new BufferedWriter(new FileWriter(outputFile, true));
                //StringBuilder sb = null;
                SequenceMatch sm = method.getSequenceMatches().get(seqId);
                if (sm!=null) {
                    DomainMatch dm = sm.getDomainMatches().get((Integer.parseInt(domainNum))-1); //domain number subtracted with once since in a list first element is 0
                    if (dm!=null) {
                       StringBuilder sb = new StringBuilder();
                        sb.append(sm.getSequenceIdentifier()+"\t");
                        sb.append(method.getMethodAccession() +"\t1000\t");
                        sb.append(method.getMethodAccessionLength()+ "\t" );
                        sb.append(dm.getAliFrom()+"\t" );
                        sb.append(dm.getAliTo() + "\t");
                        sb.append(dm.getHmmfrom() + "\t");
                        sb.append(dm.getHmmto() + "\t");
                        sb.append(dm.getIEvalue()+ "\t");
                        sb.append(dm.getScore() + "\t");
                        sb.append(dm.getScore() + "\t1\t");
                        sb.append(dm.getAliFrom() + ":");
                        sb.append(dm.getAliTo());
                        sb.append("\n");
                        System.out.println(sb.toString());
                        bw.write(sb.toString());
                    }
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
}
