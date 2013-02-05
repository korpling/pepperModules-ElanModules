package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.smfsearch.StructuredMultipleFileSearchFrame;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.ToolTipManager;


/**
 * A menu action that creates the structured search window.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class StructuredSearchMultipleMA extends FrameMenuAction implements WindowListener {
	/** count the number of open windows, after the last one is closed check the setting for 
	 * tooltips */
	private static int numWindows = 0;
	
    /**
     * Creates a new StructuredSearchMultipleMA instance
     *
     * @param name the name of the command
     * @param frame the parent frame
     */
    public StructuredSearchMultipleMA(String name, ElanFrame2 frame) {
        super(name, frame);
        numWindows++;
    }

    /**
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame searchFrame = new StructuredMultipleFileSearchFrame(frame);
        
        Object prefObj = Preferences.get("MFSearchFrame.Location", null);
        
        if (prefObj instanceof Point) {
        	Point p = (Point) prefObj;
        	final int MARGIN = 30;
        	
        	Rectangle wRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        	if (p.x < wRect.x) {
        		p.x = wRect.x;
        	} else if (p.x > wRect.width - MARGIN) {
        		p.x = wRect.width - MARGIN;
        	}
        	if (p.y < wRect.y) {
        		p.y = wRect.y;
        	} else if (p.y > wRect.height - MARGIN) {
        		p.y = wRect.height - MARGIN;
        	}
        	
        	searchFrame.setLocation(p);
        }
        
        prefObj = Preferences.get("MFSearchFrame.Size", null);
        
        if (prefObj instanceof Dimension) {
        	Dimension size = (Dimension) prefObj;
        	searchFrame.setSize(size);
        }
        
        searchFrame.addWindowListener(this);
        searchFrame.setVisible(true);
    }

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (e.getWindow() != null) {
			Preferences.set("MFSearchFrame.Location", e.getWindow().getLocation(), null, false, false);
			Preferences.set("MFSearchFrame.Size", e.getWindow().getSize(), null, false, false);
			
			e.getWindow().removeWindowListener(this);
			numWindows--;
			
			if (numWindows == 0) {
				Object ttPref = Preferences.get("UI.ToolTips.Enabled", null);
				
				if (ttPref instanceof Boolean) {
					if ( ! ((Boolean) ttPref) ) {// tooltips globally disabled
						// the search might have enabled tooltips
						if (ToolTipManager.sharedInstance().isEnabled()) {
							ToolTipManager.sharedInstance().setEnabled(false);
						}
					}
				}
			}
		}
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
