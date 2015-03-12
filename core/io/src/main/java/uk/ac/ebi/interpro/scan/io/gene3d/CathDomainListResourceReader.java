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
        Scanner scanner = new Scanner(line);
        final String domainName       = scanner.next();
        final int classNumber         = scanner.nextInt();
        final int architectureNumber  = scanner.nextInt();
        final int topologyNumber      = scanner.nextInt();
        final int homologousSuperfamilyNumber = scanner.nextInt();
        return new CathDomainListRecord(domainName, classNumber, architectureNumber,
                                        topologyNumber, homologousSuperfamilyNumber);
    }
    
}