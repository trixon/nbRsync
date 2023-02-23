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
package se.trixon.jotasync.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LauncherButtonView extends LauncherViewBase {

    private static final int NUM_OF_BUTTONS = 9;
    private static final int NUM_OF_COLUMNS = 3;
    private final StackPane mRoot = new StackPane();

    public LauncherButtonView() {
        createUI();
    }

    @Override
    public Node getNode() {
        return mRoot;
    }

    private void createUI() {
        var mGridPane = new GridPane();

        for (int i = 0; i < NUM_OF_BUTTONS; i++) {
            var speedDialButton = new SpeedDialButton(i);
            mGridPane.add(speedDialButton.getRoot(), i % NUM_OF_COLUMNS, i % NUM_OF_BUTTONS / NUM_OF_COLUMNS);
        }

        FxHelper.autoSizeColumn(mGridPane, NUM_OF_COLUMNS);
        mGridPane.setMaxSize(1, 1);
        mRoot.getChildren().add(mGridPane);
        StackPane.setAlignment(mGridPane, Pos.CENTER);
        mGridPane.setHgap(40);
        mGridPane.setVgap(40);
    }
}
