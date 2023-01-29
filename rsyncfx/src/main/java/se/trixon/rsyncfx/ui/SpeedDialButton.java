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
package se.trixon.rsyncfx.ui;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.rsyncfx.core.JobManager;
import se.trixon.rsyncfx.core.job.Job;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class SpeedDialButton {

    private final Button mButton = new Button();
    private final Text mDescText = new Text("DESCRIPTION");
    private final VBox mInternalBox = new VBox();
    private final JobManager mJobManager = JobManager.getInstance();
    private final Text mLastRunText = new Text("yyyy-MM-dd");
    private final Text mNameText = new Text("NAME");
    private final BorderPane mRoot = new BorderPane();

    public SpeedDialButton() {
        createUI();
        initListeners();
        initContextMenu();
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
        mNameText.setFill(Color.RED);

        VBox.setVgrow(mNameText, Priority.ALWAYS);

        mButton.setGraphic(mInternalBox);
        mRoot.setCenter(mButton);
        mRoot.setBackground(FxHelper.createBackground(Color.CORAL));
        FxHelper.autoSizeRegionHorizontal(mButton);
        FxHelper.autoSizeRegionVertical(mButton);
        FxHelper.setPadding(FxHelper.getUIScaledInsets(66), mButton);
        FxHelper.setMargin(FxHelper.getUIScaledInsets(16), mRoot);
    }

    private void initContextMenu() {
        var contextMenu = new ContextMenu();

        var editMenuItem = new MenuItem(Dict.EDIT.toString());
        editMenuItem.setOnAction(actionEvent -> {
        });

        var editorMenuItem = new MenuItem(Dict.EDITOR.toString());
        editorMenuItem.setOnAction(actionEvent -> {
        });

        var resetMenuItem = new MenuItem(Dict.RESET.toString());
        resetMenuItem.setOnAction(actionEvent -> {
        });

        contextMenu.getItems().addAll(editMenuItem, editorMenuItem, resetMenuItem);

        if (mJobManager.hasItems()) {
            contextMenu.getItems().add(new SeparatorMenuItem());
            for (var job : mJobManager.getItems()) {
                var menuItem = new MenuItem(job.getName());
                contextMenu.getItems().add(menuItem);
                menuItem.setOnAction(actionEvent -> {
                    System.out.println("store job id for button index");
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
        });
    }
}
