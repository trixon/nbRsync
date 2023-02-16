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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
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
class JobExecutor extends Thread {

    private Process mCurrentProcess;
    private boolean mDryRun;
    private final StringBuffer mErrBuffer;
    private final Job mJob;
    private final StorageManager mJotaManager = StorageManager.getInstance();
    private long mLastRun;
    private int mNumOfFailedTasks;
    private Options mOptions = Options.getInstance();
    private final StringBuffer mOutBuffer;
    private boolean mTaskFailed;
    private final ResourceBundle mBundle = NbBundle.getBundle(BaseEditor.class);

    JobExecutor(Job job, boolean dryRun) {
        mJob = job;
        mDryRun = dryRun;

        mErrBuffer = new StringBuffer();
        mOutBuffer = new StringBuffer();
    }

    @Override
    public void run() {
        mLastRun = System.currentTimeMillis();
        String dryRunIndicator = "";
        if (mDryRun) {
            dryRunIndicator = String.format(" (%s)", Dict.DRY_RUN.toString());
        }

        appendHistoryFile(getHistoryLine(mJob.getId(), Dict.STARTED.toString(), dryRunIndicator));
        String s = String.format("%s %s: '%s'='%s'", Jota.nowToDateTime(), Dict.START.toString(), Dict.JOB.toString(), mJob.getName());
        mOutBuffer.append(s).append("\n");
        send(ProcessEvent.OUT, s);
        var jobExecuteSection = mJob.getExecuteSection();

        try {
            // run before first task
            run(jobExecuteSection.getBefore(), "JobEditor.runBefore");

            runTasks();

            if (mNumOfFailedTasks == 0) {
                // run after last task - if all ok
                run(jobExecuteSection.getAfterOk(), "JobEditor.runAfterOk");
            } else {
                s = String.format(Dict.TASKS_FAILED.toString(), mNumOfFailedTasks);
                mOutBuffer.append(s).append("\n");
                send(ProcessEvent.OUT, s);

                // run after last task - if any failed
                run(jobExecuteSection.getAfterFail(), "JobEditor.runAfterFail");
            }

            // run after last task
            run(jobExecuteSection.getAfter(), "JobEditor.runAfter");

            Thread.sleep(500);

            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.DONE.toString(), dryRunIndicator));
            s = String.format("%s %s: %s", Jota.nowToDateTime(), Dict.DONE.toString(), Dict.JOB.toString());
            mOutBuffer.append(s).append("\n");
            updateJobStatus(0);
            writelogs();
            send(ProcessEvent.FINISHED, s);
            Xlog.timedOut(String.format(Dict.JOB_FINISHED.toString(), mJob.getName()));
        } catch (InterruptedException ex) {
            mCurrentProcess.destroy();
            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.CANCELED.toString(), dryRunIndicator));
            updateJobStatus(99);
            writelogs();
