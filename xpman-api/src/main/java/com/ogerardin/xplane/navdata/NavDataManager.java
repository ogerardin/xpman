package com.ogerardin.xplane.navdata;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.install.InstallableArchive;
import com.ogerardin.xplane.install.ProgressListener;
import com.ogerardin.xplane.util.AsyncHelper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ogerardin.xplane.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.ManagerEvent.Type.LOADING;

@Slf4j
@ToString
public class NavDataManager extends Manager<NavDataSet> implements InstallTarget {

    public NavDataManager(XPlane xPlane) {
        super(xPlane);
    }

    public List<NavDataSet> getNavDataSets() {
        if (items == null) {
            loadNavDataSets();
        }
        return Collections.unmodifiableList(items);
    }

    public void reload() {
        AsyncHelper.runAsync(this::loadNavDataSets);
    }


    public void loadNavDataSets() {

        log.info("Loading nav data sets...");
        fireEvent(ManagerEvent.<NavDataSet>builder().type(LOADING).source(this).build());

        // See https://developer.x-plane.com/article/navdata-in-x-plane-11
        items = Stream.of(
                simWideOverride(),
                baseNavData(),
                updatedBaseNavData(),
                faaUpdatedApproaches(),
                handPlacedLocalizers(),
                userData()
        ).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

        log.info("Loaded {} nav data sets", items.size());
        fireEvent( ManagerEvent.<NavDataSet>builder().type(LOADED).source(this).items(items).build());
    }

    private NavDataSet simWideOverride() {
        return new Arinc424DataSet("Sim-wide ARINC424 override",
                "<h3><span id=\"Sim-wide_ARINC424_override\">Sim-wide ARINC424 override</span></h3>\n" +
                        "<p>Professional customers with access to 424 master files can use them to override the X-Plane global database.</p>\n" +
                        "<p>Upon sim start, X-Plane will examine the <strong>$X-Plane/Custom Data/</strong> folder of its installation for a file named <strong>earth_424.dat</strong>. If this file is found, it will be interpreted according to the ARINC 424.18 standard with the FAA CIFP exceptions, and will be used to load the following information into X-Plane:</p>\n" +
                        "<ul>\n" +
                        "<li>Fixes (EA and PC records)</li>\n" +
                        "<li>Navaids (D, DB and PN records)</li>\n" +
                        "<li>Airways (ER records)</li>\n" +
                        "<li>Published Holdings (EP records)</li>\n" +
                        "<li>Minimum Sector Altitude (PS records)</li>\n" +
                        "<li>Minimum Off Route Altitudes (AS records)</li>\n" +
                        "<li>ILS (PI records)</li>\n" +
                        "<li>Markers (PM records)</li>\n" +
                        "<li>Airport data (PA records)</li>\n" +
                        "<li>Airport gate/parking locations (PB records)</li>\n" +
                        "<li>Airport terminal procedures (PD, PE, PF records)</li>\n" +
                        "<li>Airport runway information (PG records)</li>\n" +
                        "<li>Path points (PP records)</li>\n" +
                        "<li>GLS stations (PT records)</li>\n" +
                        "    <li>GBAS path points (PQ records)</li>\n" +
                        "</ul>\n" +
                        "<p>After this file has been read, X-Plane will not load any other information from other text files. It is assumed that when the installation is provided with a global 424 file, no data of any other format needs to be loaded. In particular, X-Plane will then NOT load any of the files described in the following as &#8220;Global data&#8221;.</p>\n",
                xPlane, xPlane.getPaths().customData(), "earth_424.dat");
    }

    private NavDataSet baseNavData() {
        return new XPNavDataSet("Base (shipped with X-Plane)",
                "<h3><span id=\"The_base_-_what_is_shipped_with_X-Plane\">The base &#8211; what is shipped with X-Plane:</span></h3>\n" +
                        "<p>X-Plane 11/12 ships with a global base layer of data that enables IFR navigation world-wide. The data cycle represented by those files will remain the same over the lifetime of X-Plane 12.<br />\n" +
                        "    These files are:</p>\n" +
                        "<ul>\n" +
                        "<li>earth_fix.dat</li>\n" +
                        "<li>earth_awy.dat</li>\n" +
                        "<li>earth_nav.dat</li>\n" +
                        "<li>earth_hold.dat</li>\n" +
                        "<li>earth_mora.dat</li>\n" +
                        "<li>earth_msa.dat</li>\n" +
                        "    <li>CIFP/$ICAO.dat (where $ICAO is each airport with instrument procedures)</li>\n" +
                        "</ul>\n" +
                        "<p>they are located in <strong>$X-Plane/Resources/default data/</strong>.<br />Starting with X-Plane 12, these files are also part of the default distribution:</p>\n" +
                        "<ul>\n" +
                        "<li>Resources/default data/airspaces/airspace.txt (starting with X-Plane 12)</li>\n" +
                        "    <li>Resources/default scenery/1200 atc data/Earth nav data/atc.dat (starting with X-Plane 12)</li>\n" +
                        "</ul>\n",
                xPlane, xPlane.getPaths().defaultData());
    }

