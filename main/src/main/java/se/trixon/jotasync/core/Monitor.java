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

import java.util.LinkedHashSet;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Monitor {

    private final MonitorItem mDelMonitorItem;
    private final MonitorItem mErrMonitorItem;
    private final LinkedHashSet<MonitorItem> mMonitorItems = new LinkedHashSet<>();
    private final MonitorItem mStdMonitorItem;

    public Monitor(MonitorItem delMonitorItem, MonitorItem errMonitorItem, MonitorItem stdMonitorItem) {
        mDelMonitorItem = delMonitorItem;
        mErrMonitorItem = errMonitorItem;
        mStdMonitorItem = stdMonitorItem;

        mMonitorItems.add(mStdMonitorItem);
        mMonitorItems.add(mErrMonitorItem);
        mMonitorItems.add(mDelMonitorItem);
    }

    public boolean add(String message) {
        return add(Channel.STD, message);
    }

    public boolean add(Channel channel, String message) {
        switch (channel) {
            case DEL -> {
                return mDelMonitorItem.add(message);
            }
            case ERR -> {
                return mErrMonitorItem.add(message);
            }
            case STD -> {
                return mStdMonitorItem.add(message);
            }
        }

        return false;
    }

    public void clear() {
        mMonitorItems.stream().forEachOrdered(item -> item.clear());
    }

    public void start() {
        mMonitorItems.stream().forEachOrdered(item -> item.start());
    }

    public void stop() {
        mMonitorItems.stream().forEachOrdered(item -> item.requestStop());
    }

    public enum Channel {
        STD, ERR, DEL;
    }

}
