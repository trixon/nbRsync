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
package se.trixon.rsyncfx.ui.editor;

import java.util.ResourceBundle;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.rsyncfx.core.BaseItem;
import se.trixon.rsyncfx.core.StorageManager;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BaseEditor<T extends BaseItem> extends BorderPane {

    protected ResourceBundle mBundle = NbBundle.getBundle(BaseEditor.class);
    private final TextField mDescTextField = new TextField();
    private T mItem;
    private final TextField mNameTextField = new TextField();
    private final TextArea mNoteTextArea = new TextArea();
    private final TabPane mTabPane = new TabPane();

    public BaseEditor() {
        createUI();
    }

    public TabPane getTabPane() {
        return mTabPane;
    }

    public void load(T item) {
        mItem = item;
        mNameTextField.setText(item.getName());
        mDescTextField.setText(item.getDescription());
        mNoteTextArea.setText(item.getNote());
    }

    public T save() {
        mItem.setName(mNameTextField.getText());
        mItem.setDescription(mDescTextField.getText());
        mItem.setNote(mNoteTextArea.getText());

        StorageManager.save();

        return mItem;
    }

    public Tab createNoteTab() {
        return new Tab(Dict.NOTE.toString(), mNoteTextArea);
    }

    private void createUI() {
        var nameLabel = new Label(Dict.NAME.toString());
        var descLabel = new Label(Dict.DESCRIPTION.toString());
        var vbox = new VBox(
                nameLabel,
                mNameTextField,
                descLabel,
                mDescTextField,
                mNoteTextArea
        );

        setTop(vbox);
        setCenter(mTabPane);

        mTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        mNoteTextArea.setPrefHeight(68);
        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), descLabel, mTabPane);
    }
}
