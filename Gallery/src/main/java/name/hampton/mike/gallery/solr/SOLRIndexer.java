package name.hampton.mike.gallery.solr;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.function.UnaryOperator;

import javax.imageio.ImageIO;

import name.hampton.mike.search.SearchException;
import name.hampton.mike.search.SearchIndexIntf;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest.ACTION;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used in indexing the files for the SOLR search implementation.
 * 
 * This class will index a directory using the fully qualified paths to the files
 * and directories (LINUS style slashes) sd the id for each item.
 * 
 * It has intelligence to index only changed files and directories, and does this
 * via watching the directory structure one a call is made to index or reindex a path.
 * 
 * The object that does this watching is NOT YET a true singleton in an enterprise sense,
 * and maybe should not be.  If this is implemented on multiple machines, then the object 
 * that watches the directory needs to exist for each machine.  The only complication is if
 * there are multiple processes running on a single machine, or multiple machines that
 * refer to the same drives.  I see this as a configuration question where EJBs, etc will
 * come into play.
 * 
 * 
 * @author mike.hampton
 *
 */
public class SOLRIndexer extends Observable implements SearchIndexIntf {

	private static OrFileFilter basefilter;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * The base url for the SOLR server. Example: http://localhost:8983/solr
	 * 
	 */
	private String urlString = "";
	
	/**
	 * The interface to the solr server
	 */
	HttpSolrServer coreSpecificSolrServer;
	
	/**
	 * Creates the instance, and sets the url for the SOLR server.
	 * 
	 * @param solrServerURL
	 * @throws IOException - if getting the canonical path of pathstring  causes an I/O error to occur, 
	 * 	which is possible because the construction of the canonical pathname may require filesystem queries
	 */
	public SOLRIndexer(String solrServerURL, String pathString) throws IOException {
		urlString = solrServerURL;
		logger.debug("solrServerURL='" + solrServerURL + "'");
		String coreID = SOLRUtilities.getCoreIDForPath(pathString);
		HttpSolrServer coreSpecificSolrServerHTTP = new HttpSolrServer(urlString + "/" + coreID);
		coreSpecificSolrServerHTTP.getBaseURL();
		coreSpecificSolrServer = coreSpecificSolrServerHTTP;
	}
	
	public String getBaseURL() {
		return coreSpecificSolrServer.getBaseURL();
	}
	
	@Override
	public int indexItem(Object itemid) throws SearchException, IOException {
		int returnValue = -1;
		File fileItemID = null;
		if(null != itemid){
			if( !(itemid instanceof File))
			{
				fileItemID = new File(itemid.toString()); // could blow up here
			}
			try {
				if(fileItemID.isDirectory()){
					returnValue = indexDir(fileItemID);
				}
				else {
					returnValue = indexFilesSolrCell(fileItemID)?1:0;
				}
			} catch (SolrServerException sse) {
				throw new SearchException(sse);
			}
		}		
		else
		{
			logger.warn("indexItem called with null.");
		}
		return returnValue;
	}
	
	public int reIndexItem(Object itemid) throws SearchException, IOException {
		int returnValue = -1;
		File fileItemID = null;
		if(null != itemid){
			if( !(itemid instanceof File))
			{
				fileItemID = new File(itemid.toString()); // could blow up here
			}
			try {
				if(fileItemID.isDirectory()){
					returnValue = reIndexDir(fileItemID);
				}
				else {
					returnValue = indexFilesSolrCell(fileItemID)?1:0;
				}
			} catch (SolrServerException sse) {
				throw new SearchException(sse);
			}
		}		
		else
		{
			logger.warn("reIndexItem called with null.");
		}
		return returnValue;
	}
 
	// Delete
	public int deleteItem(Object itemid) throws SearchException, IOException{
		int returnValue = -1;
		File fileItemID = null;
		if(null != itemid){
			if( !(itemid instanceof File))
			{
				fileItemID = new File(itemid.toString()); // could blow up here
			}
			else fileItemID = (File)itemid;
			returnValue = deleteFileSolrCell(fileItemID)?1:0;
		}		
		else
		{
			logger.warn("reIndexItem called with null.");
		}
		return returnValue;
	}

	public int indexDir(File directory)
			throws IOException, SolrServerException {
		return indexDir(directory, true);
	}
		
