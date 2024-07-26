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

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Server {

    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final JobManager mManager = JobManager.getInstance();
    private final File mReloadFile = new File(Places.getUserDirectory(), "server_marked_for_reload");
    private Scheduler mScheduler;
    private final File mServerFile = new File(Places.getUserDirectory(), "server");
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
            System.out.println("nbRsync server already started.");
            LifecycleManager.getDefault().exit();
        }

        try {
            FileUtils.forceDelete(new File(Places.getUserDirectory(), "lock"));
            FileUtils.touch(mServerFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("Starting nbRsync in server mode...");

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
        System.out.println("nbRsync server stopped.");

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
            System.out.println("Stopping nbRsync server...");
            delete(mServerFile);
            delete(mReloadFile);
        } else {
            System.out.println("nbRsync server not running!");
        }
        LifecycleManager.getDefault().exit();
    }

    public void stopFromGui() {
        delete(mServerFile);
    }

    private void delete(File file) {
        if (file.exists()) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
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
                    System.out.println("Scheduling %s".formatted(job.getName()));
                    job.getCronItemsAsList().stream().map(c -> c.getName()).forEachOrdered(cronString -> {
                        System.out.println("\t%s".formatted(cronString));
                        mScheduler.schedule(cronString, () -> {
                            if (job.isLocked()) {
                                System.out.println("Skipping already running job: %s".formatted(job.getName()));
                            } else {
                                mExecutorManager.start(job, false);
                            }
                        });
                    });
                }
            }
        } else {
            System.out.println("No scheduled jobs, awaiting configuration change...");
        }

        mScheduler.start();
    }

    private void reload() {
        System.out.println("Reloading configuration...");
        try {
            delete(mReloadFile);
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
