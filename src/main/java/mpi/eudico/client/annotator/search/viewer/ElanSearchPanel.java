/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.search.viewer;

import java.awt.event.ActionEvent;

import java.io.IOException;

import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.grid.GridViewerTableModel;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.search.model.EAFType;
import mpi.eudico.client.annotator.search.model.ElanSearchEngine;
import mpi.eudico.client.annotator.search.model.ElanType;
import mpi.eudico.client.annotator.search.query.viewer.ElanQueryPanel;
import mpi.eudico.client.annotator.search.result.viewer.ElanResultViewer;
import mpi.eudico.client.annotator.util.ElanFileFilter;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.viewer.AbstractViewer;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

import mpi.search.SearchLocale;

import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.xml.Query2Xml;
import mpi.search.content.query.xml.Xml2Query;
import mpi.search.content.result.model.ContentResult;
import mpi.search.content.viewer.AbstractComplexSearchPanel;

import mpi.search.model.DefaultSearchController;
import mpi.search.query.model.Query;


/**
 * The SearchDialog is a custom dialog for searching a string in a Tier It can
 * be invoked by the EudicoAnnotationFrame.
 *
 * @author Alexander Klassmann
 * @version June 2002
 */
public class ElanSearchPanel extends AbstractComplexSearchPanel
    implements ElanLocaleListener, ACMEditListener {
    static final String LAST_DIR_KEY = "SearchLastDir";
    private static final String fileExtension = "eaq";

    /** Holds value of property DOCUMENT ME! */
    private static final FileFilter resFileFilter = ElanFileFilter.createFileFilter(ElanFileFilter.EAQ_TYPE);

    /** DOCUMENT ME! */
    protected final Action replaceAction;

    /** Holds value of property DOCUMENT ME! */
    private final JLabel infoLabel = new JLabel();

    /** Holds value of property DOCUMENT ME! */
    private final ViewerManager2 viewerManager;

    /**
     * Constructor that has a ViewerManager as an argument. <b>Note: </b>the
     * viewer manager is currently needed to be able to connect and disconnect
     * as a listener. The SearchDialog is not an AbstractViewer and is
     * (currently) not created by the viewer manager, so it has to be
     * connected seperately.
     *
     * @param viewerManager the viewermanager for this document/frame
     */
    public ElanSearchPanel(ViewerManager2 viewerManager) {
        super();
        this.viewerManager = viewerManager;
        queryPanel = new ElanQueryPanel(new ElanType(
                    viewerManager.getTranscription()), startAction);
        
        resultViewer = viewerManager.createSearchResultViewer();

        ((ElanResultViewer) resultViewer).setColumnVisible(GridViewerTableModel.FILENAME,
            false);
        ((ElanResultViewer) resultViewer).setColumnVisible(GridViewerTableModel.LEFTCONTEXT,
            false);
        ((ElanResultViewer) resultViewer).setColumnVisible(GridViewerTableModel.RIGHTCONTEXT,
            false);
        ((ElanResultViewer) resultViewer).setColumnVisible(GridViewerTableModel.TIERNAME,
            false);

        // Initialize Components
        saveAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Save"));
        readAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Open"));
        exportAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Export"));

        replaceAction = new AbstractAction(SearchLocale.getString(
                    "Action.Replace")) {
                    public void actionPerformed(ActionEvent e) {
                        replace();
                    }
                };
        replaceAction.putValue(Action.SHORT_DESCRIPTION,
            SearchLocale.getString("Action.Tooltip.Replace"));
        // set icon? the standard icon is not very intuitive?
        /*
        try {
        	ImageIcon icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Replace16.gif"));
        	replaceAction.putValue(Action.SMALL_ICON, icon);
        } catch (Exception ex) {
        	// any exception, icon not crucial
        }
        */
        makeLayout();

        searchEngine = new DefaultSearchController(this,
                new ElanSearchEngine(this, viewerManager.getTranscription()));
        searchEngine.setProgressListener(progressViewer);

        try {
            viewerManager.getTranscription().addACMEditListener(this);
        } catch (Exception e) {
        }
    }

    /**
     * If the annotation document is modified (annotations changed, tier
     * removed etc.) try to establish if re-search is necessary; In most cases
     * YES, because ACMEditEvent.getOperation() is not very precise. In that
     * case all result are flagged "modified" and re-searched as soon as they
     * are visible (e.g. by going back and forth within results)
     *
     * @param e DOCUMENT ME!
     */
    public void ACMEdited(ACMEditEvent e) {
        if (queryManager.size() == 0) {
            return;
        }

        if ((e.getOperation() == ACMEditEvent.REMOVE_ANNOTATION) ||
                (e.getOperation() == ACMEditEvent.REMOVE_TIER) ||
                (e.getOperation() == ACMEditEvent.CHANGE_ANNOTATION_VALUE) ||
                (e.getOperation() == ACMEditEvent.CHANGE_ANNOTATIONS)) {
            Object o = (e.getModification() != null) ? e.getModification()
                                                     : e.getInvalidatedObject();

            String tierName = null;

            if (o instanceof TierImpl) {
                try {
                    tierName = ((TierImpl) o).getName();
                } catch (Exception ee) {
                }
            }

            if (o instanceof AbstractAnnotation) {
                try {
                    tierName = ((AbstractAnnotation) o).getTier().getName();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

            // set flag on results which contain modified tier to status
            // 'MODIFIED'
            for (int i = 1; i <= queryManager.size(); i++) {
                ContentResult result = (ContentResult) queryManager.getQuery(i)
                                                                   .getResult();

                if (tierName != null) {
                    System.out.println("Edited tier: " + tierName);

                    String[] tierNames = result.getTierNames();

                    for (int j = 0; j < tierNames.length; j++) {
                        if (tierName.equals(tierNames[j])) {
                            result.setStatus(ContentResult.MODIFIED);

                            break;
                        }
                    }
                }
                // unknown which tier was changed => flag all results
                else {
                    System.out.println("Edited tier not known");
                    result.setStatus(ContentResult.MODIFIED);
                }
            }

            if ((queryManager.getCurrentQuery().getResult() != null) &&
                    (queryManager.getCurrentQuery().getResult().getStatus() == ContentResult.MODIFIED)) {
                startSearch();
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void executionStarted() {
        super.executionStarted();
        updateResultViewer();
        infoLabel.setText(" " + ElanLocale.getString("SearchDialog.FoundNone"));
    }

    /**
     * DOCUMENT ME!
     */
    public void executionStopped() {
        super.executionStopped();
        viewerManager.setControllersForViewer((AbstractViewer) resultViewer,
            ((ContentResult) searchEngine.getResult()).getTierNames());
    }

    /**
     * handles PatternSyntaxException
     *
     * @param e DOCUMENT ME!
     */
    public void handleException(Exception e) {
        if (e instanceof PatternSyntaxException) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                SearchLocale.getString("Search.Exception.Formulation"),
                JOptionPane.ERROR_MESSAGE, null);
            searchEngine.stopExecution();
        } else {
            super.handleException(e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void updateLocale() {
        repaint();
    }

    protected Query getQuery() {
        Query query = super.getQuery();

        if (query != null) {
            query.getResult().setPageSize(Integer.MAX_VALUE);
        }

        return query;
    }

    /**
     * Disconnect from the viewermanager and dispose the dialog.
     */
    protected void close() {
        if (viewerManager != null) {
            viewerManager.destroyViewer((AbstractViewer) resultViewer);
        }

        //((Window) getTopLevelAncestor()).dispose();
    }

    /**
     * DOCUMENT ME!
     */
    protected void export() {
        JDialog dialog = new ExportResultDialog((JFrame) SwingUtilities.getRoot(
                    this), true, viewerManager.getTranscription(), (ContentQuery) queryManager.getCurrentQuery());
        dialog.setVisible(true);
    }

    /**
     * DOCUMENT ME!
     */
    protected void read() {
        String lastDir = (String) Preferences.get(LAST_DIR_KEY,
                viewerManager.getTranscription());
        FileChooser fc = new FileChooser(ElanSearchPanel.this);
        fc.createAndShowFileDialog(null, FileChooser.OPEN_DIALOG, FileExtension.EAQ_EXT, LAST_DIR_KEY);
        if(fc.getSelectedFile() != null){
        	String selectedFile = fc.getSelectedFile().toString();
//        	if (!selectedFile.endsWith(fileExtension)) {
//              selectedFile += ("." + fileExtension);
//        	}
        	ContentQuery query = new ContentQuery(null, new EAFType());
            try {
                Xml2Query.translate(selectedFile, query);
                addQuery(query);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    SearchLocale.getString("Search.Exception.QueryReadError"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void save() {
        String lastDir = (String) Preferences.get(LAST_DIR_KEY,
                viewerManager.getTranscription());
        FileChooser fc = new FileChooser(this);
        fc.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, FileExtension.EAQ_EXT, LAST_DIR_KEY);
        if(fc.getSelectedFile() != null){
        	String selectedFile = fc.getSelectedFile().toString();
            // make sure selectedFile ends with ".eaq"
            if (!selectedFile.endsWith(fileExtension)) {
                selectedFile += ("." + fileExtension);
            }

            try {
                Query2Xml.translate(selectedFile, (ContentQuery) queryManager.getCurrentQuery());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    SearchLocale.getString("Search.Exception.QuerySaveError"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void updateActions() {
        super.updateActions();
        replaceAction.setEnabled(exportAction.isEnabled());
    }

    /**
     * Disconnects from the ViewerManager and calls super.userQuit().
     *
     * @see mpi.search.content.viewer.AbstractComplexSearchPanel#userQuit()
     */
    protected void userQuit() {
        close();
        super.userQuit();
    }

    private void replace() {
        String replaceString = JOptionPane.showInputDialog(this,
                SearchLocale.getString("ReplaceDialog.Message"),
                SearchLocale.getString("ReplaceDialog.Title"),
                JOptionPane.PLAIN_MESSAGE);

        if (replaceString != null) {
            Command command = ELANCommandFactory.createCommand(viewerManager.getTranscription(),
                    ELANCommandFactory.REPLACE);
            Object[] args = new Object[] {
                    queryManager.getCurrentQuery().getResult(), replaceString
                };
            command.execute(viewerManager.getTranscription(), args);
        }
    }

    /**
             *
             */
    private void updateResultViewer() {
        if (queryManager.hasQuery()) {
            ContentQuery query = (ContentQuery) queryManager.getCurrentQuery();
            boolean multipleTiers = query.getAnchorConstraint().getTierNames().length > 1;
            ((ElanResultViewer) resultViewer).setColumnVisible(GridViewerTableModel.TIERNAME,
                multipleTiers);
        }
    }
}
