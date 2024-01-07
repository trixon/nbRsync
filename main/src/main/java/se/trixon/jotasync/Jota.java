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
package se.trixon.jotasync;

import java.io.IOException;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import se.trixon.almond.nbp.output.OutputHelper;
import se.trixon.almond.nbp.output.OutputLineMode;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jotasync.core.Rsync;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Jota {

    public static final String GSC_EDITOR = "key.editor";
    private final GlobalState mGlobalState = new GlobalState();

    public static void displaySystemInformation() {
        var io = IOProvider.getDefault().getIO(Dict.INFORMATION.toString(), false);
        var outputHelper = new OutputHelper(Dict.INFORMATION.toString(), io, false);

        io.select();
        try (var out = io.getOut()) {
            out.reset();
            outputHelper.println(OutputLineMode.ALERT, SystemHelper.getSystemInfo());
            out.println();
            outputHelper.println(OutputLineMode.ALERT, Rsync.getInfo());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static Jota getInstance() {
        return Holder.INSTANCE;
    }

    private Jota() {
    }

    public GlobalState getGlobalState() {
        return mGlobalState;
    }

    private static class Holder {

        private static final Jota INSTANCE = new Jota();
    }
}
