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
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.openide.LifecycleManager;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class App extends Application {

    public static final String APP_TITLE = "rsyncFX";
    private final Options mOptions = Options.getInstance();
    private Stage mStage;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        createUI();
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

        var label = new Label(APP_TITLE);
        var scene = new Scene(label);
        scene.setFill(Color.web("#bb6624"));
        FxHelper.applyFontScale(scene);
        mStage.setScene(scene);
    }

}
