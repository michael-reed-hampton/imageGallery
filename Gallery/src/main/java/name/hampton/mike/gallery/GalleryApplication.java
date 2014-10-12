package name.hampton.mike.gallery;

import name.hampton.mike.gallery.servlet.ThumbnailServlet;

/**
 * A place to store Application keys for the gallery Application.
 * 
 * @author mike.hampton
 *
 */
public class GalleryApplication {
	
	public static final String BASE_DIR = "baseDir";
	public static final String THUMBNAIL_DIR = ThumbnailServlet.THUMBNAIL_DIR;
	public static final String DEFAULT_THUMBNAIL = ThumbnailServlet.DEFAULT_THUMBNAIL;
	public static final String THUMBNAIL_WIDTH = ThumbnailServlet.THUMBNAIL_WIDTH;
	public static final String THUMBNAIL_HEIGHT = ThumbnailServlet.THUMBNAIL_HEIGHT;
	public static final String DEFAULT_KEY = name.hampton.mike.gallery.servlet.ConfigureServlet.DEFAULT_KEY;
	
	public static final String SOLR_URL = "solrURL";
}
