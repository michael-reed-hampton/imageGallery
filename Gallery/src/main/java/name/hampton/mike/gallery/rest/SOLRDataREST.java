package name.hampton.mike.gallery.rest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.GalleryApplication;
import name.hampton.mike.gallery.SearchCriteria;
import name.hampton.mike.gallery.ValidationTools;
import name.hampton.mike.gallery.exception.InvalidConfigurationException;
import name.hampton.mike.gallery.exception.InvalidPathException;
import name.hampton.mike.gallery.solr.SOLRData;

import org.apache.solr.client.solrj.SolrServerException;

// @Path("search")
@Path("search{identifier:.*}")
public class SOLRDataREST extends SOLRData{	

	@Context
    SecurityContext securityContext;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object searchPOST(SearchCriteria searchCriteria) throws SolrServerException, InvalidConfigurationException, IOException {
		setUrl(getSOLRURL());
		return search(searchCriteria);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object searchPOST(
			@PathParam("identifier") String identifier,
			// @QueryParam("directory") String directory,
			@QueryParam("subdirectory") boolean subdirectory,
			@QueryParam("fields") List<String> fieldList,
			@QueryParam("freeText") String freeText,
			@QueryParam("flatten") boolean flatten 
			) throws SolrServerException, IOException, InvalidConfigurationException {
		setUrl(getSOLRURL());
		SearchCriteria searchCriteria = new SearchCriteria();
		// If there is an identifier, it indicates the directory or item name
		searchCriteria.setFlatten(flatten);
		searchCriteria.setSubdirectories(subdirectory);
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
		searchCriteria.setFreeText(freeText);
		if(null!=identifier && !identifier.equals("")){
			if(null!=identifier)searchCriteria.setDirectory(identifier);
		}
		return search(searchCriteria);
	}
	
	@Override
	protected File getBaseDir() throws InvalidPathException {
		String userName = securityContext.getUserPrincipal().getName();
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
		
		// Only set it if it changes!!!
		if(!newBaseDir.equals(super.getBaseDir())){		
			super.setBaseDir(ValidationTools.getValidDirectory(baseDirString));
		}
		return super.getBaseDir();
	}

	protected String getSOLRURL() throws InvalidConfigurationException {
		String userName = securityContext.getUserPrincipal().getName();
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