    private NavDataSet updatedBaseNavData() {
        return new XPNavDataSet("Updated base (supplied by third-parties)",
                "<h3><span id=\"The_updated_base_-_what_is_supplied_by_third-party_providers\">The updated base &#8211; what is supplied by third-party providers</span></h3>\n" +
                        "<p>This layer is what advanced hobbyist users care about. They want updated data, because they want to fly online. Participation in the online networks usually requires fairly recent data. Aerosoft and Navigraph offer newest data by a monthly subscription. This data consists of the files:</p>\n" +
                        "<ul>\n" +
                        "<li>earth_fix.dat</li>\n" +
                        "<li>earth_awy.dat</li>\n" +
                        "<li>earth_nav.dat</li>\n" +
                        "<li>earth_hold.dat (starting with X-Plane 11.40)</li>\n" +
                        "<li>earth_mora.dat (starting with X-Plane 11.50)</li>\n" +
                        "<li>earth_msa.dat (starting with X-Plane 11.50)</li>\n" +
                        "<li>CIFP/$ICAO.dat (where $ICAO is each airport with instrument procedures)</li>\n" +
                        "<li>airspaces/airspace.txt (starting with X-Plane 12)</li>\n" +
                        "    <li>1200 atc data/Earth nav data/atc.dat (starting with X-Plane 12)</li>\n" +
                        "</ul>\n" +
                        "<p>they must be located in <strong>$X-Plane/Custom Data/</strong></p>\n" +
                        "<p>These files completely replace the base layer of X-Plane. If these files are present, the X-Plane base layer is ignored. Note that because of the referential integrity, it is not possible to update just the earth_fix.dat and not the earth_awy.dat. Upon load, it is checked that all files are of the same cycle number. Mix-matching different cycles is not supported.</p>\n",
                xPlane, xPlane.getPaths().customData());
    }

    private NavDataSet faaUpdatedApproaches() {
        return new Arinc424DataSet("FAA updated approaches",
                "<h3><span id=\"The_updated_approaches_-_what_we_get_from_the_FAA_for_free\">The updated approaches &#8211; what we get from the FAA for free</span></h3>\n" +
                        "<p>The FAACIFP file is an ARINC424.18 file provided by the Federal Aviation Administration free of charge and can be downloaded from their website.</p>\n" +
                        "<p>In X-Plane 11/12, this file is used to replace P* records with the latest from the FAA. The following data is read from this file, and overrides data loaded from the global layer:</p>\n" +
                        "<ul>\n" +
                        "<li>Terminal Fixes (PC records)</li>\n" +
                        "<li>Terminal Navaids (D records where the 5.6 field is not empty, PN records)</li>\n" +
                        "<li>ILS (PI records)</li>\n" +
                        "<li>Markers (PM records)</li>\n" +
                        "<li>Airport data (PA records)</li>\n" +
                        "<li>Airport gate/parking locations (PB records)</li>\n" +
                        "<li>Airport terminal procedures (PD, PE, PF records)</li>\n" +
                        "<li>Airport runway information (PG records)</li>\n" +
                        "<li>Path points (PP records)</li>\n" +
                        "<li>GLS stations (PT records)</li>\n" +
                        "    <li>GBAS path points (PQ records)</li>\n" +
                        "</ul>\n" +
                        "<p>The file</p>\n" +
                        "<ul>\n" +
                        "    <li>FAACIFP18</li>\n" +
                        "</ul>\n" +
                        "<p>must be located in <strong>$X-Plane/Custom Data/</strong></p>\n" +
                        "<p>This file is not shipped with X-Plane but can be obtained from the FAA website free of charge.</p>\n" +
                        "<p>Note that no enroute waypoints, VHF enroute navaids, or enroute airways are loaded from this file. These cannot be replaced safely as it would affect the referential integrity of the airway network.</p>\n" +
                        "<p>Note that for integrity reasons, <span style=\"text-decoration: underline;\">the cycle number of the FAA data must always match the cycle number of the underlying layer</span>. Terminal procedures do reference waypoints out of the terminal area, therefore, the data source for global waypoints must be at the same cycle number.</p>\n" +
                        "<p>Note also that when FAACIFP is in effect, terminal procedures are overridden on a per-airport basis. No attempt is made to mix-match terminal procedures from global data with those loaded from FAACIFP. As terminal procedures reference terminal waypoints, trying to build terminal procedures from global data with points loaded from FACCIFP could lead to unpredictable results. Therefore, once FAACIFP is in effect, Custom Data/CIFP/$ICAO.dat is overridden for each $ICAO with PD/PE/PF records in FAACIFP.</p>\n",
                xPlane, xPlane.getPaths().customData(),"FAACIFP18");
    }

