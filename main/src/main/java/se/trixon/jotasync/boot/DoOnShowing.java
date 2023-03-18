/*
 * Copyright 2022 Patrik Karlström.
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
package se.trixon.jotasync.boot;

import java.net.MalformedURLException;
import java.net.URL;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.jotasync.Jota;

/**
 *
 * @author Patrik Karlström
 */
@OnShowing
public class DoOnShowing implements Runnable {

    private final Jota mJota = Jota.getInstance();

    @Override
    public void run() {
        SystemHelper.setDesktopBrowser(url -> {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(url));
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

        Jota.displaySystemInformation();
//
//        mJota.getGlobalState().addListener(gsce -> {
//            Job job = gsce.getValue();
//            var jobExecutor = ExecutorManager.getInstance().getJobExecutors().get(job.getId());
//            LogTab logTab = null;
//
//            for (var tab : mTabPane.getTabs()) {
//                if (tab instanceof LogTab lt) {
//                    if (job.getId().equals(lt.getJob().getId())) {
//                        logTab = lt;
//                        logTab.clear();
//                        break;
//                    }
//                }
//            }
//
//            if (logTab == null) {
//                logTab = new LogTab(job);
//                mTabPane.getTabs().add(logTab);
//            }
//
//            jobExecutor.setProcessCallbacks(logTab);
//            mTabPane.getSelectionModel().select(logTab);
//        }, Jota.GSC_JOB_STARTED);
        SwingHelper.runLaterDelayed(500, () -> {
            //Pre-load but don't display
            Almond.getTopComponent("EditorTopComponent");
        });

    }
}
