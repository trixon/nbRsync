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
package se.trixon.jotasync.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.FileHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.Options;
import se.trixon.jotasync.core.job.Job;
import se.trixon.jotasync.core.task.Task;
import se.trixon.jotasync.ui.editor.BaseEditor;

/**
 *
 * @author Patrik Karlström
 */
public class JobExecutor {

    private final ResourceBundle mBundle = NbBundle.getBundle(BaseEditor.class);
    private Process mCurrentProcess;
    private boolean mDryRun;
    private String mDryRunIndicator = "";
    private final InputOutput mInputOutput;
    private final Job mJob;
    private long mLastRun;
    private int mNumOfFailedTasks;
    private Options mOptions = Options.getInstance();
    private final StorageManager mStorageManager = StorageManager.getInstance();
    private boolean mTaskFailed;

    public JobExecutor(Job job, boolean dryRun) {
        mJob = job;
        mDryRun = dryRun;
        mInputOutput = IOProvider.getDefault().getIO(mJob.getName(), false);

        if (mDryRun) {
            mDryRunIndicator = String.format(" (%s)", Dict.DRY_RUN.toString());
        }

        try {
            mInputOutput.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Integer execute() {
        mLastRun = System.currentTimeMillis();

        appendHistoryFile(getHistoryLine(mJob.getId(), Dict.STARTED.toString(), mDryRunIndicator));
        String s = String.format("%s %s: '%s'='%s'", Jota.nowToDateTime(), Dict.START.toString(), Dict.JOB.toString(), mJob.getName());
        mInputOutput.getOut().println(s);

        var arg = "--dry-run --archive --delete --verbose --human-readable -P --update --exclude=**/lost+found*/ /home /mnt/atlas/backup/fedora/";
//        var arg = "--version";
        var args = Arrays.asList(StringUtils.splitPreserveAllTokens(arg));
        var processBuilder = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
        processBuilder.setExecutable(mOptions.getRsyncPath());
        processBuilder.setArguments(args);

        var descriptor = new ExecutionDescriptor()
                .controllable(true)
                .frontWindow(true)
                .inputOutput(mInputOutput)
                .noReset(true)
                .preExecution(() -> {
                    mInputOutput.getErr().println("PRE");
                })
                .postExecution((Integer t) -> {
                    mInputOutput.getErr().println("PPOST " + t);
                })
                .showProgress(true);

        var service = ExecutionService.newService(
                processBuilder,
                descriptor,
                mJob.getName());

        var task = service.run();

        try {
            return task.get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return -1;
    }

    private void xec() {
    }

    public void run() {
        var jobExecuteSection = mJob.getExecuteSection();
        String s;
        try {
            // run before first task
            run(jobExecuteSection.getBefore(), "JobEditor.runBefore");

            runTasks();

            if (mNumOfFailedTasks == 0) {
                // run after last task - if all ok
                run(jobExecuteSection.getAfterOk(), "JobEditor.runAfterOk");
            } else {
                s = String.format(Dict.TASKS_FAILED.toString(), mNumOfFailedTasks);
                mInputOutput.getErr().println(s);

                // run after last task - if any failed
                run(jobExecuteSection.getAfterFail(), "JobEditor.runAfterFail");
            }

            // run after last task
            run(jobExecuteSection.getAfter(), "JobEditor.runAfter");

            Thread.sleep(500);

            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.DONE.toString(), mDryRunIndicator));
            s = String.format("%s %s: %s", Jota.nowToDateTime(), Dict.DONE.toString(), Dict.JOB.toString());
            mInputOutput.getOut().println(s);
            updateJobStatus(0);
            writelogs();
            mInputOutput.getOut().println(String.format(Dict.JOB_FINISHED.toString(), mJob.getName()));
        } catch (InterruptedException ex) {
            mCurrentProcess.destroy();
            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.CANCELED.toString(), mDryRunIndicator));
            updateJobStatus(99);
            writelogs();
            //mProcessCallbacks.onProcessEvent(ProcessEvent.CANCELED, mJob, null, null);
        } catch (IOException ex) {
            writelogs();
            Exceptions.printStackTrace(ex);
        } catch (ExecutionFailedException ex) {
            //Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            //send(ProcessEvent.OUT, "before failed and will not continue");
            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.FAILED.toString(), mDryRunIndicator));
            updateJobStatus(1);
            writelogs();
            mInputOutput.getErr().println(String.format("\n\n%s", Dict.JOB_FAILED.toString()));
        }

        ExecutorManager.getInstance().getJobExecutors().remove(mJob.getId());
    }

    private void appendHistoryFile(String string) {
        try {
            FileUtils.write(mStorageManager.getHistoryFile(), string, Charset.defaultCharset(), true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String getHistoryLine(String id, String status, String dryRunIndicator) {
        return String.format("%s %s %s%s\n", id, Jota.nowToDateTime(), status, dryRunIndicator);
    }

    private String getRsyncErrorCode(int exitValue) {
//        var bundle = NbBundle.getBundle("ExitValues");
        ResourceBundle bundle = SystemHelper.getBundle(getClass(), "ExitValues");
        String key = String.valueOf(exitValue);

        return bundle.containsKey(key) ? bundle.getString(key) : String.format((Dict.SYSTEM_CODE.toString()), key);
    }

    private boolean run(String command, boolean stopOnError, String description) throws IOException, InterruptedException, ExecutionFailedException {
        String s = String.format("%s %s: '%s'='%s'", Jota.nowToDateTime(), Dict.START.toString(), description, command);
        mInputOutput.getOut().println(s);
        boolean success = false;

        if (new File(command).isFile()) {
            var commandLine = new ArrayList<String>();
            commandLine.add(command);
            runProcess(commandLine);

            Thread.sleep(100);

            String status;
            if (mCurrentProcess.exitValue() == 0) {
                status = Dict.DONE.toString();
                success = true;
            } else {
                status = Dict.Dialog.ERROR.toString();
            }
            s = String.format("%s %s: '%s'", Jota.nowToDateTime(), status, description);
            mInputOutput.getOut().println(s);

            if (stopOnError && mCurrentProcess.exitValue() != 0) {
                String string = String.format("%s: exitValue=%d", Dict.FAILED.toString(), mCurrentProcess.exitValue());
                throw new ExecutionFailedException(string);
            }
        } else {
            s = String.format("%s: %s", Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), command);
            if (stopOnError) {
                throw new ExecutionFailedException(s);
            } else {
                mInputOutput.getErr().println(s);
            }
        }

        return success;
    }

    private void run(ExecuteItem executeItem, String key) throws IOException, InterruptedException, ExecutionFailedException {
        var command = executeItem.getCommand();
        if (executeItem.isEnabled() && StringUtils.isNotEmpty(command)) {
            run(command, executeItem.isHaltOnError(), mBundle.getString(key));
        }
    }

    private void runProcess(List<String> command) throws IOException, InterruptedException {
        var processBuilder = new ProcessBuilder(command);
        mCurrentProcess = processBuilder.start();

//        new ProcessLogThread(mCurrentProcess.getInputStream(), ProcessEvent.OUT).start();
//        new ProcessLogThread(mCurrentProcess.getErrorStream(), ProcessEvent.ERR).start();
        mCurrentProcess.waitFor();
    }

    private int runRsync(Task task) throws InterruptedException {
        try {
            var command = new ArrayList<String>();
            command.add(mOptions.getRsyncPath());
            if (mDryRun) {
                command.add("--dry-run");
            }
            command.addAll(task.getCommand());
            String s = String.format("%s %s: rsync\n\n%s\n", Jota.nowToDateTime(), Dict.START.toString(), StringUtils.join(command, " "));
            mInputOutput.getOut().println(s);

            runProcess(command);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return 9999;
        }

        return mCurrentProcess.exitValue();
    }

    private boolean runTask(Task task) throws InterruptedException {
        if (mDryRun || task.isDryRun()) {
            mDryRunIndicator = String.format(" (%s)", Dict.DRY_RUN.toString());
        }

        appendHistoryFile(getHistoryLine(task.getId(), Dict.STARTED.toString(), mDryRunIndicator));

        String s = String.format("%s %s: %s='%s'", Jota.nowToDateTime(), Dict.START.toString(), Dict.TASK.toString(), task.getName());
        mInputOutput.getOut().println(s);
        mTaskFailed = false;
        var taskExecuteSection = task.getExecuteSection();

        boolean doNextStep = runTaskStep(taskExecuteSection.getBefore(), "TaskEditor.runBefore");

        if (doNextStep) {
            int exitValue = runRsync(task);
            boolean rsyncSuccess = exitValue == 0;
            s = String.format("%s %s: rsync (%s)", Jota.nowToDateTime(), Dict.DONE.toString(), getRsyncErrorCode(exitValue));
            mInputOutput.getOut().println(s);

            if (rsyncSuccess) {
                doNextStep = runTaskStep(taskExecuteSection.getAfterOk(), "TaskEditor.runAfterOk");
            } else {
                doNextStep = runTaskStep(taskExecuteSection.getAfterFail(), "TaskEditor.runAfterFail");
            }
        }

        if (doNextStep) {
            runTaskStep(taskExecuteSection.getAfterOk(), "TaskEditor.runAfter");
        }

        if (mTaskFailed) {
            mNumOfFailedTasks++;
        }

        appendHistoryFile(getHistoryLine(task.getId(), Dict.DONE.toString(), mDryRunIndicator));

        s = String.format("%s %s: %s", Jota.nowToDateTime(), Dict.DONE.toString(), Dict.TASK.toString());
        mInputOutput.getOut().println(s);

        boolean doNextTask = !(mTaskFailed && taskExecuteSection.isJobHaltOnError());

        return doNextTask;
    }

    private boolean runTaskStep(ExecuteItem executeItem, String key) throws InterruptedException {
        boolean doNextStep = false;

        try {
            var command = executeItem.getCommand();
            if (executeItem.isEnabled() && StringUtils.isNotEmpty(command)) {
                if (!run(command, executeItem.isHaltOnError(), mBundle.getString(key))) {
                    mTaskFailed = true;
                }
            }
            doNextStep = true;
        } catch (IOException | ExecutionFailedException ex) {
            mTaskFailed = true;
            Exceptions.printStackTrace(ex);
        }

        return doNextStep;
    }

    private void runTasks() throws InterruptedException {
        for (var task : mJob.getTasks()) {
            if (!runTask(task)) {
                break;
            }
        }
    }

    private void updateJobStatus(int exitCode) {
        var job = mStorageManager.getJobManager().getById(mJob.getId());
        job.setLastRun(mLastRun);
        job.setLastRunExitCode(exitCode);

        StorageManager.save();
    }

    private void writelogs() {
        File directory = mStorageManager.getLogFile();
        String jobName = FileHelper.replaceInvalidChars(mJob.getName());
        String outFile = String.format("%s.log", jobName);
        String errFile = String.format("%s.err", jobName);

        int logMode = mJob.getLogMode();
        if (logMode == 2) {
            outFile = String.format("%s %s.log", jobName, mJob.getLastRunDateTime("", mLastRun));
            errFile = String.format("%s %s.err", jobName, mJob.getLastRunDateTime("", mLastRun));
        }

        boolean append = logMode == 0;

        try {
            FileUtils.forceMkdir(directory);
            File file = new File(directory, outFile);

            StringBuilder builder = new StringBuilder();
            if (mJob.isLogOutput() || mJob.isLogErrors() && !mJob.isLogSeparateErrors()) {
//                FileUtils.writeStringToFile(file, mOutBuffer.toString(), "utf-8", append);
                String message = file.getAbsolutePath();
                Xlog.timedOut(message);
                builder.append(String.format("%s:%s", SystemHelper.getHostname(), message));
            }

            if (mJob.isLogErrors() && mJob.isLogSeparateErrors()) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                file = new File(directory, errFile);
//                FileUtils.writeStringToFile(file, mErrBuffer.toString(), "utf-8", append);
                String message = file.getAbsolutePath();
                Xlog.timedOut(message);
                builder.append(String.format("%s:%s", SystemHelper.getHostname(), message));
            }

            if (builder.length() > 0) {
                builder.insert(0, String.format("%s\n", Dict.SAVE_LOG.toString()));
//                send(ProcessEvent.OUT, builder.toString());
            }
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }
    }

    class ExecutionFailedException extends Exception {

        public ExecutionFailedException() {
            super();
        }

        public ExecutionFailedException(String message) {
            super(message);
            mInputOutput.getErr().println(message);
        }

        public ExecutionFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        public ExecutionFailedException(Throwable cause) {
            super(cause);
        }

        public ExecutionFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
