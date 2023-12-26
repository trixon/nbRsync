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

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private transient String mHistory = "";

    public BaseItem() {

    }

    @Override
    public int compareTo(BaseItem baseItem) {
        return mName.compareTo(baseItem.getName());
    }

    public String getDescription() {
        return mDescription;
    }

    public String getHistory() {
        return mHistory;
    }

    public String getId() {
        return mId;
    }

    public long getLastRun() {
        return mLastRun;
    }

    public String getLastRunDateTime(String replacement, long lastRun) {
        String lastRunDateTime = replacement;

        if (lastRun > 0) {
            Date date = new Date(lastRun);
            lastRunDateTime = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(date);
        }

        return lastRunDateTime;
    }

    public String getLastRunDateTime(String replacement) {
        return getLastRunDateTime(replacement, mLastRun);
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

    public void setHistory(String history) {
        mHistory = history == null ? "" : history;
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
