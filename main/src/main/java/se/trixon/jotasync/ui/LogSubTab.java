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
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class LogSubTab extends Tab {

    private final ListView<String> mListView = new ListView<>();

    public LogSubTab(String string) {
        super(string);
        setContent(mListView);
    }

    public void clear() {
        mListView.getItems().clear();
    }

    public void add(String string) {
        mListView.getItems().add(string);
//        mListView.scrollTo(mListView.getItems().size());
    }

    public LogSubTab() {
        createMenu();
        mListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
        setContextMenu(contextMenu);
    }

    private void copy(ObservableList<String> items) {
        var sb = new StringBuilder(String.join("\n", items)).append("\n");
        SystemHelper.copyToClipboard(sb.toString());
    }

}
