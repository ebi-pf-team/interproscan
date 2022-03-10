package uk.ac.ebi.interpro.scan.io.funfam;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.io.model.HmmerModelParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

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
    private static String prefix = "G3DSA:";

    @Override
    public SignatureLibraryRelease parse() throws IOException {
        SignatureLibraryRelease release = new SignatureLibraryRelease(this.getSignatureLibrary(), this.getReleaseVersionNumber());

        for (Resource modelFile : modelFiles) {
            Files.walkFileTree(modelFile.getFile().toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    HmmerModelParser parser = new HmmerModelParser();
                    parser.setModelFiles(new FileSystemResource(file.toFile()));

                    for (Signature s: parser.parse().getSignatures()) {
                        Set<Model> models = new HashSet<>();
                        for (Model m: s.getModels().values()) {
                            models.add(new Model(m.getAccession(), m.getName(), m.getDescription(), m.getLength()));
                        }

                        Signature ns = new Signature(FunFamModelParser.prefix + s.getAccession(), s.getName(),
                                s.getType(), s.getDescription(), s.getAbstract(), release, models);
                        release.addSignature(ns);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return release;
    }
}
