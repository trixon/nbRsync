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

import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.nbp.fx.NbEditableList;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.core.BaseItem;
import se.trixon.jotasync.core.BaseManager;
import se.trixon.jotasync.core.JobManager;
import static se.trixon.jotasync.core.Storage.GSON;
import se.trixon.jotasync.core.StorageManager;
import se.trixon.jotasync.core.TaskManager;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class EditorPane extends HBox {

    private final ResourceBundle mBundle = NbBundle.getBundle(EditorPane.class);
    private Job mJob;
    private final JobManager mJobManager = JobManager.getInstance();
    private BaseItemPane mJobPane;
    private final Jota mJota = Jota.getInstance();
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private BaseItemPane mTaskPane;

    public EditorPane() {
        createUI();
        mJota.getGlobalState().addListener(gsce -> {
            load(gsce.getValue());
        }, Jota.GSC_EDITOR);
    }

    public BaseItemPane getJobPane() {
        return mJobPane;
    }

    public BaseItemPane getTaskPane() {
        return mTaskPane;
    }

    private void createUI() {
        mJobPane = new BaseItemPane<Job>(mJobManager) {
        };
        mTaskPane = new BaseItemPane<Task>(mTaskManager) {
        };
        getChildren().setAll(mJobPane, mTaskPane);
        HBox.setHgrow(mJobPane, Priority.ALWAYS);
        HBox.setHgrow(mTaskPane, Priority.ALWAYS);
    }

    private void load(Job job) {
        FxHelper.runLaterDelayed(10, () -> {
            mJobPane.select(job);
            mJobPane.edit(job);
        });
    }

    public abstract class BaseItemPane<T extends BaseItem> extends BorderPane {

        private EditableList<T> mEditableList;
        private final BaseManager mManager;

        public BaseItemPane(BaseManager manager) {
            mManager = manager;
            createUI();
        }

        private void createUI() {
            mEditableList = new NbEditableList.Builder<T>()
                    .setIconSize(Jota.getIconSizeToolBar())
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
                    .setItemsProperty(mManager.itemsProperty())
                    .build();

            mEditableList.getListView().setCellFactory(listView -> new ItemListCellRenderer<>() {
            });

            setCenter(mEditableList);
        }

        private void edit(String title, T item) {
            var editor = mManager.getEditor();
            editor.setPadding(FxHelper.getUIScaledInsets(8, 8, 0, 8));
            var dialogPanel = new FxDialogPanel() {
                @Override
                protected void fxConstructor() {
                    setScene(new Scene(editor));
                }
            };
            dialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(800, 600));

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
            edit(Dict.EDIT.toString(), t);
        }

        private void select(T t) {
            mEditableList.selected(t);
        }
    }

    public abstract class ItemListCellRenderer<T extends BaseItem> extends ListCell<T> {

        private final Font mDefaultFont = Font.getDefault();
        private final Label mDescLabel = new Label();
        private final Label mNameLabel = new Label();
        private final VBox mRoot = new VBox();

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
            mDescLabel.setText(item.getDescription());
            mRoot.getChildren().setAll(mNameLabel, mDescLabel);
            mRoot.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    var itemPane = (BaseItemPane<T>) getListView().getParent().getParent();
                    itemPane.edit(getItem());
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
            var fontSize = mDefaultFont.getSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.4));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));
        }
    }
}
