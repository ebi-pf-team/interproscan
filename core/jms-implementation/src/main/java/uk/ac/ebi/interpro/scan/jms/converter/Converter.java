package uk.ac.ebi.interpro.scan.jms.converter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.io.match.writer.*;
import uk.ac.ebi.interpro.scan.jms.main.AbstractI5Runner;
import uk.ac.ebi.interpro.scan.jms.master.SimpleBlackBoxMaster;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.ProteinMatchesHTMLResultWriter;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.ProteinMatchesSVGResultWriter;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.TarArchiveBuilder;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * Converter class which is used to run I5 in 'convert' mode.
 * <p/>
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 */
public class Converter extends AbstractI5Runner implements SimpleBlackBoxMaster {

    private static final Logger LOGGER = Logger.getLogger(Converter.class.getName());

    //XML mapper
    private Jaxb2Marshaller marshaller;

    // Result file writer

    private ProteinMatchesHTMLResultWriter htmlResultWriter;

    private ProteinMatchesSVGResultWriter svgResultWriter;

    //Converter properties

    private String xmlInputFilePath;

    private String outputFilePath;

    private String explicitFileName;

    private boolean isExplicitFileNameSet = false;

    private TemporaryDirectoryManager temporaryDirectoryManager;

    private String temporaryDirectory;

    private String temporaryFileDirSuffix;

    private boolean deleteWorkingDirectoryOnCompletion;

    /* Default value, if no output format is specified */
    private String[] outputFormats;

    private String interproscanVersion;

    private EntryHierarchy entryHierarchy;

    @Required
    public void setInterproscanVersion(String interproscanVersion) {
        this.interproscanVersion = interproscanVersion;
    }

