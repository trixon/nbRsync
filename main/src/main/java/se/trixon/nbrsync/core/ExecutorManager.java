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
package se.trixon.nbrsync.core;

import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.HtmlPanel;
import se.trixon.nbrsync.core.job.Job;
import se.trixon.nbrsync.core.job.JobValidator;
import se.trixon.nbrsync.ui.SummaryBuilder;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ExecutorManager {

    private final HashMap<String, JobExecutor> mJobExecutors = new HashMap<>();
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
        if (job.isLocked()) {
            NbMessage.error(Dict.Dialog.ERROR.toString(), "Job already running.");
            return;
        }

        var jobValidator = new JobValidator(job);
        var htmlPanel = new HtmlPanel();
        var dryRunButton = new JButton(Dict.DRY_RUN.toString());
        var d = new DialogDescriptor(
                htmlPanel,
                Dict.RUN.toString(),
                true,
                new Object[]{Dict.CANCEL.toString(), Dict.RUN.toString(), dryRunButton},
                dryRunButton,
                0,
                null,
                null
        );

        SwingUtilities.invokeLater(() -> {
            htmlPanel.setPreferredSize(SwingHelper.getUIScaledDim(600, 660));
        });

        if (jobValidator.isValid()) {
            htmlPanel.setHtml(mSummaryBuilder.getHtml(job));
            SwingHelper.runLaterDelayed(100, () -> dryRunButton.requestFocus());
            var result = DialogDisplayer.getDefault().notify(d);

            if (result == Dict.RUN.toString()) {
                start(job, false);
            } else if (result == dryRunButton) {
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
        job.setLocked(true);
        jobExecutor.run();
    }

    private static class Holder {

        private static final ExecutorManager INSTANCE = new ExecutorManager();
    }
}
