/*
 * Copyright 2024 Patrik Karlström.
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
package se.trixon.nbrsync.gui;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.TimeHelper;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.nbrsync.NbRsync;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 10000)
public class TimerStatusLineElement implements StatusLineElementProvider {

    private JLabel mLabel;
    private long mStartTime;
    private Timer mTimer;

    public TimerStatusLineElement() {
    }

    @Override
    public Component getStatusLineElement() {
        if (mLabel == null) {
            init();
            initListeners();
        }

        return mLabel;
    }

    private void init() {
        mLabel = new JLabel();
        mLabel.setFont(new Font("monospaced", Font.PLAIN, mLabel.getFont().getSize()));
        mLabel.setBorder(new EmptyBorder(0, 0, 0, SwingHelper.getUIScaled(16)));

        mTimer = new Timer(250, actionEvent -> {
            var minSec = TimeHelper.millisToMinSec(SystemHelper.age(mStartTime));
            mLabel.setText("%5d:%02d".formatted(minSec[0], minSec[1]));
        });

    }

    private void initListeners() {
        NbRsync.getInstance().getGlobalState().addListener(gsce -> {
            mStartTime = gsce.getValue();
            mTimer.start();
        }, NbRsync.GSC_TIMER_START);

        NbRsync.getInstance().getGlobalState().addListener(gsce -> {
            mTimer.stop();
        }, NbRsync.GSC_TIMER_STOP);
    }
}
