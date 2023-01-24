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
package se.trixon.rsyncfx.ui;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.rsyncfx.Options;
import se.trixon.rsyncfx.RsyncFx;
import static se.trixon.rsyncfx.RsyncFx.getIconSizeToolBarInt;
import se.trixon.rsyncfx.ui.common.AlwaysOpenTab;
import se.trixon.rsyncfx.ui.common.BaseModule;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class MainModule extends BaseModule implements AlwaysOpenTab {

    private MainGridView mMainGridView;
    private MainListView mMainListView;
    private final BorderPane mRoot = new BorderPane();

    public MainModule() {
        super(Dict.HOME.toString(), MaterialIcon._Action.HOME.getImageView(ICON_SIZE_MODULE, Color.WHITE).getImage());
    }

    @Override
    public Node activate() {
        return mRoot;
    }

    @Override
    public void init(Workbench workbench) {
        super.init(workbench);

        createUI();
        initAccelerators();
        updateMainMode();
    }

    private void createUI() {
        mMainGridView = new MainGridView();
        mMainListView = new MainListView();

        var startToolbarItem = new ToolbarItem(Dict.START.toString(), MaterialIcon._Av.PLAY_ARROW.getImageView(getIconSizeToolBarInt(), Color.WHITE), mouseEvent -> {
            doStart();
        });

        var gridToolbarItem = new ToolbarItem(MaterialIcon._Navigation.APPS.getImageView(getIconSizeToolBarInt(), Color.WHITE), mouseEvent -> {
            updateMainMode(0);
        });
        gridToolbarItem.setTooltip(new Tooltip("GRID VIEW"));

        var listToolbarItem = new ToolbarItem(MaterialIcon._Action.LIST.getImageView(getIconSizeToolBarInt(), Color.WHITE), mouseEvent -> {
            updateMainMode(1);
        });
        listToolbarItem.setTooltip(new Tooltip("LIST VIEW"));

        getToolbarControlsLeft().setAll(
                startToolbarItem
        );

        getToolbarControlsRight().setAll(
                gridToolbarItem,
                listToolbarItem
        );

//        var nullSelectionBooleanBinding = mListView.getSelectionModel().selectedItemProperty().isNull();
//        startToolbarItem.disableProperty().bind(nullSelectionBooleanBinding);
    }

    private void doStart() {
        System.out.println("START");
    }

    private void initAccelerators() {
        var accelerators = RsyncFx.getInstance().getStage().getScene().getAccelerators();

        accelerators.put(new KeyCodeCombination(KeyCode.NUMPAD1, KeyCombination.SHORTCUT_DOWN), () -> {
            updateMainMode(0);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.NUMPAD1, KeyCombination.SHORTCUT_DOWN), () -> {
            updateMainMode(0);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN), () -> {
            updateMainMode(0);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.NUMPAD2, KeyCombination.SHORTCUT_DOWN), () -> {
            updateMainMode(1);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN), () -> {
            updateMainMode(1);
        });

    }

    private void updateMainMode() {
        if (mOptions.getInt(Options.KEY_MAIN_MODE, Options.DEFAULT_MAIN_MODE) == 0) {
            mRoot.setCenter(mMainGridView.getNode());
        } else {
            mRoot.setCenter(mMainListView.getNode());
        }
    }

    private void updateMainMode(int mode) {
        mOptions.put(Options.KEY_MAIN_MODE, mode);
        updateMainMode();
    }
}
