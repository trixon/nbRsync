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
package se.trixon.rsyncfx.ui.editor;

import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.Dict;
import se.trixon.rsyncfx.core.TaskManager;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskEditor extends BaseEditor<Task> {

    private Task mItem;
    private final TaskManager mManager = TaskManager.getInstance();

    public TaskEditor() {
        createUI();
    }

    @Override
    public void load(Task item) {
        if (item == null) {
            item = new Task();
        }
        super.load(item);
        mItem = item;
    }

    @Override
    public Task save() {
        var map = mManager.getIdToItem();
        map.putIfAbsent(mItem.getId(), mItem);

        return super.save();
    }

    private void createUI() {
        var dirPane = new VBox();
        var dirTab = new Tab(Dict.DIRECTORY.toString(), dirPane);

        var runPane = new VBox();
        var runTab = new Tab(Dict.RUN.toString(), runPane);

        var optionsPane = new VBox();
        var optionsTab = new Tab(Dict.OPTIONS.toString(), optionsPane);

        var excludePane = new VBox();
        var excludeTab = new Tab(Dict.EXCLUDE.toString(), excludePane);

        getTabPane().getTabs().addAll(
                dirTab,
                runTab,
                optionsTab,
                excludeTab
        );
    }

}
