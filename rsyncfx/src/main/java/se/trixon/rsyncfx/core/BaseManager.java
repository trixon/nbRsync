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
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.rsyncfx.core.job.Job;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class BaseManager<T extends BaseItem> {

    private List<String> mHistoryLines = new ArrayList<>();
    private final ObjectProperty<ObservableList<T>> mItemsProperty = new SimpleObjectProperty<>();

    public BaseManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
    }

    public T[] getArray() {
        return (T[]) getItems().toArray();
    }

    public final ObservableList<T> getItems() {
        return mItemsProperty.get();
    }

    public T getById(String id) {
        for (var item : getItems()) {
            if (StringUtils.equals(id, item.getId())) {
                return item;
            }
        }

        return null;
    }

    public boolean hasItems() {
        return !getItems().isEmpty();
    }

    public boolean exists(T item) {

        for (var existingTask : getItems()) {
            if (item.getId() == existingTask.getId()) {
                return true;
            }
        }

        return false;
    }

    public ObjectProperty<ObservableList<T>> itemsProperty() {
        return mItemsProperty;
    }

    void loadHistory() {
        try {
            mHistoryLines = FileUtils.readLines(StorageManager.getInstance().getHistoryFile(), Charset.defaultCharset());
            for (var item : getItems()) {
                loadHistory(item);
            }
        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
        }
    }

    void setItems(ObservableList<T> items) {
        mItemsProperty.get().setAll(items);
        if (!items.isEmpty() && items.get(0) instanceof Job job) {
            getItems().forEach(item -> {
//                job.setTasks(TaskManager.getInstance().getItems(job.getTaskIds()));
            });
        }
    }

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
