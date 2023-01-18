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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.rsyncfx.RsyncFx.getIconSizeToolBarInt;
import se.trixon.rsyncfx.core.job.Job;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class MainModule extends BaseModule {

    private ListView<Job> mListView = new ListView<>();
    private BorderPane mMainPane = new BorderPane();
    private BorderPane mRoot = new BorderPane();
    private final SessionManager mSessionManager = new SessionManager(NbPreferences.forModule(MainModule.class));
    private final SplitPane mSplitPane = new SplitPane();
    private final WebView mWebView = new WebView();
    private Workbench mWorkbench;

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
        mWorkbench = workbench;
//        mSplitPane.setSkin(new DumbSplitPaneSkin(mSplitPane));

        createUI();
        initBindings();
        initListeners();
    }

    @Override
    public void updateNightMode(boolean state) {
        if (state) {
            mWebView.getEngine().setUserStyleSheetLocation(getClass().getResource("darkWeb.css").toExternalForm());
        } else {
            mWebView.getEngine().setUserStyleSheetLocation(getClass().getResource("lightWeb.css").toExternalForm());
        }

    }

    private void createUI() {
        mMainPane = new BorderPane(new Label("?"));
        mRoot = new BorderPane();
        mMainPane.setTop(new Button("Hello"));

        mMainPane.setBackground(FxHelper.createBackground(Color.CORAL));
        mRoot.setBackground(FxHelper.createBackground(Color.GRAY));
//        mRoot.setBackground(new ListView<>().getBackground());
//        mRoot.setPadding(FxHelper.getUIScaledInsets(16, 16, 0, 16));
        var aboutRsyncToolbarItem = new ToolbarItem(Dict.ABOUT_S.toString().formatted("rsync"), MaterialIcon._Notification.SYNC.getImageView(getIconSizeToolBarInt(), Color.WHITE), mouseEvent -> {
            System.out.println("ABOUT RSYNC");
        });

        var editorToolbarItem = new ToolbarItem(JobEditorPane.getAction().getText(), MaterialIcon._Content.CREATE.getImageView(getIconSizeToolBarInt(), Color.WHITE), mouseEvent -> {
            JobEditorPane.getAction().handle(null);
        });

        getToolbarControlsLeft().setAll(
                editorToolbarItem
        );

        getToolbarControlsRight().setAll(
                aboutRsyncToolbarItem
        );

//        mRoot.setLeft(mListView);
        mSplitPane.getItems().setAll(mListView, mWebView);
        mListView.getItems().setAll(mStorageManager.getJobManager().getJobs());

        mListView.setMinWidth(FxHelper.getUIScaled(250));
        SplitPane.setResizableWithParent(mListView, Boolean.FALSE);
//        mSplitPane.getDividers().get(0).
        mListView.setCellFactory(lListView -> new JobListCell());

    }

    private void initBindings() {
        mSessionManager.register("a/b/c", mSplitPane.getDividers().get(0).positionProperty());

    }

    private void initListeners() {
        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Job> c) -> {
            Job job = mListView.getSelectionModel().getSelectedItem();
            if (job != null) {
                mWebView.getEngine().loadContent(job.getSummaryAsHtml());
            } else {
                mWebView.getEngine().loadContent("");
            }
        });
    }

    class JobListCell extends ListCell<Job> {

        private final BorderPane mBorderPane = new BorderPane();
        private final Label mDescLabel = new Label();
        private final Duration mDuration = Duration.millis(200);
        private Action mEditAction;
        private final FadeTransition mFadeInTransition = new FadeTransition();
        private final FadeTransition mFadeOutTransition = new FadeTransition();
        private final Label mLastLabel = new Label();
        private final Label mNameLabel = new Label();
        private Action mRunAction;
        private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();
        private final StackPane mStackPane = new StackPane();

        public JobListCell() {
            mFadeInTransition.setDuration(mDuration);
            mFadeInTransition.setFromValue(0);
            mFadeInTransition.setToValue(1);

            mFadeOutTransition.setDuration(mDuration);
            mFadeOutTransition.setFromValue(1);
            mFadeOutTransition.setToValue(0);

            createUI();
            setNightMode(mOptions.isNightMode());
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

            setGraphic(mStackPane);
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

            mRunAction = new Action(Dict.RUN.toString(), (ActionEvent event) -> {
                mFadeOutTransition.playFromStart();
//                mJobController.run(getSelectedJob());
                mListView.requestFocus();
            });

            mEditAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
                mFadeOutTransition.playFromStart();
//                displayEditor(getSelectedJob().getId());
                mListView.requestFocus();
            });

            VBox mainBox = new VBox(mNameLabel, mDescLabel, mLastLabel);
            mainBox.setAlignment(Pos.CENTER_LEFT);

            Collection<? extends Action> actions = Arrays.asList(
                    mEditAction,
                    mRunAction
            );

            mOptions.nightModeProperty().addListener((observable, oldValue, newValue) -> {
                setNightMode(newValue);
            });
            ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
            toolBar.setBackground(Background.EMPTY);
            toolBar.setVisible(false);
            toolBar.setMaxWidth(4 * ICON_SIZE_PROFILE * 1.84);
            FxHelper.slimToolBar(toolBar);
            FxHelper.undecorateButtons(toolBar.getItems().stream());
            FxHelper.adjustButtonWidth(toolBar.getItems().stream(), ICON_SIZE_PROFILE * 1.8);

            toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });

            BorderPane.setAlignment(toolBar, Pos.CENTER);

            mBorderPane.setCenter(mainBox);
            BorderPane.setMargin(mainBox, new Insets(8));
            mBorderPane.setRight(toolBar);
            mFadeInTransition.setNode(toolBar);
            mFadeOutTransition.setNode(toolBar);

            mBorderPane.setOnMouseEntered((MouseEvent event) -> {
                if (!toolBar.isVisible()) {
                    toolBar.setVisible(true);
                }

                selectListItem();
                mFadeInTransition.playFromStart();
            });

            mBorderPane.setOnMouseExited((MouseEvent event) -> {
                mFadeOutTransition.playFromStart();
            });
            mStackPane.getChildren().setAll(mainBox, toolBar);
            StackPane.setAlignment(toolBar, Pos.CENTER_RIGHT);

            mStackPane.setOnMouseEntered(mouseEvent -> {
                if (!toolBar.isVisible()) {
                    toolBar.setVisible(true);
                }

                selectListItem();
//                mRunStateManager.setProfile(getSelectedProfile());
                mFadeInTransition.playFromStart();
            });

            mStackPane.setOnMouseExited(mouseEvent -> {
                mFadeOutTransition.playFromStart();
            });

        }

        private Job getSelectedJob() {
            return mListView.getSelectionModel().getSelectedItem();
        }

        private void selectListItem() {
            mListView.getSelectionModel().select(this.getIndex());
            mListView.requestFocus();
        }

        private void setNightMode(boolean state) {
            mRunAction.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(ICON_SIZE_PROFILE));
            mEditAction.setGraphic(MaterialIcon._Image.EDIT.getImageView(ICON_SIZE_PROFILE));
        }
    }
}