    @Required
    public void setMarshaller(Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Required
    public void setSvgResultWriter(ProteinMatchesSVGResultWriter svgResultWriter) {
        this.svgResultWriter = svgResultWriter;
    }

    @Required
    public void setHtmlResultWriter(ProteinMatchesHTMLResultWriter htmlResultWriter) {
        this.htmlResultWriter = htmlResultWriter;
    }

    public void setFastaFilePath(String fastaFilePath) {
        this.xmlInputFilePath = fastaFilePath;
    }

    public void setOutputBaseFilename(String outputBaseFilename) {
        this.outputFilePath = outputBaseFilename;
    }

    public void setExplicitOutputFilename(String explicitFileName) {
        this.explicitFileName = explicitFileName;
        this.isExplicitFileNameSet = true;
    }

    @Required
    public void setEntryHierarchy(EntryHierarchy entryHierarchy) {
        this.entryHierarchy = entryHierarchy;
    }

    public void setOutputFormats(String[] outputFormats) {
        this.outputFormats = outputFormats;
    }

    public String[] getOutputFormats() {
        if (outputFormats == null) {
            // If no output formats are supplied then return default formats
            return new String[]{
                    FileOutputFormat.TSV.getFileExtension(),
                    FileOutputFormat.GFF3.getFileExtension()
            };
        }
        return outputFormats;
    }

    public void setAnalyses(String[] analyses) {
        //Unused, so leave it
    }

    @Required
    public void setTemporaryDirectoryManager(TemporaryDirectoryManager temporaryDirectoryManager) {
        this.temporaryDirectoryManager = temporaryDirectoryManager;
    }

    public String getTemporaryDirectory() {
        return this.temporaryDirectory;
    }

    @Required
    public void setTemporaryDirectory(String temporaryDirectory) {
        this.temporaryDirectory = temporaryDirectory;
    }

    @Required
    public void setTemporaryFileDirSuffix(String temporaryFileDirSuffix) {
        this.temporaryFileDirSuffix = temporaryFileDirSuffix;
    }

    @Required
    public void setDeleteWorkingDirectoryOnCompletion(boolean deleteWorkingDirectoryOnCompletion) {
        this.deleteWorkingDirectoryOnCompletion = deleteWorkingDirectoryOnCompletion;
    }

    /**
     * Setup the temporary directory for use in convert mode
     */
    private void setupTemporaryDirectory() {
        if (temporaryDirectory != null) {
            if (!temporaryDirectory.endsWith(File.separator)) {
                temporaryDirectory = temporaryDirectory + File.separator;
            }
            if (!temporaryDirectory.endsWith(temporaryFileDirSuffix + File.separator)) {
                // The [UNIQUE] directory needs adding now (temp directory was probably specified by the user on the command line)
                temporaryDirectory = temporaryDirectory + temporaryFileDirSuffix;
            }
            final String temporaryDirectoryName = temporaryDirectoryManager.getReplacement();
            temporaryDirectory = temporaryDirectory.replace(temporaryFileDirSuffix, temporaryDirectoryName);

            setTemporaryDirectory(temporaryDirectory);
        }
    }

    public void run() {
        //Instantiate input file
        final File inputFile = new File(xmlInputFilePath);

        //Change default temp directory
        if (temporaryDirectory == null) {
            throw new IllegalStateException("Temporary directory for convert mode was NULL");
        }
        setupTemporaryDirectory();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("The CONVERT mode is using the following settings...");
            LOGGER.info("Input file: " + inputFile.getAbsolutePath());
            LOGGER.info("Output file path: " + (isExplicitFileNameSet ? explicitFileName : outputFilePath + ".[gff3|tsv|svg.tar.gz|html.tar.gz]"));
            LOGGER.info("Temporary directory: " + temporaryDirectory);
            final String formatsAsString = Arrays.toString(getOutputFormats());
            LOGGER.info("Requested output formats are: " + (outputFormats != null ? formatsAsString : "Undefined, therefore the default set will be use, which is " + formatsAsString));
        }
        //read in XML file and map it to the I5 model
        Source source;
        try {
            source = new StreamSource(new FileReader(new File(xmlInputFilePath)));
            IMatchesHolder object = (IMatchesHolder) marshaller.unmarshal(source);

            Collection<Protein> proteins;
            Collection<NucleotideSequence> nucleotideSequences = null;
            final char sequenceType;

            if (object instanceof ProteinMatchesHolder) {
                proteins = ((ProteinMatchesHolder) object).getProteins();
                sequenceType = 'p';
            } else {
                proteins = new HashSet<>();
                nucleotideSequences = ((NucleicAcidMatchesHolder) object).getNucleotideSequences();
                for (NucleotideSequence nucleotideSequence : nucleotideSequences) {
                    Set<OpenReadingFrame> openReadingFrames = nucleotideSequence.getOpenReadingFrames();
                    for (OpenReadingFrame orf : openReadingFrames) {
                        Protein protein = orf.getProtein();
                        if (protein != null) {
                            proteins.add(protein);
                        }
                    }
                }
                sequenceType = 'n';
            }
            if (LOGGER.isDebugEnabled()) {
                if (sequenceType == 'p') {
                    LOGGER.debug("XML file contains a set of protein sequences and associated matches!");
                } else if (sequenceType == 'n') {
                    LOGGER.debug("XML file contains a set of nucleotide sequences and associated matches!");
                }
            }
            for (String fileOutputFormat : getOutputFormats()) {
                if (fileOutputFormat.equalsIgnoreCase(FileOutputFormat.GFF3.getFileExtension())) {
                    LOGGER.info("Generating GFF3 result output...");
                    Path outputFile = initOutputFile(isExplicitFileNameSet, FileOutputFormat.GFF3);
                    if (sequenceType == 'n') {
                        outputNucleotideSequencesToGFF(outputFile, nucleotideSequences);
                    } else {
                        outputProteinsToGFF(outputFile, proteins);
                    }
                    LOGGER.info("Finished generation of GFF3.");
                } else if (fileOutputFormat.equalsIgnoreCase(FileOutputFormat.TSV.getFileExtension())) {
                    LOGGER.info("Generating TSV result output...");
                    Path outputFile = initOutputFile(isExplicitFileNameSet, FileOutputFormat.TSV);
                    outputToTSV(outputFile, proteins);
                    LOGGER.info("Finished generation of TSV.");
                } else if (fileOutputFormat.equalsIgnoreCase(FileOutputFormat.HTML.getFileExtension())) {
                    LOGGER.info("Generating HTML result output...");
                    htmlResultWriter.setTempDirectory(temporaryDirectory);
                    Path outputFile = initOutputFile(isExplicitFileNameSet, FileOutputFormat.HTML);
                    outputToHTML(outputFile, proteins);
                    LOGGER.info("Finished generation of HTML.");
                } else if (fileOutputFormat.equalsIgnoreCase(FileOutputFormat.SVG.getFileExtension())) {
                    LOGGER.info("Generating SVG result output...");
                    svgResultWriter.setTempDirectory(temporaryDirectory);
                    Path outputFile = initOutputFile(isExplicitFileNameSet, FileOutputFormat.SVG);
                    outputToSVG(outputFile, proteins);
                    LOGGER.info("Finished generation of SVG.");
                } else if (fileOutputFormat.equalsIgnoreCase(FileOutputFormat.RAW.getFileExtension())) {
                    LOGGER.info("Generating RAW result output...");
                    Path outputFile = initOutputFile(isExplicitFileNameSet, FileOutputFormat.RAW);
                    outputToRAW(outputFile, proteins);
                    LOGGER.info("Finished generation of RAW.");
                } else if (fileOutputFormat.equalsIgnoreCase(FileOutputFormat.XML.getFileExtension())) {
                    // No point to convert from XML to XML!
                    System.out.println("XML output format was ignored in convert mode.");
                } else {
                    // Note that GFF3_PARTIAL, XML_SLIM etc are internal formats, not supported by convert mode
                    LOGGER.error("The specified output format - " + fileOutputFormat + " - is not supported!");
                    System.out.println("\n\n" + "The specified output file format " + fileOutputFormat + " was not recognised." + "\n\n");
                    System.exit(1);
                }
            }
        } catch (FileNotFoundException e1) {
            throw new IllegalArgumentException("File not found exception, neither input nor output file!", e1);
        } catch (UnmarshallingFailureException e2) {
            throw new IllegalArgumentException("Input file isn't in valid IMPACT XML!", e2);
        } catch (IOException e3) {
            LOGGER.error("Cannot write or create result file!", e3);
        }

        // TODO Possible refactoring to consider, currently there is similar code in WriteOutputStep.execute() method
        if (deleteWorkingDirectoryOnCompletion) {
            // Clean up empty working directory.
            final String workingDirectory = temporaryDirectory.substring(0, temporaryDirectory.lastIndexOf(File.separatorChar));
            File file = new File(workingDirectory);
            try {
                FileUtils.deleteDirectory(file);
            }catch (IOException e) {
                LOGGER.warn("At convert completion, unable to delete temporary directory " + file.getAbsolutePath());
            }
        }

    }

