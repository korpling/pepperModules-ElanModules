package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;


/**
 * DOCUMENT ME!
 * $Id: ActiveAnnotationCommand.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class ActiveAnnotationCommand implements Command {
    private String commandName;

    /**
     * Creates a new ActiveAnnotationCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public ActiveAnnotationCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) receiver;
        Annotation annot = (Annotation) arguments[0];

        vm.getActiveAnnotation().setAnnotation(annot);

        if (annot != null) {
            if (annot instanceof AlignableAnnotation) {
                vm.getSelection().setSelection(annot.getBeginTimeBoundary(),
                    annot.getEndTimeBoundary());

                if (!vm.getMediaPlayerController().isBeginBoundaryActive()) {
                    vm.getMediaPlayerController().toggleActiveSelectionBoundary();
                }
            } else if (annot instanceof RefAnnotation) {
            	Annotation parent = annot;
            	while (true) {
            		parent = parent.getParentAnnotation();
            		if (parent == null || parent instanceof AlignableAnnotation) {
            			break;
            		}
            	}
            	if (parent instanceof AlignableAnnotation) {
            		AlignableAnnotation aa = (AlignableAnnotation) parent;
					vm.getSelection().setSelection(aa.getBeginTimeBoundary(),
							aa.getEndTimeBoundary());
            	}
            }

            vm.getMasterMediaPlayer().setMediaTime(annot.getBeginTimeBoundary());

            /* // different behavior in selectionmode / non-selectionmode
               if (!vm.getMediaPlayerController().getSelectionMode()) {
                   vm.getSelection().setSelection(annot.getBeginTimeBoundary(), annot.getEndTimeBoundary());
                   if (!vm.getMediaPlayerController().isBeginBoundaryActive()) {
                       vm.getMediaPlayerController().toggleActiveSelectionBoundary();
                   }
                   vm.getMasterMediaPlayer().setMediaTime(annot.getBeginTimeBoundary());
               }    else { //selection mode
                   if (!vm.getMediaPlayerController().isBeginBoundaryActive()) {
                       vm.getMasterMediaPlayer().setMediaTime(annot.getEndTimeBoundary());
                   }    else {
                       vm.getMasterMediaPlayer().setMediaTime(annot.getBeginTimeBoundary());
                   }
               }
               } else { // non-alignable annotation
                   vm.getMasterMediaPlayer().setMediaTime(annot.getBeginTimeBoundary());
               }
             */
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return commandName;
    }
}
