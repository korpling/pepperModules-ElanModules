package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.IndeterminateProgressMonitor;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * A Command that tokenizes the contents of the annotations a source tier 
 * new annotations on a destination tier.
 * 
 * @author Han Sloetjes
 */
public class TokenizeCommand implements UndoableCommand {
	private String commandName;
	private static final Logger LOG = Logger.getLogger(TokenizeCommand.class.getName());
	
	// store state 
	private TranscriptionImpl transcription;
	private TierImpl sourceTier;
	private TierImpl destTier;
	private String delimiter;
	private boolean preserve;
	private boolean createEmpty; 
	
	// store for undo
	/** backup data of existing annotations on the destination tier */
	private ArrayList existAnnotations;
	/** store the data of the newly created annotations */
	private ArrayList newAnnotationsNodes;
	// store for redo
	/** a list of source annotations that have been tokenized */
	private ArrayList completedTokenizations;

	/**
	 * Creates a new TokenizeCommand instance.
	 *
	 * @param name the name of the command
	 */
	public TokenizeCommand(String name) {
		commandName = name;
	}
	
	/**
	 * Undo the changes made by this command.
	 */
	public void undo() {
		if (transcription == null || sourceTier == null || destTier == null) {
			return;
		}
		int curPropMode = 0;

		curPropMode = transcription.getTimeChangePropagationMode();

		if (curPropMode != Transcription.NORMAL) {
			transcription.setTimeChangePropagationMode(Transcription.NORMAL);
		}

		transcription.setNotifying(false);
		
		setWaitCursor(true);
		
		// delete created annotations
		if (completedTokenizations.size() > 0) {
			AnnotationDataRecord srcRecord;
			AbstractAnnotation srcAnn;
			AbstractAnnotation destAnn;
			Vector childrenOnDest;
			for (int i = 0; i < completedTokenizations.size(); i++) {
				srcRecord = (AnnotationDataRecord)completedTokenizations.get(i);
				srcAnn = (AbstractAnnotation)sourceTier.getAnnotationAtTime(srcRecord.getBeginTime());
				childrenOnDest = srcAnn.getChildrenOnTier(destTier);
				for (int j = 0; j < childrenOnDest.size(); j++) {
					destAnn = (AbstractAnnotation)childrenOnDest.get(j);

					destTier.removeAnnotation(destAnn);					
				}
			}
		}
		// recreate annotations that have been overwritten 
		if (!preserve && existAnnotations.size() > 0) {
			AnnotationRecreator.createAnnotationsSequentially(transcription, existAnnotations);
		}
		transcription.setNotifying(true);
		
		setWaitCursor(false);
		
		// restore the time propagation mode
		transcription.setTimeChangePropagationMode(curPropMode);
	}

