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
package se.trixon.rsyncfx.ui.editor;

import java.util.ArrayList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ListActionView;
import org.controlsfx.control.ListSelectionView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;
import se.trixon.almond.util.fx.control.FileChooserPane.ObjectMode;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.rsyncfx.core.JobManager;
import se.trixon.rsyncfx.core.TaskManager;
import se.trixon.rsyncfx.core.job.Job;
import se.trixon.rsyncfx.core.task.Task;

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
    private final JobManager mManager = JobManager.getInstance();
    private FileChooserPane mRunAfterFailFileChooser;
    private FileChooserPane mRunAfterFileChooser;
    private FileChooserPane mRunAfterOkFileChooser;
    private RunSectionPane mRunBeforeSection;

    public JobEditor() {
        createUI();
    }

    @Override
    public void load(Job item) {
        if (item == null) {
            item = new Job();
        }

        var execute = item.getExecuteSection();
        mRunBeforeSection.load(execute.isBefore(), execute.getBeforeCommand(), execute.isBeforeHaltOnError());
        loadRun(mRunAfterFailFileChooser, execute.isAfterFailure(), execute.getAfterFailureCommand());
        loadRun(mRunAfterOkFileChooser, execute.isAfterSuccess(), execute.getAfterSuccessCommand());
        loadRun(mRunAfterFileChooser, execute.isAfter(), execute.getAfterCommand());

        mLogOutputCheckBox.setSelected(item.isLogOutput());
        mLogErrorsCheckBox.setSelected(item.isLogErrors());
        mLogSeparateCheckBox.setSelected(item.isLogSeparateErrors());

        mLogToggleGroup.selectToggle(mLogToggleGroup.getToggles().get(item.getLogMode()));

        var tasks = item.getTasks();
        mListSelectionView.getSourceItems().removeAll(tasks);
        mListSelectionView.getTargetItems().setAll(tasks);

        super.load(item);
        mItem = item;
    }

    @Override
    public Job save() {
        var map = mManager.getIdToItem();
        map.putIfAbsent(mItem.getId(), mItem);

        var execute = mItem.getExecuteSection();
        execute.setBefore(mRunBeforeSection.isActivated());
        execute.setBeforeHaltOnError(mRunBeforeSection.isHaltOnError());
        execute.setBeforeCommand(mRunBeforeSection.getCommand());

        execute.setAfterFailure(mRunAfterFailFileChooser.getCheckBox().isSelected());
        execute.setAfterFailureCommand(mRunAfterFailFileChooser.getPathAsString());

        execute.setAfterSuccess(mRunAfterOkFileChooser.getCheckBox().isSelected());
        execute.setAfterSuccessCommand(mRunAfterOkFileChooser.getPathAsString());

        execute.setAfter(mRunAfterFileChooser.getCheckBox().isSelected());
        execute.setAfterCommand(mRunAfterFileChooser.getPathAsString());

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
        var dialogTitle = mBundle.getString("JobEditor.selectFileToRun");
        var selectionMode = SelectionMode.SINGLE;
        var objectMode = ObjectMode.FILE;

        mRunBeforeSection = new RunSectionPane(mBundle.getString("JobEditor.runBefore"), true);
        mRunAfterFailFileChooser = new FileChooserPane(dialogTitle, objectMode, selectionMode, mBundle.getString("JobEditor.runAfterFail"));
        mRunAfterOkFileChooser = new FileChooserPane(dialogTitle, objectMode, selectionMode, mBundle.getString("JobEditor.runAfterOk"));
        mRunAfterFileChooser = new FileChooserPane(dialogTitle, objectMode, selectionMode, mBundle.getString("JobEditor.runAfter"));

        var root = new VBox(FxHelper.getUIScaled(12),
                mRunBeforeSection,
                mRunAfterFailFileChooser,
                mRunAfterOkFileChooser,
                mRunAfterFileChooser
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), mRunBeforeSection, mRunAfterFailFileChooser);

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
                createLogTab()
        );
    }

    private void loadRun(FileChooserPane fcp, boolean selected, String command) {
        fcp.getCheckBox().setSelected(selected);
        fcp.setPath(command);
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
