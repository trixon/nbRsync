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

import java.util.prefs.Preferences;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.core.ExecutorManager;
import se.trixon.jotasync.core.JobManager;
import se.trixon.jotasync.core.job.Job;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class SpeedDialButton {

    private final Button mButton = new Button();
    private final Label mDescLabel = new Label();
    private MenuItem mEditMenuItem;
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final int mIndex;
    private Job mJob;
    private final JobManager mJobManager = JobManager.getInstance();
    private final Label mLastRunLabel = new Label();
    private final Label mNameLabel = new Label();
    private final Preferences mPreferences = NbPreferences.forModule(SpeedDialButton.class).node("speedDial");
    private MenuItem mResetMenuItem;
    private final StackPane mRoot = new StackPane();
    private final Jota mJota = Jota.getInstance();

    public SpeedDialButton(int index) {
        mIndex = index;
        createUI();
        initListeners();
        initContextMenu();

        load();
    }

    public Button getButton() {
        return mButton;
    }

    public Node getRoot() {
        return mRoot;
    }

    private void createUI() {
        var internalBox = new VBox();
        internalBox.getChildren().setAll(mNameLabel, mDescLabel, mLastRunLabel);
        internalBox.setAlignment(Pos.CENTER);
        mButton.setGraphic(internalBox);
        mRoot.getChildren().add(mButton);
        mRoot.setMinSize(300, 200);

        FxHelper.autoSizeRegionHorizontal(mButton);
        FxHelper.autoSizeRegionVertical(mButton);

        mNameLabel.setStyle("-fx-font-size: 1.8em;-fx-font-weight: bold;");
        mDescLabel.setStyle("-fx-font-size: 1.4em;-fx-font-style: italic;");
        mLastRunLabel.setStyle("-fx-font-size: 1.4em;");

        mDescLabel.setPadding(FxHelper.getUIScaledInsets(12, 0, 12, 0));
    }

    private String getKey() {
        return "button_%d".formatted(mIndex);
    }

    private void initContextMenu() {
        var contextMenu = new ContextMenu();

        mEditMenuItem = new MenuItem(Dict.EDIT.toString());
        mEditMenuItem.setOnAction(actionEvent -> {
            mJota.getGlobalState().put(Jota.GSC_EDITOR, mJob);
        });

        mResetMenuItem = new MenuItem(Dict.RESET.toString());
        mResetMenuItem.setOnAction(actionEvent -> {
            mPreferences.remove(getKey());
            load();
        });

        contextMenu.getItems().addAll(mEditMenuItem, mResetMenuItem);

        if (mJobManager.hasItems()) {
            contextMenu.getItems().add(new SeparatorMenuItem());
            for (var job : mJobManager.getItems()) {
                var menuItem = new MenuItem(job.getName());
                contextMenu.getItems().add(menuItem);
                menuItem.setOnAction(actionEvent -> {
                    mPreferences.put(getKey(), job.getId());
                    load();
                });
            }
        }

        mButton.setContextMenu(contextMenu);
        mRoot.setOnMousePressed(mouseEvent -> {
            contextMenu.show(mRoot, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        });

        mResetMenuItem.disableProperty().bind(mButton.disableProperty());
        mEditMenuItem.disableProperty().bind(mButton.disableProperty());
    }

    private void initListeners() {
        mJobManager.getItems().addListener((ListChangeListener.Change<? extends Job> c) -> {
            FxHelper.runLater(() -> {
                initContextMenu();
                load();
            });
        });

        mButton.setOnAction(actionEvent -> {
            mExecutorManager.requestStart(mJob);
        });
    }

    private void load() {
        var jobId = mPreferences.get(getKey(), null);
        mJob = mJobManager.getById(jobId);
        var disabled = mJob == null;

        mButton.setDisable(disabled);

        if (disabled) {
            FxHelper.clearLabel(mNameLabel, mDescLabel, mLastRunLabel);

        } else {
            mNameLabel.setText(mJob.getName());
            mDescLabel.setText(mJob.getDescription());
            mLastRunLabel.setText(mJob.getLastRunDateTime("?"));
        }
    }
}
