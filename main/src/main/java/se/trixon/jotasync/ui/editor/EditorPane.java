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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.nbp.fx.NbEditableList;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import static se.trixon.almond.util.fx.FxHelper.getScaledFontSize;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.jotasync.core.BaseItem;
import se.trixon.jotasync.core.BaseManager;
import se.trixon.jotasync.core.ExecutorManager;
import se.trixon.jotasync.core.JobManager;
import se.trixon.jotasync.core.StorageManager;
import static se.trixon.jotasync.core.StorageManager.GSON;
import se.trixon.jotasync.core.TaskManager;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.task.Task;
import se.trixon.jotasync.ui.UiHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class EditorPane extends TabPane {

    private final ResourceBundle mBundle = NbBundle.getBundle(EditorPane.class);
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final JobManager mJobManager = JobManager.getInstance();
    private BaseItemPane mJobPane;
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private BaseItemPane mTaskPane;

    public EditorPane() {
        createUI();
    }

    public BaseItemPane getJobPane() {
        return mJobPane;
    }

    public BaseItemPane getTaskPane() {
        return mTaskPane;
    }

    public void load(Job job) {
        FxHelper.runLaterDelayed(10, () -> {
            mJobPane.select(job);
            mJobPane.edit(job);
        });
    }

    private void createUI() {
        Consumer<Job> onStartJob = job -> {
            mExecutorManager.requestStart(job);
        };

        mJobPane = new BaseItemPane<Job>(mJobManager, onStartJob) {
        };

        mTaskPane = new BaseItemPane<Task>(mTaskManager, null) {
        };

        setSide(Side.LEFT);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        var jobTab = new Tab(Dict.JOBS.toString(), mJobPane);

        getTabs().setAll(jobTab,
                new Tab(Dict.TASKS.toString(), mTaskPane)
        );

        getTabs().forEach(tab -> {
            tab.setStyle("-fx-font-size: %dpx;".formatted((int) (getScaledFontSize() * 1.4)));

        });
        setTabMaxHeight(99);
        getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
            SwingHelper.runLater(() -> Almond.getTopComponent("LauncherTopComponent").setHtmlDisplayName("<html><b>" + n.getText()));
        });
    }

    public abstract class BaseItemPane<T extends BaseItem> extends BorderPane {

        private EditableList<T> mEditableList;
        private final BaseManager mManager;

        public BaseItemPane(BaseManager manager, Consumer<T> onStart) {
            mManager = manager;
            createUI(onStart);
        }

        public EditableList<T> getEditableList() {
            return mEditableList;
        }

        private void createUI(Consumer<T> onStart) {
            mEditableList = new NbEditableList.Builder<T>()
                    .setIconSize(UiHelper.getIconSizeToolBar())
                    .setItemSingular(mManager.getLabelSingular())
                    .setItemPlural(mManager.getLabelPlural())
                    .setOnEdit((title, task) -> {
                        edit(title, task);
                    })
                    .setOnRemoveAll(() -> {
                        mManager.getIdToItem().clear();
                        StorageManager.save();
                    })
                    .setOnRemove(t -> {
                        mManager.getIdToItem().remove(t.getId());
                        StorageManager.save();
                    })
                    .setOnClone(t -> {
                        var original = t;
                        var json = GSON.toJson(original);
                        var clone = GSON.fromJson(json, original.getClass());
                        var uuid = UUID.randomUUID().toString();
                        clone.setId(uuid);
                        clone.setLastRun(0);
                        clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
                        mManager.getIdToItem().put(clone.getId(), clone);

                        StorageManager.save();

                        return (T) mManager.getById(uuid);
                    })
                    .setOnStart(onStart)
                    .setItemsProperty(mManager.itemsProperty())
                    .build();

            mEditableList.getListView().setCellFactory(listView -> new ItemListCellRenderer<>() {
            });

            setCenter(mEditableList);
        }

        private void edit(String title, T item) {
            var editor = mManager.getEditor();
            editor.setPadding(FxHelper.getUIScaledInsets(2, 8, 0, 8));
            var dialogPanel = new FxDialogPanel() {
                @Override
                protected void fxConstructor() {
                    setScene(new Scene(editor));
                }
            };
            dialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(800, 800));

            SwingUtilities.invokeLater(() -> {
                editor.setPrefSize(FxHelper.getUIScaled(600), FxHelper.getUIScaled(660));
                var d = new DialogDescriptor(dialogPanel, title);
                d.setValid(false);
                dialogPanel.setNotifyDescriptor(d);
                dialogPanel.initFx(() -> {
                    editor.load(item, d);
                });

                if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                    Platform.runLater(() -> {
                        var editedItem = editor.save();
                        select((T) mManager.getById(editedItem.getId()));
                    });
                }
            });
        }

        private void edit(T t) {
            edit(mEditableList.getDialogTitleEdit(t), t);
        }

        private void select(T t) {
            mEditableList.selected(t);
        }
    }

    public abstract class ItemListCellRenderer<T extends BaseItem> extends ListCell<T> {

        private final Font mDefaultFont = Font.getDefault();
        private final Label mDescLabel = new Label();
        private final Label mLastLabel = new Label();
        private final Label mNameLabel = new Label();
        private VBox mRoot;
        private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();

        public ItemListCellRenderer() {
            createUI();
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                clearContent();
            } else {
                addContent(item);
            }
        }

        private void addContent(T item) {
            setText(null);

            mNameLabel.setText(item.getName());
            mDescLabel.setText(StringUtils.defaultIfBlank(item.getDescription(), "-"));
            String lastRun = "-";
            if (item.getLastRun() != 0) {
                lastRun = mSimpleDateFormat.format(new Date(item.getLastRun()));
            }
            mLastLabel.setText(lastRun);

            mRoot.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    if (item instanceof Job job) {
                        mExecutorManager.requestStart(job);
                    } else {
                        var itemPane = (BaseItemPane<T>) getListView().getParent().getParent();
                        itemPane.edit(getItem());
                    }
                }
            });
            setGraphic(mRoot);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            var fontFamily = mDefaultFont.getFamily();
            var fontSize = FxHelper.getScaledFontSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.4));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));
            mLastLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));

            mRoot = new VBox(mNameLabel, mDescLabel, mLastLabel);
            mRoot.setAlignment(Pos.CENTER_LEFT);
        }
    }
}
