/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j.internal.deployer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Dynamically initialising a set of objects from a given jar
 * according to the manifest entry 'microservices'.
 */
public class MSSJarProcessor {
    private static final Logger log = LoggerFactory.getLogger(MSSJarProcessor.class);
    private static final String MICROSERVICES_MANIFEST_KEY = "Microservices";
    private File file;
    private List<Object> resourceInstances = new ArrayList<>();

    /**
     * @param file jar file to be processed
     *
     * @return this MSSJarProcessor instance
     */
    public MSSJarProcessor setArtifact(File file) {
        this.file = file;
        return this;
    }

    /**
     * Initialize a list of objects from the jar for the specified
     * classes in the jar's manifest file under 'microservices' key.
     *
     * @return this MSSJarProcessor instance
     * @throws MSSJarProcessorException if an error occurs while processing the jar file
     */
    public MSSJarProcessor process() throws MSSJarProcessorException {
        String jarPath = file.getAbsolutePath();
        final String[] mssClassNames = readMSSManifestEntry(jarPath);

        //Parent class loader is required to provide classes that are outside of the jar
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws MSSJarProcessorException {
                    try {
                        URLClassLoader classLoader =
                                new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
                        for (String className : mssClassNames) {
                            try {
                                Class classToLoad = classLoader.loadClass(className);
                                resourceInstances.add(classToLoad.newInstance());
                            } catch (ClassNotFoundException e) {
                                throw new MSSJarProcessorException("Class: " + className + " not found", e);
                            } catch (InstantiationException e) {
                                throw new MSSJarProcessorException("Failed to initialize class: " + className, e);
                            } catch (IllegalAccessException e) {
                                throw new MSSJarProcessorException("Failed to access class: " + className, e);
                            }
                        }
                    } catch (MalformedURLException e) {
                        throw new MSSJarProcessorException("Path to jar is invalid", e);
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            //This assignment is required to fix unchecked/unconfirmed cast findbugs issue
            Exception e1 = e.getException();
            if (e1 instanceof MSSJarProcessorException) {
                throw (MSSJarProcessorException) e1;
            }
        }

        return this;
    }

    /**
     * Extracts the comma separated list of fully qualified class
     * names of the 'microservices' key of the jar's maifest file.
     *
     * @param jarPath absolute path to the jar file
     * @return String array of fully qualified class names
     */
    private String[] readMSSManifestEntry(String jarPath) throws MSSJarProcessorException {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                throw new MSSJarProcessorException("Error retrieving manifest: " + jarPath);
            }
            Attributes mainAttributes = manifest.getMainAttributes();
            String mssEntry = mainAttributes.getValue(MICROSERVICES_MANIFEST_KEY);
            if (mssEntry == null) {
                throw new MSSJarProcessorException("Manifest entry 'microservices' not found: " + jarPath);
            }
            return mssEntry.split("\\s*,\\s*");
        } catch (IOException e) {
            throw new MSSJarProcessorException("Error retrieving manifest: " + jarPath, e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    log.warn("Cannot close jar file: " + jarPath, e);
                }
            }
        }
    }

    /**
     * Return the initialized objects.
     *
     * @return List of initialized objects
     */
    public List<Object> getResourceInstances() {
        return resourceInstances;
    }
}
