package com.ogerardin.xplane.file.data.dat;

import lombok.Data;

/** Header metadata parsed from a DAT file (origin, spec version, data cycle, build, metadata). */
@Data
public class DatHeader {
    final String origin;
    final String specVersion;
    final String dataCycle;
    final String build;
    final String metadata;
}
