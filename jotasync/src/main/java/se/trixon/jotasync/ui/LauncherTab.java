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
package se.trixon.jotasync.ui;

import javafx.application.Platform;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.Options;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class LauncherTab extends BaseTab {

    private LauncherGridView mLauncherGridView;
    private LauncherListView mLauncherListView;
    private final Options mOptions = Options.getInstance();

    public LauncherTab() {
        createUI();

        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            if (pce.getKey().equalsIgnoreCase(Options.KEY_LAUNCHER_MODE)) {
                Platform.runLater(() -> updateLauncherMode());
            }
        });

        updateLauncherMode();
        updateNightMode();
    }

    @Override
    public void updateNightMode() {
        setGraphic(MaterialIcon._Action.HOME.getImageView(Jota.getIconSizeTab()));

    }

    private void createUI() {
        setClosable(false);
        mLauncherGridView = new LauncherGridView();
        mLauncherListView = new LauncherListView();
    }

    private void updateLauncherMode() {
        if (mOptions.getInt(Options.KEY_LAUNCHER_MODE, Options.DEFAULT_LAUNCHER_MODE) == 0) {
            setContent(mLauncherGridView.getNode());
        } else {
            setContent(mLauncherListView.getNode());
        }
    }
}
