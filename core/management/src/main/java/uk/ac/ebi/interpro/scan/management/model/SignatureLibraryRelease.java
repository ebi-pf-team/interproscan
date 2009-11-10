package uk.ac.ebi.interpro.scan.management.model;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SignatureLibraryRelease {

    private Long id;

    private SignatureLibrary library;

    private String releaseNumber;

    protected SignatureLibraryRelease() {
    }

    SignatureLibraryRelease(SignatureLibrary library, String releaseNumber) {
        this.library = library;
        this.releaseNumber = releaseNumber;
    }

    public Long getId() {
        return id;
    }

    public SignatureLibrary getLibrary() {
        return library;
    }

    public String getReleaseNumber() {
        return releaseNumber;
    }
}
