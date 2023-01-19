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
import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.LifecycleManager;
import se.trixon.almond.nbp.core.ModuleHelper;
import se.trixon.almond.util.CircularInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.AboutModel;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.rsyncfx.RsyncFx.getIconSizeToolBarInt;
import se.trixon.rsyncfx.ui.BaseModule;
import se.trixon.rsyncfx.ui.CustomTab;
import se.trixon.rsyncfx.ui.EditorModule;
import se.trixon.rsyncfx.ui.HistoryModule;
import se.trixon.rsyncfx.ui.MainModule;
import se.trixon.rsyncfx.ui.OptionsPane;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class App extends Application {

    public static final String APP_TITLE = "rsyncFX";
    private Action mAboutAction;
    private WorkbenchModule mEditorModule;
    private Action mHelpAction;
    private WorkbenchModule mHistoryModule;
    private WorkbenchModule mMainModule;
    private final Options mOptions = Options.getInstance();
    private Action mOptionsAction;
    private final RsyncFx mRsyncFx = RsyncFx.getInstance();
    private Stage mStage;
    private Workbench mWorkbench;
    private StatusBar mStatusBar = new StatusBar();

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
        updateNightMode();
        FxHelper.removeSceneInitFlicker(mStage);
        initListeners();

        mStage.show();
        FxHelper.runLaterDelayed(0, () -> {
            mWorkbench.openModule(mMainModule);
            mWorkbench.openModule(mEditorModule);
            mWorkbench.openModule(mHistoryModule);
            mWorkbench.openModule(mMainModule);
            mWorkbench.openModule(mEditorModule);

            for (var module : mWorkbench.getModules()) {
                if (module instanceof BaseModule baseModule) {
                    baseModule.postInit();
                }
            }
        });
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

        mMainModule = new MainModule();
        mEditorModule = new EditorModule();
        mHistoryModule = new HistoryModule();

        mWorkbench = Workbench.builder(
                mMainModule,
                mEditorModule,
                mHistoryModule)
                .tabFactory(CustomTab::new)
                .navigationDrawerItems(
                        ActionUtils.createMenuItem(mOptionsAction),
                        ActionUtils.createMenuItem(mHelpAction),
                        ActionUtils.createMenuItem(mAboutAction)
                )
                .build();
        mWorkbench.getStylesheets().add(App.class.getResource("baseTheme.css").toExternalForm());
        mRsyncFx.setWorkbench(mWorkbench);

        var root = new BorderPane(mWorkbench);
        root.setBottom(mStatusBar);

        var scene = new Scene(root);
        FxHelper.applyFontScale(scene);
        mStage.setScene(scene);
    }

    private void displayHelp() {
        System.out.println("HELP");
    }

    private void initAccelerators() {
        var accelerators = mStage.getScene().getAccelerators();

        for (int i = 0; i < 10; i++) {
            final int index = i;
            var r = (Runnable) () -> {
                if (index == 0) {
                    if (mWorkbench.getNavigationDrawer().isVisible()) {
                        mWorkbench.hideNavigationDrawer();
                    } else {
                        mWorkbench.showNavigationDrawer();
                    }
                } else if (mWorkbench.getOpenModules().size() >= index) {
                    mWorkbench.openModule(mWorkbench.getOpenModules().get(index - 1));
                }
            };

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + i), KeyCombination.SHORTCUT_DOWN), r);
            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("NUMPAD" + i), KeyCombination.SHORTCUT_DOWN), r);
        }

        accelerators.put(new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHORTCUT_DOWN), () -> {
            var openModules = mWorkbench.getOpenModules();
            for (int i = 0; i < openModules.size(); i++) {
                var module = openModules.get(i);
                if (module == mWorkbench.getActiveModule()) {
                    var circularInt = new CircularInt(0, openModules.size() - 1, i);
                    mWorkbench.openModule(openModules.get(circularInt.inc()));
                    break;
                }
            }
        });

        accelerators.put(new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHORTCUT_DOWN, KeyCodeCombination.SHIFT_DOWN), () -> {
            var openModules = mWorkbench.getOpenModules();
            for (int i = 0; i < openModules.size(); i++) {
                var module = openModules.get(i);
                if (module == mWorkbench.getActiveModule()) {
                    var circularInt = new CircularInt(0, openModules.size() - 1, i);
                    mWorkbench.openModule(openModules.get(circularInt.dec()));
                    break;
                }
            }
        });

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

        if (!SystemUtils.IS_OS_MAC) {
            mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
            accelerators.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), () -> {
                mOptionsAction.handle(null);
            });
        }
    }

    private void initActions() {
        //options
        mOptionsAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            OptionsPane.displayOptions();
        });

        //help
        mHelpAction = new Action(Dict.HELP.toString(), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            displayHelp();
        });

        //about
        var pomInfo = new PomInfo(App.class, "se.trixon.rsyncfx", "rsyncfx");
        var aboutModel = new AboutModel(SystemHelper.getBundle(App.class, "about"), SystemHelperFx.getResourceAsImageView(App.class, "logo.png"));
        aboutModel.setAppVersion(pomInfo.getVersion());
        aboutModel.setAppDate(ModuleHelper.getBuildTime(App.class));

        var aboutAction = AboutPane.getAction(mStage, aboutModel);
        mAboutAction = new Action(Dict.ABOUT_S.toString().formatted(APP_TITLE), actionEvent -> {
            mWorkbench.hideNavigationDrawer();
            aboutAction.handle(actionEvent);
        });
    }

    private void initListeners() {
        mOptions.nightModeProperty().addListener((p, o, n) -> {
            updateNightMode();
        });
    }

    private void updateNightMode() {
        MaterialIcon.setDefaultColor(mOptions.isNightMode() ? Color.LIGHTGRAY : Color.BLACK);

        mOptionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(getIconSizeToolBarInt()));
        mHelpAction.setGraphic(MaterialIcon._Action.HELP_OUTLINE.getImageView(getIconSizeToolBarInt()));
        mAboutAction.setGraphic(MaterialIcon._Action.INFO_OUTLINE.getImageView(getIconSizeToolBarInt()));

        String lightTheme = getClass().getResource("lightTheme.css").toExternalForm();
        String darkTheme = getClass().getResource("darkTheme.css").toExternalForm();
        String darculaTheme = FxHelper.class.getResource("darcula.css").toExternalForm();

        ObservableList<String> stylesheets = mWorkbench.getStylesheets();
        FxHelper.setDarkThemeEnabled(mOptions.isNightMode());

        if (mOptions.isNightMode()) {
            FxHelper.loadDarkTheme(mStage.getScene());
            stylesheets.remove(lightTheme);
            stylesheets.add(darkTheme);
            stylesheets.add(darculaTheme);
        } else {
            FxHelper.unloadDarkTheme(mStage.getScene());
            stylesheets.remove(darkTheme);
            stylesheets.remove(darculaTheme);
            stylesheets.add(lightTheme);
        }
    }
}
