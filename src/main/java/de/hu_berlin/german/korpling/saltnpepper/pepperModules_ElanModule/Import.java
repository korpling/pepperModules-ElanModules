package de.hu_berlin.german.korpling.saltnpepper.pepperModules_ElanModule;

import org.eclipse.emf.common.util.URI;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 *
 * @author tom
 */
public class Import {
    
    public static void main(String [] args)
    {
        // initialize the Salt object
    	ElanSaltDoc esdoc = new ElanSaltDoc("/home/tom/Dropbox/ElanModule/otfrid.eaf");
	    esdoc.createDefaultLayer();
    	
        // get the primary text in a string from the eaf file
        String primaryTextString = esdoc.getValuesFromTier("Referenztext B");
        
        // put the primary text in the Salt Document Graph
        esdoc.setPrimaryText(primaryTextString);
        System.out.println(primaryTextString);
        
        // get the Tier with the tokenization
        TierImpl tokenizationTier = esdoc.getTierByName("Referenztext W");
        
        // add the tokens of the tokenization to the esdoc
        esdoc.setTokens(tokenizationTier);
        
        // add the lemma annotation to the esdoc
        esdoc.addAnnotation("Lemma");
        esdoc.addAnnotation("Übersetzung");
        esdoc.addAnnotation("M1a DDDTS Lemma");
        esdoc.addAnnotation("M1b DDDTS Beleg");
        esdoc.addAnnotation("M2a Flexion Lemma");
        esdoc.addAnnotation("M2b Flexion Beleg 1");
        esdoc.addAnnotation("M2c Flexion Beleg 2");
        esdoc.addAnnotation("S1a Satz");
          
        // save the esdoc
        URI loc = URI.createFileURI("/home/tom/Dropbox/ElanModule/out");
        esdoc.save(loc);
    }
}