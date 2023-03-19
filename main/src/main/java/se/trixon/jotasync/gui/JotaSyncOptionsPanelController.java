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
package se.trixon.jotasync.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.TopLevelRegistration(
        categoryName = "#OptionsCategory_Name_JotaSync",
        iconBase = "se/trixon/jotasync/gui/logo.png",
        keywords = "#OptionsCategory_Keywords_JotaSync",
        keywordsCategory = "JotaSync",
        position = 0
)
@org.openide.util.NbBundle.Messages({"OptionsCategory_Name_JotaSync=JotaSync", "OptionsCategory_Keywords_JotaSync=rsync path"})
public final class JotaSyncOptionsPanelController extends OptionsPanelController {

    private JotaSyncPanel mPanel;
    private final PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);
    private boolean mChanged;

    @Override
    public void update() {
        getPane().load();
        mChanged = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getPane().store();
                mChanged = false;
            }
        });
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPane().valid();
    }

    @Override
    public boolean isChanged() {
        return mChanged;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPane();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        mPropertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        mPropertyChangeSupport.removePropertyChangeListener(l);
    }

    private JotaSyncPanel getPane() {
        if (mPanel == null) {
            mPanel = new JotaSyncPanel(this);
        }
        return mPanel;
    }

    void changed() {
        if (!mChanged) {
            mChanged = true;
            mPropertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        mPropertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

}
