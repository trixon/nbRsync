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
package se.trixon.nbrsync.core.task;

import se.trixon.nbrsync.core.BaseValidator;

/**
 *
 * @author Patrik Karlström
 */
public class TaskValidator extends BaseValidator {

    private final StringBuilder mHtmlBuilder;
    private final StringBuilder mStringBuilder;
    private final Task mTask;

    public TaskValidator(Task task) {
        mTask = task;
        mHtmlBuilder = new StringBuilder("<h2>").append(mTask.getName()).append("</h2>");
        mStringBuilder = new StringBuilder();

        validateExecutors();
        validateExclusion();
    }

    @Override
    public void addSummary(String header, String message) {
        mHtmlBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i><br />&nbsp;</p>", header, message));
    }

    @Override
    public String getSummary() {
        return mStringBuilder.toString();
    }

    @Override
    public String getSummaryAsHtml() {
        return mHtmlBuilder.toString();
    }

    @Override
    public boolean isValid() {
        return !mInvalid;
    }

    private void validateExclusion() {
        var excludeSection = mTask.getExcludeSection();

        validateExecutorItem(excludeSection.getExternalFile(), "TaskEditor.externalFile");
    }

    private void validateExecutors() {
        var executeSection = mTask.getExecuteSection();

        validateExecutorItem(executeSection.getBefore(), "TaskEditor.runBefore");
        validateExecutorItem(executeSection.getAfterFail(), "TaskEditor.runAfterFail");
        validateExecutorItem(executeSection.getAfterOk(), "TaskEditor.runAfterOk");
        validateExecutorItem(executeSection.getAfter(), "TaskEditor.runAfter");
    }

}
