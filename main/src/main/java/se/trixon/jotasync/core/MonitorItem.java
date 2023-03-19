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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class MonitorItem {

    private boolean mJobRunning = false;
    private final ListView<String> mListView = new ListView<>();
    private final transient LinkedBlockingQueue<String> mQueue = new LinkedBlockingQueue<>();
    private final Tab mTab;
    private final transient TextArea mTextArea = new LogPanel();
    private Thread mThread;

    public MonitorItem(String title) {
        mTab = new Tab(title, mListView);
        createMenu();
        mListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public boolean add(String message) {
        System.out.println(message);
        boolean added = mQueue.add(message);
        return added;
    }

    public void clear() {
        mListView.getItems().clear();
    }

    public ListView<String> getListView() {
        return mListView;
    }

    public Tab getTab() {
        return mTab;
    }

    public TextArea getTextArea() {
        return mTextArea;
    }

    public void requestStop() {
        mJobRunning = false;
    }

    public void start() {
        clear();
        mJobRunning = true;
        mThread = new Thread(new MonitorThread());
        mThread.start();
    }

    private void copy(ObservableList<String> items) {
        var sb = new StringBuilder(String.join("\n", items)).append("\n");
        SystemHelper.copyToClipboard(sb.toString());
    }

    private void createMenu() {
        var copySelectionAction = new Action(Dict.COPY_SELECTION.toString(), actionEvent -> {
            copy(mListView.getSelectionModel().getSelectedItems());
        });

        var copyAllAction = new Action(Dict.COPY_ALL.toString(), actionEvent -> {
            copy(mListView.getItems());
        });

        var actions = List.of(copySelectionAction, copyAllAction);
        var contextMenu = ActionUtils.createContextMenu(actions);
        mListView.setContextMenu(contextMenu);
    }

    class MonitorThread implements Runnable {

        public MonitorThread() {
        }

        @Override
        public void run() {
            var drainedItems = new ArrayList<String>();

            while (mJobRunning) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                var numOfRows = mQueue.drainTo(drainedItems);

                if (!drainedItems.isEmpty()) {
                    Platform.runLater(() -> {
                        mListView.getItems().addAll(drainedItems);
                        mListView.scrollTo(mListView.getItems().size() - 1);
                        mListView.refresh();
                    });
                }
            }

            mListView.refresh();
        }
    }
}
