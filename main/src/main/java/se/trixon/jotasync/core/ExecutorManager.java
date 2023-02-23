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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.Options;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.job.JobValidator;
import se.trixon.jotasync.ui.App;
import se.trixon.jotasync.ui.SummaryBuilder;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ExecutorManager {

    private final HashMap<String, JobExecutor> mJobExecutors = new HashMap<>();
    private final Jota mJota = Jota.getInstance();
    private final SummaryBuilder mSummaryBuilder = new SummaryBuilder();
    private final WebView mWebView = new WebView();

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
        var name = Options.getInstance().isNightMode() ? "darkWeb.css" : "lightWeb.css";
        mWebView.getEngine().setUserStyleSheetLocation(App.class.getResource(name).toExternalForm());

        var stage = mJota.getStage();
        var alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(stage);
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setResizable(true);
        var dialogPane = alert.getDialogPane();
        dialogPane.setContent(mWebView);
        dialogPane.setPrefSize(FxHelper.getUIScaled(600), FxHelper.getUIScaled(660));
        dialogPane.getChildren().remove(0);//Remove graphics container in order to remove the spacing

        if (jobValidator.isValid()) {
            var runButtonType = new ButtonType(Dict.RUN.toString(), ButtonBar.ButtonData.OK_DONE);
            var dryRunButtonType = new ButtonType(Dict.DRY_RUN.toString(), ButtonBar.ButtonData.NEXT_FORWARD);

            alert.getButtonTypes().setAll(runButtonType, dryRunButtonType, ButtonType.CANCEL);
            alert.setTitle(Dict.RUN.toString());

            mWebView.getEngine().loadContent(mSummaryBuilder.getHtml(job));

            var result = alert.showAndWait();

            if (result.get() == runButtonType) {
                start(job, false);
            } else if (result.get() == dryRunButtonType) {
                start(job, true);
            }
        } else {
            alert.getButtonTypes().setAll(ButtonType.CLOSE);
            alert.setTitle(Dict.Dialog.ERROR_VALIDATION.toString());
            mWebView.getEngine().loadContent(jobValidator.getSummaryAsHtml());

            var result = alert.showAndWait();
        }
    }

    public void start(Job job, boolean dryRun) {
        var jobExecutor = new JobExecutor(job, dryRun);
        mJobExecutors.put(job.getId(), jobExecutor);
        mJota.getGlobalState().put(Jota.GSC_JOB_STARTED, job);
        jobExecutor.start();
    }

    private static class Holder {

        private static final ExecutorManager INSTANCE = new ExecutorManager();
    }
}
