/**
 * Copyright (C) 2010 Vijay Kiran
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

package com.vijaykiran.wicket.utils.localization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Wicket localization checker maven plugin mojo.
 *
 * @author Vijay Kiran (mail@vijaykiran.com)
 * @goal check
 * @phase verify
 * @threadSafe
 */
public final class LocalizationCheckMojo extends AbstractLocalizationCheckerMojo {


    public void execute() throws MojoExecutionException, MojoFailureException {
        final Map<String, List<String>> missingFileKeyMap = new ConcurrentHashMap<String, List<String>>();
        final List<String> missingBundles = new CopyOnWriteArrayList<String>();

        execute(new Callback() {
            public void onCheckFile(String filename) {

                ResourceBundle defaultBundle = null;
                try {
                    defaultBundle = new PropertyResourceBundle(new FileInputStream(basedir.getAbsolutePath() + File.separator + filename));
                } catch (IOException e) {
                    info("Unable to load Resource ", e);
                }

                if (defaultBundle != null) {
                    for (Locale supportedLocale : supportedLocales) {
                        String localeFileName = filename.replace(".properties", "_" + supportedLocale.getLanguage() + ".properties");
                        FileInputStream otherBundleStream = null;
                        try {
                            otherBundleStream = new FileInputStream(basedir.getAbsolutePath() + File.separator + localeFileName);
                        } catch (FileNotFoundException e) {
                            missingBundles.add(getFullPath(localeFileName));
                            debug("Unable to load file: " + getFullPath(localeFileName));
                        }

                        ResourceBundle otherBundle = null;
                        try {
                            otherBundle = new PropertyResourceBundle(otherBundleStream);
                        } catch (IOException e) {
                            getLog().error("Unable to load the resource defaultBundle" + e);
                        }

                        if (otherBundle != null) {
                            List<String> missingKeys = new ArrayList<String>();
                            Set<String> keySet = defaultBundle.keySet();
                            for (String key : keySet) {
                                if (!otherBundle.containsKey(key)) {
                                    missingKeys.add(key);
                                }
                            }
                            if (missingKeys.size() > 0) {
                                missingFileKeyMap.put(localeFileName, missingKeys);
                            }
                        }

                    }
                }

            }
        });

        StringBuffer resultsHtmlString = new StringBuffer();
        if (missingBundles.size() > 0) {
            resultsHtmlString.append("<h2>Missing Properties Files</h2><br/>");
        }

        resultsHtmlString.append("<table>");
        for (String missingBundle : missingBundles) {
            info("Missing Properties file :  " + missingBundle);
            resultsHtmlString.append("<tr><td>").append(missingBundle).append("</tr></td>");

        }
        resultsHtmlString.append("</table>");


        for (String file : missingFileKeyMap.keySet()) {
            resultsHtmlString.append("<h2>Missing Keys in ").append(file).append(" </h2><br/>");
            info("Missing keys in file " + file);
            List<String> keys = missingFileKeyMap.get(file);
            resultsHtmlString.append("<table>");
            for (String key : keys) {
                resultsHtmlString.append("<tr><td>").append(key).append("</tr></td>");
            }
            resultsHtmlString.append("</table>");
        }

        info("Adding results to " + resultsFilePath);

        FileWriter writer = null;
        try {
            writer = new FileWriter(resultsFilePath, true);
            writer.append(resultsHtmlString.toString());

        } catch (IOException e) {
            info("Unable to write to file " + e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                //Ignore.
            }
        }

        if (strictChecking) {
            if (missingBundles.size() > 0 || missingFileKeyMap.size() > 0) {
                throw new MojoExecutionException("Localization files/labels are missing, please check the results file at  " + resultsFilePath);
            }
        }

    }

    private String getFullPath(String filename) {
        return basedir.getAbsolutePath() + File.separator + filename;
    }


}
