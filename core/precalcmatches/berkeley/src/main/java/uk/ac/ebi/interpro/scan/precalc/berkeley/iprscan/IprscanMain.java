package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan.CreateMatchDBFromIprscanBerkeleyDB;

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
        String databaseUrl = args[2];
        String databaseUsername = args[3];
        String databasePassword = args[4];
        String maxUPI = args[5];
        int fetchSize = 100000;
        if (args.length >= 6) {
            fetchSize = Integer.parseInt(args[6]);
        }

        // TODO Allow user to kick of ALL (both MD5 and MATCH) or just one

        /*
        CreateMD5ListFromIprscan md5Builder = new CreateMD5ListFromIprscan();
        md5Builder.buildDatabase(
                md5DBPath,
                databaseUrl,
                databaseUsername,
                databasePassword,
                maxUPI
        );

        CreateMatchDBFromIprscan matchBuilder = new CreateMatchDBFromIprscan();
        matchBuilder.buildDatabase(
                matchDBPath,
                databaseUrl,
                databaseUsername,
                databasePassword,
                maxUPI
        );

        */

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
}
