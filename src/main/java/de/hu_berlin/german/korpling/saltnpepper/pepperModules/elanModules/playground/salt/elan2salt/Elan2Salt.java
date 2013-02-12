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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.eclipse.emf.common.util.URI;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SLemmaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SPOSAnnotation;


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
public class Elan2Salt 
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
	public static SCorpusGraph createCorpusStructure(SaltProject saltProject) 
	{
		SCorpusGraph sCorpGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
		saltProject.getSCorpusGraphs().add(sCorpGraph);
		SCorpus sCorpus1= SaltFactory.eINSTANCE.createSCorpus();
		sCorpus1.setSName("Heliand");
		sCorpGraph.addSNode(sCorpus1);
		
		SDocument sDoc= null;
		String path = "/home/tom/Dropbox/ElanModule/heliand/out";
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
	
	/**
	 * Creates a {@link STextualDS} object containing the primary text {@link Elan2Salt#PRIMARY_TEXT} and adds the object
	 * to the {@link SDocumentGraph} being contained by the given {@link SDocument} object.
	 * 
	 * @param sDocument the document, to which the created {@link STextualDS} object will be added
	 */
	public static void createPrimaryData(SDocument sDocument){
		if (sDocument== null)
			throw new Elan2SaltException("Cannot create example, because the given sDocument is empty.");
		if (sDocument.getSDocumentGraph()== null)
			throw new Elan2SaltException("Cannot create example, because the given sDocument does not contain an SDocumentGraph.");
		STextualDS sTextualDS = null;
		{//creating the primary text
			String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
			ElanWrapper ew = new ElanWrapper(filename);
			sTextualDS= SaltFactory.eINSTANCE.createSTextualDS();
			// ew.getPrimaryText gets the text from the elan document as a string
			sTextualDS.setSText(ew.getPrimaryText());
			//adding the text to the document-graph
			sDocument.getSDocumentGraph().addSNode(sTextualDS);
		}//creating the primary text
	}
	
	/**
	 * Creates a set of {@link SToken} objects tokenizing the primary text
	 * The created {@link SToken} objects and corresponding {@link STextualRelation} objects are added to the given {@link SDocument} object.
	 * @param sDocument the document, to which the created {@link SToken} objects will be added
	 */
	public static void createTokens(SDocument sDocument){
		// tricky, here only the first textualds is taken?
		STextualDS sTextualDS = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
		// as a means to group elements, layers (SLayer) can be used.
		SLayer annoLayer = SaltFactory.eINSTANCE.createSLayer();
		// TODO: parameterize
		annoLayer.setSName("annotations");
		sDocument.getSDocumentGraph().addSLayer(annoLayer);
		
		String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		ElanWrapper ew = new ElanWrapper(filename);
		
		Collection<ElanToken> elanTokens = ew.getElanTokens();
		for (ElanToken et : elanTokens){
			createToken(et.getBeginChar(), et.getEndChar(), sTextualDS, sDocument, "annotations");
		}
	}

	/**
	 * Creates an individual salt token, connects it to the document with a textual relation
	 * @param start character position where the token starts
	 * @param end character position where the token ends
	 * @param sTextualDS primary text for which start and end are valid
	 * @param sDocument the salt document
	 * @param layerName the layer in which the token is put
	 */
	public static void createToken(int start, int end, STextualDS sTextualDS, SDocument sDocument, String layerName){
		SToken sToken= SaltFactory.eINSTANCE.createSToken();
		sDocument.getSDocumentGraph().addSNode(sToken);
		SLayer layer = sDocument.getSDocumentGraph().getSLayerByName(layerName).get(0);
		layer.getSNodes().add(sToken);
		STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
		sTextRel.setSToken(sToken);
		sTextRel.setSTextualDS(sTextualDS);
		sTextRel.setSStart(start);
		sTextRel.setSEnd(end);
		sDocument.getSDocumentGraph().addSRelation(sTextRel);
	}
	
	/**
	 * Creates an annotation for a single token
	 * @param sDocument
	 * @param curTier elan tier name from which the annotation should be grabbed
	 */
	public static void createMorphologyAnnotation(SDocument sDocument, String curTier){
		List<STextualRelation> sTextRels= Collections.synchronizedList(sDocument.getSDocumentGraph().getSTextualRelations());
		
		String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		ElanWrapper ew = new ElanWrapper(filename);
		
		SAnnotation sAnno = null;
				
		for (int i=0; i< sTextRels.size(); i++){
			sAnno = SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setSName(curTier);
			STextualRelation curTextRel = sTextRels.get(i);
			int startChar = curTextRel.getSStart();
			int stopChar = curTextRel.getSEnd();
			String check = curTextRel.getSTextualDS().getSText().substring(startChar, stopChar);
			ElanToken et = ew.getElanToken(startChar, stopChar);
			String value = ew.getValueInTier(curTier, et.getBeginTime(), et.getEndTime());
			if (!check.equals(et.getTok())){
				System.out.println("error: " + check + " != " + et.getTok() + " at time " + et.getBeginTime());
			}
			if (value != null){
				sAnno.setSValue(value.trim());
				sTextRels.get(i).getSToken().addSAnnotation(sAnno);
			}
		}
	}
	
	/**
	 * Creates morphological annotations (pos and lemma) for the tokenized sample and adds them to each {@link SToken} object as
	 * {@link SPOSAnnotation} or {@link SLemmaAnnotation} object.
	 * @param sDocument the document containing the {@link SToken} and {@link STextualDS} objects
	 */
	public static void createMorphologyAnnotations(SDocument sDocument){
		createMorphologyAnnotation(sDocument, "posDict");
		createMorphologyAnnotation(sDocument, "pos");
		createMorphologyAnnotation(sDocument, "lemma");
		createMorphologyAnnotation(sDocument, "language");
		createMorphologyAnnotation(sDocument, "translation");
		createMorphologyAnnotation(sDocument, "rhyme");
		createMorphologyAnnotation(sDocument, "inflection");
		createMorphologyAnnotation(sDocument, "inflectionClass");
		createMorphologyAnnotation(sDocument, "inflectionClassDict");
		
	}
	
	/**
	 * Creates {@link SSpan} object above the tokenization.
	 * @param sDocument
	 */
	public static void createInformationStructureSpan(SDocument sDocument){
		List<SToken> sTokens= Collections.synchronizedList(sDocument.getSDocumentGraph().getSTokens());
		
		SSpan sSpan= null;
		SSpanningRelation sSpanRel= null;

		String curTierName = "verse";

		String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		ElanWrapper ew = new ElanWrapper(filename);
		Collection<ElanSpan> elanspans = ew.getElanSpans(curTierName);
		
		for (ElanSpan es : elanspans){
			sSpan= SaltFactory.eINSTANCE.createSSpan();
			sDocument.getSDocumentGraph().addSNode(sSpan);
			for (int i= 0; i< es.size(); i++)
			{
				sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
				sSpanRel.setSSpan(sSpan);
				ElanToken et = es.getElanToken(i);
				SToken st = getSTextualRelation(sDocument, et).getSToken();
				sSpanRel.setSToken(st);
				sDocument.getSDocumentGraph().addSRelation(sSpanRel);
			}
		}
	}

	/**
	 * gets the textual relation that glues together a salt document and the salt token that is taking place for the elan token
	 * @param sDocument relevant document
	 * @param et Elan token that we want to find in the document
	 * @return
	 */
	public static STextualRelation getSTextualRelation(SDocument sDocument, ElanToken et){
		STextualRelation out = null;
		List<STextualRelation> sTextRels = Collections.synchronizedList(sDocument.getSDocumentGraph().getSTextualRelations());
		for (STextualRelation st : sTextRels){
			if (st.getSStart() == et.getBeginChar() & st.getSEnd() == et.getEndChar()){
				out = st;
				break;
			}
		}
		return out;
	}
	
	/**
	 * Annotates the {@link SSpan} objects above the tokenization with information structural annotations.
	 * @param sDocument
	 */
	public static void createInformationStructureAnnotations(SDocument sDocument){
		SAnnotation sAnno= null;
		
		String curTierName = "verse";
		

		String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		ElanWrapper ew = new ElanWrapper(filename);
		ArrayList<ElanSpan> elanspans = (ArrayList) ew.getElanSpans(curTierName);
		for (int i = 0; i < elanspans.size(); i++){
			sAnno= SaltFactory.eINSTANCE.createSAnnotation();
			//setting the name of the annotation
			sAnno.setSName(elanspans.get(i).getName());
			//setting the value of the annotation
			sAnno.setSValue(elanspans.get(i).getValue());
			//adding the annotation to the placeholder span
			sDocument.getSDocumentGraph().getSSpans().get(i).addSAnnotation(sAnno);
		}
		

	}
	
	/**
	 * function to traverse an elan document (going through tiers, then going through the annotations in the tiers)
	 * the retrieved annotations are mapped to salt, either by adding them to an existing salt token or salt span
	 * or by creating a new salt token or span.
	 * @param sDocument from the meta annotation in the salt document, the actual elan file is retrieved.
	 */
	public static void traverseElanDocument(SDocument sDocument){
		String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		ElanWrapper ew = new ElanWrapper(filename);
		ArrayList<String> tierNames = ew.getTierNames();
		for (String tierName : tierNames){
			System.out.println("working on " + filename + ", adding tier " + tierName);
			for (ElanSpan es : ew.getElanSpans(tierName)){
// dirty hack to make everything as a span, to solve issue with token annotations always being represented as kwic in annis
// TODO change something in ANNIS so that				
//				if (es.size() == 1){
//					ElanToken et = es.getElanToken(0);
//					setSaltToken(sDocument, et, es.getName(), es.getValue());
//				}
//				if (es.size() > 1){
					setSaltSpan(sDocument, es, es.getName(), es.getValue());
//				}
			}
		}
	}
	
	/**
	 * the elan span in the input is added (or created) to the salt document, with an annotation that consist of name and value 
	 * @param sDocument the salt document in which this span should be added or created
	 * @param es the elan span
	 * @param name annotation name
	 * @param value annotation value
	 */
	private static void setSaltSpan(SDocument sDocument, ElanSpan es, String name, String value) {
		// dirty hack to check for weird symbols in the value that we do not want.
		if (value.trim().length() != 0){
			if (String.format("%040x", new BigInteger(value.getBytes())).startsWith("-")){
				value = "";
			}
		}
		if (value.trim().length() != 0){
			SSpan sSpan= null;
			SSpanningRelation sSpanRel= null;
			SAnnotation sAnno = null;
			sSpan= SaltFactory.eINSTANCE.createSSpan();
			//sDocument.getSDocumentGraph().addSNode(sSpan);
			for (int i= 0; i< es.size(); i++)
			{
				sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
				sSpanRel.setSSpan(sSpan);
				ElanToken et = es.getElanToken(i);
				STextualRelation str = getSTextualRelation(sDocument, et);
				SToken st = null;
				if (str == null){
					st = SaltFactory.eINSTANCE.createSToken();
					sDocument.getSDocumentGraph().addSNode(st);
					if (sDocument.getSDocumentGraph().getSLayerByName("annotations").size() == 0){
						SLayer annoLayer = SaltFactory.eINSTANCE.createSLayer();
						annoLayer.setSName("annotations");
						sDocument.getSDocumentGraph().addSLayer(annoLayer);
					}
					SLayer layer = sDocument.getSDocumentGraph().getSLayerByName("annotations").get(0);
					layer.getSNodes().add(st);
					STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
					sTextRel.setSToken(st);
					sTextRel.setSTextualDS(sDocument.getSDocumentGraph().getSTextualDSs().get(0));
					sTextRel.setSStart(et.getBeginChar());
					sTextRel.setSEnd(et.getEndChar());
					sDocument.getSDocumentGraph().addSRelation(sTextRel);
				}
				if (str != null){
					st = str.getSToken();
				}
				sSpanRel.setSToken(st);
	//			sDocument.getSDocumentGraph().addSRelation(sSpanRel);
			}
			
			// TODO this does not seem to work
			boolean find = false;
			for (SSpan sp : sDocument.getSDocumentGraph().getSSpans()){
				if (sSpan.equals(sp)){
					sSpan = sp;
					find = true;
					break;
				}
			}
			
			if (find == true){
				sAnno= SaltFactory.eINSTANCE.createSAnnotation();
				//setting the name of the annotation
				sAnno.setSName(name);
				//setting the value of the annotation
				sAnno.setSValue(value.trim());
				//adding the annotation to the placeholder span
				sSpan.addSAnnotation(sAnno);
			}
			
			if (find == false){
				sSpan= null;
				sSpanRel= null;
				sSpan= SaltFactory.eINSTANCE.createSSpan();
				sDocument.getSDocumentGraph().addSNode(sSpan);
				for (int i= 0; i< es.size(); i++)
				{
					sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
					sSpanRel.setSSpan(sSpan);
					ElanToken et = es.getElanToken(i);
					SToken st = getSTextualRelation(sDocument, et).getSToken();
					sSpanRel.setSToken(st);
					sDocument.getSDocumentGraph().addSRelation(sSpanRel);
				}
				
				sAnno= SaltFactory.eINSTANCE.createSAnnotation();
				//setting the name of the annotation
				sAnno.setSName(name);
				//setting the value of the annotation
				sAnno.setSValue(value.trim());
				//adding the annotation to the placeholder span
				sSpan.addSAnnotation(sAnno);
			}
		}
	}

	/**
	 * the elan token in the input is added (or created) to the salt document, with an annotation that consists of the tier name and the value
	 * @param sDocument salt document to which this token is added
	 * @param et the elan token
	 * @param curTierName the name of the annotation to be added
	 * @param curValue the value of the annotation to be added
	 */
	private static void setSaltToken(SDocument sDocument, ElanToken et, String curTierName, String curValue) {
		STextualRelation curTextRel = getSTextualRelation(sDocument, et);
		String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		ElanWrapper ew = new ElanWrapper(filename);
		SAnnotation sAnno = null;
		if (curTextRel != null){
			sAnno = SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setSName(curTierName);
			int startChar = curTextRel.getSStart();
			int stopChar = curTextRel.getSEnd();
			String check = curTextRel.getSTextualDS().getSText().substring(startChar, stopChar);
			String value = ew.getValueInTier(curTierName, et.getBeginTime(), et.getEndTime());
			if (!check.equals(et.getTok())){
				System.out.println("error: " + check + " != " + et.getTok() + " at time " + et.getBeginTime());
			}
			if (value != null){
				sAnno.setSValue(value.trim());
				sAnno.setSNS("elan");
				curTextRel.getSToken().addSAnnotation(sAnno);
			}
		}
		if (curTextRel == null){
			STextualDS sTextualDS = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
			//as a means to group elements, layers (SLayer) can be used. here, a layer
			//named "morphology" is created and the tokens will be added to it
			SLayer annoLayer = SaltFactory.eINSTANCE.createSLayer();
			annoLayer.setSName("annotations");
			sDocument.getSDocumentGraph().addSLayer(annoLayer);
			
			SToken sToken= SaltFactory.eINSTANCE.createSToken();
			sDocument.getSDocumentGraph().addSNode(sToken);
			SLayer layer = sDocument.getSDocumentGraph().getSLayerByName("annotations").get(0);
			layer.getSNodes().add(sToken);
			STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
			sTextRel.setSToken(sToken);
			sTextRel.setSTextualDS(sTextualDS);
			sTextRel.setSStart(et.getBeginChar());
			sTextRel.setSEnd(et.getEndChar());
			sDocument.getSDocumentGraph().addSRelation(sTextRel);
			
			sAnno = SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setSName(curTierName);
			sAnno.setSValue(curValue.trim());
			sAnno.setSNS("elan");
			sTextRel.getSToken().addSAnnotation(sAnno);
		}
	}

	/**
	 * 
	 * @param sDocument
	 */
	public static void createSDocumentStructure(SDocument sDocument){
		
		
		//create SDocumentGraph object and set it to SDocument object
		sDocument.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		// create the primary text - DONE
		createPrimaryData(sDocument);
		// goes through the elan document, and makes all the elan tiers into salt tiers
		// TODO add an option to ignore certain elan tiers
		traverseElanDocument(sDocument);
	}
	
	public static String tmpPathName= "./_tmp/";
		
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
		try
		{
			//Creating a new salt project, this is the main object and contains all the others. 
			SaltProject saltProject= SaltFactory.eINSTANCE.createSaltProject();

			{//creating a corpus structure for salt project
				System.out.print("creating a corpus structure for salt project...");
				createCorpusStructure(saltProject);
				System.out.println("OK");
			}//creating a corpus structure for salt project

			{//filling all of the documents in the corpus structure with document structure data
				System.out.print("filling all of the documents in the corpus structure with document structure data...");
				//this works, because after createCorpusStructure() was called, only one graph exists in salt project
				SCorpusGraph sCorpusGraph= saltProject.getSCorpusGraphs().get(0);
				for (SDocument sDocument: sCorpusGraph.getSDocuments())
				{//filling all of the documents in the corpus structure with document structure data	
					createSDocumentStructure(sDocument);
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
			
			{//load salt project from tmp path
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
