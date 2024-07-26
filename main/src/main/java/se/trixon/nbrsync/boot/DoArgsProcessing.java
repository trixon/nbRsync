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

import java.io.IOException;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.NbHelper;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.nbrsync.NbRsync;
import se.trixon.nbrsync.core.ExecutorManager;
import se.trixon.nbrsync.core.JobManager;
import se.trixon.nbrsync.core.Rsync;
import se.trixon.nbrsync.core.Server;
import se.trixon.nbrsync.core.StorageManager;
import se.trixon.nbrsync.ui.SummaryBuilder;

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

    @Arg(longName = "remove-locks")
    @Description(
            shortDescription = "#DoArgsProcessing.remove-locks.desc"
    )
    @Messages("DoArgsProcessing.remove-locks.desc=remove locks for running jobs")
    public boolean mRemoveLocksOption;

    @Arg(longName = "start-server")
    @Description(
            shortDescription = "#DoArgsProcessing.start-server.desc"
    )
    @Messages("DoArgsProcessing.start-server.desc=start server")
    public boolean mStartServerOption;

    @Arg(longName = "stop-server")
    @Description(
            shortDescription = "#DoArgsProcessing.stop-server.desc"
    )
    @Messages("DoArgsProcessing.stop-server.desc=stop server")
    public boolean mStopServerOption;

    @Arg(longName = "version")
    @Description(
            shortDescription = "#DoArgsProcessing.version.desc"
    )
    @Messages("DoArgsProcessing.version.desc=print the version information")
    public boolean mVersionOption;
    private final ResourceBundle mBundle = NbBundle.getBundle(DoArgsProcessing.class);

    public DoArgsProcessing() {
    }

    @Override
    public void process(Env env) throws CommandException {
        if (mStartServerOption) {
            NbHelper.disableGui();
            Server.getInstance().start();
        } else if (mStopServerOption) {
            Server.getInstance().stop();
        } else if (mVersionOption) {
            displayVersion();
            LifecycleManager.getDefault().exit();
        } else if (mListOption) {
            load();
            listJobs();
            LifecycleManager.getDefault().exit();
        } else if (mStartOption != null) {
            NbHelper.disableGui();
            load();
            startJob(mStartOption);
            LifecycleManager.getDefault().exit();
        } else if (mRemoveLocksOption) {
            removeLocks();
            LifecycleManager.getDefault().exit();
        }
    }

    private void displayVersion() {
        var pomInfo = new PomInfo(SummaryBuilder.class, "se.trixon.nbrsync", "main");
        System.out.println(mBundle.getString("DoArgsProcessing.version").formatted(pomInfo.getVersion()));
        System.out.println(SystemHelper.getSystemInfo());
        System.out.println(Rsync.getInfo());
    }

    private void listJobs() {
        JobManager.getInstance().getItems().forEach(job -> {
            if (job.isScheduled()) {
                System.out.println("%s\n %s".formatted(job.getName(), String.join("\n ", StringUtils.split(job.getCronItems(), "|"))));
            } else {
                System.out.println(job.getName());
            }
        });
    }

    private void load() {
        try {
            StorageManager.getInstance().load();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void removeLocks() {
        try {
            FileUtils.forceDelete(NbRsync.getRunningJobsDirectory());
        } catch (IOException ex) {
            //nvm
        }
    }

    private void startJob(String jobName) {
        var job = JobManager.getInstance().getByName(jobName);
        if (job != null) {
            if (job.isLocked()) {
                System.out.println("Skipping already running job: %s".formatted(job.getName()));
            } else {
                ExecutorManager.getInstance().start(job, true);
            }
        } else {
            System.out.println("JOB NOT FOUND " + jobName);
        }
    }
}
