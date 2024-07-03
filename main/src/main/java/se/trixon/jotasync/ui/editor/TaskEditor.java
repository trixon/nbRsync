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
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.swing.JFileChooser;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Validator;
import org.openide.DialogDescriptor;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPaneSwingFx;
import se.trixon.almond.util.fx.control.FilterableListSelectionView;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.core.TaskManager;
import se.trixon.jotasync.core.task.Task;
import se.trixon.jotasync.ui.editor.task.ArgBase;
import se.trixon.jotasync.ui.editor.task.ArgExclude;
import se.trixon.jotasync.ui.editor.task.ArgRsync;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskEditor extends BaseEditor<Task> {

    private FilterableListSelectionView<ArgExclude> mArgExcludeListSelectionView;
    private FilterableListSelectionView<ArgRsync> mArgRsyncListSelectionView;
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
        getTabPane().getSelectionModel().selectFirst();
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

        var opts = mArgRsyncListSelectionView.getUnfilteredTargetItems().stream()
                .map(o -> o.getArg())
                .collect(Collectors.joining(" "));
        mItem.getOptionSection().setOptions(opts);

        var excludes = mArgExcludeListSelectionView.getUnfilteredTargetItems().stream()
                .map(o -> o.getArg())
                .collect(Collectors.joining(" "));
        mItem.getExcludeSection().setOptions(excludes);

        return super.save();
    }

    private Tab createArgExcludeTab() {
        mArgExcludeListSelectionView = new FilterableListSelectionView<>();
        mArgExcludeListSelectionView.setCellFactory(listView -> new OptionListCell<>());
        mArgExcludeListSelectionView.setFilterSourceHeader(new Label(Dict.AVAILABLE.toString()));
        mArgExcludeListSelectionView.setFilterTargetHeader(new Label(Dict.SELECTED.toString()));
//        mArgExcludeListSelectionView.setComparator((o1, o2) -> {
//            return o1.getTitle().compareToIgnoreCase(o2.getTitle());
//        });
        mArgExcludeListSelectionView.setFilterSourcePredicate(arg -> {
            var filterText = mArgExcludeListSelectionView.getFilterTextSource();
            return matches(arg, filterText);
        });
        mArgExcludeListSelectionView.setFilterTargetPredicate(arg -> {
            var filterText = mArgExcludeListSelectionView.getFilterTextTarget();
            return matches(arg, filterText);
        });
        mArgExcludeListSelectionView.setPadding(FxHelper.getUIScaledInsets(8, 0, 16, 0));
        mRunExcludeSection = new RunSectionPane(mBundle.getString("TaskEditor.externalFile"), false, false);
        var borderPane = new BorderPane(mArgExcludeListSelectionView);
        borderPane.setBottom(mRunExcludeSection);

        for (var arg : ArgExclude.values()) {
            arg.setDynamicArg(null);
        }

        var tab = new Tab(Dict.EXCLUDE.toString(), borderPane);

        return tab;
    }

    private Tab createRunTab() {
        mRunBeforeSection = new RunSectionPane(mBundle.getString("TaskEditor.runBefore"), true, false);
        mRunAfterFailSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfterFail"), true, false);
        mRunAfterOkSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfterOk"), true, false);
        mRunAfterSection = new RunSectionPane(mBundle.getString("TaskEditor.runAfter"), true, false);

        mArgRsyncListSelectionView = new FilterableListSelectionView();
        mArgRsyncListSelectionView.setCellFactory(listView -> new OptionListCell<>());
        mArgRsyncListSelectionView.setFilterSourceHeader(new Label("%s %s".formatted(Dict.AVAILABLE.toString(), Dict.COMMANDS.toLower())));
        mArgRsyncListSelectionView.setFilterTargetHeader(new Label("%s %s".formatted(Dict.SELECTED.toString(), Dict.COMMANDS.toLower())));
        mArgRsyncListSelectionView.setFilterSourcePredicate(arg -> {
            var filterText = mArgRsyncListSelectionView.getFilterTextSource();
            return matches(arg, filterText);
        });
        mArgRsyncListSelectionView.setFilterTargetPredicate(arg -> {
            var filterText = mArgRsyncListSelectionView.getFilterTextTarget();
            return matches(arg, filterText);
        });

        for (var arg : ArgRsync.values()) {
            arg.setDynamicArg(null);
//            mArgRsyncDualListPane.getAvailablePane().getItems().add(arg);
        }

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), mRunBeforeSection);
        int row = 0;
        var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(8));
        mRunStopJobOnErrorCheckBox = new CheckBox(mBundle.getString("TaskEditor.stopJobOnError"));
        gp.add(mRunStopJobOnErrorCheckBox, 0, row++, GridPane.REMAINING, 1);
        gp.add(mRunBeforeSection, 0, row++, GridPane.REMAINING, 1);
        gp.add(mArgRsyncListSelectionView, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, mRunAfterFailSection, mRunAfterOkSection);
        gp.add(mRunAfterSection, 0, row++, GridPane.REMAINING, 1);
        FxHelper.autoSizeColumn(gp, 2);
        GridPane.setVgrow(mArgRsyncListSelectionView, Priority.ALWAYS);
        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 8, 0), gp);
        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0),
                mRunBeforeSection,
                mRunAfterSection
        );

        var tab = new Tab(Dict.RUN.toString(), gp);

        return tab;
    }

    private void createUI() {
        var sourceTitle = Dict.SOURCE.toString();
        var destTitle = Dict.DESTINATION.toString();

        mDirSourceFileChooser = new FileChooserPaneSwingFx(sourceTitle, sourceTitle, Almond.getFrame(), JFileChooser.DIRECTORIES_ONLY);
        mDirDestFileChooser = new FileChooserPaneSwingFx(destTitle, destTitle, Almond.getFrame(), JFileChooser.DIRECTORIES_ONLY);
        getGridPane().addRow(2, mDirSourceFileChooser, mDirDestFileChooser);
        var forceSourceSlash = mBundle.getString("TaskEditor.forceSourceSlash");
        mDirForceSourceSlashCheckBox = new CheckBox();
        var a = new Text(StringUtils.substringBefore(forceSourceSlash, "/"));
        var b = new Text(StringUtils.substringBetween(forceSourceSlash, "/"));
        var c = new Text(StringUtils.substringAfterLast(forceSourceSlash, "/"));
        b.setUnderline(true);
        var hBox = new HBox(a, b, c);
        hBox.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8));
        mDirForceSourceSlashCheckBox.setGraphic(hBox);
        var button = new Button(mBundle.getString("TaskEditor.swapSourceDest"));
        button.setOnAction(actionEvent -> {
            var oldSource = mDirSourceFileChooser.getPathAsString();
            var oldDest = mDirDestFileChooser.getPathAsString();
            if (mDirForceSourceSlashCheckBox.isSelected()) {
                oldDest = StringUtils.appendIfMissing(oldDest, "/");
            } else {
                oldDest = StringUtils.removeEnd(oldDest, "/");
            }
            mDirSourceFileChooser.setPath(oldDest);
            mDirDestFileChooser.setPath(oldSource);
        });

        var leftRightBorderPane = new BorderPane(mDirForceSourceSlashCheckBox);
        BorderPane.setAlignment(mDirForceSourceSlashCheckBox, Pos.CENTER_LEFT);
        leftRightBorderPane.setRight(button);
        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0),
                mDirSourceFileChooser,
                mDirDestFileChooser,
                leftRightBorderPane
        );

        var borderPane = new BorderPane(getTabPane());
        borderPane.setTop(leftRightBorderPane);

        getTabPane().getTabs().addAll(
                createRunTab(),
                createArgExcludeTab()
        );

        setCenter(borderPane);
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
        var targetItems = FXCollections.<ArgExclude>observableArrayList();

        for (var optionString : StringUtils.splitPreserveAllTokens(joinedOptions, " ")) {
            for (var option : ArgExclude.values()) {
                if (StringUtils.equals(optionString, option.getArg())) {
                    targetItems.add(option);
                    break;
                }
            }
        }

        mArgExcludeListSelectionView.filterLoad(FXCollections.observableArrayList(ArgExclude.values()), targetItems);
    }

    private void loadArgRsync(String joinedOptions) {
        var targetItems = FXCollections.<ArgRsync>observableArrayList();

        for (var optionString : StringUtils.splitPreserveAllTokens(joinedOptions, " ")) {
            for (var option : ArgRsync.values()) {
                if (optionString.contains("=")) {
                    String[] elements = StringUtils.split(optionString, "=", 2);
                    if (StringUtils.equals(elements[0], StringUtils.split(option.getArg(), "=", 2)[0])) {
                        option.setDynamicArg(elements[1]);
                        targetItems.add(option);
                    }
                } else if (StringUtils.equals(optionString, option.getArg())) {
                    targetItems.add(option);
                    break;
                }
            }
        }

        mArgRsyncListSelectionView.filterLoad(FXCollections.observableArrayList(ArgRsync.values()), targetItems);
    }

    private boolean matches(ArgBase argExclude, String filterText) {
        return StringHelper.matchesSimpleGlob(filterText, true, true,
                argExclude.getArg(),
                argExclude.getLongArg(),
                argExclude.getShortArg(),
                argExclude.getTitle());
    }

    class OptionListCell<T extends ArgBase> extends ListCell<T> {

        protected final Font mDefaultFont = Font.getDefault();
        private final Label mArgLabel = new Label();
        private final Label mDescLabel = new Label();
        private VBox mRoot;

        public OptionListCell() {
            createUI();
        }

        @Override
        protected void updateItem(T option, boolean empty) {
            super.updateItem(option, empty);
            if (option == null || empty) {
                clearContent();
            } else {
                addContent(option);
            }
        }

        private void addContent(ArgBase argBase) {
            setText(null);
            String separator = (StringUtils.isBlank(argBase.getLongArg()) || StringUtils.isBlank(argBase.getShortArg())) ? "" : ", ";

            var arg = "%s%s%s".formatted(argBase.getShortArg(), separator, argBase.getLongArg());
            if (StringUtils.isBlank(arg)) {
                arg = argBase.getArg();
            }
            mDescLabel.setText(argBase.getTitle());
            mArgLabel.setText(arg);
            mRoot.setOnMouseClicked(mouseEvent -> {
                //TODO
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    Jota.getInstance().getGlobalState().put("dblclck_" + "mKey", argBase);
                }
            });

            setGraphic(mRoot);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            var fontSize = FxHelper.getScaledFontSize();
            var fontStyle = "-fx-font-size: %.0fpx; -fx-font-weight: %s;";

            mDescLabel.setStyle(fontStyle.formatted(fontSize * 1.0, "bold"));
            mArgLabel.setStyle(fontStyle.formatted(fontSize * 1.0, "normal"));

            mRoot = new VBox(mDescLabel, mArgLabel);

            mRoot.setAlignment(Pos.CENTER_LEFT);
        }
    }

}
