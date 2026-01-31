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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javafx.collections.ObservableMap;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import se.trixon.nbrsync.core.job.Job;
import se.trixon.nbrsync.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class StorageManager {

    public final static JsonMapper JSON = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .build();

    private final File mHistoryFile;
    private final JobManager mJobManager = JobManager.getInstance();
    private final File mProfilesFile;
    private Storage mStorage = new Storage();
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private final File mUserDirectory;

    public static StorageManager getInstance() {
        return Holder.INSTANCE;
    }

    public static void save() {
        try {
            StorageManager.getInstance().saveToFile();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private StorageManager() {
        mUserDirectory = Places.getUserDirectory();

        mProfilesFile = new File(mUserDirectory, "profiles.json");
        mHistoryFile = new File(mUserDirectory, "var/history");
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
            mStorage = Storage.open(mProfilesFile);

            var taskItems = mTaskManager.getIdToItem();
            taskItems.clear();
            taskItems.putAll(mStorage.getTasks());

            var jobItems = mJobManager.getIdToItem();
            jobItems.clear();
            jobItems.putAll(mStorage.getJobs());
        } else {
            mStorage = new Storage();
        }
    }

    private void saveToFile() throws IOException {
        mStorage.setJobs(mJobManager.getIdToItem());
        mStorage.setTasks(mTaskManager.getIdToItem());
        mStorage.save(mProfilesFile);

        load(); //This will refresh and sort ListViews
    }

    private static class Holder {

        private static final StorageManager INSTANCE = new StorageManager();
    }

    public static class Storage {

        private static final int FILE_FORMAT_VERSION = 1;
        @JsonProperty("fileFormatVersion")
        private int mFileFormatVersion;
        @JsonProperty("jobs")
        private final HashMap<String, Job> mJobs = new HashMap<>();
        @JsonProperty("tasks")
        private final HashMap<String, Task> mTasks = new HashMap<>();

        public static Storage open(File file) throws IOException {
            var storage = JSON.readValue(file, Storage.class);
            if (storage.mFileFormatVersion != FILE_FORMAT_VERSION) {
                //TODO Handle file format version change
            }

            return storage;
        }

        public int getFileFormatVersion() {
            return mFileFormatVersion;
        }

        public HashMap<String, Job> getJobs() {
            return mJobs;
        }

        public HashMap<String, Task> getTasks() {
            return mTasks;
        }

        public void save(File file) throws IOException {
            mFileFormatVersion = FILE_FORMAT_VERSION;
            JSON.writeValue(file, this);
        }

        void setJobs(ObservableMap<String, Job> jobs) {
            mJobs.clear();
            mJobs.putAll(jobs);
        }

        void setTasks(ObservableMap<String, Task> tasks) {
            mTasks.clear();
            mTasks.putAll(tasks);
        }
    }
}
