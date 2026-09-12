package com.ogerardin.xplane.util.exec;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Hold the results of running a command through {@link CommandExecutor#exec()}
 * @param process the {@link Process} instance returned by {@link Runtime#exec}
 * @param command the command that was executed
 * @param outputLines capture of the command's standard output
 * @param errorLines capture of the command's standard error
 */
public record ExecResults(Process process, String command, List<String> outputLines, List<String> errorLines) {

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
     * containing the command name, exit status, and stderr output.
     */
    public ExecResults orThrow() {
        return orThrow(() -> {
            String stderr = errorLines.isEmpty() ? "" : "\n" + String.join("\n", errorLines);
            return new RuntimeException(
                String.format("Command %s failed (exit %d)%s", command, getExitValue(), stderr)
            );
        });
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
