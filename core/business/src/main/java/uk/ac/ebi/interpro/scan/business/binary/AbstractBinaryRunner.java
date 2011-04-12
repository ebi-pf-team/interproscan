/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.interpro.scan.business.binary;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Default implementation.
 *
 * @author Antony Quinn
 * @version $Id$
 */
abstract class AbstractBinaryRunner implements BinaryRunner {

    private static final Logger LOGGER = Logger.getLogger(AbstractBinaryRunner.class.getName());


    private CommandLineConversation commandLineConversation;
    private String binary;
    private Resource binaryPath;
    private String arguments;
    private String temporaryFilePath = null;
    private boolean deleteTemporaryFiles = true;

    @Override
    public CommandLineConversation getCommandLineConversation() {
        return commandLineConversation;
    }

    @Override
    public void setCommandLineConversation(CommandLineConversation commandLineConversation) {
        this.commandLineConversation = commandLineConversation;
    }

    @Override
    public String getBinary() {
        return binary;
    }

    @Override
    public void setBinary(String binary) {
        this.binary = binary;
    }

    @Override
    public Resource getBinaryPath() {
        return binaryPath;
    }

    @Override
    public void setBinaryPath(Resource binaryPath) {
        this.binaryPath = binaryPath;
    }

    @Override
    public String getArguments() {
        return arguments;
    }

    @Override
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean isDeleteTemporaryFiles() {
        return deleteTemporaryFiles;
    }

    @Override
    public void setDeleteTemporaryFiles(boolean deleteTemporaryFiles) {
        this.deleteTemporaryFiles = deleteTemporaryFiles;
    }

    @Override
    public String getTemporaryFilePath() {
        return temporaryFilePath;
    }

    @Override
    public void setTemporaryFilePath(String temporaryFilePath) {
        this.temporaryFilePath = temporaryFilePath;
    }

    @Override
    public InputStream run() throws IOException {
        return runCommand(buildCommand(null));
    }

    @Override
    public InputStream run(String additionalArguments) throws IOException {
        return runCommand(buildCommand(additionalArguments));
    }

    private InputStream runCommand(String command) throws IOException {
        // Store results in temporary file
        File file;
        file = (temporaryFilePath == null)
                ? File.createTempFile("ipr-", ".out")
                : new File(temporaryFilePath);

        commandLineConversation.setOutputPathToFile(file.getAbsolutePath(), true, false);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("About to run command line: " + command);
        }
        final int exitCode = commandLineConversation.runCommand(command);
        if (exitCode != 0) {
            LOGGER.error("Command exited with non-zero exit status.");
            throw new IllegalStateException("Command " + command + " exited with exit status " + exitCode);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Command COMPLETED SUCCESSFULLY.  Returning FileInputStream on output file.");
        }
        return new FileInputStream(file);
    }

    private String buildCommand(String additionalArguments) throws IOException {

        String command = binary;

        if (binaryPath != null) {
            command = binaryPath.getFile().getCanonicalPath() + File.separator + command;
        }

        if (arguments != null) {
            command = command + " " + arguments;
        }

        if (additionalArguments != null) {
            command = command + " " + additionalArguments;
        }

        if (LOGGER.isDebugEnabled()) LOGGER.debug("Command being run (not including single quotes): '" + command + "'");

        return command;
    }

}
