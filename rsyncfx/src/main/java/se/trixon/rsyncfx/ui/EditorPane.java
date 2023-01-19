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

import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.rsyncfx.RsyncFx;
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

    private static Action sAction;
    private final ResourceBundle mBundle = NbBundle.getBundle(EditorPane.class);
    private final Job mJob;
    private final StorageManager mStorageManager = StorageManager.getInstance();
    private final JobManager mJobManager = JobManager.getInstance();
    private final TaskManager mTaskManager = TaskManager.getInstance();

    public static void displayEditor(Job job) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(RsyncFx.getInstance().getStage());
        alert.setResizable(true);

        alert.setTitle(Dict.EDITOR.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);

        var jobEditorPane = new EditorPane(job);
        var dialogPane = alert.getDialogPane();
        var button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.setText(Dict.SAVE.toString());

        dialogPane.setContent(jobEditorPane);
        dialogPane.getChildren().remove(0);//Remove graphics container in order to remove the spacing
        FxHelper.removeSceneInitFlicker(dialogPane);

        var result = FxHelper.showAndWait(alert, RsyncFx.getInstance().getStage());
        if (result.get() == ButtonType.OK) {
            System.out.println("SAVE");
        }
    }

    public static Action getAction() {
        if (sAction == null) {
            sAction = new Action(Dict.EDITOR.toString(), actionEvent -> {
                RsyncFx.getInstance().getWorkbench().hideNavigationDrawer();
                EditorPane.displayEditor(null);
            });
        }

        return sAction;
    }

    public EditorPane(Job job) {
        mJob = job;
        createUI();
    }

    private void createUI() {
        setSpacing(FxHelper.getUIScaled(16));

        var jobPane = new JobPane(Dict.JOBS.toString());
        var taskPane = new TaskPane(Dict.TASKS.toString());
        getChildren().setAll(jobPane, taskPane);
        HBox.setHgrow(jobPane, Priority.ALWAYS);
        HBox.setHgrow(taskPane, Priority.ALWAYS);

        var jobListView = jobPane.getListView();
        jobListView.getItems().setAll(mJobManager.getJobs());
        jobListView.setCellFactory(listView -> new ItemListCellRenderer<>() {
        });

        var taskListView = taskPane.getListView();
        taskListView.getItems().setAll(mTaskManager.getTasks());
        taskListView.setCellFactory(listView -> new ItemListCellRenderer<>() {
        });
    }

    private abstract class BaseItemPane<T> extends BorderPane {

        private final Label mLabel = new Label();
        private final ListView<T> mListView = new ListView<>();
        private ToolBar mToolBar = new ToolBar();

        public BaseItemPane(String title) {
            mLabel.setText(title);
            createUI();
        }

        public ListView<T> getListView() {
            return mListView;
        }

        abstract void onAdd();

        abstract void onClone();

        abstract void onEdit();

        abstract void onRemove();

        abstract void onRemoveAll();

        private void createUI() {
            var addAction = new Action(Dict.ADD.toString(), actionEvent -> {
                onAdd();
            });
            addAction.setGraphic(MaterialIcon._Content.ADD.getImageView(getIconSizeToolBarInt()));

            var editAction = new Action(Dict.EDIT.toString(), actionEvent -> {
            });
            editAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(getIconSizeToolBarInt()));

            var remAction = new Action(Dict.REMOVE.toString(), actionEvent -> {
            });
            remAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(getIconSizeToolBarInt()));

            var remAllAction = new Action(Dict.REMOVE_ALL.toString(), actionEvent -> {
            });
            remAllAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

            var cloneAction = new Action(Dict.CLONE.toString(), actionEvent -> {
            });
            cloneAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(getIconSizeToolBarInt()));

            var mActions = Arrays.asList(
                    addAction,
                    remAction,
                    editAction,
                    cloneAction,
                    ActionUtils.ACTION_SPAN,
                    remAllAction
            );

            mToolBar = ActionUtils.createToolBar(mActions, ActionUtils.ActionTextBehavior.HIDE);

            FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), getIconSizeToolBarInt());
            FxHelper.undecorateButtons(mToolBar.getItems().stream());
            FxHelper.slimToolBar(mToolBar);

            setTop(new VBox(mLabel, mToolBar));
            setCenter(mListView);
        }
    }

    private class JobPane extends BaseItemPane<Job> {

        public JobPane(String title) {
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

    private class TaskPane extends BaseItemPane<Task> {

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
