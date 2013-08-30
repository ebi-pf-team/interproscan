package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

/**
 * Takes a protein identifier and returns a string with any characters that would be unsuitable for a url eg '?'
 * replaced by underscores
 * Required for the html and svg writers as these use the identifier as the filename
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
