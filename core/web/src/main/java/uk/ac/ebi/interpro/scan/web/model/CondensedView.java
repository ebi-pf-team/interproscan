package uk.ac.ebi.interpro.scan.web.model;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.web.io.svg.ScaleMarkerUtil;
import uk.ac.ebi.interpro.scan.web.io.svg.ScaledLocationUtil;

import java.io.Serializable;
import java.util.*;

/**
 * @author Phil Jones
 *         Date: 24/01/12
 *         Time: 16:27
 *         Comprises the lines for the condensed view and is responsible for
 *         building this structure.
 */
public class CondensedView implements Serializable {

    private static final Logger LOG = Logger.getLogger(CondensedView.class.getName());

    private List<SimpleEntry> entries;
    private int proteinLength = 0;

    private static final List<EntryType> INCLUDED_TYPES = Arrays.asList(EntryType.DOMAIN, EntryType.REPEAT);

    // The CondensedLines in this Set are ordered by their lineNumber,
    // 0 indexed.
    private Set<CondensedLine> lines;

    private int numSuperMatchBlobs = 0;

    public CondensedView(final List<SimpleEntry> entries, int proteinLength) {
        this.entries = entries;
        this.proteinLength = proteinLength;

        // First of all, need to build SuperMatches.
        final List<SimpleSuperMatch> superMatches = buildSuperMatchList();

        prepareBuckets(superMatches);
    }

    public CondensedView(int proteinLength, List<SimpleSuperMatch> superMatches) {
        this.proteinLength = proteinLength;
        if (superMatches != null && superMatches.size() > 0) {
            prepareBuckets(superMatches);
        }
    }

    private void prepareBuckets(List<SimpleSuperMatch> superMatches) {
        // Second, need to build "SuperMatchBucket" objects.  This process also merges
        // matches to entries in the same hierarchy.
        final List<SuperMatchBucket> buckets = buildBuckets(superMatches);

        // Finally, add the buckets to the lines, aiming for the least number of lines possible.
        buildLines(buckets);
    }

    /**
     * Very dumb method - just makes "SimpleSuperMatch" objects out of SimpleEntry
     * objects - however at this point they are not Supermatches - that is the job
     * of the next method (buildBuckets).
     * <p/>
     * Note - only includes features of the types allowed in the INCLUDED_TYPES list.
     *
     * @return a List of SimpleSuperMatch objects, one for each Entry / location.
     */
    private List<SimpleSuperMatch> buildSuperMatchList() {
        final List<SimpleSuperMatch> superMatchList = new ArrayList<SimpleSuperMatch>();
        // Initially the SimpleSuperMatches are just matches - the merging occurs in the next method call.
        for (final SimpleEntry entry : entries) {
            if (INCLUDED_TYPES.contains(entry.getType())) {
                for (SimpleSignature simpleSignature : entry.getSignatures()) {
                    for (final SimpleLocation location : simpleSignature.getLocations()) {
                        superMatchList.add(new SimpleSuperMatch(entry, location));
                    }
                }
            }
        }

        return superMatchList;
    }

    /**
     * Iterates over the supermatches and merges / buckets them according to their
     * relationships in the hierarchy.
     *
     * @param superMatches to be merged & bucketed.
     * @return a List of SuperMatchBuckets.
     */
    public List<SuperMatchBucket> buildBuckets(final List<SimpleSuperMatch> superMatches) {
        if (superMatches == null) {
            return null;
        }
        List<SuperMatchBucket> superMatchBucketList = new ArrayList<SuperMatchBucket>();
        for (SimpleSuperMatch superMatch : superMatches) {
            boolean inList = false;
            for (final SuperMatchBucket bucket : superMatchBucketList) {
                // addIfSameHierarchyMergeIfOverlap also merges matches into supermatches.
                inList = bucket.addIfSameHierarchyMergeIfOverlap(superMatch);
                if (inList) break; // Will be only one bucket per hierarchy, so no need to go further.
            }
            if (!inList) {
                // Need a new Bucket.
                superMatchBucketList.add(new SuperMatchBucket(superMatch));
            }
        }
        return superMatchBucketList;
    }

    /**
     * Considering each bucket in turn, attempt to add the buckets to a line, minimising the
     * number of lines and attempting to add the
     *
     * @param buckets
     */
    private void buildLines(List<SuperMatchBucket> buckets) {
        //While building, don't try to sort.  This should speed things up
        // as well as prevent sort order errors due to mutable objects.
        final Set<CondensedLine> unsortedLines = new HashSet<CondensedLine>();
        for (SuperMatchBucket bucket : buckets) {
            boolean bucketFoundAHome = false;
            // Check if this bucket can be added to any existing lines
            for (CondensedLine line : unsortedLines) {  // This will give the lines in the correct order, as they are in a TreeSet.
                bucketFoundAHome = line.addSuperMatchesSameTypeWithoutOverlap(bucket);
                if (bucketFoundAHome) {
                    break; // out of the Condensed line loop - stop trying to add this bucket to any more lines.
                }
            }
            // if the bucket has still not found a line to live on, need to create a new line for it.
            if (!bucketFoundAHome) {
                unsortedLines.add(new CondensedLine(bucket));
            }

            numSuperMatchBlobs += bucket.getSupermatches().size();

        }
        // Sort them when finished building, by placing into a TreeSet.
        lines = new TreeSet<CondensedLine>(unsortedLines);
    }

