package mpi.eudico.client.annotator.search.query.viewer;

import mpi.eudico.client.annotator.Constants;
import mpi.search.content.model.CorpusType;

import mpi.search.content.query.model.Constraint;

import mpi.search.content.query.viewer.ConstraintRenderer;
import mpi.search.content.query.viewer.QueryPanel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;


/**
 * Subclass for ELAN so that ELAN's fonts etc. can be applied.
 * 
 * @author HS
 * @version Aug 2008
  */
public class ElanQueryPanel extends QueryPanel {
    /**
     * Creates a new ElanQueryPanel instance
     *
     * @param type corpus type, is EAFType
     * @param startAction the start action
     */
    public ElanQueryPanel(CorpusType type, Action startAction) {
        super(type, startAction);
    }

    /**
     * Creates the constraints tree, using ELAN sepecific components.
     *
     * @param startAction the start action
     */
    protected void createTree(Action startAction) {   	
    	setFont(Constants.DEFAULTFONT);
        jTree = new JTree(treeModel) {
                    public boolean isPathEditable(TreePath path) {
                        return ((Constraint) path.getLastPathComponent()).isEditable();
                    }
                };
               
        jTree.setFont(getFont());
        jTree.setEditable(true);
        jTree.setCellRenderer(new ConstraintRenderer());
        jTree.setCellEditor(new ElanConstraintEditor(treeModel, type, startAction));

        //hack to kill mouse event (otherwise they would activate subcomponents of ConstraintPanel)
//        jTree.setUI(new BasicTreeUI() {
//                protected boolean startEditing(TreePath path, MouseEvent event) {
//                    return super.startEditing(path, null);
//                }
//            });

        //explicitly overwriting default height defined by Mac
        jTree.setRowHeight(0);

        jTree.setBorder(new EmptyBorder(5, 5, 5, 5));
        jTree.setOpaque(false);

        setFont(getFont().deriveFont(Font.PLAIN));
        setLayout(new BorderLayout());
        add(jTree, BorderLayout.CENTER);

        jTree.startEditingAtPath(jTree.getPathForRow(0));

        treeModel.addTreeModelListener(new TreeModelListener() {
                public void treeNodesInserted(final TreeModelEvent e) {
                    try {
                        //editing has to start after JTree has updated itself. Otherwise one gets a bad layout. 
                        javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable() {
                                public void run() {
                                    jTree.startEditingAtPath(e.getTreePath()
                                                              .pathByAddingChild(e.getChildren()[0]));
                                }
                            });
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param e DOCUMENT ME!
                 */
                public void treeNodesChanged(TreeModelEvent e) {
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param e DOCUMENT ME!
                 */
                public void treeStructureChanged(TreeModelEvent e) {
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param e DOCUMENT ME!
                 */
                public void treeNodesRemoved(TreeModelEvent e) {
                    jTree.startEditingAtPath(e.getTreePath());
                }
            });
    }
}
