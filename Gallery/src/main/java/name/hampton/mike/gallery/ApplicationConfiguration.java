package name.hampton.mike.gallery;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This will be a true singleton with respect to the application.
 * 
 * This static memory implementation will not be sufficient for clustered instances.
 * 
 * The application is expected to have only one reference to this in a memory space, 
 * so if there is any expectation of needing multiple values for a single key, expect 
 * that additional work would need to be done.  I am using the username as the initial 
 * key to get to configuration.  This indicates that every user will have a configuration.
 * So the application configuration is mostly by contract.
 * 
 *  For the Gallery app, I have put the configurationkeys into the web.xml as an init-param 
 *  to the name.hampton.mike.gallery.ConfigureServlet.  Each of these values would have a
 *  value for each user.  I will decide how to put together persistence of these values later...
 * 
 * 
 * @author mike.hampton
 *
 */
public class ApplicationConfiguration {

	// Note, this needs to be a REAL singleton
	private static ApplicationConfiguration singleton = new ApplicationConfiguration();	
	private HashMap<String, Object> configuration = new HashMap<String, Object>();
	private String defaultKey;
	File configFile = null;
	
	public static ApplicationConfiguration getSingleton() {
		return singleton;
	}
	
	private ApplicationConfiguration()
	{
		String tempDir = System.getProperty("java.io.tmpdir");
		configFile = new File(tempDir, "galleryConfig.json");
		System.out.println("*************************************************************************************************");
		System.out.println("ApplicationConfiguration is being obtained from configFile='" + configFile.getAbsolutePath() +
				"' + this is currently HARDCODED  in " + this.getClass().getName() + ", (yeah, I know...) so to change it "
						+ "you need to reploace this implementation.");
		System.out.println("*************************************************************************************************");
		loadThis();
	}

	public HashMap<String, Object> getConfiguration() {
		return configuration;
	}

	public void setDefaultConfigurationKey(String defaultKey) {
		this.defaultKey = defaultKey;
	}

	public String setConfigurationValue(String majorKey, String configurationKey, String configurationValue) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> majorKeyProperties =  (HashMap<String, String>) getConfiguration().get(majorKey);
		if(null == majorKeyProperties)
		{
			majorKeyProperties = new HashMap<String, String>(); 
			getConfiguration().put(majorKey, majorKeyProperties);
		}
		majorKeyProperties.put(configurationKey, configurationValue);
		saveThis();
		
		return configurationValue;
	}
	
	public String lookupConfigurationValue(String majorKey, String configurationKey) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> majorKeyProperties =  (HashMap<String, String>) getConfiguration().get(majorKey);
		if(null == majorKeyProperties)
		{
			majorKeyProperties = new HashMap<String, String>(); 
			getConfiguration().put(majorKey, majorKeyProperties);
		}
		String configurationValue = null;		
		configurationValue = (String)majorKeyProperties.get(configurationKey);
		
		// Allow a default configuration to be used.  If the application does not set the 'defaultKey',
		// then this is not used.  If it does, configuration values for the default are set like any other,
		// but will be returned if any other configuration has the same key, but no corresponding value.
		// Note: majorKey != defaultKey prevents infinite recursion
		if(null == configurationValue && null != defaultKey && majorKey != defaultKey)
		{
			configurationValue = lookupConfigurationValue(defaultKey, configurationKey);
		}		
		return configurationValue;
	}
	
	
	// I am tired of reentering config...
	private void saveThis()
	{		
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println("*************************************************************************************************");
			System.out.println("ApplicationConfiguration is being obtained from configFile='" + configFile.getAbsolutePath() +
					"' + this is currently HARDCODED  in " + this.getClass().getName() + ", (yeah, I know...) so to change it "
							+ "you need to reploace this implementation.");
			System.out.println("*************************************************************************************************");
			mapper.writeValue(configFile, configuration);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	@SuppressWarnings("unchecked")
	private void loadThis()
	{		
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println("*************************************************************************************************");
			System.out.println("ApplicationConfiguration is being obtained from configFile='" + configFile.getAbsolutePath() +
					"' + this is currently HARDCODED  in " + this.getClass().getName() + ", (yeah, I know...) so to change it "
							+ "you need to reploace this implementation.");
			System.out.println("*************************************************************************************************");
			configuration = mapper.readValue(configFile, configuration.getClass());		
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
