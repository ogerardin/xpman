package com.ogerardin.xplane.exec;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Provides a convenient way to run a command and get its console output as a List of Strings.
 * The command and parameters to execute can be provided as either a String array through {@link #cmdarray} or a
 * command line as a single String through {@link #command}.
 * Appropriate for short-lived commands only.
 */
@Slf4j
@ToString
@Builder
public class ProcessExecutor {

    private final String command;
    private final String[] cmdarray;
    private final Path dir;
    private final String[] envp;
    private final String stdin;

    public ExecResults exec() throws IOException, InterruptedException {
        Path actualDir = (dir != null) ? dir : Paths.get(".").toAbsolutePath();
        Process process;
        if (cmdarray != null) {
            log.info("Executing {}", Arrays.asList(cmdarray));
            process = Runtime.getRuntime().exec(cmdarray, envp, actualDir.toFile());
        } else {
            log.info("Executing '{}'", command);
            process = Runtime.getRuntime().exec(command, envp, actualDir.toFile());
        }
        if (stdin != null) {
            new OutputStreamWriter(process.getOutputStream()).write(stdin);
        }
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());

        int exitValue = process.waitFor();

        log.debug("Process exited with value {}", exitValue);

        return new ExecResults(process, outputGobbler.getLines(), errorGobbler.getLines());
    }

    /**
     * A quick way to execute a command without using the builder.
     */
    public static ExecResults exec(String... args) throws IOException, InterruptedException {
        return builder()
                .cmdarray(args)
                .build().exec();
    }


}
