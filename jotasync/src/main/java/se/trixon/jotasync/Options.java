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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Options extends OptionsBase {

    public static final int DEFAULT_LAUNCHER_MODE = 0;
    public static final String DEFAULT_PATH_RSYNC = "rsync";
    public static final String KEY_LAUNCHER_MODE = "main.mode";
    public static final String KEY_PATH_RSYNC = "path.rsync";

    private static final boolean DEFAULT_UI_NIGHTMODE = false;
    private static final String KEY_UI_NIGHTMODE = "ui.nightmode";
    private final BooleanProperty mNightModeProperty = new SimpleBooleanProperty();

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
        mPreferences = NbPreferences.forModule(getClass());
        init();
    }

    public String getRsyncPath() {
        return mPreferences.get(KEY_PATH_RSYNC, DEFAULT_PATH_RSYNC);
    }

    public boolean isNightMode() {
        return mNightModeProperty.get();
    }

    public BooleanProperty nightModeProperty() {
        return mNightModeProperty;
    }

    public void setNightMode(boolean nightMode) {
        mNightModeProperty.set(nightMode);
    }

    public void setRsyncPath(String value) {
        mPreferences.put(KEY_PATH_RSYNC, value);
    }

    private void init() {
        initListeners();
        mNightModeProperty.set(is(KEY_UI_NIGHTMODE, DEFAULT_UI_NIGHTMODE));
    }

    private void initListeners() {
        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            MaterialIcon.setDefaultColor(isNightMode() ? Color.LIGHTGRAY : Color.BLACK);

            save();
        };

        mNightModeProperty.addListener(changeListener);
    }

    private void save() {
        put(KEY_UI_NIGHTMODE, isNightMode());
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }

}
