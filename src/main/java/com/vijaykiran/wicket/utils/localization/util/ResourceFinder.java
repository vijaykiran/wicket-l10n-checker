/**
 * Copyright (C) 2008 http://code.google.com/p/maven-license-plugin/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vijaykiran.wicket.utils.localization.util;

import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * <b>Date:</b> 26-Feb-2008<br>
 * <b>Author:</b> Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ResourceFinder {
    private final File basedir;
    private CustomClassLoader compileClassPath;
    private CustomClassLoader pluginClassPath;

    public ResourceFinder(File basedir) {
        this.basedir = basedir;
    }

    public void setCompileClassPath(List<String> classpath) {
        compileClassPath = new CustomClassLoader();
        if (classpath != null) {
            for (String absolutePath : classpath) {
                compileClassPath.addFolder(absolutePath);
            }
        }
    }

    public void setPluginClassPath(ClassLoader classLoader) {
        pluginClassPath = new CustomClassLoader(classLoader);
    }

    /**
     * Find a resource by searching:<br/>
     * 1. In the filesystem, relative to basedir<br/>
     * 2. In the filesystem, as an absolute path (or relative to current execution directory)<br/>
     * 3. In project classpath<br/>
     * 4. In plugin classpath<br/>
     * 5. As a URL
     *
     * @param resource The resource to get
     * @return A valid URL
     * @throws MojoFailureException If the resource is not found
     */
    public URL findResource(String resource) throws MojoFailureException {
        URL res = toURL(new File(basedir, resource));
        if (res != null) {
            return res;
        }

        res = toURL(new File(resource));
        if (res != null) {
            return res;
        }

        String cpResource = resource.startsWith("/") ? resource.substring(1) : resource;

        res = compileClassPath.getResource(cpResource);
        if (res != null) {
            return res;
        }

        res = pluginClassPath.getResource(cpResource);
        if (res != null) {
            return res;
        }

        try {
            res = new URL(resource);
            res.openStream().close();
            return res;
        } catch (Exception e) {
            throw new MojoFailureException("Resource " + resource + " not found in file system, classpath or URL: " + e.getMessage(), e);
        }
    }

    private URL toURL(File file) {
        if (file.exists() && file.canRead()) {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException ignored) {
                //Ignored
            }
        }
        return null;
    }

}
