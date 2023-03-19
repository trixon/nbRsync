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
package se.trixon.jotasync.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import se.trixon.jotasync.Options;

@ActionID(
        category = "View",
        id = "se.trixon.jotasync.actions.LauncherModeButtonAction"
)
@ActionRegistration(
        displayName = "#CTL_LauncherModeButtonAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 201),
    @ActionReference(path = "Shortcuts", name = "D-2")
})
@Messages("CTL_LauncherModeButtonAction=Buttons")
public final class LauncherModeButtonAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Options.getInstance().put(Options.KEY_LAUNCHER_MODE, 1);
    }

}
