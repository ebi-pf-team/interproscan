package uk.ac.ebi.interpro.scan.management.model;

import java.util.List;
import java.util.ArrayList;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SignatureLibrary {

    private Long id;

    private String name;

    private List<SignatureLibraryRelease> releases = new ArrayList<SignatureLibraryRelease>();

    protected SignatureLibrary(){}

    public SignatureLibrary(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SignatureLibraryRelease createNewRelease(String releaseNumber){
        SignatureLibraryRelease release = new SignatureLibraryRelease(this, releaseNumber);
        releases.add (release);
        return release;
    }

    public List<SignatureLibraryRelease> getReleases() {
        return releases;
    }
}
