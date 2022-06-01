package uk.ac.ebi.interpro.scan.io.funfam;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.io.model.HmmerModelParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import java.util.*;

/**
 * Reads FunFam HMM files.
 * @author Matthias Blum
 */

public class FunFamModelParser extends AbstractModelFileParser {
    private final static String prefix = "G3DSA:";
    private String funfamNamesFile;

    public String getFunfamNamesFile() {
        return funfamNamesFile;
    }

    public void setFunfamNamesFile(String funfamNamesFile) {
        this.funfamNamesFile = funfamNamesFile;
    }

    @Override
    public SignatureLibraryRelease parse() throws IOException {
        SignatureLibraryRelease release = new SignatureLibraryRelease(this.getSignatureLibrary(), this.getReleaseVersionNumber());

        Map<String, String> names = this.parseNames();

        for (Resource modelFile : modelFiles) {
            Files.walkFileTree(modelFile.getFile().toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    HmmerModelParser parser = new HmmerModelParser();
                    parser.setModelFiles(new FileSystemResource(file.toFile()));

                    for (Signature s: parser.parse().getSignatures()) {
                        // e.g. 3.40.50.1170-FF-000001
                        String accession = s.getAccession();
                        String name = names.get(s.getAccession());

                        String[] parts = accession.split("-");
                        // e.g. G3DSA:3.40.50.1170:FF:000001
                        accession = FunFamModelParser.prefix + parts[0] + ":FF:" + parts[2];

                        Set<Model> models = new HashSet<>();
                        for (Model m: s.getModels().values()) {
                            models.add(new Model(m.getAccession(), m.getName(), name != null ? name : m.getDescription(), m.getLength()));
                        }

                        Signature ns = new Signature(accession, s.getName(), s.getType(), name != null ? name : s.getDescription(), s.getAbstract(), release, models);
                        release.addSignature(ns);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return release;
    }

    private Map<String, String> parseNames() throws IOException {
        Map<String, String> names = new HashMap<>();

        try (FileReader freader = new FileReader(this.getFunfamNamesFile());
             BufferedReader breader = new BufferedReader(freader)) {
            String line;

            while ((line = breader.readLine()) != null) {
                String[] values = line.split("\\s+", 2);

                if (values.length == 2 && !values[1].equals("-")) {
                    names.put(values[0], values[1]);
                }
            }

        }

        return names;
    }


}
