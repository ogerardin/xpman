package com.ogerardin.xpman.util.test;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks that every column of the width-persisted tables has a unique non-blank fx:id in its FXML. */
class TableColumnFxmlIdTest {

    private static final String[] FXML_FILES = {
            "src/main/resources/fxml/panels/scenery.fxml",
            "src/main/resources/fxml/panels/plugins.fxml",
            "src/main/resources/fxml/rules.fxml",
            "src/main/resources/fxml/acftree.fxml",
            "src/main/resources/fxml/inspectionResults.fxml",
            "src/main/resources/fxml/install_wizard/page2.fxml",
    };

    @Test
    void allColumnsOfPersistedTablesHaveUniqueNonBlankIds() throws Exception {
        for (String fxml : FXML_FILES) {
            assertColumnsHaveUniqueIds(Path.of(fxml));
        }
    }

    private static void assertColumnsHaveUniqueIds(Path fxml) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fxml.toFile());
        Set<String> ids = new HashSet<>();
        for (String tag : new String[]{"TableColumn", "TreeTableColumn"}) {
            NodeList columns = document.getElementsByTagName(tag);
            for (int i = 0; i < columns.getLength(); i++) {
                Element column = (Element) columns.item(i);
                String id = column.getAttribute("fx:id");
                assertFalse(id.isBlank(), fxml + ": column without fx:id");
                assertTrue(ids.add(id), fxml + ": duplicate column fx:id " + id);
            }
        }
    }
}
