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
package se.trixon.rsyncfx;

import com.dlsc.gemsfx.util.StageManager;
import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.LifecycleManager;
import se.trixon.almond.nbp.core.ModuleHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.AboutModel;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.rsyncfx.ui.JobEditorPane;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class App extends Application {

    public static final String APP_TITLE = "rsyncFX";
    private Action mAboutAction;
    private Action mAboutRsyncAction;
    private AppModule mAppModule;
    private Action mEditorAction;
    private Action mHelpAction;
    private Action mHistoryAction;
    private final Options mOptions = Options.getInstance();
    private Action mOptionsAction;
    private final RsyncFx mRsyncFx = RsyncFx.getInstance();
    private Stage mStage;
    private Workbench mWorkbench;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        mRsyncFx.setStage(stage);
        createUI();

        initAccelerators();

        mStage.show();
    }

    @Override
    public void stop() throws Exception {
        LifecycleManager.getDefault().exit();
    }

    private void createUI() {
        mStage.getIcons().add(new Image(App.class.getResourceAsStream("logo.png")));
        mStage.setTitle(APP_TITLE);
        int minWidth = FxHelper.getUIScaled(200);
        mStage.setMinWidth(minWidth);
        int minHeight = FxHelper.getUIScaled(200);
        mStage.setMinHeight(minHeight);
        StageManager.install(mStage, mOptions.getPreferences().node("stage"), minWidth, minHeight);
        initActions();

        mAppModule = new AppModule();
        mWorkbench = Workbench.builder(mAppModule)
                .toolbarLeft(new ToolbarItem())
                .navigationDrawerItems(
                        ActionUtils.createMenuItem(mEditorAction),
                        ActionUtils.createMenuItem(mHistoryAction),
                        ActionUtils.createMenuItem(mOptionsAction),
                        ActionUtils.createMenuItem(mHelpAction),
                        ActionUtils.createMenuItem(mAboutRsyncAction),
                        ActionUtils.createMenuItem(mAboutAction)
                )
                .build();
        mWorkbench.getStylesheets().add(AppModule.class.getResource("customTheme.css").toExternalForm());

        mRsyncFx.setWorkbench(mWorkbench);

        var scene = new Scene(mWorkbench);
        scene.setFill(Color.web("#bb6624"));
        FxHelper.applyFontScale(scene);
        mStage.setScene(scene);
    }

    private void displayAboutRsync() {
        System.out.println("ABOUT RSYNC");
    }

    private void displayHelp() {
        System.out.println("HELP");
    }

    private void displayHistory() {
        System.out.println("HISTORY");
    }

    private void displayOptions() {
        System.out.println("OPTIONS");
    }

    private void initAccelerators() {
        var accelerators = mStage.getScene().getAccelerators();

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), () -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), () -> {
            var lifecycleManager = LifecycleManager.getDefault();
            lifecycleManager.markForRestart();
            lifecycleManager.exit();
        });

        accelerators.put(new KeyCodeCombination(KeyCode.F1), () -> {
            mHelpAction.handle(null);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), () -> {
            mEditorAction.handle(null);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN), () -> {
            mHistoryAction.handle(null);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), () -> {
        });

        if (!SystemUtils.IS_OS_MAC) {
            mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
            accelerators.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), () -> {
                mOptionsAction.handle(null);
            });
        }
    }

    private void initActions() {
        //editor
        mEditorAction = new Action(Dict.EDITOR.toString(), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            JobEditorPane.displayJobEditor(null);
        });

        //history
        mHistoryAction = new Action(Dict.HISTORY.toString(), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            displayHistory();
        });

        //options
        mOptionsAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            displayOptions();
        });

        //help
        mHelpAction = new Action(Dict.HELP.toString(), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            displayHelp();
        });

        //about rsync
        mAboutRsyncAction = new Action(String.format(Dict.ABOUT_S.toString(), "rsync"), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            displayAboutRsync();
        });

        //about
        var pomInfo = new PomInfo(App.class, "se.trixon.rsyncfx", "rsyncfx");
        var aboutModel = new AboutModel(SystemHelper.getBundle(App.class, "about"), SystemHelperFx.getResourceAsImageView(App.class, "logo.png"));
        aboutModel.setAppVersion(pomInfo.getVersion());
        aboutModel.setAppDate(ModuleHelper.getBuildTime(App.class));

        var aboutAction = AboutPane.getAction(mStage, aboutModel);
        mAboutAction = new Action(aboutAction.getText(), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            aboutAction.handle(actionEvent);
        });
    }
}
