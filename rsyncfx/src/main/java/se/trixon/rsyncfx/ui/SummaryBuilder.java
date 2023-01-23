/*
 * Copyright 2023 Patrik Karlström.
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
package se.trixon.rsyncfx.ui;

import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.rsyncfx.core.job.Job;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class SummaryBuilder {

    private Job mJob;
    private StringBuilder mJobBuilder;
    private StringBuilder mTaskBuilder;

    public SummaryBuilder() {
    }

    public String getHtml(Job job) {
        mJob = job;

        return getSummaryAsHtmlJob();
    }

    private void addOptionalForJob(boolean active, String command, String header) {
        if (active) {
            mJobBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i></p>", header, command));
        }
    }

    private void addOptionalForTask(boolean active, String command, String header) {
        if (active) {
            mTaskBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i></p>", header, command));
        }
    }

    private String getSummaryAsHtml(Task task) {
        mTaskBuilder = new StringBuilder("<h2>").append(task.getName()).append("</h2>");

        addOptionalForTask(true, task.getSource(), Dict.SOURCE.toString());
        addOptionalForTask(true, task.getDestination(), Dict.DESTINATION.toString());

        var bundle = NbBundle.getBundle(Task.class);
        var mExecuteSection = task.getExecuteSection();
        addOptionalForTask(mExecuteSection.getBefore().isEnabled(), mExecuteSection.getBefore().getCommand(), bundle.getString("TaskExecutePanel.beforePanel.header"));
        if (mExecuteSection.getBefore().isEnabled() && mExecuteSection.getBefore().isHaltOnError()) {
            mTaskBuilder.append(Dict.STOP_ON_ERROR.toString());
        }

        addOptionalForTask(mExecuteSection.getAfterFail().isEnabled(), mExecuteSection.getAfterFail().getCommand(), bundle.getString("TaskExecutePanel.afterFailurePanel.header"));
        if (mExecuteSection.getAfterFail().isEnabled() && mExecuteSection.getAfterFail().isHaltOnError()) {
            mTaskBuilder.append(Dict.STOP_ON_ERROR.toString());
        }

        addOptionalForTask(mExecuteSection.getAfterOk().isEnabled(), mExecuteSection.getAfterOk().getCommand(), bundle.getString("TaskExecutePanel.afterSuccessPanel.header"));
        if (mExecuteSection.getAfterOk().isEnabled() && mExecuteSection.getAfterOk().isHaltOnError()) {
            mTaskBuilder.append(Dict.STOP_ON_ERROR.toString());
        }

        addOptionalForTask(mExecuteSection.getAfter().isEnabled(), mExecuteSection.getAfter().getCommand(), bundle.getString("TaskExecutePanel.afterPanel.header"));
        if (mExecuteSection.getAfter().isEnabled() && mExecuteSection.getAfter().isHaltOnError()) {
            mTaskBuilder.append(Dict.STOP_ON_ERROR.toString());
        }

        if (mExecuteSection.isJobHaltOnError()) {
            mTaskBuilder.append("<p>").append(bundle.getString("TaskExecutePanel.jobHaltOnErrorCheckBox.text")).append("</p>");
        }

        mTaskBuilder.append("<h2>rsync</h2>").append(task.getCommandAsString());

        return mTaskBuilder.toString();
    }

    private String getSummaryAsHtmlJob() {
        mJobBuilder = new StringBuilder("<html><body>");
        mJobBuilder.append("<h1>").append(mJob.getName()).append("</h1>");
        var bundle = NbBundle.getBundle(Job.class);
        var mExecuteSection = mJob.getExecuteSection();
        addOptionalForJob(mExecuteSection.getBefore().isEnabled(), mExecuteSection.getBefore().getCommand(), bundle.getString("JobPanel.beforePanel.header"));
        if (mExecuteSection.getBefore().isEnabled() && mExecuteSection.getBefore().isHaltOnError()) {
            mJobBuilder.append(Dict.STOP_ON_ERROR.toString());
        }

        addOptionalForJob(mExecuteSection.getAfterFail().isEnabled(), mExecuteSection.getAfterFail().getCommand(), bundle.getString("JobPanel.afterFailurePanel.header"));
        addOptionalForJob(mExecuteSection.getAfterOk().isEnabled(), mExecuteSection.getAfterOk().getCommand(), bundle.getString("JobPanel.afterSuccessPanel.header"));
        addOptionalForJob(mExecuteSection.getAfter().isEnabled(), mExecuteSection.getAfter().getCommand(), bundle.getString("JobPanel.afterPanel.header"));

        for (Task task : mJob.getTasks()) {
            mJobBuilder.append("<hr>");
            mJobBuilder.append(getSummaryAsHtml(task));
        }

        mJobBuilder.append("</body></html>");

        return mJobBuilder.toString();
    }
}