//            mServer.getClientCallbacks().stream().forEach((clientCallback) -> {
//                try {
//                    clientCallback.onProcessEvent(ProcessEvent.CANCELED, mJob, null, null);
//                } catch (RemoteException ex1) {
//                    // nvm Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
//                }
//            });
        } catch (IOException ex) {
            writelogs();
            Exceptions.printStackTrace(ex);
        } catch (ExecutionFailedException ex) {
            //Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            //send(ProcessEvent.OUT, "before failed and will not continue");
            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.FAILED.toString(), dryRunIndicator));
            updateJobStatus(1);
            writelogs();
            send(ProcessEvent.FAILED, String.format("\n\n%s", Dict.JOB_FAILED.toString()));
        }

        ExecutorManager.getInstance().getJobExecutors().remove(mJob.getId());
    }

    public void stopJob() {
        mCurrentProcess.destroy();
        interrupt();
    }

    private void appendHistoryFile(String string) {
        try {
            FileUtils.write(mJotaManager.getHistoryFile(), string, Charset.defaultCharset(), true);
        } catch (IOException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getHistoryLine(String id, String status, String dryRunIndicator) {
        return String.format("%d %s %s%s\n", id, Jota.nowToDateTime(), status, dryRunIndicator);
    }

    private String getRsyncErrorCode(int exitValue) {
        ResourceBundle bundle = SystemHelper.getBundle(getClass(), "ExitValues");
        String key = String.valueOf(exitValue);

        return bundle.containsKey(key) ? bundle.getString(key) : String.format((Dict.SYSTEM_CODE.toString()), key);
    }

    private boolean run(String command, boolean stopOnError, String description) throws IOException, InterruptedException, ExecutionFailedException {
        //String s = String.format("%s %s: '%s'='%s' ('%s'=%s)", Jota.nowToDateTime(), Dict.START.toString(), description, command, Dict.STOP_ON_ERROR.toString(), StringHelper.booleanToYesNo(stopOnError));
        String s = String.format("%s %s: '%s'='%s'", Jota.nowToDateTime(), Dict.START.toString(), description, command);
        mOutBuffer.append(s).append("\n");
        send(ProcessEvent.OUT, s);
        boolean success = false;

        if (new File(command).exists()) {
            ArrayList<String> commandLine = new ArrayList<>();
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
            mOutBuffer.append(s).append("\n");
            send(ProcessEvent.OUT, s);

            if (stopOnError && mCurrentProcess.exitValue() != 0) {
                String string = String.format("%s: exitValue=%d", Dict.FAILED.toString(), mCurrentProcess.exitValue());
                throw new ExecutionFailedException(string);
            }
        } else {
            s = String.format("%s: %s", Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), command);
            if (stopOnError) {
                throw new ExecutionFailedException(s);
            } else {
                mOutBuffer.append(s).append("\n");
                send(ProcessEvent.ERR, s);
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
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        mCurrentProcess = processBuilder.start();

        new ProcessLogThread(mCurrentProcess.getInputStream(), ProcessEvent.OUT).start();
        new ProcessLogThread(mCurrentProcess.getErrorStream(), ProcessEvent.ERR).start();

        mCurrentProcess.waitFor();
    }

    private int runRsync(Task task) throws InterruptedException {
        try {
            ArrayList<String> command = new ArrayList<>();
            command.add(mOptions.getRsyncPath());
            if (mDryRun) {
                command.add("--dry-run");
            }
            command.addAll(task.getCommand());
            String s = String.format("%s %s: rsync\n\n%s\n", Jota.nowToDateTime(), Dict.START.toString(), StringUtils.join(command, " "));
            mOutBuffer.append(s).append("\n");
            send(ProcessEvent.OUT, s);

            runProcess(command);

            Thread.sleep(500);
            send(ProcessEvent.OUT, "");
        } catch (IOException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            return 9999;
        }

        return mCurrentProcess.exitValue();
    }

    private boolean runTask(Task task) throws InterruptedException {
        String dryRunIndicator = "";
        if (mDryRun || task.isDryRun()) {
            dryRunIndicator = String.format(" (%s)", Dict.DRY_RUN.toString());
        }
        appendHistoryFile(getHistoryLine(task.getId(), Dict.STARTED.toString(), dryRunIndicator));

        String s = String.format("%s %s: %s='%s'", Jota.nowToDateTime(), Dict.START.toString(), Dict.TASK.toString(), task.getName());
        send(ProcessEvent.OUT, s);
        mTaskFailed = false;
        var taskExecuteSection = task.getExecuteSection();

        // run before
        boolean doNextStep = runTaskStep(taskExecuteSection.getBefore(), "TaskEditor.runBefore");

        // run rsync
        if (doNextStep) {
            int exitValue = runRsync(task);
            boolean rsyncSuccess = exitValue == 0;
            s = String.format("%s %s: rsync (%s)", Jota.nowToDateTime(), Dict.DONE.toString(), getRsyncErrorCode(exitValue));
            mOutBuffer.append(s).append("\n");
            send(ProcessEvent.OUT, s);
            if (rsyncSuccess) {
                // run after success
                doNextStep = runTaskStep(taskExecuteSection.getAfterOk(), "TaskEditor.runAfterOk");

//                command = taskExecuteSection.getAfterSuccessCommand();
//                if (taskExecuteSection.isAfterSuccess() && StringUtils.isNoneEmpty(command)) {
//                    doNextStep = runTaskStep(command, taskExecuteSection.isAfterSuccessHaltOnError(), mBundle.getString("TaskExecutePanel.afterSuccessPanel.header"));
//                }
            } else {
                // run after failure
                doNextStep = runTaskStep(taskExecuteSection.getAfterFail(), "TaskEditor.runAfterFail");
//                command = taskExecuteSection.getAfterFailureCommand();
//                if (taskExecuteSection.isAfterFailure() && StringUtils.isNoneEmpty(command)) {
//                    doNextStep = runTaskStep(command, taskExecuteSection.isAfterFailureHaltOnError(), mBundle.getString("TaskExecutePanel.afterFailurePanel.header"));
//                }
            }
        }

        if (doNextStep) {
            // run after
            runTaskStep(taskExecuteSection.getAfterOk(), "TaskEditor.runAfter");
//            command = taskExecuteSection.getAfterCommand();
//            if (taskExecuteSection.isAfter() && StringUtils.isNoneEmpty(command)) {
//                runTaskStep(command, taskExecuteSection.isAfterHaltOnError(), mBundle.getString("TaskExecutePanel.afterPanel.header"));
//            }
        }

        if (mTaskFailed) {
            mNumOfFailedTasks++;
        }

        appendHistoryFile(getHistoryLine(task.getId(), Dict.DONE.toString(), dryRunIndicator));

        s = String.format("%s %s: %s", Jota.nowToDateTime(), Dict.DONE.toString(), Dict.TASK.toString());
        send(ProcessEvent.OUT, s);

        boolean doNextTask = !(mTaskFailed && taskExecuteSection.isJobHaltOnError());
        return doNextTask;
    }

    private boolean runTaskStep(ExecuteItem executeItem, String key) throws InterruptedException {
//        var command = executeItem.getCommand();
//        if (executeItem.isEnabled() && StringUtils.isNotEmpty(command)) {
//            run(command, executeItem.isHaltOnError(), mBundle.getString(key));
//        }
//    }
//    private boolean runTaskStep(String command, boolean stopOnError, String description) throws InterruptedException {
        boolean doNextStep = false;

        try {
            if (run(executeItem.getCommand(), executeItem.isHaltOnError(), mBundle.getString(key))) {
            } else {
                mTaskFailed = true;
            }
            doNextStep = true;
        } catch (IOException | ExecutionFailedException ex) {
            mTaskFailed = true;
            Exceptions.printStackTrace(ex);
        }

        return doNextStep;
    }

    private void runTasks() throws InterruptedException {
        for (Task task : mJob.getTasks()) {
            if (!runTask(task)) {
                break;
            }
        }
    }

    private synchronized void send(ProcessEvent processEvent, String line) {
        System.out.println(processEvent + ": " + line);
//        mServer.getClientCallbacks().stream().forEach((clientCallback) -> {
//            try {
//                clientCallback.onProcessEvent(processEvent, mJob, null, line);
//            } catch (RemoteException ex) {
//                // nvm Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        });
    }

    private void updateJobStatus(int exitCode) {
        var job = mJotaManager.getJobManager().getById(mJob.getId());
        job.setLastRun(mLastRun);
        job.setLastRunExitCode(exitCode);

        StorageManager.save();
    }

    private void writelogs() {
        File directory = StorageManager.getInstance().getLogFile();
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
            send(ProcessEvent.OUT, "");

            StringBuilder builder = new StringBuilder();
            if (mJob.isLogOutput() || mJob.isLogErrors() && !mJob.isLogSeparateErrors()) {
                FileUtils.writeStringToFile(file, mOutBuffer.toString(), "utf-8", append);
                String message = file.getAbsolutePath();
                Xlog.timedOut(message);
                builder.append(String.format("%s:%s", SystemHelper.getHostname(), message));
            }

            if (mJob.isLogErrors() && mJob.isLogSeparateErrors()) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                file = new File(directory, errFile);
                FileUtils.writeStringToFile(file, mErrBuffer.toString(), "utf-8", append);
                String message = file.getAbsolutePath();
                Xlog.timedOut(message);
                builder.append(String.format("%s:%s", SystemHelper.getHostname(), message));
            }

            if (builder.length() > 0) {
                builder.insert(0, String.format("%s\n", Dict.SAVE_LOG.toString()));
                send(ProcessEvent.OUT, builder.toString());
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
            mOutBuffer.append(message).append("\n");
            send(ProcessEvent.OUT, message);
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

    class ProcessLogThread extends Thread {

        private final InputStream mInputStream;
        private final ProcessEvent mProcessEvent;
        private String mDateTimePrefix = "";

        public ProcessLogThread(InputStream inputStream, ProcessEvent processEvent) {
            mInputStream = inputStream;
            mProcessEvent = processEvent;
            if (mJob.getLogMode() == 0) {
                mDateTimePrefix = Jota.millisToDateTime(mLastRun) + " ";
            }
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream), 1);
                String line;

                while ((line = reader.readLine()) != null) {
                    String string = String.format("%s%s%s", mDateTimePrefix, line, System.lineSeparator());
                    if (mJob.isLogSeparateErrors()) {
                        if (mJob.isLogOutput() && mProcessEvent == ProcessEvent.OUT) {
                            mOutBuffer.append(string);
                        } else if (mJob.isLogErrors() && mProcessEvent == ProcessEvent.ERR) {
                            mErrBuffer.append(string);
                        }
                    } else if (!mJob.isLogSeparateErrors()) {
                        if (mJob.isLogOutput() && mProcessEvent == ProcessEvent.OUT) {
                            mOutBuffer.append(string);
                        } else if (mJob.isLogErrors() && mProcessEvent == ProcessEvent.ERR) {
                            mOutBuffer.append(string);
                        }
                    }

                    //mOutBuffer.append(line).append("\n");
                    send(mProcessEvent, line);
                }
            } catch (IOException e) {
                Xlog.timedErr(e.getLocalizedMessage());
            }
        }
    }
}
