package com.ogerardin.xpman.panels.aircraft.details;

import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xpman.util.jfx.panels.TreeTableViewLoadTask;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AcfTreeController {
    @FXML
    private TreeTableView<UiProperty> treeTableView;

    public void setAircraft(Aircraft aircraft) {
        final TreeTableViewLoadTask<UiProperty> loadTask = new TreeTableViewLoadTask<>(
                treeTableView,
                () -> new UiProperty(getPropertyTree(aircraft)),
                uiProperty -> treeItem(uiProperty.getItem())
        );
        AsyncHelper.runAsync(loadTask);
    }

    /**
     * Returns all the aircraft's properties organized as a tree
     */
    private PropertyTreeNode getPropertyTree(Aircraft aircraft) {
        final PropertyTreeNode root = new PropertyTreeNode("root");

        // aircraft properties are provided as a flat map where the key is the full path of the property
        final Map<String, String> properties = aircraft.getAcfFile().getProperties();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            // full property path, e.g. "_engn/0/_type"
            final String path = property.getKey();

            // property value, e.g. "JET"
            final String value = property.getValue();

            // the property path components, e.g. [ "_engn", "0", "_type" ]
            final String[] pathParts = path.split("/");

            // the property path components excluding the last one, which is the simple property name, e.g. [ "_engn", "0"]
            final String[] nodePath = Arrays.copyOfRange(pathParts, 0, pathParts.length - 1);

            // the simple property name, e.g. "_type"
            final String name = pathParts[pathParts.length - 1];

            // get the node that matches the path
            final PropertyTreeNode node = getNodeByPath(root, nodePath);

            // add the property as child with simple name and value
            node.addChild(new PropertyTreeValue(name, value));
        }
        return root;
    }

    /**
     * @return a TreeItem matching the specified {@link PropertyTreeItem}.
     */
    private static TreeItem<UiProperty> treeItem(PropertyTreeItem propertyTreeItem) {
        UiProperty value = new UiProperty(propertyTreeItem);
        TreeItem<UiProperty> treeItem = new TreeItem<>(value);
        List<TreeItem<UiProperty>> children = propertyTreeItem.getChildren().stream()
                .map(AcfTreeController::treeItem)
                .toList();
        treeItem.getChildren().addAll(children);
        return treeItem;
    }

    /**
     * Returns a {@link PropertyTreeNode} such that it can be accessed by navigating from the specified root node
     * and selecting the child nodes that match each path component in sequence.
     * If such a node does not exist, it will be created, and similarly for all missing intermediate nodes.
     */
    private PropertyTreeNode getNodeByPath(PropertyTreeNode root, String[] nodePath) {
        if (nodePath.length == 0) {
            return root;
        }
        final String nodeName = nodePath[0];
        PropertyTreeNode node = (PropertyTreeNode) root.getChildren().stream()
                .filter(p -> p.getName().equals(nodeName))
                .findFirst()
                .orElseGet(() -> root.addChild(new PropertyTreeNode(nodeName)));
        return getNodeByPath(node, Arrays.copyOfRange(nodePath, 1, nodePath.length));

    }
}
