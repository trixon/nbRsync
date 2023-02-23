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
package se.trixon.jotasync.ui;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.core.ExecutorManager;
import se.trixon.jotasync.core.Rsync;
import se.trixon.jotasync.core.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class LauncherListView extends LauncherViewBase {

    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final ListView<Job> mListView = new ListView<>();
    private SplitPane mSplitPane;
    private final WebView mWebView = new WebView();
    private final Jota mJota = Jota.getInstance();

    public LauncherListView() {
        createUI();

        initBindings();
        initListeners();

        updateNightMode(mOptions.isNightMode());

        displaySystemInformation();
    }

    public ListView<Job> getListView() {
        return mListView;
    }

    @Override
    public Node getNode() {
        return mSplitPane;
    }

    private void createUI() {
        mSplitPane = new SplitPane(mListView, mWebView);

        mListView.setMinWidth(FxHelper.getUIScaled(250));
        SplitPane.setResizableWithParent(mListView, Boolean.FALSE);
        mListView.setCellFactory(listView -> new JobListCell());

        mSummaryBuilder = new SummaryBuilder();
    }

    private void displaySystemInformation() {
        FxHelper.runLater(() -> {
            mWebView.getEngine().loadContent("<h2>%s</h2><pre>%s</pre><h2>%s</h2><pre>%s</pre>".formatted(
                    Dict.SYSTEM.toString().toUpperCase(Locale.ENGLISH),
                    SystemHelper.getSystemInfo(),
                    "RSYNC",
                    Rsync.getInfo()
            ));
        });
    }

    private void initBindings() {
        mListView.itemsProperty().bind(mJobManager.itemsProperty());
        mSplitPane.setDividerPositions(0);
        mSessionManager.register("mainModule.splitter1", mSplitPane.getDividers().get(0).positionProperty());
    }

    private void initListeners() {
        mOptions.nightModeProperty().addListener((p, o, n) -> {
            updateNightMode(n);
        });

        mListView.getSelectionModel().selectedItemProperty().addListener((p, o, job) -> {
            if (job != null) {
                mWebView.getEngine().loadContent(mSummaryBuilder.getHtml(job));
            } else {
                displaySystemInformation();
            }
        });
    }

    private void updateNightMode(boolean state) {
        var name = state ? "darkWeb.css" : "lightWeb.css";
        mWebView.getEngine().setUserStyleSheetLocation(getClass().getResource(name).toExternalForm());
    }

    class JobListCell extends ListCell<Job> {

        private final Label mDescLabel = new Label();
        private final Label mLastLabel = new Label();
        private final Label mNameLabel = new Label();
        private VBox mRoot;
        private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();

        public JobListCell() {
            createUI();
        }

        @Override
        protected void updateItem(Job job, boolean empty) {
            super.updateItem(job, empty);

            if (job == null || empty) {
                clearContent();
            } else {
                addContent(job);
            }
        }

        private void addContent(Job job) {
            setText(null);

            mNameLabel.setText(job.getName());
            mDescLabel.setText(job.getDescription());
            String lastRun = "-";
            if (job.getLastRun() != 0) {
                lastRun = mSimpleDateFormat.format(new Date(job.getLastRun()));
            }
            mLastLabel.setText(lastRun);

            var runAction = new Action(Dict.RUN.toString(), actionEvent -> {
                mExecutorManager.requestStart(job);
            });

            var editAction = new Action("%s %s".formatted(Dict.EDIT.toString(), job.getName()), actionEvent -> {
                mJota.getGlobalState().put(Jota.GSC_EDITOR, job);
            });

            var editorAction = new Action(Dict.EDITOR.toString(), actionEvent -> {
                mJota.getGlobalState().put(Jota.GSC_EDITOR, null);
            });

            var actions = Arrays.asList(
                    runAction,
                    editAction,
                    editorAction
            );

            var contextMenu = ActionUtils.createContextMenu(actions);
            contextMenu.setOnShowing(windowEvent -> {
                runAction.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(Jota.getIconSizeToolBarInt()));
                editAction.setGraphic(MaterialIcon._Content.CREATE.getImageView(Jota.getIconSizeToolBarInt()));
            });

            mRoot.setOnMousePressed(mouseEvent -> {
                if (mouseEvent.isSecondaryButtonDown()) {
                    contextMenu.show(this, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            });

            mRoot.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    mExecutorManager.requestStart(job);
                }
            });

            setGraphic(mRoot);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.4));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));
            mLastLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));

            mRoot = new VBox(mNameLabel, mDescLabel, mLastLabel);
            mRoot.setAlignment(Pos.CENTER_LEFT);
        }
    }

}
