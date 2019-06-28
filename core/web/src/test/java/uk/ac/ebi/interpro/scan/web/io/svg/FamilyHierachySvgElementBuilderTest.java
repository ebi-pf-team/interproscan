package uk.ac.ebi.interpro.scan.web.io.svg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.awt.*;

/**
 * TODO: Description
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FamilyHierachySvgElementBuilderTest {

    private final Point coordinate = new Point(10, 10);
    private final int rightShift = 10;
    private final int downShift = 10;


    @Test
    public void testGetNewCoordinate() {
        FamilyHierachySvgElementBuilder instance = new FamilyHierachySvgElementBuilder();
        int hierarchyLevel = 1;
        int rowCounter = 1;
        Point newCoordinate = instance.getNewCoordinate(coordinate, hierarchyLevel, rowCounter, rightShift, downShift);
        assertEquals(10, newCoordinate.x);
        assertEquals(10, newCoordinate.y);
        //
        hierarchyLevel = 2;
        rowCounter = 1;
        newCoordinate = instance.getNewCoordinate(coordinate, hierarchyLevel, rowCounter, rightShift, downShift);
        assertEquals(20, newCoordinate.x);
        assertEquals(10, newCoordinate.y);
        //
        hierarchyLevel = 2;
        rowCounter = 5;
        newCoordinate = instance.getNewCoordinate(coordinate, hierarchyLevel, rowCounter, rightShift, downShift);
        assertEquals(20, newCoordinate.x);
        assertEquals(50, newCoordinate.y);
        //
        rowCounter = 5;
        newCoordinate = instance.getNewCoordinate(coordinate, null, rowCounter, rightShift, downShift);
        assertEquals(10, newCoordinate.x);
        assertEquals(50, newCoordinate.y);
    }
}