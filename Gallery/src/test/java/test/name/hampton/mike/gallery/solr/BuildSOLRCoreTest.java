package test.name.hampton.mike.gallery.solr;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import name.hampton.mike.gallery.solr.BuildSOLRCore;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class BuildSOLRCoreTest extends TestCase {
	
	// look at http://lucidworks.com/blog/indexing-with-solrj/
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public void testEnsureCore() throws SolrServerException, IOException{
		if(true){ // change this to run the test.  I did not want to run it every time I build
			long start = System.currentTimeMillis();
			BuildSOLRCore solrCoreBuilder = new BuildSOLRCore("http://localhost:8983/solr");
			
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

	public void testEnsureCoreWithReindex() throws SolrServerException, IOException{
		if(true){ // change this to run the test.  I did not want to run it every time I build
			long start = System.currentTimeMillis();
			BuildSOLRCore solrCoreBuilder = new BuildSOLRCore("http://localhost:8983/solr");
			
			Observer observer = new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					logger.debug("Observable update: " + arg);
				}
			};
			solrCoreBuilder.addObserver(observer );
			
			solrCoreBuilder.ensureCore("C:\\Users\\mike.hampton\\Pictures\\temp", true);
			// solrCoreBuilder.ensureCore("C:\\Users\\mike.hampton\\Pictures");
			long end = System.currentTimeMillis();
			
			System.out.println("It took " + ((end-start)/1000) + " seconds.");
		}
		assertTrue(true);		
	}
	
}