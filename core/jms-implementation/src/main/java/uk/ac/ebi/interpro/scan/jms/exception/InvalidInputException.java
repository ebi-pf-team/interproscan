package uk.ac.ebi.interpro.scan.jms.exception;

import java.util.Set;

/**
 * Invalid user input into InterProScan.
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(Set<String> messages) {
        this(flattenMessages(messages));
    }

    private static String flattenMessages(Set<String> messages) {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message).append("\n");
        }
        return sb.toString();
    }

}
