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
package se.trixon.rsyncfx.ui.editor;

import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class RunSectionPane extends VBox {

    private final CheckBox mCheckBox;
    private final FileChooserPane mFileChooser;

    public RunSectionPane(String header, boolean addSeparator) {
        super(FxHelper.getUIScaled(8));
        java.lang.String dialogTitle = NbBundle.getMessage(BaseEditor.class, "JobEditor.selectFileToRun");
        javafx.scene.control.SelectionMode selectionMode = SelectionMode.SINGLE;
        se.trixon.almond.util.fx.control.FileChooserPane.ObjectMode objectMode = FileChooserPane.ObjectMode.FILE;
        mFileChooser = new FileChooserPane(dialogTitle, objectMode, selectionMode, header);
        mCheckBox = new CheckBox(Dict.STOP_ON_ERROR.toString());
        mCheckBox.disableProperty().bind(mFileChooser.getCheckBox().selectedProperty().not());
        getChildren().setAll(mFileChooser, mCheckBox);

        if (addSeparator) {
            getChildren().add(new Separator());
        }
    }

    public String getCommand() {
        return mFileChooser.getPathAsString();
    }

    public boolean isActivated() {
        return mFileChooser.getCheckBox().isSelected();
    }

    public boolean isHaltOnError() {
        return mCheckBox.isSelected();
    }

    public void load(boolean active, String command, boolean haltOnError) {
        mFileChooser.getCheckBox().setSelected(active);
        mFileChooser.setPath(command);
        mCheckBox.setSelected(haltOnError);
    }

}
