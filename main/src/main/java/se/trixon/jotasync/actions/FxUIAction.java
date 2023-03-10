/*
 * Copyright 2023 pata.
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
import se.trixon.almond.util.SystemHelper;
import se.trixon.jotasync.ui.App;

@ActionID(
        category = "Tools",
        id = "se.trixon.jotasync.actions.FxUIAction"
)
@ActionRegistration(
        displayName = "#CTL_FxUIAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 0),
    @ActionReference(path = "Shortcuts", name = "DO-F")
})
@Messages("CTL_FxUIAction=FX UI")
public final class FxUIAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        SystemHelper.runLaterDelayed(1, () -> App.main(null));
    }
}
