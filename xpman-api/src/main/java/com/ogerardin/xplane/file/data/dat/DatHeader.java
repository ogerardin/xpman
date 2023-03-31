package com.ogerardin.xplane.file.data.dat;

import lombok.Data;

@Data
public class DatHeader {
    final String origin;
    final String specVersion;
    final String dataCycle;
    final String build;
    final String metadata;
}
