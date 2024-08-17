/*
 * Copyright 2024 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.nbrsync.ui.editor;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class EnvironmentTab extends Tab {

    private final TextArea mEnvTextArea = new TextArea();

    public EnvironmentTab() {
        super(Dict.ENVIRONMENT_VARIABLES.toString());
        setContent(mEnvTextArea);
        mEnvTextArea.setPromptText(NbBundle.getMessage(EnvironmentTab.class, "hintEnv"));
    }

    public TextArea getEnvTextArea() {
        return mEnvTextArea;
    }

    String getEnv() {
        return mEnvTextArea.getText();
    }

    void setEnvironment(String env) {
        mEnvTextArea.setText(env);
    }

}
