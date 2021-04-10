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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class ExhibitTab extends Tab {

    private final Exhibit exhibit;
    private boolean started = false;
    private Scene scene = null;

    @FXML
    private StackPane displayStackPane;

    @FXML
    private TabPane viewsTabPane;

    public ExhibitTab(Exhibit exhibit) {
        super();
        this.exhibit = exhibit;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/exhibitTab.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setClassLoader(getClass().getClassLoader());
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setText(exhibit.getName());
    }

    public void start() {
        if (!started) {
            started = true;
            try {
                Stage stage = new Stage();
                Constructor<Application>[] constructors = (Constructor<Application>[]) exhibit.getApplicationClass().getDeclaredConstructors();
                if (constructors.length > 0) {
                    Constructor<Application> constructor = constructors[0];
                    Application application = constructor.newInstance();
                    application.start(stage);
                    scene = stage.getScene();
                    displayStackPane.getChildren().add(scene.getRoot());
                    viewsTabPane.getTabs().add(new NodesViewTab(scene));
                    this.setText(stage.getTitle());
                    stage.toBack();
                    stage.close();
                    setOnCloseRequest(e -> {
                        try {
                            application.stop();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Exhibit getExhibit() {
        return exhibit;
    }

    public Scene getScene() {
        return scene;
    }
}
