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

import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LauncherGridView extends LauncherViewBase {

    private static final int NUM_OF_BUTTONS = 9;
    private static final int NUM_OF_COLUMNS = 3;
    private final ArrayList<SpeedDialButton> mButtons = new ArrayList<>();
    private final GridPane mGridPane = new GridPane();
    private final BorderPane mRoot = new BorderPane();

    public LauncherGridView() {
        createUI();
        initBindings();
    }

    @Override
    public Node getNode() {
        return mRoot;
    }

    private void createUI() {
//        mRoot.setBackground(FxHelper.createBackground(Color.GRAY));

        for (int i = 0; i < NUM_OF_BUTTONS; i++) {
            var speedDialButton = new SpeedDialButton(i);
            mButtons.add(speedDialButton);
            mGridPane.add(speedDialButton.getRoot(), i % NUM_OF_COLUMNS, i % NUM_OF_BUTTONS / NUM_OF_COLUMNS);
        }

        FxHelper.autoSizeColumn(mGridPane, NUM_OF_COLUMNS);
        mGridPane.setMaxSize(1, 1);
        mRoot.setCenter(mGridPane);
        VBox.setVgrow(mGridPane, Priority.NEVER);
    }

    private void initBindings() {
    }

}
