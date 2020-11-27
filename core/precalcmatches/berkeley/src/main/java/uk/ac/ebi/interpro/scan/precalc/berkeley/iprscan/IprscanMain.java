package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan.CreateMatchDBFromIprscanBerkeleyDB;

import java.util.Arrays;

/**
 * @author Phil Jones
 *         Date: 20/05/11
 *         Time: 11:59
 *         Hacky bit of code that runs the BekerleyDB building
 *         mechanism from the command line.
 */
public class IprscanMain {

    private static final String databaseName="Iprscan";

    public static void main(String[] args) {
        if (args.length < 6) {
            throw new IllegalArgumentException("Please provide the following arguments:\n\npath to match berkeleyDB directory\npath to MD5 check berkeleyDB directory\n"
                    + databaseName + "DB URL (jdbc:oracle:thin:@host:port:SID)\n"
                    + databaseName + " DB username\n"
                    + databaseName + " DB password\nMaximum UPI");
        }
        String matchDBPath = args[0];
        String md5DBPath = args[1];
        String siteDBPath = args[2];
        String databaseUrl = args[3];
        String databaseUsername = args[4];
        String databasePassword = args[5];
        String maxUPI = args[6];
        String buildsteps = args[7];

        System.out.println("args  #" + args.length + " : " + Arrays.toString(args));
        int fetchSize = 100000;
        if (args.length >= 9) {
            fetchSize = Integer.parseInt(args[7]);
        }

        // TODO Allow user to kick of ALL (both MD5 and MATCH) or just one



        // we have now this built

        //md5

        
        if ( buildsteps.contains("1")) {
        CreateMD5ListFromIprscan md5Builder = new CreateMD5ListFromIprscan();
        md5Builder.buildDatabase(
                md5DBPath,
                databaseUrl,
                databaseUsername,
                databasePassword,
                maxUPI,
                fetchSize
        );

	}
       
	if ( buildsteps.contains("2")) {

	    //matches
        CreateMatchDBFromIprscanBerkeleyDB matchBuilder = new CreateMatchDBFromIprscanBerkeleyDB();
        matchBuilder.buildDatabase(
                matchDBPath,
                databaseUrl,
                databaseUsername,
                databasePassword,
                maxUPI,
                fetchSize
        );

        }

	if ( buildsteps.contains("3")) {

        //sites

        CreateSiteDBFromIprscanBerkeleyDB siteMatchBuilder = new CreateSiteDBFromIprscanBerkeleyDB();
        siteMatchBuilder.buildDatabase(
                siteDBPath,
                databaseUrl,
                databaseUsername,
                databasePassword,
                maxUPI,
                fetchSize
        );

	}

    }
}
