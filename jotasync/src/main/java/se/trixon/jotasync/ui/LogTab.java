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
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.RandomStringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jotasync.core.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class LogTab extends BaseTab {

    /*
    TODO
    Tabs?
    Filter
    Populate toolbar
        Edit, Start, Cancel
     */
    private final BorderPane mBorderPane = new BorderPane();
    private final Job mJob;
    private final ListView<String> mListView = new ListView<>();
    private final ProgressBar mProgressBar = new ProgressBar(0.75);

    public LogTab(Job job) {
        mJob = job;
        createUI();
        createMenu();
    }

    @Override
    public void updateNightMode() {
    }

    private void copy(ObservableList<String> items) {
        var sb = new StringBuilder(String.join("\n", items)).append("\n");
        SystemHelper.copyToClipboard(sb.toString());
    }

    private void createMenu() {
        var copySelectionAction = new Action(Dict.COPY_SELECTION.toString(), actionEvent -> {
            copy(mListView.getSelectionModel().getSelectedItems());
        });

        var copyAllAction = new Action(Dict.COPY_ALL.toString(), actionEvent -> {
            copy(mListView.getItems());
        });

        var actions = List.of(copySelectionAction, copyAllAction);
        var contextMenu = ActionUtils.createContextMenu(actions);
        mListView.setContextMenu(contextMenu);
    }

    private void createUI() {
        var toolBar = new ToolBar(new Button("xyz"));
        var box = new VBox(mProgressBar, toolBar);
        mBorderPane.setTop(box);
        mBorderPane.setCenter(mListView);
        for (int i = 0; i < 1000; i++) {
            mListView.getItems().add(RandomStringUtils.randomAlphanumeric(new Random().nextInt(10, 80)));
        }
        setContent(mBorderPane);
        setText(mJob.getName());
        mListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mProgressBar.prefWidthProperty().bind(box.widthProperty());
    }

}
