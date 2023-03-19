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

import javafx.scene.Node;
import javafx.scene.text.Font;
import se.trixon.jotasync.Options;
import se.trixon.jotasync.core.JobManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class LauncherViewBase {

    protected final Font mDefaultFont = Font.getDefault();
    protected final JobManager mJobManager = JobManager.getInstance();
    protected final Options mOptions = Options.getInstance();
    protected SummaryBuilder mSummaryBuilder;

    public LauncherViewBase() {
    }

    public abstract Node getNode();
}
