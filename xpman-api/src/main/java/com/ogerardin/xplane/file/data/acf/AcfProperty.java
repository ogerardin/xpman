package com.ogerardin.xplane.file.data.acf;

import lombok.Data;

/** A single property (name-value pair) extracted from an ACF file. */
@Data
public class AcfProperty {
    final String name;
    final String value;
}
