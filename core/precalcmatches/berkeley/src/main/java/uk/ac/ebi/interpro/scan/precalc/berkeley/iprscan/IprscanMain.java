package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;
import java.io.File;

public class IprscanMain {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java -jar berkeley-db-builder.jar TYPE DIR URL\n\n" +
                    "TYPE: md5|matches|sites\n" +
                    "DIR:  output directory of the BerkleyDB database\n" +
                    "URL:  Oracle connection URL, i.e. jdbc:oracle:thin:@//<host>:<port>/<service>");
            System.exit(1);
        }
        String databaseType = args[0].toLowerCase();
        String databasePath = args[1];
        String databaseUrl = args[2];
        String databasePassword = System.getenv("ORACLE_PASSWORD");

        if (databasePassword == null || databasePassword.length() == 0) {
            System.err.println("ORACLE_PASSWORD environment variable not set");
            System.exit(1);
        }

        File outputDir = new File(databasePath);
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                System.err.println("Not a directory: " + databasePath);
                System.exit(1);
            }
            File[] files = outputDir.listFiles();
            if (files != null && files.length > 0) {
                System.err.println("Not empty: " + databasePath);
                System.exit(1);
            } else if (!outputDir.canWrite()) {
                System.err.println("Not writeable: " + databasePath);
                System.exit(1);
            }
        } else if (!outputDir.mkdirs()) {
            System.err.println("Cannot create " + databasePath);
            System.exit(1);
        }

        int fetchSize = 100000;
        switch (databaseType) {
            case "md5": {
                CreateMD5DBFromOracle builder = new CreateMD5DBFromOracle();
                builder.buildDatabase(databaseUrl, databasePassword, fetchSize, outputDir);
                break;
            }
            case "matches": {
                CreateMatchesDBFromOracle builder = new CreateMatchesDBFromOracle();
                builder.buildDatabase(databaseUrl, databasePassword, fetchSize, outputDir);
                break;
            }
            case "sites": {
                CreateSitesDBFromOracle builder = new CreateSitesDBFromOracle();
                builder.buildDatabase(databaseUrl, databasePassword, fetchSize, outputDir);
                break;
            }
            default: {
                System.err.println("Invalid mode: " + args[0]);
                System.exit(1);
            }
        }
    }
}
