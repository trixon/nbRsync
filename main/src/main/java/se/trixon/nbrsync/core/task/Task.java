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
package se.trixon.nbrsync.core.task;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.nbrsync.core.BaseItem;

/**
 *
 * @author Patrik Karlström
 */
public class Task extends BaseItem {

    private final transient List<String> mCommand = new ArrayList<>();
    @SerializedName("destination")
    private String mDestination;
    @SerializedName("environment")
    private String mEnvironment = "";
    @SerializedName("excludeSection")
    private final ExcludeSection mExcludeSection;
    @SerializedName("executeSection")
    private final TaskExecuteSection mExecuteSection;
    @SerializedName("extraOptions")
    private String mExtraOptions;
    @SerializedName("noAdditionalDir")
    private boolean mNoAdditionalDir;
    @SerializedName("optionSection")
    private final OptionSection mOptionSection;
    @SerializedName("source")
    private String mSource;

    public Task() {
        mExecuteSection = new TaskExecuteSection();
        mExcludeSection = new ExcludeSection();
        mOptionSection = new OptionSection();
    }

    public List<String> getCommand() {
        mCommand.clear();

        if (!StringUtils.isBlank(StringUtils.join(mOptionSection.getCommand(), ""))) {
            mCommand.addAll(mOptionSection.getCommand());
        }

        if (!StringUtils.isBlank(StringUtils.join(mExcludeSection.getCommand(), ""))) {
            mCommand.addAll(mExcludeSection.getCommand());
        }

        mCommand.addAll(Arrays.asList(StringUtils.split(getExtraOptions())));

        add(getPath(mSource));
        add(getPath(mDestination));

        return mCommand;
    }

    public String getCommandAsString() {
        return StringUtils.join(getCommand(), " ");
    }

    public String getDestination() {
        return mDestination;
    }

    public String getEnvironment() {
        return mEnvironment;
    }

    public ExcludeSection getExcludeSection() {
        return mExcludeSection;
    }

    public TaskExecuteSection getExecuteSection() {
        return mExecuteSection;
    }

    public String getExtraOptions() {
        return mExtraOptions;
    }

    public OptionSection getOptionSection() {
        return mOptionSection;
    }

    public String getPath(String path) {
        return SystemUtils.IS_OS_WINDOWS ? convertToWindowsCygwinPath(path) : path;
    }

    public String getSource() {
        return mSource;
    }

    public boolean isDryRun() {
        return mOptionSection.getCommand().contains("--dry-run");
    }

    public boolean isNoAdditionalDir() {
        return mNoAdditionalDir;
    }

    public void setDestination(String destination) {
        mDestination = destination;
    }

    public void setEnvironment(String environment) {
        mEnvironment = environment;
    }

    public void setExtraOptions(String extraOptions) {
        mExtraOptions = extraOptions;
    }

    public void setNoAdditionalDir(boolean value) {
        mNoAdditionalDir = value;
    }

    public void setSource(String source) {
        mSource = source;
    }

    @Override
    public String toString() {
        return getName();
    }

    private void add(String command) {
        if (!mCommand.contains(command)) {
            mCommand.add(command);
        }
    }

    private String convertToWindowsCygwinPath(String path) {
        var s = StringUtils.remove(path, ":");
        s = StringUtils.replace(s, "\\", "/");

        return "/cygdrive/" + s;
    }

}
