package mpi.eudico.client.annotator.commands;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.ClosableFrame;

import mpi.eudico.client.util.TierTree;

import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.server.corpora.util.ACMEditableDocument;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 */
public class TierDependenciesCA extends CommandAction implements ACMEditListener {
	private JFrame dependencyFrame;
	private int locationX = -1;
	private int locationY = -1;
	private int sizeWidth = -1;
	private int sizeHeight = -1;

	private JTree tree;

	/**
	 * Creates a new TierDependenciesCA instance
	 *
	 * @param theVM DOCUMENT ME!
	 */
	public TierDependenciesCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.TIER_DEPENDENCIES);

		try {
			((ACMEditableDocument) vm.getTranscription()).addACMEditListener(
				(ACMEditListener) this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	protected void newCommand() {
		command =
			ELANCommandFactory.createCommand(
				vm.getTranscription(),
				ELANCommandFactory.TIER_DEPENDENCIES);
	}

	/**
	 *
	 */
	protected Object getReceiver() {
		return null;
	}

	/**
	 * Returns null, no arguments need to be passed.
	 *
	 * @return DOCUMENT ME!
	 */
	protected Object[] getArguments() {
		Object[] args = new Object[1];
		args[0] = getDependencyFrame();

		return args;
	}
	
	/**
	 * Give access to the frame.
	 * @return the dependency frame or null
	 */
	public JFrame getFrame() {
		return dependencyFrame;
	}

	private JFrame getDependencyFrame() {
		if (dependencyFrame == null) {
			createDependencyFrame();
			
			Object val = Preferences.get("DependenciesFrame.Location", null);
			if (val instanceof Point) {
				Point p = (Point) val;
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				int x = p.x <= screen.width - 50 ? p.x : screen.width - 50;
				int y = p.y <= screen.height - 50 ? p.y : screen.height - 50;
				dependencyFrame.setLocation(x, y);
			}
			
			val = Preferences.get("DependenciesFrame.Size", null);
			if (val instanceof Dimension) {
				dependencyFrame.setSize((Dimension) val);
			}
		}

		return dependencyFrame;
	}

	private void createDependencyFrame() {
		try {
			TierTree tTree = new TierTree(vm.getTranscription());
			JTree tree = initTree(tTree.getTree());       
	        
			dependencyFrame = new ClosableFrame(ElanLocale.getString("Tier Dependencies"));
			dependencyFrame.getContentPane().add(new JScrollPane(tree));
			
			if ((locationX != -1)
				&& (locationY != -1)
				&& (sizeWidth != -1)
				&& (sizeHeight != -1)) {
				dependencyFrame.setLocation(locationX, locationY);
				dependencyFrame.setSize(sizeWidth, sizeHeight);
			}

			if ((dependencyFrame.getHeight() < 100) || (dependencyFrame.getWidth() < 133)) {
				dependencyFrame.setSize(133, 200);
			}
			addCloseActions();
			
			updateLocale();
		}
		catch (Exception ex) {
			System.out.println("Couldn't create dependencyFrame.");
			//ex.printStackTrace();
		}
	}

	/**
	 * Initialize a JTree.
	 * 
	 * @param rootNode the root node
	 * 
	 * @return a configured JTree
	 */
	private JTree initTree(DefaultMutableTreeNode rootNode) {
		JTree tree = new JTree(rootNode);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);
        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        
		return tree;
	}
	
	/**
	 * Returns the tree object.
	 * 
	 * @return the tree, can be null.
	 */
	public JTree getTree() {
		return tree;
	}

	//needed to set title of dialog
	public void updateLocale() {
		super.updateLocale();

		if (dependencyFrame != null) {
			dependencyFrame.setTitle(ElanLocale.getString("Menu.View.DependenciesDialog"));
			dependencyFrame.repaint();
		}
	}

    /**
     * Adds a listener to the closing action. The window will be removed as ACMEditListener
     * and the location will be stored as preference.
     */
    protected void addCloseActions() {
        if (dependencyFrame != null) {
	        dependencyFrame.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                ((ACMEditableDocument) vm.getTranscription()).removeACMEditListener(
	        				(ACMEditListener) TierDependenciesCA.this);
	                Point p = dependencyFrame.getLocationOnScreen();
	                Dimension d = dependencyFrame.getSize();
	                Preferences.set("DependenciesFrame.Location", p, null, false, false);
	                Preferences.set("DependenciesFrame.Size", d, null, false, false);
	            }
	        });
        }
    }
        
	/**
	 * DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void ACMEdited(ACMEditEvent e) {
		switch (e.getOperation()) {
			case ACMEditEvent.ADD_TIER :
			case ACMEditEvent.REMOVE_TIER :
			case ACMEditEvent.CHANGE_TIER :
				{
					if (dependencyFrame == null) {
						break;
					}

					boolean bVisible = dependencyFrame.isVisible();

					//remember last position before disposing
					locationX = (int) dependencyFrame.getLocation().getX();
					locationY = (int) dependencyFrame.getLocation().getY();
					sizeWidth = (int) dependencyFrame.getSize().getWidth();
					sizeHeight = (int) dependencyFrame.getSize().getHeight();

					dependencyFrame.dispose();

					//update tree
					createDependencyFrame();

					//if tree was visible before updating, show it again
					if ((command != null) && (bVisible == true)) {
						command.execute(getReceiver(), getArguments());
					}
				}
		}
	}
}
