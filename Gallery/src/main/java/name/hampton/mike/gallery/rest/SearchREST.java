package name.hampton.mike.gallery.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import name.hampton.mike.gallery.SearchCriteria;
import name.hampton.mike.gallery.SortField;
import name.hampton.mike.gallery.exception.InvalidConfigurationException;
import name.hampton.mike.search.SearchException;
import name.hampton.mike.search.SearchIntf;
import name.hampton.mike.search.SearchProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Search Rest interface.  It obtains the search functionality from the SearchProvider.
 * 
 * This class just deals with calling the searrch functionality.
 * 
 * @author mike.hampton
 *
 */
// @Path("search")
@Path("search{identifier:.*}")
public class SearchREST {	

	@Context
    SecurityContext securityContext;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object searchPOST(SearchCriteria searchCriteria) throws InvalidConfigurationException, IOException, SearchException {
		SearchIntf searchIntf = SearchProvider.getSingleton().getSearchIntf(securityContext.getUserPrincipal());
		return searchIntf.search(searchCriteria);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object searchPOST(
			@HeaderParam("X-Range") String itemRange,  
			@PathParam("identifier") String identifier,
			// @QueryParam("directory") String directory,
			@QueryParam("sort") String sortFields,  
			@QueryParam("subdirectory") boolean subdirectory,
			@QueryParam("fields") List<String> fieldList,
			@QueryParam("freeText") String freeText,
			@QueryParam("flatten") boolean flatten 
			) throws IOException, InvalidConfigurationException, SearchException {
		SearchCriteria searchCriteria = new SearchCriteria();
		// If there is an identifier, it indicates the directory or item name
		searchCriteria.setFlatten(flatten);
		searchCriteria.setSubdirectories(subdirectory);
		setFieldConstraints(fieldList, searchCriteria);
		List<SortField> sortFieldObjs = parseSortFields(sortFields);
		if(null != sortFieldObjs && sortFieldObjs.size() > 0){
			searchCriteria.setSortFields(sortFieldObjs);
		}
		setItemRange(itemRange, searchCriteria);

		searchCriteria.setFreeText(freeText);
		if(null!=identifier && !identifier.equals("")){
			if(null!=identifier)searchCriteria.setDirectory(identifier);
		}		
		return this.searchPOST(searchCriteria);
	}

	protected void setFieldConstraints(List<String> fieldList,
			SearchCriteria searchCriteria) {
		if(null!=fieldList && fieldList.size() > 0) {
			Map<String, String> fields = new HashMap<String, String>(fieldList.size());
			for(String fieldEntry : fieldList){
				int firstColon = fieldEntry.indexOf(':');
				if(firstColon < 1){
					logger.error("Invalid field syntax, must have a colon : as delimiter between key and value- '" + fieldEntry + "'");
				}
				else{
					String key = fieldEntry.substring(0, firstColon);
					String value = fieldEntry.substring(firstColon+1);
					fields.put(key, value);
				}
			}
			searchCriteria.setFields(fields);
		}
	}

	protected void setItemRange(String itemRange, SearchCriteria searchCriteria) {
		if(null!=itemRange){
			// format is "items=0-19"
			String[] parts = itemRange.split("=");
			// I am not sure if 'items' can ever change.  At least in the dojo JsonRest object, it is hardcoded.
			String[] range = parts[1].split("-");
			try{
				searchCriteria.setStartIndex(Integer.parseInt(range[0]));
				searchCriteria.setNumberOfRowsToReturn(Integer.parseInt(range[1]));
			}
			catch(NumberFormatException nfe){
				logger.error("Got a rest call with an invalid range specified in the HTTP X-Range header.  The value received was: " + itemRange);
				// Flag values for 'ignore me'
				searchCriteria.setStartIndex(-1);
				searchCriteria.setNumberOfRowsToReturn(-1);
			}
		}
	}

	// format is "-sortedDescendingField, sortedAscendingField" etc
	protected List<SortField> parseSortFields(String sortFields) {
		List<SortField> sortFieldObjs = null;
		if(null!=sortFields){
			String[] fields = sortFields.split(",");
			if(fields.length > 0){
				sortFieldObjs = new ArrayList<SortField>(fields.length); 
				for(String field : fields){
					field = field.trim();
					if(!field.equals("") && !field.equals("-") ){
						SortField sortField = new SortField();
						sortField.setDescending(field.startsWith("-"));
						if(sortField.isDescending()){
							field = field.substring(1);
						}
						sortField.setFieldName(field);
						sortFieldObjs.add(sortField);
					}
				}
			}				
		}
		return sortFieldObjs;
	}
}
