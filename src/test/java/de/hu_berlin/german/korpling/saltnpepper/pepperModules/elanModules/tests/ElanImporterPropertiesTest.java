package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.tests;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.ElanImporterProperties;

public class ElanImporterPropertiesTest extends TestCase
{

	private ElanImporterProperties fixture;

	public ElanImporterProperties getFixture() {
		return fixture;
	}

	public void setFixture(ElanImporterProperties fixture) {
		this.fixture = fixture;
	}
	
	public void setUp()
	{
		this.setFixture(new ElanImporterProperties());
	}
	
	public void testGetIgnoreTierNames()
	{
		String propVal= "firstTier,secondTier, thirdTier,          fourthTier      ";
		Properties props= new Properties();
		props.put(ElanImporterProperties.PROP_IGNORE_TIERNAMES, propVal);
		this.getFixture().setPropertyValues(props);
		
		List<String> ignoreTierNames= this.getFixture().getIgnoreTierNames();
		assertNotNull(ignoreTierNames);
		assertEquals(4, ignoreTierNames.size());
		assertTrue(ignoreTierNames.contains("firstTier"));
		assertTrue(ignoreTierNames.contains("secondTier"));
		assertTrue(ignoreTierNames.contains("thirdTier"));
		assertTrue(ignoreTierNames.contains("fourthTier"));
	}
}
