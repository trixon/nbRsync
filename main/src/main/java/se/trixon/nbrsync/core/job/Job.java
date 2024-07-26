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
package se.trixon.nbrsync.core.job;

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import se.trixon.almond.util.fx.dialogs.cron.CronItem;
import se.trixon.nbrsync.NbRsync;
import se.trixon.nbrsync.core.BaseItem;
import se.trixon.nbrsync.core.ProcessState;
import se.trixon.nbrsync.core.TaskManager;
import se.trixon.nbrsync.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class Job extends BaseItem {

    @SerializedName("cronActivated")
    private boolean mCronActivated;
    @SerializedName("cronItems")
    private String mCronItems = "";
    @SerializedName("executeSection")
    private final JobExecuteSection mExecuteSection;
    private final transient ObjectProperty<ProcessState> mProcessStateProperty = new SimpleObjectProperty<>(ProcessState.STARTABLE);
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

    public String getCronItems() {
        return mCronItems;
    }

    public List<CronItem> getCronItemsAsList() {
        return Arrays.stream(StringUtils.split(getCronItems(), "|")).map(s -> new CronItem(s)).toList();
    }

    public JobExecuteSection getExecuteSection() {
        return mExecuteSection;
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

    public boolean isCronActivated() {
        return mCronActivated;
    }

    public boolean isLocked() {
        return getLockFile().isFile();
    }

    public boolean isScheduled() {
        return isCronActivated() && !getCronItemsAsList().isEmpty();
    }

    public ObjectProperty<ProcessState> processStateProperty() {
        return mProcessStateProperty;
    }

    public void setCronActivated(boolean cronActivated) {
        mCronActivated = cronActivated;
    }

    public void setCronItems(String cronItems) {
        mCronItems = cronItems;
    }

    public void setLocked(boolean locked) {
        try {
            if (locked) {
                FileUtils.touch(getLockFile());
            } else {
                FileUtils.forceDelete(getLockFile());
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
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

    private File getLockFile() {
        return new File(NbRsync.getRunningJobsDirectory(), getId());
    }
}
