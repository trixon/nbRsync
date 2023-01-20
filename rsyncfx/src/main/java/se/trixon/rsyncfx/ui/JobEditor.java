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

import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.Dict;
import se.trixon.rsyncfx.core.JobManager;
import se.trixon.rsyncfx.core.job.Job;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class JobEditor extends BaseEditor<Job> {

    private Job mItem;
    private final JobManager mManager = JobManager.getInstance();

    public JobEditor() {
        createUI();
    }

    @Override
    public void load(Job item) {
        if (item == null) {
            item = new Job();
        }
        super.load(item);
        mItem = item;
    }

    @Override
    public Job save() {
        var map = mManager.getIdToItem();
        map.putIfAbsent(mItem.getId(), mItem);

        return super.save();
    }

    private void createUI() {
        var runPane = new VBox();
        var runTab = new Tab(Dict.RUN.toString(), runPane);

        var logPane = new GridPane();
        var logTab = new Tab(Dict.LOGGING.toString(), logPane);

        getTabPane().getTabs().addAll(runTab, logTab);
    }

}
