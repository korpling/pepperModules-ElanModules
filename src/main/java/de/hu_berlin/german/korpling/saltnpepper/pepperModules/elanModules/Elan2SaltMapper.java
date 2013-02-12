package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules;

import org.eclipse.emf.common.util.URI;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;

/**
 * This class maps data coming from the ELAN model to a Salt model.
 * @author Florian Zipser
 *
 */
public class Elan2SaltMapper 
{
	//TODO remove when inheriting from PepperMapper
	protected SDocument sDocument= null;
	//TODO remove when inheriting from PepperMapper
	public SDocument getSDocument() {
		return(sDocument);
	}
	//TODO remove when inheriting from PepperMapper
	public void setSDocument(SDocument sDocument) {
		this.sDocument= sDocument;
	}
	//TODO remove when inheriting from PepperMapper
	protected SCorpus sCorpus= null;
	//TODO remove when inheriting from PepperMapper
	public SCorpus getSCorpus() {
		return(sCorpus);
	}
	//TODO remove when inheriting from PepperMapper
	public void setSCorpus(SCorpus sCorpus) {
		this.sCorpus= sCorpus;
	}
	//TODO remove when inheriting from PepperMapper
	protected URI resourceURI= null;
	//TODO remove when inheriting from PepperMapper
	public URI getResourceURI() {
		return(resourceURI);
	}
	//TODO remove when inheriting from PepperMapper
	public void setResourceURI(URI resourceURI) {
		this.resourceURI= resourceURI;
	}
	
	//TODO set @override
	public void mapSCorpus()
	{
		
	}
	
	//TODO set @override
	public void mapSDocument()
	{
		
	}
}