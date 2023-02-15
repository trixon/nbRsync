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

import com.dlsc.gemsfx.util.SessionManager;
import javafx.stage.Stage;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.ExecutionFlow;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.jotasync.ui.Launcher;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Jota {

    public static final String GSC_EDITOR = "key.editor";
    public static final int ICON_SIZE_TOOLBAR = 32;
    private static Stage sStage;
    private final ExecutionFlow mExecutionFlow = new ExecutionFlow();
    private final GlobalState mGlobalState = new GlobalState();
    private final SessionManager mSessionManager = new SessionManager(NbPreferences.forModule(Launcher.class).node("sessionManager"));

    public static int getIconSizeToolBar() {
        return FxHelper.getUIScaled(ICON_SIZE_TOOLBAR);
    }

    public static int getIconSizeToolBarInt() {
        return (int) (getIconSizeToolBar() / 1.3);
    }

    public static Jota getInstance() {
        return Holder.INSTANCE;
    }

    public static Stage getStage() {
        return sStage;
    }

    public static void setStage(Stage stage) {
        sStage = stage;
    }

    private Jota() {
    }

    public ExecutionFlow getExecutionFlow() {
        return mExecutionFlow;
    }

    public GlobalState getGlobalState() {
        return mGlobalState;
    }

    public SessionManager getSessionManager() {
        return mSessionManager;
    }

    private static class Holder {

        private static final Jota INSTANCE = new Jota();
    }
}
