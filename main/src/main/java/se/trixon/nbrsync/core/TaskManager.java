/*
 * Copyright 2024 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.nbrsync.core;

import java.util.ArrayList;
import java.util.List;
import se.trixon.almond.util.Dict;
import se.trixon.nbrsync.core.task.Task;
import se.trixon.nbrsync.ui.editor.BaseEditor;
import se.trixon.nbrsync.ui.editor.TaskEditor;

/**
 *
 * @author Patrik Karlström
 */
public class TaskManager extends BaseManager<Task> {

    private BaseEditor mEditor;

    public static TaskManager getInstance() {
        return Holder.INSTANCE;
    }

    private TaskManager() {
    }

    @Override
    public BaseEditor getEditor() {
        if (mEditor == null) {
            mEditor = new TaskEditor();
        }

        return mEditor;
    }

    @Override
    public String getLabelPlural() {
        return Dict.TASKS.toString();
    }

    @Override
    public String getLabelSingular() {
        return Dict.TASK.toString();
    }

    public List<Task> getTasks(ArrayList<String> taskIds) {
        var tasks = new ArrayList<Task>();

        taskIds.forEach(id -> {
            var task = getById(id);
            if (task != null) {
                tasks.add(task);
            }
        });

        return tasks;
    }

    private static class Holder {

        private static final TaskManager INSTANCE = new TaskManager();
    }
}
