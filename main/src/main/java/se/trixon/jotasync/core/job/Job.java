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
package se.trixon.jotasync.core.job;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import se.trixon.jotasync.core.BaseItem;
import se.trixon.jotasync.core.ProcessState;
import se.trixon.jotasync.core.TaskManager;
import se.trixon.jotasync.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class Job extends BaseItem {

    @SerializedName("executeSection")
    private final JobExecuteSection mExecuteSection;
    @SerializedName("logErrors")
    private boolean mLogErrors = true;
    @SerializedName("logMode")
    private int mLogMode = 0;
    @SerializedName("logOutput")
    private boolean mLogOutput = true;
    @SerializedName("logSeparateErrors")
    private boolean mLogSeparateErrors = true;
    private transient ObjectProperty<ProcessState> mProcessStateProperty = new SimpleObjectProperty<>(ProcessState.STARTABLE);
    @SerializedName("tasks")
    private ArrayList<String> mTaskIds = new ArrayList<>();

    public Job() {
        mExecuteSection = new JobExecuteSection();
    }

    public Job(String id, String name, String description) {
        mId = id;
        mName = name;
        mDescription = description;
        mExecuteSection = new JobExecuteSection();
    }

    public JobExecuteSection getExecuteSection() {
        return mExecuteSection;
    }

    public int getLogMode() {
        return mLogMode;
    }

    public ProcessState getProcessState() {
        return mProcessStateProperty.get();
    }

    public ArrayList<String> getTaskIds() {
        return mTaskIds;
    }

    public ArrayList<Task> getTasks() {
        var tasks = new ArrayList<Task>();
        getTaskIds().stream()
                .map(id -> TaskManager.getInstance().getIdToItem().get(id))
                .filter(task -> (task != null))
                .forEachOrdered(task -> {
                    tasks.add(task);
                });

        return tasks;
    }

    public boolean isLogErrors() {
        return mLogErrors;
    }

    public boolean isLogOutput() {
        return mLogOutput;
    }

    public boolean isLogSeparateErrors() {
        return mLogSeparateErrors;
    }

    public ObjectProperty<ProcessState> processStateProperty() {
        return mProcessStateProperty;
    }

    public void setLogErrors(boolean logErrors) {
        mLogErrors = logErrors;
    }

    public void setLogMode(int logMode) {
        mLogMode = logMode;
    }

    public void setLogOutput(boolean logOutput) {
        mLogOutput = logOutput;
    }

    public void setLogSeparateErrors(boolean logSeparateErrors) {
        mLogSeparateErrors = logSeparateErrors;
    }

    public void setProcessStateProperty(ProcessState processState) {
        mProcessStateProperty.set(processState);
    }

    public void setTaskIds(ArrayList<String> taskIds) {
        mTaskIds = taskIds;
    }

    @Override
    public String toString() {
        return getName();
    }
}
