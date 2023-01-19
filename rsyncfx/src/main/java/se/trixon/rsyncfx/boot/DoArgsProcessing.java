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
package se.trixon.rsyncfx.boot;

import java.util.ResourceBundle;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.LifecycleManager;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.util.PomInfo;
import se.trixon.rsyncfx.ui.App;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class DoArgsProcessing implements ArgsProcessor {

    @Arg(longName = "list")
    @Description(
            shortDescription = "#DoArgsProcessing.list.desc"
    )
    @Messages("DoArgsProcessing.list.desc=list jobs")
    public boolean mListOption;
    @Arg(longName = "start")
    @Description(
            displayName = "#DoArgsProcessing.start.name",
            shortDescription = "#DoArgsProcessing.start.desc"
    )
    @Messages({
        "DoArgsProcessing.start.name=--start <job>",
        "DoArgsProcessing.start.desc=start job"
    })
    public String mStartOption;

    @Arg(longName = "version")
    @Description(
            shortDescription = "#DoArgsProcessing.version.desc"
    )
    @Messages("DoArgsProcessing.version.desc=print the version information and exit")
    public boolean mVersionOption;
    private final ResourceBundle mBundle = NbBundle.getBundle(DoArgsProcessing.class);

    {
        Installer.GUI = false;
    }

    public DoArgsProcessing() {
    }

    @Override
    public void process(Env env) throws CommandException {
        if (mVersionOption) {
            displayVersion();
        } else if (mListOption) {
            listJobs();
        } else if (mStartOption != null) {
            startJob(mStartOption);
        }

        LifecycleManager.getDefault().exit();
    }

    private void displayVersion() {
        var pomInfo = new PomInfo(App.class, "se.trixon.rsyncfx", "rsyncfx");
        System.out.println(mBundle.getString("DoArgsProcessing.version").formatted(pomInfo.getVersion()));
    }

    private void listJobs() {
        System.out.println("LIST JOBS");
    }

    private void startJob(String jobName) {
        System.out.println("START JOB <%s>".formatted(jobName));
    }
}
