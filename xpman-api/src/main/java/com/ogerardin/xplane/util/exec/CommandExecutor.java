package com.ogerardin.xplane.util.exec;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides a convenient way to run a command and get its console output as a List of Strings.
 * The command and parameters to execute can be provided as either a String array through {@link #cmdarray} or a
 * command line as a single String through {@link #command}.
 * Appropriate for short-lived commands only.
 */
@Slf4j
@ToString
@Builder
public class CommandExecutor {

    /** The command to execute as a single String. */
    private final String command;
    /** The command and parameters to execute as a String array */
    private final String[] cmdarray;
    /** Working directory to run the command in */
    private final Path dir;
    /** A set of environment variables to pass to the command, each in the form <code>name=value</code> */
    private final String[] envp;
    /** A String that will be fed to the command as its standard input */
    private final String stdin;
    /** A custom consumer to be invoked on each line read from the command's standard output */
    private final Consumer<String> outLineHandler;
    /** A custom consumer to be invoked on each line read from the command's standard error */
    private final Consumer<String> errLineHandler;

    public ExecResults exec() throws IOException, InterruptedException {
        Path actualDir = (dir != null) ? dir : Paths.get(".").toAbsolutePath();
        Process process;
        if (cmdarray != null && command != null) {
            throw new IllegalArgumentException("Only one of cmdarray or command can be specified");
        }
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

        List<String> outLines = startStreamGobbler(process.getInputStream(), outLineHandler);
        List<String> errLines = startStreamGobbler(process.getErrorStream(), errLineHandler);

        int exitValue = process.waitFor();

        log.debug("Process exited with value {}", exitValue);

        return new ExecResults(process, outLines, errLines);
    }

    private List<String> startStreamGobbler(InputStream inputStream, Consumer<String> customHandler) {
        List<String> outLines = new ArrayList<>();
        Consumer<String> handler = outLines::add;
        if (customHandler != null) {
            handler = handler.andThen(customHandler);
        }
        new StreamGobbler(inputStream, handler);
        return outLines;
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