	/**
	 * Redo the changes made by this command.
	 */
	public void redo() {
		if (transcription == null || sourceTier == null || destTier == null) {
			return;
		}
		int curPropMode = 0;

		curPropMode = transcription.getTimeChangePropagationMode();

		if (curPropMode != Transcription.NORMAL) {
			transcription.setTimeChangePropagationMode(Transcription.NORMAL);
		}

		transcription.setNotifying(false);
		
		setWaitCursor(true);
		
		if (completedTokenizations.size() > 0) {
			if (!preserve) {
				AnnotationDataRecord srcRecord;
				AbstractAnnotation srcAnn;
				AbstractAnnotation destAnn;
				Vector childrenOnDest;
				for (int i = 0; i < completedTokenizations.size(); i++) {
					srcRecord = (AnnotationDataRecord)completedTokenizations.get(i);
					srcAnn = (AbstractAnnotation)sourceTier.getAnnotationAtTime(srcRecord.getBeginTime());
					childrenOnDest = srcAnn.getChildrenOnTier(destTier);
					for (int j = 0; j < childrenOnDest.size(); j++) {
						destAnn = (AbstractAnnotation)childrenOnDest.get(j);

						destTier.removeAnnotation(destAnn);
					
					}					
				}
			}
			
			if (newAnnotationsNodes.size() > 0) {
				AnnotationRecreator.createAnnotationsSequentiallyDepthless(transcription, newAnnotationsNodes);
				//AnnotationRecreator.createAnnotationsSequentially(transcription, newAnnotationsNodes);
			}
		}		
		transcription.setNotifying(true);
		
		setWaitCursor(false);
		
		// restore the time propagation mode
		transcription.setTimeChangePropagationMode(curPropMode);
	}

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver the TranscriptionImpl
	 * @param arguments the arguments: <ul><li>arg[0] = the name of the source 
	 * 		tier (String)<li> <li>arg[1] = the name of the destination tier (String)
	 * 		</li> <li>arg[2] = the token delimiter(s) (String)</li> <li>arg[3] = 
	 * 		a flag denoting whether or not to preserve existing annotations 
	 * 		on the destination tier (Boolean)</li> <li>arg[4] = a flag denoting 
	 * 		whether or not to create new annotations for empty source annotations 
	 *    (Boolean)</li> </ul>
	 */
	public void execute(Object receiver, Object[] arguments) {
		transcription = (TranscriptionImpl)receiver;
		String sourceName = (String)arguments[0];
		String destName = (String)arguments[1];

		delimiter = (String) arguments[2]; //can be null
		preserve = ((Boolean)arguments[3]).booleanValue();
		createEmpty = ((Boolean)arguments[4]).booleanValue();

		sourceTier = (TierImpl)transcription.getTierWithId(sourceName);
		destTier = (TierImpl)transcription.getTierWithId(destName);

		if (transcription == null || sourceTier == null || destTier == null) {
			LOG.severe("Error in retrieving the transcription or one of the tiers.");
			return;
		}
		// create a blocking progress monitor and start tokenizing
		existAnnotations = new ArrayList();
		newAnnotationsNodes = new ArrayList();
		completedTokenizations = new ArrayList();
	
		new TokenizeThread().start();

	}

	/**
	 * Returns the name of the command.
	 *
	 * @return the name of the command
	 */
	public String getName() {
		return commandName;
	}
	
	/**
	 * Changes the cursor to either a 'busy' cursor or the default cursor.
	 *
	 * @param showWaitCursor when <code>true</code> show the 'busy' cursor
	 */
	private void setWaitCursor(boolean showWaitCursor) {
		if (showWaitCursor) {
			ELANCommandFactory.getRootFrame(transcription).getRootPane()
							  .setCursor(Cursor.getPredefinedCursor(
					Cursor.WAIT_CURSOR));
		} else {
			ELANCommandFactory.getRootFrame(transcription).getRootPane()
							  .setCursor(Cursor.getDefaultCursor());
		}
	}
	
	///////////////////////////////////////////
	// inner class: execution thread
	///////////////////////////////////////////
	/**
	 * Class that handles the tokenization in a separate thread.
	 * @author Han Sloetjes
	 */
	private class TokenizeThread extends Thread {
		
		/**
		 * Before using this inner class we must ensure none of the relevant fields
		 * (transcription, sourcetier and destination tier) are null!
		 */
		TokenizeThread() {
		}
		
