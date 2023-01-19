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
package se.trixon.rsyncfx.core.job;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.rsyncfx.core.BaseItem;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class Job extends BaseItem {

    public static OUTPUT TO_STRING = OUTPUT.VERBOSE;

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
    private transient StringBuilder mSummaryBuilder;
    @SerializedName("tasks")
    private ArrayList<Long> mTaskIds = new ArrayList<>();
    private transient List<Task> mTasks = new ArrayList<>();

    public Job() {
        mExecuteSection = new JobExecuteSection();
    }

    public Job(long id, String name, String description, String comment) {
        mId = id;
        mName = name;
        mDescription = description;
        mNote = comment;
        mExecuteSection = new JobExecuteSection();
    }

    public String getCaption(boolean verbose) {
        String caption;
        if (verbose) {
            String template = "<html><center><h2><b>%s</b></h2><p><i>%s</i></p><br />%s <font size=\"6\">%s</font></center></html>";
            caption = String.format(template, mName, mDescription, getLastRunDateTime("-"), getLastRunStatus());
        } else {
            String template = "<html><b>%s</b><i>%s</i> %s %s</html>";
            String description = mDescription;
            if (StringUtils.isEmpty(description)) {
                description = "&nbsp;";
            } else {
                description = String.format("(%s)", description);
            }
            caption = String.format(template, mName, description, getLastRunDateTime(""), getLastRunStatus());
        }

        return caption;
    }

    public JobExecuteSection getExecuteSection() {
        return mExecuteSection;
    }

    public int getLogMode() {
        return mLogMode;
    }

    public String getSummaryAsHtml() {
        mSummaryBuilder = new StringBuilder("<html><body>");
        mSummaryBuilder.append("<h1>").append(getName()).append("</h1>");
        var bundle = NbBundle.getBundle(Job.class);

        addOptionalToSummary(mExecuteSection.isBefore(), mExecuteSection.getBeforeCommand(), bundle.getString("JobPanel.beforePanel.header"));
        if (mExecuteSection.isBefore() && mExecuteSection.isBeforeHaltOnError()) {
            mSummaryBuilder.append(Dict.STOP_ON_ERROR.toString());
        }

        addOptionalToSummary(mExecuteSection.isAfterFailure(), mExecuteSection.getAfterFailureCommand(), bundle.getString("JobPanel.afterFailurePanel.header"));
        addOptionalToSummary(mExecuteSection.isAfterSuccess(), mExecuteSection.getAfterSuccessCommand(), bundle.getString("JobPanel.afterSuccessPanel.header"));
        addOptionalToSummary(mExecuteSection.isAfter(), mExecuteSection.getAfterCommand(), bundle.getString("JobPanel.afterPanel.header"));

        for (Task task : getTasks()) {
            mSummaryBuilder.append("<hr>");
            mSummaryBuilder.append(task.getSummaryAsHtml());
        }

        mSummaryBuilder.append("</body></html>");

        return mSummaryBuilder.toString();
    }

    public ArrayList<Long> getTaskIds() {
        return mTaskIds;
    }

    public List<Task> getTasks() {
        if (mTasks == null) {
            mTasks = new ArrayList<>();
        }

        return mTasks;
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

    public void setTasks(List<Task> tasksSkip) {
        mTasks = tasksSkip;
    }

    public void setTasks(DefaultListModel model) {
        mTasks.clear();
        mTaskIds.clear();

        for (Object object : model.toArray()) {
            Task task = (Task) object;
            mTasks.add(task);
            mTaskIds.add(task.getId());
        }
    }

    @Override
    public String toString() {
        if (TO_STRING == OUTPUT.NORMAL) {
            return mName;
        } else {
            String description = StringUtils.isBlank(mDescription) ? "&nbsp;" : mDescription;

            return String.format("<html><b>%s</b><br /><i>%s</i></html>", mName, description);
        }
    }

    private void addOptionalToSummary(boolean active, String command, String header) {
        if (active) {
            mSummaryBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i></p>", header, command));
        }
    }

    public enum OUTPUT {

        NORMAL, VERBOSE;
    }
}
