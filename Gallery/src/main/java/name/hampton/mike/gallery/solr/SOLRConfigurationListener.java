package name.hampton.mike.gallery.solr;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.ConfigurationChangeEvent;
import name.hampton.mike.gallery.ConfigurationListener;
import name.hampton.mike.gallery.exception.InvalidConfigurationException;
import name.hampton.mike.search.SearchException;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reacts to configuration changes.  In the case of SOLR, it may reindex or deindex an item or several items.
 * It may even create an entirely new index.
 * 
 * @author mike.hampton
 *
 */
public class SOLRConfigurationListener implements ConfigurationListener {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Override
	public void configurationChanged(ConfigurationChangeEvent scce) {		
				
		List<String> keys = scce.getChangedKeys();
		
		// Only need to react if name.hampton.mike.gallery.GalleryApplication.SOLR_URL or name.hampton.mike.gallery.GalleryApplication.BASE_DIR
		// changed
		if(	keys.contains(name.hampton.mike.gallery.GalleryApplication.BASE_DIR) || 
			keys.contains(name.hampton.mike.gallery.GalleryApplication.SOLR_URL) ) {
			String userName = scce.getPrincipal().getName();
			String solrURL = ApplicationConfiguration.getSingleton().lookupConfigurationValue(userName, name.hampton.mike.gallery.GalleryApplication.SOLR_URL);
			BuildSOLRCore solrCoreBuilder = new BuildSOLRCore(solrURL);
			Observer observer = new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					logger.debug("Observable update: " + arg);
				}
			};
			solrCoreBuilder.addObserver(observer );
			String myBaseDir = ApplicationConfiguration.getSingleton().lookupConfigurationValue(userName, name.hampton.mike.gallery.GalleryApplication.BASE_DIR);
			File myBaseDirFile = new File(myBaseDir);
			try {
				myBaseDir = myBaseDirFile.getCanonicalPath();
				solrCoreBuilder.ensureCore(myBaseDir, true);
			} catch (SolrServerException e) {
				logger.error("",e);
			} catch (IOException e) {
				logger.error("",e);
			} catch (InvalidConfigurationException e) {
				logger.error("",e);
			} catch (SearchException e) {
				logger.error("",e);
			}
		}
	}
}
