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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.io.File;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class MainController {

    private final DynamicClassLoader systemClassLoader = (DynamicClassLoader) ClassLoader.getSystemClassLoader();

    @FXML
    public ListView<Exhibit> exhibitsListView;

    @FXML
    private ViewerController viewerController;

    private final ReadOnlyListWrapper<Exhibit> exhibits = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    @FXML
    private void initialize() {
        // load samples
        File jarDirectory = new File(System.getProperty("fxibit.appsDir", "apps"));
        if (jarDirectory.exists() && jarDirectory.isDirectory()) {
            File[] files = jarDirectory.listFiles();
            if (files != null) {
                Arrays.asList(files).forEach(file -> {
                    if (file.isFile() && file.getName().endsWith("jar")) {
                        try {
                            systemClassLoader.add(file.toPath().toUri().toURL());
                            Manifest manifest = new JarFile(file).getManifest();
                            String mainClassName = manifest.getMainAttributes().getValue("Main-Class");
                            Class<Application> applicationClass = (Class<Application>) systemClassLoader.loadClass(mainClassName);
                            Exhibit exhibit = new Exhibit(applicationClass);
                            manifest.getMainAttributes().forEach((key, value) -> {
                                if ("Application-Name".equals(key.toString())) {
                                    exhibit.setName(value.toString());
                                }
                            });
                            exhibits.get().add(exhibit);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }

        exhibitsListView.setCellFactory(param -> new ListCell<>() {
            protected void updateItem(Exhibit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                    setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
                            try {
                                viewerController.open(item);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        exhibitsListView.itemsProperty().bind(exhibits);
    }

    public ObservableList<Exhibit> getExhibits() {
        return exhibits.get();
    }

    public ReadOnlyListProperty<Exhibit> exhibitsProperty() {
        return exhibits.getReadOnlyProperty();
    }
}
