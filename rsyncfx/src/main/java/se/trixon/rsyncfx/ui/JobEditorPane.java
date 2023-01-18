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
import se.trixon.rsyncfx.core.job.Job;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class JobEditorPane extends HBox {

    private static Action sAction;
    private final ResourceBundle mBundle = NbBundle.getBundle(JobEditorPane.class);
    private final Job mJob;

    public static void displayJobEditor(Job job) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(RsyncFx.getInstance().getStage());
        alert.setResizable(true);

        alert.setTitle(NbBundle.getMessage(JobEditorPane.class, "jobEditor.Title"));
        alert.setGraphic(null);
        alert.setHeaderText(null);

        var jobEditorPane = new JobEditorPane(job);
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
                JobEditorPane.displayJobEditor(null);
            });
        }

        return sAction;
    }

    public JobEditorPane(Job job) {
        mJob = job;
        createUI();
    }

    private void createUI() {
        setSpacing(FxHelper.getUIScaled(16));

        var jobListView = new JobListView(Dict.JOBS.toString());
        var taskListView = new TaskListView(Dict.TASKS.toString());
        getChildren().setAll(jobListView, taskListView);
        HBox.setHgrow(jobListView, Priority.ALWAYS);
        HBox.setHgrow(taskListView, Priority.ALWAYS);
    }

    private abstract class BaseListView<T> extends BorderPane {

        private final Label mLabel = new Label();
        private final ListView<T> mListView = new ListView<>();
        private ToolBar mToolBar = new ToolBar();

        public BaseListView(String title) {
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

    private class JobListView extends BaseListView<Job> {

        public JobListView(String title) {
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

    private class TaskListView extends BaseListView<Task> {

        public TaskListView(String title) {
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
