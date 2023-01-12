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
package se.trixon.rsyncfx.core.job;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Patrik Karlström
 */
public class JobExecuteSection extends JobSection {

    @SerializedName("after")
    private boolean mAfter;
    @SerializedName("afterCommand")
    private String mAfterCommand = "";
    @SerializedName("afterFailure")
    private boolean mAfterFailure;
    @SerializedName("afterFailureCommand")
    private String mAfterFailureCommand = "";
    @SerializedName("afterSuccess")
    private boolean mAfterSuccess;
    @SerializedName("afterSuccessCommand")
    private String mAfterSuccessCommand = "";
    @SerializedName("before")
    private boolean mBefore;
    @SerializedName("beforeCommand")
    private String mBeforeCommand = "";
    @SerializedName("beforeHaltOnError")
    private boolean mBeforeHaltOnError;

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

    public boolean isAfter() {
        return mAfter;
    }

    public boolean isAfterFailure() {
        return mAfterFailure;
    }

    public boolean isAfterSuccess() {
        return mAfterSuccess;
    }

    public boolean isBefore() {
        return mBefore;
    }

    public boolean isBeforeHaltOnError() {
        return mBeforeHaltOnError;
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

    public void setAfterSuccess(boolean value) {
        mAfterSuccess = value;
    }

    public void setAfterSuccessCommand(String value) {
        mAfterSuccessCommand = value;
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
}
