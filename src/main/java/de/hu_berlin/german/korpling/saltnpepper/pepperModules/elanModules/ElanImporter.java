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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.exceptions.ELANImporterException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * @author Tom Ruette
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="ElanImporterComponent", factory="PepperImporterComponentFactory")
public class ElanImporter extends PepperImporterImpl implements PepperImporter
{
	public ElanImporter()
	{
		super();
		this.name= "ElanImporter";
		this.addSupportedFormat("elan", "4.5.0", null);
	}
	
	/**
	 * Stores relation between documents and their resource 
	 */
	private Map<SElementId, URI> documentResourceTable= null;
	
	@Override
	public void importCorpusStructure(SCorpusGraph corpusGraph)
			throws ELANImporterException
	{
		this.setSCorpusGraph(corpusGraph);
		if (this.getSCorpusGraph()== null)
			throw new ELANImporterException(this.name+": Cannot start with importing corpus, because salt project is not set.");
		if (this.getCorpusDefinition()== null)
			throw new ELANImporterException(this.name+": Cannot start with importing corpus, because no corpus definition to import is given.");
		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new ELANImporterException(this.name+": Cannot start with importing corpus, because the path of given corpus definition is null.");
		if (this.getCorpusDefinition().getCorpusPath().isFile())
		{
			this.documentResourceTable= new Hashtable<SElementId, URI>();
			//clean uri in corpus path (if it is a folder and ends with/, / has to be removed)
			if (	(this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("/")) || 
					(this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("\\")))
			{
				this.getCorpusDefinition().setCorpusPath(this.getCorpusDefinition().getCorpusPath().trimSegments(1));
			}
			try {
				EList<String> endings= new BasicEList<String>();
				endings.add("eaf");
				endings.add("xml");
				this.documentResourceTable= this.createCorpusStructure(this.getCorpusDefinition().getCorpusPath(), null, endings);
			} catch (IOException e) {
				throw new ELANImporterException(this.name+": Cannot start with importing corpus, because saome exception occurs: ",e);
			}
		}	
	}
	
	/**
	 * This method is called by method {@link #start()} of superclass {@link PepperImporter}, if the method was not overwritten
	 * by the current class. If this is not the case, this method will be called for every document which has
	 * to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument) ||
				((sElementId.getSIdentifiableElement() instanceof SCorpus))))
		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
			if (sElementId.getSIdentifiableElement() instanceof SDocument)
			{
				SDocument sDocument= (SDocument) sElementId.getSIdentifiableElement(); 
				Elan2SaltMapper mapper= new Elan2SaltMapper();
				mapper.setSDocument(sDocument);
				URI documentPath= this.documentResourceTable.get(sElementId);
				if (documentPath== null)
					throw new ELANImporterException("Cannot retrieve a uri for document "+ sElementId);
				mapper.setResourceURI(documentPath);
				PepperModuleProperties props = new PepperModuleProperties();
				URI specparamsuri = this.getSpecialParams();
				props.addProperties(specparamsuri);
				mapper.setProps(props);
				mapper.mapSDocument();
			}
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
}
