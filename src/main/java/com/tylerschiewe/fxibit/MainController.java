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

import javafx.beans.binding.Bindings;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class MainController {

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private ListView<Exhibit> exhibitsListView;

    @FXML
    private ViewerController viewerController;

    @FXML
    private TextField searchField;

    private final DepsDirectory depsDirectory = new DepsDirectory(new File(System.getProperty("fxibit.depsDir", "deps")));
    private final AppsDirectory appsDirectory = new AppsDirectory(new File(System.getProperty("fxibit.appsDir", "apps")));

    @FXML
    private void initialize() {
        depsDirectory.startWatchingAsync();
        appsDirectory.startWatchingAsync();

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

        SortedList<Exhibit> searchSortedExhibits = new SortedList<>(appsDirectory.exhibitsProperty());

        searchSortedExhibits.comparatorProperty().bind(Bindings.createObjectBinding(() ->
                        "".equals(searchField.getText()) ? Comparator.comparing(Exhibit::getName) : Comparator.comparingInt((e) -> 100 - FuzzySearch.weightedRatio(e.getName(), searchField.getText()))
        , searchField.textProperty()));

        exhibitsListView.setItems(searchSortedExhibits);

        rootBorderPane.setOnDragOver(e -> {
            if (e.getGestureSource() != rootBorderPane &&
                    e.getDragboard().hasFiles() &&
                    e.getDragboard().getFiles().stream().allMatch(f -> f.isFile() && f.getName().endsWith("jar"))) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            e.consume();
        });

        // watch for dropped jars
        rootBorderPane.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = true;
            if (db.hasFiles()) {
                // copy non-runnable jars to deps directory, runnable jars to apps directory
                List<File> deps = new ArrayList<>();
                List<File> apps = new ArrayList<>();
                for (File f : db.getFiles()) {
                    try (JarFile jarFile = new JarFile(f)) {

                        Manifest manifest = jarFile.getManifest();
                        String mainClassName = manifest.getMainAttributes().getValue("Main-Class");
                        if (mainClassName != null) {
                            apps.add(f);
                        } else {
                            deps.add(f);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        success = false;
                    }
                }
                deps.forEach(dep -> {
                    try {
                        depsDirectory.copyDependency(dep);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });
                apps.forEach(app -> {
                    try {
                        appsDirectory.copyApp(app);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    public void teardown() {
        appsDirectory.stopWatching();
    }
}
