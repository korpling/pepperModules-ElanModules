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
package org.corpus_tools.peppermodules.elanModules;

import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

/**
 * Defines the properties to be used for the {@link GenericXMLImporter}.
 * 
 * @author Tom Ruette
 *
 */
public class ElanImporterProperties extends PepperModuleProperties {
	public static final String PREFIX = "elan.importer.";

	public static final String PROP_PRIMARY_TEXT_TIER_NAME = PREFIX + "primTextTierName";
	public static final String PROP_SEGMENTATION_TIERNAMES = PREFIX + "segTierNames";
	public static final String PROP_IGNORE_TIERNAMES = PREFIX + "ignoreTierNames";
	public static final String PROP_LINKED_FOLDER = PREFIX + "linkedFolder";

	public ElanImporterProperties() {
		this.addProperty(new PepperModuleProperty<String>(PROP_PRIMARY_TEXT_TIER_NAME, String.class, "Name of the tier containing the primary text.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SEGMENTATION_TIERNAMES, String.class, "Names of the tiers that will be used as segmentation layers.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_IGNORE_TIERNAMES, String.class, "Names of the tiers that will be ignored.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LINKED_FOLDER, String.class, "Location of the linked files.", false));
	}

	public String getPrimTextTierName() {
		return ((String) this.getProperty(PROP_PRIMARY_TEXT_TIER_NAME).getValue());
	}

	public List<String> getSegmentationTierNames() {
		List<String> retVal = new ArrayList<>();
		;
		String rawNames = (String) this.getProperty(PROP_SEGMENTATION_TIERNAMES).getValue();
		if (rawNames != null) {
			String[] rawNamesArr = rawNames.split(",");
			if (rawNamesArr != null) {
				for (String rawName : rawNamesArr) {
					retVal.add(rawName.trim());
				}
			}

		}
		return (retVal);
	}

	public List<String> getIgnoreTierNames() {
		List<String> retVal = new ArrayList<>();
		;
		String rawNames = (String) this.getProperty(PROP_IGNORE_TIERNAMES).getValue();
		if (rawNames != null) {
			String[] rawNamesArr = rawNames.split(",");
			if (rawNamesArr != null) {
				for (String rawName : rawNamesArr) {
					retVal.add(rawName.trim());
				}
			}

		}
		return (retVal);
	}

	public String getLinkedFolder() {
		return ((String) this.getProperty(PROP_LINKED_FOLDER).getValue());
	}

}
