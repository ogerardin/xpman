package com.ogerardin.xplane.diag;

import lombok.Data;

import java.util.List;

@Data
public class DiagItem {

    DiagLevel level;

    String message;

    List<Fix> fixes;
}
