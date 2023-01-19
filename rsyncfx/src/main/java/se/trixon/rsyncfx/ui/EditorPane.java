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
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.rsyncfx.RsyncFx.getIconSizeToolBarInt;
import se.trixon.rsyncfx.core.JobManager;
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
    private JobPane mJobPane;
    private final StorageManager mStorageManager = StorageManager.getInstance();
    private final JobManager mJobManager = JobManager.getInstance();
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private TaskPane mTaskPane;

    public EditorPane() {
        createUI();
    }

    private void createUI() {
        setSpacing(FxHelper.getUIScaled(12));

        mJobPane = new JobPane(Dict.JOBS.toString());
        mTaskPane = new TaskPane(Dict.TASKS.toString());
        getChildren().setAll(mJobPane, mTaskPane);
        HBox.setHgrow(mJobPane, Priority.ALWAYS);
        HBox.setHgrow(mTaskPane, Priority.ALWAYS);

        var jobListView = mJobPane.getListView();
        jobListView.getItems().setAll(mJobManager.getItems());
        jobListView.setCellFactory(listView -> new ItemListCellRenderer<>() {
        });

        var taskListView = mTaskPane.getListView();
        taskListView.getItems().setAll(mTaskManager.getItems());
        taskListView.setCellFactory(listView -> new ItemListCellRenderer<>() {
        });
    }

    public JobPane getJobPane() {
        return mJobPane;
    }

    public TaskPane getTaskPane() {
        return mTaskPane;
    }

    public abstract class BaseItemPane<T> extends BorderPane {

        private final ToolbarItem mLabelToolbarItem;
        private final ListView<T> mListView = new ListView<>();
        private final String mTitle;
        private List<ToolbarItem> mToolBarItems;

        public BaseItemPane(String title) {
            mTitle = title;
            mLabelToolbarItem = new ToolbarItem("%s".formatted(title));

            createUI();
            initListerners();
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

            setCenter(mListView);
        }

        private T getSelected() {
            return mListView.getSelectionModel().getSelectedItem();
        }

        private void initListerners() {
            mListView.setOnMouseClicked(mouseEvent -> {
                if (getSelected() != null
                        && mouseEvent.getButton() == MouseButton.PRIMARY
                        && mouseEvent.getClickCount() == 2) {
                    onEdit();
                }
            });
        }
    }

    public class JobPane extends BaseItemPane<Job> {

        public JobPane(String title) {
            super(title);
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

        public TaskPane(String title) {
            super(title);
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
