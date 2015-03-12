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

import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a binary application.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public interface BinaryRunner {

    boolean isDeleteTemporaryFiles();

    void setDeleteTemporaryFiles(boolean deleteTemporaryFiles);

    String getTemporaryFilePath();

    void setTemporaryFilePath(String temporaryFilePath);

    /** Returns file name of binary. For example "grep".
     *
     * @return file name of binary
     */
    String getBinary();

    void setBinary(String binary);

    /**
     * Returns location of binary on file system. For example "/usr/bin"
     *
     * @return location of binary on file system.
     */
    Resource getBinaryPath();

    void setBinaryPath(Resource binaryPath);

    /**
     * Returns arguments to pass to binary. For example "-e stuff"
     *
     * @return arguments to pass to binary
     */
    String getArguments();

    void setArguments(String arguments);

    CommandLineConversation getCommandLineConversation();

    void setCommandLineConversation(CommandLineConversation commandLineConversation);

    /**
     * Return result of running command. For example "/usr/bin/grep -e stuff"
     *
     * @return result of running command
     * @throws IOException if could not run command
     */
    InputStream run() throws IOException;

    /**
     * Returns result of running command with additional arguments. For example "/usr/bin/grep -e stuff /tmp/file.txt"
     *
     * @param  additionalArguments
     * @return result of running command with additional arguments
     * @throws IOException if could not run command
     */
    InputStream run(String additionalArguments) throws IOException;

}