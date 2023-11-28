package uk.ac.ebi.interpro.scan.management.model.implementations;
import uk.ac.ebi.interpro.scan.model.*;

import java.util.HashSet;
import java.util.Set;

public class Domain {
    private final Location location;
    private final Set<Integer> residues;
    private final boolean pfam;

    public Location getLocation() {
        return location;
    }

    public Set<Integer> getResidues() {
        return residues;
    }

    public boolean isPfam() {
        return pfam;
    }

    public Domain(Location location, boolean isPfam) {
        this.location = location;
        this.residues = new HashSet<>();
        this.pfam = isPfam;

        Set<LocationFragment> fragments = location.getLocationFragments();
        for (LocationFragment fragment: fragments) {
            for (int i = fragment.getStart(); i <= fragment.getEnd(); i++) {
                this.residues.add(i);
            }
        }
    }

    public boolean overlaps(Domain other, double threshold) {
        Set<Integer> overlap = new HashSet<Integer>(this.residues);
        overlap.retainAll(other.getResidues());
        if (overlap.size() > 0) {
            return ((double) overlap.size() / Math.min(this.residues.size(), other.getResidues().size())) >= threshold;
        }

        return false;
    }
}
