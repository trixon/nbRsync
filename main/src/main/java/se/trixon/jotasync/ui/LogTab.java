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
package se.trixon.jotasync.ui;

import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.core.ExecutorManager;
import se.trixon.jotasync.core.ProcessCallbacks;
import se.trixon.jotasync.core.ProcessEvent;
import se.trixon.jotasync.core.ProcessState;
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
    private final LogListView mDeletionsListView = new LogListView();
    private Action mEditAction;
    private final LogListView mErrorsListView = new LogListView();
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final Job mJob;
    private final Jota mJota = Jota.getInstance();
    private final LogListView mLogListView = new LogListView();
    private final ProgressBar mProgressBar = new ProgressBar(0.75);
    private Action mSaveAction;
    private Action mStartAction;
    private ButtonBase mStartButton;
    private final TabPane mTabPane = new TabPane();
    private final TextField mTextField = TextFields.createClearableTextField();
    private ToolBar mToolBar;

    public LogTab(Job job) {
        mJob = job;
        createUI();
        initListeners();
        updateNightMode();
    }

    public Job getJob() {
        return mJob;
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Job job, Task task, Object object) {
        FxHelper.runLater(() -> {
            mLogListView.getItems().add(object.toString());
            if (processEvent == ProcessEvent.FINISHED) {
                mJob.setProcessStateProperty(ProcessState.STARTABLE);
            }
        });
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
            mJob.setProcessStateProperty(ProcessState.STARTABLE);
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

        var box = new HBox(mToolBar, mTextField, mProgressBar);
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(mProgressBar, Priority.ALWAYS);

        mBorderPane.setTop(box);
        mBorderPane.setCenter(mTabPane);

        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mTabPane.getTabs().addAll(
                new Tab(Dict.LOG.toString(), mLogListView),
                new Tab(Dict.Dialog.ERRORS.toString(), mErrorsListView),
                new Tab(Dict.DELETIONS.toString(), mDeletionsListView)
        );

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
                        mToolBar.getItems().add(0, mCancelButton);
                    case CLOSEABLE ->
                        System.out.println(n);
                    case STARTABLE ->
                        mToolBar.getItems().add(0, mStartButton);
                    default ->
                        throw new AssertionError();
                }
            });
        });
    }
}
