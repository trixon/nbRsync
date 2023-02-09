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
package se.trixon.rsyncfx.ui;

import com.dlsc.gemsfx.util.StageManager;
import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import se.trixon.almond.nbp.core.ModuleHelper;
import se.trixon.almond.util.CircularInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.AboutModel;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.ExceptionDialogDisplayerHandler;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.rsyncfx.Jota;
import se.trixon.rsyncfx.Options;
import se.trixon.rsyncfx.ui.common.BaseModule;
import se.trixon.rsyncfx.ui.common.CustomTab;
import se.trixon.rsyncfx.ui.editor.EditorModule;
import se.trixon.rsyncfx.ui.history.HistoryModule;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class App extends Application {

    public static final String APP_TITLE = "JotaSync";
    private Action mAboutAction;
    private Action mAboutRsyncAction;
    private Action mHomeAction;
    private Action mEditorAction;
    private WorkbenchModule mEditorModule;
    private Action mHelpAction;
    private WorkbenchModule mHistoryModule;
    private WorkbenchModule mMainModule;
    private final Options mOptions = Options.getInstance();
    private Action mOptionsAction;
    private Action mQuitAction;
    private Action mRestartAction;
    private final Jota mJota = Jota.getInstance();
    private Stage mStage;
    private StatusBar mStatusBar = new StatusBar();
    private Workbench mWorkbench;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Logger.getLogger("").addHandler(new ExceptionDialogDisplayerHandler(stage));
        mStage = stage;
        mJota.setStage(stage);
        createUI();

        initAccelerators();
        updateNightMode();
        FxHelper.removeSceneInitFlicker(mStage);
        initListeners();

        mStage.show();
        FxHelper.runLaterDelayed(10, () -> {
            mWorkbench.openModule(mMainModule);
//            mWorkbench.openModule(mEditorModule);
//            mWorkbench.openModule(mHistoryModule);

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
        mStage.setWidth(500);
        mStage.setHeight(500);
        mStage.centerOnScreen();
        var stageManager = StageManager.install(mStage, mOptions.getPreferences().node("stage"), 1, 1);
        stageManager.setSupportFullScreenAndMaximized(!SystemUtils.IS_OS_MAC);
        initActions();

        mMainModule = new MainModule();
        mEditorModule = new EditorModule();
        mHistoryModule = new HistoryModule();

        mWorkbench = Workbench.builder(
                mMainModule,
                mEditorModule,
                mHistoryModule)
                .tabFactory(CustomTab::new)
                .build();
        mWorkbench.getStylesheets().add(App.class.getResource("baseTheme.css").toExternalForm());
        mJota.setWorkbench(mWorkbench);

        var root = new BorderPane(mWorkbench);

        var actions = List.of(
                new ActionGroup(Dict.FILE_MENU.toString(),
                        mRestartAction,
                        mQuitAction
                ),
                new ActionGroup(Dict.TOOLS.toString(),
                        mHomeAction,
                        mEditorAction,
                        ActionUtils.ACTION_SEPARATOR,
                        mOptionsAction
                ),
                new ActionGroup(Dict.HELP.toString(),
                        mHelpAction,
                        ActionUtils.ACTION_SEPARATOR,
                        mAboutRsyncAction,
                        mAboutAction
                )
        );

        var menubar = ActionUtils.createMenuBar(actions);

        root.setTop(menubar);
        root.setBottom(mStatusBar);

        var scene = new Scene(root);
        FxHelper.applyFontScale(scene);
        mStage.setScene(scene);
    }

    private void displayHelp() {
        System.out.println("HELP");
    }

    private void displayRsyncInformation() {
        new Thread(() -> {
            var processBuilder = new ProcessBuilder(new String[]{mOptions.getRsyncPath()});
            String result = "";

            try {
                var process = processBuilder.start();
                result = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
                process.waitFor();
                var information = StringUtils.substringBefore(result, "Usage: rsync");
                FxHelper.runLater(() -> {
                    var alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initOwner(mJota.getStage());

                    alert.setTitle(Dict.ABOUT_S.toString().formatted("rsync"));
                    alert.setGraphic(null);
                    alert.setHeaderText(null);
                    alert.setResizable(true);

                    alert.setContentText(information);
                    var dialogPane = alert.getDialogPane();

                    dialogPane.setPrefWidth(FxHelper.getUIScaled(600));
                    FxHelper.removeSceneInitFlicker(dialogPane);

                    FxHelper.showAndWait(alert, mJota.getStage());
                });
            } catch (IOException | InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();
    }

    private void initAccelerators() {
        var accelerators = mStage.getScene().getAccelerators();

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

        accelerators.put(new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN), () -> {
            mHomeAction.handle(null);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), () -> {
            mEditorAction.handle(null);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), () -> {
            mQuitAction.handle(null);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), () -> {
            mRestartAction.handle(null);
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
        //restart
        mRestartAction = new Action(Dict.RESTART.toString(), actionEvent -> {
            SystemHelper.runLaterDelayed(0, () -> {
                var lifecycleManager = LifecycleManager.getDefault();
                lifecycleManager.markForRestart();
                lifecycleManager.exit();
            });
        });
        mRestartAction.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));

        //quit
        mQuitAction = new Action(Dict.QUIT.toString(), actionEvent -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        mQuitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));

        //home
        mHomeAction = new Action(Dict.HOME.toString(), actionEvent -> {
            mWorkbench.openModule(mMainModule);
        });
        mHomeAction.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN));

        //editor
        mEditorAction = new Action(Dict.EDITOR.toString(), actionEvent -> {
            mWorkbench.openModule(mEditorModule);
        });
        mEditorAction.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));

        //options
        mOptionsAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            OptionsPane.displayOptions();
        });

        //help
        mHelpAction = new Action(Dict.HELP.toString(), actionEvent -> {
            displayHelp();
        });
        mHelpAction.setAccelerator(new KeyCodeCombination(KeyCode.F1));

        //about
        var pomInfo = new PomInfo(App.class, "se.trixon.rsyncfx", "rsyncfx");
        var aboutModel = new AboutModel(SystemHelper.getBundle(App.class, "about"), SystemHelperFx.getResourceAsImageView(App.class, "logo.png"));
        aboutModel.setAppVersion(pomInfo.getVersion());
        aboutModel.setAppDate(ModuleHelper.getBuildTime(App.class));

        var aboutAction = AboutPane.getAction(mStage, aboutModel);
        mAboutAction = new Action(Dict.ABOUT_S.toString().formatted(APP_TITLE), actionEvent -> {
            aboutAction.handle(actionEvent);
        });

        mAboutRsyncAction = new Action(Dict.ABOUT_S.toString().formatted("rsync"), actionEvent -> {
            displayRsyncInformation();
        });
    }

    private void initListeners() {
        mOptions.nightModeProperty().addListener((p, o, n) -> {
            updateNightMode();
        });

        mJota.getGlobalState().addListener(gsce -> {
            mWorkbench.openModule(mEditorModule);
        }, Jota.GSC_EDITOR);
    }

    private void updateNightMode() {
        MaterialIcon.setDefaultColor(mOptions.isNightMode() ? Color.LIGHTGRAY : Color.BLACK);

        String lightTheme = getClass().getResource("lightTheme.css").toExternalForm();
        String darkTheme = getClass().getResource("darkTheme.css").toExternalForm();
        String darculaTheme = FxHelper.class.getResource("darcula.css").toExternalForm();

        var stylesheets = mWorkbench.getStylesheets();
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
