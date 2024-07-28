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
package se.trixon.nbrsync.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.nbrsync.NbRsync;

@ActionID(
        category = "Tools",
        id = "se.trixon.nbrsync.actions.RemoveLocksAction"
)
@ActionRegistration(
        displayName = "#CTL_RemoveLocksAction"
)
@ActionReference(path = "Menu/Tools", position = 50, separatorAfter = 75)
@Messages("CTL_RemoveLocksAction=Remove locks for jobs")
public final class RemoveLocksAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            var locks = NbRsync.getRunningJobsDirectory();
            if (locks.isDirectory()) {
                FileUtils.forceDelete(locks);
                NbMessage.information(Dict.INFORMATION.toString(), NbBundle.getMessage(RemoveLocksAction.class, "locksRemoved"));
            } else {
                NbMessage.warning(Dict.WARNING.toString(), NbBundle.getMessage(RemoveLocksAction.class, "noLocks"));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
