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
package se.trixon.jotasync.ui.editor;

import java.util.Comparator;
import java.util.HashSet;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.textfield.TextFields;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class FilterableListSelectionView<T> extends ListSelectionView<T> {

    private Comparator<T> mComparator;
    private Predicate<T> mFilterSourcePredicate;
    private Predicate<T> mFilterTargetPredicate;
    private final FilterSection mSourceFilterSection = new FilterSection(FilterMode.SOURCE);
    private final FilterSection mTargetFilterSection = new FilterSection(FilterMode.TARGET);

    public FilterableListSelectionView() {
        getSourceItems().addListener((ListChangeListener.Change<? extends T> c) -> {
            while (c.next()) {
                mTargetFilterSection.mUnfilteredItems.removeAll(c.getAddedSubList());
            }
        });

        getTargetItems().addListener((ListChangeListener.Change<? extends T> c) -> {
            while (c.next()) {
                c.getAddedSubList().stream().forEachOrdered(t -> {
                    if (!mTargetFilterSection.mUnfilteredItems.contains(t)) {
                        mTargetFilterSection.mUnfilteredItems.add(t);
                    }
                });
            }
        });
    }

    public void filterLoad(ObservableList<T> sourceItems, ObservableList<T> targetItems) {
        sourceItems.removeAll(targetItems);
        getUnfilteredSourceItems().setAll(sourceItems);
        getUnfilteredTargetItems().setAll(targetItems);
        mSourceFilterSection.mFilterTextField.clear();
        mTargetFilterSection.mFilterTextField.clear();

        updateLists();
    }

    public String getFilterText(FilterMode mode) {
        if (mode == FilterMode.SOURCE) {
            return mSourceFilterSection.mFilterTextField.getText();
        } else {
            return mTargetFilterSection.mFilterTextField.getText();
        }
    }

    public ObservableList<T> getUnfilteredSourceItems() {
        return mSourceFilterSection.mUnfilteredItems;
    }

    public ObservableList<T> getUnfilteredTargetItems() {
        return mTargetFilterSection.mUnfilteredItems;
    }

    public void setComparator(Comparator< T> comparator) {
        mComparator = comparator;
    }

    public void setFilterSourcePredicate(Predicate<T> sourcePredicate) {
        mFilterSourcePredicate = sourcePredicate;
    }

    public void setFilterTargetPredicate(Predicate<T> targetPredicate) {
        mFilterTargetPredicate = targetPredicate;
    }

    public void setFilterableSourceHeader(Node node) {
        mSourceFilterSection.getChildren().set(0, node);
    }

    public void setFilterableTargetHeader(Node node) {
        mTargetFilterSection.getChildren().set(0, node);
    }

    public void updateLists() {
        mSourceFilterSection.updateList();
        mTargetFilterSection.updateList();
    }

    public enum FilterMode {
        SOURCE, TARGET;
    }

    public class FilterSection extends VBox {

        private final FilterMode mFilterMode;
        private final TextField mFilterTextField = TextFields.createClearableTextField();
        private final ObservableList<T> mUnfilteredItems = FXCollections.observableArrayList();

        public FilterSection(FilterMode filterMode) {
            mFilterMode = filterMode;
            createUI();
            initListeners();
        }

        private void createUI() {
            setSpacing(FxHelper.getUIScaled(4.0));
            mFilterTextField.setPromptText(Dict.FILTER.toString());

            if (mFilterMode == FilterMode.SOURCE) {
                getChildren().addAll(getSourceHeader(), mFilterTextField);
                setSourceHeader(this);
            } else {
                getChildren().addAll(getTargetHeader(), mFilterTextField);
                setTargetHeader(this);
            }
        }

        private void initListeners() {
            mFilterTextField.textProperty().addListener((p, o, n) -> {
                updateList();
            });
        }

        private void updateList() {
            var theOtherSideAsSet = new HashSet(mFilterMode == FilterMode.SOURCE ? getUnfilteredTargetItems() : getUnfilteredSourceItems());
            var filteredItems = mUnfilteredItems.stream()
                    .filter(mFilterMode == FilterMode.SOURCE ? mFilterSourcePredicate : mFilterTargetPredicate)
                    .filter(t -> !theOtherSideAsSet.contains(t))
                    .toList();

            if (mComparator != null) {
                filteredItems = filteredItems.stream().sorted(mComparator).toList();
            }

            var items = mFilterMode == FilterMode.SOURCE ? getSourceItems() : getTargetItems();
            items.setAll(filteredItems);
        }
    }
}
