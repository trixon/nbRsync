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
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.jotasync.core.Rsync;

@ActionID(
        category = "Help",
        id = "se.trixon.jotasync.actions.AboutRsyncAction"
)
@ActionRegistration(
        displayName = "#CTL_AboutRsyncAction"
)
@ActionReference(path = "Menu/Help", position = 1000)
@Messages("CTL_AboutRsyncAction=About rsync")
public final class AboutRsyncAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        NbMessage.information(
                Actions.cutAmpersand(Bundle.CTL_AboutRsyncAction()),
                Rsync.getInfo()
        );
    }
}
