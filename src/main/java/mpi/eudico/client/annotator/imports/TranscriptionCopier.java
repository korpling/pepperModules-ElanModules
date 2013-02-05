package mpi.eudico.client.annotator.imports;

import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.client.util.TierTree;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * A class for copying a complete transcription.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class TranscriptionCopier implements ClientLogger {
    /**
     * Creates a new TranscriptionCopier instance
     */
    public TranscriptionCopier() {
    }

    /**
     * Copies the contents of one transcription to another.<br>
     * Note: there may be quite a bit of overlap with classes that copy a tier
     * etc.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyTranscription(TranscriptionImpl src, TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        copyHeader(src, dest);
        copyMediaDescriptors(src, dest);
        copyControlledVocabularies(src, dest);
        copyLinguisticTypes(src, dest);
        copyTiers(src, dest);
        copyAnnotations(src, dest);
    }

    /**
     * Copies information stored in the header of an .eaf Currently only the
     * author field is copied.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyHeader(TranscriptionImpl src, TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        dest.setAuthor(src.getAuthor()); //??
    }

    /**
     * Copies the Media Descriptors.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyMediaDescriptors(TranscriptionImpl src,
        TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        Vector mediaDescs = src.getMediaDescriptors();
        Vector copyDesc = new Vector(mediaDescs.size());
        MediaDescriptor srcMd;
        MediaDescriptor copyMd;

        for (int i = 0; i < mediaDescs.size(); i++) {
            srcMd = (MediaDescriptor) mediaDescs.get(i);
            copyMd = (MediaDescriptor) srcMd.clone();
            copyDesc.add(copyMd);
        }

        dest.setMediaDescriptors(copyDesc);
    }

    /**
     * Copies the Controlled Vocabularies.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyControlledVocabularies(TranscriptionImpl src,
        TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        Vector srcCVS = src.getControlledVocabularies();
        Vector copyCVS = new Vector(srcCVS.size());
        CVEntry[] srcEntries;
        ControlledVocabulary cv;
        ControlledVocabulary cpCV;
        CVEntry srcEntry;
        CVEntry cpEntry;

        for (int i = 0; i < srcCVS.size(); i++) {
            cv = (ControlledVocabulary) srcCVS.get(i);
            cpCV = new ControlledVocabulary(cv.getName(), cv.getDescription());

            srcEntries = cv.getEntries();

            for (int j = 0; j < srcEntries.length; j++) {
                srcEntry = srcEntries[j];
                cpEntry = new CVEntry(srcEntry.getValue(),
                        srcEntry.getDescription());
                cpCV.addEntry(cpEntry);
            }

            copyCVS.add(cpCV);
        }

        dest.setControlledVocabularies(copyCVS);
    }

    /**
     * Copies the LinguisticTypes.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyLinguisticTypes(TranscriptionImpl src,
        TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        Vector srcTypes = src.getLinguisticTypes();
        Vector destTypes = new Vector(srcTypes.size());
        LinguisticType srcLt;
        LinguisticType cpLt;

        for (int i = 0; i < srcTypes.size(); i++) {
            srcLt = (LinguisticType) srcTypes.get(i);
            cpLt = new LinguisticType(srcLt.getLinguisticTypeName());
            cpLt.setControlledVocabularyName(srcLt.getControlledVocabylaryName());
            cpLt.setGraphicReferences(srcLt.hasGraphicReferences());
            cpLt.setTimeAlignable(srcLt.isTimeAlignable()); //??
            cpLt.addConstraint(srcLt.getConstraints());
            destTypes.add(cpLt);
        }

        dest.setLinguisticTypes(destTypes);
    }

    /**
     * Copies the tiers.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyTiers(TranscriptionImpl src, TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        TierImpl srcTier;
        TierImpl parTier;
        TierImpl cpTier;
        String parentName = null;
        String typeName = null;
        LinguisticType lt;
        LinguisticType destLt;
        Vector destTypes = dest.getLinguisticTypes();

        // create a tree structure of the tiers
        TierTree tierTree = new TierTree(src);
        DefaultMutableTreeNode root = tierTree.getTree();
        Enumeration ten = root.breadthFirstEnumeration();
        ten.nextElement(); // skip the empty root

        Object next;
        DefaultMutableTreeNode node;

        while (ten.hasMoreElements()) {
            node = (DefaultMutableTreeNode) ten.nextElement();
            next = node.getUserObject();

            if (next instanceof String) {
                srcTier = (TierImpl) src.getTierWithId((String) next);
                if (srcTier == null) {
                    LOG.warning("A tier could not be found in the source transcription: " +
                            next);
                    continue;
                }
                parTier = (TierImpl) srcTier.getParentTier();

                if (parTier != null) {
                    parentName = parTier.getName();
                }

                lt = srcTier.getLinguisticType();
                typeName = lt.getLinguisticTypeName();

                cpTier = null;

                if (parTier == null) {
                    cpTier = new TierImpl(srcTier.getName(),
                            srcTier.getParticipant(), dest, null);
                } else {
                    parTier = (TierImpl) dest.getTierWithId(parentName);

                    if (parTier != null) {
                        cpTier = new TierImpl(parTier, srcTier.getName(),
                                srcTier.getParticipant(), dest, null);
                    } else {
                        LOG.warning("The parent tier: " + parentName +
                            " for tier: " + cpTier.getName() +
                            " was not found in the destination transcription");
                    }
                }

                if (cpTier != null) {
                    destLt = null;

                    for (int i = 0; i < destTypes.size(); i++) {
                        lt = (LinguisticType) destTypes.get(i);

                        if (lt.getLinguisticTypeName().equals(typeName)) {
                            destLt = lt;

                            break;
                        }
                    }

                    if (destLt != null) {
                        cpTier.setLinguisticType(destLt);

                        // transcription does not perform any checks..
                        if (dest.getTierWithId(cpTier.getName()) == null) {
                            dest.addTier(cpTier);
                            LOG.info("Created and added tier to destination: " +
                                cpTier.getName());
                        } else {
                            LOG.info("Could not add tier to destination: " +
                                cpTier.getName() +
                                " already exists in the transcription");
                        }
                    } else {
                        LOG.warning("Could not add tier: " + cpTier.getName() +
                            " because the Linguistic Type was not found in the destination transcription.");
                    }
                    
                    cpTier.setDefaultLocale(srcTier.getDefaultLocale());
                    cpTier.setAnnotator(srcTier.getAnnotator());
                }
            } else {
                LOG.warning("Unknown object in the tier tree.");
            }
        }
    }

    /**
     * Copies the annotations.
     *
     * @param src the source transcription
     * @param dest the destination transcription
     *
     * @throws NullPointerException if any of the arguments is null
     */
    public void copyAnnotations(TranscriptionImpl src, TranscriptionImpl dest) {
        if (src == null) {
            throw new NullPointerException("Source transcription is null");
        }

        if (dest == null) {
            throw new NullPointerException("Destination transcription is null");
        }

        Vector tiers = src.getTiers();
        Vector annos;
        AlignableAnnotation aa;
        DefaultMutableTreeNode annNode;
        TierImpl srcTier;

        for (int i = 0; i < tiers.size(); i++) {
            srcTier = (TierImpl) tiers.get(i);

            // only toplevel tiers
            if (!srcTier.hasParentTier()) {
                annos = srcTier.getAnnotations();

                int size = annos.size();

                for (int j = 0; j < size; j++) {
                    aa = (AlignableAnnotation) annos.get(j);
                    annNode = AnnotationRecreator.createTreeForAnnotation(aa);
                    AnnotationRecreator.createAnnotationFromTree(dest, annNode);
                }
            }
        }
    }
}
