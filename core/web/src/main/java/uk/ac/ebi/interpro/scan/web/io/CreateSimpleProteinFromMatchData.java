package uk.ac.ebi.interpro.scan.web.io;

import java.io.*;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatch;

/**
 * Query for match data and construct a simple protein object from the query results.
 *
 * @author  Matthew Fraser
 * @author  Antony Quinn
 * @version $Id$
 */
@Component
public class CreateSimpleProteinFromMatchData implements ResourceLoaderAware {

    private static final Logger LOGGER = Logger.getLogger(CreateSimpleProteinFromMatchData.class);

    // TODO: Make this configurable
    private static final String MATCH_DATA_URL = "http://wwwdev.ebi.ac.uk/interpro/match/";
    private static final String STRUCTURAL_MATCH_DATA_URL = "http://wwwdev.ebi.ac.uk/interpro/structure/";
    private static final String EXTENSION = ".tsv";

    private final AnalyseMatchDataResult matchAnalyser;
    private final AnalyseStructuralMatchDataResult structuralMatchAnalyser;

    private ResourceLoader resourceLoader;

    // TODO: Configure in Spring context
    private CreateSimpleProteinFromMatchData() {
        matchAnalyser = null;
        structuralMatchAnalyser = null;
    }

    public CreateSimpleProteinFromMatchData(AnalyseMatchDataResult matchAnalyser,
                                            AnalyseStructuralMatchDataResult structuralMatchAnalyser) {
        this.matchAnalyser = matchAnalyser;
        this.structuralMatchAnalyser = structuralMatchAnalyser;
    }

    public SimpleProtein queryByAccession(String ac) throws IOException {
        return retrieveMatches(createMatchesUrl(ac, true), createStructuralMatchesUrl(ac, true));
    }

    public SimpleProtein queryByMd5(String md5) throws IOException {
        return retrieveMatches(createMatchesUrl(md5, false), createStructuralMatchesUrl(md5, false));
    }

    @Override public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private SimpleProtein retrieveMatches(String matchesUrl, String structuralMatchesUrl) {

        // TODO: Shouldn't need this -- why isn't Spring giving us the resourceLoader??
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader();
        }
        // Get match data
        SimpleProtein protein =  this.matchAnalyser.parseMatchDataOutput(resourceLoader.getResource(matchesUrl));

        // Add structural matches
        if (protein == null) {
            throw new IllegalStateException("Protein match data was not found or could not be parsed");
        }
        List<SimpleStructuralMatch> structuralMatches =
                structuralMatchAnalyser.parseStructuralMatchDataOutput(resourceLoader.getResource(structuralMatchesUrl));
        if (structuralMatches != null) {
            for (SimpleStructuralMatch m : structuralMatches) {
                protein.getStructuralMatches().add(m);
            }
        }

        return protein;

    }

    private String createMatchesUrl(String proteinAc, boolean isProteinAc) {
        return buildUrl(proteinAc, isProteinAc, true);
    }

    private String createStructuralMatchesUrl(String proteinAc, boolean isProteinAc) {
        return buildUrl(proteinAc, isProteinAc, false);
    }

    private String buildUrl(String proteinAc, boolean isProteinAc, boolean isMatchUrl) {
        String prefix = STRUCTURAL_MATCH_DATA_URL;
        if (isMatchUrl) {
            prefix = MATCH_DATA_URL;
        }
        String extension = EXTENSION;
        if (useLocalData()) {
            String currentDir = System.getProperty("user.dir");
            currentDir = currentDir.replace(File.separatorChar, '/');
            prefix = "file://" + currentDir + "/src/test/resources/data/";
            if (isMatchUrl) {
                extension = "-match";
            }
            else {
                extension = "-structure";
            }
            extension += EXTENSION;
        }
        // TODO: Use MD5 as filter if not proteinAc
        return prefix + proteinAc + extension;
    }

    // Only use for testing -- means we don't need a connection to data source
    private static boolean useLocalData()  {
        return System.getProperty("ebi.local.data", "false").equals("true");
    }

}
