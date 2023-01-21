/*
 * Copyright 2022 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.rsyncfx.ui.history;

import com.dlsc.workbenchfx.Workbench;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.rsyncfx.ui.common.AlwaysOpenTab;
import se.trixon.rsyncfx.ui.common.BaseModule;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class HistoryModule extends BaseModule implements AlwaysOpenTab {

    private BorderPane mMainPane = new BorderPane();
    private BorderPane mRoot = new BorderPane();
    private Workbench mWorkbench;

    public HistoryModule() {
        super("", MaterialIcon._Action.HISTORY.getImageView(ICON_SIZE_MODULE, Color.WHITE).getImage());
    }

    @Override
    public Node activate() {
        return mRoot;
    }

    @Override
    public void init(Workbench workbench) {
        super.init(workbench);
        mWorkbench = workbench;

        createUI();
    }

    private void createUI() {
        mMainPane = new BorderPane(new Label("?"));
        mRoot = new BorderPane(mMainPane);
        mMainPane.setTop(new Button("Hello"));

        mMainPane.setBackground(FxHelper.createBackground(Color.CORAL));
        mRoot.setPadding(FxHelper.getUIScaledInsets(16, 16, 0, 16));
    }

}
