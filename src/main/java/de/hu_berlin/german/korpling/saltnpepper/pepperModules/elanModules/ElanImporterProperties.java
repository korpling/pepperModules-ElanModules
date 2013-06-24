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

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperty;

/**
 * Defines the properties to be used for the {@link GenericXMLImporter}. 
 * @author Florian Zipser
 *
 */
public class ElanImporterProperties extends PepperModuleProperties 
{
	public static final String PREFIX="elan.importer.";
	
	public static final String PROP_PRIMARY_TEXT_TIER_NAME=PREFIX+"primTextTierName";
	public static final String PROP_SEGMENTATION_TIERNAMES=PREFIX+"segTierNames";
	public static final String PROP_IGNORE_TIERNAMES=PREFIX+"ignoreTierNames";
	public static final String PROP_ADD_SORDERRELATION=PREFIX+"addSOrderRelation";
	public static final String PROP_GRID_STRUCTURE=PREFIX+"gridStructure";
	
	public ElanImporterProperties()
	{
		this.addProperty(new PepperModuleProperty<String>(PROP_PRIMARY_TEXT_TIER_NAME, String.class, "Name of the tier containing the primary text.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SEGMENTATION_TIERNAMES, String.class, "Names of the tiers that will be used as segmentation layers.",false));
		this.addProperty(new PepperModuleProperty<String>(PROP_IGNORE_TIERNAMES, String.class, "Names of the tiers that will be ignored.", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_ADD_SORDERRELATION, Boolean.class, "Determines if, this module shall add SOrderRelations to all tokens (segmentation layers).",true, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_GRID_STRUCTURE, String.class, "How the annotation levels are bundled in layers.", false));
	}
	
	public String getPrimTextTierName()
	{
		return((String)this.getProperty(PROP_PRIMARY_TEXT_TIER_NAME).getValue());
	}
	
	public List<String> getSegmentationTierNames()
	{
		List<String> retVal= new Vector<String>();;
		String rawNames= (String)this.getProperty(PROP_SEGMENTATION_TIERNAMES).getValue();
		if (rawNames!= null)
		{
			String[] rawNamesArr= rawNames.split(",");
			if (rawNamesArr!= null)
			{
				for (String rawName: rawNamesArr)
				{
					retVal.add(rawName.trim());
				}
			}
			
		}
		return(retVal);
	}
	
	public List<String> getIgnoreTierNames()
	{
		List<String> retVal= new Vector<String>();;
		String rawNames= (String)this.getProperty(PROP_IGNORE_TIERNAMES).getValue();
		if (rawNames!= null)
		{
			String[] rawNamesArr= rawNames.split(",");
			if (rawNamesArr!= null)
			{
				for (String rawName: rawNamesArr)
				{
					retVal.add(rawName.trim());
				}
			}
			
		}
		return(retVal);
	}
	
	public boolean isAddSOrderRelation()
	{
		return((Boolean)this.getProperty(PROP_ADD_SORDERRELATION).getValue());
	}
	
	public HashMap<String, String[]> getGridStructure()
	{
		HashMap<String, String[]> retVal= new HashMap<String, String[]>();;
		String raw= (String)this.getProperty(PROP_GRID_STRUCTURE).getValue();
		if (raw!= null)
		{
			String[] rawLayers= raw.split(";");
			if (rawLayers!= null)
			{
				for (String rawLayer: rawLayers)
				{
					String key = rawLayer.trim().split(" ")[0];
					String rawValues = rawLayer.substring(key.length());
					String[] values = rawValues.trim().split(",");
					retVal.put(key, values);
				}
			}
		}
		return(retVal);
	}
}
