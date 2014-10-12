package name.hampton.mike.gallery.solr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import name.hampton.mike.gallery.GalleryItemFactory;
import name.hampton.mike.gallery.GalleryItemIntf;
import name.hampton.mike.gallery.SearchCriteria;
import name.hampton.mike.gallery.exception.InvalidPathException;

import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOLRData {


	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * The base url for the SOLR server. Example: http://localhost:8983/solr
	 * 
	 */
	private String urlString = "";
	private File baseDir = null;
	private SolrServer solrServer = null;

	/**
	 * Interprets SearchCriteria for a SOLR query. 
	 * 
	 * Example of some searches
	 * 	Specific fields: the directory must be 'temp' and the gps latitude must be 'S'
	 * 		(containedin:C\:/Users/mike.hampton/Pictures/temp AND gps_latitude_ref:S)
	 * 	Specific fields, and a free text query: the directory must be 'temp' and the gps latitude must be 'S', or if it 
	 * 	has 'Cipher' in the default search field.  
	 * 			NOTE:  The default search field is set up in the SOLR schema.xml, and (as of this writing) is named "text".  
	 * 				It has the value of various other fields copied into it for indexing.  It
	 * 				is NOT returned in the results (although I HAVE done so for debugging occasionally). 
	 * 		(containedin:C\:/Users/mike.hampton/Pictures/temp AND gps_latitude_ref:S) OR Cipher
	 * 	Specific fields, and a free text query: the directory must be 'temp' and the gps latitude must be 'S', or if it 
	 * 	has 'Cipher' in the default search field or 'Wolverine' in the default search field.  
	 *  (containedin:C\:/Users/mike.hampton/Pictures/temp AND gps_latitude_ref:S) OR (Cipher OR Wolverine)
	 *  
	 *  The main use cases will be:
	 *  	the specific field 'containedin' to specify the directory, and a free text query.  This is searching in a specified directory.
	 *  		(containedin:C\:/Users/mike.hampton/Pictures/temp) AND Cipher
	 *		the specific field 'path' to speify the directory and its descendants and a free text query. This is searching in a specified directory, and all those beneath it hierarchically.
	 *  		(path:C\:/Users/mike.hampton/Pictures/temp) AND Cipher
	 *  	a free text query.  This is searching in the entire set.
	 *  		Cipher
	 *  		NOTE:  This is equivalent to 
	 *  			(path:C\:/Users/mike.hampton/Pictures) AND Cipher
	 *  			where 'C\:/Users/mike.hampton/Pictures' is the base dir, or ROOT
	 *  
	 *  Additionally, many of the searches will only be interested in image type files
	 *  	http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=(path:C\:/Users/mike.hampton/Pictures/temp) AND content_type:image/*
	 * 
	 * Also, the query may be for directories
	 * 		All directories
	 * 			http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=dir:true
	 * 		Sub directories of a specific directory 
	 * 			http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=containedin:C\:/Users/mike.hampton/Pictures/temp/xmen AND dir:true
	 * 		Sub directory hierarchy of a specific directory (Note that this also returns the directory passed) 
	 * 			http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=path:C\:/Users/mike.hampton/Pictures/temp AND dir:true
	 * 
	 * @param searchCriteria
	 * @return Either the originating object with children populated, or the children alone.
	 * @throws SolrServerException
	 * @throws InvalidPathException
	 * @throws IOException
	 */
	public Object search(SearchCriteria searchCriteria) throws SolrServerException, InvalidPathException, IOException {
		SolrQuery query = new SolrQuery();

		if(null == searchCriteria)
		{
			searchCriteria = new SearchCriteria();
			// We will interpret this to mean everything, including subdirectories. This is the common interpretation.
			// query.set("q","*");
		}
		if(null==searchCriteria.getDirectory()){
			// This should not ever happen, but handle it if it does.
			logger.warn("searchCriteria.getDirectory() is null, this indicates the root - setting it as such");
			searchCriteria.setDirectory("");
		}
		
		// The final query string to pass
		StringBuilder queryString = new StringBuilder();
		// Start with WHERE the user will be searching.  This should be determined by the directory and the subdirectory flag.
		// Build up the dir, it is based on the configured base directory
		File fqSearchDir = new File(getBaseDir(), searchCriteria.getDirectory());
		
		/*
		 * Would you believe that I need to do this differently?
		 * 
		 * It has to do with the way Dojo JsonRest formats requests, and the way the 
		 * solr.PathHierarchyTokenizerFactory works in SOLR.  UGH.
		 * 
		 * List of items from solr 												http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=containedin:C\:\/Users\/mike.hampton\/Pictures\/temp\/xmen\/
		 * get all items inside the dir, note the lack of the trailing slash!!! http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=path:C\:\/Users\/mike.hampton\/Pictures\/temp\/xmen
		 * 
		 * 
		 */
		if(searchCriteria.isSubdirectories()){
			// the field to use is the "path".  This allows for searching through subdirectories as well.
			// Normalize the path to Unix characters
			String fqSearchDirNormalized = FilenameUtils.separatorsToUnix(fqSearchDir.getCanonicalPath());
			// Escape the characters for the query
			String fqSearchDirNormalizedEscaped = SOLRUtilities.escapeQueryChars(fqSearchDirNormalized);
			queryString.append("path:").append(fqSearchDirNormalizedEscaped);
		}
		else{
			File fqSearchDirTrailingSlash = new File(fqSearchDir, "/"); 
			// Normalize the path to Unix characters
			String fqSearchDirNormalized = FilenameUtils.separatorsToUnix(fqSearchDirTrailingSlash.getCanonicalPath());
			// Escape the characters for the query
			String fqSearchDirNormalizedEscaped = SOLRUtilities.escapeQueryChars(fqSearchDirNormalized);
			queryString.append("containedin:").append(fqSearchDirNormalizedEscaped);
		}
		
		// Now add specific fields.
		if(null != searchCriteria.getFields()){
			queryString.append(" AND (");
			Iterator<String> keys = searchCriteria.getFields().keySet().iterator();
			String joinOperator = "";
			while(keys.hasNext()){
				String key = keys.next();
				queryString.append(joinOperator);
				queryString.append(key);
				queryString.append(":");
				// queryString.append(escapeQueryChars(searchCriteria.getFields().get(key)));
				queryString.append(searchCriteria.getFields().get(key));
				joinOperator = " " + searchCriteria.getOperator() + " ";
			}
			queryString.append(")");
		}
		// Now add the additional query.
		if(null != searchCriteria.getFreeText()){
			queryString.append(" AND (");
			queryString.append(searchCriteria.getFreeText());
			queryString.append(")");
		}
		
		//
		//TODO:  Need to implement sorting and pagination. 
		//
		query.setRows(10000);
		SortClause sortClause = new SortClause("creation_date", ORDER.desc);
		query.addSort(sortClause);
		//
		//TODO:  Need to implement sorting and pagination. 
		//

		query.set("q",queryString.toString());
		
	    QueryResponse response = getSolrServer().query(query);
	    
	    SolrDocumentList results = response.getResults();
	    
		GalleryItemFactory factory = GalleryItemFactory.getInstance();
		GalleryItemIntf containerItem = null;
		containerItem = factory.createGalleryItem(getBaseDir(), searchCriteria.getDirectory());
		
		List<GalleryItemIntf> galleryItems = new ArrayList<GalleryItemIntf>();
		if(containerItem.isAllowsChildren()){
			containerItem.setChildren(galleryItems);
		}
		// Little bit of weirdness here.  If the result of the passed criteria does not indicate a container, and
		// the searchcriteria does not indicate that the results should be flattened, then there is no reason to 
		// build the results here, because they will not be returned.
		if(containerItem.isAllowsChildren() || searchCriteria.isFlatten()){
		    for (int i = 0; i < results.size(); ++i) {
		    	SolrDocument result = results.get(i);
		    	String fieldValue = (String) result.getFieldValue("id");
				File path = new File(fieldValue);
				GalleryItemIntf galleryItem = factory.createGalleryItem(getBaseDir(), path);
				galleryItems.add(galleryItem);		
		    }
		}
		Object returnObject = null; 
		if(searchCriteria.isFlatten()){
			returnObject = galleryItems;
		}
		else{
			returnObject = containerItem;
		}
        logger.debug("Returning " + returnObject);
	    
	    return returnObject;
	}
	
	
	private SolrServer getSolrServer() throws IOException, InvalidPathException {
		if(null == solrServer){
			String coreID = SOLRUtilities.getCoreIDForPath(getBaseDir().getCanonicalPath());
			solrServer = new HttpSolrServer(urlString + "/" + coreID);
		}
		return solrServer;
	}

	public void setUrl(String urlString) {
		clearSolr();
		this.urlString = urlString;
	}

	public String getUrl() {
		return this.urlString; 
	}

	protected File getBaseDir() throws InvalidPathException {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		clearSolr();
		this.baseDir = baseDir;
	}
	
	protected void finalize(){		
	}
	
	protected void clearSolr(){
		if(null != solrServer){
			try{solrServer.shutdown();}catch(Exception e){logger.error("Error shutting down SOLR interface.",e);}
			solrServer = null;
		}
	}

}
