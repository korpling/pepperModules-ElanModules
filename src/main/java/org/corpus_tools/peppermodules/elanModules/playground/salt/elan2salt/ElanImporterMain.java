/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.elanModules.playground.salt.elan2salt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import org.corpus_tools.peppermodules.elanModules.Elan2SaltMapper;
import org.eclipse.emf.common.util.URI;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;


/**
 * This class shows the usage of the linguistic meta model Salt. 
 * Therefore we create a linguistic corpus with direct use of the Salt object model. We here show how to store this model to disk and
 * how to load a model from disk into main memory. 
 * 
 * The method creatingSCorpusStructure() shows how to create a corpus structure.
 * The method creatingSDocumentStructure() shows how to create a document structure.
 * 
 * @author Florian Zipser
 * @author Tom Ruette
 */
public class ElanImporterMain 
{
	private SCorpusGraph sCorpusGraph=null;
		
	/**
	 * Creates the following corpus structure and adds it to the given salt project.
	 * 
	 * 				rootCorpus
	 * 	/		\				/		\
	 * doc1		doc2		doc3		doc4 ...
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static SCorpusGraph createCorpusStructure(SaltProject saltProject, String path, String name) 
	{
		SCorpusGraph sCorpGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
		saltProject.getSCorpusGraphs().add(sCorpGraph);
		SCorpus sCorpus1= SaltFactory.eINSTANCE.createSCorpus();
		System.out.println("Creating corpus structure with name: " + name);
		sCorpus1.setSName(name);
		sCorpGraph.addSNode(sCorpus1);
		
		SDocument sDoc= null;
		Collection<String> filenames = getFileNamesInDirectory(path);
		System.out.println(filenames);
		
		for (String filename : filenames){
			sDoc= SaltFactory.eINSTANCE.createSDocument();
			sDoc.setSName(filename);
			sDoc.createSMetaAnnotation(null, "origFile", path + "/" + filename);
			
			//get properties file
/*			FileInputStream in;
			try {
				String metafname = path.replaceAll("elan-public", "meta") + "/" + filename.replace(".eaf", ".meta");
				in = new FileInputStream(metafname);
				Properties props = new Properties();
				props.load(new InputStreamReader(in, "UTF-8"));
				for (Object key : props.keySet()) {
					sDoc.createSMetaAnnotation(null, (String) key, props.getProperty((String) key));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/			
			sCorpGraph.addSDocument(sCorpus1, sDoc);
		}
		
		return(sCorpGraph);
	}
	
	/**
	 * Creates a collection of strings which contain the actual filenames in a certain path
	 * without the path.
	 * 
	 * @param String path, the path in which Elan files ending in eaf should be retrieved
	 */
	public static Collection<String> getFileNamesInDirectory(String path){
		String files;
		Collection<String> out = new Vector<String>();	
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		System.out.println("there are a number of files in this place: " + path + ", " + listOfFiles.length);
		for (int i = 0; i < listOfFiles.length; i++){ 
			if (listOfFiles[i].isFile()){
				files = listOfFiles[i].getName();
				if (files.endsWith(".eaf") || files.endsWith("(1).EAF")){
					out.add(files);
				}
		    }
	    }
		return out;
	}
	
		
	public static String getHello()
	{
		StringBuffer retVal= new StringBuffer();
		retVal.append("****************************************************************************\n");
		retVal.append("***                        Welcome to Elan2Salt                          ***\n");
		retVal.append("****************************************************************************\n");
		return(retVal.toString());
	}
	
	public static String getBye()
	{
		StringBuffer retVal= new StringBuffer();
		retVal.append("****************************************************************************\n");
		retVal.append("*** Bye from Elan2Salt                                                   ***\n");
		retVal.append("****************************************************************************\n");
		return(retVal.toString());
	}
	
	public SCorpusGraph getCorpus(){
		return sCorpusGraph;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println(getHello());
		
		if (	(args== null)||
				(args.length<1))
			throw new NullPointerException("Please set corpus source path.");
		String path= args[0];
		
		try
		{
			//Creating a new salt project, this is the main object and contains all the others. 
			SaltProject saltProject= SaltFactory.eINSTANCE.createSaltProject();

			{//creating a corpus structure for salt project
				System.out.print("creating a corpus structure for salt project...");
				String corpusname = path.replaceAll("/$", "");
				System.out.println(corpusname);
				createCorpusStructure(saltProject, path + "elan-public", corpusname.substring(corpusname.lastIndexOf("/")+1));
				System.out.println("OK");
			}//creating a corpus structure for salt project

			{//filling all of the documents in the corpus structure with document structure data
				System.out.println("filling all of the documents in the corpus structure with document structure data...");
				//this works, because after createCorpusStructure() was called, only one graph exists in salt project
				SCorpusGraph sCorpusGraph= saltProject.getSCorpusGraphs().get(0);
				
				for (SDocument sDocument: sCorpusGraph.getSDocuments())
				{//filling all of the documents in the corpus structure with document structure data
					System.out.println("working on sdoc: " + sDocument);
					Elan2SaltMapper mapper = new Elan2SaltMapper();
					sDocument.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
					mapper.setSDocument(sDocument);
					mapper.mapSDocument();
				}//filling all of the documents in the corpus structure with document structure data
				System.out.println("OK");
			}//filling all of the documents in the corpus structure with document structure data
			
			String tmpPathName= path + "salt";

			File tmpPath= new File(tmpPathName);
			if (!tmpPath.exists())
				tmpPath.mkdirs();
			
			{//store salt project to tmp path
				System.out.print("store salt project to tmp path ('"+tmpPath.getAbsolutePath()+"')...");
				saltProject.saveSaltProject(URI.createFileURI(tmpPath.getAbsolutePath()));
				System.out.println("OK");
			}//store salt project to tmp path
			
			System.out.println();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			System.out.println(getBye());
		}
	}
}
