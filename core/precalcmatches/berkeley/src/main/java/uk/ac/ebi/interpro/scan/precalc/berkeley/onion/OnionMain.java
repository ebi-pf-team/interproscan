package uk.ac.ebi.interpro.scan.precalc.berkeley.onion;

/**
 * @author Phil Jones
 *         Date: 20/05/11
 *         Time: 11:59
 *         Hacky bit of code that runs the BekerleyDB building
 *         mechanism from the command line.
 */
public class OnionMain {

    public static void main(String[] args) {
        if (args.length < 6) {
            throw new IllegalArgumentException("Please provide the following arguments:\n\npath to match berkeleyDB directory\npath to MD5 check berkeleyDB directory\nOnion DB URL (jdbc:oracle:thin:@host:port:SID)\nOnion DB username\nOnion DB password\nMaximum UPI");
        }
        String matchDBPath = args[0];
        String md5DBPath = args[1];
        String onionDBUrl = args[2];
        String onionUsername = args[3];
        String onionPassword = args[4];
        String maxUPI = args[5];

        CreateMD5ListFromOnion md5Builder = new CreateMD5ListFromOnion();
        md5Builder.buildDatabase(
                md5DBPath,
                onionDBUrl,
                onionUsername,
                onionPassword,
                maxUPI
        );

        CreateMatchDBFromOnion matchBuilder = new CreateMatchDBFromOnion();
        matchBuilder.buildDatabase(
                matchDBPath,
                onionDBUrl,
                onionUsername,
                onionPassword,
                maxUPI
        );
    }
}