    private NavDataSet handPlacedLocalizers() {
        return new XPNavDataSet("Hand-placed localizers",
                "<h3><span id=\"Hand-placed_localizers_-_manual_corrections\">Hand-placed localizers &#8211; manual corrections</span></h3>\n" +
                        "<p>Starting with X-Plane 11.50, curated localizer data is only applied to a small number of airports. X-Plane is instead relying on the earth_nav.dat of the <a href=\"//developer.x-plane.com/wp-content/uploads/2020/03/XP-NAV1150-Spec.pdf\">XPNAV1150</a> or newer variety for almost all airports. Such data is shipped with X-Plane 11.50 by default, and also available from Navigraph and Aerosoft. Please be sure to select &#8220;11.50 and later&#8221; as the data download format if you are on X-Plane 11.50 or later. Data made for X-Plane 11.41 or earlier will not assure 1000th-of-a-degree accuracy for localizers and can thus lead to ILS signals guiding the aircraft beside the runway.</p>\n" +
                        "<p>With this data, we have currently identified 5 airports for which the necessary data quality is not assured. These will continue to be supplied from the X-Plane scenery gateway. The file</p>\n" +
                        "<ul>\n" +
                        "<li>Custom Scenery/Global Airports/Earth nav data/earth_nav.dat (X-Plane 11)</li>\n" +
                        "    <li>Global Scenery/Global Airports/Earth nav data/earth_nav.dat (X-Plane 12)</li>\n" +
                        "</ul>\n" +
                        "<p>contains these manual corrections for 5 airports. This number might increase if we find more airports with sub-par data quality.</p>\n",
                xPlane, xPlane.getPaths().handPlacedLocalizers(), "earth_nav.dat");
    }

    private NavDataSet userData() {
        return new XPNavDataSet("User data",
                "<h3><span id=\"User_data_-_per-user_overrides\">User data &#8211; per-user overrides</span></h3>\n" +
                        "<p>The last layer is the user-defined layer.</p>\n" +
                        "<p>These files are where all custom waypoints are saved. Whenever a custom waypoint is created (through the default FMS) it is saved in the user_fix.dat file, which overrides previously loaded information. The user_nav.dat can hold custom navaids, though there is no way in the X-Plane UI to create them directly.</p>\n" +
                        "<p>The files</p>\n" +
                        "<ul>\n" +
                        "<li>user_nav.dat</li>\n" +
                        "    <li>user_fix.dat</li>\n" +
                        "</ul>\n" +
                        "<p>are located in <strong>$X-Plane/Custom Data/</strong></p>\n" +
                        "<p>and are non-existent in a default installation of X-Plane and not touched by the updater.</p>\n" +
                        "<p>The user_fix.dat is first created once a pilot-defined waypoint was stored in the FMS. They are the highest layer and ensure user modifications are preserved even with updates from Aerosoft or Navigraph.</p>\n" +
                        "<p>ONLY in these user_fix.dat/user_nav.dat can you add or edit the X-Plane world data without being at risk of breaking data integrity. So if you want to add custom fixes or navaids to X-Plane, this is the only safe place to do it.</p>\n" +
                        "<p>Note that this does not work for deleting objects that were loaded in a lower layer. You can no longer delete a fix from the UI in a persistent way. If we’d allow for selective deletion of fixes or navaids, airways that might be referencing them would break.</p>\n",
                xPlane, xPlane.getPaths().customData(), "user_nav.dat", "user_fix.dat");
    }

    @Override
    public void install(InstallableArchive archive, ProgressListener progressListener) throws IOException {
        archive.installTo(xPlane.getPaths().customData(), progressListener);
        reload();
    }
}
