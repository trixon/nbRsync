/*
 * Copyright 2023 Patrik Karlström.
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
package se.trixon.rsyncfx.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.rsyncfx.core.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class MainGridView extends MainViewBase {

    private final ComboBox<Job> mComboBox = new ComboBox<>();
    private final GridPane mGridPane = new GridPane();
    private final BorderPane mRoot = new BorderPane();

    public MainGridView() {
        createUI();
        initBindings();
    }

    @Override
    public Node getNode() {
        return mRoot;
    }

    private void createUI() {
//        mRoot.setBackground(FxHelper.createBackground(Color.BLUE));
        mGridPane.setBackground(FxHelper.createBackground(Color.CYAN));
        mComboBox.prefWidthProperty().bind(mRoot.widthProperty());
        mRoot.setOpacity(1.0);
        var button = new Button("job");
        mGridPane.add(button, 0, 0);

        mGridPane.setMaxSize(400, 300);
//        mRoot.getChildren().setAll(mComboBox, mGridPane);
//        mRoot = new VBox(mComboBox, mGridPane);
        mRoot.setCenter(mGridPane);
        mRoot.setTop(mComboBox);
        var label = new Label("xxx");
        mRoot.setCenter(mGridPane);
        label.setAlignment(Pos.TOP_RIGHT);
        label.setBackground(FxHelper.createBackground(Color.RED));
        VBox.setVgrow(mGridPane, Priority.NEVER);
//        GridPane.setFillWidth(label, true);
    }

    private void initBindings() {
        mComboBox.itemsProperty().bind(mJobManager.itemsProperty());
    }

}
