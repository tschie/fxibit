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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AppsDirectory {

    private final File directory;
    private boolean watching = false;

    private final ReadOnlyListWrapper<Exhibit> exhibits = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    public AppsDirectory(File appsDir) {
        this.directory = appsDir;
        if (!this.directory.exists() || !appsDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory");
        }
    }

    public void startWatching() throws Exception {
        if (!watching) {
            watching = true;
            File[] files = directory.listFiles();
            if (files != null) {
                Arrays.asList(files).forEach(file -> {
                    if (file.isFile() && file.getName().endsWith("jar")) {
                        try {
                            addExhibit(file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            WatchService watcher = FileSystems.getDefault().newWatchService();

            try (watcher) {
                directory.toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
                while (watching) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path filename = ((WatchEvent<Path>) event).context();
                            File child = directory.toPath().resolve(filename).toFile();
                            if (child.exists() && child.isFile() && child.getName().endsWith("jar")) {
                                try {
                                    addExhibit(child);
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void startWatchingAsync() {
        Thread thread = new Thread(() -> {
            try {
                startWatching();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopWatching() {
        watching = false;
    }

    public void copyExhibit(File file) throws IOException {
        if (directory.canWrite()) {
            Files.copy(file.toPath(), directory.toPath().resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void addExhibit(File file) throws Exception {
        if (file.isFile() && file.getName().endsWith("jar")) {
            try (JarFile jarFile = new JarFile(file); URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toPath().toUri().toURL()})) {
                Manifest manifest = jarFile.getManifest();
                String mainClassName = manifest.getMainAttributes().getValue("Main-Class");
                Class<Application> applicationClass = (Class<Application>) urlClassLoader.loadClass(mainClassName);
                Exhibit exhibit = new Exhibit(applicationClass);
                if (isNewExhibit(exhibit)) {
                    manifest.getMainAttributes().forEach((key, value) -> {
                        if ("Application-Name".equals(key.toString())) {
                            exhibit.setName(value.toString());
                        }
                    });
                    Platform.runLater(() -> exhibits.add(exhibit));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            throw new Exception("File is not a jar file");
        }
    }

    private boolean isNewExhibit(Exhibit exhibit) {
        return exhibits.stream().noneMatch(e ->
                e.getApplicationClass().getName().equals(exhibit.getApplicationClass().getName())
        );
    }

    public ObservableList<Exhibit> getExhibits() {
        return exhibits.get();
    }

    public ReadOnlyListProperty<Exhibit> exhibitsProperty() {
        return exhibits.getReadOnlyProperty();
    }
}
