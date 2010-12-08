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

import com.vijaykiran.wicket.utils.localization.util.ResourceFinder;
import com.vijaykiran.wicket.utils.localization.util.Selection;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static java.util.Arrays.deepToString;


/**
 * @author Vijay Kiran (mail@vijaykiran.com)
 */
public abstract class AbstractLocalizationCheckerMojo extends AbstractMojo {

    /**
     * The base directory, in which to search for files.
     *
     * @parameter expression="${l10n.basedir}" default-value="${basedir}"
     * @required
     */
    protected File basedir;

    /**
     * A comma seperated list of languages to test the labels in for. The language name should be a valid ISO language
     * code as defined by ISO-639 : http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt
     *
     * @parameter expression=${l10n.checkLocales} default-value="en,nl,it"
     */
    protected String checkLocales = "en,nl,it";

    /**
     * When set to true, the plugin will make sure that the build will fail if the localizations are not
     * there for any lables.
     *
     * @parameter expression=${l10n.strictChecking} default-value="false"
     */
    protected boolean strictChecking = false;

    /**
     * Maven localization plugin uses concurrency to check localization headers. This factor is used to control the number
     * of threads used to check. The rule is:
     * <br>
     * {@code <nThreads> = <number of cores> *  concurrencyFactor}
     * <br>
     * The default is 1.5.
     *
     * @parameter expression="${l10n.concurrencyFactor}" default-value="1.5"
     */
    protected float concurrencyFactor = 1.5f;

    /**
     * Set this to "true" to cause limited output, defaults to false.
     *
     * @parameter expression="${l10n.quiet}" default-value="false"
     */
    protected boolean quiet = false;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project = new MavenProject();

    /**
     * The absolute path where the results of the localization checking will be stored.
     * Defaults to project.build.directory/localization-results.html
     *
     * @parameter expression="${l10n.resultsFilePath}"  default-value="${project.build.directory}/localization-results.html"
     * @required
     * @readonly
     */
    protected String resultsFilePath;

    protected Locale[] supportedLocales;

    @SuppressWarnings({"unchecked"})
    protected final void execute(final Callback callback) throws MojoExecutionException, MojoFailureException {
        info("Checking localizations for " + checkLocales);
        supportedLocales = getSupportedLocales();

        ResourceFinder finder = new ResourceFinder(basedir);
        try {
            finder.setCompileClassPath(project.getCompileClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        finder.setPluginClassPath(getClass().getClassLoader());

        int nThreads = (int) (Runtime.getRuntime().availableProcessors() * concurrencyFactor);
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        CompletionService completionService = new ExecutorCompletionService(executorService);

        debug("Number of execution threads: %s", nThreads);
        try {
            for (final String file : listSelectedFiles()) {
                completionService.submit(new Runnable() {
                    public void run() {
                        callback.onCheckFile(file);
                    }
                }, null);
            }
        } catch (Exception e) {
            executorService.shutdownNow();
        }
    }

    protected final Locale[] getSupportedLocales() {
        StringTokenizer localeTokenizer = new StringTokenizer(checkLocales, ",");
        List<Locale> localeList = new ArrayList<Locale>();
        while (localeTokenizer.hasMoreTokens()) {
            localeList.add(new Locale(localeTokenizer.nextToken().trim()));
        }
        return localeList.toArray(new Locale[localeList.size()]);
    }

    protected final String[] listSelectedFiles() throws MojoFailureException {
        Selection selection = new Selection(basedir);
        debug("From: %s", basedir);
        debug("Including: %s", deepToString(selection.getIncluded()));
        debug("Excluding: %s", deepToString(selection.getExcluded()));
        return selection.getSelectedFiles();
    }

    protected final void info(String format, Object... params) {
        if (!quiet) {
            getLog().info(format(format, params));
        }
    }

    protected final void debug(String format, Object... params) {
        if (!quiet) {
            getLog().debug(format(format, params));
        }
    }

    protected final void warn(String format, Object... params) {
        getLog().warn(format(format, params));
    }

}
