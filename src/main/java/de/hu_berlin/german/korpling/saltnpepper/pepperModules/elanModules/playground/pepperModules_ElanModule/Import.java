package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.playground.pepperModules_ElanModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.emf.common.util.URI;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 *
 * @author tom
 */
public class Import {
	
	private static SaltProject sp;
	private static SCorpus corp;
    
    public static void main(String [] args) throws Exception
    {
    	//get properties file
    	FileInputStream in = new FileInputStream("/home/tom/Dropbox/ElanModule/settings.txt");
    	Properties prop = new Properties();
    	prop.load(new InputStreamReader(in, "UTF-8"));
    	
    	// initialize the Salt stuff
   	    sp = SaltFactory.eINSTANCE.createSaltProject();
        SCorpusGraph sCorpGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
        sp.getSCorpusGraphs().add(sCorpGraph);
        corp= SaltFactory.eINSTANCE.createSCorpus();
        corp.setSName(prop.getProperty("corpusname"));
        sCorpGraph.addSNode(corp);
        //creates a meta annotation on the corpus sampleCorpus
        corp.createSMetaAnnotation(null, "annotator", "tom");
    	
        // go through the files
    	Collection<String> fnames = getFileNamesInDirectory(prop.getProperty("output"));
    	for (String fname : fnames){
    		elan2salt(fname, prop);
    	}
    	
        // save the corpus
        URI loc = URI.createFileURI(prop.getProperty("saltoutput"));
        save(loc);
    }
    
    public static void elan2salt(String fname, Properties prop){
    	
        // initialize the Salt object
    	ElanSaltDoc esdoc = new ElanSaltDoc(prop.getProperty("output") + "/" + fname, fname, corp);
	    esdoc.createDefaultLayer();
    	
        // get the primary text in a string from the eaf file
        String primaryTextString = esdoc.getValuesFromTier(prop.getProperty("primarytext"));
        
        // put the primary text in the Salt Document Graph
        esdoc.setPrimaryText(primaryTextString);
        System.out.println(primaryTextString);
        
        // get the Tier with the tokenization
        TierImpl tokenizationTier = esdoc.getTierByName(prop.getProperty("tokenization"));
        
        // add the tokens of the tokenization to the esdoc
        esdoc.setSTokens(tokenizationTier);
        
        // add the lemma annotation to the esdoc
        
        String annotations[] = prop.getProperty("annotations").split(",");
        for (String annotation : annotations){
        	esdoc.addAnnotation(annotation.trim());
        }
    }
    
	public static void save(URI saveloc) {
		sp.saveSaltProject(saveloc);
	}
    
	private static Collection<String> getFileNamesInDirectory(String path){
		String files;
		Collection<String> out = new Vector();	
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles(); 
		for (int i = 0; i < listOfFiles.length; i++){ 
			if (listOfFiles[i].isFile()){
				files = listOfFiles[i].getName();
				if (files.endsWith(".eaf") || files.endsWith(".EAF")){
					out.add(files);
				}
		    }
	    }
		return out;
	}
}