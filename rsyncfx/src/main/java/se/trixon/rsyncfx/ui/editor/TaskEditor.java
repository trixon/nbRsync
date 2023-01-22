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

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;
import se.trixon.rsyncfx.core.TaskManager;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskEditor extends BaseEditor<Task> {

    private FileChooserPane mDirDestFileChooser;
    private CheckBox mDirForceSourceSlashCheckBox;
    private FileChooserPane mDirSourceFileChooser;
    private Task mItem;
    private final TaskManager mManager = TaskManager.getInstance();
    private RunSectionPane mRunAfterFailureSection;
    private RunSectionPane mRunAfterOkSection;
    private RunSectionPane mRunAfterSection;
    private RunSectionPane mRunBeforeSection;
    private CheckBox mRunStopJobOnErrorCheckBox;

    public TaskEditor() {
        createUI();
    }

    @Override
    public void load(Task item) {
        if (item == null) {
            item = new Task();
        }

        mDirSourceFileChooser.setPath(item.getSource());
        mDirDestFileChooser.setPath(item.getDestination());
        mDirForceSourceSlashCheckBox.setSelected(item.isNoAdditionalDir());

        var execute = item.getExecuteSection();
        mRunBeforeSection.load(execute.isBefore(), execute.getBeforeCommand(), execute.isBeforeHaltOnError());
        mRunAfterFailureSection.load(execute.isAfterFailure(), execute.getAfterFailureCommand(), execute.isAfterFailureHaltOnError());
        mRunAfterOkSection.load(execute.isAfterSuccess(), execute.getAfterSuccessCommand(), execute.isAfterSuccessHaltOnError());
        mRunAfterSection.load(execute.isAfter(), execute.getAfterCommand(), execute.isAfterHaltOnError());
        mRunStopJobOnErrorCheckBox.setSelected(execute.isJobHaltOnError());

        super.load(item);
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
        execute.setBefore(mRunBeforeSection.isActivated());
        execute.setBeforeHaltOnError(mRunBeforeSection.isHaltOnError());
        execute.setBeforeCommand(mRunBeforeSection.getCommand());

        execute.setAfterFailure(mRunAfterFailureSection.isActivated());
        execute.setAfterFailureHaltOnError(mRunAfterFailureSection.isHaltOnError());
        execute.setAfterFailureCommand(mRunAfterFailureSection.getCommand());

        execute.setAfterSuccess(mRunAfterOkSection.isActivated());
        execute.setAfterSuccessHaltOnError(mRunAfterOkSection.isHaltOnError());
        execute.setAfterSuccessCommand(mRunAfterOkSection.getCommand());

        execute.setAfter(mRunAfterSection.isActivated());
        execute.setAfterHaltOnError(mRunAfterSection.isHaltOnError());
        execute.setAfterCommand(mRunAfterSection.getCommand());

        execute.setJobHaltOnError(mRunStopJobOnErrorCheckBox.isSelected());
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

    private Tab createRunTab() {
        mRunBeforeSection = new RunSectionPane(mBundle.getString("TaskEditor.runBefore"), true);
        mRunAfterFailureSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfterFailure"), true);
        mRunAfterOkSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfterOk"), true);
        mRunAfterSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfter"), true);

        mRunStopJobOnErrorCheckBox = new CheckBox(mBundle.getString("TaskEditor.stopJobOnError"));

        var root = new VBox(FxHelper.getUIScaled(12),
                mRunBeforeSection,
                mRunAfterFailureSection,
                mRunAfterOkSection,
                mRunAfterSection,
                mRunStopJobOnErrorCheckBox
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), mRunBeforeSection);

        var tab = new Tab(Dict.RUN.toString(), root);
        return tab;
    }

    private void createUI() {
        var optionsPane = new VBox();
        var optionsTab = new Tab(Dict.OPTIONS.toString(), optionsPane);

        var excludePane = new VBox();
        var excludeTab = new Tab(Dict.EXCLUDE.toString(), excludePane);

        getTabPane().getTabs().addAll(
                createDirsTab(),
                createRunTab(),
                optionsTab,
                excludeTab,
                createNoteTab()
        );
    }

}
