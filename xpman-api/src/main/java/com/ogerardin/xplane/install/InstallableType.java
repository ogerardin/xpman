package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

/**
 * Represents a type of content that can be installed into X-Plane.
 * Each implementation knows how to recognize itself in an archive,
 * check preconditions, and perform installation.
 */
public interface InstallableType {
    

    /**
     * Check if the given archive contains content matching this type.
     * 
     * @param archive the archive to check
     * @return true if this archive matches this installable type
     */
    boolean recognizes(Archive archive);
    
    /**
     * Check preconditions for installation.
     * 
     * @param xPlane the X-Plane installation context
     * @param archive the archive to be installed
     * @return inspection result with any errors or warnings
     */
    InspectionResult preconditions(XPlane xPlane, Archive archive);
    
    /**
     * Install the archive contents into X-Plane.
     * 
     * @param xPlane the X-Plane installation context
     * @param archive the archive to install
     * @param progress progress listener for reporting installation progress
     * @throws InstallationException if installation fails
     */
    void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException;
}
