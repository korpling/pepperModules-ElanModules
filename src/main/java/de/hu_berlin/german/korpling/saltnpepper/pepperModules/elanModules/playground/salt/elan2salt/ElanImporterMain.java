/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.playground.salt.elan2salt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import org.eclipse.emf.common.util.URI;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.Elan2SaltMapper;
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
	public static SCorpusGraph createCorpusStructure(SaltProject saltProject, String path) 
	{
		SCorpusGraph sCorpGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
		saltProject.getSCorpusGraphs().add(sCorpGraph);
		SCorpus sCorpus1= SaltFactory.eINSTANCE.createSCorpus();
		sCorpus1.setSName("Heliand");
		sCorpGraph.addSNode(sCorpus1);
		
		SDocument sDoc= null;
		Collection<String> filenames = getFileNamesInDirectory(path);
		
		for (String filename : filenames){
			sDoc= SaltFactory.eINSTANCE.createSDocument();
			sDoc.setSName(filename);
			sDoc.createSMetaAnnotation("elan", "origFile", path + "/" + filename);
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
		System.out.println("path: "+ path);
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
	
	public static String tmpPathName= "/home/tom/DDDcorpora/salt-public/Heliand";
		
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
				createCorpusStructure(saltProject, path);
				System.out.println("OK");
			}//creating a corpus structure for salt project

			{//filling all of the documents in the corpus structure with document structure data
				System.out.print("filling all of the documents in the corpus structure with document structure data...");
				//this works, because after createCorpusStructure() was called, only one graph exists in salt project
				SCorpusGraph sCorpusGraph= saltProject.getSCorpusGraphs().get(0);
				
				for (SDocument sDocument: sCorpusGraph.getSDocuments())
				{//filling all of the documents in the corpus structure with document structure data	
					Elan2SaltMapper mapper = new Elan2SaltMapper();
					sDocument.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
					System.out.println("sdoc meta" + sDocument.getSMetaAnnotations());
					mapper.setSDocument(sDocument);
					mapper.mapSDocument();
				}//filling all of the documents in the corpus structure with document structure data
				System.out.println("OK");
			}//filling all of the documents in the corpus structure with document structure data
			
			File tmpPath= new File(tmpPathName);
			if (!tmpPath.exists())
				tmpPath.mkdirs();
			
			{//store salt project to tmp path
				System.out.print("store salt project to tmp path ('"+tmpPath.getAbsolutePath()+"')...");
				saltProject.saveSaltProject(URI.createFileURI(tmpPath.getAbsolutePath()));
				System.out.println("OK");
			}//store salt project to tmp path
			
/*			{//load salt project from tmp path
				System.out.print("load salt project from tmp path ('"+tmpPath.getAbsolutePath()+"')...");
				saltProject= SaltFactory.eINSTANCE.createSaltProject();
				saltProject.loadSaltProject(URI.createFileURI(tmpPath.getAbsolutePath()));
				System.out.println("OK");
			}//load salt project from tmp path
			
			{//store salt project in DOT format
				System.out.print("store dot representation of salt project to tmp path ('"+tmpPath.getAbsolutePath()+"\\DOT')...");
				URI uri = URI.createFileURI(tmpPath.getAbsolutePath()+"/DOT");
				saltProject.saveSaltProject_DOT(uri);
				System.out.println("OK");
			}//store salt project in DOT format
*/			
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