    public Set<CondensedLine> getLines() {
        return lines;
    }

    public int getNumSuperMatchBlobs() {
        return numSuperMatchBlobs;
    }

    private StringBuilder build(final Map<String, Integer> entryColourMap, final String scale) {
        final float rectangleWidth = 930;
        final float scaleFactor = rectangleWidth / (float) proteinLength;
        int lineHeight = 17;
        int svgTagYDimension = 19;
        int annotationYDimension = 20;
        String[] scaleMarkers = scale.split(",");
        StringBuilder result = new StringBuilder();
        if (lines != null) {
            int lineCounter = 0;
            for (CondensedLine line : lines) {
                lineCounter++;
                EntryType entryType = line.getType();
                result.append("<svg x=\"123px\" y=\"" + svgTagYDimension + "px\">");
                result.append("<rect width=\"" + rectangleWidth + "px\" height=\"" + lineHeight + "px\" style=\"fill:#EFEFEF\"/>");
                if (lineCounter != lines.size()) {
                    ScaleMarkerUtil.appendScaleMarkers(result, scaleMarkers, scaleFactor, lineHeight);
                } else {//last line
                    ScaleMarkerUtil.appendScaleMarkers(result, scaleMarkers, scaleFactor, lineHeight, true);
                }
                Set<SimpleSuperMatch> superMatches = line.getSuperMatchList();
                if (superMatches != null) {
                    for (SimpleSuperMatch superMatch : superMatches) {
                        int locStart = superMatch.getLocation().getStart();
                        int locEnd = superMatch.getLocation().getEnd();
                        int scaledLocationStart = ScaledLocationUtil.getScaledLocationStart(scaleFactor, locStart);
                        int scaledRectangleWidth = ScaledLocationUtil.getScaledLocationLength(scaleFactor, locStart, locEnd + 1, proteinLength); // Blob drawn as ending at location + 1 (start of next amino acid)
                        SimpleEntry firstSimpleEntry = superMatch.getFirstEntry();
                        String entryAccession = "";
                        if (firstSimpleEntry != null) {
                            entryAccession = firstSimpleEntry.getAc();
                        }
                        result.append("<rect");
                        result.append(" ");
                        appendColourClass(result, entryType.toString(), entryColourMap, entryAccession);
                        result.append("x=\"" + scaledLocationStart + "px\" y=\"" + 5 + "px\" width=\"" + (scaledRectangleWidth == 0 ? 1 : scaledRectangleWidth) + "px\" height=\"" + 7 + "px\"");
                        result.append(" ");
                        result.append("rx=\"3.984848\" ry=\"5.6705141\"");
                        result.append(" ");
                        result.append("style=\"stroke:black;stroke-width:1.0\"");
                        result.append(" ");
                        result.append("onmouseover=\"ShowTooltip(evt, '" + locStart + " - " + locEnd + "', 760, 345)\"");
                        result.append(" ");
                        result.append("onmouseout=\"HideTooltip(evt)\">");
                        result.append("<title>" + locStart + " - " + locEnd + "</title>");
                        result.append("</rect>");
                    }
                }
                result.append("</svg>");
                result.append("<svg id=\"domainLink" + lineCounter + "\" x=\"1058px\" y=\"" + annotationYDimension + "px\">");
                result.append("<use xlink:href=\"#blackArrowComponent\"/>");
                result.append("<text x=\"15px\" y=\"10.5px\"");
                result.append(" ");
                result.append("style=\"font-family:Verdana,Helvetica,sans-serif;font-size:11px;stroke:none;fill:#525252;\">");
                if (entryType.equals(EntryType.DOMAIN)) {
                    result.append(EntryType.DOMAIN.toString());
                } else {
                    result.append(EntryType.REPEAT.toString());
                }
                result.append("</text>");
                result.append("</svg>");
                svgTagYDimension += lineHeight;
                annotationYDimension += lineHeight;
            }
        }

        return result;
    }

    /**
     * Appends the colour class for the different entry types.
     *
     * @param result
     * @param entryType
     * @param entryColourMap
     * @param entryAccession
     */
    private void appendColourClass(final StringBuilder result, final String entryType,
                                   final Map<String, Integer> entryColourMap,
                                   final String entryAccession) {
        final Integer colourCode = entryColourMap.get(entryAccession);
        if (entryType != null && colourCode != null) {
            result.append("class=\"");
            result.append("c" + colourCode + " " + entryType);
            result.append("\" ");
        }
    }

    /**
     * Builds condensed view for the SVG template.
     *
     * @param entryColourMap
     * @param scale
     * @return
     */
    public String getCondensedViewForSVG(final Map<String, Integer> entryColourMap, final String scale) {
        return build(entryColourMap, scale).toString();
    }

    /**
     * Returns the exact height in pixel a for the summary view (condensed view).
     *
     * @param heightPerSummaryLine
     * @param globalHeight
     * @return
     */
    public int getCondensedViewComponentHeightForSVG(int heightPerSummaryLine, int globalHeight) {
        int result = globalHeight;
        if (lines != null) {
            result += lines.size() * heightPerSummaryLine;
        }
        return result;
    }

    public int getProteinLength() {
        return proteinLength;
    }
}
