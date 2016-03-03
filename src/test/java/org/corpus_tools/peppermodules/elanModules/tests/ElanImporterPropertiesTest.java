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
package org.corpus_tools.peppermodules.elanModules.tests;

import java.util.List;
import java.util.Properties;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.peppermodules.elanModules.ElanImporterProperties;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class ElanImporterPropertiesTest extends TestCase {

	private ElanImporterProperties fixture;

	public ElanImporterProperties getFixture() {
		return fixture;
	}

	public void setFixture(ElanImporterProperties fixture) {
		this.fixture = fixture;
	}
	
	@Before
	public void setUp() {
		this.setFixture(new ElanImporterProperties());
	}

	@Test
	public void testGetIgnoreTierNames() {
		String propVal = "firstTier,secondTier, thirdTier,          fourthTier      ";
		Properties props = new Properties();
		props.put(ElanImporterProperties.PROP_IGNORE_TIERNAMES, propVal);
		getFixture().setPropertyValues(props);

		List<String> ignoreTierNames = getFixture().getIgnoreTierNames();
		assertNotNull(ignoreTierNames);
		assertEquals(4, ignoreTierNames.size());
		assertTrue(ignoreTierNames.contains("firstTier"));
		assertTrue(ignoreTierNames.contains("secondTier"));
		assertTrue(ignoreTierNames.contains("thirdTier"));
		assertTrue(ignoreTierNames.contains("fourthTier"));
	}
	
	@Test
	public void test_getAnnotationNamespace_default(){
		ElanImporterProperties props = this.getFixture();
		assertNull(props.getAnnotationNamespace());
	}
}
