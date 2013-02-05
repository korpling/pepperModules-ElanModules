package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.IndeterminateProgressMonitor;
import mpi.eudico.client.annotator.util.AnnotationRecreator;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.Cursor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;


/**
 * A Command to change tier attributes.
 *
 * @author HB, HS
 * @version 1.0
 */
public class ChangeTierAttributesCommand implements UndoableCommand {
    private String commandName;

    // old state
    private String oldTierName;
    private String oldParticipant;
    private String oldAnnotator;
    private Locale oldLocale;
    private LinguisticType oldLingType;
    private Tier oldParentTier;

    // new state
    private String tierName;
    private Tier parentTier;
    private String lingTypeName;
    private LinguisticType lingType;
    private String participant;
    private String annotator;
    private Locale locale;

    // receiver
    private TierImpl tier;
    private TranscriptionImpl transcription;

    //private ArrayList annotationsNodes;
    // an ArrayList of SVGAnnotationDataRecord objects

    /** Holds value of property DOCUMENT ME! */
    ArrayList storedGraphicsData;

    /**
     * Creates a new ChangeTierAttributesCommand instance
     *
     * @param theName the name of the command
     */
    public ChangeTierAttributesCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Tier
     * @param arguments the arguments:  <ul><li>arg[0] = the tier name
     *        (String)</li> <li>arg[1] = the parent tier (Tier)</li>
     *        <li>arg[2] = the linguistic type (String)</li> <li>arg[3] = the
     *        participant (String)</li> <li>arg[4] =  the annotator
     *        (String)</li> <li>arg[5] =  the default language
     *        (Locale)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        // receiver is Tier
        tier = (TierImpl) receiver;

        transcription = (TranscriptionImpl) tier.getParent();

        // arguments, store for redo
        tierName = (String) arguments[0];
        parentTier = (Tier) arguments[1];
        lingTypeName = (String) arguments[2];
        participant = (String) arguments[3];
        annotator = (String) arguments[4];
        locale = (Locale) arguments[5];

        if (tier != null) {
            setWaitCursor(true);

            try {
                oldTierName = tier.getName();
                oldParticipant = tier.getParticipant();
                oldLocale = tier.getDefaultLocale();
                oldLingType = tier.getLinguisticType();
                oldParentTier = (TierImpl) tier.getParentTier();
                oldAnnotator = tier.getAnnotator();

                // first back up the annotations if necessary
                // HS sep-04 
                // as long as there is no proper mechanism of updating  
                // existing annotations, changes that can effect data integrity 
                // are prevented in the change tier dialog

                /*
                   if ((parentTier != oldParentTier) ||
                           !oldLingType.getLinguisticTypeName().equals(lingTypeName)) {
                
                       annotationsNodes = new ArrayList();
                       Vector annos = tier.getAnnotations(null);
                       Iterator anIter = annos.iterator();
                       AbstractAnnotation ann;
                       while (anIter.hasNext()) {
                           ann = (AbstractAnnotation) anIter.next();
                           annotationsNodes.add(AnnotationRecreator.createTreeForAnnotation(
                                   ann));
                       }
                
                   }
                 */
                if (!tierName.equals(oldTierName)) {
                    // assumes there has been a check on the uniqueness of the name 
                    tier.setName(tierName);
                }

                if (parentTier != oldParentTier) {
                    tier.setParentTier(parentTier);
                }

                if (!participant.equals(oldParticipant)) {
                    tier.setParticipant(participant);
                }

                if (!annotator.equals(oldAnnotator)) {
                    tier.setAnnotator(annotator);
                }
                
                if (locale == null || locale != oldLocale) {
                    tier.setDefaultLocale(locale);
                }

                if ((oldLingType == null) ||
                        (lingTypeName != oldLingType.getLinguisticTypeName())) {
                    Vector types = ((Transcription) (tier.getParent())).getLinguisticTypes();
                    LinguisticType t = null;
                    Iterator typeIter = types.iterator();

                    while (typeIter.hasNext()) {
                        t = (LinguisticType) typeIter.next();

                        if (t.getLinguisticTypeName().equals(lingTypeName)) {
                            break;
                        }
                    }

                    if (t != null) {
                        lingType = t;
                        tier.setLinguisticType(lingType);

                        if (oldLingType.hasGraphicReferences() != lingType.hasGraphicReferences()) {
                            // convert AlignableAnnotations to SVGAlignableAnnotations or vice versa
                            if (oldLingType.hasGraphicReferences()) {
                                new ConversionThread(transcription, tier).start(true);
                            } else {
                                new ConversionThread(transcription, tier).start();
                            }
                        }
                    }
                }
                
                if (!tierName.equals(oldTierName)) {
                	updatePreferences(oldTierName, tierName);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                setWaitCursor(false);
            }

            setWaitCursor(false);
        }
    }

