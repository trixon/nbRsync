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
package se.trixon.jotasync.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javafx.scene.Scene;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.jotasync.Jota;
import se.trixon.jotasync.ui.editor.EditorPane;

@ActionID(
        category = "Tools",
        id = "se.trixon.jotasync.actions.EditorAction"
)
@ActionRegistration(
        displayName = "#CTL_EditorAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 0),
    @ActionReference(path = "Shortcuts", name = "D-E")
})
@Messages("CTL_EditorAction=Editor")
public final class EditorAction implements ActionListener {

    private static boolean FIRST_RUN = true;
    private final EditorPane mEditorPane = new EditorPane();
    private NotifyDescriptor mNotifyDescriptor;

    public EditorAction() {
        initDialog();

        if (FIRST_RUN) {
            FIRST_RUN = false;
            Jota.getInstance().getGlobalState().addListener(gsce -> {
                mEditorPane.load(gsce.getValue());
                actionPerformed(null);
            }, Jota.GSC_EDITOR);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            DialogDisplayer.getDefault().notify(mNotifyDescriptor);
        });

    }

    private void initDialog() {
        var dialogPanel = new FxDialogPanel() {
            @Override
            protected void fxConstructor() {
                setScene(new Scene(mEditorPane));
            }
        };

        dialogPanel.initFx();
        dialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(800, 600));

        mNotifyDescriptor = new NotifyDescriptor(
                dialogPanel,
                Bundle.CTL_EditorAction(),
                NotifyDescriptor.PLAIN_MESSAGE,
                NotifyDescriptor.PLAIN_MESSAGE,
                new String[]{Dict.CLOSE.toString()},
                Dict.CLOSE.toString()
        );
    }
}
