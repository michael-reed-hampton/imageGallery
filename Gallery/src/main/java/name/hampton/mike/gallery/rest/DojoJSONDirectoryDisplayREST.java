package name.hampton.mike.gallery.rest;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.GalleryApplication;
import name.hampton.mike.gallery.GalleryItemFactory;
import name.hampton.mike.gallery.GalleryItemIntf;
import name.hampton.mike.gallery.ValidationTools;
import name.hampton.mike.gallery.exception.InvalidPathException;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

@Path("dojoSearch{path:.*}")
public class DojoJSONDirectoryDisplayREST {	

	@Context
    SecurityContext securityContext;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object searchPOST(
			@PathParam("path") String path,
			@DefaultValue("true") @QueryParam("includeDir") boolean includeDir, // include directories in the result
			@QueryParam("includeSubDir") boolean includeSubDir, // include all subdirectories
			@QueryParam("wildcard") String wildcard, // a pattern for the name of the images
			@Context UriInfo uriInfo
			) {
		String swapSlashesPath = path.replace("\\", "/");
		// Will use the following when we have a searchable database of image metadata
		// MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		return search(swapSlashesPath, includeDir, includeSubDir,wildcard);
	}

	
	/**
	 * Just maps a path to the directory system, and returns the files in that directory.
	 * 
	 * @param item
	 * @return
	 */
	public GalleryItemIntf search(String swapSlashesPath, boolean includeDir, boolean includeSubDir,String wildcard) {
		GalleryItemIntf containerItem = null;
		try {
			File baseDir = getBaseDir();
			File directory = ValidationTools.validateDirectory(new File(baseDir,
					swapSlashesPath));
			if (null != directory) {
				GalleryItemFactory factory = GalleryItemFactory.getInstance();
				containerItem = factory.createGalleryItem(baseDir, swapSlashesPath);
				List<GalleryItemIntf> galleryItems = new ArrayList<GalleryItemIntf>();

				// Here is where the search Criteria will come in (at Least partially)				
				OrFileFilter filter =  new OrFileFilter();
				// If we have a wildcard passed in, build a filter for it. 
				if(null!=wildcard){
					filter.addFileFilter(new WildcardFileFilter(wildcard, IOCase.INSENSITIVE));
				}
				// If we have no filters, then accept everything
				if(0 >= filter.getFileFilters().size()){
					filter.addFileFilter(FileFileFilter.FILE);
				}
				// Accept all directories, always
				filter.addFileFilter(DirectoryFileFilter.DIRECTORY);
				// Look at org.apache.commons.io.filefilter to see if there are more you want to use here.
				// We could implement the search via these filters...
				
				loadGalleryItems(baseDir, directory, factory, galleryItems,
						filter, includeDir, includeSubDir);

				containerItem.setChildren(galleryItems);
			}
		} 
		catch (InvalidPathException ipe) {
			ipe.printStackTrace();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return containerItem;
	}	
	
	private void loadGalleryItems(File baseDir, File directory,
			GalleryItemFactory factory, List<GalleryItemIntf> galleryItems,
			FileFilter filter, boolean includeDir, boolean includeSubDir) throws InvalidPathException {
		// Load up the files first...
		File[] files = (null==filter)?directory.listFiles():directory.listFiles(filter);				
		for(File file : files){			
			if(includeDir && file.isDirectory()){				
				GalleryItemIntf galleryItem = factory.createGalleryItem(baseDir, file);
				galleryItems.add(galleryItem);
			}
			else{
				GalleryItemIntf galleryItem = factory.createGalleryItem(baseDir, file);
				galleryItems.add(galleryItem);
			}
			if(includeSubDir && file.isDirectory()){
				loadGalleryItems(baseDir, file, factory, galleryItems, filter, includeDir, includeSubDir);
			}
		}
	}

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
