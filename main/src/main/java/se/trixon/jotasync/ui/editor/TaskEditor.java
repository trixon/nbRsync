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

import java.io.File;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javax.swing.JFileChooser;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Validator;
import org.openide.DialogDescriptor;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPaneSwingFx;
import se.trixon.jotasync.core.TaskManager;
import se.trixon.jotasync.core.task.Task;
import se.trixon.jotasync.ui.editor.task.ArgBase;
import se.trixon.jotasync.ui.editor.task.ArgExclude;
import se.trixon.jotasync.ui.editor.task.ArgRsync;
import se.trixon.jotasync.ui.editor.task.DualListPane;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskEditor extends BaseEditor<Task> {

    private DualListPane<ArgExclude> mArgExcludeDualListPane;
    private DualListPane<ArgRsync> mArgRsyncDualListPane;
    private FileChooserPaneSwingFx mDirDestFileChooser;
    private CheckBox mDirForceSourceSlashCheckBox;
    private FileChooserPaneSwingFx mDirSourceFileChooser;
    private Task mItem;
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
        initListeners();
    }

    @Override
    public void load(Task item, DialogDescriptor dialogDescriptor) {
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
        loadArgRsync(item.getOptionSection().getOptions());
        loadArgExcludes(item.getExcludeSection().getOptions());

//        mDirForceSourceSlashCheckBox.setSelected(item.isNoAdditionalDir());
        mDirForceSourceSlashCheckBox.setSelected(StringUtils.endsWith(mDirSourceFileChooser.getPathAsString(), File.separator));
        super.load(item, dialogDescriptor);
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

        var opts = mArgRsyncDualListPane.getSelectedPane().getItems().stream()
                .map(o -> o.getArg())
                .toList();
        mItem.getOptionSection().setOptions(StringUtils.join(opts, " "));

        var excludes = mArgExcludeDualListPane.getSelectedPane().getItems().stream()
                .map(o -> o.getArg())
                .toList();
        mItem.getExcludeSection().setOptions(StringUtils.join(excludes, " "));

        return super.save();
    }

    private Tab createArgExcludeTab() {
        mArgExcludeDualListPane = new DualListPane<>();
        mArgExcludeDualListPane.getRoot().setPadding(FxHelper.getUIScaledInsets(8, 0, 16, 0));
        mRunExcludeSection = new RunSectionPane(mBundle.getString("TaskEditor.externalFile"), false, false);
        var borderPane = new BorderPane(mArgExcludeDualListPane.getRoot());
        borderPane.setBottom(mRunExcludeSection);

        for (var arg : ArgExclude.values()) {
            arg.setDynamicArg(null);
            mArgExcludeDualListPane.getAvailablePane().getItems().add(arg);
        }
        mArgExcludeDualListPane.updateLists();

        var tab = new Tab(Dict.EXCLUDE.toString(), borderPane);

        return tab;
    }

    private Tab createArgRsyncTab() {
        mArgRsyncDualListPane = new DualListPane<>();
        mArgRsyncDualListPane.getRoot().setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0));

        for (var arg : ArgRsync.values()) {
            arg.setDynamicArg(null);
            mArgRsyncDualListPane.getAvailablePane().getItems().add(arg);
        }

        mArgRsyncDualListPane.updateLists();

        var tab = new Tab(Dict.OPTIONS.toString(), mArgRsyncDualListPane.getRoot());

        return tab;
    }

    private Tab createDirsTab() {
        var sourceTitle = Dict.SOURCE.toString();
        var destTitle = Dict.DESTINATION.toString();
        var selectionMode = SelectionMode.SINGLE;

        mDirSourceFileChooser = new FileChooserPaneSwingFx(sourceTitle, sourceTitle, Almond.getFrame(), JFileChooser.DIRECTORIES_ONLY, selectionMode);
        mDirDestFileChooser = new FileChooserPaneSwingFx(destTitle, destTitle, Almond.getFrame(), JFileChooser.DIRECTORIES_ONLY, selectionMode);
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
                createArgRsyncTab(),
                createArgExcludeTab()
        );
    }

    private void initListeners() {
        mDirForceSourceSlashCheckBox.selectedProperty().addListener((p, o, n) -> {
            var path = mDirSourceFileChooser.getPathAsString();
            if (n) {
                if (!StringUtils.endsWith(path, File.separator)) {
                    mDirSourceFileChooser.setPath(path + File.separator);
                }
            } else {
                mDirSourceFileChooser.setPath(StringUtils.removeEnd(path, File.separator));
            }
        });
    }

    private void initValidation() {
        final String textRequired = "Text is required";

        Platform.runLater(() -> {
            mValidationSupport.registerValidator(mDirSourceFileChooser.getTextField(), true, Validator.createEmptyValidator(textRequired));
            mValidationSupport.registerValidator(mDirDestFileChooser.getTextField(), true, Validator.createEmptyValidator(textRequired));
        });
    }

    private void loadArgExcludes(String joinedOptions) {
        var options = StringUtils.splitPreserveAllTokens(joinedOptions, " ");
        var availableItems = mArgExcludeDualListPane.getAvailablePane().getItems();
        var selectedItems = mArgExcludeDualListPane.getSelectedPane().getItems();
        var itemsToRemove = new ArrayList<ArgBase>();

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

        mArgExcludeDualListPane.updateLists();
    }

    private void loadArgRsync(String joinedOptions) {
        var options = StringUtils.splitPreserveAllTokens(joinedOptions, " ");
        var availableItems = mArgRsyncDualListPane.getAvailablePane().getItems();
        var selectedItems = mArgRsyncDualListPane.getSelectedPane().getItems();
        var itemsToRemove = new ArrayList<ArgBase>();

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

        mArgRsyncDualListPane.updateLists();
    }

}
