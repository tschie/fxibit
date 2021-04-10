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
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class MainController {

    private final DynamicClassLoader systemClassLoader = (DynamicClassLoader) ClassLoader.getSystemClassLoader();

    @FXML
    public ListView<Exhibit> exhibitsListView;

    @FXML
    private StackPane viewer;

    private Application currentApp;

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
                            exhibitsListView.getItems().add(exhibit);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }

        exhibitsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    open(newValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void close() {
        if (currentApp != null) {
            try {
                currentApp.stop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                currentApp = null;
            }
        }
        viewer.getChildren().clear();
    }

    public void open(Exhibit exhibit) throws Exception {
        close();
        Constructor<Application>[] constructors = (Constructor<Application>[]) exhibit.getApplicationClass().getDeclaredConstructors();
        if (constructors.length > 0) {
            Constructor<Application> constructor = constructors[0];
            Stage stage = new Stage();
            currentApp = constructor.newInstance();
            currentApp.start(stage);
            stage.toBack();
            viewer.getChildren().add(stage.getScene().getRoot());
            stage.close();
        }
    }
}
