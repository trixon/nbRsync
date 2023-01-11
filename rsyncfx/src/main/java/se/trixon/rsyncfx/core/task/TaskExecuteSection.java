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
package se.trixon.rsyncfx.core.task;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Patrik Karlström
 */
public class TaskExecuteSection extends TaskSection implements Serializable {

    @SerializedName("after")
    private boolean mAfter;
    @SerializedName("after_command")
    private String mAfterCommand = "";
    @SerializedName("after_failure")
    private boolean mAfterFailure;
    @SerializedName("after_failure_command")
    private String mAfterFailureCommand = "";
    @SerializedName("after_failure_halt_on_error")
    private boolean mAfterFailureHaltOnError;
    @SerializedName("after_halt_on_error")
    private boolean mAfterHaltOnError;
    @SerializedName("after_success")
    private boolean mAfterSuccess;
    @SerializedName("after_success_command")
    private String mAfterSuccessCommand = "";
    @SerializedName("after_success_halt_on_error")
    private boolean mAfterSuccessHaltOnError;
    @SerializedName("before")
    private boolean mBefore;
    @SerializedName("before_command")
    private String mBeforeCommand = "";
    @SerializedName("before_halt_on_error")
    private boolean mBeforeHaltOnError;
    @SerializedName("job_halt_on_error")
    private boolean mJobHaltOnError;

    public String getAfterCommand() {
        return mAfterCommand;
    }

    public String getAfterFailureCommand() {
        return mAfterFailureCommand;
    }

    public String getAfterSuccessCommand() {
        return mAfterSuccessCommand;
    }

    public String getBeforeCommand() {
        return mBeforeCommand;
    }

    @Override
    public List<String> getCommand() {
        return null;
    }

    public boolean isAfter() {
        return mAfter;
    }

    public boolean isAfterFailure() {
        return mAfterFailure;
    }

    public boolean isAfterFailureHaltOnError() {
        return mAfterFailureHaltOnError;
    }

    public boolean isAfterHaltOnError() {
        return mAfterHaltOnError;
    }

    public boolean isAfterSuccess() {
        return mAfterSuccess;
    }

    public boolean isAfterSuccessHaltOnError() {
        return mAfterSuccessHaltOnError;
    }

    public boolean isBefore() {
        return mBefore;
    }

    public boolean isBeforeHaltOnError() {
        return mBeforeHaltOnError;
    }

    public boolean isJobHaltOnError() {
        return mJobHaltOnError;
    }

    public void setAfter(boolean value) {
        mAfter = value;
    }

    public void setAfterCommand(String value) {
        mAfterCommand = value;
    }

    public void setAfterFailure(boolean value) {
        mAfterFailure = value;
    }

    public void setAfterFailureCommand(String value) {
        mAfterFailureCommand = value;
    }

    public void setAfterFailureHaltOnError(boolean value) {
        mAfterFailureHaltOnError = value;
    }

    public void setAfterHaltOnError(boolean value) {
        mAfterHaltOnError = value;
    }

    public void setAfterSuccess(boolean value) {
        mAfterSuccess = value;
    }

    public void setAfterSuccessCommand(String value) {
        mAfterSuccessCommand = value;
    }

    public void setAfterSuccessHaltOnError(boolean value) {
        mAfterSuccessHaltOnError = value;
    }

    public void setBefore(boolean value) {
        mBefore = value;
    }

    public void setBeforeCommand(String value) {
        mBeforeCommand = value;
    }

    public void setBeforeHaltOnError(boolean value) {
        mBeforeHaltOnError = value;
    }

    public void setJobHaltOnError(boolean value) {
        mJobHaltOnError = value;
    }
}
