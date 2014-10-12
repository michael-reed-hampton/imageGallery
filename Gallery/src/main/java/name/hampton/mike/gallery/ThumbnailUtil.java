package name.hampton.mike.gallery;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailUtil {

	public static final String THUMBNAIL_WIDTH = "thumbnailWidth";
	public static final String THUMBNAIL_HEIGHT = "thumbnailHeight";
	
	private ResourceProvider resourceProvider;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * Makes sure that there is a thumbnail for the passed imageFile located at the path provided by
	 * the passed thumbnailFile
	 * 
	 * @param principal
	 * @param imageFile
	 * @param thumbnailFile
	 * @param imageFormat
	 * @throws IOException
	 */
	public void ensureThumbnailExists(Principal principal, File imageFile, File thumbnailFile) throws IOException {
		String imageFormat = getImageFormat(imageFile);
		ensureThumbnailExists(principal, imageFile, thumbnailFile, imageFormat);
	}
	
	/**
	 * Attempts to find a known image format for the passed file.
	 * 
	 * @param file - a file that may be an image, but may be of anyother type
	 * @return if the passed image is of a known image type, the understood suffix. 
	 */
	public static String getImageFormat(File file) {
		if(null!=file)
		{
			String[] suffixes = ImageIO.getReaderFileSuffixes();
			
			for(String suffix : suffixes){
				if(file.getName().toUpperCase().endsWith(suffix.toUpperCase())){
					return suffix;
				}
			}
		}
		return null;
	}
	
	/**
	 * Makes sure that there is a thumbnail for the passed imageFile located at the path provided by
	 * the passed thumbnailFile
	 * 
	 * @param principal
	 * @param imageFile
	 * @param thumbnailFile
	 * @param imageFormat
	 * @throws IOException
	 */
	public void ensureThumbnailExists(Principal principal, File imageFile, File thumbnailFile, String imageFormat) throws IOException {
        logger.debug("ensureThumbnailExists !!!!!!!");
        logger.debug("thumbnailFile.canRead() = " + thumbnailFile.canRead());
        logger.debug("thumbnailFile.exists() = " + thumbnailFile.exists());
		if (!thumbnailFile.canRead() || !thumbnailFile.exists()) {
			if(
				(thumbnailFile.getParentFile().exists() || thumbnailFile.getParentFile().mkdirs()) && 
				thumbnailFile.getParentFile().canRead() && 
				thumbnailFile.getParentFile().canWrite()&& 
				thumbnailFile.getParentFile().canExecute()) 
			{
				//if(thumbnailFile.canWrite()){
					Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(imageFormat);
					
					if(readers.hasNext()){
						ImageReader reader = readers.next();
						ImageInputStream input = new FileImageInputStream(imageFile);
						reader.setInput(input);
						
						BufferedImage thumbImg = null;
						
						boolean tryToReadThumbnail = false;
								
						try
						{
							tryToReadThumbnail = reader.readerSupportsThumbnails() && reader.getNumThumbnails(0) > 0;
						}
						catch(IIOException iioe)
						{
							logger.warn("Problem trying to determine if image has embedded thumbnails: " + iioe);
						}
						
						if(tryToReadThumbnail){
							try
							{
								thumbImg = reader.readThumbnail(0, 0);
							}
							catch(IOException ioe)
							{
								logger.warn("Problem trying to read thumbnail from image: " + ioe);
							}
						}
						if(null == thumbImg)
						{	
							BufferedImage img = reader.read(0); // load image
							
							int thumbnailWidth = 50;
							int thumbnailHeight = 50;
							
							String thumbnailWidthString =  ApplicationConfiguration.getSingleton().lookupConfigurationValue(principal.getName(), THUMBNAIL_WIDTH);
							if(null != thumbnailWidthString)
							try
							{								
								thumbnailWidth = Integer.parseInt(thumbnailWidthString);
							}
							catch(NumberFormatException nfe)
							{
								logger.warn("Invalid configuration value for " + THUMBNAIL_WIDTH + ", value='"+thumbnailWidthString+"'");
							}
							String thumbnailHeightString =  ApplicationConfiguration.getSingleton().lookupConfigurationValue(principal.getName(), THUMBNAIL_HEIGHT);
							if(null != thumbnailHeightString)
							try
							{								
								thumbnailHeight = Integer.parseInt(thumbnailHeightString);
							}
							catch(NumberFormatException nfe)
							{
								logger.warn("Invalid configuration value for " + THUMBNAIL_HEIGHT + ", value='"+thumbnailHeightString+"'");
							}

							thumbImg = Scalr.resize(
									img, 
									Method.AUTOMATIC, //scalingMethod - The method used for scaling the image; 
													// preferring speed to quality or a balance of both.
													// see org.imgscalr.Scalr.Method
									Mode.AUTOMATIC, //resizeMode
									thumbnailWidth,
									thumbnailHeight, 
									Scalr.OP_ANTIALIAS);
						}
						ImageWriter writer = ImageIO.getImageWriter(reader);
						ImageOutputStream output = new FileImageOutputStream(thumbnailFile);					
						writer.setOutput(output);
						writer.write(thumbImg);
						output.flush();
						output.close();						
					}
				//}
				//else
				//{
					//throw new IOException("Cannot write thumbnail file - " + thumbnailFile);
				//}
			}
			else
			{
				throw new IOException("Cannot make directories - " + thumbnailFile.getParentFile().getAbsolutePath());
			}
		} 	
	}

	// 			String[] values = map.get("DEFAULT");
	public File getDefaultThumbnail(String[] typeFlavors){
		// urls for default thumbnails:
		//	./thumbnail/dir?DEFAULT=dir
		//	./thumbnail/foo?DEFAULT=dir
		//	./thumbnail/bar?DEFAULT=wmv
		//	./thumbnail/?DEFAULT=true
		//	./thumbnail/noideawhathappenedhere
		//  http://server:8080/gallery/thumbnail/.squirrel-sql/cellImportExport.xml?DEFAULT=true   -> requestedFile is    /.squirrel-sql/cellImportExport.xml
		//  http://server:8080/gallery/thumbnail/.squirrel-sql?DEFAULT=foo   -> requestedFile is   /.squirrel-sql

		File returnFile = null;
		// return a mapped icon.
		try {
			// Allow defaultThumbnail to be configured.
			String[] prioritizedTypes = {".svg", ".gif", ".png", ".jpg"}; // <- get this from config 
			File defaultThumbnailFile = null;
			if(null!=typeFlavors)
			{
				// try to find a default thumbnail.  Return the first one found.
				for(int idx=0;idx<typeFlavors.length;idx++)
				{
					for(int typeIdx=0;typeIdx<prioritizedTypes.length;typeIdx++)
					{
						String defaultThumbnailURLString = "/WEB-INF/classes/" + typeFlavors[idx] + prioritizedTypes[typeIdx];
						//URL defaultThumbnailURL = getServletContext().getResource(defaultThumbnailURLString);
						URL defaultThumbnailURL = getResource(defaultThumbnailURLString);
						if(null!=defaultThumbnailURL)
						{				
							defaultThumbnailFile = new File(defaultThumbnailURL.getFile());
							break;
						}
						else
						{
							logger.warn("Could not get default thumbnail.  Resource " + defaultThumbnailURLString + " could not be found.");
						}
					}
				}
			}
			if(null == defaultThumbnailFile)
			{
				String defaultThumbnailURLString = //ApplicationConfiguration.getSingleton().lookupConfigurationValue(principal.getName(), DEFAULT_THUMBNAIL);
						"/WEB-INF/classes/default.svg";
				//URL defaultThumbnailURL = getServletContext().getResource(defaultThumbnailURLString);
				URL defaultThumbnailURL = getResource(defaultThumbnailURLString);
				if(null!=defaultThumbnailURL)
				{				
					defaultThumbnailFile = new File(defaultThumbnailURL.getFile());
				}
				else
				{
					logger.warn("Could not get default thumbnail.  Resource " + defaultThumbnailURLString + " could not be found.");
				}
			}
			if(null == defaultThumbnailFile)
			{
				logger.warn("Could not get default thumbnail.");
			}
			else
			{
				returnFile = defaultThumbnailFile;
			}
		} catch (MalformedURLException e) {
			logger.warn("Could not get default thumbnail.", e);
		}
		return returnFile;
	}

	public ResourceProvider getResourceProvider() {
		return resourceProvider;
	}

	public void setResourceProvider(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
	}
	
	/**
	 * This allows us to use the class when we might get the resource from a different context.
	 * 
	 * @param path
	 * @return
	 * @throws MalformedURLException
	 */
	protected URL getResource(String path) throws MalformedURLException{
		if(null==resourceProvider)
		{
			setResourceProvider(new ResourceProvider() {
					@Override
					public URL getResource(String path){
						// 	URL defaultThumbnailURL = getServletContext().getResource(defaultThumbnailURLString);
						return this.getClass().getResource(path);
					}
				}
			);
		}
		return getResourceProvider().getResource(path);
	}
}
