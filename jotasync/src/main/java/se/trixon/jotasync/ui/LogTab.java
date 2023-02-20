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
import java.util.Random;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.RandomStringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.core.ExecutorManager;
import se.trixon.jotasync.core.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class LogTab extends BaseTab {

    private final BorderPane mBorderPane = new BorderPane();
    private final LogListView mDeletionsListView = new LogListView();
    private final LogListView mErrorsListView = new LogListView();
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final Job mJob;
    private final Jota mJota = Jota.getInstance();
    private final LogListView mLogListView = new LogListView();
    private final ProgressBar mProgressBar = new ProgressBar(0.75);
    private final TabPane mTabPane = new TabPane();
    private final TextField mTextField = TextFields.createClearableTextField();

    public LogTab(Job job) {
        mJob = job;
        createUI();
    }

    @Override
    public void updateNightMode() {
    }

    private void createUI() {
        var cancelAction = new Action(Dict.CANCEL.toString(), actionEvent -> {
        });
        cancelAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(Jota.getIconSizeToolBarInt()));

        var editAction = new Action(Dict.EDIT.toString(), actionEvent -> {
            mJota.getGlobalState().put(Jota.GSC_EDITOR, mJob);
        });
        editAction.setGraphic(MaterialIcon._Content.CREATE.getImageView(Jota.getIconSizeToolBarInt()));

        var startAction = new Action(Dict.START.toString(), actionEvent -> {
            mExecutorManager.requestStart(mJob);
        });
        startAction.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(Jota.getIconSizeToolBarInt()));

        var saveAction = new Action(Dict.SAVE.toString(), actionEvent -> {
        });
        saveAction.setGraphic(MaterialIcon._Content.SAVE.getImageView(Jota.getIconSizeToolBarInt()));

        var actions = List.of(
                cancelAction,
                startAction,
                editAction,
                saveAction
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        var box = new HBox(toolBar, mTextField, mProgressBar);
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

        for (int i = 0; i < 1000; i++) {
            mLogListView.getItems().add(RandomStringUtils.randomAlphanumeric(new Random().nextInt(10, 80)));
            mErrorsListView.getItems().add(RandomStringUtils.randomAlphanumeric(new Random().nextInt(10, 80)));
            mDeletionsListView.getItems().add(RandomStringUtils.randomAlphanumeric(new Random().nextInt(10, 80)));
        }

        setContent(mBorderPane);
        setText(mJob.getName());
        mProgressBar.setMaxWidth(Double.MAX_VALUE);
    }

}
