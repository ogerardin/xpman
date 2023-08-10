package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.PredicateInspection;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * A {@link PredicateInspection} that produces an aborting error if the source zip is not a valid archive.
 */
@RequiredArgsConstructor
public enum CheckIsValidArchive implements Inspection<Archive> {

    INSTANCE(new PredicateInspection<>(
            Archive::isValidArchive,
            () -> InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("File is not a valid zip archive")
                    .abort(true) // abort following inspections
                    .build()
    ));

    @Delegate
    private final Inspection<Archive> delegate;
}