    private Path initOutputFile(final boolean isExplicitFileNameSet,
                                final FileOutputFormat fileFormat) {

        /* Boolean flag for the HTML and SVG output generation. If TRUE, the generated tar archives will be compress (gzipped) as well */
        boolean compressHtmlAndSVGOutput = true;

        /* Not required. If TRUE (default), it will archive all SVG output files into a single archive.*/
        boolean archiveSVGOutput = true;

        Path outputPath = null;

        if (isExplicitFileNameSet) {
            outputPath = Paths.get(explicitFileName);
            if (Files.exists(outputPath)) {
                try {
                    Files.delete(outputPath);
                } catch (IOException e) {
                    System.out.println("Unable to overwrite file " + outputPath + ".  Please check file permissions.");
                    System.exit(101);
                }
            }
        } else {
            // Try to use the file name provided. If the file already exists, append a bracketed number (Chrome style).
            // but using an underscore rather than a space (pah!)
            Integer counter = null;
            boolean pathAvailable = false;
            while (!pathAvailable) {
                final StringBuilder candidateFileName = new StringBuilder(outputFilePath);
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
                        .append(fileFormat.getFileExtension());
                //Extend file name by tar (tar.gz) extension if HTML or SVG
                if (fileFormat.equals(FileOutputFormat.HTML) || fileFormat.equals(FileOutputFormat.SVG)) {
                    outputPath = Paths.get(TarArchiveBuilder.buildTarArchiveName(candidateFileName.toString(), archiveSVGOutput, compressHtmlAndSVGOutput, fileFormat));
                } else {
                    outputPath = Paths.get(candidateFileName.toString());
                }
                pathAvailable = !Files.exists(outputPath);
            }

        }
        return outputPath;
    }

    private void outputToTSV(final Path path,
                             final Collection<Protein> proteins) throws IOException {
        try (ProteinMatchesTSVResultWriter writer = new ProteinMatchesTSVResultWriter(path)) {
            writeProteinMatches(writer, proteins);
        }
    }

    private void outputToHTML(final Path path,
                              final Collection<Protein> proteins) throws IOException {
        if (proteins != null && proteins.size() > 0) {
            for (Protein protein : proteins) {
                htmlResultWriter.write(protein, entryHierarchy);
            }
            List<Path> resultFiles = htmlResultWriter.getResultFiles();
            buildTarArchive(path, resultFiles);
        }
    }

