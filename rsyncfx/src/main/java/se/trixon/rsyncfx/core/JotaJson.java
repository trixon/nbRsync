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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import org.apache.commons.io.FileUtils;
import se.trixon.rsyncfx.core.job.Job;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class JotaJson {

    private static final int FILE_FORMAT_VERSION = 2;
    private static final Gson GSON = new GsonBuilder()
            .addSerializationExclusionStrategy(new ExclusionStrategy() {

                @Override
                public boolean shouldSkipClass(Class<?> arg0) {
                    return false;
                }

                @Override
                public boolean shouldSkipField(FieldAttributes arg0) {
                    return arg0.getDeclaringClass() == Task.class && arg0.getName().equalsIgnoreCase("mCommand")
                            || arg0.getDeclaringClass() == Task.class && arg0.getName().equalsIgnoreCase("mHistory")
                            || arg0.getDeclaringClass() == Job.class && arg0.getName().equalsIgnoreCase("mTasks")
                            || arg0.getDeclaringClass() == Job.class && arg0.getName().equalsIgnoreCase("mHistory");
                }
            })
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    @SerializedName("format_version")
    private int mFileFormatVersion;
    @SerializedName("jobs")
    private final LinkedList<Job> mJobs = new LinkedList<>();
    @SerializedName("tasks")
    private final LinkedList<Task> mTasks = new LinkedList<>();

    public static JotaJson open(File file) throws IOException, JsonSyntaxException {
        String json = FileUtils.readFileToString(file, Charset.defaultCharset());

        JotaJson profiles = GSON.fromJson(json, JotaJson.class);

        if (profiles.mFileFormatVersion != FILE_FORMAT_VERSION) {
            //TODO Handle file format version change
        }

        return profiles;
    }

    public int getFileFormatVersion() {
        return mFileFormatVersion;
    }

    public LinkedList<Job> getJobs() {
        return mJobs;
    }

    public LinkedList<Task> getTasks() {
        return mTasks;
    }

    public String save(File file) throws IOException {
        mFileFormatVersion = FILE_FORMAT_VERSION;
        String json = GSON.toJson(this);
        FileUtils.writeStringToFile(file, json, Charset.defaultCharset());

        return json;
    }

    void setJobs(LinkedList<Job> jobs) {
        mJobs.clear();
        mJobs.addAll(jobs);
    }

    void setTasks(LinkedList<Task> tasks) {
        mTasks.clear();
        mTasks.addAll(tasks);
    }
}
