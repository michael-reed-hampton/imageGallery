package name.hampton.mike.gallery.rest;

import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.DirectoryDisplay;
import name.hampton.mike.gallery.DisplayData;
import name.hampton.mike.gallery.GalleryApplication;
import name.hampton.mike.gallery.SearchCriteria;
import name.hampton.mike.gallery.ValidationTools;
import name.hampton.mike.gallery.exception.InvalidPathException;

@Path("searchOld")
public class DirectoryDisplayREST extends DirectoryDisplay{	

	@Context
    SecurityContext securityContext;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public DisplayData searchPOST(SearchCriteria searchCriteria) {
		return search(searchCriteria);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public DisplayData searchPOST(
			@QueryParam("directory") String directory,
			@QueryParam("subdirectories") boolean subdirectories
			) {
		SearchCriteria searchCriteria = new SearchCriteria();
		searchCriteria.setDirectory(directory);
		searchCriteria.setSubdirectories(subdirectories);
		
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
		return ValidationTools.getValidDirectory(baseDirString);
	}
}
