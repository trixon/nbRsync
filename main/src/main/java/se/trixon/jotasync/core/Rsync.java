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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.jotasync.Options;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Rsync {

    public static String getInfo() {
        Callable<String> callable = () -> {
            var command = Options.getInstance().getRsyncPath();
            var processBuilder = new ProcessBuilder(new String[]{command});

            try {
                var process = processBuilder.start();
                var result = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
                process.waitFor();
                return StringUtils.substringBefore(result, "rsync comes with");
            } catch (IOException | InterruptedException ex) {
                return Dict.COMMAND_NOT_FOUND_S.toString().formatted(command);
            }
        };

        try {
            var futureTask = new FutureTask<String>(callable);
            new Thread(futureTask).start();
            return futureTask.get();
        } catch (InterruptedException | ExecutionException ex) {
            return ex.getMessage();
        }
    }
}
