/*
 * Copyright 2012 Brian Matthews
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.btmatthews.maven.plugins.crx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

/**
 * Implement the crx-verify goal for the plug-in. The crx-verify goal verifies the signature of a Chrome Browser
 * Extension.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
@Mojo(name = "crx-verify", defaultPhase = LifecyclePhase.PACKAGE)
public class CRXVerifyMojo extends AbstractMojo {

    /**
     * The location of the Chrome Extension which is used to override the default location calculated using the
     * output directory final name and classifier.
     */
    @Parameter(required = false)
    private File crxPath;

    /**
     * The final name of the artifact.
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;

    /**
     * The build target directory.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * An optional classifier for the artifact.
     */
    @Parameter
    private String classifier;

    /**
     * The archive helper is used to read the CRX archive.
     */
    @Component
    private ArchiveHelper archiveHelper;

    /**
     * The signature helper is used to verify the signature of the CRX archive.
     */
    @Component
    private SignatureHelper signatureHelper;

    /**
     * Called when the Maven plug-in is executing. It loads and verifies the signature of a CRX archive.
     *
     * @throws MojoExecutionException If there was an error that should stop the build.
     * @throws MojoFailureException   If there was an error but the build might be allowed to continue.
     */
    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {

        final File crxFile;

        if (crxPath == null) {

            // Generate CRX file name

            final StringBuilder crxFilename = new StringBuilder();
            crxFilename.append(finalName);
            if (StringUtils.isNotEmpty(classifier)) {
                crxFilename.append('-');
                crxFilename.append(classifier);
            }
            crxFilename.append(".crx");
            crxFile = new File(outputDirectory, crxFilename.toString());
        } else {
            crxFile = crxPath;
        }

        try {
            final CRXArchive archive = archiveHelper.readArchive(crxFile);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            final KeySpec keySpec = new X509EncodedKeySpec(archive.getPublicKey());
            final PublicKey publicKey = keyFactory.generatePublic(keySpec);
            if (!signatureHelper.check(archive.getData(), publicKey, archive.getSignature())) {
                throw new MojoFailureException("The signature is not valid");
            }
        } catch (final FileNotFoundException e) {
            throw new MojoExecutionException("Could not find CRX archive", e);
        } catch (final IOException e) {
            throw new MojoExecutionException("Could not load CRX archive", e);
        } catch (final GeneralSecurityException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
