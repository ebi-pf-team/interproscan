package uk.ac.ebi.interpro.scan.io.funfam;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.io.model.HmmerModelParser;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;

public class FunFamModelParser extends AbstractModelFileParser {
    @Override
    public SignatureLibraryRelease parse() throws IOException {
        SignatureLibrary library = this.getSignatureLibrary();
        String versionNumber = this.getReleaseVersionNumber();
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, versionNumber);

        for (Resource modelFile : modelFiles) {
            Files.walkFileTree(modelFile.getFile().toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    HmmerModelParser parser = new HmmerModelParser();
                    parser.setSignatureLibrary(library);
                    parser.setReleaseVersionNumber(versionNumber);
                    parser.setModelFiles(new FileSystemResource(file.toFile()));

                    for (Signature signature : parser.parse().getSignatures()) {
                        release.addSignature(signature);
                    }

                    return FileVisitResult.CONTINUE;
                }

            });
        }

        return release;
    }
}
