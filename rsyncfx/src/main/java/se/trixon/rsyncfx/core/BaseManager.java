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
package se.trixon.rsyncfx.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.rsyncfx.ui.editor.BaseEditor;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BaseManager<T extends BaseItem> {

    private List<String> mHistoryLines = new ArrayList<>();
    private final ObjectProperty<ObservableMap<String, T>> mIdToItemProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<T>> mItemsProperty = new SimpleObjectProperty<>();

    public BaseManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
        mIdToItemProperty.setValue(FXCollections.observableHashMap());

        mIdToItemProperty.get().addListener((MapChangeListener.Change<? extends String, ? extends T> change) -> {
            var values = new ArrayList<T>(getIdToItem().values());
            values.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            getItems().setAll(values);
        });
    }

    public boolean exists(T item) {
        return getIdToItem().containsValue(item);
    }

    public T getById(String id) {
        return getIdToItem().get(id);
    }

    public abstract BaseEditor getEditor();

    public final ObservableMap<String, T> getIdToItem() {
        return mIdToItemProperty.get();
    }

    public final ObservableList<T> getItems() {
        return mItemsProperty.get();
    }

    public abstract String getLabelPlural();

    public abstract String getLabelSingular();

//    public Set<T> getSet() {
//        return new HashSet<>(getItems());
//    }
//
//    public Set<String> getSetId() {
//        var list = getItems().stream()
//                .map(task -> task.getId())
//                .toList();
//
//        return new HashSet<>(list);
//    }
    public boolean hasItems() {
        return !getIdToItem().isEmpty();
    }

    public ObjectProperty<ObservableMap<String, T>> idToItemProperty() {
        return mIdToItemProperty;
    }

    public ObjectProperty<ObservableList<T>> itemsProperty() {
        return mItemsProperty;
    }

    void loadHistory() {
        try {
            mHistoryLines = FileUtils.readLines(StorageManager.getInstance().getHistoryFile(), Charset.defaultCharset());
            for (var item : getIdToItem().values()) {
                loadHistory(item);
            }
        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
        }
    }

//    void setItems(ObservableList<T> items) {
//        mItemsProperty.get().setAll(items);
//        if (!items.isEmpty() && items.get(0) instanceof Job job) {
//            getIdToItem().forEach(item -> {
////                job.setTasks(TaskManager.getInstance().getIdToItem(job.getTaskIds()));
//            });
//        }
//    }
    private void loadHistory(T item) {
        var builder = new StringBuilder();

        for (var line : mHistoryLines) {
            var id = String.valueOf(item.getId());
            if (StringUtils.contains(line, id)) {
                builder.append(StringUtils.remove(line, id + " ")).append("\n");
            }
        }

        item.setHistory(builder.toString());
    }
}
