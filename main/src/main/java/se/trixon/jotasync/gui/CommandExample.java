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
package se.trixon.jotasync.gui;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.base.ProcessBuilder;
import org.openide.util.Exceptions;

public class CommandExample {

    public Integer execute() {
//        var s = "--dry-run --archive --delete --verbose --human-readable -P --update --exclude=**/lost+found*/ /home /mnt/atlas/backup/fedora/";
        var s = "--version";
        var args = Arrays.asList(StringUtils.splitPreserveAllTokens(s));
        var processBuilder = ProcessBuilder.getLocal();
        processBuilder.setExecutable("/bin/rsync");
        processBuilder.setArguments(args);

        var descriptor = new ExecutionDescriptor()
                .controllable(true)
                .frontWindow(true)
                .showProgress(true);

        var service = ExecutionService.newService(
                processBuilder,
                descriptor,
                "rsnyc");

        var task = service.run();

        try {
            return task.get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        return -1;
    }
}
