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

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ExecuteItem {

    @SerializedName("enabled")
    private boolean mEnabled;
    @SerializedName("command")
    private String mCommand = "";
    @SerializedName("haltOnError")
    private boolean mHaltOnError;

    public ExecuteItem() {
    }

    public String getCommand() {
        return mCommand;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean isHaltOnError() {
        return mHaltOnError;
    }

    public void setCommand(String command) {
        mCommand = command;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void setHaltOnError(boolean haltOnError) {
        mHaltOnError = haltOnError;
    }

}
