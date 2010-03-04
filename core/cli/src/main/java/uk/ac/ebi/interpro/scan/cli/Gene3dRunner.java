package uk.ac.ebi.interpro.scan.cli;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.persistence.SignatureDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceWriter;
import uk.ac.ebi.interpro.scan.io.sequence.FastaSequenceReader;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceReader;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceRecord;
import uk.ac.ebi.interpro.scan.business.binary.RawMatchBinaryRunner;
import uk.ac.ebi.interpro.scan.business.filter.RawMatchFilter;

import javax.xml.transform.stream.StreamResult;
import java.util.*;
import java.io.*;

/**
 * Run binary and filtering for Gene 3D.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class Gene3dRunner {

    private SequenceWriter sequenceWriter;
    private ProteinDAO proteinDao;
    private SignatureDAO signatureDao;
    private RawMatchDAO<Gene3dHmmer3RawMatch> rawMatchDao;
    private Marshaller marshaller;
    private RawMatchBinaryRunner<Gene3dHmmer3RawMatch> binaryRunner;
    private RawMatchFilter<Gene3dHmmer3RawMatch> filter;
    private boolean deleteTemporaryFiles = true;

    public void execute(Resource file, Resource hmmFile) throws IOException {

        // Parse FASTA file
        final Set<Protein> proteins = parseFasta(file);

        // Persist proteins
        proteinDao.insert(proteins);

        // Write FASTA to /tmp (use database primary keys as sequence identifiers)
        Resource resource = new FileSystemResource(File.createTempFile("ipr-", ".fasta"));
        sequenceWriter.write(proteins, resource);

        // Run HMMER
        final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins = binaryRunner.process(resource, hmmFile);

        // Delete FASTA file
        if (deleteTemporaryFiles)   {
            if (!resource.getFile().delete())   {
                System.err.println("Could not delete " + resource.getDescription());
            }
        }

        // Persist raw matches
        rawMatchDao.insertProteinMatches(rawProteins);

        // Run DomainFinder
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = filter.filter(rawProteins);        

        // Match up raw matches to proteins
        // TODO: Do this in DAO
        for (Protein p : proteins)  {
            String id = String.valueOf(p.getId());
            for (RawProtein<Gene3dHmmer3RawMatch> rp : filteredProteins)    {
                if (rp.getProteinIdentifier().equals(id))   {
                    // Convert raw matches to filtered matches
                    Collection<Hmmer3Match> filteredMatches = Hmmer3RawMatch.getMatches(rp.getMatches(), new RawMatch.Listener()  {
                        @Override public Signature getSignature(String modelAccession,
                                                                SignatureLibrary signatureLibrary,
                                                                String signatureLibraryRelease) {
                            // TODO: Look up correct Signature accession given model ID ...etc (best done in Onion)
                            Signature s = new Signature(modelAccession);
                            // TODO: In reality we wouldn't do this (signatures would be already loaded)
                            signatureDao.insert(s);
                            return s;
                        }
                    }
                    );
                    // Add matches to protein
                    for (Hmmer3Match m : filteredMatches)   {
                        p.addMatch(m);
                    }
                }
            }
        }

        // Persist filtered matches
        for (Protein p : proteins)  {
            proteinDao.insert(p);
        }

        // Print filtered results as XML
        for (Protein p : proteins)  {
            System.out.println(marshal(p));
        }

    }

    public void setSequenceWriter(SequenceWriter sequenceWriter) {
        this.sequenceWriter = sequenceWriter;
    }

    public void setProteinDao(ProteinDAO proteinDao) {
        this.proteinDao = proteinDao;
    }

    public void setRawMatchDao(RawMatchDAO<Gene3dHmmer3RawMatch> rawMatchDao) {
        this.rawMatchDao = rawMatchDao;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setSignatureDao(SignatureDAO signatureDao) {
        this.signatureDao = signatureDao;
    }

    public void setProcessor(RawMatchBinaryRunner<Gene3dHmmer3RawMatch> binaryRunner) {
        this.binaryRunner = binaryRunner;
    }

    public void setDeleteTemporaryFiles(boolean deleteTemporaryFiles) {
        this.deleteTemporaryFiles = deleteTemporaryFiles;
    }

    public void setFilter(RawMatchFilter<Gene3dHmmer3RawMatch> filter) {
        this.filter = filter;
    }    

    // Parse FASTA file
    private Set<Protein> parseFasta(Resource file) throws IOException {
        final Set<Protein> proteins = new LinkedHashSet<Protein>();
        SequenceReader reader = new FastaSequenceReader(
                new SequenceReader.Listener() {
                    @Override public void mapRecord(SequenceRecord record) {
                        proteins.add(new Protein.Builder(record.getSequence())
                                                .crossReference(new Xref(record.getId()))
                                                .build());
                    }
                }
        );
        reader.read(file.getInputStream());
        return proteins;
    }

    private String marshal(Protein protein) throws IOException  {
        Writer writer = new StringWriter();
        marshaller.marshal(protein, new StreamResult(writer));
        return writer.toString();
    }

}