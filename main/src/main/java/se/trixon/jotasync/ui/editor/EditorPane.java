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

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.core.BaseItem;
import se.trixon.jotasync.core.BaseManager;
import se.trixon.jotasync.core.JobManager;
import se.trixon.jotasync.core.Storage;
import se.trixon.jotasync.core.StorageManager;
import se.trixon.jotasync.core.TaskManager;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class EditorPane extends HBox {

    private final ResourceBundle mBundle = NbBundle.getBundle(EditorPane.class);
    private final JobManager mJobManager = JobManager.getInstance();
    private BaseItemPane mJobPane;
    private final Jota mJota = Jota.getInstance();
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private BaseItemPane mTaskPane;

    public static void displayEditor(Job job) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(Jota.getStage());

        alert.setTitle(Dict.EDITOR.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setResizable(true);
        alert.getButtonTypes().setAll(ButtonType.CLOSE);

        var editorPane = new EditorPane(job);
        var dialogPane = alert.getDialogPane();

        dialogPane.setContent(editorPane);
        dialogPane.getChildren().remove(0);//Remove graphics container in order to remove the spacing
        dialogPane.setPrefWidth(FxHelper.getUIScaled(700));
        FxHelper.removeSceneInitFlicker(dialogPane);

        FxHelper.showAndWait(alert, Jota.getStage());
    }

    public EditorPane(Job job) {
        createUI();
        initListeners();
        if (job != null) {
            load(job);
        }
    }

    public BaseItemPane getJobPane() {
        return mJobPane;
    }

    public BaseItemPane getTaskPane() {
        return mTaskPane;
    }

    private void createUI() {
        mJobPane = new BaseItemPane<Job>(mJobManager) {
        };
        mTaskPane = new BaseItemPane<Task>(mTaskManager) {
        };
        getChildren().setAll(mJobPane, mTaskPane);
        HBox.setHgrow(mJobPane, Priority.ALWAYS);
        HBox.setHgrow(mTaskPane, Priority.ALWAYS);

        mJobPane.getListView().setCellFactory(listView -> new ItemListCellRenderer<>() {
        });

        mTaskPane.getListView().setCellFactory(listView -> new ItemListCellRenderer<>() {
        });
    }

    private void initListeners() {
    }

    private void load(Job job) {
        FxHelper.runLaterDelayed(10, () -> {
            var listView = mJobPane.getListView();
            listView.requestFocus();
            listView.getSelectionModel().select(job);
            FxHelper.scrollToItemIfNotVisible(listView, job);
            mJobPane.edit(job);
        });
    }

    public abstract class BaseItemPane<T extends BaseItem> extends BorderPane {

        private final ListView<T> mListView = new ListView<>();
        private final BaseManager mManager;
        private final String mTitleP;
        private final String mTitleS;
        private List<Action> mActions;

        public BaseItemPane(BaseManager manager) {
            mManager = manager;
            mTitleS = mManager.getLabelSingular();
            mTitleP = mManager.getLabelPlural();

            createUI();
        }

        public ListView<T> getListView() {
            return mListView;
        }

        public List<Action> getActions() {
            return mActions;
        }

        private boolean confirm(String title, String header, String content, String buttonText) {
            var stage = mJota.getStage();
            var alert = new Alert(AlertType.CONFIRMATION);
            alert.initOwner(stage);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            var confirmButtonType = new ButtonType(buttonText, ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(ButtonType.CANCEL, confirmButtonType);

            var result = alert.showAndWait();

            return result.get() == confirmButtonType;
        }

        private Tooltip createTooltip(String string, boolean singular) {
            return new Tooltip("%s %s".formatted(string, (singular ? mTitleS : mTitleP).toLowerCase(Locale.ENGLISH)));
        }

        private void createUI() {
            final int size = Jota.getIconSizeToolBar();

            var addAction = new Action(Dict.ADD.toString(), actionEvent -> {
                edit(null);
            });
            addAction.setGraphic(MaterialIcon._Content.ADD.getImageView(size));

            var remAction = new Action(Dict.REMOVE.toString(), actionEvent -> {
                var baseTitle = Dict.Dialog.TITLE_REMOVE_S.toString().formatted(mTitleS.toLowerCase(Locale.ENGLISH));
                var action = Dict.Dialog.TITLE_REMOVE_S.toString().formatted("'%s'".formatted(getSelected().getName()));
                var baseHeader = Dict.Dialog.YOU_ARE_ABOUT_TO_S.toString().formatted(action.toLowerCase(Locale.ENGLISH));

                if (confirm(baseTitle + "?",
                        baseHeader,
                        Dict.Dialog.ACTION_CANT_BE_UNDONE.toString(),
                        baseTitle)) {
                    onRemove();
                }
            });
            remAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(size));

            var remAllAction = new Action(Dict.REMOVE_ALL.toString(), actionEvent -> {
                var baseTitle = Dict.Dialog.TITLE_REMOVE_ALL_S.toString().formatted(mTitleP.toLowerCase(Locale.ENGLISH));
                var action = Dict.Dialog.TITLE_REMOVE_ALL_S.toString().formatted(mTitleP.toLowerCase(Locale.ENGLISH));
                var baseHeader = Dict.Dialog.YOU_ARE_ABOUT_TO_S.toString().formatted(action.toLowerCase(Locale.ENGLISH));

                if (confirm(
                        baseTitle + "?",
                        baseHeader,
                        Dict.Dialog.ACTION_CANT_BE_UNDONE.toString(),
                        baseTitle)) {
                    onRemoveAll();
                }
            });
            remAllAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(size));

            var editAction = new Action(Dict.EDIT.toString(), actionEvent -> {
                edit(getSelected());
            });
            editAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(size));

            var cloneAction = new Action(Dict.CLONE.toString(), actionEvent -> {
                onClone();
            });
            cloneAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(size));

            mActions = List.of(
                    addAction,
                    remAction,
                    editAction,
                    cloneAction,
                    remAllAction
            );

            var nullSelectionBooleanBinding = mListView.getSelectionModel().selectedItemProperty().isNull();
            editAction.disabledProperty().bind(nullSelectionBooleanBinding);
            cloneAction.disabledProperty().bind(nullSelectionBooleanBinding);
            remAction.disabledProperty().bind(nullSelectionBooleanBinding);
            remAllAction.disabledProperty().bind(Bindings.isEmpty(mManager.getItems()));

            mListView.itemsProperty().bind(mManager.itemsProperty());
            var label = new Label(mTitleP);
            var toolBar = ActionUtils.createToolBar(mActions, ActionUtils.ActionTextBehavior.HIDE);
            FxHelper.undecorateButtons(toolBar.getItems().stream());
            FxHelper.slimToolBar(toolBar);

            setTop(new VBox(label, toolBar));
            setCenter(mListView);
        }

        private void edit(T item) {
            var stage = mJota.getStage();
            var alert = new Alert(Alert.AlertType.NONE);
            alert.initOwner(stage);
            alert.setTitle(item == null ? Dict.ADD.toString() : Dict.EDIT.toString());

            var saveButtonType = new ButtonType(Dict.SAVE.toString(), ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(saveButtonType, ButtonType.CANCEL);
            alert.setGraphic(null);
            alert.setHeaderText(null);
            alert.setResizable(true);

            var dialogPane = alert.getDialogPane();
            var editor = mManager.getEditor();
            editor.load(item, dialogPane.lookupButton(saveButtonType));
            dialogPane.setContent(editor);
            dialogPane.setPrefSize(FxHelper.getUIScaled(600), FxHelper.getUIScaled(660));
            dialogPane.getChildren().remove(0);//Remove graphics container in order to remove the spacing

            var result = alert.showAndWait();

            if (result.get() == saveButtonType) {
                var editedItem = editor.save();
                mListView.getSelectionModel().select((T) mManager.getById(editedItem.getId()));
                mListView.requestFocus();
            }
        }

        private T getSelected() {
            return mListView.getSelectionModel().getSelectedItem();
        }

        private void onClone() {
            T original = getSelected();
            var json = Storage.GSON.toJson(original);
            var clone = Storage.GSON.fromJson(json, original.getClass());
            var uuid = UUID.randomUUID().toString();
            clone.setId(uuid);
            clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
            mManager.getIdToItem().put(clone.getId(), clone);

            StorageManager.save();

            mListView.getSelectionModel().select((T) mManager.getById(uuid));
            mListView.requestFocus();
            edit(getSelected());
        }

        private void onRemove() {
            mManager.getIdToItem().remove(getSelected().getId());
            StorageManager.save();
        }

        private void onRemoveAll() {
            mManager.getIdToItem().clear();

            StorageManager.save();
        }
    }

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
            mRoot.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    var itemPane = (BaseItemPane<T>) getListView().getParent();
                    itemPane.edit(getItem());
                }
            });
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
}
