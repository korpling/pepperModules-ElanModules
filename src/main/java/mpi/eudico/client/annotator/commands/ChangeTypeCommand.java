package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.IndeterminateProgressMonitor;
import mpi.eudico.client.annotator.util.AnnotationRecreator;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

//import mpi.library.util.LogUtil;

import java.util.ArrayList;
import java.util.Vector;
//import java.util.logging.Logger;


/**
 * Changes a Linguistic Type in a Transcription.
 *
 * @author Han Sloetjes
 * @version jun 04 added the name of a linked Controlled Vocabulary
 * @version apr 08 added the id of a Data Category
 */
public class ChangeTypeCommand implements UndoableCommand {
    /** Holds value of property DOCUMENT ME! */
    //private static final Logger LOG = Logger.getLogger(ChangeTypeCommand.class.getName());
    private String commandName;

    // receiver
    private TranscriptionImpl transcription;

    // store the arguments for undo /redo
    // old values
    private String oldTypeName;
    private Constraint oldConstraint;
    private String oldCVName;
    private boolean oldTimeAlignable;
    private boolean oldGraphicsAllowed;
    private String oldDcId;
	private LexiconQueryBundle2 oldQueryBundle;

    // new values
    private String typeName;
    private Constraint constraint;
    private String cvName;
    private boolean timeAlignable;
    private boolean graphicsAllowed;
    private String dcId;
    private LexiconQueryBundle2 queryBundle;

    // the LinguisticType
    private LinguisticType linType;

    // an ArrayList of SVGAnnotationDataRecord objects

    /** Holds value of property DOCUMENT ME! */
    ArrayList storedGraphicsData;

    /**
     * Creates a new ChangeTypeCommand instance
     *
     * @param name the name of the command
     */
    public ChangeTypeCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action.
     */
    public void undo() {
        if (linType != null) {
            Vector constraints = new Vector();
            constraints.add(oldConstraint);

            transcription.changeLinguisticType(linType, oldTypeName,
                    constraints, oldCVName, oldTimeAlignable, oldGraphicsAllowed, oldDcId, oldQueryBundle);

            if ( /*SVGPrefs.getUseSVG() && */
                oldGraphicsAllowed != graphicsAllowed) {
                // convert AlignableAnnotations to SVGAlignableAnnotations or vice versa
                //AnnotationRecreator.convertAnnotations(transcription, linType);
                new ConversionThread(transcription, linType).start();
            }
        }
    }

    /**
     * The redo action.
     */
    public void redo() {
        if (linType != null) {
            Vector constraints = new Vector();
            constraints.add(constraint);

            transcription.changeLinguisticType(linType, typeName, constraints,
                    cvName, timeAlignable, graphicsAllowed, dcId, queryBundle);

            if ( /*SVGPrefs.getUseSVG() && */
                oldGraphicsAllowed != graphicsAllowed) {
                // convert AlignableAnnotations to SVGAlignableAnnotations or vice versa
                //AnnotationRecreator.convertAnnotations(transcription, linType);
                new ConversionThread(transcription, linType).start();
            }
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the new name of the
     *        type (String)</li> <li>arg[1] = the new Constraint for the type
     *        (Constraint)</li> <li>arg[2] = the name of the new
     *        ControlledVocabulary (String)</li> <li>arg[3] = the new time
     *        alignable  value (Boolean)</li> <li>arg[4] = the new graphics
     *        reference allowed value (Boolean)</li> <li>arg[5] = the
     *        Linguistic Type to change</li> <li>arg[6] = the
     *        id of a referenced data category</li> 
     *        <li>arg[7] = the name of the new lexicon service</li></ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        // new
        typeName = (String) arguments[0];
        constraint = (Constraint) arguments[1];
        cvName = (String) arguments[2];
        timeAlignable = ((Boolean) arguments[3]).booleanValue();
        graphicsAllowed = ((Boolean) arguments[4]).booleanValue();
        linType = (LinguisticType) arguments[5];
        if (arguments.length >= 7) {
        	dcId = (String) arguments[6];
        }
        if (arguments.length >= 8) {
    		queryBundle = (LexiconQueryBundle2) arguments[7];
    	}
        // old
        oldTypeName = linType.getLinguisticTypeName();
        oldConstraint = linType.getConstraints();
        oldCVName = linType.getControlledVocabylaryName();
        oldTimeAlignable = linType.isTimeAlignable();
        oldGraphicsAllowed = linType.hasGraphicReferences();
        oldDcId = linType.getDataCategory();
        oldQueryBundle = linType.getLexiconQueryBundle();
        
        Vector constraints = new Vector();
        constraints.add(constraint);

        transcription.changeLinguisticType(linType, typeName, constraints,
                cvName, timeAlignable, graphicsAllowed, dcId, queryBundle);

        // conversion of AlignableAnnotations
        if (oldGraphicsAllowed != graphicsAllowed) {
            // convert AlignableAnnotations to SVGAlignableAnnotations or vice versa
            if (oldGraphicsAllowed) {
                new ConversionThread(transcription, linType).start(true);
            } else {
                new ConversionThread(transcription, linType).start();
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

    ////////////////////////////////////////////////////////////////////
    // conversion thread
    ////////////////////////////////////////////////////////////////////

    /**
     * A thread to execute and monitor conversion of annotations on tiers
     * using a certain LinguisticType.
     *
     * @author Han Sloetjes
     */
    class ConversionThread extends Thread {
        /** Holds value of property DOCUMENT ME! */
        TranscriptionImpl transcription;

        /** Holds value of property DOCUMENT ME! */
        LinguisticType type;

        /** Holds value of property DOCUMENT ME! */
        boolean storeGraphicsData = false;

        /**
         * Constructor for a thread that does not store annotation information.
         *
         * @param transcription the transcription
         * @param type the effected LinguisticType
         */
        public ConversionThread(TranscriptionImpl transcription,
            LinguisticType type) {
            this.transcription = transcription;
            this.type = type;
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
                ChangeTypeCommand.this.storedGraphicsData = AnnotationRecreator.storeGraphicsData(transcription,
                        type);
            }

            AnnotationRecreator.convertAnnotations(transcription, type);

            if ((ChangeTypeCommand.this.storedGraphicsData != null) &&
                    type.hasGraphicReferences()) {
                // apparently we are undoing a previous conversion...
                AnnotationRecreator.restoreGraphicsData(transcription,
                    ChangeTypeCommand.this.storedGraphicsData);
            }

            transcription.setNotifying(true);

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);

            monitor.close();
        }
    }
}
