package mpi.eudico.p2p;

import java.util.Vector;
import java.util.Iterator;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.client.annotator.*;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;

public class P2P2Here implements ControllerListener, SelectionListener, ActiveAnnotationListener, ACMEditListener {
	public final static String SET_MEDIA_TIME = "setMediaTime";
	public final static String SET_SELECTION = "setSelection";
	public final static String SET_ACTIVE_ANNOTATION = "setActiveAnnotation";
	public final static String CHANGE_ANNOTATION_VALUE = "changeAnnValue";

	private ElanP2P p2p;
	private ViewerManager2 viewerManager;
	private ElanFrame2 frame;
	private boolean deaf;

	public P2P2Here(ElanP2P p2p, ViewerManager2 viewerManager, ElanFrame2 frame) {
		this.p2p = p2p;
		this.viewerManager = viewerManager;
		this.frame = frame;

		viewerManager.connectListener(this);
	}

	public void controllerUpdate(ControllerEvent event) {
		// ignore time events while media player is playing
		if (deaf) {
			return;
		}

		if (event instanceof TimeEvent) {
			long time = viewerManager.getMasterMediaPlayer().getMediaTime();
			// set time in ElanP2P
			p2p.sendElanCommand(SET_MEDIA_TIME, Long.toString(time));
		} else if (event instanceof StartEvent) {
			deaf = true;
		} else if (event instanceof StopEvent) {
			deaf = false;
		}
	}

	public void handleCommand(String command, String parameter1, String parameter2) {
		if (command.equals(SET_MEDIA_TIME)) {
			long time = Long.parseLong(parameter1);
			viewerManager.getMasterMediaPlayer().setMediaTime(time);
		} else if (command.equals(SET_SELECTION)) {
			long beginTime = Long.parseLong(parameter1);
			long endTime = Long.parseLong(parameter2);
			viewerManager.getSelection().setSelection(beginTime, endTime);
		} else if (command.equals(SET_ACTIVE_ANNOTATION)) {
			//int i = Integer.parseInt(parameter1);
			//Annotation ann = getAnnotationForIndex(i);
			String name = parameter1;
			int i = Integer.parseInt(parameter2);
			Annotation ann = getAnnotationForIndexOnTier(name, i);
			viewerManager.getActiveAnnotation().setAnnotation(ann);
		} else if (command.equals(CHANGE_ANNOTATION_VALUE)) {
			int i = Integer.parseInt(parameter1);
			Annotation ann = getAnnotationForIndex(i);
			String value = parameter2;
			if (ann != null) {
				ann.setValue(value);
			}			
		}

		System.out.println("received command: " + command + " par1 = " + parameter1 + " par2 = " + parameter2 + "\n");
	}

	public void updateSelection() {
		long beginTime = viewerManager.getSelection().getBeginTime();
		long endTime = viewerManager.getSelection().getEndTime();
		// set selection in ElanP2P
		p2p.sendElanCommand(SET_SELECTION, Long.toString(beginTime), Long.toString(endTime));

	}

	public void updateActiveAnnotation() {
		Annotation annotation = viewerManager.getActiveAnnotation().getAnnotation();
		//int index = getIndexForAnnotation(annotation);
		int index = getIndexOnTier(annotation);
		String tiername = "";
		if (annotation != null) {
			tiername = annotation.getTier().getName();		
		}
		// set active annotation in ElanP2P
		//p2p.sendElanCommand(SET_ACTIVE_ANNOTATION, Integer.toString(index));
		p2p.sendElanCommand(SET_ACTIVE_ANNOTATION, tiername, Integer.toString(index));
	}

	public void ACMEdited(ACMEditEvent event) {
		// send edit events to ElanP2P
		System.out.println("ACM: " + event);
		switch (event.getOperation()) {
				case ACMEditEvent.ADD_TIER:

					break;

				case ACMEditEvent.REMOVE_TIER:

					break;

				case ACMEditEvent.CHANGE_TIER:

					break;

				case ACMEditEvent.ADD_ANNOTATION_HERE:

					break;

				case ACMEditEvent.ADD_ANNOTATION_BEFORE:

				// fall through
				//break;
				case ACMEditEvent.ADD_ANNOTATION_AFTER:

					break;

				case ACMEditEvent.REMOVE_ANNOTATION:

					break;

				case ACMEditEvent.CHANGE_ANNOTATION_TIME:

					break;
					
				case ACMEditEvent.CHANGE_ANNOTATION_VALUE:

					if (event.getSource() instanceof Annotation) {
						Annotation a = (Annotation) event.getSource();
						String value = a.getValue();
						int index = getIndexForAnnotation(a);
						p2p.sendElanCommand(CHANGE_ANNOTATION_VALUE, Integer.toString(index), value);
					}

					break;

				default:
					break;
				}
	}

	// wordt aangeroepen vanuit ElanP2P, not needed, is done in ElanP2P
	public void openEAF(String fullPath) {
		frame.openEAF(fullPath);
	}

	// ??
	// wordt aangeroepen vanuit ElanP2P
	public void setVideoPointer(float relX, float relY) {

	}

	private int getIndexForAnnotation(Annotation annotation) {
		TranscriptionImpl transcription = (TranscriptionImpl) viewerManager.getTranscription();

		int index = 0;

		try {
			Vector tiers = transcription.getTiers();
			Iterator tierIter = tiers.iterator();
			while (tierIter.hasNext()) {
				Vector annots = ((TierImpl) tierIter.next()).getAnnotations();
				Iterator annIter = annots.iterator();
				while (annIter.hasNext()) {
					if (annIter.next() == annotation) {
						return index;
					}
					index++;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return 0;
	}

	private Annotation getAnnotationForIndex(int index) {
		TranscriptionImpl transcription = (TranscriptionImpl) viewerManager.getTranscription();

		Annotation annot = null;
		int counter = 0;

		try {
			Vector tiers = transcription.getTiers();
			Iterator tierIter = tiers.iterator();
			while (tierIter.hasNext()) {
				Vector annots = ((TierImpl) tierIter.next()).getAnnotations();
				Iterator annIter = annots.iterator();
				while (annIter.hasNext()) {
					annot = (Annotation) annIter.next();

					if (counter == index) return annot;
					counter++;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
	
	private int getIndexOnTier(Annotation annotation) {
		if (annotation == null) {
			return -1;
		}
		//DobesTranscription transcription = (DobesTranscription) viewerManager.getTranscription();
		TierImpl tier = (TierImpl)annotation.getTier();
		int index = 0;

		Vector annots = tier.getAnnotations();
		Iterator annIter = annots.iterator();
		while (annIter.hasNext()) {
			if (annIter.next() == annotation) {
				return index;
			}
			index++;
		}
		
		return -1;
	}
	
	private Annotation getAnnotationForIndexOnTier (String name, int index) {
		if (name == null || name.length() == 0 || index < 0) {
			return null;
		}
		TranscriptionImpl transcription = (TranscriptionImpl) viewerManager.getTranscription();
		Annotation annot = null;
		int counter = 0;

		TierImpl tier = (TierImpl)transcription.getTierWithId(name);
		Vector annots = tier.getAnnotations();
		Iterator annIter = annots.iterator();
		while (annIter.hasNext()) {
			annot = (Annotation) annIter.next();

			if (counter == index) return annot;
				counter++;
		}
		
		return null;
	}

}
