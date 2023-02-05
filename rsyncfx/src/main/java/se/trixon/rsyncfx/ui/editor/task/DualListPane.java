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

import java.util.Arrays;
import javafx.geometry.Orientation;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.rsyncfx.Jota;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class DualListPane<T extends OptionHandler> {

    private final ListPane<T> mAvailablePane = new ListPane<>("available");
    private Action mClearAction;
    private final Jota mJota = Jota.getInstance();
    private final HBox mRoot = new HBox();
    private final ListPane<T> mSelectedPane = new ListPane<>("selected");

    public DualListPane() {
        createUI();
        initListeners();
    }

    public ListPane<T> getAvailablePane() {
        return mAvailablePane;
    }

    public Region getRoot() {
        return mRoot;
    }

    public ListPane<T> getSelectedPane() {
        return mSelectedPane;
    }

    public void updateLists() {
        mAvailablePane.updateList();
        mSelectedPane.updateList();
        updateClearButtonState();
    }

    private void createUI() {
        int iconSize = Jota.getIconSizeToolBarInt();
        mAvailablePane.setHeader(Dict.AVAILABLE.toString());
        mSelectedPane.setHeader(Dict.SELECTED.toString());

        var activateAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            itemActivate();
        });
        activateAction.setGraphic(MaterialIcon._Navigation.ARROW_FORWARD.getImageView(iconSize));
        activateAction.disabledProperty().bind(mAvailablePane.getListView().getSelectionModel().selectedItemProperty().isNull());

        var deactivateAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            itemDeactivate();
        });
        deactivateAction.setGraphic(MaterialIcon._Navigation.ARROW_BACK.getImageView(iconSize));
        deactivateAction.disabledProperty().bind(mSelectedPane.getListView().getSelectionModel().selectedItemProperty().isNull());

        mClearAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            itemClear();
        });
        mClearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(iconSize));
        mClearAction.setDisabled(true);

        var actions = Arrays.asList(
                ActionUtils.ACTION_SPAN,
                activateAction,
                deactivateAction,
                mClearAction,
                ActionUtils.ACTION_SPAN
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        toolBar.setOrientation(Orientation.VERTICAL);
        toolBar.setBackground(FxHelper.createBackground(Color.CORAL));
        mRoot.getChildren().addAll(mAvailablePane.getRoot(), toolBar, mSelectedPane.getRoot());
        HBox.setHgrow(mAvailablePane.getRoot(), Priority.ALWAYS);
        HBox.setHgrow(mSelectedPane.getRoot(), Priority.ALWAYS);
    }

    private void initListeners() {
        mJota.getGlobalState().addListener(gsce -> {
            itemActivate();
        }, "dblclck_available");

        mJota.getGlobalState().addListener(gsce -> {
            itemDeactivate();
        }, "dblclck_selected");
    }

    private void itemActivate() {
        var availableItems = mAvailablePane.getItems();
        var selectedItems = mSelectedPane.getItems();

        for (T selectedItem : mAvailablePane.getSelectedItems()) {
            if (selectedItem.getLongArg().contains("=")) {
                String input = requestArg(selectedItem);
                if (input != null) {
                    selectedItems.add(selectedItem);
                    availableItems.remove(selectedItem);
                    selectedItem.setDynamicArg(input);
                }
            } else {
                selectedItems.add(selectedItem);
                availableItems.remove(selectedItem);
            }
        }

        updateLists();
    }

    private void itemClear() {
        for (T item : mSelectedPane.getItems()) {
            item.setDynamicArg(null);
            mAvailablePane.getItems().add(item);
        }

        mSelectedPane.getItems().clear();
        updateLists();
    }

    private void itemDeactivate() {
        var availableItems = mAvailablePane.getItems();
        var selectedItems = mSelectedPane.getItems();

        for (T selectedItem : mSelectedPane.getSelectedItems()) {
            availableItems.add(selectedItem);
            selectedItems.remove(selectedItem);
            selectedItem.setDynamicArg(null);
        }

        updateLists();
    }

    private String requestArg(T t) {
        var textInputDialog = new TextInputDialog();
        var okButton = textInputDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        textInputDialog.getEditor().textProperty().addListener((p, o, n) -> {
            okButton.setDisable(StringUtils.isBlank(n));
        });
//        textInputDialog.setTitle(t.getTitle());
        textInputDialog.setHeaderText(t.getTitle());
        textInputDialog.setContentText(t.getLongArg());

        var result = textInputDialog.showAndWait();

        return result.orElse(null);
    }

    private void updateClearButtonState() {
        mClearAction.setDisabled(mSelectedPane.getItems().isEmpty());
    }
}
