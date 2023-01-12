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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import se.trixon.rsyncfx.core.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class JobManager {

    private List<String> mHistoryLines = new ArrayList<>();
    private final ArrayList<Job> mJobs = new ArrayList<>();

    public static JobManager getInstance() {
        return Holder.INSTANCE;
    }

    private JobManager() {
    }

    public Object[] getArray() {
        return mJobs.toArray();
    }

    public Job getJobById(long id) {
        for (var job : mJobs) {
            if (job.getId() == id) {
                return job;
            }
        }

        return null;
    }

    public ArrayList<Job> getJobs() {
        return mJobs;
    }

    public boolean hasJobs() {
        return !getJobs().isEmpty();
    }

    void loadHistory() {
        try {
            mHistoryLines = FileUtils.readLines(StorageManager.getInstance().getHistoryFile(), Charset.defaultCharset());
            for (var job : mJobs) {
                loadHistory(job);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void setJobs(ArrayList<Job> jobs) {
        mJobs.clear();
        mJobs.addAll(jobs);
        mJobs.forEach(job -> {
            job.setTasks(TaskManager.getInstance().getTasks(job.getTaskIds()));
        });
    }

    private void loadHistory(Job job) {
        var builder = new StringBuilder();

        for (var line : mHistoryLines) {
            var id = String.valueOf(job.getId());
            if (StringUtils.contains(line, id)) {
                builder.append(StringUtils.remove(line, id + " ")).append("\n");
            }
        }

        job.setHistory(builder.toString());
    }

    private static class Holder {

        private static final JobManager INSTANCE = new JobManager();
    }
}
