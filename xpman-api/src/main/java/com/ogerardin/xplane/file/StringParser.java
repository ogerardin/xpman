package com.ogerardin.xplane.file;

/**
 * Interface for a generic parser that accepts a String and parses it into an instance of type R.
 */
@FunctionalInterface
public interface StringParser<R> {

    R parse(String contents) throws Exception;

}
