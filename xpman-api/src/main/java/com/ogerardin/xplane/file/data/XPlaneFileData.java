package com.ogerardin.xplane.file.data;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Delegate;

@Data
@ToString
public class XPlaneFileData {
    @ToString.Include
    @Delegate
    final Header header;
}
