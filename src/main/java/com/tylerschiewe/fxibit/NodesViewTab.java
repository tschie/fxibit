/*
 * Fxibit
 *
 * Copyright © 2021 Tyler Schiewe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tylerschiewe.fxibit;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class NodesViewTab extends Tab  {

    private final Scene scene;

    public NodesViewTab(Scene scene) {
        super();
        this.scene = scene;
        this.setText("Nodes");
        TreeView<Node> nodeTreeView = new TreeView<>();
        this.setContent(nodeTreeView);

        nodeTreeView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.getValue().setStyle("-fx-effect: none;");
            }
            if (newValue != null) {
                newValue.getValue().setStyle("-fx-effect: innershadow(gaussian, #43ffe3, 1, 1.0, 0, 0);");
            }
        }));

        nodeTreeView.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) {
                nodeTreeView.getSelectionModel().clearSelection();
            }
        }));

        nodeTreeView.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<Node> call(TreeView<Node> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(Node item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getClass().getSimpleName());
                        }
                    }
                };
            }
        });

        nodeTreeView.setRoot(new NodeTreeItem(scene.getRoot()));
    }

    public Scene scene() {
        return scene;
    }
}