	/**
	 * Index the path, check for new/modified.
	 * 
	 * 
	 * @param pathString
	 * @return
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public int reIndexDir(File directory)
			throws IOException, SolrServerException {
		return indexDir(directory, false);
	}

	/**
	 * Something to note here.  If I had multiple machines, this would lend itself to a
	 * map reduce type job (divide and parallel).
	 * 
	 * 
	 * @param pathString
	 * @param fullIndex
	 * @return
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public int indexDir(File directory, boolean fullIndex)
			throws IOException, SolrServerException {
		
		int successCount = 0;
		try
		{		
			// We need to start the directory monitor here.  There should only be one of these per
			//	core.  This could result in having more than one monitoring the same files if 
			//	there are paths of interest that are embedded in others.
			//
			//	In some deployment scenarios, this should probably be in a naming 
			//	directory (JNDI) to ensure uniqueness.
			//
			// The filter here could lead to having multiple watchers on a single directory.
			// We should be careful to make sure there is one watcher with a flexible filter.
			
			DirectoryWatch watch = DirectoryWatchRegistry.getSingleton().getDirectoryWatch(this, SOLRUtilities.getPathStringForDirectory(directory));

			// This is basically ((file.extension in {accepted extensions}) OR file.isdirectory)
			IOFileFilter indexFilter = getBaseFilter();
			// We will NOT add on the age filter to the watch, because there are cases where the file is 'unchanged',
			// but the watch still needs to process it (for Example in the case of deletions).
			IOFileFilter watchFilter = indexFilter;
			
			if(!fullIndex){
				// Get the last updated item in the store.  All other items should be at least up to date wrt this date
				Date lastUpdated = getLastUpdated();
				
				/*
			     * Constructs a new age file filter for files newer than (at or before)
			     * the lastupdated cutoff date. 
				 */
				IOFileFilter ageFilter = new NotFileFilter(new AgeFileFilter(lastUpdated));				
				
				// Add the age filter in to the file filter.  We need a wrapper, because this is not an OR, it is an AND.
				// if(((file.extension in {accepted extensions}) OR file.isdirectory) AND file.lastmodified is more recent than lastUpdated)
				//
				// There is additional logic that is added on later, and tied to the 'fullIndex' flag
				AndFileFilter filterWithAgeAndExists = new AndFileFilter();
				filterWithAgeAndExists.addFileFilter(indexFilter);
				filterWithAgeAndExists.addFileFilter(ageFilter);

