/*
 * Copyright 2024 pata.
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
package se.trixon.nbrsync.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.nbrsync.core.Server;

@ActionID(
        category = "Tools",
        id = "se.trixon.nbrsync.actions.StopServerAction"
)
@ActionRegistration(
        displayName = "#CTL_StopServerAction"
)
@ActionReference(path = "Menu/Tools", position = 0)
@Messages("CTL_StopServerAction=Stop server")
public final class StopServerAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Server.getInstance().isServerLocked()) {
            Server.getInstance().stopFromGui();
        } else {
            NbMessage.error(Dict.Dialog.ERROR.toString(), "nbRsync server is not running.");
        }
    }
}
