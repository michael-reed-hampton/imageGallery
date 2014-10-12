package name.hampton.mike.gallery.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.solr.BuildSOLRCore;
import name.hampton.mike.gallery.solr.SOLRIndexingEvent;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes anything that needs to be initialized
 * 
 * Reindexes any solr cores as needed.
 * 
 * 
 * @author mike.hampton
 *
 */
public class ApplicationListener implements ServletContextListener {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	

	@Override
	public void contextInitialized(ServletContextEvent sce) {
	    try {
			String defaultKey =
					sce.getServletContext().getInitParameter(name.hampton.mike.gallery.GalleryApplication.DEFAULT_KEY);
			ApplicationConfiguration.getSingleton().setDefaultConfigurationKey(defaultKey);

			Iterator<String> configurationMajorKeys = ApplicationConfiguration.getSingleton().getConfiguration().keySet().iterator();
	    	
	    	Map<File, Boolean> alreadyindexed = new HashMap<File, Boolean>();
	    	
			Observer observer = new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					SOLRIndexingEvent event = (SOLRIndexingEvent)arg;
					if(event.getEventType().equals(SOLRIndexingEvent.EventType.INDEXING_START)){
						logger.debug("Indexing update the number of files to process is : " + event.getCount() + " for the directory " + event.getPathString() + ". " + event);
					}
					else if(event.getEventType().equals(SOLRIndexingEvent.EventType.INDEXING_COMPLETE)){
						logger.debug("Indexing update: " + event);
					}
					else if(event.getEventType().equals(SOLRIndexingEvent.EventType.INDEXING_ERROR)){
						logger.debug("Indexing Error!: " + event);
					}
					
				}
			};
	    	while(configurationMajorKeys.hasNext()){
	    		String configurationMajorKey = configurationMajorKeys.next();
	    		
				// ****************************************************************
				//TODO: Make this more abstract.  This Should be elsewhere!!!!
	    		// This is also in the configure servlet, this needs to get fixed...
				// ****************************************************************
				String myBaseDir = ApplicationConfiguration.getSingleton().lookupConfigurationValue(configurationMajorKey, name.hampton.mike.gallery.GalleryApplication.BASE_DIR);
				File myBaseDirFile = new File(myBaseDir);
				if(null == alreadyindexed.get(myBaseDirFile)){
					alreadyindexed.put(myBaseDirFile, Boolean.TRUE);
					// Normalize this here.
					myBaseDir = myBaseDirFile.getCanonicalPath();
					ApplicationConfiguration.getSingleton().setConfigurationValue(configurationMajorKey, name.hampton.mike.gallery.GalleryApplication.BASE_DIR, myBaseDir);

					String solrURL = ApplicationConfiguration.getSingleton().lookupConfigurationValue(configurationMajorKey, name.hampton.mike.gallery.GalleryApplication.SOLR_URL);
					BuildSOLRCore solrCoreBuilder = new BuildSOLRCore(solrURL);
					solrCoreBuilder.addObserver(observer);
					
					// This should be done threaded.  This might mean doing it with jms, or on a nother machine - etc.					
					Thread coreBuilderThread = new Thread(new SOLRInitTask(solrCoreBuilder, myBaseDir));
					coreBuilderThread.start();
					
				}
				else{
				}
				// ****************************************************************
				// ****************************************************************
	    	}
	    } catch (Exception e) {
	      logger.error("Error in Application initialization", e);
	    }
	    logger.info("Application initialization complete");	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	    try {
	      } catch (Exception e) {
	        logger.error("Error in Application shutdown", e);
	      }
	      logger.info("Application shutdown complete");
	}
	
	class SOLRInitTask implements Runnable {
		BuildSOLRCore solrCoreBuilder;
		String pathToIndex;
		
		SOLRInitTask(BuildSOLRCore solrCoreBuilder, String pathToIndex){
			this.solrCoreBuilder = solrCoreBuilder;
			this.pathToIndex = pathToIndex;
		}
		
		@Override
		public void run() {
			try {
				solrCoreBuilder.ensureCore(pathToIndex, true);
			} catch (SolrServerException e) {
			      logger.error("Error in Application initialization, could not build/reindex solr for path = '" + pathToIndex, e);
			} catch (IOException e) {
			      logger.error("Error in Application initialization, could not build/reindex solr for path = '" + pathToIndex, e);
			}
		}
	};	

}
