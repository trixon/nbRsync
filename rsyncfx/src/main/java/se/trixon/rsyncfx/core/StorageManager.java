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
package se.trixon.rsyncfx.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.openide.modules.Places;

/**
 *
 * @author Patrik Karlström
 */
public class StorageManager {

    private final File mHistoryFile;
    private final JobManager mJobManager = JobManager.getInstance();
    private final File mLogFile;
    private final File mProfilesBackupFile;
    private final File mProfilesFile;
    private Storage mStorage = new Storage();
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private final File mUserDirectory;

    public static StorageManager getInstance() {
        return Holder.INSTANCE;
    }

    private StorageManager() {
        mUserDirectory = Places.getUserDirectory();

        mProfilesFile = new File(mUserDirectory, "profiles.json");
        mProfilesBackupFile = new File(mUserDirectory, "profiles.bak");
        mHistoryFile = new File(mUserDirectory, "var/history");
        mLogFile = new File(mUserDirectory, "var/rsync.log");
    }

    public int getFileFormatVersion() {
        return mStorage.getFileFormatVersion();
    }

    public File getHistoryFile() {
        return mHistoryFile;
    }

    public JobManager getJobManager() {
        return mJobManager;
    }

    public File getLogFile() {
        return mLogFile;
    }

    public File getProfilesFile() {
        return mProfilesFile;
    }

    public TaskManager getTaskManager() {
        return mTaskManager;
    }

    public File getUserDirectory() {
        return mUserDirectory;
    }

    public void load() throws IOException {
        if (mProfilesFile.exists()) {
            mStorage = mStorage.open(mProfilesFile);

            var taskItems = mTaskManager.getIdToItem();
            taskItems.clear();
            taskItems.putAll(mStorage.getTasks());

            var jobItems = mJobManager.getIdToItem();
            jobItems.clear();
            jobItems.putAll(mStorage.getJobs());
            mJobManager.loadHistory();
        } else {
            mStorage = new Storage();
        }
    }

    public void save() throws IOException {
        mStorage.setJobs(mJobManager.getIdToItem());
        mStorage.setTasks(mTaskManager.getIdToItem());
        String json = mStorage.save(mProfilesFile);
        String tag = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        FileUtils.writeStringToFile(mProfilesBackupFile, String.format("%s=%s\n", tag, json), Charset.defaultCharset(), true);
        load();
    }

    private static class Holder {

        private static final StorageManager INSTANCE = new StorageManager();
    }
}
