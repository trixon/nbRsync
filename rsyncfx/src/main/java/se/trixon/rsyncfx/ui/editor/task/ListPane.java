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
package se.trixon.rsyncfx.ui.editor.task;

import java.util.ArrayList;
import java.util.Collections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ListPane<T extends OptionHandler> {

    private final ArrayList<T> mFilteredItems = new ArrayList<>();
    private final ArrayList<T> mItems = new ArrayList<>();
    private final Label mLabel = new Label();
    private final ListView<T> mListView = new ListView<>();
    private final BorderPane mRoot;
    private final TextField mTextField = new TextField();

    public ListPane() {
        mRoot = new BorderPane(mListView);
        mRoot.setTop(new VBox(mLabel, mTextField));
        mListView.setCellFactory(listView -> new OptionListCell());
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

    public Node getRoot() {
        return mRoot;
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

        private void addContent(OptionHandler option) {
            setText(null);
            String separator = (StringUtils.isBlank(option.getLongArg()) || StringUtils.isBlank(option.getShortArg())) ? "" : ", ";

            var arg = "%s%s%s".formatted(option.getShortArg(), separator, option.getLongArg());
            if (StringUtils.isBlank(arg)) {
                arg = option.getArg();
            }
            mDescLabel.setText(option.getTitle());
            mArgLabel.setText(arg);
            setGraphic(mRoot);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.0));
            mArgLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.0));
            mRoot = new VBox(mDescLabel, mArgLabel);
            mRoot.setAlignment(Pos.CENTER_LEFT);
        }
    }
}
