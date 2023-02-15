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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    private final Label mDescText = new Label();
    private MenuItem mEditMenuItem;
    private MenuItem mEditorMenuItem;
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final int mIndex;
    private final VBox mInternalBox = new VBox();
    private Job mJob;
    private final JobManager mJobManager = JobManager.getInstance();
    private final Text mLastRunText = new Text("yyyy-MM-dd");
    private final Text mNameText = new Text("NAME");
    private final Preferences mPreferences = NbPreferences.forModule(SpeedDialButton.class).node("speedDial");
    private MenuItem mResetMenuItem;
    private final BorderPane mRoot = new BorderPane();
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
        mInternalBox.getChildren().setAll(mNameText, mDescText, mLastRunText);
        mNameText.setTextAlignment(TextAlignment.CENTER);

        VBox.setVgrow(mNameText, Priority.ALWAYS);

        mButton.setGraphic(mInternalBox);
        mRoot.setCenter(mButton);
        mRoot.setBackground(FxHelper.createBackground(Color.CORAL));
        FxHelper.autoSizeRegionHorizontal(mButton);
        FxHelper.autoSizeRegionVertical(mButton);
        FxHelper.setPadding(FxHelper.getUIScaledInsets(66), mButton);
        FxHelper.setMargin(FxHelper.getUIScaledInsets(16), mRoot);
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

        mEditorMenuItem = new MenuItem(Dict.EDITOR.toString());
        mEditorMenuItem.setOnAction(actionEvent -> {
            mJota.getGlobalState().put(Jota.GSC_EDITOR, null);
        });

        mResetMenuItem = new MenuItem(Dict.RESET.toString());
        mResetMenuItem.setOnAction(actionEvent -> {
            mPreferences.remove(getKey());
            load();
        });

        contextMenu.getItems().addAll(mEditMenuItem, mEditorMenuItem, mResetMenuItem);

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
    }

    private void initListeners() {
        mJobManager.getItems().addListener((ListChangeListener.Change<? extends Job> c) -> {
            initContextMenu();
            load();
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
        mEditMenuItem.setDisable(disabled);
        mEditorMenuItem.setDisable(disabled);
        mResetMenuItem.setDisable(disabled);

        if (disabled) {
            mNameText.setText("");
            mDescText.setText("");
            mLastRunText.setText("");
        } else {
            mNameText.setText(mJob.getName());
            mDescText.setText(mJob.getDescription());
            mLastRunText.setText(mJob.getLastRunDateTime("?"));
        }
    }
}
