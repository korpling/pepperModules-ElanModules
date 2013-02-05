package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class InterlinearEditor extends AbstractViewer 
implements ComponentListener {
	private TranscriptionImpl transcription = null;
	
	public InterlinearEditor(Transcription transcription) {
		this.transcription = (TranscriptionImpl) transcription;
		initViewer();
		
	}
	
	private void initViewer() {
		setBackground(Color.WHITE);
		// set layout
		// add components
		addComponentListener(this);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	public void controllerUpdate(ControllerEvent event) {
		// TODO Auto-generated method stub

	}

	public void updateSelection() {
		// TODO Auto-generated method stub

	}

	public void updateActiveAnnotation() {
		// TODO Auto-generated method stub

	}

	public void updateLocale() {
		// TODO Auto-generated method stub

	}

	public void preferencesChanged() {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent e) {
		repaint();
	}

	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

}
