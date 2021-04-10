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

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;

public class NodeTreeItem extends TreeItem<Node> {

    public NodeTreeItem(Node value) {
        super(value);
        setExpanded(true);

        if (value instanceof Parent) {
            ((Parent) value).getChildrenUnmodifiable().addListener((ListChangeListener<Node>) c -> {
                c.next();
                if (c.wasAdded()) {
                    for (int i = 0; i < c.getAddedSize(); i++) {
                        getChildren().add(c.getFrom() + i, new NodeTreeItem(c.getAddedSubList().get(i)));
                    }
                }
                if (c.wasRemoved()) {
                    getChildren().remove(c.getFrom(), c.getTo());
                }
            });
            ((Parent) value).getChildrenUnmodifiable().forEach(childNode ->
                this.getChildren().add(new NodeTreeItem(childNode))
            );
        }
    }


}
