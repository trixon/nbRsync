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
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.rsyncfx.RsyncFx.getIconSizeToolBarInt;
import se.trixon.rsyncfx.core.BaseItem;
import se.trixon.rsyncfx.core.BaseManager;
import se.trixon.rsyncfx.core.JobManager;
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

        mJobPane = new JobPane(Dict.JOBS.toString(), mJobManager);
        mTaskPane = new TaskPane(Dict.TASKS.toString(), mTaskManager);
        getChildren().setAll(mJobPane, mTaskPane);
        HBox.setHgrow(mJobPane, Priority.ALWAYS);
        HBox.setHgrow(mTaskPane, Priority.ALWAYS);

        mJobPane.getListView().setCellFactory(listView -> new ItemListCellRenderer<>() {
        });

        mTaskPane.getListView().setCellFactory(listView -> new ItemListCellRenderer<>() {
        });
    }

    public abstract class BaseItemPane<T> extends BorderPane {

        private final ToolbarItem mLabelToolbarItem;
        private final ListView<T> mListView = new ListView<>();
        private final BaseManager mManager;
        private final String mTitle;
        private List<ToolbarItem> mToolBarItems;

        public BaseItemPane(String title, BaseManager manager) {
            mTitle = title;
            mManager = manager;
            mLabelToolbarItem = new ToolbarItem("%s".formatted(title));

            createUI();
        }

        public ListView<T> getListView() {
            return mListView;
        }

        public List<ToolbarItem> getToolBarItems() {
            return mToolBarItems;
        }

        abstract void onAdd();

        abstract void onClone();

        abstract void onEdit();

        abstract void onRemove();

        abstract void onRemoveAll();

        private Tooltip createTooltip(String string) {
            return new Tooltip("%s %s".formatted(string, mTitle));
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
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    onRemove();
                }
            });
            remToolbarItem.setTooltip(createTooltip(Dict.REMOVE.toString()));

            var remAllToolbarItem = new ToolbarItem(MaterialIcon._Content.CLEAR.getImageView(size, color), mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
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
            remAllToolbarItem.disableProperty().bind(Bindings.isEmpty(mListView.getItems()));

            mListView.itemsProperty().bind(mManager.itemsProperty());

            setCenter(mListView);
        }

        private T getSelected() {
            return mListView.getSelectionModel().getSelectedItem();
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

        public JobPane(String title, BaseManager manager) {
            super(title, manager);
        }

        @Override
        void onAdd() {
            System.out.println("ADD");
        }

        @Override
        void onClone() {
            System.out.println("CLONE");
        }

        @Override
        void onEdit() {
            System.out.println("EDIT");
        }

        @Override
        void onRemove() {
            System.out.println("REMOVE");
        }

        @Override
        void onRemoveAll() {
            System.out.println("REMOVE ALL");
        }

    }

    public class TaskPane extends BaseItemPane<Task> {

        public TaskPane(String title, BaseManager manager) {
            super(title, manager);
        }

        @Override
        void onAdd() {
        }

        @Override
        void onClone() {
        }

        @Override
        void onEdit() {
        }

        @Override
        void onRemove() {
        }

        @Override
        void onRemoveAll() {
        }

    }

}
