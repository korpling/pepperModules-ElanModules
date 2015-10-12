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

import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * @author Tom Ruette
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name = "ElanImporterComponent", factory = "PepperImporterComponentFactory")
public class ElanImporter extends PepperImporterImpl implements PepperImporter {
	/** all file endings supported by this importer **/
	public static final String[] ELAN_FILE_ENDINGS = { "eaf", "xml" };

	public ElanImporter() {
		super();
		this.setName("ElanImporter");
		setSupplierContact(URI.createURI("saltnpepper@lists.hu-berlin.de"));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-ELANModules"));
		setDesc("This importer transforms data in ELAN format to a Salt model. ");
		this.addSupportedFormat("elan", "4.5.0", null);
		this.setProperties(new ElanImporterProperties());

		// register file endings to be imported
		for (String ending : ELAN_FILE_ENDINGS)
			this.getSDocumentEndings().add(ending);
	}

	/**
	 * Creates a mapper of type {@link Elan2SaltMapper}. {@inheritDoc
	 * PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId) {
		Elan2SaltMapper mapper = new Elan2SaltMapper();
		return (mapper);
	}
}
