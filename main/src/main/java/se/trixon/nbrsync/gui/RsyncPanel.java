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
package se.trixon.nbrsync.gui;

import se.trixon.nbrsync.Options;

final class RsyncPanel extends javax.swing.JPanel {

    private final RsyncOptionsPanelController mController;
    private final Options mOptions = Options.getInstance();

    RsyncPanel(RsyncOptionsPanelController controller) {
        mController = controller;
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rsyncFileChooserPanel = new se.trixon.almond.util.swing.dialogs.FileChooserPanel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/nbrsync/gui/Bundle"); // NOI18N
        rsyncFileChooserPanel.setHeader(bundle.getString("RsyncPanel.rsyncFileChooserPanel.header")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rsyncFileChooserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rsyncFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        rsyncFileChooserPanel.setPath(mOptions.getRsyncPath());
    }

    void store() {
        mOptions.setRsyncPath(rsyncFileChooserPanel.getPath());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private se.trixon.almond.util.swing.dialogs.FileChooserPanel rsyncFileChooserPanel;
    // End of variables declaration//GEN-END:variables
}
