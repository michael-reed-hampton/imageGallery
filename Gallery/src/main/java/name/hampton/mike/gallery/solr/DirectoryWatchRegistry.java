package name.hampton.mike.gallery.solr;

import java.io.IOException;
import java.util.HashMap;

public class DirectoryWatchRegistry {
	
	// Note, this needs to be a REAL singleton
	private static DirectoryWatchRegistry singleton = new DirectoryWatchRegistry();
	
	private HashMap<String, DirectoryWatch> configuration = new HashMap<String, DirectoryWatch>();
	
	private int seconds = 2;
	
	private DirectoryWatchRegistry()
	{		
	}
	
	public static DirectoryWatchRegistry getSingleton() {
		return singleton;
	}
	
	public void setDirectoryWatchDefaultSeconds(int seconds){
		this.seconds = seconds;
	}
	
	public DirectoryWatch getDirectoryWatch(
			SOLRIndexer indexer, 
			String pathString) throws IOException {
		String key = indexer.getBaseURL() + pathString;		
		DirectoryWatch watch = configuration.get(key);
		if(null==watch){
			watch = new DirectoryWatch(indexer, pathString, seconds);
			configuration.put(key,watch);
		}
		return watch;
	}	
}
