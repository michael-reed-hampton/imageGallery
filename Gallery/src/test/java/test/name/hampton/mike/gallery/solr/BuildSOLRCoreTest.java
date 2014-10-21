package test.name.hampton.mike.gallery.solr;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Observable;
import java.util.Observer;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.exception.InvalidConfigurationException;
import name.hampton.mike.gallery.solr.BuildSOLRCore;
import name.hampton.mike.search.SearchException;

import org.apache.http.auth.BasicUserPrincipal;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;



/**
 * This is not a unit test but an integration test.
 * 
 * This requires that solr be running on http://localhost:8983/solr, and that  it is set up per the 
 * solr-image-index.zip in this project. 
 * 
 * @author mike.hampton
 *
 */
public class BuildSOLRCoreTest extends TestCase {
	
	// look at http://lucidworks.com/blog/indexing-with-solrj/
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public void testEnsureCore() throws SolrServerException, IOException, InvalidConfigurationException, SearchException{
		
		ApplicationConfiguration.getSingleton().setDefaultConfigurationKey("testdefaultKey");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.BASE_DIR, "C:\\Users\\mike.hampton\\Pictures\\temp");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.SOLR_URL, "http://localhost:8983/solr");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.THUMBNAIL_DIR, "C:\\temp");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.THUMBNAIL_HEIGHT, "100");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.THUMBNAIL_WIDTH, "100");
		
		if(true){ // change this to run the test.  I did not want to run it every time I build
			long start = System.currentTimeMillis();
			Principal principal = new BasicUserPrincipal("user"); 

			String solrURL = ApplicationConfiguration.getSingleton().lookupConfigurationValue(principal.getName(), name.hampton.mike.gallery.GalleryApplication.SOLR_URL);
			BuildSOLRCore solrCoreBuilder = new BuildSOLRCore(solrURL);
			
			Observer observer = new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					logger.debug("Observable update: " + arg);
				}
			};
			solrCoreBuilder.addObserver(observer );
			
			solrCoreBuilder.ensureCore("C:\\Users\\mike.hampton\\Pictures\\temp", false);
			// solrCoreBuilder.ensureCore("C:\\Users\\mike.hampton\\Pictures");
			long end = System.currentTimeMillis();
			
			System.out.println("It took " + ((end-start)/1000) + " seconds.");
		}
		assertTrue(true);		
	}

	public void testEnsureCoreWithReindex() throws SolrServerException, IOException, InvalidConfigurationException, SearchException{
		
		ApplicationConfiguration.getSingleton().setDefaultConfigurationKey("testdefaultKey");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.BASE_DIR, "C:\\Users\\mike.hampton\\Pictures\\temp");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.SOLR_URL, "http://localhost:8983/solr");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.THUMBNAIL_DIR, "C:\\temp");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.THUMBNAIL_HEIGHT, "100");
		ApplicationConfiguration.getSingleton().setConfigurationValue("testdefaultKey", name.hampton.mike.gallery.GalleryApplication.THUMBNAIL_WIDTH, "100");

		if(true){ // change this to run the test.  I did not want to run it every time I build
			long start = System.currentTimeMillis();
			Principal principal = new BasicUserPrincipal("user"); 

			String solrURL = ApplicationConfiguration.getSingleton().lookupConfigurationValue(principal.getName(), name.hampton.mike.gallery.GalleryApplication.SOLR_URL);
			BuildSOLRCore solrCoreBuilder = new BuildSOLRCore(solrURL);
			
			Observer observer = new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					logger.debug("Observable update: " + arg);
				}
			};
			solrCoreBuilder.addObserver(observer );
			
			String myBaseDir = ApplicationConfiguration.getSingleton().lookupConfigurationValue(principal.getName(), name.hampton.mike.gallery.GalleryApplication.BASE_DIR);
			File myBaseDirFile = new File(myBaseDir);
			myBaseDir = myBaseDirFile.getCanonicalPath();
			solrCoreBuilder.ensureCore(myBaseDir, true);
			long end = System.currentTimeMillis();
			
			System.out.println("It took " + ((end-start)/1000) + " seconds.");
		}
		assertTrue(true);		
	}
	
}