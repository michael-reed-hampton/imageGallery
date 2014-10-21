package name.hampton.mike.search;

import java.io.File;
import java.io.IOException;
import java.security.Principal;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.GalleryApplication;
import name.hampton.mike.gallery.ValidationTools;
import name.hampton.mike.gallery.exception.InvalidConfigurationException;
import name.hampton.mike.gallery.exception.InvalidPathException;
import name.hampton.mike.gallery.solr.SOLRData;
import name.hampton.mike.gallery.solr.SOLRIndexer;


/**
 * Yet another cheap singleton that needs to be converted to a real one.  This one deals with
 * providing the implementation for the search functionality.
 * 
 * @author mike.hampton
 *
 */
public class SearchProvider {
	
	// Note, this needs to be a REAL singleton
	private static SearchProvider singleton = new SearchProvider();
	
	public static SearchProvider getSingleton() {
		return singleton;
	}
	
	private SearchProvider()
	{
		System.out.println("*************************************************************************************************");
		System.out.println("SearchProvider is ='" + this.getClass().getName() );
		System.out.println("*************************************************************************************************");
	}
	
	/**
	 * 
	 * 
	 * @param principal
	 * @return
	 * @throws InvalidConfigurationException
	 */
	public SearchIntf getSearchIntf(Principal principal) throws InvalidConfigurationException{
		SOLRData solrData = new SOLRData(getBaseDir(principal), getSOLRURL(principal)); 
		return solrData;
	}
	
	/**
	 * 
	 * 
	 * @param principal
	 * @return
	 * @throws InvalidConfigurationException
	 */
	public SearchIndexIntf getSearchIndexIntf(Principal principal) throws InvalidConfigurationException{
		// notify someone
		
		String userName = null;
				
		if(null == principal){
			userName = ApplicationConfiguration.getSingleton().getDefaultConfigurationKey();
		}
		else{
			userName = principal.getName();
		}
		
		String myBaseDir = ApplicationConfiguration.getSingleton().lookupConfigurationValue(userName, name.hampton.mike.gallery.GalleryApplication.BASE_DIR);
		File myBaseDirFile = new File(myBaseDir);
		try {
			myBaseDir = myBaseDirFile.getCanonicalPath();
		} catch (IOException e) {
			throw new InvalidConfigurationException("Error getting canonical path for path in application configuration: " + myBaseDirFile, e);
		}
		
		
		String solrURL = ApplicationConfiguration.getSingleton().lookupConfigurationValue(userName, name.hampton.mike.gallery.GalleryApplication.SOLR_URL);
		SOLRIndexer indexer;
		try {
			indexer = new SOLRIndexer(solrURL, myBaseDir);
		} catch (IOException e) {
			throw new InvalidConfigurationException("Error getting indexer for solrURL = "+solrURL+" and base path = " + 
					myBaseDir + " in application configuration: " + myBaseDirFile, e);
		}
		return indexer;
	}

	protected File getBaseDir(Principal principal) throws InvalidPathException {
		String userName = principal.getName();
		String baseDirString =  ApplicationConfiguration.getSingleton().lookupConfigurationValue(userName, GalleryApplication.BASE_DIR);
		// NOTE, if baseDirString is null, then this indicates that the application is not configured correctly.
		if(null == baseDirString)
		{
			throw new InvalidPathException("baseDirString for user "+userName+
					" is null.  This indicates that the user has not configured a "
					+ "value, and the application is not configured with a default value.  "
					+ "Go to the configuration page of the application, or the configuration page for the user"
					+ " to set the '" + GalleryApplication.BASE_DIR + "' value.");
		}		
		File newBaseDir = ValidationTools.getValidDirectory(baseDirString);
		return newBaseDir;
	}

	protected String getSOLRURL(Principal principal) throws InvalidConfigurationException {
		String userName = principal.getName();
		String solrURL =  ApplicationConfiguration.getSingleton().lookupConfigurationValue(userName, GalleryApplication.SOLR_URL);
		// NOTE, if this is null, then this indicates that the application is not configured correctly.
		if(null == solrURL)
		{
			throw new InvalidConfigurationException("solrURL for user "+userName+
					" is null.  This indicates that the user has not configured a "
					+ "value, and the application is not configured with a default value.  "
					+ "Go to the configuration page of the application, or the configuration page for the user"
					+ " to set the '" + GalleryApplication.SOLR_URL + "' value.");
		}
		return solrURL;
	}
}
