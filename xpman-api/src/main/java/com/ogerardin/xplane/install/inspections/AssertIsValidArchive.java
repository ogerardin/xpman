package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.PredicateInspection;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * A {@link PredicateInspection} that produces an aborting error if the source zip is not a valid archive.
 */
@RequiredArgsConstructor
public enum AssertIsValidArchive implements Inspection<Archive> {

    INSTANCE(new PredicateInspection<>(
            Archive::isValidArchive,
            () -> InspectionMessage.error("File is not a valid zip archive", true)
    ));

    @Delegate
    private final Inspection<Archive> delegate;
}
