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
import java.util.List;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import se.trixon.nbrsync.Options;

@ActionID(
        category = "Help",
        id = "se.trixon.nbrsync.actions.ManRsyncAction"
)
@ActionRegistration(
        displayName = "#CTL_ManRsyncAction"
)
@ActionReference(path = "Menu/Help", position = 410)
@Messages("CTL_ManRsyncAction=man rsync")
public final class ManRsyncAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            runProcess(List.of("man", Options.getInstance().getRsyncPath()));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void runProcess(List<String> command) throws IOException {
        var processBuilder = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
        processBuilder.setExecutable(command.getFirst());
        if (command.size() > 1) {
            processBuilder.setArguments(command.subList(1, command.size()));
        }

        var inputOutput = IOProvider.getDefault().getIO("man rsync", false);
        inputOutput.getOut().reset();
        var descriptor = new ExecutionDescriptor()
                .frontWindow(true)
                .inputOutput(inputOutput)
                .noReset(true)
                .errLineBased(true)
                .outLineBased(true)
                .showProgress(false);

        var service = ExecutionService.newService(
                processBuilder,
                descriptor,
                "man rsync");

        service.run();
    }
}
