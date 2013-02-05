package mpi.eudico.client.annotator.layout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.SoftBevelBorder;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.ResizeComponent;
import mpi.eudico.client.annotator.interlinear.edit.InterlinearEditor;

public class InterlinearizationManager implements ModeLayoutManager{
	
	private ViewerManager2 viewerManager;
	private ElanLayoutManager layoutManager;
	private Container container;
	// store for restoring orig values
	int origMediaAreaWidth = ElanLayoutManager.MASTER_MEDIA_WIDTH;
	int origMediaAreaHeight = ElanLayoutManager.MASTER_MEDIA_HEIGHT;
	// components
	private ResizeComponent leftRightResizer;
	private ResizeComponent topBottomResizer;
	
	private JPanel leftPanel;
	private JPanel topPanel;
	private JComponent interPanel;

	/**
	 * Creates an instance of the InterlinearizationManager
	 * 
	 * @param viewerManager
	 * @param elanLayoutManager
	 */
    public InterlinearizationManager(ViewerManager2 viewerManager, ElanLayoutManager elanLayoutManager) {
        this.viewerManager = viewerManager;
        this.layoutManager = elanLayoutManager;
        
        container = layoutManager.getContainer();
        leftPanel = new JPanel();
        topPanel = new JPanel();
        interPanel = new InterlinearEditor(viewerManager.getTranscription());
    }

	
	public void add(Object object) {
		// TODO Auto-generated method stub
		
	}
	
	public void remove(Object object) {
		// TODO Auto-generated method stub
		
	}
	
	public void doLayout() {
		int containerWidth 		= container.getWidth();
	    int containerHeight 	= container.getHeight();
	    int mediaAreaWidth 		= layoutManager.getMediaAreaWidth();
	    int mediaAreaHeight     = layoutManager.getMediaAreaHeight();
	    int visibleMediaX 		= ElanLayoutManager.CONTAINER_MARGIN;
		// resize divider       
        int lrDivX = visibleMediaX + mediaAreaWidth + ElanLayoutManager.CONTAINER_MARGIN;
        int lrDivY = containerHeight - leftRightResizer.getPreferredSize().height; 

        leftRightResizer.setBounds(lrDivX, lrDivY, leftRightResizer.getPreferredSize().width,  
        		leftRightResizer.getPreferredSize().height);
		
        int tbDivX = lrDivX + leftRightResizer.getWidth();
        int tbDivY = 2 * ElanLayoutManager.CONTAINER_MARGIN + mediaAreaHeight;
        
        topBottomResizer.setBounds(tbDivX, tbDivY, containerWidth - tbDivX, topBottomResizer.getPreferredSize().height);
        
        leftPanel.setBounds(ElanLayoutManager.CONTAINER_MARGIN, ElanLayoutManager.CONTAINER_MARGIN, 
        		lrDivX - ElanLayoutManager.CONTAINER_MARGIN, containerHeight - (2 * ElanLayoutManager.CONTAINER_MARGIN));
        
        int rightCompX = tbDivX + ElanLayoutManager.CONTAINER_MARGIN;
        int rightCompW = containerWidth - rightCompX - ElanLayoutManager.CONTAINER_MARGIN;
        topPanel.setBounds(rightCompX, ElanLayoutManager.CONTAINER_MARGIN, 
        		rightCompW, tbDivY - ElanLayoutManager.CONTAINER_MARGIN);
        
        int interCompY = tbDivY + topBottomResizer.getHeight() + ElanLayoutManager.CONTAINER_MARGIN;
        int interCompH = containerHeight - interCompY - ElanLayoutManager.CONTAINER_MARGIN;
        interPanel.setBounds(rightCompX, interCompY, rightCompW, interCompH);
	}
	
	public void updateLocale() {
		// TODO Auto-generated method stub
		
	}
	
	public void clearLayout() {
		// remove all interlinear components
		container.remove(leftRightResizer);
		container.remove(topBottomResizer);
        container.remove(leftPanel);
        container.remove(topPanel);
        container.remove(interPanel);
		// restore some values
		layoutManager.setMediaAreaWidth(origMediaAreaWidth);
		layoutManager.setMediaAreaHeight(origMediaAreaHeight);
		// add the video players
		PlayerLayoutModel model = null;
		List visuals = layoutManager.getPlayerList();
		for (int i = 0; i < visuals.size(); i++) {
			model = (PlayerLayoutModel) visuals.get(i);
			if (model.isVisual() && model.isAttached()) {
				container.add(model.visualComponent);
			}
		}

		container.repaint();
	}
	
	public void initComponents() {
		origMediaAreaWidth = layoutManager.getMediaAreaWidth();
		origMediaAreaHeight = layoutManager.getMediaAreaHeight();
		// remove remaining video player components
		PlayerLayoutModel model = null;
		List visuals = layoutManager.getPlayerList();
		for (int i = 0; i < visuals.size(); i++) {
			model = (PlayerLayoutModel) visuals.get(i);
			if (model.isVisual() && model.isAttached()) {
				container.remove(model.visualComponent);
			}
		}
		// add mode specific components
        // divider component
        leftRightResizer = new ResizeComponent(layoutManager, SwingConstants.HORIZONTAL);
        leftRightResizer.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        leftRightResizer.setPreferredSize(new Dimension(8,container.getHeight()));
        Component n = leftRightResizer.getComponent(0);
        leftRightResizer.remove(n);
        leftRightResizer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = gbc.PAGE_END;
        gbc.weighty = 1.0; 
        leftRightResizer.add(n, gbc);
        
    	container.add(leftRightResizer);
    	
    	topBottomResizer = new ResizeComponent(layoutManager, SwingConstants.VERTICAL);
    	topBottomResizer.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    	topBottomResizer.setPreferredSize(new Dimension(container.getWidth(), 8));
        n = topBottomResizer.getComponent(0);
        topBottomResizer.remove(n);
        topBottomResizer.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.anchor = gbc.PAGE_END;
        gbc.weightx = 1.0; 
        topBottomResizer.add(n, gbc);
        
        container.add(topBottomResizer);
        
        leftPanel.setBackground(new Color(220, 220, 255));
        topPanel.setBackground(new Color(220, 255, 220));
        interPanel.setBackground(Color.WHITE);
        container.add(leftPanel);
        container.add(topPanel);
        container.add(interPanel);
        // readPreferences
        doLayout();
	}

	public void enableOrDisableMenus(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	public void detach(Object object) {
		// TODO Auto-generated method stub
		
	}
	
	public void attach(Object object) {
		// TODO Auto-generated method stub
		
	}

	public void preferencesChanged() {
		// TODO Auto-generated method stub
		
	}
	
	public void cleanUpOnClose() {
		// TODO Auto-generated method stub		
	}

	public void shortcutsChanged() {
		// TODO Auto-generated method stub
		
	}


	public void createAndAddViewer(String viewerName) {
		// TODO Auto-generated method stub
		
	}

	public boolean destroyAndRemoveViewer(String viewerName) {
		return false;
		
	}

	@Override
	public void isClosing() {
		// TODO Auto-generated method stub
		
	}
} 


