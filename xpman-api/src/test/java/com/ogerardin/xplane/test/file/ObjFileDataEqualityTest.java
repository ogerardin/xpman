package com.ogerardin.xplane.test.file;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.obj.ObjFileData;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/** Equality contract for {@link ObjFileData}: parsed content is part of the value. */
class ObjFileDataEqualityTest {

    private static final Header HEADER = new Header("I", "800", "OBJ");

    @Test
    void identicalHeadersAndContentAreEqual() {
        var a = objFileData();
        var b = objFileData();

        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    void sameHeaderWithDifferentAttributesIsNotEqual() {
        var a = objFileData();
        var b = objFileData();
        b.getAttributes().add(new com.ogerardin.xplane.file.data.obj.ObjTexture("TEXTURE", "test.png"));

        assertThat(a, not(b));
    }

    @Test
    void sameHeaderWithDifferentDataIsNotEqual() {
        var a = objFileData();
        var b = objFileData();
        b.getData().add(new com.ogerardin.xplane.file.data.obj.ObjDatum());

        assertThat(a, not(b));
    }

    private static ObjFileData objFileData() {
        return new ObjFileData(HEADER);
    }
}
