/*
 * Copyright 2024 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.nbrsync.ui.editor;

import java.util.ArrayList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.ListActionView;
import org.controlsfx.control.ListSelectionView;
import org.openide.DialogDescriptor;
import se.trixon.almond.nbp.fx.NbCronPane;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.nbrsync.core.JobManager;
import se.trixon.nbrsync.core.TaskManager;
import se.trixon.nbrsync.core.job.Job;
import se.trixon.nbrsync.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class JobEditor extends BaseEditor<Job> {

    private CheckBox mActivatedCheckBox = new CheckBox(Dict.ACTIVE.toString());
    private NbCronPane mCronPane;
    private Job mItem;
    private ListSelectionView<Task> mListSelectionView;
    private RunSectionPane mRunAfterFailSection;
    private RunSectionPane mRunAfterOkSection;
    private RunSectionPane mRunAfterSection;
    private RunSectionPane mRunBeforeSection;

    public JobEditor() {
        super(JobManager.getInstance());
        createUI();
    }

    @Override
    public void cancel() {
    }

    @Override
    public void load(Job item, DialogDescriptor dialogDescriptor) {
        if (item == null) {
            item = new Job();
        }

        var execute = item.getExecuteSection();
        mRunBeforeSection.load(execute.getBefore());
        mRunAfterFailSection.load(execute.getAfterFail());
        mRunAfterOkSection.load(execute.getAfterOk());
        mRunAfterSection.load(execute.getAfter());

        var selectedTasks = item.getTasks();
        var availableTasks = new ArrayList<>(TaskManager.getInstance().getItems());
        availableTasks.removeAll(selectedTasks);
        mListSelectionView.getSourceItems().setAll(availableTasks);
        mListSelectionView.getTargetItems().setAll(selectedTasks);

        mActivatedCheckBox.setSelected(item.isCronActivated());
        mCronPane.getItems().setAll(item.getCronItemsAsList());

        super.load(item, dialogDescriptor);
        mItem = item;
    }

    @Override
    public Job save() {
        var map = mManager.getIdToItem();
        map.putIfAbsent(mItem.getId(), mItem);

        var execute = mItem.getExecuteSection();
        save(execute.getBefore(), mRunBeforeSection);
        save(execute.getAfterFail(), mRunAfterFailSection);
        save(execute.getAfterOk(), mRunAfterOkSection);
        save(execute.getAfter(), mRunAfterSection);

        var taskIds = mListSelectionView.getTargetItems().stream()
                .map(task -> task.getId())
                .toList();
        mItem.setTaskIds(new ArrayList<>(taskIds));
        mItem.setCronActivated(mActivatedCheckBox.isSelected());
        mItem.setCronItems(String.join("|", mCronPane.getItems().stream().sorted().map(c -> c.getName()).toList()));

        return super.save();
    }

    private ListActionView.ListAction[] createTaskTargetActions() {
        int imageSize = FxHelper.getUIScaled(16);

        return new ListActionView.ListAction[]{
            new ListActionView.ListAction<Task>(MaterialIcon._Navigation.EXPAND_LESS.getImageView(imageSize)) {
                @Override
                public void initialize(ListView<Task> listView) {
                    setEventHandler(event -> moveSelectedTasksUp(listView));
                }
            },
            new ListActionView.ListAction<Task>(MaterialIcon._Navigation.EXPAND_MORE.getImageView(imageSize)) {
                @Override
                public void initialize(ListView<Task> listView) {
                    setEventHandler(event -> moveSelectedTasksDown(listView));
                }
            }
        };
    }

    private void createUI() {
        mRunBeforeSection = new RunSectionPane(mBundle.getString("JobEditor.runBefore"), true, false);
        mRunAfterFailSection = new RunSectionPane(mBundle.getString("JobEditor.runAfterFail"), false, false);
        mRunAfterOkSection = new RunSectionPane(mBundle.getString("JobEditor.runAfterOk"), false, false);
        mRunAfterSection = new RunSectionPane(mBundle.getString("JobEditor.runAfter"), false, false);

        mListSelectionView = new ListSelectionView();
        mListSelectionView.setSourceHeader(new Label("%s %s".formatted(Dict.AVAILABLE.toString(), Dict.TASKS.toLower())));
        mListSelectionView.setTargetHeader(new Label("%s %s".formatted(Dict.SELECTED.toString(), Dict.TASKS.toLower())));
        mListSelectionView.getSourceItems().addAll(TaskManager.getInstance().getItems());
        mListSelectionView.getTargetActions().addAll(createTaskTargetActions());

        var headerLabelStyle = FxHelper.createFontStyle(1.4, FontWeight.NORMAL);
        var runLabel = new Label(Dict.RUN.toString());
        runLabel.setStyle(headerLabelStyle);
        var runBox = new VBox(FxHelper.getUIScaled(16),
                mRunBeforeSection,
                mRunAfterFailSection,
                mRunAfterOkSection,
                mRunAfterSection
        );
        var runBorderPane = new BorderPane(runBox);
        runBorderPane.setTop(runLabel);

        var cronLabel = new Label(Dict.SCHEDULER.toString());
        cronLabel.setStyle(headerLabelStyle);

        mCronPane = new NbCronPane(24);
        mCronPane.getEditableList().setPrefHeight(100);
        var cronBorderPane = new BorderPane(mCronPane.getEditableList());
        var cronBox = new VBox(cronLabel, mActivatedCheckBox);
        cronBorderPane.setTop(cronBox);
        mCronPane.getEditableList().disableProperty().bind(mActivatedCheckBox.selectedProperty().not());

        int row = 0;
        var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(8));
        gp.add(mListSelectionView, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, runBorderPane, cronBorderPane);
        FxHelper.autoSizeColumn(gp, 2);
        GridPane.setVgrow(mListSelectionView, Priority.ALWAYS);
        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 8, 0), gp, mActivatedCheckBox);
        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0),
                mRunBeforeSection,
                mListSelectionView,
                mRunAfterSection
        );

        setCenter(gp);
    }

    private void moveSelectedTasksDown(ListView<Task> listView) {
        var items = listView.getItems();
        var selectionModel = listView.getSelectionModel();
        var selectedIndices = selectionModel.getSelectedIndices();
        int lastIndex = items.size() - 1;

        for (int index = selectedIndices.size() - 1; index >= 0; index--) {
            var selectedIndex = selectedIndices.get(index);
            if (selectedIndex < lastIndex) {
                if (selectedIndices.contains(selectedIndex + 1)) {
                    continue;
                }
                var item = items.get(selectedIndex);
                var itemToBeReplaced = items.get(selectedIndex + 1);
                items.set(selectedIndex + 1, item);
                items.set(selectedIndex, itemToBeReplaced);
                selectionModel.clearSelection(selectedIndex);
                selectionModel.select(selectedIndex + 1);
            }
        }
    }

    private void moveSelectedTasksUp(ListView<Task> listView) {
        var items = listView.getItems();
        var selectionModel = listView.getSelectionModel();
        var selectedIndices = selectionModel.getSelectedIndices();

        for (var selectedIndex : selectedIndices) {
            if (selectedIndex > 0) {
                if (selectedIndices.contains(selectedIndex - 1)) {
                    continue;
                }
                var item = items.get(selectedIndex);
                var itemToBeReplaced = items.get(selectedIndex - 1);
                items.set(selectedIndex - 1, item);
                items.set(selectedIndex, itemToBeReplaced);
                selectionModel.clearSelection(selectedIndex);
                selectionModel.select(selectedIndex - 1);
            }
        }
    }
}
