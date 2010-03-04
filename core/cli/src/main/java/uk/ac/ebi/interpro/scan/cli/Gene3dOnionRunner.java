package uk.ac.ebi.interpro.scan.cli;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.io.*;
import uk.ac.ebi.interpro.scan.business.binary.RawMatchBinaryRunner;
import uk.ac.ebi.interpro.scan.business.filter.RawMatchFilter;

import java.util.*;
import java.io.*;

/**
 * Runs Gene3D in "Onion" mode, writing raw and filtered results to separate text files.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class Gene3dOnionRunner {

    private RawMatchBinaryRunner<Gene3dHmmer3RawMatch> binaryRunner;
    private RawMatchFilter<Gene3dHmmer3RawMatch> filter;
    private ResourceWriter<Gene3dHmmer3RawMatch> analysisWriter;
    private ResourceWriter<Gene3dHmmer3RawMatch> resultsWriter;    

    public void execute(Resource fastaFile, Resource hmmFile, Resource resultsDir) throws IOException {

        // Run HMMER
        final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins = binaryRunner.process(fastaFile, hmmFile);

        // Write TSV
        Resource rawResource = new FileSystemResource(File.createTempFile("ipr-", ".raw", resultsDir.getFile()));
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins)    {
            analysisWriter.write(rawResource, p.getMatches(), true);
        }

        // Run DomainFinder
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = filter.filter(rawProteins);

        // Write TSV
        Resource filteredResource = new FileSystemResource(File.createTempFile("ipr-", ".fil", resultsDir.getFile()));
        for (RawProtein<Gene3dHmmer3RawMatch> p : filteredProteins)    {
            resultsWriter.write(filteredResource, p.getMatches(), true);
        }

        System.out.println("Raw matches:");
        cat(rawResource.getInputStream(), System.out);
        System.out.println("");

        System.out.println("Filtered matches:");
        if (filteredResource.exists())  {
            cat(filteredResource.getInputStream(), System.out);
        }
        else    {
            System.out.println("No results");
        }

    }

    public void setAnalysisWriter(ResourceWriter<Gene3dHmmer3RawMatch> analysisWriter) {
        this.analysisWriter = analysisWriter;
    }

    public void setResultsWriter(ResourceWriter<Gene3dHmmer3RawMatch> resultsWriter) {
        this.resultsWriter = resultsWriter;
    }

    public void setProcessor(RawMatchBinaryRunner<Gene3dHmmer3RawMatch> binaryRunner) {
        this.binaryRunner = binaryRunner;
    }

    public void setFilter(RawMatchFilter<Gene3dHmmer3RawMatch> filter) {
        this.filter = filter;
    }

    private void cat(InputStream in, PrintStream out) throws IOException {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(in));
            while (reader.ready()) {
                out.println(reader.readLine());
            }
        }
        finally {
            if (reader != null){
                reader.close();
            }
        }
    }

}