		public void run() {
			int curPropMode = 0;

			curPropMode = transcription.getTimeChangePropagationMode();

			if (curPropMode != Transcription.NORMAL) {
				transcription.setTimeChangePropagationMode(Transcription.NORMAL);
			}
			
			final IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(
					ELANCommandFactory.getRootFrame(transcription),
					true, 
					ElanLocale.getString("TokenizeDialog.Message.Tokenizing"),
					true, 
					ElanLocale.getString("Button.Cancel"));
				// if we are blocking (modal) call show from a separate thread
			new Thread(new Runnable(){
				public void run() {
					monitor.show();				
				}
			}).start();
				
			Vector sourceAnnos = sourceTier.getAnnotations();
			if (sourceAnnos.size() <= 0) {
				monitor.close();
				return;
			}
			TokenizeCommand.this.transcription.setNotifying(false);
				
			//start iterating over source annotations
			int stereotype = destTier.getLinguisticType().getConstraints().getStereoType();
			StringTokenizer tokenizer;
			String nextToken;
			Iterator annIt = sourceAnnos.iterator();
			AbstractAnnotation srcAnn;
			String srcValue;
			AbstractAnnotation destAnn;
			Annotation prevAnn;
			Vector childrenOnDest;
			ArrayList newAnnos = new ArrayList();
			ArrayList siblings;
				
			while (annIt.hasNext()) {
				srcAnn = (AbstractAnnotation)annIt.next();
				childrenOnDest = srcAnn.getChildrenOnTier(destTier);
				if (childrenOnDest.size() > 0 && !preserve) {
					// store old annotations
					Iterator childIt = childrenOnDest.iterator();
					while (childIt.hasNext()) {
						destAnn = (AbstractAnnotation)childIt.next();
						existAnnotations.add(AnnotationRecreator.createTreeForAnnotation(destAnn));
					}
					// next remove them
					childIt = childrenOnDest.iterator();
					while (childIt.hasNext()) {
						destTier.removeAnnotation((AbstractAnnotation)childIt.next());
					}
				}
				// if existing anns need to be preserved, do nothing
				if (childrenOnDest.size() == 0 || !preserve) {
					srcValue = srcAnn.getValue();
					if (delimiter != null) {
						tokenizer = new StringTokenizer(srcValue, delimiter);
					} else {
						tokenizer = new StringTokenizer(srcValue);
					}
					newAnnos.clear();
					if (tokenizer.countTokens() == 0) {
						// if the source annotation is empty and the create destination
						// for empty source is selected create one empty annotation
						if (createEmpty) {
							Annotation ann;
							if (stereotype == Constraint.SYMBOLIC_SUBDIVISION) {								
								long time = (long)(srcAnn.getBeginTimeBoundary() + srcAnn.getEndTimeBoundary()) / 2;
								ann = destTier.createAnnotation(time, time);
							} else {			
								ann = destTier.createAnnotation(srcAnn.getBeginTimeBoundary(), srcAnn.getEndTimeBoundary());
							}
							if (ann != null) {
								newAnnos.add(ann);
							}
						}
					} else {						
						prevAnn = null;
						while (tokenizer.hasMoreTokens()) {
							nextToken = tokenizer.nextToken();
							if (prevAnn == null) {
								if (stereotype == Constraint.SYMBOLIC_SUBDIVISION) {								
									long time = (long) (srcAnn.getBeginTimeBoundary() + srcAnn.getEndTimeBoundary()) / 2;
									prevAnn = destTier.createAnnotation(time, time);
								} else {			
									prevAnn = destTier.createAnnotation(srcAnn.getBeginTimeBoundary(), srcAnn.getEndTimeBoundary());
								}
							} else {
								prevAnn = destTier.createAnnotationAfter(prevAnn);		
							}
							prevAnn.setValue(nextToken);
							newAnnos.add(prevAnn);
						}
					}
					// now create datarecords of the created annotations...
					if (newAnnos.size() > 0) {
						siblings = new ArrayList(newAnnos.size());
						for (int i = 0; i< newAnnos.size(); i++) {
							//newAnnotationsNodes.add(new DefaultMutableTreeNode(
							//	new AnnotationDataRecord((Annotation)newAnnos.get(i))));
							siblings.add(new AnnotationDataRecord((Annotation)newAnnos.get(i)));
						}
						newAnnotationsNodes.add(siblings);
					}
					completedTokenizations.add(new AnnotationDataRecord(srcAnn));
				}
				// after completion of a whole source annotation, check the cancelled value of the monitor
				if (monitor.isCancelled()) {
					//monitor.close();
					//return;
					break;
				}
			}
			TokenizeCommand.this.transcription.setNotifying(true);
			
			// restore the time propagation mode
			transcription.setTimeChangePropagationMode(curPropMode);

			monitor.close();
		}
	}
}
