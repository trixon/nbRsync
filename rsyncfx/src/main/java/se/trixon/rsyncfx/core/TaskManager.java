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
package se.trixon.rsyncfx.core;

import java.util.ArrayList;
import java.util.List;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class TaskManager extends BaseManager<Task> {

    public static TaskManager getInstance() {
        return Holder.INSTANCE;
    }

    private TaskManager() {
    }

//    public Task getTaskById(long id) {
//        Task foundTask = null;
//
//        for (var task : mItems) {
//            if (task.getId() == id) {
//                foundTask = task;
//                break;
//            }
//        }
//
//        return foundTask;
//    }
    public List<Task> getTasks(ArrayList<Long> taskIds) {
        var tasks = new ArrayList<Task>();

        taskIds.forEach(id -> {
            var task = getById(id);
            if (task != null) {
                tasks.add(task);
            }
        });

        return tasks;
    }
//
//    public void setItems(ArrayList<Task> items) {
//        mItems.clear();
//        mItems.addAll(items);
//    }
//
//    void loadHistory() {
//        try {
//            mHistoryLines = FileUtils.readLines(StorageManager.getInstance().getHistoryFile(), Charset.defaultCharset());
//            for (Task task : mItems) {
//                loadHistory(task);
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(JobManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    private void loadHistory(Task task) {
//        var builder = new StringBuilder();
//        for (var line : mHistoryLines) {
//            var id = String.valueOf(task.getId());
//            if (StringUtils.contains(line, id)) {
//                builder.append(StringUtils.remove(line, id + " ")).append("\n");
//            }
//        }
//        task.setHistory(builder.toString());
//    }

    private static class Holder {

        private static final TaskManager INSTANCE = new TaskManager();
    }
}
