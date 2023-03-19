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
package se.trixon.jotasync.ui;

import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.Options;
import se.trixon.jotasync.core.ExecutorManager;
import se.trixon.jotasync.core.Monitor;
import se.trixon.jotasync.core.MonitorItem;
import se.trixon.jotasync.core.ProcessCallbacks;
import se.trixon.jotasync.core.ProcessEvent;
import se.trixon.jotasync.core.ProcessState;
import se.trixon.jotasync.core.Progress;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class LogTab extends BaseTab implements ProcessCallbacks {

    private final BorderPane mBorderPane = new BorderPane();
    private Action mCancelAction;
    private ButtonBase mCancelButton;
    private final MonitorItem mDelMonitorItem = new MonitorItem(Dict.DELETIONS.toString());
    private Action mEditAction;
    private final MonitorItem mErrMonitorItem = new MonitorItem(Dict.Dialog.ERRORS.toString());
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final Job mJob;
    private final Jota mJota = Jota.getInstance();
    private boolean mLastLineWasBlank;
    private boolean mLastRowWasProgress;
    private final Monitor mMonitor;
    private final Options mOptions = Options.getInstance();
    private final Progress mProgress = new Progress();
    private final ProgressBar mProgressBar = new ProgressBar();
    private Action mSaveAction;
    private Action mStartAction;
    private ButtonBase mStartButton;
    private final MonitorItem mStdMonitorItem = new MonitorItem(Dict.LOG.toString());
    private final TabPane mTabPane = new TabPane();
    private ToolBar mToolBar;

    public LogTab(Job job) {
        mJob = job;
        createUI();
        mMonitor = new Monitor(mDelMonitorItem, mErrMonitorItem, mStdMonitorItem);
        initListeners();
        updateNightMode();
        mMonitor.start();
    }

    public void clear() {
        mProgressBar.setProgress(-1);
        mMonitor.clear();
        mTabPane.getTabs().removeAll(mErrMonitorItem.getTab(), mDelMonitorItem.getTab());
    }

    public Job getJob() {
        return mJob;
    }

    synchronized public void log(ProcessEvent processEvent, String string) {
        String line = string + "\n";
//mOptions.
        var lp = mStdMonitorItem.getTab();
        var splitDelete = true;//mOptions.isSplitDeletions()
        var splitErrors = true;//mOptions.isSplitErrors()
        if (splitDelete && StringUtils.startsWith(line, "deleting ")) {
            lp = mDelMonitorItem.getTab();
            if (!mTabPane.getTabs().contains(lp)) {
                var elp = lp;
//                FxHelper.runLater(() -> mTabPane.getTabs().add(elp));
            }
        } else if (splitErrors && (StringUtils.startsWith(line, "rsync: ") || StringUtils.startsWith(line, "rsync error: "))) {
            lp = mErrMonitorItem.getTab();
            if (!mTabPane.getTabs().contains(lp)) {
                var elp = lp;
//                FxHelper.runLater(() -> mTabPane.getTabs().add(elp));
            }
        }

        if (mProgress.parse(line)) {//is line a percent progress indicator?
//            mProgressBar.setIndeterminate(false);
//            mProgressBar.setStringPainted(true);

            mProgressBar.setProgress(mProgress.getPercentage());
//            mProgressBar.setString(mProgress.toString());
            System.out.println(mProgress.toString());
            Jota.getStatusBar().setProgress(mProgress.getPercentage());
            Jota.getStatusBar().setText(mProgress.toString());
            mLastRowWasProgress = true;
        } else {
            if (mLastRowWasProgress && mLastLineWasBlank) {
                try {
                    int size = lp.getText().length();
//                    lp.getTextArea().replaceRange(null, size - 1, size);
                } catch (IllegalArgumentException e) {
                }
            }
            //lp.add(line);
            mMonitor.add(line);
            mLastLineWasBlank = StringUtils.isBlank(line);
            mLastRowWasProgress = false;
        }
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Job job, Task task, String string) {
//        FxHelper.runLater(() -> {
        switch (processEvent) {
            case STARTED:
                start();
                mMonitor.start();
                mTabPane.getSelectionModel().select(0);
                //updateTitle(job, "b"); //TODO Make tab text bold for running jobs
                break;

            case OUT:
            case ERR:
                log(processEvent, string);
                break;

            case CANCELED:
                log(ProcessEvent.OUT, String.format("\n\n%s", Dict.JOB_INTERRUPTED.toString()));
                mJob.setProcessStateProperty(ProcessState.STARTABLE);
                //updateTitle(job, "i");
                break;

            case FAILED:
                log(ProcessEvent.OUT, String.format("\n\n%s", Dict.JOB_FAILED.toString()));
                //updateTitle(job, "strike");
                break;

            case FINISHED:
                if (string != null) {
                    log(ProcessEvent.OUT, string);
                }
                //updateTitle(job, "normal");
                mJob.setProcessStateProperty(ProcessState.STARTABLE);
                FxHelper.runLater(() -> mProgressBar.setProgress(1.0));
                mMonitor.stop();
                break;

            default:
                break;
        }
//        });
    }

    @Override
    public void updateNightMode() {
        mCancelAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(Jota.getIconSizeToolBarInt()));
        mEditAction.setGraphic(MaterialIcon._Content.CREATE.getImageView(Jota.getIconSizeToolBarInt()));
        mStartAction.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(Jota.getIconSizeToolBarInt()));
        mSaveAction.setGraphic(MaterialIcon._Content.SAVE.getImageView(Jota.getIconSizeToolBarInt()));
    }

    private void createUI() {
        mCancelAction = new Action(Dict.CANCEL.toString(), actionEvent -> {
            mExecutorManager.stop(mJob);
        });

        mEditAction = new Action(Dict.EDIT.toString(), actionEvent -> {
            mJota.getGlobalState().put(Jota.GSC_EDITOR, mJob);
        });

        mStartAction = new Action(Dict.START.toString(), actionEvent -> {
            mExecutorManager.requestStart(mJob);
        });

        mSaveAction = new Action(Dict.SAVE.toString(), actionEvent -> {
        });

        var actions = List.of(
                mCancelAction,
                mStartAction,
                mEditAction,
                mSaveAction
        );

        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.undecorateButtons(mToolBar.getItems().stream());
        FxHelper.slimToolBar(mToolBar);

        var box = new HBox(mToolBar, mProgressBar);
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(mProgressBar, Priority.ALWAYS);

        mBorderPane.setTop(box);
        mBorderPane.setCenter(mTabPane);

        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mTabPane.getTabs().setAll(mStdMonitorItem.getTab(), mErrMonitorItem.getTab(), mDelMonitorItem.getTab());

        setContent(mBorderPane);
        setText(mJob.getName());
        mProgressBar.setMaxWidth(Double.MAX_VALUE);

        mCancelButton = FxHelper.getButtonForAction(mCancelAction, mToolBar.getItems());
        mStartButton = FxHelper.getButtonForAction(mStartAction, mToolBar.getItems());

        closableProperty().bind(mJob.processStateProperty().isEqualTo(ProcessState.STARTABLE));

        var editButton = FxHelper.getButtonForAction(mEditAction, mToolBar.getItems());
        mEditAction.disabledProperty().bind(mJob.processStateProperty().isNotEqualTo(ProcessState.STARTABLE));

        var saveButton = FxHelper.getButtonForAction(mSaveAction, mToolBar.getItems());
        mSaveAction.disabledProperty().bind(mJob.processStateProperty().isNotEqualTo(ProcessState.STARTABLE));
    }

    private void initListeners() {
        mJob.processStateProperty().addListener((p, o, n) -> {
            FxHelper.runLater(() -> {
                mToolBar.getItems().removeAll(mCancelButton, mStartButton);

                switch (n) {
                    case CANCELABLE ->
                        start();

                    case CLOSEABLE -> {
                        System.out.println(n);
                        mProgressBar.setProgress(1.0);
                    }

                    case STARTABLE -> {
                        mProgressBar.setProgress(1.0);
                        mToolBar.getItems().add(0, mStartButton);
                    }

                    default ->
                        throw new AssertionError();
                }
            });
        });
    }

    private void start() {
        mToolBar.getItems().add(0, mCancelButton);
        clear();
    }

}
