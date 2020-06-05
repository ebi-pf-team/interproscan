package uk.ac.ebi.interpro.scan.web.io;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Query for match data and construct a simple protein object from the query results.
 *
 * @author Matthew Fraser
 * @author Antony Quinn
 * @version $Id$
 */
@Component
public class CreateSimpleProteinFromMatchDataImpl implements CreateSimpleProteinFromMatchData {

    private static final Logger LOGGER = LogManager.getLogger(CreateSimpleProteinFromMatchDataImpl.class);

    private final AnalyseMatchDataResult matchAnalyser;
    private final AnalyseStructuralMatchDataResult structuralMatchAnalyser;
    private String matchDataLocation;
    private String structuralMatchDataLocation;

    private ResourceLoader resourceLoader;

    private static final String EXTENSION = ".tsv";

    // TODO: Configure in Spring context
    private CreateSimpleProteinFromMatchDataImpl() {
        matchAnalyser = null;
        structuralMatchAnalyser = null;
        matchDataLocation = null;
        structuralMatchDataLocation = null;
    }

    public CreateSimpleProteinFromMatchDataImpl(AnalyseMatchDataResult matchAnalyser,
                                                AnalyseStructuralMatchDataResult structuralMatchAnalyser,
                                                String matchDataLocation,
                                                String structuralMatchDataLocation) {
        this.matchAnalyser = matchAnalyser;
        this.structuralMatchAnalyser = structuralMatchAnalyser;
        this.matchDataLocation = matchDataLocation;
        this.structuralMatchDataLocation = structuralMatchDataLocation;
    }

    @Autowired
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Cacheable(value = "simple_prot_by_ac")
    public SimpleProtein queryByAccession(String ac) throws IOException {
        return retrieveMatches(createMatchesUrl(ac, true), createStructuralMatchesUrl(ac, true));
    }

    public SimpleProtein queryByMd5(String md5) throws IOException {
        return retrieveMatches(createMatchesUrl(md5, false), createStructuralMatchesUrl(md5, false));
    }

    private SimpleProtein retrieveMatches(String matchesUrl, String structuralMatchesUrl) {

        // TODO: Shouldn't need this -- why isn't Spring giving us the resourceLoader??
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader();
        }
        // Get match data
        SimpleProtein protein = this.matchAnalyser.parseMatchDataOutput(resourceLoader.getResource(matchesUrl));

        if (protein == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Protein match data was not found or could not be parsed from " + matchesUrl);
            }
            // else this protein had no match data, but continue to look for structural match data
        }

        // Add structural matches
        Collection<SimpleStructuralDatabase> structuralDatabases =
                structuralMatchAnalyser.parseStructuralMatchDataOutput(resourceLoader.getResource(structuralMatchesUrl));
        if (structuralDatabases != null) {
            for (SimpleStructuralDatabase db : structuralDatabases) {
                if (protein == null) {
                    // There were no matches above, but we do have structural matches here so now need to initialise the protein object
                    // TODO Note that we do not have the taxonomy information, but the protein structure web pages do not display that information anyway
                    StructuralMatchDataRecord data = structuralMatchAnalyser.getSampleStructuralMatch();
                    protein = new SimpleProtein(
                            data.getProteinAc(),
                            data.getProteinId(),
                            data.getProteinDescription(),
                            data.getProteinLength(),
                            "N/A",
                            data.getCrc64(),
                            data.isProteinFragment());

                    if (protein == null) {
                        throw new IllegalStateException("Error constructing a SimpleProtein");
                    }
                }
                protein.getStructuralDatabases().add(db);
            }
        }

        return protein;

    }

    private String createMatchesUrl(String proteinAc, boolean isProteinAc) {
        if (matchDataLocation.startsWith("classpath:") || matchDataLocation.startsWith("file:")) {
            // If using a manually supplied file on the classpath then just use that location (good for testing)
            return matchDataLocation;
        }
        // User has just supplied the protein accession, so build up the REST URL from that
        return buildUrl(proteinAc, isProteinAc, true);
    }

    private String createStructuralMatchesUrl(String proteinAc, boolean isProteinAc) {
        if (structuralMatchDataLocation.startsWith("classpath:") || structuralMatchDataLocation.startsWith("file:")) {
            // If using a manually supplied file on the classpath then just use that location (good for testing)
            return structuralMatchDataLocation;
        }
        // User has just supplied the protein accession, so build up the REST URL from that
        return buildUrl(proteinAc, isProteinAc, false);
    }

    private String buildUrl(String proteinAc, boolean isProteinAc, boolean isMatchUrl) {
        String prefix;
        if (isMatchUrl) {
            prefix = matchDataLocation;
        }
        else {
            prefix = structuralMatchDataLocation;
        }
        if (useLocalData()) {
            String currentDir = System.getProperty("user.dir");
            currentDir = currentDir.replace(File.separatorChar, '/');
            prefix = "file://" + currentDir + "/src/test/resources/data/";
            if (isMatchUrl) {
                prefix += "proteins/";
            } else {
                prefix += "proteinStructures/";
            }
        }
        // TODO: Use MD5 as filter if not proteinAc
        return prefix + proteinAc + EXTENSION;
    }

    // Only use for testing -- means we don't need a connection to data source
    private static boolean useLocalData() {
        return System.getProperty("ebi.local.data", "false").equals("true");
    }
}
