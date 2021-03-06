/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.epirus.console.project.kotlin;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import io.epirus.console.project.ProjectCreator;
import io.epirus.console.project.utils.ClassExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import static java.io.File.separator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KotlinProjectCreatorTest extends ClassExecutor {
    private static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private InputStream inputStream;
    private static String tempDirPath;
    @TempDir static Path temp;

    @BeforeAll
    static void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        tempDirPath = temp.toString();
    }

    @Test
    @Order(1)
    public void testWhenCorrectArgsArePassedProjectStructureCreated() {
        final String[] args = {"-p=org.com", "-n=Test", "-o=" + tempDirPath};
        final KotlinProjectCreatorCLIRunner kotlinProjectCreatorCLIRunner =
                new KotlinProjectCreatorCLIRunner();
        new CommandLine(kotlinProjectCreatorCLIRunner).parseArgs(args);
        assert kotlinProjectCreatorCLIRunner.packageName.equals("org.com");
        assert kotlinProjectCreatorCLIRunner.projectName.equals("Test");
        assert kotlinProjectCreatorCLIRunner.outputDir.equals(tempDirPath);
    }

    @Test
    @Order(2)
    public void testWithPicoCliWhenArgumentsAreCorrect() throws IOException, InterruptedException {
        final String[] args = {"-p", "org.com", "-n", "Test", "-o" + tempDirPath};
        int exitCode =
                executeClassAsSubProcessAndReturnProcess(
                                ProjectCreator.class, Collections.emptyList(), Arrays.asList(args))
                        .inheritIO()
                        .start()
                        .waitFor();
        assertEquals(0, exitCode);
    }

    @Test
    @Order(3)
    public void verifyThatTestsAreGenerated() {
        final File pathToTests =
                new File(
                        String.join(
                                separator,
                                tempDirPath,
                                "Test",
                                "src",
                                "test",
                                "kotlin",
                                "org",
                                "com",
                                "generated",
                                "contracts",
                                "HelloWorldTest.kt"));
        assertTrue(pathToTests.exists());
    }

    @Test
    public void testWithPicoCliWhenArgumentsAreEmpty() throws Exception {
        final String[] args = {"-n=", "-p="};
        ProjectCreator.main(args);
        assertEquals(
                outContent.toString(), "Please make sure the required parameters are not empty.\n");
    }

    @Test
    public void testWhenInteractiveAndArgumentsAreCorrect()
            throws IOException, InterruptedException {
        final String[] args = {"new", "kotlin"};
        Process process =
                executeClassAsSubProcessAndReturnProcess(
                                ProjectCreator.class, Collections.emptyList(), Arrays.asList(args))
                        .start();
        BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write("test", 0, "test".length());
        writer.newLine();
        writer.write("org.com", 0, "org.com".length());
        writer.newLine();
        writer.write(tempDirPath, 0, tempDirPath.length());
        writer.newLine();
        writer.close();
        process.waitFor();
        assertEquals(0, process.exitValue());
    }
}
