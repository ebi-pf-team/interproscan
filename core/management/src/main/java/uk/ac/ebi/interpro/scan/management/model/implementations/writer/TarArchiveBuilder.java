package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;

import java.io.*;
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

    private final List<File> tarArchiveEntries = new ArrayList<File>();

    private final File tarArchive;

    private boolean compress;

    public TarArchiveBuilder(File tarArchiveEntry, File tarArchive) {
        this(tarArchiveEntry, tarArchive, false);
    }

    public TarArchiveBuilder(File tarArchiveEntry, File tarArchive, boolean compress) {
        this.tarArchiveEntries.add(tarArchiveEntry);
        this.tarArchive = tarArchive;
        this.compress = compress;
    }

    public TarArchiveBuilder(List<File> tarArchiveEntries, File tarArchive) {
        this(tarArchiveEntries, tarArchive, false);
    }

    public TarArchiveBuilder(List<File> tarArchiveEntries, File tarArchive, boolean compress) {
        this.tarArchiveEntries.addAll(tarArchiveEntries);
        this.tarArchive = tarArchive;
        this.compress = compress;
    }

    public void buildTarArchive() throws IOException {
        // E.g. for "-b OUT" tarArchive = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT.html.tar.gz"
        if (tarArchive != null) {
            tarArchive.createNewFile();
        }
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        GzipCompressorOutputStream gzipCompressorOutputStream = null;
        TarArchiveOutputStream tarArchiveOutputStream = null;
        //
        try {
            fileOutputStream = new FileOutputStream(tarArchive);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            if (compress) {
                gzipCompressorOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
                tarArchiveOutputStream = new TarArchiveOutputStream(gzipCompressorOutputStream);

            } else {
                tarArchiveOutputStream = new TarArchiveOutputStream(bufferedOutputStream);
            }

            for (File entry : tarArchiveEntries) {
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

    private void addNewEntryToArchive(final File tarArchiveEntry,
                                      final TarArchiveOutputStream os,
                                      String entryFileName) throws IOException {
        if (!tarArchiveEntry.isHidden()) {
            entryFileName = entryFileName + tarArchiveEntry.getName();
            TarArchiveEntry tarEntry = (TarArchiveEntry) os.createArchiveEntry(tarArchiveEntry, entryFileName);

            os.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            os.putArchiveEntry(tarEntry);

            if (tarArchiveEntry.isFile()) {
//                IOUtils.copy(new FileInputStream(tarArchiveEntry), os);
                os.write(getBytesFromFile(tarArchiveEntry));
                os.flush();
                os.closeArchiveEntry();
            } else if (tarArchiveEntry.isDirectory()) {
                File[] children = tarArchiveEntry.listFiles();
                if (children != null) {
                    for (File child : children) {
                        addNewEntryToArchive(new File(child.getAbsolutePath()), os, entryFileName + File.separator);
                    }
                }
            }
        }
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IllegalStateException("Could not completely read the file " + file.getName());
        }
        is.close();
        return bytes;
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
