package org.bitbucket.krausening;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In brewing, krausening (KROI-zen-ing) refers to adding a small amount of fresh wort to prime finished beer for carbonation. 
 * In Java, Krausening is a project to populate finished archives for deployment. This approach allows Properties files to be 
 * externalized from deployment units, enabling the same deployment unit to be leveraged repeatedly without the need to rebuild 
 * or hacking the archive.
 * 
 * To use, you need to minimally set a system property "KRAUSENING_BASE", which points to a directory where your {@link Proeprties}
 * files will be located.  Krausening will load those up and make them available via the {@code getProperties(<file name>)} method.
 * 
 * You can then use a system property called "KRAUSENING_EXTENSIONS" to set up extensions to the {@link Proeprties} files located 
 * in "KRAUSENING_BASE".  The first the base properties will be loaded, with anything in the extensions location being added on 
 * to the top.  This allows value to be added or overridden, which is especially useful when you have a standard configuration
 * defined in your base files, but need to specialize some values for different deployments.
 * 
 * Only .properties files will be loaded.  Any other file encountered will be skipped.
 */
public final class Krausening {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Krausening.class);
	
	/** Location of the base set of {@link Properties} files. */
	public static final String BASE_LOCATION = "KRAUSENING_BASE";
	
	/** Location of a set of extension {@link Properties} files. */
	public static final String EXTENSIONS_LOCATION = "KRAUSENING_EXTENSIONS";
	
	/** Value of a set a master encryption password. */
	public static final String KRAUSENING_PASSWORD = "KRAUSENING_PASSWORD";
	
	/** Contains the set of all properties managed by the base + extension locations. */
	private Map<String, Properties> managedProperties;
	
	/** The location which contains base properties. */
	private String baseLocation;
	
	/** The location which contains extension properties. */
	private String extensionsLocation;
	
	/** Whether or not KRAUSENING_PASSWORD is non-blank. */
	private boolean hasMasterPassword;
	
	/** Singleton instance of this class. */
	private static Krausening instance = new Krausening();
	
	/**
	 * Private constructor to prevent external construction of this singleton instance, automatically
	 * triggers a loading of properties.
	 */
	private Krausening() {
		loadProperties();
	}
	
	/**
	 * Returns the singleton instance of this class.
	 * @return singleton reference to Krausening
	 */
	public static Krausening getInstance() {
		return instance;
	}
	
	/**
	 * Loads the properties defined by the base and extension locations, making them 
	 * accessible for use by Krausening clients.  This method can also be leveraged to
	 * refresh values at any time.
	 */
	public void loadProperties() {
		long start = System.currentTimeMillis();
		LOGGER.debug("Loading Krausening properties...");
		
		managedProperties = new ConcurrentHashMap<String, Properties>();
		
		boolean hasLocations = setLocations();
		
		setEncryptionFoundation();
		
		if (hasLocations) {
			File baseLocationAsFile = new File(baseLocation);
			if (baseLocationAsFile.exists()) {
				loadPropertiesFromLocation(baseLocationAsFile);
				
			} else {
				logFileDoesNotExist(baseLocationAsFile, BASE_LOCATION);
				
			}
			
			if (StringUtils.isNotBlank(extensionsLocation)) {
				File extensionsLocationAsFile = new File(extensionsLocation);
				if (extensionsLocationAsFile.exists()) {
					loadPropertiesFromLocation(extensionsLocationAsFile);
					
				} else {
					logFileDoesNotExist(extensionsLocationAsFile, EXTENSIONS_LOCATION);
					
				}
			}  
		}
		
		long stop = System.currentTimeMillis();
		LOGGER.debug("Loaded Krausening properties in " + (stop - start) + "ms");
		
	}
	
	protected void setEncryptionFoundation() {
		String masterPassword = System.getProperty(KRAUSENING_PASSWORD);
		if (StringUtils.isBlank(masterPassword)) {
			LOGGER.warn("No " + KRAUSENING_PASSWORD + " set, Krausening will not support encrypted property values!");
			
		} else {
			LOGGER.warn(KRAUSENING_PASSWORD + " configured, Krausening will support encrypted property values.");
			hasMasterPassword = true;
		}
	}

	/**
	 * Log an error for the file and location type when the location does not exist.
	 * @param file The file that does not exist
	 * @param location The location type (i.e., base or extension)
	 */
	protected void logFileDoesNotExist(File file, String location) {
		LOGGER.error(location + " refers to a location that does not exist: " + file.getAbsolutePath());
		
	}
	
	/**
	 * Load system properties from into member variables.
	 */
	private boolean setLocations() {
		boolean hasLocations = false;
		
		baseLocation = System.getProperty(BASE_LOCATION);
		if (StringUtils.isBlank(baseLocation)) {
			LOGGER.error("Without a " + BASE_LOCATION + " set, Krausening cannot load any properties!");
			
		} else {
			LOGGER.error("Krausening base location: " + baseLocation);
			hasLocations = true;
			
		}
		
		extensionsLocation = System.getProperty(EXTENSIONS_LOCATION);
		if (StringUtils.isBlank(extensionsLocation)) {
			LOGGER.warn("No " + EXTENSIONS_LOCATION + " set...");
			
		} else {
			LOGGER.error("Krausening extensions location: " + extensionsLocation);
			
		}
		
		return hasLocations;
		
	}
	
	/**
	 * Loads all .properties files from the passed location.
	 * @param location the location containing properties files
	 */
	private void loadPropertiesFromLocation(File location) {
		File[] files = location.listFiles((FilenameFilter)new SuffixFileFilter(".properties"));
		
		if ((files == null) || (files.length == 0)) {
			LOGGER.warn("No files were found within: " + location.getAbsolutePath());
			
		} else {
			String fileName = null;
			Reader fileReader = null;
			Properties fileProperties = null;
			for (File file : files) {
				try {
					fileName = file.getName();
					fileProperties = (managedProperties.containsKey(fileName)) 
							? managedProperties.get(fileName) : createEmptyProperties();
					fileReader = new FileReader(file);
					fileProperties.load(fileReader);
					managedProperties.put(fileName, fileProperties);
					
				} catch (IOException e) {
					LOGGER.error("Could not read the file: " + file.getAbsolutePath(), e);
					
				} finally {
					IOUtils.closeQuietly(fileReader);
					
				}				
			}			
		}
	}
	
	/**
	 * Creates an empty Properties file, either standard or encrypted, based on whether or not the master password
	 * is set.
	 * @return An empty properties instance
	 */
	private Properties createEmptyProperties() {
		Properties properties;
		if (hasMasterPassword) {
			//TODO: could externalize this so the type is configurable:
			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
			encryptor.setPassword(System.getProperty(KRAUSENING_PASSWORD));			
			properties = new EncryptableProperties(encryptor);
			
		} else {
			properties = new Properties();
		}
		
		return properties;
	}
		
	/**
	 * Returns the properties file loaded by Krausening for given file name.
	 * @param propertiesFileName The file name to retrieve
	 * @return The file or null if that file name is not know
	 */
	public Properties getProperties(String propertiesFileName) {
		return managedProperties.get(propertiesFileName);
		
	}
	
}
