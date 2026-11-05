package com.ogerardin.xpman.util.test;

import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.util.jfx.TableColumnWidthHelper;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TableColumnWidthHelperTest {

    // instantiating JavaFX controls requires the toolkit; tolerate one already started by another test class
    @BeforeAll
    static void startToolkit() {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX requires a display");
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException _) {
            // toolkit already initialized in this JVM
        }
    }

    private static XPManPrefs prefsWith(Map<String, Double> widths) {
        XPManPrefs prefs = new XPManPrefs();
        prefs.setTableColumnWidths(new HashMap<>(widths));
        TableColumnWidthHelper.initialize(prefs);
        return prefs;
    }

    private static TableColumn<String, String> column(String id, double prefWidth) {
        TableColumn<String, String> column = new TableColumn<>();
        column.setId(id);
        column.setPrefWidth(prefWidth);
        return column;
    }

    private static TreeTableColumn<String, String> treeColumn(String id, double prefWidth) {
        TreeTableColumn<String, String> column = new TreeTableColumn<>();
        column.setId(id);
        column.setPrefWidth(prefWidth);
        return column;
    }

    @Test
    void restoresSavedWidthsByIdRegardlessOfColumnOrder() {
        prefsWith(Map.of("scenery.nameColumn", 250.0));
        TableView<String> table = new TableView<>();
        TableColumn<String, String> other = column("otherColumn", 100);
        TableColumn<String, String> name = column("nameColumn", 100);
        table.getColumns().addAll(other, name);

        TableColumnWidthHelper.setup("scenery", table.getColumns());

        assertThat(name.getPrefWidth(), is(250.0));
        assertThat(other.getPrefWidth(), is(100.0)); // no saved width -> FXML default kept
    }

    @Test
    void restoresTreeTableColumnWidths() {
        prefsWith(Map.of("acfTree.valueColumn", 333.0));
        TreeTableView<String> table = new TreeTableView<>();
        TreeTableColumn<String, String> value = treeColumn("valueColumn", 200);
        table.getColumns().add(value);

        TableColumnWidthHelper.setup("acfTree", table.getColumns());

        assertThat(value.getPrefWidth(), is(333.0));
    }

    @Test
    void recordsPrefWidthChanges() {
        XPManPrefs prefs = prefsWith(Map.of());
        TableView<String> table = new TableView<>();
        TableColumn<String, String> name = column("nameColumn", 100);
        table.getColumns().add(name);
        TableColumnWidthHelper.setup("plugins", table.getColumns());

        name.setPrefWidth(150);

        assertThat(prefs.getTableColumnWidths(), hasEntry("plugins.nameColumn", 150.0));
    }

    @Test
    void recordsWidthFromUserResize() {
        XPManPrefs prefs = prefsWith(Map.of());
        TableView<String> table = new TableView<>();
        TableColumn<String, String> name = column("nameColumn", 100);
        table.getColumns().add(name);
        TableColumnWidthHelper.setup("plugins", table.getColumns());

        // the same call the column-header drag handler makes: TableUtil.resize -> doSetWidth (width, not prefWidth)
        table.resizeColumn(name, 50);

        assertThat(name.getWidth(), is(150.0));
        assertThat(prefs.getTableColumnWidths(), hasEntry("plugins.nameColumn", 150.0));
    }

    @Test
    void recordsTreeTableWidthFromUserResize() {
        XPManPrefs prefs = prefsWith(Map.of());
        TreeTableView<String> table = new TreeTableView<>();
        TreeTableColumn<String, String> value = treeColumn("valueColumn", 200);
        table.getColumns().add(value);
        TableColumnWidthHelper.setup("acfTree", table.getColumns());

        table.resizeColumn(value, -60);

        assertThat(value.getWidth(), is(140.0));
        assertThat(prefs.getTableColumnWidths(), hasEntry("acfTree.valueColumn", 140.0));
    }

    @Test
    void sameColumnIdInDifferentTablesStaysIndependent() {
        XPManPrefs prefs = prefsWith(Map.of("scenery.nameColumn", 250.0));
        TableColumn<String, String> sceneryName = column("nameColumn", 100);
        TableView<String> sceneryTable = new TableView<>();
        sceneryTable.getColumns().add(sceneryName);
        TableColumn<String, String> rulesName = column("nameColumn", 100);
        TableView<String> rulesTable = new TableView<>();
        rulesTable.getColumns().add(rulesName);

        TableColumnWidthHelper.setup("scenery", sceneryTable.getColumns());
        TableColumnWidthHelper.setup("sceneryRules", rulesTable.getColumns());
        rulesName.setPrefWidth(400);

        assertThat(sceneryName.getPrefWidth(), is(250.0));
        assertThat(prefs.getTableColumnWidths(), hasEntry("sceneryRules.nameColumn", 400.0));
    }

    @Test
    void clampsSavedWidthToColumnBounds() {
        prefsWith(Map.of(
                "scenery.narrow", 5.0,    // below min
                "scenery.wide", 5000.0)); // above max
        TableView<String> table = new TableView<>();
        TableColumn<String, String> narrow = column("narrow", 100);
        narrow.setMinWidth(50);
        TableColumn<String, String> wide = column("wide", 100);
        wide.setMaxWidth(300);
        table.getColumns().addAll(narrow, wide);

        TableColumnWidthHelper.setup("scenery", table.getColumns());

        assertThat(narrow.getPrefWidth(), is(50.0));
        assertThat(wide.getPrefWidth(), is(300.0));
    }

    @Test
    void ignoresInvalidSavedWidthsAndObsoleteEntries() {
        Map<String, Double> invalid = new HashMap<>();
        invalid.put("scenery.zero", 0.0);
        invalid.put("scenery.negative", -3.0);
        invalid.put("scenery.nan", Double.NaN);
        invalid.put("scenery.stale", 120.0); // no such column anymore
        prefsWith(invalid);
        TableView<String> table = new TableView<>();
        TableColumn<String, String> zero = column("zero", 100);
        TableColumn<String, String> negative = column("negative", 100);
        TableColumn<String, String> nan = column("nan", 100);
        table.getColumns().addAll(zero, negative, nan);

        TableColumnWidthHelper.setup("scenery", table.getColumns());

        assertThat(zero.getPrefWidth(), is(100.0));
        assertThat(negative.getPrefWidth(), is(100.0));
        assertThat(nan.getPrefWidth(), is(100.0));
    }

    @Test
    void ignoresColumnsWithoutId() {
        XPManPrefs prefs = prefsWith(Map.of());
        TableView<String> table = new TableView<>();
        TableColumn<String, String> anonymous = column(null, 100);
        TableColumn<String, String> blank = column("", 90);
        table.getColumns().addAll(anonymous, blank);
        TableColumnWidthHelper.setup("scenery", table.getColumns());

        anonymous.setPrefWidth(180);
        blank.setPrefWidth(160);

        assertThat(prefs.getTableColumnWidths(), anEmptyMap());
        assertThat(anonymous.getPrefWidth(), is(180.0));
    }

    @Test
    void setupWithoutInitializationIsNoOp() {
        TableColumnWidthHelper.initialize(null); // standalone use, no XPmanFX
        TableView<String> table = new TableView<>();
        TableColumn<String, String> name = column("nameColumn", 100);
        table.getColumns().add(name);

        TableColumnWidthHelper.setup("scenery", table.getColumns());
        name.setPrefWidth(200);

        assertThat(name.getPrefWidth(), is(200.0)); // no crash, nothing recorded
    }
}
