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
package se.trixon.jotasync.ui.editor;

import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.jotasync.core.BaseItem;
import se.trixon.jotasync.core.BaseManager;
import se.trixon.jotasync.core.ExecuteItem;
import se.trixon.jotasync.core.StorageManager;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BaseEditor<T extends BaseItem> extends BorderPane {

    protected ResourceBundle mBundle = NbBundle.getBundle(BaseEditor.class);
    protected BaseManager<T> mManager;
    protected final ValidationSupport mValidationSupport = new ValidationSupport();
    private final TextField mDescTextField = new TextField();
    private T mItem;
    private final TextField mNameTextField = new TextField();
    private final TextArea mNoteTextArea = new TextArea();
    private Node mSaveNode;
    private final TabPane mTabPane = new TabPane();

    public BaseEditor(BaseManager<T> manager) {
        mManager = manager;
        createUI();
        initValidation();
    }

    public Tab createNoteTab() {
        return new Tab(Dict.NOTE.toString(), mNoteTextArea);
    }

    public TabPane getTabPane() {
        return mTabPane;
    }

    public void load(T item, Node saveNode) {
        mItem = item;
        mSaveNode = saveNode;
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

    public void save(ExecuteItem item, RunSectionPane runSectionPane) {
        item.setEnabled(runSectionPane.isEnabled());
        item.setHaltOnError(runSectionPane.isHaltOnError());
        item.setCommand(runSectionPane.getCommand());
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

    private void initValidation() {
        mValidationSupport.validationResultProperty().addListener((p, o, n) -> {
            mSaveNode.setDisable(mValidationSupport.isInvalid());
        });

        mValidationSupport.initInitialDecoration();

        final String textRequired = "Text is required";
        final String textUnique = "Text has to be unique";

        Predicate uniqueNamePredicate = (Predicate) (Object o) -> {
            var newName = mNameTextField.getText();
            if (!mManager.exists(newName)) {
                return true;
            } else {
                return StringUtils.equalsIgnoreCase(newName, mItem.getName());
            }
        };

        Platform.runLater(() -> {
            mValidationSupport.registerValidator(mNameTextField, true, Validator.combine(
                    Validator.createEmptyValidator(textRequired),
                    Validator.createPredicateValidator(uniqueNamePredicate, textUnique)
            ));
            mValidationSupport.registerValidator(mDescTextField, true, Validator.createEmptyValidator(textRequired));
        });
    }
}