				// Reseat filter
				indexFilter = filterWithAgeAndExists;
			}
			if(!watchFilter.equals(watch.getFilter())){
				// reset the filter.
				watch.setFilter(watchFilter);
			}
			watch.startProcessing();
			
			Map<File, Integer> countCache = new HashMap<File, Integer>();
			
			successCount = runIndexForDir(directory, indexFilter, countCache, fullIndex);
			logger.debug("Successfully indexed " + successCount + " items.");
		}
		catch(IOException | SolrServerException e){
			notifyIndexingError(SOLRUtilities.getPathStringForDirectory(directory));
		}		
		return successCount;		
	}

	protected Date getLastUpdated() throws SolrServerException {
		
		Date lastUpdated = null;
		
		SolrQuery query = new SolrQuery();
		query.setRows(1);
		SortClause sortClause = new SortClause("updated", ORDER.desc);
		query.addSort(sortClause);
		query.set("q","*");

		QueryResponse response = coreSpecificSolrServer.query(query);
		
		SolrDocumentList results = response.getResults();
		
		// better be no more than one!
		if(results.size()>0)
		{
			if(results.size()>1)
			{
				logger.warn("Query returned more than one result.  There is something wrong, the query only asked for one row!  "
						+ "Will continue with first result, but this should be analyzed,  query: " +
						query + ",  response: " + response);
			}
			SolrDocument result = results.get(0);
			lastUpdated = (Date) result.getFieldValue("updated");
		}
		return lastUpdated;
	}

	//List of items from solr - 
	//	http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=containedin:C\:\/Users\/mike.hampton\/Pictures\/temp\/xmen\/&fl=id,dir
	//	http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/query?q=containedin:C\:\/Users\/mike.hampton\/Pictures\/temp\/xmen\/&fl=id,dir
	protected List<File> getItemsContainedIn(File containedIn) throws SolrServerException, IOException {
		
		List<File> files = new ArrayList<File>();
		
		SolrQuery query = new SolrQuery();
		File fqSearchDirTrailingSlash = new File(containedIn, "/"); 
		// Normalize the path to Unix characters
		String fqSearchDirNormalized = FilenameUtils.separatorsToUnix(fqSearchDirTrailingSlash.getCanonicalPath());
		// Escape the characters for the query
		String fqSearchDirNormalizedEscaped = SOLRUtilities.escapeQueryChars(fqSearchDirNormalized);
		query.set("q","containedin:"+fqSearchDirNormalizedEscaped + "&fl=id,dir");

		QueryResponse response = coreSpecificSolrServer.query(query);
		
		SolrDocumentList results = response.getResults();
		
		if(results.size()>0)
		{
			SolrDocument result = results.get(0);
			files.add(new File((String) result.getFieldValue("id")));
		}
		return files;
	}

	/**
	 * Build the file filter for this
	 * 
	 * @return
	 */
	protected static OrFileFilter getBaseFilter() {
		if(null==basefilter) 
		{
			OrFileFilter filter = new OrFileFilter();
			
			// Build a filter that accepts only the image types we are interested in.
			// TODO: We might want to add other things to this later.  Possible abstract 
			//	the filter to configuration time. 
			String[] suffixes = ImageIO.getReaderFileSuffixes();
			List<String> suffixList = Arrays.asList(suffixes);
			UnaryOperator<String> operator = new UnaryOperator<String>(){
				@Override
				public String apply(String t) {
					return "." + t;
				}
			};
			suffixList.replaceAll(operator);
			
			filter.addFileFilter(new SuffixFileFilter(suffixList,  IOCase.INSENSITIVE));
			// If we have no filters, then accept everything
			if (0 >= filter.getFileFilters().size()) {
				filter.addFileFilter(FileFileFilter.FILE);
			}
			// Accept all directories, always
			filter.addFileFilter(DirectoryFileFilter.DIRECTORY);
			// Look at org.apache.commons.io.filefilter to see if there are more you
			// want to use here.
			basefilter = filter;
		}
		return basefilter;
	}

	/**
	 * Index the files in the directory. This will beat the crap out of the solr
	 * server, and this one. Not sure about this.
	 * 
	 * @param solr - the solr server interface
	 * @param directory - the directory being indexed
	 * @param filter - the file filter to use to indicate what files are indexed
	 * @param countCache - a map we will use to count the number of files in the directories.  This helps us not recount.
	 * @return - the number of files and directories indexed
	 * @throws IOException
	 * @throws SolrServerException
	 */
	private int runIndexForDir(
			File directory,
			FileFilter filter, 
			Map<File, Integer> countCache,
			boolean fullIndex) throws IOException, SolrServerException {
		int successCount = 0;
		
		// Note that this count is an estimate.  It does not take into account
		// possible directory/file deletions, which will NOT be indexed
		int numberOfFilesToIndex = countFiles(directory,filter,countCache);
		
		// Notify on progress at most 100 times, but do not notify on progress 
		//	any less than ten files in the smallest increment
		// 
		int notificationFrequency = (int)Math.ceil((double)numberOfFilesToIndex / (double)100);
		int mostRecentProgressNotification = 0;
		if(notificationFrequency < 1){
			// less than 10, just notify 10 times
			notificationFrequency = 1;
		}
		
		notifyIndexingStart(directory.getCanonicalPath(), numberOfFilesToIndex);
		
		successCount += indexDirectorySolrCell(directory)?1:0;
		//			We need to get the list of items in the directory (not recursive yet) and compare it to the list of items from SOLR
		//				List of items from solr - 
		//					http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=containedin:C\:\/Users\/mike.hampton\/Pictures\/temp\/xmen\/&fl=id,dir
		//					http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/query?q=containedin:C\:\/Users\/mike.hampton\/Pictures\/temp\/xmen\/&fl=id,dir
		List<File> containedInSOLR = null;
		if(!fullIndex){
			containedInSOLR = getItemsContainedIn(directory);
			Collections.sort(containedInSOLR);
		}

		File[] files = (null == filter) ? directory.listFiles() : directory
				.listFiles(filter);
		for (File file : files) {
			if (file.isDirectory()) {
				if(!fullIndex){
					//	If a subdirectory was added or modified it will have a more recent last modified (I think, not sure on all file systems).
					//		If it is in the list from the directory, but not solr, then it was added.
					//			Then we need to add the subdirectory and all of its child items recursively. 
					//				(this is heuristic, we could just do a recursive call, but it is less efficient)
					//			else
					//				we need to make a recursive call to this function with the subdirectory
					int itemIndex = Collections.binarySearch(containedInSOLR, file);					
					boolean isNewDirectory = (itemIndex < 0);
					// If it is a new directory, do a full index, otherwise do not
					successCount += runIndexForDir(file, filter, countCache, isNewDirectory);
					if(!isNewDirectory){
						// remove it from the list of things in solr so we can tell what is in solr that is not in the directory
						containedInSOLR.remove(itemIndex);
					}
					//			We need to see if a items were deleted.  This is more complicated
					//				If the item is in the list from solr, but not the directory, then it was deleted
					//				delete from SOLR
					//					if it is an item, just delete it
					//					else if it is a subdir, we need to find all items recursively in it, and delete them all
					//						get all items inside the dir, note the lack of the trailing slash!!! 
					//							http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=path:C\:\/Users\/mike.hampton\/Pictures\/temp\/xmen&fl=id,dir
				}
				else successCount += runIndexForDir(file, filter, countCache, fullIndex);
			} else {
				if(!fullIndex){
					//	We need to see if a file was added - this should be easy, but it is not. 
					//		If the file is in the list from the directory, but not solr, then it was added.
					//		Add to SOLR
					int itemIndex = Collections.binarySearch(containedInSOLR, file);					
					boolean isNewFile = (itemIndex < 0);
					if(indexFilesSolrCell(file)){
						successCount++;
					}
					if(!isNewFile){
						// remove it from the list of things in solr so we can tell what is in solr that is not in the directory
						containedInSOLR.remove(itemIndex);
					}
				}
				else if(indexFilesSolrCell(file)){
					successCount++;
				}
			}
			// Might want to think about this one.  How often should we notify?  The observer
			// will already be notified of the completion of each directory.  Should we notify 
			// of the completion of each file?
			//
			// See above - Notify on progress at most 100 times
			int shouldINotify = successCount%notificationFrequency;
			if(shouldINotify == 0){
				mostRecentProgressNotification = (successCount*100)/numberOfFilesToIndex;
				mostRecentProgressNotification = mostRecentProgressNotification>100?100:mostRecentProgressNotification;
				notifyIndexingProgress(directory.getCanonicalPath(), mostRecentProgressNotification);
			}
		}
		if(!fullIndex){
			// We have indexed or reindexed everything in the directory, and removed it from the list of things in SOLR.  Anything left
			// in the containedInSOLR list is no longer in the directory, delete it from solr.
			for(File file : containedInSOLR){
				deleteFileSolrCell(file);
			}
		}
		
		// Always notify on exit, unless we already have
		if(mostRecentProgressNotification != 100){
			notifyIndexingProgress(directory.getCanonicalPath(), 100);
		}
		notifyIndexingComplete(directory.getCanonicalPath(), successCount);
		
		return successCount;
	}

	/**
	 * Adds the directory itself to the solr search index.
	 * 
	 * @param solr
	 * @param file
	 * @return
	 */
	private boolean indexDirectorySolrCell(File file){
		boolean success = false;
		try {
			SolrInputDocument document = new SolrInputDocument();
			File solrIdFile = new File(file, "/");
			String solrId = FilenameUtils.separatorsToUnix(solrIdFile.getCanonicalPath());
			document.addField("id", solrId);
			File containedinFile = new File(file.getParentFile(), "/");
			String containedin = FilenameUtils.separatorsToUnix(containedinFile.getCanonicalPath());
			document.addField("containedin", containedin);
			document.addField("dir", true);
			
		    UpdateRequest up = new UpdateRequest();
		    up.setAction(ACTION.COMMIT, true, true);
		    
		    // Add to core0
		    up.add(document);
		    UpdateResponse response = coreSpecificSolrServer.add(document);
			if (response.getStatus() == 0) {
				success = true;
				notifyItemIndexed(solrId);				
			} 
		} catch (Exception e) {
			// Log the error, but do NOT throw.  We will just keep going.
			logger.error("Error indexing '" + file.getAbsolutePath()
					+ "'", e);
		}
		return success;
	}

	/**
	 * This amounts to doing a listing of the files in the directory and calling
	 * the equivalent of curl
	 * "http://localhost:8983/solr/update/extract?literal.id=C:\Users\mike.hampton\Pictures\000.jpg&uprefix=attr_&fmap.content=attr_content&commit=true"
	 * -F "myfile=@C:\Users\mike.hampton\Pictures\000.jpg"
	 * 
	 * 
	 * @param file
	 * @param solrId
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public boolean indexFilesSolrCell(File file) throws IOException {
		boolean success = false;
		try {
			ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
			up.addFile(file, Files.probeContentType(file.toPath()));

			// Normalize the file path to unix style.
			String solrId = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
			up.setParam("literal.id", solrId);
			File containedinFile = new File(file.getParentFile(), "/");
			String containedin = FilenameUtils.separatorsToUnix(containedinFile.getCanonicalPath());
			up.setParam("literal.containedin", containedin);
			
			up.setParam("uprefix", "attr_");
			up.setParam("fmap.content", "attr_content");
			up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
			NamedList<Object> result = coreSpecificSolrServer.request(up);
			logger.debug("Index Result: " + result);
			success = true;
			notifyItemIndexed(solrId);
		} catch (Exception e) {
			// Log the error, but do NOT throw.  We will just keep going.
			logger.error("Error indexing '" + file.getAbsolutePath()
					+ "'", e);
		} /* catch (SolrServerException | SolrException sse) {
			throw new IOException("Error indexing '" + file.getAbsolutePath()
					+ "'", sse);
		}*/
		return success;
	}

	/**
	 * Remove an item from the solr index.  This could be a directory or file.
	 * 
	 * @param solr
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public boolean deleteFileSolrCell(File file) throws IOException {
		boolean success = false;
		try {
			// Normalize the file path to unix style.
			String solrId = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
			UpdateResponse result = coreSpecificSolrServer.deleteById(solrId);
			logger.debug("Delete Result: " + result);
			success = true;
			notifyItemDeleted(solrId);
		} catch (Exception e) {
			// Log the error, but do NOT throw.  We will just keep going.
			logger.error("Error deleting '" + file.getAbsolutePath()
					+ "'", e);
		}
		return success;
	}
	
	/**
	 * Count the number of files in a directory and its subdirectories based on a filter
	 * 
	 * @param directory - the directory to count files in
	 * @param filter - determines what files are counted
	 * @param countCache - a map that is populated with counts to help avoid re-counting later.
	 * @return the number of files in the directory and its subdirectories based on the filter.
	 */
	private int countFiles(File directory, FileFilter filter, Map<File, Integer> countCache) {
		int count = 0;
		Integer countObj = countCache.get(directory);
		if(null != countObj) {
			count = countObj;// autounboxing...weird, and dangerous
		}
		else {		
			File[] files = (null == filter) ? directory.listFiles() : directory.listFiles(filter);
			count = files.length;
			File[] directories = directory.listFiles((FileFilter)DirectoryFileFilter.DIRECTORY);
			for(File subDirectory : directories){
				count += countFiles(subDirectory, filter, countCache);
			}
			countCache.put(directory, count);// autoboxing...weird, and dangerous
		}
		return count;
	}

	
	// Notifications
	private void notifyIndexingStart(String itemContextID, int numberOfItemsToIndex){
		setChanged();
		SOLRIndexingEvent event = new SOLRIndexingEvent(this, itemContextID, SOLRIndexingEvent.EventType.INDEXING_START); 
		event.setCount(numberOfItemsToIndex);
		notifyObservers(event);
	}

	private void notifyIndexingProgress(String itemContextID, int numberOfItemsIndexed){
		setChanged();
		SOLRIndexingEvent event = new SOLRIndexingEvent(this, itemContextID, SOLRIndexingEvent.EventType.INDEXING_PROGRESS); 
		event.setCount(numberOfItemsIndexed);
		notifyObservers(event);
	}

	private void notifyIndexingComplete(String itemContextID, int count){
		setChanged();
		SOLRIndexingEvent event = new SOLRIndexingEvent(this, itemContextID, SOLRIndexingEvent.EventType.INDEXING_COMPLETE);
		event.setCount(count);
		notifyObservers(event);
	}

	private void notifyIndexingError(String itemContextID){
		setChanged();
		SOLRIndexingEvent event = new SOLRIndexingEvent(this, itemContextID, SOLRIndexingEvent.EventType.INDEXING_ERROR);
		notifyObservers(event);
	}
	
	private void notifyItemIndexed(String itemContextID){
		setChanged();
		SOLRIndexingEvent event = new SOLRIndexingEvent(this, itemContextID, SOLRIndexingEvent.EventType.ITEM_INDEXED);
		notifyObservers(event);
	}
	
	private void notifyItemDeleted(String itemContextID){
		setChanged();
		SOLRIndexingEvent event = new SOLRIndexingEvent(this, itemContextID, SOLRIndexingEvent.EventType.ITEM_DELETED);
		notifyObservers(event);
	}
}
