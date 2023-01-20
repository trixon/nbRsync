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

import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import java.io.IOException;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.rsyncfx.RsyncFx;
import static se.trixon.rsyncfx.RsyncFx.getIconSizeToolBarInt;
import se.trixon.rsyncfx.core.BaseItem;
import se.trixon.rsyncfx.core.BaseManager;
import se.trixon.rsyncfx.core.JobManager;
import se.trixon.rsyncfx.core.Storage;
import se.trixon.rsyncfx.core.StorageManager;
import se.trixon.rsyncfx.core.TaskManager;
import se.trixon.rsyncfx.core.job.Job;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class EditorPane extends HBox {

    private final ResourceBundle mBundle = NbBundle.getBundle(EditorPane.class);
    private final JobManager mJobManager = JobManager.getInstance();
    private JobPane mJobPane;
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private TaskPane mTaskPane;

    public EditorPane() {
        createUI();
    }

    public JobPane getJobPane() {
        return mJobPane;
    }

    public TaskPane getTaskPane() {
        return mTaskPane;
    }

    private void createUI() {
        setSpacing(FxHelper.getUIScaled(12));

        mJobPane = new JobPane(mJobManager);
        mTaskPane = new TaskPane(mTaskManager);
        getChildren().setAll(mJobPane, mTaskPane);
        HBox.setHgrow(mJobPane, Priority.ALWAYS);
        HBox.setHgrow(mTaskPane, Priority.ALWAYS);

        mJobPane.getListView().setCellFactory(listView -> new ItemListCellRenderer<>() {
        });

        mTaskPane.getListView().setCellFactory(listView -> new ItemListCellRenderer<>() {
        });
    }

    public abstract class BaseItemPane<T extends BaseItem> extends BorderPane {

        private final ToolbarItem mLabelToolbarItem;
        private final ListView<T> mListView = new ListView<>();
        private final BaseManager mManager;
        private final String mTitleP;
        private final String mTitleS;
        private List<ToolbarItem> mToolBarItems;

        public BaseItemPane(BaseManager manager) {
            mManager = manager;
            mTitleS = mManager.getLabelSingular();
            mTitleP = mManager.getLabelPlural();
            mLabelToolbarItem = new ToolbarItem("%s".formatted(mTitleS));

            createUI();
        }

        public ListView<T> getListView() {
            return mListView;
        }

        public List<ToolbarItem> getToolBarItems() {
            return mToolBarItems;
        }

        abstract void onAdd();

        abstract void onEdit();

        private boolean confirm(MouseEvent mouseEvent, String title, String header, String content, String buttonText) {
            if (mouseEvent != null && mouseEvent.getButton() != MouseButton.PRIMARY) {
                return false;
            }

            var stage = RsyncFx.getInstance().getStage();
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

        private Tooltip createTooltip(String string) {
            return new Tooltip("%s %s".formatted(string, mTitleS));
        }

        private void createUI() {
            final int size = getIconSizeToolBarInt();
            final Color color = Color.WHITE;

            var addToolbarItem = new ToolbarItem(MaterialIcon._Content.ADD.getImageView(size, color), mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    onAdd();
                }
            });
            addToolbarItem.setTooltip(createTooltip(Dict.ADD.toString()));

            var remToolbarItem = new ToolbarItem(MaterialIcon._Content.REMOVE.getImageView(size, color), mouseEvent -> {
                var baseTitle = Dict.Dialog.TITLE_REMOVE_S.toString().formatted(mTitleS.toLowerCase(Locale.ENGLISH));
                var action = Dict.Dialog.TITLE_REMOVE_S.toString().formatted("'%s'".formatted(getSelected().getName()));
                var baseHeader = Dict.Dialog.YOU_ARE_ABOUT_TO_S.toString().formatted(action.toLowerCase(Locale.ENGLISH));

                if (confirm(mouseEvent,
                        baseTitle + "?",
                        baseHeader,
                        Dict.Dialog.ACTION_CANT_BE_UNDONE.toString(),
                        baseTitle)) {
                    onRemove();
                }
            });
            remToolbarItem.setTooltip(createTooltip(Dict.REMOVE.toString()));

            var remAllToolbarItem = new ToolbarItem(MaterialIcon._Content.CLEAR.getImageView(size, color), mouseEvent -> {
                var baseTitle = Dict.Dialog.TITLE_REMOVE_ALL_S.toString().formatted(mTitleP.toLowerCase(Locale.ENGLISH));
                var action = Dict.Dialog.TITLE_REMOVE_ALL_S.toString().formatted(mTitleP.toLowerCase(Locale.ENGLISH));
                var baseHeader = Dict.Dialog.YOU_ARE_ABOUT_TO_S.toString().formatted(action.toLowerCase(Locale.ENGLISH));

                if (confirm(mouseEvent,
                        baseTitle + "?",
                        baseHeader,
                        Dict.Dialog.ACTION_CANT_BE_UNDONE.toString(),
                        baseTitle)) {
                    onRemoveAll();
                }
            });
            remAllToolbarItem.setTooltip(createTooltip(Dict.REMOVE_ALL.toString()));

            var editToolbarItem = new ToolbarItem(MaterialIcon._Editor.MODE_EDIT.getImageView(size, color), mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    onEdit();
                }
            });
            editToolbarItem.setTooltip(createTooltip(Dict.EDIT.toString()));

            var cloneToolbarItem = new ToolbarItem(MaterialIcon._Content.CONTENT_COPY.getImageView(size, color), mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    onClone();
                }
            });
            cloneToolbarItem.setTooltip(createTooltip(Dict.CLONE.toString()));

            mToolBarItems = List.of(
                    mLabelToolbarItem,
                    addToolbarItem,
                    remToolbarItem,
                    editToolbarItem,
                    cloneToolbarItem,
                    remAllToolbarItem
            );

            var nullSelectionBooleanBinding = mListView.getSelectionModel().selectedItemProperty().isNull();
            editToolbarItem.disableProperty().bind(nullSelectionBooleanBinding);
            cloneToolbarItem.disableProperty().bind(nullSelectionBooleanBinding);
            remToolbarItem.disableProperty().bind(nullSelectionBooleanBinding);
            remAllToolbarItem.disableProperty().bind(Bindings.isEmpty(mManager.getItems()));

            mListView.itemsProperty().bind(mManager.itemsProperty());

            setCenter(mListView);
        }

        private T getSelected() {
            return mListView.getSelectionModel().getSelectedItem();
        }

        private void onClone() {
            T original = getSelected();
            var json = Storage.GSON.toJson(original);
            var clone = Storage.GSON.fromJson(json, original.getClass());
            clone.setId(UUID.randomUUID().toString());
            clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
            mManager.getIdToItem().put(clone.getId(), clone);

            save();

            mListView.getSelectionModel().select((T) clone);
            mListView.requestFocus();
        }

        private void onRemove() {
            mManager.getIdToItem().remove(getSelected().getId());
            save();
        }

        private void onRemoveAll() {
            mManager.getIdToItem().clear();

            save();
        }

        private void save() {
            try {
                StorageManager.getInstance().save();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
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
                    itemPane.onEdit();
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

    public class JobPane extends BaseItemPane<Job> {

        public JobPane(BaseManager manager) {
            super(manager);
        }

        @Override
        void onAdd() {
            System.out.println("ADD");
        }

        @Override
        void onEdit() {
            System.out.println("EDIT");
        }
    }

    public class TaskPane extends BaseItemPane<Task> {

        public TaskPane(BaseManager manager) {
            super(manager);
        }

        @Override
        void onAdd() {
        }

        @Override
        void onEdit() {
        }
    }
}
