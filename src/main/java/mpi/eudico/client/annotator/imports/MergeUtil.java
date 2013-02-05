package mpi.eudico.client.annotator.imports;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;


public class MergeUtil implements ClientLogger {

    /**
     * Checks whether the tiers can be added: this depends on tier dependencies and
     * compatibility of linguistic types.
     * 
     * @return a list of tiers that can be added
     */
    public ArrayList getAddableTiers(Transcription srcTrans, Transcription destTrans,
            ArrayList selTiers) {
        if (srcTrans == null || destTrans == null) {
            LOG.warning("A Transcription is null");
            return new ArrayList(0); 
        }
        if (selTiers == null) {
            int size = srcTrans.getTiers().size();
            selTiers = new ArrayList(size);
            TierImpl ti;
            for (int i = 0; i < size; i++) {
                ti = (TierImpl)srcTrans.getTiers().get(i);
                selTiers.add(ti.getName());
            }
        }
        ArrayList validTiers = new ArrayList(selTiers.size());

        String name;
        TierImpl t, t2;
        for (int i = 0; i < selTiers.size(); i++) {
            name = (String)selTiers.get(i);
            t = (TierImpl) srcTrans.getTierWithId(name);
            if (t != null) {
                t2 = (TierImpl) destTrans.getTierWithId(name);
                if (t2 == null) { // not yet in destination
                    if (t.getParentTier() == null) {
                        // a toplevel tier can always be added
                        validTiers.add(t);    
                    } else {
                        // check whether:
                        // 1 - the parent/ancestors are also in the list to be added
                        // 2 - the parent/ancestors are already in the destination
                        TierImpl parent = null;
                        String parentName = null;
                        TierImpl loopTier = t;
                        while (loopTier.getParentTier() != null) {
                            parent = (TierImpl) loopTier.getParentTier();
                            parentName = parent.getName();
                            if (selTiers.contains(parentName)) {
                                if (parent.getParentTier() == null) {
                                    validTiers.add(t);
                                    break;
                                } else if (destTrans.getTierWithId(parentName) != null) {
                                    if (lingTypeCompatible(parent, (TierImpl) destTrans.getTierWithId(parentName))) {
                                        validTiers.add(t); 
                                    }
                                    
                                    break;
                                } else {
                                    // try next ancestor
                                    loopTier = parent;
                                    continue;
                                }
                            } else {
                                // the parent is not selected
                                if (destTrans.getTierWithId(parentName) != null) {
                                    if (lingTypeCompatible(parent, (TierImpl) destTrans.getTierWithId(parentName))) {
                                        validTiers.add(t); 
                                    }
                                    
                                    break;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    
                } else {
                    // already in destination, check linguistic type
                    if (lingTypeCompatible(t, t2)) {
                        validTiers.add(t);
                    }
                }
            } else {
                LOG.warning("Tier " + name + " does not exist.");
            }
            if (!validTiers.contains(t)) {
                LOG.warning("Cannot add tier " + name);
            }
        }
        return validTiers;
    }

    /**
     * Check whether the LinguisticTypes of the tiers have the same stereotype.
     * This is a loose check, other attributes could also be checked; name, cv etc. 
     */       
    public boolean lingTypeCompatible(TierImpl t, TierImpl t2) {
        if (t == null || t2 == null) {
            return false;
        }
        // check linguistic type
        LinguisticType lt = t.getLinguisticType();
        LinguisticType lt2 = t2.getLinguisticType();
        // losely check the linguistic types
        if (/*lt.getLinguisticTypeName().equals(lt2.getLinguisticTypeName()) &&*/ 
                lt.hasConstraints() == lt2.hasConstraints()) {
            if (lt.getConstraints() != null) {
                if (lt.getConstraints().getStereoType() == lt2.getConstraints().getStereoType()) {
                    return true;
                } else {
                    LOG.warning("Incompatible tier types in source and destination: " + t.getName());
                    return false;
                }
            } else {
                // both toplevel tiers
                return true;
            }
        }
        return false;
    }

    /**
     * Sort the tiers in the list hierarchically.
     * @param tiers the tiers
     */
    public ArrayList sortTiers(ArrayList tiersToSort) {
        if (tiersToSort == null || tiersToSort.size() == 0) {
            return null;
        }
        
        DefaultMutableTreeNode sortedRootNode = new DefaultMutableTreeNode(
        "sortRoot");
        HashMap nodes = new HashMap();
        TierImpl t = null;
        for (int i = 0; i < tiersToSort.size(); i++) {
            t = (TierImpl) tiersToSort.get(i);

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(t);
            nodes.put(t, node);
        }

        for (int i = 0; i < tiersToSort.size(); i++) {
            t = (TierImpl) tiersToSort.get(i);

            if ((t.getParentTier() == null) ||
                    !tiersToSort.contains(t.getParentTier())) {
                sortedRootNode.add((DefaultMutableTreeNode) nodes.get(t));
            } else {
                ((DefaultMutableTreeNode) nodes.get(t.getParentTier())).add(
                        (DefaultMutableTreeNode) nodes.get(t));
            }
        }
        
        //tiersToAdd.clear();
        ArrayList sorted = new ArrayList(tiersToSort.size());

        Enumeration en = sortedRootNode.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();

            if (node.getUserObject() instanceof TierImpl) {
                sorted.add(node.getUserObject());
            }
        }
        return sorted;
    }

    
    /**
     * Adds the tiers that are not yet in the destination transcription,
     * after performing some checks.  If Linguistic types and/or CV's should
     * be copied/added these are copied/added first. It is assumed that it is 
     * save to add LT's and CV's to the destination Transcription without cloning.
     *
     * @param tiersToAdd a list of tiers to add to the destination
     */
    public void addTiersTypesAndCVs(TranscriptionImpl srcTrans, TranscriptionImpl destTrans, 
            ArrayList tiersToAdd) {
        if (srcTrans == null) {
            LOG.warning("Source transcription is null.");
            return;
        }
        if (destTrans == null){
            LOG.warning("Destination transcription is null");
            return;
        }
        if (tiersToAdd == null || tiersToAdd.size() == 0) {
            LOG.warning("No tiers to add");
            return;
        }
//        System.out.println("num tiers: " + tiersToAdd.size());
        Hashtable renamedCVS = new Hashtable(5);
        Hashtable renamedTypes = new Hashtable(5);
        ArrayList typesToAdd = new ArrayList(5);
        ArrayList cvsToAdd = new ArrayList(5);
        TierImpl t;
        TierImpl t2;
        TierImpl newTier;
        LinguisticType lt;
        LinguisticType lt2 = null;
        String typeName;
        ControlledVocabulary cv;
        ControlledVocabulary cv2 = null;

        for (int i = 0; i < tiersToAdd.size(); i++) {
            t = (TierImpl) tiersToAdd.get(i);
            if (t == null || destTrans.getTierWithId(t.getName()) != null) {
                // don't do further checks on ling. type and cv
                continue;
            }
            lt = t.getLinguisticType();
            if (typesToAdd.contains(lt)) {
                continue;
            }
            typeName = lt.getLinguisticTypeName();
            lt2 = destTrans.getLinguisticTypeByName(typeName);
            if (lt2 != null) {//already there
                if (lt.getConstraints() == null && lt2.getConstraints() == null) {
                    continue;
                } else if (lt.getConstraints() != null && lt2.getConstraints() != null) {
                    if (lt.getConstraints().getStereoType() == lt.getConstraints().getStereoType()) {
                        continue;
                    }
                }
                // rename and add
                String nname = typeName + "-cp";
                int c = 1;
                while (destTrans.getLinguisticTypeByName(nname + c) != null) {
                    c++;
                }
                nname = nname + c;
                if (!renamedTypes.containsKey(typeName)) {
                    renamedTypes.put(typeName, nname); 
                }
                
            }// check if they are the same?

            typesToAdd.add(lt);

            if (lt.isUsingControlledVocabulary()) {
                cv = srcTrans.getControlledVocabulary(lt.getControlledVocabylaryName());

                if (!cvsToAdd.contains(cv)) {
                    cvsToAdd.add(cv);
                }
            }
        }

        // add CV's, renaming when necessary
        for (int i = 0; i < cvsToAdd.size(); i++) {
            cv = (ControlledVocabulary) cvsToAdd.get(i);
            cv2 = destTrans.getControlledVocabulary(cv.getName());

            if (cv2 == null) {
                destTrans.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            } else if (!cv.equals(cv2)) {
                // rename
                String newCVName = cv.getName() + "-cp";
                int c = 1;
                while (destTrans.getControlledVocabulary(newCVName + c) != null) {
                    c++;
                }
                newCVName = newCVName + c;
                LOG.info("Renamed Controlled Vocabulary: " + cv.getName() +
                    " to " + newCVName);
                renamedCVS.put(cv.getName(), cv);
                cv.setName(newCVName);
                destTrans.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            }
        } // end cv
        // add linguistic types
        for (int i = 0; i < typesToAdd.size(); i++) {
            lt = (LinguisticType) typesToAdd.get(i);

            typeName = lt.getLinguisticTypeName();

            if (lt.isUsingControlledVocabulary() &&
                    renamedCVS.containsKey(lt.getControlledVocabylaryName())) {
                cv2 = (ControlledVocabulary) renamedCVS.get(lt.getControlledVocabylaryName());
                lt.setControlledVocabularyName(cv2.getName());
            }

            if (renamedTypes.containsKey(lt.getLinguisticTypeName())) {     
                String newLTName = (String) renamedTypes.get(lt.getLinguisticTypeName());
                
                LOG.info("Renamed Linguistic Type: " +
                            lt.getLinguisticTypeName() + " to " + newLTName);
                lt.setLinguisticTypeName(newLTName);                           
            } 
            destTrans.addLinguisticType(lt);
            LOG.info("Added Linguistic Type: " +
                        lt.getLinguisticTypeName());

        } // end linguistic types

        // add tiers if necessary
        for (int i = 0; i < tiersToAdd.size(); i++) {
//            System.out.println("i: " + i);
            t = (TierImpl) tiersToAdd.get(i);
            
            if (destTrans.getTierWithId(t.getName()) != null) {
                continue;
            }
            t2 = (TierImpl) t.getParentTier();

            String parentTierName = null;

            if (t2 != null) {
                parentTierName = t2.getName();
            }

            newTier = null;
            if (parentTierName == null) {
                newTier = new TierImpl(t.getName(), t.getParticipant(),
                        destTrans, null);
            } else {
                t2 = (TierImpl) destTrans.getTierWithId(parentTierName);

                if (t2 != null) {
                    newTier = new TierImpl(t2, t.getName(), t.getParticipant(),
                            destTrans, null);
                } else {
                    LOG.warning("The parent tier: " + parentTierName +
                        " for tier: " + t.getName() +
                        " was not found in the destination transcription");
                }
            }

            if (newTier != null) {
                lt = t.getLinguisticType();
                lt2 = destTrans.getLinguisticTypeByName(lt.getLinguisticTypeName());

                if (lt2 != null) {
                    newTier.setLinguisticType(lt2);

                    destTrans.addTier(newTier);
                    LOG.info("Created and added tier to destination: " +
                            newTier.getName());
                } else {
                    LOG.warning("Could not add tier: " + newTier.getName() +
                        " because the Linguistic Type was not found in the destination transcription.");
                }
                newTier.setDefaultLocale(t.getDefaultLocale());
                newTier.setAnnotator(t.getAnnotator());
            }
        } //end tiers
    }

    
}
