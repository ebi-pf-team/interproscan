package uk.ac.ebi.interpro.scan.io.cli;

import java.io.IOException;

/**
 * Exception to inform calling code that the path provided is not a directory.
 *
 * @author  Phil Jones, EMBL-EBI
 * @version $Id: FileIsNotADirectoryException.java 19 2009-07-16 14:39:29Z aquinn.ebi $
 * @since   1.0
 */
public class FileIsNotADirectoryException extends IOException {

    /**
     * Constructor that requires an explanatory message.
     * @param message explanation of the source of the Exception.
     */
    public FileIsNotADirectoryException(String message) {
        super(message);
    }

}
