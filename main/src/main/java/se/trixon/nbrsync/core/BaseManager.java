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
package se.trixon.nbrsync.core;

import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.StringUtils;
import se.trixon.nbrsync.ui.editor.BaseEditor;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BaseManager<T extends BaseItem> {

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

    public boolean exists(String name) {
        return getItems().stream()
                .anyMatch(item -> (StringUtils.equalsIgnoreCase(name, item.getName())));
    }

    public T getById(String id) {
        return getIdToItem().get(id);
    }

    public T getByName(String name) {
        for (var item : getItems()) {
            if (StringUtils.equalsIgnoreCase(name, item.getName())) {
                return item;
            }
        }

        return null;
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

    public boolean hasItems() {
        return !getIdToItem().isEmpty();
    }

    public ObjectProperty<ObservableMap<String, T>> idToItemProperty() {
        return mIdToItemProperty;
    }

    public ObjectProperty<ObservableList<T>> itemsProperty() {
        return mItemsProperty;
    }
}
