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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

/**
 * Watches deps directory for new jars to load into system classloader.
 */
public class DepsDirectory {

    private final File directory;
    private boolean watching = false;
    private final DynamicClassLoader dynamicClassLoader = (DynamicClassLoader) ClassLoader.getSystemClassLoader();

    public DepsDirectory(File depsDir) {
        this.directory = depsDir;
        if (!this.directory.exists() || !depsDir.isDirectory()) {
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
                            addDep(file);
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
                                    addDep(child);
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

    public void copyDependency(File file) throws IOException {
        if (directory.canWrite()) {
            Files.copy(file.toPath(), directory.toPath().resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void addDep(File file) throws Exception {
        if (file.isFile() && file.getName().endsWith("jar")) {
            dynamicClassLoader.add(file.toPath().toUri().toURL());
        } else {
            throw new Exception("File is not a jar file");
        }
    }
}
