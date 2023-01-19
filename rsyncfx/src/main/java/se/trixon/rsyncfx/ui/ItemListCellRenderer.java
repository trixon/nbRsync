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

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import se.trixon.rsyncfx.core.BaseItem;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class ItemListCellRenderer<T extends BaseItem> extends ListCell<T> {

    private final Font mDefaultFont = Font.getDefault();
    private final Label mDescLabel = new Label();
    private final Label mNameLabel = new Label();
    private final VBox mRoot = new VBox();

    public ItemListCellRenderer() {
        createUI();
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            clearContent();
        } else {
            addContent(item);
        }
    }

    private void addContent(T item) {
        setText(null);

        mNameLabel.setText(item.getName());
        mDescLabel.setText(item.getDescription());
        mRoot.getChildren().setAll(mNameLabel, mDescLabel);

        setGraphic(mRoot);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        String fontFamily = mDefaultFont.getFamily();
        double fontSize = mDefaultFont.getSize();

        mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.4));
        mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));
    }
}
