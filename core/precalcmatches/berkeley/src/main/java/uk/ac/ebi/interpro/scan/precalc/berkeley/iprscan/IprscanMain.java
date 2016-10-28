package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import uk.ac.ebi.interpro.scan.util.Utilities;

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
            throw new IllegalArgumentException("Please provide the following arguments:\n\npath to match berkeleyDB directory\npath to MD5 check berkeleyDB directory\n" +databaseName+"DB URL (jdbc:oracle:thin:@host:port:SID)\n"+databaseName+" DB username\n"+databaseName+" DB password\nMaximum UPI");
        }
        String matchDBPath = args[0];
        String md5DBPath = args[1];
        String siteDBPath = args[2];
        String databaseUrl = args[3];
        String databaseUsername = args[4];
        String databasePassword = args[5];
        String dbName = args[6].trim();

        if ( dbName.equals("site") ) {
            System.out.println(Utilities.getTimeNow() + " Create siteDB");
            CreateSiteDBFromIprscan matchBuilder = new CreateSiteDBFromIprscan();
            matchBuilder.buildDatabase(
                    siteDBPath,
                    databaseUrl,
                    databaseUsername,
                    databasePassword
            );
        }

        if ( dbName.equals("md5") ) {
            System.out.println(Utilities.getTimeNow() + " Create md5DB");
            CreateMD5ListFromIprscan md5Builder = new CreateMD5ListFromIprscan();
            md5Builder.buildDatabase(
                    md5DBPath,
                    databaseUrl,
                    databaseUsername,
                    databasePassword
            );


        }
        if ( dbName.equals("match") ) {
            System.out.println(Utilities.getTimeNow() + " Create matchDB");
            CreateMatchDBFromIprscan matchBuilder = new CreateMatchDBFromIprscan();
            matchBuilder.buildDatabase(
                    matchDBPath,
                    siteDBPath,
                    databaseUrl,
                    databaseUsername,
                    databasePassword
            );

        }
        if ( dbName.equals("md5_and_match")  ) {
            System.out.println(Utilities.getTimeNow() + " Create md5DB and matchDB");
            CreateMD5ListFromIprscan md5Builder = new CreateMD5ListFromIprscan();
            md5Builder.buildDatabase(
                    md5DBPath,
                    databaseUrl,
                    databaseUsername,
                    databasePassword
            );

            CreateMatchDBFromIprscan matchBuilder = new CreateMatchDBFromIprscan();
            matchBuilder.buildDatabase(
                    matchDBPath,
                    siteDBPath,
                    databaseUrl,
                    databaseUsername,
                    databasePassword
            );
        }
    }
}
