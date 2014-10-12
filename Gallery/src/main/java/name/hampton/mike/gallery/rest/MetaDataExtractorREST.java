package name.hampton.mike.gallery.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.GalleryApplication;
import name.hampton.mike.gallery.GalleryItem;
import name.hampton.mike.gallery.MetadataExtractor;
import name.hampton.mike.gallery.ValidationTools;
import name.hampton.mike.gallery.exception.InvalidPathException;

import org.apache.commons.imaging.ImageReadException;

@Path("imageMetaData{path:.*}")
public class MetaDataExtractorREST extends MetadataExtractor{	

	@Context
    SecurityContext securityContext;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object extractImageMetaDataLocal(
			@PathParam("path") String path
			) {
		GalleryItem item = new GalleryItem();
		String swapSlashesPath = path.replace("\\", "/");
		item.setPath(swapSlashesPath);

		return search(item);
	}
	
	public Map<String, Object> search(GalleryItem item) {
        Map<String, Object> metaData = new HashMap<String, Object>(); 
		try {
			File baseDir = getBaseDir();
			File itemLocation = ValidationTools.validateItem(new File(baseDir,
						item.getPath()));
			if (null != itemLocation) {
				InputStream itemStream = null;
				try
				{
					itemStream = new FileInputStream(itemLocation);
					metaData = extractImageMetaData(itemStream, metaData);
				}
				catch(IOException ioe)
				{
					logger.debug("Error reading metadata for image.", ioe);					
					metaData.put("error:",ioe.getMessage());
				}
				catch(ImageReadException ire)
				{
					logger.debug("Error reading metadata for image.", ire);					
					metaData.put("error:",ire.getMessage());
				}
				finally
				{
					if(null != itemStream)
					{
						try
						{
							itemStream.close();
						}
						catch(IOException ioe2)
						{
							logger.debug("Error closing stream.");
						}
					}
				}
			}					
			else
			{
				metaData.put("error:","itemlocation is null");
			}
		} 
		catch (InvalidPathException ipe) {
			ipe.printStackTrace();
			metaData.put("error:",ipe);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			metaData.put("error:",ex);
		}
		return metaData;
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
