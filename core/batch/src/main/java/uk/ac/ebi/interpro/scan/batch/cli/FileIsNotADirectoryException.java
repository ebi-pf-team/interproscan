package uk.ac.ebi.interpro.scan.batch.cli;

/**
 * Exception to inform calling code that the path provided is not a directory.
 *
 * @author  Phil Jones, EMBL-EBI
 * @version $Id: FileIsNotADirectoryException.java,v 1.2 2009/06/18 10:53:08 aquinn Exp $
 * @since   1.0
 */
public class FileIsNotADirectoryException extends Throwable {

    /**
     * Constructor that requires an explanatory message.
     * @param message explanation of the source of the Exception.
     */
    public FileIsNotADirectoryException(String message) {
        super(message);
    }

}
