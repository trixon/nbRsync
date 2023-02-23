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

import java.io.IOException;
import se.trixon.jotasync.Options;
import se.trixon.jotasync.core.BaseValidator;
import se.trixon.jotasync.core.task.TaskValidator;

/**
 *
 * @author Patrik Karlström
 */
public class JobValidator extends BaseValidator {

    private final StringBuilder mHtmlBuilder;
    private final Job mJob;
    private final Options mOptions = Options.getInstance();
    private boolean mRsync;
    private final StringBuilder mStringBuilder;

    public JobValidator(Job job) {
        mJob = job;
        mStringBuilder = new StringBuilder();
        mHtmlBuilder = new StringBuilder("<html>");

        validateRsync();
        mHtmlBuilder.append("<h1>").append(mJob.getName()).append("</h1>");
        validateExecutors();
        validateTasks();
    }

    @Override
    public void addSummary(String header, String message) {
        mHtmlBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i><br />&nbsp;</p>", header, message));
        mStringBuilder.append(header).append(message).append("\n");
    }

    @Override
    public String getSummary() {
        return mStringBuilder.toString();
    }

    @Override
    public String getSummaryAsHtml() {
        return mHtmlBuilder.toString();
    }

    public boolean isRsync() {
        return mRsync;
    }

    @Override
    public boolean isValid() {
        return !mInvalid;
    }

    private void validateExecutors() {
        var executeSection = mJob.getExecuteSection();

        validateExecutorItem(executeSection.getBefore(), "JobEditor.runBefore");
        validateExecutorItem(executeSection.getAfterFail(), "JobEditor.runAfterFail");
        validateExecutorItem(executeSection.getAfterOk(), "JobEditor.runAfterOk");
        validateExecutorItem(executeSection.getAfter(), "JobEditor.runAfter");
    }

    private void validateRsync() {
        try {
            var processBuilder = new ProcessBuilder(new String[]{mOptions.getRsyncPath(), "--version"});
            var process = processBuilder.start();
            process.waitFor();
            mRsync = true;
        } catch (InterruptedException | IOException ex) {
            mInvalid = true;
            mRsync = false;
            addSummary("Command not found", mOptions.getRsyncPath());
        }
    }

    private void validateTasks() {
        for (var task : mJob.getTasks()) {
            var taskValidator = new TaskValidator(task);
            if (!taskValidator.isValid()) {
                mHtmlBuilder.append("<hr>");
                mHtmlBuilder.append(taskValidator.getSummaryAsHtml());
                mInvalid = true;
            }
        }
    }
}
