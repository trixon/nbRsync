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

import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.Options;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class OptionsPane extends VBox {

    private static final Jota sJota = Jota.getInstance();
    private final ResourceBundle mBundle = NbBundle.getBundle(OptionsPane.class);
    private final ToggleSwitch mNightModeToggleSwitch = new ToggleSwitch(Dict.NIGHT_MODE.toString());
    private final Options mOptions = Options.getInstance();
    private final FileChooserPane mRsyncChooser = new FileChooserPane(Dict.SELECT.toString(), mBundle.getString("options.rsyncPath"), FileChooserPane.ObjectMode.FILE, SelectionMode.SINGLE);

    public static void displayOptions() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(sJota.getStage());

        alert.setTitle(Dict.OPTIONS.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.getButtonTypes().setAll(ButtonType.CLOSE);

        var optionsPane = new OptionsPane();
        var dialogPane = alert.getDialogPane();

        dialogPane.setContent(optionsPane);
        dialogPane.getChildren().remove(0);//Remove graphics container in order to remove the spacing
        dialogPane.setPrefWidth(FxHelper.getUIScaled(400));
        FxHelper.removeSceneInitFlicker(dialogPane);

        FxHelper.showAndWait(alert, sJota.getStage());
        optionsPane.save();
    }

    public OptionsPane() {
        createUI();
    }

    private void createUI() {
        setSpacing(FxHelper.getUIScaled(16));
        getChildren().setAll(
                mRsyncChooser,
                mNightModeToggleSwitch
        );

        mNightModeToggleSwitch.setMaxWidth(Double.MAX_VALUE);
        mNightModeToggleSwitch.selectedProperty().bindBidirectional(mOptions.nightModeProperty());

        mRsyncChooser.setEnabledPathExpander(false);
        mRsyncChooser.setPath(mOptions.getRsyncPath());
    }

    private void save() {
        mOptions.setRsyncPath(mRsyncChooser.getPathAsString());
    }
}
