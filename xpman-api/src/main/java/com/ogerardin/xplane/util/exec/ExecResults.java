package com.ogerardin.xplane.util.exec;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Hold the results of running a command through {@link CommandExecutor#exec()}
 * @param process the {@link Process} instance returned by {@link Runtime#exec}
 * @param outputLines capture of the command's standard output
 * @param errorLines capture of the command's standard error
 */
public record ExecResults(Process process, List<String> outputLines, List<String> errorLines) {

    public int getExitValue() {
        return process.exitValue();
    }

    public boolean isSuccessful() {
        return getExitValue() == 0;
    }

    /**
     * If this represents the results of a command that failed, throws an exception obtained from the specified supplier.
     */
    public ExecResults orThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
        return or((results) -> {
            throw exceptionSupplier.get();
        });
    }

    /**
     * If this represents the results of a command that failed, throws a {@link RuntimeException} with a message
     * containing the command line and the exit status.
     */
    public ExecResults orThrow() {
        return orThrow(() -> new RuntimeException("Command failed with non-zero exit status: " + process.info().commandLine()));
    }

    /**
     * If this represents the results of a command that failed, invokes the specified consumer passing this as argument.
     */
    public ExecResults or(Consumer<ExecResults> consumer) {
        if (! isSuccessful()) {
            consumer.accept(this);
        }
        return this;
    }
}
