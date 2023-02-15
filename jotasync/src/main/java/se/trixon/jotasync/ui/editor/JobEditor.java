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

import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ListActionView;
import org.controlsfx.control.ListSelectionView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jotasync.core.JobManager;
import se.trixon.jotasync.core.TaskManager;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class JobEditor extends BaseEditor<Job> {

    private Job mItem;
    private ListSelectionView<Task> mListSelectionView;
    private RadioButton mLogAppendRadioButton;
    private CheckBox mLogErrorsCheckBox;
    private CheckBox mLogOutputCheckBox;
    private RadioButton mLogReplaceRadioButton;
    private CheckBox mLogSeparateCheckBox;
    private final ToggleGroup mLogToggleGroup = new ToggleGroup();
    private RadioButton mLogUniqueRadioButton;
    private RunSectionPane mRunAfterFailSection;
    private RunSectionPane mRunAfterOkSection;
    private RunSectionPane mRunAfterSection;
    private RunSectionPane mRunBeforeSection;

    public JobEditor() {
        super(JobManager.getInstance());
        createUI();
    }

    @Override
    public void load(Job item, Node saveNode) {
        if (item == null) {
            item = new Job();
        }

        var execute = item.getExecuteSection();
        mRunBeforeSection.load(execute.getBefore());
        mRunAfterFailSection.load(execute.getAfterFail());
        mRunAfterOkSection.load(execute.getAfterOk());
        mRunAfterSection.load(execute.getAfter());

        mLogOutputCheckBox.setSelected(item.isLogOutput());
        mLogErrorsCheckBox.setSelected(item.isLogErrors());
        mLogSeparateCheckBox.setSelected(item.isLogSeparateErrors());

        mLogToggleGroup.selectToggle(mLogToggleGroup.getToggles().get(item.getLogMode()));

        var tasks = item.getTasks();
        mListSelectionView.getSourceItems().removeAll(tasks);
        mListSelectionView.getTargetItems().setAll(tasks);

        super.load(item, saveNode);
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

        mItem.setLogOutput(mLogOutputCheckBox.isSelected());
        mItem.setLogErrors(mLogErrorsCheckBox.isSelected());
        mItem.setLogSeparateErrors(mLogSeparateCheckBox.isSelected());

        mItem.setLogMode(mLogToggleGroup.getToggles().indexOf(mLogToggleGroup.getSelectedToggle()));

        var taskIds = mListSelectionView.getTargetItems().stream()
                .map(task -> task.getId())
                .toList();
        mItem.setTaskIds(new ArrayList<>(taskIds));

        return super.save();
    }

    private Tab createLogTab() {
        mLogOutputCheckBox = new CheckBox(Dict.LOG_OUTPUT.toString());
        mLogErrorsCheckBox = new CheckBox(Dict.LOG_ERRORS.toString());
        mLogSeparateCheckBox = new CheckBox(Dict.LOG_SEPARATE_ERRORS.toString());

        mLogAppendRadioButton = new RadioButton(Dict.APPEND.toString());
        mLogReplaceRadioButton = new RadioButton(Dict.REPLACE.toString());
        mLogUniqueRadioButton = new RadioButton(Dict.UNIQUE.toString());

        mLogAppendRadioButton.setToggleGroup(mLogToggleGroup);
        mLogReplaceRadioButton.setToggleGroup(mLogToggleGroup);
        mLogUniqueRadioButton.setToggleGroup(mLogToggleGroup);

        var root = new GridPane();
        root.addColumn(0, mLogOutputCheckBox, mLogErrorsCheckBox, mLogSeparateCheckBox);
        root.addColumn(1, mLogAppendRadioButton, mLogReplaceRadioButton, mLogUniqueRadioButton);
        root.setPadding(FxHelper.getUIScaledInsets(16));
        root.setHgap(FxHelper.getUIScaled(32));
        root.setVgap(FxHelper.getUIScaled(12));

        var tab = new Tab(Dict.LOGGING.toString(), root);

        return tab;
    }

    private Tab createRunTab() {
        mRunBeforeSection = new RunSectionPane(mBundle.getString("JobEditor.runBefore"), true, true);
        mRunAfterFailSection = new RunSectionPane(mBundle.getString("JobEditor.runAfterFail"), false, true);
        mRunAfterOkSection = new RunSectionPane(mBundle.getString("JobEditor.runAfterOk"), false, true);
        mRunAfterSection = new RunSectionPane(mBundle.getString("JobEditor.runAfter"), false, false);

        var root = new VBox(FxHelper.getUIScaled(12),
                mRunBeforeSection,
                mRunAfterFailSection,
                mRunAfterOkSection,
                mRunAfterSection
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), mRunBeforeSection, mRunAfterFailSection);

        var tab = new Tab(Dict.RUN.toString(), root);

        return tab;
    }

    private Tab createTaskTab() {
        mListSelectionView = new ListSelectionView();
        mListSelectionView.setSourceHeader(new Label(Dict.AVAILABLE.toString()));
        mListSelectionView.setTargetHeader(new Label(Dict.SELECTED.toString()));
        mListSelectionView.getSourceItems().addAll(TaskManager.getInstance().getItems());
        mListSelectionView.getTargetActions().addAll(createTaskTargetActions());

        var tab = new Tab(Dict.TASKS.toString(), mListSelectionView);

        return tab;
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
        getTabPane().getTabs().addAll(
                createTaskTab(),
                createRunTab(),
                createLogTab(),
                createNoteTab()
        );
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
