package com.ogerardin.xplane.test.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.navdata.Arinc424DataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/** Equality contract for {@link Arinc424DataSet}: the role is part of the value. */
class Arinc424DataSetEqualityTest {

    @TempDir
    Path xplaneRoot;

    @Test
    void sameRoleAndFolderAreEqual() throws Exception {
        var a = dataSet(Arinc424DataSet.Role.FAA_APPROACHES);
        var b = dataSet(Arinc424DataSet.Role.FAA_APPROACHES);

        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    void differentRolesAreNotEqual() throws Exception {
        var faa = dataSet(Arinc424DataSet.Role.FAA_APPROACHES);
        var override = dataSet(Arinc424DataSet.Role.SIM_WIDE_OVERRIDE);

        assertThat(faa, not(override));
    }

    private Arinc424DataSet dataSet(Arinc424DataSet.Role role) throws Exception {
        return new Arinc424DataSet("Test", "test", new XPlane(xplaneRoot),
                xplaneRoot.resolve("Custom Data"), role, "FAACIFP18");
    }
}
