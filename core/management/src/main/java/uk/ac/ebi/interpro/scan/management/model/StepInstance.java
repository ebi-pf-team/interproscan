package uk.ac.ebi.interpro.scan.management.model;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class StepInstance {

    private Long id;

    private Step step;

    private Long bottomProtein;

    private Long topProtein;

    private Long bottomModel;

    private Long topModel;

    private List<StepInstance> dependencies = new ArrayList<StepInstance>();

    private List<StepExecution> executions = new ArrayList<StepExecution>();
    
}
