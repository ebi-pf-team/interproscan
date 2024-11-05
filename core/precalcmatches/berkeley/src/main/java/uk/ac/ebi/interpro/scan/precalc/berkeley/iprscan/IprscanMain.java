package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;
import java.io.File;

public class IprscanMain {
    private static String databaseType = null;
    private static String databasePath = null;
    private static String databaseUrl = null;
    private static boolean verbose = false;

    public static void main(String[] args) {
        parseArgs(args);

        if (databaseType == null || databasePath == null || databaseUrl == null) {
            usage();
        }

        String databasePassword = System.getenv("ORACLE_PASSWORD");
        if (databasePassword == null || databasePassword.isEmpty()) {
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
                builder.buildDatabase(databaseUrl, databasePassword, fetchSize, outputDir, verbose);
                break;
            }
            case "sites": {
                CreateSitesDBFromOracle builder = new CreateSitesDBFromOracle();
                builder.buildDatabase(databaseUrl, databasePassword, fetchSize, outputDir, verbose);
                break;
            }
            default: {
                System.err.println("Invalid mode: " + args[0]);
                System.exit(1);
            }
        }
    }

    private static void parseArgs(String[] args) {
        for(int i = 0; i < args.length; ++i) {
            if (args[i].startsWith("-")) {
                switch(args[i].substring(1)) {
                    case "type":
                        databaseType = args[++i];
                        break;
                    case "dir":
                        databasePath = args[++i];
                        break;
                    case "url":
                        databaseUrl = args[++i];
                        break;
                    case "verbose":
                        verbose = true;
                        break;
                    default:
                        usage();
                }
            } else {
                usage();
            }
        }
    }

    private static void usage() {
        System.out.println("Usage: java -jar berkeley-db-builder.jar -type TYPE -dir PATH -url URL");
        System.out.println("  -type TYPE: type of database to build (md5, matches, sites)");
        System.out.println("  -dir PATH : output directory of the Berkeley DB");
        System.out.println("  -url URL  : Oracle connection URL, i.e. jdbc:oracle:thin:@//<host>:<port>/<service>");
        System.out.println("  -verbose  : increase frequency of progress messages");
        System.exit(1);
    }
}
