/*
 * Copyright 2018, Ben Gruver
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.fusionIdea.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.ui.DefaultFacetSettingsEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.python.PythonModuleTypeBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.fusionIdea.FusionIdeaIcons;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class FusionFacetType extends FacetType<FusionFacet, FusionFacetConfiguration> {

    private static final String STRING_ID = "fusion360plugin";
    public static final FacetTypeId<FusionFacet> ID = new FacetTypeId<FusionFacet>(STRING_ID);

    public FusionFacetType() {
        super(ID, STRING_ID, "Fusion 360 Support");
    }

    @Nullable @Override public Icon getIcon() {
        return FusionIdeaIcons.LOGO;
    }

    public static FusionFacetType getInstance() {
        return findInstance(FusionFacetType.class);
    }
    
    public static class OS {
        String os_name = System.getProperty("os.name");
        String startPath = getStartPath();
        String fusionExec = getFusionExec();

        public String getStartPath() {
            if (os_name.contains("Windows")) {
                String startPath = new String("/AppData/Local/Autodesk/webdeploy/production");
                return startPath;
            } else if (os_name.contains("Mac")) {
                String startPath = new String("/Library/Application Support/Autodesk/webdeploy/production");
                return startPath;
            } else {
                throw new IllegalArgumentException("OS not supported!\nOS: " + os_name);
            }
        }

        public String getFusionExec() {
            if (os_name.contains("Windows")) {
                String fusionExec = ("Fusion360.exe");
                return fusionExec;
            } else if (os_name.contains("Mac")) {
                String fusionExec = ("Autodesk Fusion 360.app");
                return fusionExec;
            } else {
                throw new IllegalArgumentException("OS not supported!\nOS: " + os_name);
            }
        }
    }

    public static FusionFacetConfiguration newDefaultConfiguration() {
        VirtualFile homeDir = VfsUtil.getUserHomeDir();

        OS os = new OS();

        File startPath = new File(homeDir.getCanonicalPath(), os.startPath);

        File fusionPath = null;

        if (startPath.exists()) {
            for (File subdir : startPath.listFiles(File::isDirectory)) {
                File candidatePath = new File(subdir, os.fusionExec);
                if (candidatePath.exists()) {
                    fusionPath = candidatePath;
                    break;
                }
            }
        }

        if (fusionPath != null) {
            try {
                return new FusionFacetConfiguration(fusionPath.getCanonicalPath());
            } catch (IOException e) {
            }
            return new FusionFacetConfiguration(fusionPath.getAbsolutePath());
        }
        return new FusionFacetConfiguration(null);
    }

    @Override public FusionFacetConfiguration createDefaultConfiguration() {
        return newDefaultConfiguration();
    }

    @Override
    public FusionFacet createFacet(@NotNull Module module, String name,
                                   @NotNull FusionFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new FusionFacet(this, module, name, configuration, underlyingFacet);
    }

    @Override public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof PythonModuleTypeBase;
    }

    @Nullable @Override
    public DefaultFacetSettingsEditor createDefaultConfigurationEditor(@NotNull Project project, @NotNull FusionFacetConfiguration configuration) {
        return super.createDefaultConfigurationEditor(project, configuration);
    }
}
