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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.nbp.fx.NbEditableList;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.TimeHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.nbrsync.core.BaseItem;
import se.trixon.nbrsync.core.BaseManager;
import se.trixon.nbrsync.core.ExecutorManager;
import se.trixon.nbrsync.core.JobManager;
import se.trixon.nbrsync.core.Server;
import se.trixon.nbrsync.core.StorageManager;
import static se.trixon.nbrsync.core.StorageManager.GSON;
import se.trixon.nbrsync.core.TaskManager;
import se.trixon.nbrsync.core.job.Job;
import se.trixon.nbrsync.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class EditorPane extends TabPane {

    private static final int ICON_SIZE_TOOLBAR = FxHelper.getUIScaled(28);
    private final ResourceBundle mBundle = NbBundle.getBundle(EditorPane.class);
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final JobManager mJobManager = JobManager.getInstance();
    private BaseItemPane mJobPane;
    private final Consumer<Task> mOnStartTask;
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private BaseItemPane mTaskPane;

    public EditorPane() {
        mOnStartTask = task -> {
            var job = new Job();
            job.setName("(%s)".formatted(task.getName()));
            job.getTaskIds().add(task.getId());
            mExecutorManager.requestStart(job);
        };

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

        mTaskPane = new BaseItemPane<Task>(mTaskManager, mOnStartTask) {
        };

        setSide(Side.LEFT);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        var jobTab = new Tab(Dict.JOBS.toString(), mJobPane);

        getTabs().setAll(jobTab,
                new Tab(Dict.TASKS.toString(), mTaskPane)
        );

        getTabs().forEach(tab -> {
            tab.setStyle(FxHelper.createFontStyle(1.2, FontWeight.NORMAL));

        });
        setTabMaxHeight(99);
        getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
            SwingHelper.runLater(() -> Almond.getTopComponent("LauncherTopComponent").setHtmlDisplayName("<html><b>" + n.getText()));
        });
    }

    public abstract class BaseItemPane<T extends BaseItem> extends BorderPane {

        private EditableList<T> mEditableList;
        private final Map<Class<? extends BaseManager>, Scene> mItemClassToScene = new HashMap<>();
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
                    .setIconSize(ICON_SIZE_TOOLBAR)
                    .setItemSingular(mManager.getLabelSingular())
                    .setItemPlural(mManager.getLabelPlural())
                    .setOnEdit((title, item) -> {
                        edit(title, item);
                    })
                    .setOnRemoveAll(() -> {
                        mManager.getIdToItem().clear();
                        StorageManager.save();
                    })
                    .setOnRemove(item -> {
                        mManager.getIdToItem().remove(item.getId());
                        StorageManager.save();
                    })
                    .setOnClone(item -> {
                        var original = item;
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
            var scene = mItemClassToScene.computeIfAbsent(mManager.getClass(), k -> {
                return new Scene(editor);
            });

            var dialogPanel = new FxDialogPanel() {
                @Override
                protected void fxConstructor() {
                    setScene(scene);
                }
            };

            if (editor instanceof JobEditor) {
                dialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(700, 650));
            } else {
                dialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(700, 550));
            }

            SwingUtilities.invokeLater(() -> {
                var d = new DialogDescriptor(dialogPanel, title);
                d.setValid(false);
                editor.setNotificationLineSupport(d.createNotificationLineSupport());
                dialogPanel.setNotifyDescriptor(d);
                dialogPanel.initFx(() -> {
                    editor.load(item, d);
                });

                if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                    Platform.runLater(() -> {
                        var editedItem = editor.save();
                        select((T) mManager.getById(editedItem.getId()));
                        Server.getInstance().markForReload();
                    });
                } else {
                    Platform.runLater(() -> editor.cancel());
                }
            });
        }

        private void edit(T t) {
            edit(mEditableList.getDialogTitleEdit(t), t);
        }

        private void select(T t) {
            mEditableList.select(t);
        }
    }

    public abstract class ItemListCellRenderer<T extends BaseItem> extends ListCell<T> {

        private final Label mDescLabel = new Label();
        private final Label mLastLabel = new Label();
        private final Label mNameLabel = new Label();
        private GridPane mRoot;
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

            if (item instanceof Job job) {
                var cronIdicator = job.isScheduled() ? " ⏰" : "";
                mNameLabel.setText(item.getName() + cronIdicator);
            } else {
                mNameLabel.setText(item.getName());
            }

            mDescLabel.setText(StringUtils.defaultIfBlank(item.getDescription(), "-"));
            String lastRun = "-";
            if (item.getLastRun() > 0) {
                var minSec = TimeHelper.millisToMinSec(item.getDuration());
                lastRun = String.format("%s (%dm %ds)",
                        mSimpleDateFormat.format(new Date(item.getLastRun())),
                        minSec[0],
                        minSec[1]
                );
            }
            mLastLabel.setText(lastRun);

            mRoot.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    if (mouseEvent.isControlDown()) {
                        var itemPane = (BaseItemPane<T>) getListView().getParent().getParent();
                        itemPane.edit(getItem());
                    } else if (item instanceof Job job) {
                        mExecutorManager.requestStart(job);
                    } else if (item instanceof Task task) {
                        mOnStartTask.accept(task);
                    }
                }
            });
            setGraphic(mRoot);

            var sb = new StringBuilder();
            switch (item) {
                case Job job -> {
                    job.getTasks().forEach(task -> {
                        appendTask(sb, task);
                        sb.append("\r");
                    });
                }
                case Task task -> {
                    appendTask(sb, task);
                }
                default -> {
                }
            }

            for (int i = sb.length() - 1; i > 0; i--) {
                var c = sb.charAt(i);

                if (c == '\r' || c == '\n' || c == ' ') {
                    sb.deleteCharAt(i);
                } else {
                    break;
                }
            }

            var tooltipString = StringUtils.defaultIfBlank(sb.toString(), mBundle.getString("noTasksForJob"));
            var tooltip = new Tooltip(tooltipString);
            tooltip.setShowDelay(Duration.seconds(2));
            tooltip.setHideDelay(Duration.seconds(5));
            tooltip.setStyle(FxHelper.createFontStyle(1.0, FontWeight.BOLD));
            mRoot.getChildren().stream()
                    .filter(n -> n instanceof Control)
                    .map(n -> (Control) n)
                    .forEach(c -> {
                        c.setTooltip(tooltip);
                        FxHelper.autoSizeRegionHorizontal(c);
                    });
        }

        private void appendTask(StringBuilder sb, Task task) {
            var command = StringUtils.remove(task.getCommandAsString(), task.getPath(task.getSource()));
            command = StringUtils.remove(command, task.getPath(task.getDestination()));
            sb.append(task.getName().toUpperCase(Locale.ROOT)).append("\r");
            sb.append(task.getSource()).append("\r");
            sb.append(task.getDestination()).append("\r");
            sb.append(command).append("\r");
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            mNameLabel.setStyle(FxHelper.createFontStyle(1.2, FontWeight.BOLD));
            mDescLabel.setStyle(FxHelper.createFontStyle(1.1, FontWeight.NORMAL));
            mLastLabel.setStyle(FxHelper.createFontStyle(1.1, FontWeight.NORMAL));

            mRoot = new GridPane();
            mRoot.addColumn(0, mNameLabel, mDescLabel, mLastLabel);

            var copyRsyncAction = new Action(mBundle.getString("copyRsyncOptions"), actionEvent -> {
                var rsync = "rsync ";
                var command = rsync;
                switch (getItem()) {
                    case Job job ->
                        command = String.join("\n", job.getTasks().stream().map(task -> rsync + task.getCommandAsString()).toList());
                    case Task task ->
                        command = rsync + task.getCommandAsString();
                    default -> {
                    }
                }

                SystemHelper.copyToClipboard(command);
            });
            copyRsyncAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(FxHelper.getUIScaled(16)));

            var actions = Arrays.asList(copyRsyncAction);
            var contextMenu = ActionUtils.createContextMenu(actions);

            setOnMousePressed(mouseEvent -> {
                getScene().getWindow().requestFocus();
                if (getItem() != null) {
                    if (mouseEvent.isSecondaryButtonDown()) {
                        contextMenu.show(this, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                    }
                }
            });
        }
    }
}