    /**
     * This method renders the SVG output files.
     * <p/>
     * Please note:
     * <p/>
     * When the SVG files aren't archived, they are written to the output file directory.
     * Otherwise they will be written into the default tempDirectory of the {@link ProteinMatchesSVGResultWriter).
     *
     * @param outputDir Output directory (this file object contains the output path).
     * @param proteins  Set of result proteins.
     * @throws IOException
     */
    private void outputToSVG(final Path path, final Collection<Protein> proteins) throws IOException {
        if (proteins != null && proteins.size() > 0) {
            for (Protein protein : proteins) {
                try {
                    svgResultWriter.write(protein, entryHierarchy);
                } catch (IOException e) {
                    LOGGER.error("Cannot write SVG output file!", e);
                }
            }
            List<Path> resultFiles = svgResultWriter.getResultFiles();
            buildTarArchive(path, resultFiles);
        }
    }

    private void buildTarArchive(Path path, List<Path> resultFiles) throws IOException {
        TarArchiveBuilder tarArchiveBuilder = new TarArchiveBuilder(resultFiles, path, true);
        tarArchiveBuilder.buildTarArchive();
        //Delete result files in the temp directory at the end
        for (Path resultFile : resultFiles) {
            //Only delete HTML files, but not the resource directory which is also part of the result files list
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

    private void outputProteinsToGFF(final Path path,
                                     final Collection<Protein> proteins) throws IOException {
        try (ProteinMatchesGFFResultWriter writer = new GFFResultWriterForProtSeqs(path, interproscanVersion)) {
            //This step writes features (protein matches) into the GFF file
            writeProteinMatches(writer, proteins);
            //This step writes FASTA sequence at the end of the GFF file
            writeFASTASequences(writer);
        }
    }

    private void outputNucleotideSequencesToGFF(final Path path,
                                                final Collection<NucleotideSequence> nucleotideSequences) throws IOException {
        try (GFFResultWriterForNucSeqs writer = new GFFResultWriterForNucSeqs(path, interproscanVersion)) {
            //This step writes features (protein matches) into the GFF file
            writeProteinMatches(writer, nucleotideSequences);
            //This step writes FASTA sequence at the end of the GFF file
            writeFASTASequences(writer);
        }
    }

    /**
     * Output in InterProScan 4 RAW (TSV) output format.
     *
     * @param path     The file to create
     * @param proteins Protein data
     * @throws IOException In the event of an input/output error.
     */
    private void outputToRAW(final Path path,
                             final Collection<Protein> proteins) throws IOException {
        try (ProteinMatchesRAWResultWriter writer = new ProteinMatchesRAWResultWriter(path)) {
            writeProteinMatches(writer, proteins);
        }
    }

    private void writeProteinMatches(final ProteinMatchesResultWriter writer,
                                     final Collection<Protein> proteins) throws IOException {
//        final Map<String, String> parameters = stepInstance.getParameters();
//        final boolean mapToPathway = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_PATHWAY));
//        final boolean mapToGO = Boolean.TRUE.toString().equals(parameters.get(MAP_TO_GO));
//        final boolean mapToInterProEntries = mapToPathway || mapToGO || Boolean.TRUE.toString().equals(parameters.get(MAP_TO_INTERPRO_ENTRIES));
        writer.setMapToInterProEntries(true);
        writer.setMapToGO(true);
        writer.setMapToPathway(true);
        if (proteins != null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Loaded " + proteins.size() + " proteins.");
            }
            for (Protein protein : proteins) {
                writer.write(protein);
            }
        }
    }

    private void writeProteinMatches(final GFFResultWriterForNucSeqs writer,
                                     final Collection<NucleotideSequence> nucleotideSequences) throws IOException {
        writer.setMapToInterProEntries(true);
        writer.setMapToGO(true);
        writer.setMapToPathway(true);
        if (nucleotideSequences != null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Loaded " + nucleotideSequences.size() + " nucleotide sequences.");
            }
            for (NucleotideSequence nucleotideSequence : nucleotideSequences) {
                writer.write(nucleotideSequence);
            }
        }
    }

    private void writeFASTASequences(ProteinMatchesGFFResultWriter writer) throws IOException {
        Map<String, String> identifierToSeqMap = writer.getIdentifierToSeqMap();
        for (String key : identifierToSeqMap.keySet()) {
            writer.writeFASTASequence(key, identifierToSeqMap.get(key));
        }
    }
}
