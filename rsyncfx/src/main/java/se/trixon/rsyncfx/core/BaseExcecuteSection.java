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
public abstract class BaseExcecuteSection {

    @SerializedName("after")
    private ExecuteItem mAfter = new ExecuteItem();
    @SerializedName("afterFail")
    private ExecuteItem mAfterFail = new ExecuteItem();
    @SerializedName("afterOk")
    private ExecuteItem mAfterOk = new ExecuteItem();
    @SerializedName("before")
    private ExecuteItem mBefore = new ExecuteItem();

    public BaseExcecuteSection() {
    }

    public ExecuteItem getAfter() {
        return mAfter;
    }

    public ExecuteItem getAfterFail() {
        return mAfterFail;
    }

    public ExecuteItem getAfterOk() {
        return mAfterOk;
    }

    public ExecuteItem getBefore() {
        return mBefore;
    }

    public void setAfter(ExecuteItem after) {
        mAfter = after;
    }

    public void setAfterFail(ExecuteItem afterFail) {
        mAfterFail = afterFail;
    }

    public void setAfterOk(ExecuteItem afterOk) {
        mAfterOk = afterOk;
    }

    public void setBefore(ExecuteItem before) {
        mBefore = before;
    }

}
