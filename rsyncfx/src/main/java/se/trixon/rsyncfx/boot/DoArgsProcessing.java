/*
 * Copyright 2023 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.rsyncfx.boot;

import java.io.IOException;
import java.util.ResourceBundle;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.util.PomInfo;
import se.trixon.rsyncfx.core.ExecutorManager;
import se.trixon.rsyncfx.core.JobManager;
import se.trixon.rsyncfx.core.StorageManager;
import se.trixon.rsyncfx.ui.App;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class DoArgsProcessing implements ArgsProcessor {

    @Arg(longName = "list")
    @Description(
            shortDescription = "#DoArgsProcessing.list.desc"
    )
    @Messages("DoArgsProcessing.list.desc=list jobs")
    public boolean mListOption;
    @Arg(longName = "start")
    @Description(
            displayName = "#DoArgsProcessing.start.name",
            shortDescription = "#DoArgsProcessing.start.desc"
    )
    @Messages({
        "DoArgsProcessing.start.name=--start <job>",
        "DoArgsProcessing.start.desc=start job"
    })
    public String mStartOption;

    @Arg(longName = "version")
    @Description(
            shortDescription = "#DoArgsProcessing.version.desc"
    )
    @Messages("DoArgsProcessing.version.desc=print the version information and exit")
    public boolean mVersionOption;
    private final ResourceBundle mBundle = NbBundle.getBundle(DoArgsProcessing.class);
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();

    {
        Installer.GUI = false;
    }

    public DoArgsProcessing() {
    }

    @Override
    public void process(Env env) throws CommandException {
        if (mVersionOption) {
            displayVersion();
        } else if (mListOption) {
            load();
            listJobs();
        } else if (mStartOption != null) {
            load();
            startJob(mStartOption);
        }

        LifecycleManager.getDefault().exit();
    }

    private void displayVersion() {
        var pomInfo = new PomInfo(App.class, "se.trixon.rsyncfx", "rsyncfx");
        System.out.println(mBundle.getString("DoArgsProcessing.version").formatted(pomInfo.getVersion()));
    }

    private void listJobs() {
        for (var job : JobManager.getInstance().getItems()) {
            System.out.println(job.getName());
        }
    }

    private void load() {
        try {
            StorageManager.getInstance().load();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void startJob(String jobName) {
        var job = JobManager.getInstance().getByName(jobName);
        if (job != null) {
            mExecutorManager.start(job);
        } else {
            System.out.println("JOB NOT FOUND " + jobName);
        }
    }
}
