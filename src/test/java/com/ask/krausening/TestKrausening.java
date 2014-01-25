package com.ask.krausening;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Test;

public class TestKrausening {

	private static final String VALUE_NEW_VALUE = "new.value";
	private static final String KEY_NEWLY_ADDED_IN_EXTENSIONS = "newly.added.in.extensions";
	private static final String VALUE_BAR = "bar";
	private static final String KEY_FOO = "foo";
	private static final String BASE_TEST_LOCATION = "./src/test/resources/base";
	private static final String EXTENSIONS_TEST_LOCATION = "./src/test/resources/extensions";
	private static final String NONEXISTENT_LOCATION = "./src/test/resources/does-not-exist";
	private static final String EMPTY_LOCATION = "./src/test/resources/empty";
	private static final String NOT_JUST_PROPERTIES_FILES_LOCATION = "./src/test/resources/not-just-properties-files";
	private static final String NO_LOCATION = "";

	private static final String DEFAULT_EXAMPLE_PROPERTIES = "example.properties";
	private static final String EMPTY_PROPERTIES = "empty.properties";
	
	@Test
	public void testBaseLocationFileRead() {
		System.setProperty(Krausening.BASE_LOCATION, BASE_TEST_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, NO_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		assertNotNull(properties);
		
	}
	
	@Test
	public void testBaseLocationPropertyRead() {
		System.setProperty(Krausening.BASE_LOCATION, BASE_TEST_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, NO_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		Object value = properties.get(KEY_FOO);
		assertEquals(VALUE_BAR, value);
		
	}
	
	@Test
	public void testExtensionLocationPropertyReadOfNonOverridenValue() {
		System.setProperty(Krausening.BASE_LOCATION, BASE_TEST_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, EXTENSIONS_TEST_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		Object value = properties.get(KEY_FOO);
		assertEquals(VALUE_BAR, value);
		
	}
	
	@Test
	public void testExtensionLocationPropertyReadOfOverridenValue() {
		System.setProperty(Krausening.BASE_LOCATION, BASE_TEST_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, EXTENSIONS_TEST_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		Object value = properties.get("override.me");
		assertEquals("some-localized-value", value);
		
	}
	
	@Test
	public void testExtensionLocationPropertyReadOfNewValue() {
		System.setProperty(Krausening.BASE_LOCATION, BASE_TEST_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, EXTENSIONS_TEST_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		Object value = properties.get(KEY_NEWLY_ADDED_IN_EXTENSIONS);
		assertEquals(VALUE_NEW_VALUE, value);
		
	}
	
	@Test
	public void testFileReadWithoutAnyLocation() {
		System.setProperty(Krausening.BASE_LOCATION, NO_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, NO_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		assertNull(properties);
		
	}
	
	@Test
	public void testFileReadWithNonexistantBaseLocation() {
		System.setProperty(Krausening.BASE_LOCATION, NONEXISTENT_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, NO_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		assertNull(properties);
		
	}
	
	@Test
	public void testFileReadWithNonexistantExtensionsLocation() {
		System.setProperty(Krausening.BASE_LOCATION, BASE_TEST_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, NONEXISTENT_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		Object value = properties.get(KEY_NEWLY_ADDED_IN_EXTENSIONS);
		assertNull(value);
		
	}
	
	@Test
	public void testFileReadOfEmptyProperties() {
		System.setProperty(Krausening.BASE_LOCATION, BASE_TEST_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, NO_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(EMPTY_PROPERTIES);
		Object value = properties.get(KEY_NEWLY_ADDED_IN_EXTENSIONS);
		assertNull(value);
		
	}
	
	@Test
	public void testFileReadWithEmptyBaseLocation() {
		System.setProperty(Krausening.BASE_LOCATION, EMPTY_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, NO_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		assertNull(properties);
		
	}
	
	@Test
	public void testFileReadWithEmptyExtensionsLocation() {
		System.setProperty(Krausening.BASE_LOCATION, BASE_TEST_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, EMPTY_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties(DEFAULT_EXAMPLE_PROPERTIES);
		Object value = properties.get(KEY_FOO);
		assertEquals(VALUE_BAR, value);
		
	}
	
	@Test
	public void testFileLoadingWhenNonPropertiesFilesPresent() {
		System.setProperty(Krausening.BASE_LOCATION, NOT_JUST_PROPERTIES_FILES_LOCATION);
		System.setProperty(Krausening.EXTENSIONS_LOCATION, EMPTY_LOCATION);
		Krausening krausening = Krausening.getInstance();
		krausening.loadProperties();
		Properties properties = krausening.getProperties("some-other-configuration-file.xml");
		assertNull(properties);
		
	}
	
	
}
