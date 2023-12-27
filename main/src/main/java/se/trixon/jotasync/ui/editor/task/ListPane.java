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
package se.trixon.jotasync.ui.editor.task;

import java.util.ArrayList;
import java.util.Collections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.jotasync.Jota;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ListPane<T extends ArgBase> {

    private final ArrayList<T> mFilteredItems = new ArrayList<>();
    private final ArrayList<T> mItems = new ArrayList<>();
    private final String mKey;
    private final Label mLabel = new Label();
    private final ListView<T> mListView = new ListView<>();
    private final BorderPane mRoot;
    private final TextField mTextField = TextFields.createClearableTextField();

    public ListPane(String key) {
        mKey = key;
        mRoot = new BorderPane(mListView);
        mRoot.setTop(new VBox(mLabel, mTextField));
        mListView.setCellFactory(listView -> new OptionListCell());
        mListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mTextField.textProperty().addListener((p, o, n) -> {
            updateList();
        });
    }

    public ArrayList<T> getFilteredItems() {
        return mFilteredItems;
    }

    public ArrayList<T> getItems() {
        return mItems;
    }

    public ListView<T> getListView() {
        return mListView;
    }

    public Node getRoot() {
        return mRoot;
    }

    public ObservableList<T> getSelectedItems() {
        return getListView().getSelectionModel().getSelectedItems();
    }

    public void setHeader(String value) {
        mLabel.setText(value);
    }

    public void updateList() {
        var options = new ArrayList<>(mItems);

        Collections.sort(options, (o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));

        mFilteredItems.clear();
        mItems.clear();
        mItems.addAll(options);

        String filter = mTextField.getText();
        for (T item : mItems) {
            if (item.filter(filter)) {
                mFilteredItems.add(item);
            }
        }

        mListView.getItems().setAll(mFilteredItems);
    }

    class OptionListCell extends ListCell<T> {

        protected final Font mDefaultFont = Font.getDefault();
        private final Label mArgLabel = new Label();
        private final Label mDescLabel = new Label();
        private VBox mRoot;

        public OptionListCell() {
            createUI();
        }

        @Override
        protected void updateItem(T option, boolean empty) {
            super.updateItem(option, empty);
            if (option == null || empty) {
                clearContent();
            } else {
                addContent(option);
            }
        }

        private void addContent(ArgBase argBase) {
            setText(null);
            String separator = (StringUtils.isBlank(argBase.getLongArg()) || StringUtils.isBlank(argBase.getShortArg())) ? "" : ", ";

            var arg = "%s%s%s".formatted(argBase.getShortArg(), separator, argBase.getLongArg());
            if (StringUtils.isBlank(arg)) {
                arg = argBase.getArg();
            }
            mDescLabel.setText(argBase.getTitle());
            mArgLabel.setText(arg);
            mRoot.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    Jota.getInstance().getGlobalState().put("dblclck_" + mKey, argBase);
                }
            });

            setGraphic(mRoot);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            var fontSize = FxHelper.getScaledFontSize();
            var fontStyle = "-fx-font-size: %.0fpx; -fx-font-weight: %s;";

            mDescLabel.setStyle(fontStyle.formatted(fontSize * 1.0, "bold"));
            mArgLabel.setStyle(fontStyle.formatted(fontSize * 1.0, "normal"));

            mRoot = new VBox(mDescLabel, mArgLabel);

            mRoot.setAlignment(Pos.CENTER_LEFT);
        }
    }
}
