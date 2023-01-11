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
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class Job implements Comparable<Job>, Serializable {

    public static OUTPUT TO_STRING = OUTPUT.VERBOSE;

    @SerializedName("cron_active")
    private boolean mCronActive;
    @SerializedName("cron_items")
    private String mCronItems = "";
    @SerializedName("description")
    private String mDescription = "";
    @SerializedName("execute_section")
    private final JobExecuteSection mExecuteSection;
    private String mHistory = "";
    @SerializedName("id")
    private long mId = System.currentTimeMillis();
    @SerializedName("last_run")
    private long mLastRun = -1;
    @SerializedName("last_run_exit_code")
    private int mLastRunExitCode = -1;
    @SerializedName("log_errors")
    private boolean mLogErrors = true;
    @SerializedName("log_mode")
    private int mLogMode = 0;
    @SerializedName("log_output")
    private boolean mLogOutput = true;
    @SerializedName("log_separate_errors")
    private boolean mLogSeparateErrors = true;
    @SerializedName("name")
    private String mName = "";
    @SerializedName("note")
    private String mNote = "";
    private transient StringBuilder mSummaryBuilder;
    @SerializedName("tasks")
    private ArrayList<Long> mTaskIds = new ArrayList<>();
    private List<Task> mTasks = new LinkedList<>();

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

    @Override
    public int compareTo(Job o) {
        return mName.compareTo(o.getName());
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

    public String getCronItems() {
        return mCronItems;
    }

    public String getDescription() {
        return mDescription;
    }

    public JobExecuteSection getExecuteSection() {
        return mExecuteSection;
    }

    public String getHistory() {
        return mHistory;
    }

    public long getId() {
        return mId;
    }

    public long getLastRun() {
        return mLastRun;
    }

    public String getLastRunDateTime(String replacement, long lastRun) {
        String lastRunDateTime = replacement;

        if (lastRun > 0) {
            Date date = new Date(lastRun);
            lastRunDateTime = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(date);
        }

        return lastRunDateTime;
    }

    public String getLastRunDateTime(String replacement) {
        return getLastRunDateTime(replacement, mLastRun);
    }

    public int getLastRunExitCode() {
        return mLastRunExitCode;
    }

    public String getLastRunStatus() {
        String status = "";
        if (mLastRun > 0) {
            status = getLastRunExitCode() == 0 ? "" : "⚠";
            //if (isRunnning()) {
            //    status = "∞";
            //}
        }

        return status;
    }

    public int getLogMode() {
        return mLogMode;
    }

    public String getName() {
        return mName;
    }

    public String getNote() {
        return mNote;
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
            mTasks = new LinkedList<>();
        }

        return mTasks;
    }

    public boolean isCronActive() {
        return mCronActive;
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

    public boolean isValid() {
        return !getName().isEmpty();
    }

    public void setCronActive(boolean cronActive) {
        mCronActive = cronActive;
    }

    public void setCronItems(String cronItems) {
        mCronItems = cronItems;
    }

    public void setDescription(String string) {
        mDescription = string;
    }

    public void setHistory(String history) {
        mHistory = history == null ? "" : history;
    }

    public void setId(long id) {
        mId = id;
    }

    public void setLastRun(long lastRun) {
        mLastRun = lastRun;
    }

    public void setLastRunExitCode(int lastRunExitCode) {
        mLastRunExitCode = lastRunExitCode;
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

    public void setName(String name) {
        mName = name;
    }

    public void setNote(String string) {
        mNote = string;
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
