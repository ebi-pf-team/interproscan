package uk.ac.ebi.interpro.scan.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Phil Jones
 *         Date: 24/01/12
 *         Time: 16:27
 *         Comprises the lines for the condensed view and is responsible for
 *         building this structure.
 */
public class CondensedView {

    private final SimpleProtein protein;

    private Set<CondensedLine> lines = new TreeSet<CondensedLine>();

    public CondensedView(SimpleProtein protein) {
        this.protein = protein;
        // First of all, need to build SuperMatches.
        final List<SimpleSuperMatch> superMatches = buildSuperMatchList(protein);

        // Second, need to build "SuperMatchBucket" objects.
        final List<SuperMatchBucket> superMatchBucketList = buildKeepTogetherList(superMatches);

    }

    private List<SimpleSuperMatch> buildSuperMatchList(SimpleProtein protein) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private List<SuperMatchBucket> buildKeepTogetherList(final List<SimpleSuperMatch> superMatches) {
        List<SuperMatchBucket> superMatchBucketList = new ArrayList<SuperMatchBucket>();
        for (SimpleSuperMatch superMatch : superMatches) {
            boolean inList = false;
            for (final SuperMatchBucket bucket : superMatchBucketList) {
                inList = bucket.addIfSameHierarchy(superMatch);
                if (inList) break;
            }
            if (!inList) {
                // Need a new Bucket.
                superMatchBucketList.add(
                        new SuperMatchBucket(superMatch)
                );
            }
        }
        return superMatchBucketList;
    }

    public Set<CondensedLine> getLines() {
        return lines;
    }
}
