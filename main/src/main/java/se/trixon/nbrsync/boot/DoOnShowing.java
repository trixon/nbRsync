/*
 * Copyright 2024 Patrik Karlström <patrik@trixon.se>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.nbrsync.boot;

import java.net.MalformedURLException;
import java.net.URI;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.NbHelper;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.nbrsync.NbRsync;
import se.trixon.nbrsync.core.Server;

/**
 *
 * @author Patrik Karlström
 */
@OnShowing
public class DoOnShowing implements Runnable {

    @Override
    public void run() {
        if (Boolean.FALSE.equals(NbHelper.isGui().get())) {
            return;
        }

        SystemHelper.setDesktopBrowser(url -> {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(URI.create(url).toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        var container = IOContainer.getDefault();
        container.open();

        var windowManager = WindowManager.getDefault();
        var outputMode = windowManager.findMode("output");
        var editorMode = windowManager.findMode("editor");

        for (var tc : windowManager.getOpenedTopComponents(outputMode)) {
            editorMode.dockInto(tc);
        }

        for (var tc : windowManager.getOpenedTopComponents(editorMode)) {
            tc.setIcon(null);
        }

        NbRsync.displaySystemInformation();

        var server = Server.getInstance();

        if (server.isServerLocked()) {
            NbMessage.information(Dict.INFORMATION.toString(), "nbRsync server is running in the background.\nConfiguration changes will be applied immediately.");
        }

        server.getStartMonitors().add(() -> {
            NbMessage.information(Dict.INFORMATION.toString(), "nbRsync server started.");
        });

        server.getStopMonitors().add(() -> {
            NbMessage.information(Dict.INFORMATION.toString(), "nbRsync server stopped.");
        });

        server.startMonitor();
    }
}
