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
package se.trixon.jotasync.core;

import java.util.HashMap;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.HtmlPanel;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.job.JobValidator;
import se.trixon.jotasync.ui.SummaryBuilder;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ExecutorManager {

    private final HashMap<String, JobExecutor> mJobExecutors = new HashMap<>();
    private final Jota mJota = Jota.getInstance();
    private final SummaryBuilder mSummaryBuilder = new SummaryBuilder();

    public static ExecutorManager getInstance() {
        return Holder.INSTANCE;
    }

    private ExecutorManager() {
    }

    public HashMap<String, JobExecutor> getJobExecutors() {
        return mJobExecutors;
    }

    public void requestStart(Job job) {
        var jobValidator = new JobValidator(job);
        var htmlPanel = new HtmlPanel();
        var d = new DialogDescriptor(
                htmlPanel,
                Dict.RUN.toString(),
                true,
                new Object[]{Dict.CANCEL.toString(), Dict.RUN.toString(), Dict.DRY_RUN.toString()},
                Dict.DRY_RUN.toString(),
                0,
                null,
                null
        );

        SwingUtilities.invokeLater(() -> {
            htmlPanel.setPreferredSize(SwingHelper.getUIScaledDim(600, 660));
        });

        if (jobValidator.isValid()) {
            htmlPanel.setHtml(mSummaryBuilder.getHtml(job));

            var result = DialogDisplayer.getDefault().notify(d);

            if (result == Dict.RUN.toString()) {
                start(job, false);
            } else if (result == Dict.DRY_RUN.toString()) {
                start(job, true);
            }
        } else {
            d.setTitle(Dict.Dialog.ERROR_VALIDATION.toString());
            d.setClosingOptions(new String[]{Dict.CLOSE.toString()});
            d.setOptions(new String[]{Dict.CLOSE.toString()});
            htmlPanel.setHtml(jobValidator.getSummaryAsHtml());
            DialogDisplayer.getDefault().notify(d);
        }
    }

    public void start(Job job, boolean dryRun) {
        var jobExecutor = new JobExecutor(job, dryRun);
        mJobExecutors.put(job.getId(), jobExecutor);
        mJota.getGlobalState().put(Jota.GSC_JOB_STARTED, job);
        jobExecutor.start();
    }

    public void stop(Job job) {
        var executor = mJobExecutors.get(job.getId());
        if (executor != null) {
            executor.stopJob();
        }
    }

    private static class Holder {

        private static final ExecutorManager INSTANCE = new ExecutorManager();
    }
}
