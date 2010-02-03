package uk.ac.ebi.interpro.scan.io.gene3d;

import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;

import java.util.Scanner;

/**
 * TODO: Add class description
 *
 * http://www.cathdb.info/wiki/data:cathdomainlist
 *
 * http://release.cathdb.info/v3.3.0/CathDomainList
 *
 *   1 	CATH domain name (seven characters)
 *   2 	Class number
 *   3 	Architecture number
 *   4 	Topology number
 *   5 	Homologous superfamily number
 *   6 	S35 sequence cluster number
 *   7 	S60 sequence cluster number
 *   8 	S95 sequence cluster number
 *   9 	S100 sequence cluster number
 *   10 	S100 sequence count number
 *   11 	Domain length
 *   12 	Structure resolution (Angstroms)
 *   (999.000 for NMR structures and 1000.000 for obsolete PDB entries)
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class CathDomainListResourceReader extends AbstractResourceReader<CathDomainListRecord> {

    @Override
    protected CathDomainListRecord createRecord(String line) {
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