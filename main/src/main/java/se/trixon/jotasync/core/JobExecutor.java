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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.FileHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.almond.util.fx.FxHelper;
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
    private boolean mDryRun;
    private String mDryRunIndicator = "";
    private Thread mExecutorThread;
    private boolean mIndeterminate = true;
    private final InputOutput mInputOutput;
    private boolean mInterrupted;
    private final Job mJob;
    private long mLastRun;
    private int mNumOfFailedTasks;
    private Options mOptions = Options.getInstance();
    private ProgressHandle mProgressHandle;
    private final StatusDisplayer mStatusDisplayer = StatusDisplayer.getDefault();
    private final StorageManager mStorageManager = StorageManager.getInstance();
    private boolean mTaskFailed;

    public JobExecutor(Job job, boolean dryRun) {
        mJob = job;
        mDryRun = dryRun;
        mInputOutput = IOProvider.getDefault().getIO(mJob.getName(), false);
        mInputOutput.select();

        if (mDryRun) {
            mDryRunIndicator = String.format(" (%s)", Dict.DRY_RUN.toString());
        }

        try {
            mInputOutput.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void run() {
        var allowToCancel = (Cancellable) () -> {
            mExecutorThread.interrupt();
            var s = NbBundle.getMessage(JobExecutor.class, "jobCancelled").formatted(mJob.getName());
            mInputOutput.getErr().println(Jota.prependTimestamp(s));
            mInterrupted = true;
            mProgressHandle.finish();
            ExecutorManager.getInstance().getJobExecutors().remove(mJob.getId());
            jobEnded(Dict.CANCELED.toString(), 99);

            return true;
        };

        mInterrupted = false;
        mLastRun = System.currentTimeMillis();
        mProgressHandle = ProgressHandle.createHandle(mJob.getName(), allowToCancel);
        mProgressHandle.start();
        mProgressHandle.switchToIndeterminate();

        mExecutorThread = new Thread(() -> {
            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.STARTED.toString(), mDryRunIndicator));
            mInputOutput.getOut().println(getLogLine(Dict.START.toString(), mJob.getName()));

            var jobExecuteSection = mJob.getExecuteSection();
            try {
                // run before first task
                run(jobExecuteSection.getBefore(), "JobEditor.runBefore");

                runTasks();

                if (!mJob.getTasks().isEmpty()) {
                    if (mNumOfFailedTasks == 0) {
                        // run after last task - if all ok
                        run(jobExecuteSection.getAfterOk(), "JobEditor.runAfterOk");
                    } else {
                        var s = String.format(Dict.TASKS_FAILED.toString(), mNumOfFailedTasks);
                        mInputOutput.getErr().println(s);

                        // run after last task - if any failed
                        run(jobExecuteSection.getAfterFail(), "JobEditor.runAfterFail");
                    }
                }

                // run after last task
                run(jobExecuteSection.getAfter(), "JobEditor.runAfter");

                if (!mInterrupted) {
                    mInputOutput.getOut().println(getLogLine(Dict.DONE.toString(), mJob.getName()));
                    jobEnded(Dict.DONE.toString(), 0);
                }
            } catch (InterruptedException ex) {
                jobEnded(Dict.CANCELED.toString(), 99);
            } catch (IOException ex) {
                writelogs();
                Exceptions.printStackTrace(ex);
            } catch (ExecutionFailedException ex) {
                jobEnded(Dict.FAILED.toString(), 1);
                mInputOutput.getErr().println(String.format("\n\n%s", Dict.JOB_FAILED.toString()));
            }

            mProgressHandle.finish();
            ExecutorManager.getInstance().getJobExecutors().remove(mJob.getId());
        }, "JobExecutor");

        mExecutorThread.start();
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

    private String getLogLine(String header, String text) {
        return Jota.prependTimestamp("%s '%s'".formatted(header.toUpperCase(Locale.ROOT), text));
    }

    private String getRsyncErrorCode(int exitValue) {
        var bundle = SystemHelper.getBundle(getClass(), "ExitValues");
        var key = String.valueOf(exitValue);

        return bundle.containsKey(key) ? bundle.getString(key) : Dict.SYSTEM_CODE.toString().formatted(key);
    }

    private void jobEnded(String type, int status) {
        appendHistoryFile(getHistoryLine(mJob.getId(), type, mDryRunIndicator));
        updateJobStatus(status);
        writelogs();
        mStatusDisplayer.setStatusText(type);
    }

    private boolean run(String command, boolean stopOnError, String description) {
        mInputOutput.getOut().println(getLogLine(Dict.START.toString(), description));
        mInputOutput.getOut().println(Jota.prependTimestamp(command));
        boolean success = false;

        if (new File(command).isFile()) {
            var commandLine = new ArrayList<String>();
            commandLine.add(command);
            var result = runProcess(commandLine);

            String status;
            if (result == 0) {
                status = Dict.DONE.toUpper();
                success = true;
            } else {
                status = Dict.Dialog.ERROR.toUpper();
            }

            if (!mInterrupted) {
                mInputOutput.getOut().println(getLogLine(status, description));
            }

            if (stopOnError && result != 0) {
                var s = String.format("%s: exitValue=%d", Dict.FAILED.toString(), result);
                mInputOutput.getErr().println(s);
//                throw new ExecutionFailedException(string);
            }
        } else {
            var s = String.format("%s: %s", Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), command);
            if (stopOnError) {
//                throw new ExecutionFailedException(s);
                success = false;
            } else {
                mInputOutput.getErr().println(s);
            }
        }

        return success;
    }

    private void run(ExecuteItem executeItem, String key) throws IOException, InterruptedException, ExecutionFailedException {
        var command = executeItem.getCommand();
        if (!mInterrupted && executeItem.isEnabled() && StringUtils.isNotEmpty(command)) {
            run(command, executeItem.isHaltOnError(), mBundle.getString(key));
        }
    }

    private int runProcess(List<String> command) {
        if (mInterrupted) {
            return -1;
        }

        mProgressHandle.switchToIndeterminate();
        mIndeterminate = true;

        var processBuilder = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
        processBuilder.setExecutable(command.getFirst());
        if (command.size() > 1) {
            processBuilder.setArguments(command.subList(1, command.size()));
        }

        var outLineConvertorFactory = new ExecutionDescriptor.LineConvertorFactory() {
            private String mPrevLine;
            private final Progress mProgress = new Progress();

            @Override
            public LineConvertor newLineConvertor() {
                return (LineConvertor) line -> {
                    var lines = new ArrayList<ConvertedLine>();

                    try {
                        if (StringUtils.isBlank(line)) {
                            lines.add(ConvertedLine.forText("", null));
                        } else if (StringUtils.startsWith(line, "*deleting   ") || StringUtils.startsWith(line, "deleting ")) {
                            mInputOutput.getErr().println(line);
                        } else if (mProgress.parse(line)) {
                            if (mIndeterminate) {
                                mIndeterminate = false;
                                mProgressHandle.switchToDeterminate(100);
                            }
                            mProgressHandle.progress(mProgress.getStep());
                            var currentProgressString = new StringBuilder(mPrevLine).append(" ").append(mProgress.toString()).toString();
                            mProgressHandle.progress(currentProgressString);
                            mStatusDisplayer.setStatusText(currentProgressString);
                        } else {
                            lines.add(ConvertedLine.forText(line, null));
                            mPrevLine = line;
                        }
                    } catch (Exception e) {
                        lines.add(ConvertedLine.forText(e.toString(), null));
                    }

                    if (StringUtils.contains(line, "(xfr#")) {
                        mProgressHandle.switchToIndeterminate();
                        mIndeterminate = true;
                    }

                    return lines;
                };
            }
        };

        var descriptor = new ExecutionDescriptor()
                .frontWindow(true)
                .inputOutput(mInputOutput)
                .noReset(true)
                .errLineBased(true)
                .outLineBased(true)
                .outConvertorFactory(outLineConvertorFactory)
                .preExecution(() -> {
                    //mInputOutput.getErr().println("PRE");
                })
                .postExecution(exitCode -> {
                    //mInputOutput.getErr().println("POST exitCode=" + exitCode);
                })
                .showProgress(false);

        var service = ExecutionService.newService(
                processBuilder,
                descriptor,
                mJob.getName());

        var task = service.run();

        try {
            var returnCode = task.get();

            return returnCode;
        } catch (InterruptedException ex) {
            mInterrupted = true;
            task.cancel(true);
        } catch (ExecutionException ex) {
            mInterrupted = true;
            task.cancel(true);
            mInputOutput.getErr().println(ex);
            Exceptions.printStackTrace(ex);
        }

        return -1;
    }

    private int runRsync(Task task) {
        var command = new ArrayList<String>();
        command.add(mOptions.getRsyncPath());
        if (mDryRun) {
            command.add("--dry-run");
        }
        command.addAll(task.getCommand());
        var s = String.format("%s %s: rsync\n\n%s\n", Jota.nowToDateTime(), Dict.START.toString(), StringUtils.join(command, " "));
        mInputOutput.getOut().println(s);

        return runProcess(command);
    }

    private boolean runTask(Task task) {
        if (mInterrupted) {
            return false;
        }
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

    private boolean runTaskStep(ExecuteItem executeItem, String key) {
        boolean doNextStep = false;

//        try {
        var command = executeItem.getCommand();
        if (executeItem.isEnabled() && StringUtils.isNotEmpty(command)) {
            if (!run(command, executeItem.isHaltOnError(), mBundle.getString(key))) {
                mTaskFailed = true;
            }
        }
        doNextStep = true;
//        } catch (IOException | ExecutionFailedException ex) {
//            mTaskFailed = true;
//            Exceptions.printStackTrace(ex);
//        }

        return doNextStep;
    }

    private void runTasks() throws InterruptedException {
        if (mInterrupted) {
            return;
        }
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
        FxHelper.runLater(() -> StorageManager.save());
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
