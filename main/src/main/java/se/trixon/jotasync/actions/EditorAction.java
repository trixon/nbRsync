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

@ActionID(
        category = "Tools",
        id = "se.trixon.jotasync.actions.EditorAction"
)
@ActionRegistration(
        displayName = "#CTL_EditorAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 100),
    @ActionReference(path = "Shortcuts", name = "D-J")
})
@Messages("CTL_EditorAction=Job editor")
public final class EditorAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
    }
}