    /**
     * The undo action.
     */
    public void undo() {
        if (tier != null) {
            try {
                if ((tierName != null) && (!tierName.equals(oldTierName))) {
                    tier.setName(oldTierName);
                }

                if (parentTier != oldParentTier) {
                    tier.setParentTier(oldParentTier);
                }

                tier.setLinguisticType(oldLingType);

                if ((participant != null) &&
                        (!participant.equals(oldParticipant))) {
                    tier.setParticipant(oldParticipant);
                }

                if ((annotator != null) &&
                        (!annotator.equals(oldAnnotator))) {
                    tier.setAnnotator(oldAnnotator);
                }
                
                if (locale != oldLocale) {
                    tier.setDefaultLocale(oldLocale);
                }

                // finally recreate annotations if necessary
                //HS sep-04 see execute

                /*
                   if ((parentTier != oldParentTier) ||
                           !oldLingType.getLinguisticTypeName().equals(lingTypeName)) {
                       if ((transcription != null) && (annotationsNodes != null) &&
                               (annotationsNodes.size() > 0)) {
                           setWaitCursor(true);
                
                           tier.removeAllAnnotations();
                           DefaultMutableTreeNode node;
                           if (tier.hasParentTier()) {
                               AnnotationRecreator.createAnnotationsSequentially(transcription,
                                   annotationsNodes);
                           } else {
                               for (int i = 0; i < annotationsNodes.size(); i++) {
                                   node = (DefaultMutableTreeNode) annotationsNodes.get(i);
                                   AnnotationRecreator.createAnnotationFromTree(transcription,
                                       node);
                               }
                           }
                           setWaitCursor(false);
                       }
                   }
                 */
                if ((lingType != null) &&
                        (oldLingType.hasGraphicReferences() != lingType.hasGraphicReferences())) {
                    new ConversionThread(transcription, tier).start();
                }
                if (!tierName.equals(oldTierName)) {
                	updatePreferences(tierName, oldTierName);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                setWaitCursor(false);
            }
        }
    }

    /**
     * The redo action.
     */
    public void redo() {
        if (tier != null) {
            setWaitCursor(true);

            try {
                if ((tierName != null) && (!tierName.equals(oldTierName))) {
                    tier.setName(tierName);
                }

                if (parentTier != oldParentTier) {
                    tier.setParentTier(parentTier);
                }

                if (lingType != null) {
                    tier.setLinguisticType(lingType);
                }

                /*
                   if ((oldLingType == null) ||
                           (lingTypeName != oldLingType.getLinguisticTypeName())) {
                       Vector types = ((Transcription) (tier.getParent())).getLinguisticTypes();
                       LinguisticType t = null;
                       Iterator typeIter = types.iterator();
                       while (typeIter.hasNext()) {
                           t = (LinguisticType) typeIter.next();
                           if (t.getLinguisticTypeName().equals(lingTypeName)) {
                               break;
                           }
                       }
                       tier.setLinguisticType(t);
                   }
                 */
                if ((participant != null) &&
                        (!participant.equals(oldParticipant))) {
                    tier.setParticipant(participant);
                }

                if (!annotator.equals(oldAnnotator)) {
                    tier.setAnnotator(annotator);
                }
                
                if (locale != oldLocale) {
                    tier.setDefaultLocale(locale);
                }

                if ((lingType != null) &&
                        (oldLingType.hasGraphicReferences() != lingType.hasGraphicReferences())) {
                    new ConversionThread(transcription, tier).start();
                }
                if (!tierName.equals(oldTierName)) {
                	updatePreferences(oldTierName, tierName);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                setWaitCursor(false);
            }

            setWaitCursor(false);
        }
    }

    /**
     * Updates a few user preferences for the tier, if any.
     * 
     * @param oldTierName old name in the preferences maps
     * @param tierName the new name in the preferences maps
     */
    private void updatePreferences(String oldTierName, String tierName) {
    	Object colors = Preferences.get("TierColors", transcription);
    	if (colors instanceof Map) {
    		Map colorMap = (Map) colors;
    		if (colorMap.containsKey(oldTierName)) {
    			Object col = colorMap.remove(oldTierName);
    			colorMap.put(tierName, col);
    			Preferences.set("TierColors", colorMap, transcription, true);
    		}
    	}
    	
		Object fonts = Preferences.get("TierFonts", transcription);
		if (fonts instanceof Map) {
			Map fontsMap = (Map) fonts;
			if (fontsMap.containsKey(oldTierName)) {
				Object font = fontsMap.remove(oldTierName);
				fontsMap.put(tierName, font);
				Preferences.set("TierFonts", fontsMap, transcription, true);
			}
		}
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

    ////////////////////////////////////////////////////////////////////
    // conversion thread
    ////////////////////////////////////////////////////////////////////

    /**
     * A thread to execute and monitor conversion of annotations on a tier.
     *
     * @author Han Sloetjes
     */
    class ConversionThread extends Thread {
        /** Holds value of property DOCUMENT ME! */
        TranscriptionImpl transcription;

        /** Holds value of property DOCUMENT ME! */
        TierImpl tier;

        /** Holds value of property DOCUMENT ME! */
        boolean storeGraphicsData = false;

        /**
         * Constructor.
         *
         * @param transcription the transcription
         * @param tier the single tier to convert
         */
        public ConversionThread(TranscriptionImpl transcription, TierImpl tier) {
            this.transcription = transcription;
            this.tier = tier;
        }

        /**
         * Tells the converter whether or not to store relevant data of
         * annotations  referencing a graphical object.
         *
         * @param storeGraphicsData if true store datarecords of annotations
         *        with  graphical objects
         */
        public void start(boolean storeGraphicsData) {
            this.storeGraphicsData = storeGraphicsData;
            this.start();
        }

        /**
         * Implementation of the Runnable interface.
         */
        public void run() {
            final IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(ELANCommandFactory.getRootFrame(
                        transcription), true,
                    ElanLocale.getString("EditTypeDialog.Message.Convert"),
                    false, null);

            // if we are blocking (modal) call show from a separate thread
            new Thread(new Runnable() {
                    public void run() {
                        monitor.show();
                    }
                }).start();

            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            transcription.setNotifying(false);

            if (storeGraphicsData) {
                ChangeTierAttributesCommand.this.storedGraphicsData = AnnotationRecreator.storeGraphicsData(transcription,
                        tier);
            }

            AnnotationRecreator.convertAnnotations(transcription, tier);

            if ((ChangeTierAttributesCommand.this.storedGraphicsData != null) &&
                    tier.getLinguisticType().hasGraphicReferences()) {
                // apparently we are undoing a previous conversion...
                AnnotationRecreator.restoreGraphicsData(transcription,
                    ChangeTierAttributesCommand.this.storedGraphicsData);
            }

            transcription.setNotifying(true);

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);

            monitor.close();
        }
    }
}
