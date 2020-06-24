package com.ogerardin.xplane.file.data;

import lombok.Data;
import lombok.experimental.Delegate;

@Data
public class XPlaneFileData {
    @Delegate
    final Header header;
}
