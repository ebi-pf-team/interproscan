package uk.ac.ebi.interpro.scan.io;

/**
 * Simple util to check the operating system the JVM runs on.
 *
 * @author Maxim Scheremetjew
 */
public class OsUtils {

    private static final boolean operatingSystemIsMacOsX;

    private static final boolean operatingSystemIsLinux;

    static {
        String os = System.getProperty("os.name");
        if (os != null) {
            os = os.toLowerCase();
        }
        operatingSystemIsMacOsX = "mac os x".equals(os);
        operatingSystemIsLinux = os != null && os.indexOf("linux") != -1;
    }

    public static boolean isMacOSX() {
        return operatingSystemIsMacOsX;
    }

    public static boolean isLinux() {
        return operatingSystemIsLinux;
    }
}
