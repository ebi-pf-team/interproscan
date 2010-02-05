package uk.ac.ebi.interpro.scan.io;

/**
 * This Exception class is used to indicate that the FILE being parsed contains
 * unexpected content.  It should not be used to trap errors in code, or
 * anything that extends IOException (inclusive).
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseException extends RuntimeException {

    private String fileName;
    private String line;
    private Integer lineNumber;
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param fileName of the file being parsed (OPTIONAL)
     * @param line being the actual line the problem is on (OPTIONAL)
     * @param lineNumber being the number of the line in the file when the problem was detected. (OPTIONAL)
     */
    public ParseException(String message, String fileName, String line, Integer lineNumber) {
        super(message);
        this.fileName = fileName;
        this.line = line;
        this.lineNumber = lineNumber;
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param line being the contents of the failed line.
     */
    public ParseException(String message, String line) {
       this(message, null, line, null);

    }

    public ParseException(String message) {
        this(message, null, null, null);
    }

    public String getFileName() {
        return fileName;
    }

    public String getLine() {
        return line;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }


    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getMessage()).append('\n');
        if (fileName != null){
            buf.append("filename = ").append(fileName).append('\n');
        }
        if (line != null){
            buf.append("Line = ").append(line).append('\n');
        }
        if (lineNumber != null){
            buf.append("Line Number = ").append(lineNumber);
        }
        return buf.toString();
    }
}
