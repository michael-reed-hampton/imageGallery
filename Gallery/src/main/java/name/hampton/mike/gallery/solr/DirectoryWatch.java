package name.hampton.mike.gallery.solr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import name.hampton.mike.WatchDir;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatch extends WatchDir {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private SOLRIndexer indexer;
	
	private IOFileFilter filter;		
	private Boolean DELETE = Boolean.FALSE;
	private Boolean INDEX = Boolean.TRUE;		
	private Map<File, Boolean> buffer = new ConcurrentHashMap<File, Boolean>();		
	private Timer processingTimer;
	private Timer indexingTimer;
	private int seconds = 1;
	private String pathString;	
	
	public DirectoryWatch(
			SOLRIndexer indexer, 
			String pathString, 
			int seconds) throws IOException{
		super(Paths.get(pathString), true);
		this.pathString = pathString;  // <- this is used only in debugging
		this.indexer = indexer;
        this.seconds  = seconds;
	}		
	
	public void startProcessing() {
		if(!isRunprocess()){
			// if it is already processing, just keep going
			setRunprocess(true);
			processingTimer = new Timer();  //At this line a new Thread will be created
			processingTimer.schedule(new ProcessEventsTask(), 0); //delay in milliseconds
			indexingTimer = new Timer();  //At this line a new Thread will be created
			indexingTimer.schedule(new IndexingTask(), 0, seconds*1000); //delay in milliseconds
		}
	}
	
	public void stopProcessing() {
		setRunprocess(false);
	}
	
	@Override
	public void handleDelete(WatchEvent<Path> event, Path child) {
		File childFile = child.toFile();
		if(null==filter || filter.accept(childFile)){
			buffer.put(childFile, DELETE);
		}
	}
	@Override
	public void handleCreate(WatchEvent<Path> event, Path child) {
		handleCreateModify(child.toFile());
	}
	@Override
	public void handleModify(WatchEvent<Path> event, Path child) {
    	if( System.currentTimeMillis()-child.toFile().lastModified() < seconds*1000){
    		handleCreateModify(child.toFile());
    	}
	}
	@Override
	public void handleOverflow(WatchEvent<Path> event, Path child) {
		if(null==filter || filter.accept(child.toFile())){
			logger.debug("Overflow event, what now? event=" + event + ", child=" + child );
		}
	}
	
	public void handleCreateModify(File childFile) {
		// Put a check in to make sure it was actually modified.  Has to have been changed in the last second
		if(null==filter || filter.accept(childFile)){
			buffer.put(childFile, INDEX);
		}
	}

	public IOFileFilter getFilter() {
		return filter;
	}
	public void setFilter(IOFileFilter filter) {
		this.filter = filter;
	}

	/**
	 * This is the buffer/meter
	 * @author mike.hampton
	 *
	 */
	class ProcessEventsTask extends TimerTask {
		@Override
		public void run() {
			logger.debug("Watching " + pathString);
			processEvents();
		}
	}
	/**
	 * This is the buffer/meter
	 * @author mike.hampton
	 *
	 */
	class IndexingTask extends TimerTask {
		@Override
		public void run() {
			Set<Entry<File, Boolean>> entries = buffer.entrySet();
			for(Entry<File, Boolean> entry:entries){
				if(entry.getValue()){
					try {
						indexer.indexFilesSolrCell(entry.getKey());
					} catch (IOException e) {
						logger.error("Error indexing " + entry.getKey(), e);
					}
				}
				else{
					try {
						indexer.deleteFileSolrCell(entry.getKey());
					} catch (IOException e) {
						logger.error("Error deleting file from index " +entry.getKey(), e);
					}
				}	
				buffer.remove(entry.getKey());
			}
		}
	}
}
