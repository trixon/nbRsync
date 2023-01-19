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
package se.trixon.rsyncfx.boot;

import java.io.IOException;
import org.apache.commons.lang3.RandomStringUtils;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import se.trixon.almond.util.SystemHelper;
import se.trixon.rsyncfx.App;
import se.trixon.rsyncfx.core.StorageManager;
import se.trixon.rsyncfx.core.job.Job;
import se.trixon.rsyncfx.core.task.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Installer extends ModuleInstall {

    static boolean GUI = true;

    @Override
    public void restored() {
        initStorage();
        //Give ArgsProcessor a chance to disable GUI
        SystemHelper.runLaterDelayed(100, () -> {
            if (GUI) {
                App.main(null);
            }
        });
    }

    private void initStorage() {
        try {
            var manager = StorageManager.getInstance();
            manager.load();

            var jobManager = manager.getJobManager();
            var taskManager = manager.getTaskManager();

            var task1 = new Task();
            task1.setName("Task 1");
            task1.setName("Task %d %s".formatted(taskManager.getItems().size(), RandomStringUtils.random(5, true, false)));
            task1.setDescription(RandomStringUtils.random(15, true, true));
            taskManager.getItems().add(task1);

            var job1 = new Job();
            job1.setName("Job %d %s".formatted(jobManager.getItems().size(), RandomStringUtils.random(5, true, false)));
            job1.setDescription(RandomStringUtils.random(15, true, true));
            job1.getTaskIds().add(task1.getId());
            jobManager.getItems().add(job1);

            manager.save();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
