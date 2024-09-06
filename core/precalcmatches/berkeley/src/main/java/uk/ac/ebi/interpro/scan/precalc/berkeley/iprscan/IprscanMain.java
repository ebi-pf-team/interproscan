package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;
import java.io.File;

public class IprscanMain {
    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Usage: java -jar berkeley-db-builder.jar TYPE DIR URL\n\n" +
                            "TYPE: md5|matches|sites\n" +
                            "DIR:  output directory of the BerkleyDB database\n" +
                            "URL:  Oracle connection URL, i.e. jdbc:oracle:thin:@//<host>:<port>/<service>\n");
        }
        String databaseType = args[0].toLowerCase();
        String databasePath = args[1];
        String databaseUrl = args[2];
        String databasePassword = System.getenv("ORACLE_PASSWORD");

        File outputDir = new File(databasePath);
        if (outputDir.exists()) {
            if (outputDir.isDirectory()) {
                throw new IllegalStateException("Not a directory: " + databasePath);
            }
            File[] files = outputDir.listFiles();
            if (files != null && files.length > 0) {
                throw new IllegalStateException("Not empty: " + databasePath);
            } else if (!outputDir.canWrite()) {
                throw new IllegalStateException("Not writeable: " + databasePath);
            }
        } else if (!outputDir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + databasePath);
        }

        int fetchSize = 100000;
        switch (databaseType) {
            case "md5": {
                CreateMD5ListFromIprscan builder = new CreateMD5ListFromIprscan();
                builder.buildDatabase(databaseUrl, databasePassword, fetchSize, outputDir);
                break;
            }
            case "matches": {
                CreateMatchDBFromIprscanBerkeleyDB builder = new CreateMatchDBFromIprscanBerkeleyDB();
                builder.buildDatabase(databaseUrl, databasePassword, fetchSize, outputDir);
                break;
            }
            case "sites": {
                CreateSiteDBFromIprscanBerkeleyDB builder = new CreateSiteDBFromIprscanBerkeleyDB();
                builder.buildDatabase(databaseUrl, databasePassword, fetchSize, outputDir);
                break;
            }
            default:
                throw new IllegalStateException("Invalid mode: " + args[0] + "\n");
        }
    }
}
