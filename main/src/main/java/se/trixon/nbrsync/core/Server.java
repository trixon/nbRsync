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
package se.trixon.nbrsync.core;

import it.sauronsoftware.cron4j.Scheduler;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.openide.LifecycleManager;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.nbrsync.NbRsync;
import se.trixon.nbrsync.boot.DoArgsProcessing;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Server {

    private final ResourceBundle mBundle = NbBundle.getBundle(Server.class);
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final File mLockFile = new File(Places.getUserDirectory(), "lock");
    private final JobManager mManager = JobManager.getInstance();
    private final File mReloadFile = new File(Places.getUserDirectory(), "server_marked_for_reload");
    private Scheduler mScheduler;
    private final File mServerFile = new File(Places.getUserDirectory(), "runningServer");
    private final ArrayList<Runnable> mStartMonitors = new ArrayList<>();
    private final ArrayList<Runnable> mStopMonitors = new ArrayList<>();

    public static Server getInstance() {
        return Holder.INSTANCE;
    }

    private Server() {
    }

    public ArrayList<Runnable> getStartMonitors() {
        return mStartMonitors;
    }

    public ArrayList<Runnable> getStopMonitors() {
        return mStopMonitors;
    }

    public boolean isRunning() {
        return mScheduler != null && mScheduler.isStarted();
    }

    public boolean isServerLocked() {
        return mServerFile.isFile();
    }

    public void markForReload() {
        if (isServerLocked()) {
            try {
                FileUtils.touch(mReloadFile);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void start() {
        if (mServerFile.isFile()) {
            System.out.println(Dict.SERVER_ALREADY_STARTED.toString());
            LifecycleManager.getDefault().exit();
        }

        try {
            FileUtils.forceDelete(mLockFile);
            FileUtils.touch(mServerFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println(mBundle.getString("startingInServerMode"));

        load();

        while (mServerFile.isFile()) {
            try {
                Thread.sleep(Duration.ofSeconds(1));
                if (mReloadFile.isFile()) {
                    reload();
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        mScheduler.stop();
        System.out.println(Dict.SERVER_STOPPED.toString());

        LifecycleManager.getDefault().exit();
    }

    public void startMonitor() {
        var directory = Places.getUserDirectory();
        var filter = FileFilterUtils.nameFileFilter(mServerFile.getName());
        var fileAlterationListener = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                mStartMonitors.forEach(r -> r.run());
            }

            @Override
            public void onFileDelete(File file) {
                mStopMonitors.forEach(r -> r.run());
            }
        };

        var observer = new FileAlterationObserver(directory, filter, IOCase.INSENSITIVE);
        observer.addListener(fileAlterationListener);

        var monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(1));
        monitor.addObserver(observer);

        try {
            monitor.start();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void stop() {
        if (mServerFile.exists()) {
            System.out.println(Dict.SERVER_STOPPING.toString());
            NbRsync.delete(mServerFile, mReloadFile);
        } else {
            System.out.println(Dict.SERVER_NOT_RUNNING.toString());
        }
        LifecycleManager.getDefault().exit();
    }

    public void stopFromGui() {
        NbRsync.delete(mServerFile);
    }

    private boolean hasScheduledJobs() {
        for (var job : mManager.getItems()) {
            if (job.isScheduled()) {
                return true;
            }
        }

        return false;
    }

    private void load() {
        mScheduler = new Scheduler();

        if (hasScheduledJobs()) {
            for (var job : mManager.getItems()) {
                if (job.isScheduled()) {
                    System.out.println(mBundle.getString("scheduling_s").formatted(job.getName()));
                    job.getCronItemsAsList().stream().map(c -> c.getName()).forEachOrdered(cronString -> {
                        System.out.println("\t%s".formatted(cronString));
                        mScheduler.schedule(cronString, () -> {
                            if (job.isLocked()) {
                                System.out.println(NbBundle.getMessage(DoArgsProcessing.class, "skipRunningJob").formatted(job.getName()));
                            } else {
                                mExecutorManager.start(job, false);
                            }
                        });
                    });
                }
            }
        } else {
            System.out.println(mBundle.getString("noScheduledJobs"));
        }

        mScheduler.start();
    }

    private void reload() {
        System.out.println(Dict.RELOADING_CONFIGURATION);
        try {
            NbRsync.delete(mReloadFile);
            mScheduler.stop();
            StorageManager.getInstance().load();
            load();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static class Holder {

        private static final Server INSTANCE = new Server();
    }
}
