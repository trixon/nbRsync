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

import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Options extends OptionsBase {

    public static final int DEFAULT_LAUNCHER_MODE = 0;
    public static final String DEFAULT_PATH_RSYNC = "rsync";
    public static final String KEY_LAUNCHER_MODE = "main.mode";
    public static final String KEY_PATH_RSYNC = "path.rsync";

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
        mPreferences = NbPreferences.forModule(getClass());
    }

    public String getRsyncPath() {
        return mPreferences.get(KEY_PATH_RSYNC, DEFAULT_PATH_RSYNC);
    }

    public void setRsyncPath(String value) {
        mPreferences.put(KEY_PATH_RSYNC, value);
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }

}
