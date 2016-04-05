//package uk.ac.ebi.interpro.scan.io.sequence;
//
//import org.springframework.core.io.Resource;
//import uk.ac.ebi.interpro.scan.model.Protein;
//import uk.ac.ebi.interpro.scan.model.ProteinXref;
//
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Collection;
//
///**
// * Write FASTA file.
// * TODO: Shouldn't there be only 1 fasta file writer ({@link uk.ac.ebi.interpro.scan.business.sequence.fasta.FastaFileWriter})?
// *
// * @author Antony Quinn
// * @version $Id$
// */
//public final class FastaSequenceWriter implements SequenceWriter {
//
//    // NOTE: Use Java NIO if profiling identifies I/O as bottleneck
//    // NOTE: [http://java.sun.com/docs/books/tutorial/essential/io/file.html]
//
//    private static final String ID_LINE_SEPARATOR = "|";
//    private static final int DEFAULT_WIDTH = 60;
//
//    private final int width;
//    private final boolean idNullable;
//    private final boolean addXrefs;
//
//    public FastaSequenceWriter() {
//        this(false, DEFAULT_WIDTH, false);
//    }
//
//    public FastaSequenceWriter(int width) {
//        this(false, width, false);
//    }
//
//    public FastaSequenceWriter(boolean idNullable) {
//        this(false, DEFAULT_WIDTH, idNullable);
//    }
//
//    public FastaSequenceWriter(boolean idNullable, int width, boolean addXrefs) {
//        if (width < 1) {
//            throw new IllegalArgumentException("Width must be greater than 0 (width: " + width + ")");
//        }
//        this.width = width;
//        this.idNullable = idNullable;
//        this.addXrefs = addXrefs;
//    }
//
//    @Override
//    public void write(Collection<Protein> proteins, Resource resource) throws IOException {
//        if (proteins == null) {
//            throw new IllegalArgumentException("Proteins collection must not be null");
//        }
//        if (resource == null) {
//            throw new IllegalArgumentException("Resource must not be null");
//        }
//        if (resource.exists()) {
//            // TODO: Decide if we should delete existing file or throw exception
//            if (resource.isOpen()) {
//                throw new IllegalStateException("Resource is already open: " + resource.getDescription());
//            }
//        }
//        BufferedWriter writer = null;
//        try {
//            writer = new BufferedWriter(new FileWriter(resource.getFile(), false));
//            for (Protein p : proteins) {
//                writer.write(createRecord(p).toString());
//            }
//        } catch (IOException e) {
//            throw new IOException("Could not write to resource: " + resource.getDescription(), e);
//        } finally {
//            if (writer != null) {
//                writer.close();
//            }
//        }
//    }
//
//    @Override
//    public int getWidth() {
//        return width;
//    }
//
//    @Override
//    public boolean isIdNullable() {
//        return idNullable;
//    }
//
//    @Override
//    public boolean isAddXrefs() {
//        return addXrefs;
//    }
//
//    private StringBuilder createRecord(Protein p) {
//        final String RECORD_START = ">";
//        final String NEW_LINE = "\n";
//        StringBuilder record = new StringBuilder(RECORD_START);
//        // Add ID
//        if (p.getId() == null) {
//            if (idNullable) {
//                // Use MD5 instead of ID
//                record.append(p.getMd5());
//            } else {
//                throw new NullPointerException("Protein ID is null: " + p.toString());
//            }
//        } else {
//            record.append(p.getId().toString());
//        }
//        // Add xrefs
//        if (addXrefs && p.getCrossReferences().size() > 0) {
//            record.append(ID_LINE_SEPARATOR);
//            for (ProteinXref xref : p.getCrossReferences()) {
//                record.append(xref.getIdentifier());
//                record.append(" ");
//            }
//        }
//        record.append(NEW_LINE);
//        // Add sequence
//        if (p.getSequence().length() > width) {
//            int count = 0;
//            boolean wrap = false;
//            for (char c : p.getSequence().toCharArray()) {
//                record.append(c);
//                count++;
//                // Word wrap every N chars
//                wrap = (count % width == 0);
//                if (wrap) {
//                    record.append(NEW_LINE);
//                }
//            }
//            if (!wrap) {
//                record.append(NEW_LINE);
//            }
//        } else {
//            record.append(p.getSequence());
//            record.append(NEW_LINE);
//        }
//        return record;
//    }
//
//}
