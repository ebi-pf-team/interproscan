package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

/**
 * Created with IntelliJ IDEA.
 * User: craigm
 * Date: 30/08/13
 * Time: 16:15
 * To change this template use File | Settings | File Templates.
 */
public class UrlFriendlyIdGenerator {

    private String REPLACEMENT_REGEX  = "[/;?:@&=+$,]";

    private static UrlFriendlyIdGenerator instance = null;

    private UrlFriendlyIdGenerator() {}

    public static UrlFriendlyIdGenerator getInstance() {
        if (instance == null) {
            instance = new UrlFriendlyIdGenerator();
        }
        return instance;
    }

    public final String generate(String identifier) {
        return identifier.replaceAll(REPLACEMENT_REGEX, "_");
    }

}
