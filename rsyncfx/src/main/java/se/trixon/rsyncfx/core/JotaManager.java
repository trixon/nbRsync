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
import se.trixon.almond.util.Xlog;

/**
 *
 * @author Patrik Karlström
 */
class JotaManager {

    private final File mDirectory;
    private final File mHistoryFile;
    private final File mJobBakFile;
    private final JobManager mJobManager = JobManager.getInstance();
    private JotaJson mJotaJson = new JotaJson();
    private final File mLogFile;
    private final File mProfilesFile;
    private final TaskManager mTaskManager = TaskManager.getInstance();

    public static JotaManager getInstance() {
        return Holder.INSTANCE;
    }

    private JotaManager() {
        mDirectory = new File(System.getProperty("user.home"), ".config/jotasync");
        mHistoryFile = new File(mDirectory, "jotasync.history");
        mProfilesFile = new File(mDirectory, "jotasync2.profiles");
        mJobBakFile = new File(mDirectory, "jotasync.profiles.bak");
        mLogFile = new File(mDirectory, "jotasync.log");

        try {
            FileUtils.forceMkdir(mDirectory);
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }
    }

    public File getDirectory() {
        return mDirectory;
    }

    public int getFileFormatVersion() {
        return mJotaJson.getFileFormatVersion();
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

    public void load() throws IOException {
        if (mProfilesFile.exists()) {
            mJotaJson = mJotaJson.open(mProfilesFile);
            mTaskManager.setTasks(mJotaJson.getTasks());
            mJobManager.setJobs(mJotaJson.getJobs());
            mJobManager.loadHistory();
        } else {
            mJotaJson = new JotaJson();
        }
    }

    public void save() throws IOException {
        mJotaJson.setJobs(mJobManager.getJobs());
        mJotaJson.setTasks(mTaskManager.getTasks());
        String json = mJotaJson.save(mProfilesFile);
        String tag = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        FileUtils.writeStringToFile(mJobBakFile, String.format("%s=%s\n", tag, json), Charset.defaultCharset(), true);
        load();
    }

    private static class Holder {

        private static final JotaManager INSTANCE = new JotaManager();
    }
}
