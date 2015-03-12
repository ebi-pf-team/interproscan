package uk.ac.ebi.interpro.scan.web.io.svg;

import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Builder class that can create a set of nested ul / li for
 * a hierarchy of families.
 *
 * @author Phil Jones,EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FamilyHierachySvgElementBuilder {

    private final List<List<SimpleEntry>> groupedEntries = new ArrayList<List<SimpleEntry>>();
    private Map<String, SimpleEntry> familyAccessions = new HashMap<String, SimpleEntry>();
    private int rowCounter = 1;
    private final Point startCoordinate = new Point(0, 24);
    private final int rightShift = 28;
    private final int downShift = 18;

    /**
     * Used in JUnit tests only.
     */
    protected FamilyHierachySvgElementBuilder() {
    }


    public FamilyHierachySvgElementBuilder(SimpleProtein simpleProtein) {
        //reset row counter
        this.rowCounter = 1;
        if (simpleProtein != null) {
            Set<SimpleEntry> familyEntries = simpleProtein.getFamilyEntries();
            if (familyEntries != null && familyEntries.size() > 0) {
                // Place families into a List of Lists grouped by hierarchy.
                outerLoop:
                for (SimpleEntry familyEntry : familyEntries) {
                    familyAccessions.put(familyEntry.getAc(), familyEntry);
                    for (List<SimpleEntry> existingList : groupedEntries) {
                        if (existingList.size() > 0) {
                            if (SimpleEntry.getEntryHierarchy().areInSameHierarchy(familyEntry, existingList.get(0))) {
                                existingList.add(familyEntry);
                                continue outerLoop;
                            }
                        }
                    }
                    // This entry is not in the same hierarchy as any of the entries already considered,
                    // so create a new list.
                    final List<SimpleEntry> newList = new ArrayList<SimpleEntry>();
                    newList.add(familyEntry);
                    groupedEntries.add(newList);
                }
            }
        }
    }

    public StringBuilder build() {
        StringBuilder result = new StringBuilder();
        try {
            for (List<SimpleEntry> hierarchy : groupedEntries) {
                EntryHierarchyData root = hierarchy.get(0).getHierarchyData();
                if (root == null) {
                    // Flat - just spit out this Entry on its own.
                    SimpleEntry entry = hierarchy.get(0);
                    appendEntry(entry, result);
                } else {
                    StringBuilder list = siblings(root);
                    result.append(list);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected Point getNewCoordinate(Point startCoordinate, Integer hierarchyLevel, int rowCounter, int rightShift, int downShift) {
        //set hierarchyLevel to 1 if no hierarchy data is available
        if (hierarchyLevel == null) {
            hierarchyLevel = new Integer(1);
        }
        return new Point(startCoordinate.x + (hierarchyLevel - 1) * rightShift, startCoordinate.y + (rowCounter - 1) * downShift);
    }

    protected StringBuilder siblings(EntryHierarchyData sibling) throws IOException {
        StringBuilder siblings = new StringBuilder();
        final SimpleEntry includedEntry = entryDataMatched(sibling);
        if (includedEntry != null) {
            appendEntry(includedEntry, siblings);
        }
        siblings.append(children(sibling));
        return siblings;
    }

    protected SimpleEntry entryDataMatched(EntryHierarchyData ehd) {
        return familyAccessions.get(ehd.getEntryAc());
    }

    private StringBuilder children(EntryHierarchyData parent) throws IOException {
        StringBuilder children = new StringBuilder();

        // Iterate over children
        for (final EntryHierarchyData child : parent.getImmediateChildren()) {
            children.append(siblings(child));
        }
        return children;
    }

    protected void appendEntry(SimpleEntry entry, StringBuilder sb) {
        final Point coordinate = getNewCoordinate(startCoordinate, entry.getHierarchyLevel(), rowCounter, rightShift, downShift);
        sb.append("<svg x=\"" + coordinate.x + "px\" y=\"" + coordinate.y + "px\">");
        sb.append("<path stroke=\"black\" stroke-width=\"1\" stroke-dasharray=\"1\" d=\"M1,5 V10\" />");
        sb.append("<path stroke=\"black\" stroke-width=\"1\" stroke-dasharray=\"1\" d=\"M0.5,9.5 H6\" />");
        sb.append("<use x=\"8\" y=\"2\" xlink:href=\"#ico_type_family_small\"/>");
        sb.append("<a xlink:href=\"http://www.ebi.ac.uk/interpro/entry/");
        sb.append(entry.getAc());
        sb.append("\" target=\"_top\">");
        sb.append("<text x=\"24px\" y=\"12px\" text-decoration=\"underline\">");
        sb.append("<tspan style=\"font-family:Verdana,Helvetica,sans-serif;font-size:13px;stroke:none;fill:#393939;\">");
        sb.append(entry.getName());
        sb.append("</tspan>");
        sb.append(" ");
        sb.append("<tspan style=\"font-family:Verdana,Helvetica,sans-serif;font-size:13px;stroke:none;fill:#0072FE;\">");
        sb.append("(" + entry.getAc() + ")");
        sb.append("</tspan>");
        sb.append("</text>");
        sb.append("</a>");
        sb.append("</svg>");
        rowCounter++;
    }

    public int getRowCounter() {
        return rowCounter;
    }
}
