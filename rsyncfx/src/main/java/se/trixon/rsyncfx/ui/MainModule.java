/*
 * Copyright 2022 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.rsyncfx.ui;

import com.dlsc.gemsfx.util.SessionManager;
import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.rsyncfx.RsyncFx.getIconSizeToolBarInt;
import se.trixon.rsyncfx.core.job.Job;
import se.trixon.rsyncfx.ui.common.AlwaysOpenTab;
import se.trixon.rsyncfx.ui.common.BaseModule;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class MainModule extends BaseModule implements AlwaysOpenTab {

    private final ListView<Job> mListView = new ListView<>();
    private final SessionManager mSessionManager = new SessionManager(NbPreferences.forModule(MainModule.class).node("sessionManager"));
    private final SplitPane mSplitPane = new SplitPane();
    private SummaryBuilder mSummaryBuilder;
    private final WebView mWebView = new WebView();

    public MainModule() {
        super(Dict.HOME.toString(), MaterialIcon._Action.HOME.getImageView(ICON_SIZE_MODULE, Color.WHITE).getImage());
    }

    @Override
    public Node activate() {
        return mSplitPane;
    }

    @Override
    public void init(Workbench workbench) {
        super.init(workbench);

        createUI();
        initBindings();
        initListeners();

        displaySystemInformation();
        mSummaryBuilder = new SummaryBuilder();
    }

    @Override
    public void updateNightMode(boolean state) {
        var name = state ? "darkWeb.css" : "lightWeb.css";
        mWebView.getEngine().setUserStyleSheetLocation(getClass().getResource(name).toExternalForm());
    }

    private void createUI() {
        var startToolbarItem = new ToolbarItem(Dict.START.toString(), MaterialIcon._Av.PLAY_ARROW.getImageView(getIconSizeToolBarInt(), Color.WHITE), mouseEvent -> {
            doStart();
        });

        var aboutRsyncToolbarItem = new ToolbarItem(Dict.ABOUT_S.toString().formatted("rsync"), MaterialIcon._Notification.SYNC.getImageView(getIconSizeToolBarInt(), Color.WHITE), mouseEvent -> {
            System.out.println("ABOUT RSYNC");
        });

        getToolbarControlsLeft().setAll(
                startToolbarItem
        );

        getToolbarControlsRight().setAll(
                aboutRsyncToolbarItem
        );

        mSplitPane.getItems().setAll(mListView, mWebView);
        mListView.itemsProperty().bind(mJobManager.itemsProperty());

        mListView.setMinWidth(FxHelper.getUIScaled(250));
        SplitPane.setResizableWithParent(mListView, Boolean.FALSE);
        mListView.setCellFactory(listView -> new JobListCell());

        var nullSelectionBooleanBinding = mListView.getSelectionModel().selectedItemProperty().isNull();
        startToolbarItem.disableProperty().bind(nullSelectionBooleanBinding);
    }

    private void displaySystemInformation() {
        mWebView.getEngine().loadContent("<pre>%s</pre>".formatted(SystemHelper.getSystemInfo()));
    }

    private void doStart() {
        System.out.println("START " + mListView.getSelectionModel().getSelectedItem());
    }

    private void initBindings() {
        mSplitPane.setDividerPositions(0);
        mSessionManager.register("mainModule.splitter1", mSplitPane.getDividers().get(0).positionProperty());
    }

    private void initListeners() {
        mListView.getSelectionModel().selectedItemProperty().addListener((p, o, job) -> {
            if (job != null) {
                mWebView.getEngine().loadContent(mSummaryBuilder.getHtml(job));
            } else {
                displaySystemInformation();
            }
        });
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
            mRoot.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY
                        && mouseEvent.getClickCount() == 2) {
                    doStart();
                }
            });
        }
    }
}
