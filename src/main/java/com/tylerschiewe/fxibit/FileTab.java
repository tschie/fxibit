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

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTab extends Tab {

    private final String name;
    private final File file;

    @FXML
    private WebView webView;

    @FXML
    private TextField find;
    @FXML
    private Button findButton;

    public FileTab(String name, File file) {
        super();
        this.name = name;
        this.file = file;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/fileTab.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setClassLoader(getClass().getClassLoader());
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setText(name);

        try {
            String source = IOUtils.toString(file.toURI().toURL(), StandardCharsets.UTF_8);
            WebEngine engine = webView.getEngine();
            URL htmlURL = getClass().getResource("/html/code.html");
            if (htmlURL != null) {
                engine.load(htmlURL.toExternalForm());
                engine.getLoadWorker().stateProperty().addListener((o, p, n) -> {
                    if (n == Worker.State.SUCCEEDED) {
                        Element codeElement = engine.getDocument().getElementById("source");
                        codeElement.setTextContent(source);
                        String extension = FilenameUtils.getExtension(file.getName());
                        String language = switch (extension) {
                            case "md" -> "markdown";
                            case "fxml" -> "xml";
                            default -> extension;
                        };
                        codeElement.setAttribute("class", "language-" + language);
                        engine.executeScript("render();");
                        //engine.executeScript("format();");
                    }
                });
            }
        } catch (Exception var4) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error generating source viewer", var4);
        }
    }

    @FXML
    private void clear() {
        webView.getEngine().executeScript("clear();");
        this.find.setText("");
    }

    @FXML
    public void highlight() {
        String text = this.find.getText();
        webView.getEngine().executeScript("clear();");
        webView.getEngine().executeScript("highlight(\"" + text + "\");");
    }

    @FXML
    public void next() {
        webView.getEngine().executeScript("next();");
    }

    @FXML
    public void prev() {
        webView.getEngine().executeScript("prev();");
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }
}
