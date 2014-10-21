package name.hampton.mike.gallery.servlet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import name.hampton.mike.gallery.ResourceProvider;
import name.hampton.mike.gallery.ThumbnailUtil;
import name.hampton.mike.gallery.exception.InvalidPathException;

/**
 * Returns the thumbnail of an image.
 * 
 * @author mike.hampton
 *
 */
public class ThumbnailServlet extends FileServlet {

    public static final String THUMBNAIL_DIR = "thumbnailDir";
    public static final String DEFAULT_THUMBNAIL = "defaultThumbnail";
	public static final String THUMBNAIL_WIDTH = "thumbnailWidth";
	public static final String THUMBNAIL_HEIGHT = "thumbnailHeight";
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -5038128930274745921L;

	@Override
	protected boolean shouldAbortOnNonExistantFile(HttpServletRequest request, HttpServletResponse response, File fileNoLongerExists)
			throws IOException {
		// Do your thing if the file appears to be non-existing.
		// Throw an exception, or send 404, or show default/warning page, or just ignore it.
		String hasDefault = request.getParameter("DEFAULT");
		boolean abort = false;
		if(null == hasDefault)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			abort = true;
		}
		return abort;
	}

	/**
     * Map file to a thumbnail.
     * 
     * For now, we will just use the file extension to determine the file type.
     * 
     * The type will either determine the thumbnail - if it is not a visual file, or
     * allow us to generate a thumbnail.
     * 
     * @param file
     * @return file
     */
	@Override
    protected File mutateFile(Principal principal, Map<String, String[]> map, File file, String requestedFile) {
        logger.debug("mutateFile !!!!!!!");
        ThumbnailUtil thumbnailGenerator = new ThumbnailUtil();
        thumbnailGenerator.setResourceProvider(new ResourceProvider() {
				@Override
				public URL getResource(String path) throws MalformedURLException{
					return getServletContext().getResource(path);
				}
			}
        );
        
		String imageFormat = ThumbnailUtil.getImageFormat(file);
		if(null != imageFormat){
			try {
				File thumbnailDir = getDir(principal, THUMBNAIL_DIR);
				File thumbnailFile = new File(thumbnailDir, requestedFile);
				
				thumbnailGenerator.ensureThumbnailExists(principal, file, thumbnailFile, imageFormat);
				file = thumbnailFile;
			} catch (InvalidPathException e) {
				logger.warn("Could not retreive thumbnail dir.", e);
			} catch (IOException e) {
				logger.warn("Could not create thumbnail.", e);
			}
		}
		else
		{
			// urls for default thumbnails:
			//	./thumbnail/dir?DEFAULT=dir
			//	./thumbnail/foo?DEFAULT=dir
			//	./thumbnail/bar?DEFAULT=wmv
			//	./thumbnail/?DEFAULT=true
			//	./thumbnail/noideawhathappenedhere
			//  http://server:8080/gallery/thumbnail/.squirrel-sql/cellImportExport.xml?DEFAULT=true   -> requestedFile is    /.squirrel-sql/cellImportExport.xml
			//  http://server:8080/gallery/thumbnail/.squirrel-sql?DEFAULT=foo   -> requestedFile is   /.squirrel-sql

			// return a mapped icon.
			String[] values = map.get("DEFAULT");
			file = thumbnailGenerator.getDefaultThumbnail(values);
		}
		return file;
	}
}
