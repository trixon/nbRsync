/*
 * Copyright 2023 Patrik Karlström.
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
package se.trixon.jotasync.core;

import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public interface ProcessCallbacks {

    void onProcessEvent(ProcessEvent processEvent, Job job, Task task, Object object);

}
