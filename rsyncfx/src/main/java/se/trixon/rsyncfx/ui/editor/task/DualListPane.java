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
package se.trixon.rsyncfx.ui.editor.task;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class DualListPane<T extends OptionHandler> {

    private final ListPane<T> mAvailablePane = new ListPane<>();
    private final ListPane<T> mSelectedPane = new ListPane<>();
    private HBox mRoot = new HBox();

    public DualListPane() {
        mAvailablePane.setHeader(Dict.AVAILABLE.toString());
        mSelectedPane.setHeader(Dict.SELECTED.toString());

        ToolBar toolBar = new ToolBar();
        toolBar.setOrientation(Orientation.VERTICAL);
        toolBar.setBackground(FxHelper.createBackground(Color.CORAL));
        mRoot.getChildren().addAll(mAvailablePane.getRoot(), toolBar, mSelectedPane.getRoot());
        HBox.setHgrow(mAvailablePane.getRoot(), Priority.ALWAYS);
        HBox.setHgrow(mSelectedPane.getRoot(), Priority.ALWAYS);
    }

    public ListPane<T> getAvailablePane() {
        return mAvailablePane;
    }

    public Node getRoot() {
        return mRoot;
    }

    public ListPane<T> getSelectedPane() {
        return mSelectedPane;
    }
}
