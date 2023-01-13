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
package se.trixon.rsyncfx;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class AppModule extends WorkbenchModule {

    private static final int MODULE_ICON_SIZE = 32;
    private BorderPane mRoot = new BorderPane();
    private Workbench mWorkbench;

    public AppModule() {
        super("", MaterialIcon._Places.CASINO.getImageView(MODULE_ICON_SIZE).getImage());
    }

    @Override
    public void init(Workbench workbench) {
        super.init(workbench);
        mWorkbench = workbench;

        createUI();
    }

    @Override
    public Node activate() {
        return mRoot;
    }

    private void createUI() {
        mRoot = new BorderPane(new Label("?"));
    }

}
