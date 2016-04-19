package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.XmlWriter;
import uk.ac.ebi.interpro.scan.io.match.writer.*;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.ProteinMatchesHTMLResultWriter;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.ProteinMatchesSVGResultWriter;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.TarArchiveBuilder;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinXrefDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Writes all matches for a slice of proteins to a file.
 * <p/>
 * Should be run once analysis is complete.
 *
 * @author David Binns
 * @author Maxim Scheremetjew
 * @author Phil Jones
 */

public class WriteOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(WriteOutputStep.class.getName());

    //DAOs
    private ProteinDAO proteinDAO;

    private ProteinXrefDAO proteinXrefDAO;

    //Output writer
    private XmlWriter xmlWriter;

    private ProteinMatchesHTMLResultWriter htmlResultWriter;

    private ProteinMatchesSVGResultWriter svgResultWriter;

    //Misc
    private boolean deleteWorkingDirectoryOnCompletion = true;

    /* Boolean flag for the HTML and SVG output generation. If TRUE, the generated tar archives will be compress (gzipped) as well */
    private boolean compressHtmlAndSVGOutput;

    /* Not required. If TRUE (default), it will archive all SVG output files into a single archive.*/
    private boolean archiveSVGOutput = true;

    public static final String OUTPUT_EXPLICIT_FILE_PATH_KEY = "EXPLICIT_OUTPUT_FILE_PATH";

    public static final String OUTPUT_FILE_PATH_KEY = "OUTPUT_PATH";
    public static final String OUTPUT_FILE_FORMATS = "OUTPUT_FORMATS";
    public static final String MAP_TO_INTERPRO_ENTRIES = "MAP_TO_INTERPRO_ENTRIES";
    public static final String MAP_TO_GO = "MAP_TO_GO";
    public static final String MAP_TO_PATHWAY = "MAP_TO_PATHWAY";
    public static final String SEQUENCE_TYPE = "SEQUENCE_TYPE";

    public void setArchiveSVGOutput(boolean archiveSVGOutput) {
        this.archiveSVGOutput = archiveSVGOutput;
    }

    @Required
    public void setCompressHtmlAndSVGOutput(boolean compressHtmlAndSVGOutput) {
        this.compressHtmlAndSVGOutput = compressHtmlAndSVGOutput;
    }

    @Required
    public void setHtmlResultWriter(ProteinMatchesHTMLResultWriter htmlResultWriter) {
        this.htmlResultWriter = htmlResultWriter;
    }

    @Required
    public void setSvgResultWriter(ProteinMatchesSVGResultWriter svgResultWriter) {
        this.svgResultWriter = svgResultWriter;
    }

    @Required
    public void setXmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    @Required
    public void setDeleteWorkingDirectoryOnCompletion(String deleteWorkingDirectoryOnCompletion) {
        this.deleteWorkingDirectoryOnCompletion = "true".equalsIgnoreCase(deleteWorkingDirectoryOnCompletion);
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setXrefDao(ProteinXrefDAO proteinXrefDAO) {
        this.proteinXrefDAO = proteinXrefDAO;
    }

    /**
     * Sets/persists new unique protein xref identifiers in cases where they are non unique (same ID, different sequences).
     */
    private void setUniqueXrefs() {
        if (proteinXrefDAO == null) {
            throw new IllegalStateException("Protein Xref database accession object is NULL. Unexpected state. Cannot go on.");
        }
        Collection<ProteinXref> updates = new HashSet<>();
        Collection<String> nonUniqueIdentifiers = proteinXrefDAO.getNonUniqueXrefs();
        if (nonUniqueIdentifiers != null && nonUniqueIdentifiers.size() > 0) {
            System.out.println("Found " + nonUniqueIdentifiers.size() + " non unique identifier(s). These identifiers do have different sequences, within the FASTA protein sequence input file.");
            System.out.println("InterProScan will make them unique by adding '_sequential number' in the order of their appearance (e.g. P11111 will be P11111_1 for the first protein sequence).");
            System.out.println("Please find below a list of detected identifiers:");
            for (String nonUniqueIdentifier : nonUniqueIdentifiers) {
                System.out.println(nonUniqueIdentifier);
            }
        }
        for (String identifier : nonUniqueIdentifiers) {
            List<ProteinXref> proteinXrefs = proteinXrefDAO.getXrefAndProteinByProteinXrefIdentifier(identifier);
            if (proteinXrefs.size() < 2) {
                LOGGER.warn("Unexpected databases query result. The size of the result set should be > 1.");
            }
            int counter = 0;
            for (ProteinXref xref : proteinXrefs) {
                counter++;
                xref.setIdentifier(xref.getIdentifier() + "_" + counter);
                updates.add(xref);
            }
        }
        proteinXrefDAO.updateAll(updates);
    }


    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting step with Id " + this.getId());
        }
        final Map<String, String> parameters = stepInstance.getParameters();
        final String outputFormatStr = parameters.get(OUTPUT_FILE_FORMATS);
        final Set<FileOutputFormat> outputFormats = FileOutputFormat.stringToFileOutputFormats(outputFormatStr);
        final boolean explicitPath = parameters.containsKey(OUTPUT_EXPLICIT_FILE_PATH_KEY);
        final String filePathName = (explicitPath)
                ? parameters.get(OUTPUT_EXPLICIT_FILE_PATH_KEY)
                : parameters.get(OUTPUT_FILE_PATH_KEY);


        int waitTimeFactor = 2;
        if (! Utilities.isRunningInSingleSeqMode()) {
            //use loge to get wait time
            waitTimeFactor = Utilities.getWaitTimeFactorLogE(20 * Utilities.getSequenceCount()).intValue();
        }
        Utilities.sleep(waitTimeFactor * 1000);                 //1000 milliseconds is one second.
        Utilities.verboseLog(10, " WriteOutputStep - get proteins, waitTime - " + waitTimeFactor + " seconds");

        Long timeNow = System.currentTimeMillis();
        List<Protein> proteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        Utilities.verboseLog(10, " WriteOutputStep - proteins to writeout: " + proteins.size()
                + " time taken to get proteins: "
                + (System.currentTimeMillis() - timeNow)
                + " millis");
        final String sequenceType = parameters.get(SEQUENCE_TYPE);
        if (sequenceType.equalsIgnoreCase("p")) {
            LOGGER.debug("Setting unique protein cross references (Please note this function is only performed if the input sequences are proteins)...");
            setUniqueXrefs();
        }

        for (FileOutputFormat outputFormat : outputFormats) {
            Path outputPath = getPathName(explicitPath, filePathName, outputFormat);
            try {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Writing out " + outputFormat + " file");
                }
                switch (outputFormat) {
                    case TSV:
                        outputToTSV(outputPath, stepInstance, proteins);
                        break;
                    case XML:
                        outputToXML(outputPath, sequenceType, proteins, false);
                        break;
                    case XML_SLIM:
                        outputToXML(outputPath, sequenceType, proteins, true);
                        break;
                    case GFF3:
                        outputToGFF(outputPath, stepInstance, sequenceType, proteins);
                        break;
                    case GFF3_PARTIAL:
                        outputToGFFPartial(outputPath, stepInstance, proteins);
                        break;
                    case HTML:
                        //Replace the default temp dir with the user specified one
                        if (temporaryFileDirectory != null) {
                            htmlResultWriter.setTempDirectory(temporaryFileDirectory);
                        }
                        outputToHTML(outputPath, proteins);
                        break;
                    case SVG:
                        //Replace the default temp dir with the user specified one
                        if (temporaryFileDirectory != null) {
                            svgResultWriter.setTempDirectory(temporaryFileDirectory);
                        }
                        outputToSVG(outputPath, proteins);
                        break;
                    default:
                        LOGGER.warn("Unrecognised output format " + outputFormat + " - cannot write the output file.");
                }
            } catch (IOException ioe) {
                final String p = outputPath.toAbsolutePath().toString();
                throw new IllegalStateException("IOException thrown when attempting to writeComment output from InterProScan to path: " + p, ioe);
            }
        }


        cleanUpWorkingDir(temporaryFileDirectory);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Step with Id " + this.getId() + " finished.");
        }
    }

    private void cleanUpWorkingDir(final String temporaryFileDirectory) {

        LOGGER.debug("deleteWorkingDirectoryOnCompletion: " + deleteWorkingDirectoryOnCompletion);

        if (deleteWorkingDirectoryOnCompletion) {
            // Clean up empty working directory.
            final String workingDirectory = temporaryFileDirectory.substring(0, temporaryFileDirectory.lastIndexOf(File.separatorChar));
            File file = new File(workingDirectory);
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                LOGGER.warn("At write output completion, unable to delete temporary directory " + file.getAbsolutePath());
            }
        }else{
            LOGGER.debug("Files in temporaryFileDirectory not deleted since  delete.working.directory.on.completion =  " + deleteWorkingDirectoryOnCompletion);
        }
    }

    private Path getPathName(final boolean explicitPath,
                             final String filePathName,
                             final FileOutputFormat outputFormat) {
        // E.g. for "-b OUT" filePathName = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT"
        Path outputPath = null;

        if (explicitPath) {
            outputPath = Paths.get(filePathName);
            if (Files.exists(outputPath)) {
                try {
                    Files.delete(outputPath);
                } catch (IOException e) {
                    final String p = outputPath.toAbsolutePath().toString();
                    System.out.println("Unable to overwrite file " + p + ".  Please check file permissions.");
                    System.exit(101);
                }
            }
        } else {
            // Try to use the file name provided. If the file already exists, append a bracketed number (Chrome style).
            // but using an underscore rather than a space (pah!)
            Integer counter = null;
            int ioCounter = 0;
            boolean pathAvailable = false;
            while (!pathAvailable) {
                final StringBuilder candidateFileName = new StringBuilder(filePathName);
                if (counter == null) {
                    counter = 1;
                } else {
                    // E.g. Output file name could become "test_proteins.fasta_1.tsv"
                    candidateFileName
                            .append('_')
                            .append(counter++);
                }
                candidateFileName
                        .append('.')
                        .append(outputFormat.getFileExtension());
                //Extend file name by tar (tar.gz) extension if HTML or SVG
                if (outputFormat.equals(FileOutputFormat.HTML) || outputFormat.equals(FileOutputFormat.SVG)) {
                    outputPath = Paths.get(TarArchiveBuilder.buildTarArchiveName(candidateFileName.toString(), archiveSVGOutput, compressHtmlAndSVGOutput, outputFormat));
                } else {
                    outputPath = Paths.get(candidateFileName.toString());
                }
                pathAvailable = !Files.exists(outputPath);
                if (pathAvailable) {
                    try {
                        // Start creating the empty output file now, while the path is still available
                        outputPath = Files.createFile(outputPath);
                    } catch (IOException e) {
                        pathAvailable = false; // Nope, that path has probably just been taken (e.g. by another copy of InterProScan writing to the same output directory)
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Path " + candidateFileName.toString() + " was available for writing to, but I/O exception thrown");
                        }
                        ioCounter++;
                        if (ioCounter > 2000) {
                            // Stop possible infinite loop!
                            throw new IllegalStateException("Path " + candidateFileName.toString() + " was available, but I/O exception thrown on file creation");
                        }
                    }
                }
            }

        }
        return outputPath;
    }

    private void outputToXML(Path outputPath, String sequenceType, List<Protein> proteins, boolean isSlimOutput) throws IOException {
        IMatchesHolder matchesHolder;
        if (sequenceType.equalsIgnoreCase("n")) {
            matchesHolder = new NucleicAcidMatchesHolder();
        } else {
            matchesHolder = new ProteinMatchesHolder();
        }
        Utilities.verboseLog(10, " WriteOutputStep - outputToXML ");
        if (isSlimOutput) {
            // Only include a protein in the output if it has at least one match
            for (Protein protein : proteins) {
                Set<Match> matches = protein.getMatches();
                if (matches != null && matches.size() > 0) {
                    matchesHolder.addProtein(protein);
                }
            }
        } else {
            // Include all proteins in the output, whether they have any matches or not
            matchesHolder.addProteins(proteins);
        }
        Utilities.verboseLog(10, " WriteOutputStep - outputToXML xml-slim? " + isSlimOutput);
        xmlWriter.writeMatches(outputPath, matchesHolder);
    }

    private void outputToTSV(final Path path,
                             final StepInstance stepInstance,
                             final List<Protein> proteins) throws IOException {
        try (ProteinMatchesTSVResultWriter writer = new ProteinMatchesTSVResultWriter(path)) {
            writeProteinMatches(writer, stepInstance, proteins);
        }
    }

    private void outputToGFF(Path path, StepInstance stepInstance, String sequenceType, List<Protein> proteins) throws IOException {
        ProteinMatchesGFFResultWriter writer = null;
        try {
            if (sequenceType.equalsIgnoreCase("n")) {
                writer = new GFFResultWriterForNucSeqs(path);
            }//Default tsvWriter for proteins
            else {
                writer = new GFFResultWriterForProtSeqs(path);
            }

            //This step writes features (protein matches) into the GFF file
            writeProteinMatches(writer, stepInstance, proteins);
            //This step writes FASTA sequence at the end of the GFF file
            writeFASTASequences(writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void outputToGFFPartial(Path path, StepInstance stepInstance, List<Protein> proteins) throws IOException {
        try (ProteinMatchesGFFResultWriter writer = new GFFResultWriterForProtSeqs(path, false)) {
            writeProteinMatches(writer, stepInstance, proteins);
        }

    }


    private void outputToHTML(final Path path, final List<Protein> proteins) throws IOException {
        // E.g. for "-b OUT" file = "/home/matthew/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT.html.tar.gz"
        if (proteins != null && proteins.size() > 0) {
            for (Protein protein : proteins) {
                htmlResultWriter.write(protein);
            }
            List<Path> resultFiles = htmlResultWriter.getResultFiles();
            // E.g. resultFiles =
            // - data/freemarker/resources
            //   - data/freemarker/resources/images
            //     - data/freemarker/resources/images/ico_type_family_small.png
            //     ...
            //   - data/freemarker/resources/javascript
            //   ...
            // - ~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/my-computer-name_20160301_141713605_ivyx/jobWriteOutput/P22298.html
            // - ~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/my-computer-name_20160301_141713605_ivyx/jobWriteOutput/P02939.html
            // ...

            buildTarArchive(path, resultFiles);
        }
    }

    /**
     * This method renders the SVG output files.
     * <p/>
     * Please note:
     * <p/>
     * When the SVG files aren't archived, they are written to the output file directory.
     * Otherwise they will be written into the default outputDirectory of the {@link ProteinMatchesSVGResultWriter).
     *
     * @param outputDir Output directory (this file object contains the output path).
     * @param proteins  Set of result proteins.
     * @throws IOException
     */
    private void outputToSVG(final Path path, final List<Protein> proteins) throws IOException {
        // E.g. for "-b OUT" outputDir = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT.svg.tar.gz"
        if (proteins != null && proteins.size() > 0) {
            //If the archive mode is switched off single SVG files should be written to the global output directory
            if (!archiveSVGOutput) {
                final String outputDirPath = path.toAbsolutePath().toString();
                svgResultWriter.setTempDirectory(outputDirPath);
            }
            for (Protein protein : proteins) {
                svgResultWriter.write(protein);
            }
            if (archiveSVGOutput) {
                List<Path> resultFiles = svgResultWriter.getResultFiles();
                // E.g. resultFiles =
                // - ~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/my-computer-name_20160301_141713605_ivyx/jobWriteOutput/P22298.svg
                // - ~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/my-computer-name_20160301_141713605_ivyx/jobWriteOutput/P02939.svg
                // ...

                buildTarArchive(path, resultFiles);
            }
        }
    }

    private void buildTarArchive(Path path, List<Path> resultFiles) throws IOException {
        TarArchiveBuilder tarArchiveBuilder = new TarArchiveBuilder(resultFiles, path, compressHtmlAndSVGOutput);
        tarArchiveBuilder.buildTarArchive();
        //Delete result files in the temp directory at the end
        for (Path resultFile : resultFiles) {
            //Only delete HTML/SVG files, but not the resource directory which is also part of the result files list
            if (Files.isRegularFile(resultFile)) {
                try {
                    Files.delete(resultFile);
                } catch (IOException e) {
                    if (LOGGER.isEnabledFor(Level.WARN)) {
                        final String r = resultFile.toAbsolutePath().toString();
                        LOGGER.warn("Couldn't delete file " + r);
                    }
                }
            }
        }
    }

    private void writeFASTASequences(ProteinMatchesGFFResultWriter writer) throws IOException {
        writer.writeFASTADirective();
        Map<String, String> identifierToSeqMap = writer.getIdentifierToSeqMap();
        for (String key : identifierToSeqMap.keySet()) {
            writer.writeFASTASequence(key, identifierToSeqMap.get(key));
        }
    }

    private void writeProteinMatches(ProteinMatchesResultWriter writer, StepInstance stepInstance, List<Protein> proteins) throws IOException {
        Utilities.verboseLog(10, " WriteOutputStep - outputToTSV-etc ");
        final Map<String, String> parameters = stepInstance.getParameters();
        final boolean mapToPathway = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_PATHWAY));
        final boolean mapToGO = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_GO));
        final boolean mapToInterProEntries = mapToPathway || mapToGO || Boolean.TRUE.toString().equals(parameters.get(MAP_TO_INTERPRO_ENTRIES));
        writer.setMapToInterProEntries(mapToInterProEntries);
        writer.setMapToGo(mapToGO);
        writer.setMapToPathway(mapToPathway);
        if (proteins != null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Loaded " + proteins.size() + " proteins.");
            }
            Utilities.verboseLog(10, " WriteOutputStep -tsv-etc " + "Loaded " + proteins.size() + " proteins.");
            int count = 0;
            for (Protein protein : proteins) {
                writer.write(protein);
                count++;
                if (count % 20000 == 0) {
                    Utilities.verboseLog(10, " WriteOutout - wrote " + count + " proteins");
                }
            }
        }
    }

}
