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
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Validator;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;
import se.trixon.rsyncfx.core.TaskManager;
import se.trixon.rsyncfx.core.task.Task;
import se.trixon.rsyncfx.ui.editor.task.DualListPane;
import se.trixon.rsyncfx.ui.editor.task.ExcludeOption;
import se.trixon.rsyncfx.ui.editor.task.OptionHandler;
import se.trixon.rsyncfx.ui.editor.task.RsyncOption;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskEditor extends BaseEditor<Task> {

    private FileChooserPane mDirDestFileChooser;
    private CheckBox mDirForceSourceSlashCheckBox;
    private FileChooserPane mDirSourceFileChooser;
    private DualListPane<ExcludeOption> mExcludeDualListPane;
    private Task mItem;
    private DualListPane<RsyncOption> mOptionDualListPane;
    private RunSectionPane mRunAfterFailSection;
    private RunSectionPane mRunAfterOkSection;
    private RunSectionPane mRunAfterSection;
    private RunSectionPane mRunBeforeSection;
    private RunSectionPane mRunExcludeSection;
    private CheckBox mRunStopJobOnErrorCheckBox;

    public TaskEditor() {
        super(TaskManager.getInstance());
        createUI();
        initValidation();
    }

    @Override
    public void load(Task item, Node saveNode) {
        if (item == null) {
            item = new Task();
        }

        mDirSourceFileChooser.setPath(item.getSource());
        mDirDestFileChooser.setPath(item.getDestination());
        mDirForceSourceSlashCheckBox.setSelected(item.isNoAdditionalDir());

        var execute = item.getExecuteSection();
        mRunBeforeSection.load(execute.getBefore());
        mRunAfterFailSection.load(execute.getAfterFail());
        mRunAfterOkSection.load(execute.getAfterOk());
        mRunAfterSection.load(execute.getAfter());
        mRunStopJobOnErrorCheckBox.setSelected(execute.isJobHaltOnError());

        mRunExcludeSection.load(item.getExcludeSection().getExternalFile());
        loadOptions(item.getOptionSection().getOptions());
        loadExcludes(item.getExcludeSection().getOptions());
        super.load(item, saveNode);
        mItem = item;
    }

    @Override
    public Task save() {
        var map = mManager.getIdToItem();
        map.putIfAbsent(mItem.getId(), mItem);

        mItem.setSource(mDirSourceFileChooser.getPathAsString());
        mItem.setDestination(mDirDestFileChooser.getPathAsString());
        mItem.setNoAdditionalDir(mDirForceSourceSlashCheckBox.isSelected());

        var execute = mItem.getExecuteSection();
        save(execute.getBefore(), mRunBeforeSection);
        save(execute.getAfterFail(), mRunAfterFailSection);
        save(execute.getAfterOk(), mRunAfterOkSection);
        save(execute.getAfter(), mRunAfterSection);
        save(mItem.getExcludeSection().getExternalFile(), mRunExcludeSection);

        execute.setJobHaltOnError(mRunStopJobOnErrorCheckBox.isSelected());

        var opts = mOptionDualListPane.getSelectedPane().getItems().stream()
                .map(o -> o.getArg())
                .toList();
        mItem.getOptionSection().setOptions(StringUtils.join(opts, " "));

        var excludes = mExcludeDualListPane.getSelectedPane().getItems().stream()
                .map(o -> o.getArg())
                .toList();
        mItem.getExcludeSection().setOptions(StringUtils.join(excludes, " "));

        return super.save();
    }

    private Tab createDirsTab() {
        var sourceTitle = Dict.SOURCE.toString();
        var destTitle = Dict.DESTINATION.toString();
        var selectionMode = SelectionMode.SINGLE;
        var objectMode = FileChooserPane.ObjectMode.FILE;

        mDirSourceFileChooser = new FileChooserPane(sourceTitle, sourceTitle, objectMode, selectionMode);
        mDirDestFileChooser = new FileChooserPane(destTitle, destTitle, objectMode, selectionMode);
        mDirForceSourceSlashCheckBox = new CheckBox(mBundle.getString("TaskEditor.forceSourceSlash"));

        var button = new Button(mBundle.getString("TaskEditor.swapSourceDest"));
        button.setOnAction(actionEvent -> {
            var source = mDirSourceFileChooser.getPathAsString();
            mDirSourceFileChooser.setPath(mDirDestFileChooser.getPathAsString());
            mDirDestFileChooser.setPath(source);
        });

        var borderPane = new BorderPane(mDirForceSourceSlashCheckBox);
        BorderPane.setAlignment(mDirForceSourceSlashCheckBox, Pos.CENTER_LEFT);
        borderPane.setRight(button);

        var root = new VBox(FxHelper.getUIScaled(12),
                mDirSourceFileChooser,
                mDirDestFileChooser,
                borderPane
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), mDirSourceFileChooser);

        var tab = new Tab(Dict.DIRECTORIES.toString(), root);

        return tab;
    }

    private Tab createExcludeTab() {
        mExcludeDualListPane = new DualListPane<>();
        mRunExcludeSection = new RunSectionPane(mBundle.getString("TaskEditor.externalFile"), false, false);
        var borderPane = new BorderPane(mExcludeDualListPane.getRoot());
        borderPane.setBottom(mRunExcludeSection);

        for (var option : ExcludeOption.values()) {
            option.setDynamicArg(null);
            mExcludeDualListPane.getAvailablePane().getItems().add(option);
        }
        mExcludeDualListPane.updateLists();

        var tab = new Tab(Dict.EXCLUDE.toString(), borderPane);

        return tab;
    }

    private Tab createOptionsTab() {
        mOptionDualListPane = new DualListPane<>();

        for (var option : RsyncOption.values()) {
            option.setDynamicArg(null);
            mOptionDualListPane.getAvailablePane().getItems().add(option);
        }

        mOptionDualListPane.updateLists();

        var tab = new Tab(Dict.OPTIONS.toString(), mOptionDualListPane.getRoot());

        return tab;
    }

    private Tab createRunTab() {
        mRunBeforeSection = new RunSectionPane(mBundle.getString("TaskEditor.runBefore"), true, true);
        mRunAfterFailSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfterFail"), true, true);
        mRunAfterOkSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfterOk"), true, true);
        mRunAfterSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfter"), true, true);

        mRunStopJobOnErrorCheckBox = new CheckBox(mBundle.getString("TaskEditor.stopJobOnError"));

        var root = new VBox(FxHelper.getUIScaled(12),
                mRunBeforeSection,
                mRunAfterFailSection,
                mRunAfterOkSection,
                mRunAfterSection,
                mRunStopJobOnErrorCheckBox
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), mRunBeforeSection);

        var tab = new Tab(Dict.RUN.toString(), root);
        return tab;
    }

    private void createUI() {
        getTabPane().getTabs().addAll(
                createDirsTab(),
                createRunTab(),
                createOptionsTab(),
                createExcludeTab(),
                createNoteTab()
        );
    }

    private void initValidation() {
        final String textRequired = "Text is required";

        Platform.runLater(() -> {
            mValidationSupport.registerValidator(mDirSourceFileChooser.getTextField(), true, Validator.createEmptyValidator(textRequired));
            mValidationSupport.registerValidator(mDirDestFileChooser.getTextField(), true, Validator.createEmptyValidator(textRequired));
        });
    }

    private void loadExcludes(String joinedOptions) {
        var options = StringUtils.splitPreserveAllTokens(joinedOptions, " ");
        var availableItems = mExcludeDualListPane.getAvailablePane().getItems();
        var selectedItems = mExcludeDualListPane.getSelectedPane().getItems();
        var itemsToRemove = new ArrayList<OptionHandler>();

        for (var optionString : options) {
            for (var option : availableItems) {
                if (StringUtils.equals(optionString, option.getArg())) {
                    selectedItems.add(option);
                    itemsToRemove.add(option);
                    break;
                }
            }
        }

        availableItems.removeAll(itemsToRemove);

        mExcludeDualListPane.updateLists();
    }

    private void loadOptions(String joinedOptions) {
        var options = StringUtils.splitPreserveAllTokens(joinedOptions, " ");
        var availableItems = mOptionDualListPane.getAvailablePane().getItems();
        var selectedItems = mOptionDualListPane.getSelectedPane().getItems();
        var itemsToRemove = new ArrayList<OptionHandler>();

        for (var optionString : options) {
            for (var option : availableItems) {
                if (optionString.contains("=")) {
                    String[] elements = StringUtils.split(optionString, "=", 2);
                    if (StringUtils.equals(elements[0], StringUtils.split(option.getArg(), "=", 2)[0])) {
                        option.setDynamicArg(elements[1]);
                        selectedItems.add(option);
                        itemsToRemove.add(option);
                    }
                } else if (StringUtils.equals(optionString, option.getArg())) {
                    selectedItems.add(option);
                    itemsToRemove.add(option);
                    break;
                }
            }
        }

        availableItems.removeAll(itemsToRemove);

        mOptionDualListPane.updateLists();
    }

}
