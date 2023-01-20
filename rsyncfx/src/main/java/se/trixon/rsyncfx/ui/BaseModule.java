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
package se.trixon.rsyncfx.ui;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import org.kordamp.ikonli.Ikon;
import se.trixon.rsyncfx.Options;
import se.trixon.rsyncfx.core.JobManager;
import se.trixon.rsyncfx.core.StorageManager;
import se.trixon.rsyncfx.core.TaskManager;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BaseModule extends WorkbenchModule {

    public static final int ICON_SIZE_MODULE = 32;
    public static final int ICON_SIZE_PROFILE = 24;
    protected Font mDefaultFont = Font.getDefault();
    protected final JobManager mJobManager = JobManager.getInstance();
    protected final Options mOptions = Options.getInstance();
    protected final StorageManager mStorageManager = StorageManager.getInstance();
    protected final TaskManager mTaskManager = TaskManager.getInstance();

    public BaseModule(String name, Ikon icon) {
        super(name, icon);
        init();
    }

    public BaseModule(String name, Image icon) {
        super(name, icon);
        init();
    }

    public void postInit() {
        updateNightMode(mOptions.isNightMode());
    }

    public void updateNightMode(boolean n) {
    }

    private void init() {
        initListeners();
    }

    private void initListeners() {
        mOptions.nightModeProperty().addListener((p, o, n) -> updateNightMode(n));
    }

}
