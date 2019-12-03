package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simple Tarball builder.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TarArchiveBuilder {

    private final List<Path> tarArchiveEntries = new ArrayList<>();

    private final Path tarArchive;

    private boolean compress;

    public TarArchiveBuilder(Path tarArchiveEntry, Path tarArchive) {
        this(tarArchiveEntry, tarArchive, false);
    }

    public TarArchiveBuilder(Path tarArchiveEntry, Path tarArchive, boolean compress) {
        this.tarArchiveEntries.add(tarArchiveEntry);
        this.tarArchive = tarArchive;
        this.compress = compress;
    }

    public TarArchiveBuilder(List<Path> tarArchiveEntries, Path tarArchive) {
        this(tarArchiveEntries, tarArchive, false);
    }

    public TarArchiveBuilder(List<Path> tarArchiveEntries, Path tarArchive, boolean compress) {
        this.tarArchiveEntries.addAll(tarArchiveEntries);
        this.tarArchive = tarArchive;
        this.compress = compress;
    }

    public void buildTarArchive() throws IOException {
        // E.g. for "-b OUT" tarArchive = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT.html.tar.gz"
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        GzipCompressorOutputStream gzipCompressorOutputStream = null;
        TarArchiveOutputStream tarArchiveOutputStream = null;
        //
        try {
            fileOutputStream = new FileOutputStream(tarArchive.toFile());
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            if (compress) {
                gzipCompressorOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
                tarArchiveOutputStream = new TarArchiveOutputStream(gzipCompressorOutputStream);

            } else {
                tarArchiveOutputStream = new TarArchiveOutputStream(bufferedOutputStream);
            }

            for (Path entry : tarArchiveEntries) {
                addNewEntryToArchive(entry, tarArchiveOutputStream, "");
            }
        } finally {
            if (tarArchiveOutputStream != null) {
                tarArchiveOutputStream.finish();
                tarArchiveOutputStream.close();
            }
            if (gzipCompressorOutputStream != null) {
                gzipCompressorOutputStream.close();
            }
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    private void addNewEntryToArchive(final Path tarArchiveEntry,
                                      final TarArchiveOutputStream os,
                                      String entryFileName) throws IOException {
        if (!Files.isHidden(tarArchiveEntry)) {
            entryFileName = entryFileName + tarArchiveEntry.getFileName();
            // E.g. entryFileName = "resources/images/" +  "ico_type_family_small.png"
            // OR e.g. entryFileName = "resources/" + "css"
            TarArchiveEntry tarEntry = (TarArchiveEntry) os.createArchiveEntry(tarArchiveEntry.toFile(), entryFileName);

            os.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            os.putArchiveEntry(tarEntry);

            if (Files.isRegularFile(tarArchiveEntry)) {
                os.write(Files.readAllBytes(tarArchiveEntry));
                os.flush();
                os.closeArchiveEntry();
            } else if (Files.isDirectory(tarArchiveEntry)) {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tarArchiveEntry)) {
                    for (Path child : directoryStream) {
                        addNewEntryToArchive(child, os, entryFileName + File.separator);
                    }
                }
            }
        }
    }

    /**
     * Builds a sensible tarball file name.<br>
     * e.g. for a compressed file it would be: file-name.tar.gz
     * <p/>
     * The expected file format would be <file-name>.<extension>
     *
     * @param fileName                Input filename without extension
     * @param archiveHtmlAndSVGOutput If TRUE add tar extension
     * @param compressHtmlOutput      If TRU
     * @return Tarball filename with extension added
     */
    public static String buildTarArchiveName(final String fileName,
                                       final boolean archiveHtmlAndSVGOutput,
                                       final boolean compressHtmlOutput,
                                       final FileOutputFormat outputFormat) {
        if (fileName == null) {
            throw new IllegalStateException("HTML/SVG output file name was NULL");
        } else if (fileName.length() == 0) {
            throw new IllegalStateException("HTML/SVG output file name was empty");
        }

        StringBuffer fileExtension = new StringBuffer();
        if (outputFormat.equals(FileOutputFormat.SVG)) {
            fileExtension.append(archiveHtmlAndSVGOutput ? ".tar" : "");
            fileExtension.append((archiveHtmlAndSVGOutput && compressHtmlOutput) ? ".gz" : "");
        } else if (outputFormat.equals(FileOutputFormat.HTML)) {
            fileExtension.append(compressHtmlOutput ? ".tar.gz" : ".tar");
        }

        if (fileName.endsWith(fileExtension.toString())) {
            return fileName;
        }
        return fileName + fileExtension.toString();
    }
}
