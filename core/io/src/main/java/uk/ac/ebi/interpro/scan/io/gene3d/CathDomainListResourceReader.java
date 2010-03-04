package uk.ac.ebi.interpro.scan.io.gene3d;

import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;

import java.util.Scanner;

/**
 * Reader for
 * <a href="http://www.cathdb.info/wiki/data:cathdomainlist">CATH List File (CLF)</a>.
 * See {@see CathDomainListRecord}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class CathDomainListResourceReader extends AbstractResourceReader<CathDomainListRecord> {

    @Override protected CathDomainListRecord createRecord(String line) {
        if (line.startsWith("#"))   {
            return null;
        }
        final String domainName;
        final int classNumber;
        final int architectureNumber;
        final int topologyNumber;
        final int homologousSuperfamilyNumber;        
        Scanner scanner = new Scanner(line);
        domainName          = scanner.next();
        classNumber         = scanner.nextInt();
        architectureNumber  = scanner.nextInt();
        topologyNumber      = scanner.nextInt();
        homologousSuperfamilyNumber = scanner.nextInt();          
        return new CathDomainListRecord(domainName, classNumber, architectureNumber,
                                        topologyNumber, homologousSuperfamilyNumber);
    }
    
}