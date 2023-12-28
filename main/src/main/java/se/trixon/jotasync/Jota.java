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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.controlsfx.control.StatusBar;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.ExecutionFlow;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.jotasync.core.Rsync;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Jota {

    public static final String GSC_EDITOR = "key.editor";
    private static final int ICON_SIZE_TOOLBAR = 32;
    private static StatusBar mStatusBar;
    private static final DateTimeFormatter sDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss");
    private final ExecutionFlow mExecutionFlow = new ExecutionFlow();
    private final GlobalState mGlobalState = new GlobalState();

    public static void displaySystemInformation() {
        var io = IOProvider.getDefault().getIO(Dict.INFORMATION.toString(), false);
        io.select();
        try (var out = io.getOut()) {
            out.reset();
            IOColorLines.println(io, SystemHelper.getSystemInfo(), Colors.alert());
            out.println();
            IOColorLines.println(io, Rsync.getInfo(), Colors.alert());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static int getIconSizeTab() {
        return (int) FxHelper.getUIScaled(ICON_SIZE_TOOLBAR * 1.5);
    }

    public static int getIconSizeToolBar() {
        return FxHelper.getUIScaled(ICON_SIZE_TOOLBAR);
    }

    public static int getIconSizeToolBarInt() {
        return (int) (getIconSizeToolBar() / 1.3);
    }

    public static Jota getInstance() {
        return Holder.INSTANCE;
    }

    public static StatusBar getStatusBar() {
        return mStatusBar;
    }

    public static String millisToDateTime(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(date);
    }

    public static String nowToDateTime() {
        return millisToDateTime(System.currentTimeMillis());
    }

    public static void openOutput() {
        SwingHelper.runLater(() -> IOContainer.getDefault().open());
    }

    public static String prependTimestamp(String s) {
        return "%s %s".formatted(LocalDateTime.now().format(sDateTimeFormatter), s);
    }

    public static void setStatusBar(StatusBar statusBar) {
        mStatusBar = statusBar;
    }

    private Jota() {
    }

    public ExecutionFlow getExecutionFlow() {
        return mExecutionFlow;
    }

    public GlobalState getGlobalState() {
        return mGlobalState;
    }

    private static class Holder {

        private static final Jota INSTANCE = new Jota();
    }
}
