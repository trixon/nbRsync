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
package se.trixon.jotasync.boot;

import java.io.IOException;
import org.apache.commons.lang3.RandomStringUtils;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jotasync.core.StorageManager;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Installer extends ModuleInstall {

    static boolean GUI = true;
    private final StorageManager mStorageManager = StorageManager.getInstance();

    @Override
    public void restored() {
        try {
            mStorageManager.load();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        //initStorage();
        //Give ArgsProcessor a chance to disable GUI
        SystemHelper.runLaterDelayed(100, () -> {
            if (GUI) {
                //App.main(null);
            }
        });
    }

    private void initStorage() {
        var jobManager = mStorageManager.getJobManager();
        var taskManager = mStorageManager.getTaskManager();

        var task = new Task();
        task.setName("Task %d %s".formatted(taskManager.getIdToItem().size(), RandomStringUtils.random(5, true, false)));
        task.setDescription(RandomStringUtils.random(15, true, true));
        taskManager.getIdToItem().put(task.getId(), task);

        var job = new Job();
        job.setName("Job %d %s".formatted(jobManager.getIdToItem().size(), RandomStringUtils.random(5, true, false)));
        job.setDescription(RandomStringUtils.random(15, true, true));
        job.getTaskIds().add(task.getId());
        jobManager.getIdToItem().put(job.getId(), job);

        mStorageManager.save();
    }

}
