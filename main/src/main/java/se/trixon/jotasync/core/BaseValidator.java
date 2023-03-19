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
package se.trixon.jotasync.core;

import java.io.File;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.jotasync.ui.editor.BaseEditor;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BaseValidator {

    protected ResourceBundle mBundle = NbBundle.getBundle(BaseEditor.class);

    protected boolean mInvalid = false;

    public abstract void addSummary(String header, String message);

    public abstract String getSummary();

    public abstract String getSummaryAsHtml();

    public abstract boolean isValid();

    public void validateExecutorItem(ExecuteItem item, String key) {
        validateFile(item.isEnabled(), item.getCommand(), key);
    }

    public void validateFile(boolean active, String command, String key) {
        var file = new File(command);
        if (active && !file.isFile()) {
            mInvalid = true;
            addSummary(mBundle.getString(key), String.format("%s: %s", Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), command));
        }
    }
}
