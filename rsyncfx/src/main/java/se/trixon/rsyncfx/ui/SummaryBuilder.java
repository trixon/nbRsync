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

import static j2html.TagCreator.*;
import j2html.tags.specialized.DivTag;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.rsyncfx.core.ExecuteItem;
import se.trixon.rsyncfx.core.job.Job;
import se.trixon.rsyncfx.ui.editor.BaseEditor;

/**
 *
 * @author Patrik Karlström
 */
public class SummaryBuilder {

    private final ResourceBundle mBundle = NbBundle.getBundle(BaseEditor.class);

    public SummaryBuilder() {
    }

    public String getHtml(Job job) {
        var exec = job.getExecuteSection();

        var html = body(div(
                h1(job.getName()),
                getExecTag(exec.getBefore(), mBundle.getString("JobEditor.runBefore")),
                getExecTag(exec.getAfterFail(), mBundle.getString("JobEditor.runAfterFail")),
                getExecTag(exec.getAfterOk(), mBundle.getString("JobEditor.runAfterOk")),
                getExecTag(exec.getAfter(), mBundle.getString("JobEditor.runAfter")),
                each(job.getTasks(), task -> div(
                hr(),
                h2(task.getName()),
                 p(join(b(Dict.SOURCE.toString()), br(), i(task.getSource()))),
                p(join(b(Dict.DESTINATION.toString()), br(), i(task.getDestination()))),
                getExecTag(task.getExecuteSection().getBefore(), mBundle.getString("TaskEditor.runBefore")),
                getExecTag(task.getExecuteSection().getAfterFail(), mBundle.getString("TaskEditor.runAfterFail")),
                getExecTag(task.getExecuteSection().getAfterOk(), mBundle.getString("TaskEditor.runAfterOk")),
                getExecTag(task.getExecuteSection().getAfter(), mBundle.getString("TaskEditor.runAfter")),
                iff(task.getExecuteSection().isJobHaltOnError(), p(mBundle.getString("TaskEditor.stopJobOnError"))),
                h3("rsync"),
                p(task.getCommandAsString())
        )
                ),
                hr()
        ));

        return html.render();
    }

    private DivTag getExecTag(ExecuteItem item, String text) {
        if (item.isEnabled()) {
            return div(
                    p(join(b(text), br(), i(item.getCommand()))),
                    iff(item.isHaltOnError(), p(Dict.STOP_ON_ERROR.toString()))
            );
        } else {
            return null;
        }
    }
}
