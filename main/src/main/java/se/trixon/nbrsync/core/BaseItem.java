/*
 * Copyright 2024 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.nbrsync.core;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;
import se.trixon.almond.util.fx.control.editable_list.EditableListItem;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BaseItem implements Comparable<BaseItem>, EditableListItem {

    @SerializedName("description")
    protected String mDescription = "";
    @SerializedName("id")
    protected String mId = UUID.randomUUID().toString();
    @SerializedName("lastRun")
    protected long mLastRun = -1;
    @SerializedName("lastRunExitCode")
    protected int mLastRunExitCode = -1;
    @SerializedName("name")
    protected String mName = "";

    public BaseItem() {

    }

    @Override
    public int compareTo(BaseItem baseItem) {
        return mName.compareTo(baseItem.getName());
    }

    public String getDescription() {
        return mDescription;
    }

    public String getId() {
        return mId;
    }

    public long getLastRun() {
        return mLastRun;
    }

    public int getLastRunExitCode() {
        return mLastRunExitCode;
    }

    public String getLastRunStatus() {
        String status = "";
        if (mLastRun > 0) {
            status = getLastRunExitCode() == 0 ? "" : "⚠";
            //if (isRunnning()) {
            //    status = "∞";
            //}
        }

        return status;
    }

    @Override
    public String getName() {
        return mName;
    }

    public boolean isValid() {
        return !getName().isEmpty();
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setLastRun(long lastRun) {
        mLastRun = lastRun;
    }

    public void setLastRunExitCode(int lastRunExitCode) {
        mLastRunExitCode = lastRunExitCode;
    }

    public void setName(String name) {
        mName = name;
    }
}
