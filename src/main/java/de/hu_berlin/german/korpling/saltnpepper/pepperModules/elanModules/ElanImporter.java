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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.CorpusDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
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
@Service(value=PepperImporter.class)
public class ElanImporter extends PepperImporterImpl implements PepperImporter
{
	public ElanImporter()
	{
		super();
		this.name= "ElanImporter";
		this.addSupportedFormat("elan", "4.5.0", null);
	}
	
	/**
	 * This method is called by Pepper at the start of conversion process. 
	 * It shall create the structure the corpus to import. That means creating all necessary {@link SCorpus}, 
	 * {@link SDocument} and all Relation-objects between them. The path to the corpus to import is given by
	 * {@link #getCorpusDefinition()} and {@link CorpusDefinition#getCorpusPath()}.
	 * 
	 * Often the corpus structure is not given by formats, a common use to deal with it is to map the corpus structure to the file 
	 * structure. Therefore we offer the method {@link #createCorpusStructure(org.eclipse.emf.common.util.URI, SElementId, org.eclipse.emf.common.util.EList)}.
	 * This method reads a file structure and maps each folder to a {@link SCorpus} object. Each file having an ending contained in the passed list
	 * will be added to the returned table and correspond to a {@link SDocument} object for later access.
	 * Here is a snippet to use the method.
	 * <code> 
	 * EList<String> endings= new BasicEList<String>();
	 * endings.add("FORMAT1");
	 * endings.add("FORMAT2");
	 * ...
	 * documentResourceTable= this.createCorpusStructure(this.getCorpusDefinition().getCorpusPath(), null, endings);
	 * </code>
	 * @param an empty graph given by Pepper, which shall contains the corpus structure
	 */
	@Override
	public void importCorpusStructure(SCorpusGraph sCorpusGraph)
			throws PepperModuleException
	{
		//TODO /6/: implement this method 	
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
			//TODO /8/: create your own mapping
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
}
