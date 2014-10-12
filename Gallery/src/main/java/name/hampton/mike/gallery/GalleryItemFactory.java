package name.hampton.mike.gallery;

import java.io.File;
import java.util.Date;

import name.hampton.mike.gallery.exception.InvalidPathException;


/**
 * This is the beginning of a factory.  For right now it is all static, but the idea
 * is to make this more elaborate and capable over time.  Might make this into an 
 * abstract factory long term.
 * 
 * @author mike.hampton
 *
 */
public class GalleryItemFactory {
	
	private static volatile GalleryItemFactory instance = null;
	
	public static final GalleryItemFactory getInstance() {
        if (instance == null) {
            synchronized (GalleryItemFactory.class) {
                // Double check
                if (instance == null) {
                    instance = new GalleryItemFactory();
                }
            }
        }
        return instance;
	}
	
	private GalleryItemFactory()
	{
		
	}
	
	public GalleryItemIntf createGalleryItem(File baseDir, String path) throws InvalidPathException{
		String swapSlashesPath = path.replace("\\", "/");
		File directory = ValidationTools.validateItem(new File(baseDir,
				swapSlashesPath));
		return createGalleryItem(baseDir, directory);
	}

	public GalleryItemIntf createGalleryItem(File baseDir, File path) throws InvalidPathException{
		GalleryItemIntf galleryItem = new GalleryItem();
		galleryItem = populateGalleryItemCommonMetaData(baseDir, path, galleryItem);
		galleryItem = populateGalleryItemCustomMetaData(baseDir, path, galleryItem);
		return galleryItem;
	}

	private GalleryItemIntf populateGalleryItemCommonMetaData(File baseDir, File file,
			GalleryItemIntf galleryItem) {
		galleryItem.setName(file.getName());
		String path = file.getAbsolutePath();
		String relativePath = path.substring(baseDir.getAbsolutePath().length());
		if(relativePath.startsWith(System.getProperty("file.separator"))){
			relativePath = relativePath.substring(1);
		}
		String swapSlashesPath = relativePath.replace("\\", "/");
		galleryItem.setPath(swapSlashesPath);
		galleryItem.setLastModified(new Date(file.lastModified()));
		if(file.isDirectory()){
			// reseat the type.  We have to do this because we do not know what this is
			// until now...
			galleryItem.setType("folder");
			galleryItem.setAllowsChildren(true);
		}
		else
		{
			galleryItem.setSize(file.length());

			int lastDot = file.getName().lastIndexOf(".");
			if((-1 == lastDot) || lastDot == file.getName().length()){
				galleryItem.setType("unknown");
			}
			else
			{
				galleryItem.setType(file.getName().substring(lastDot+1));
			}
		}
		
		return galleryItem;
	}
	
	/**
	 * Idea here is to add information to the galleryitem.  This could possible return 
	 * a different instance than the one passed in.
	 * 
	 * @param baseDir
	 * @param file
	 * @param galleryItem
	 */
	private GalleryItemIntf populateGalleryItemCustomMetaData(File baseDir, File file,
			GalleryItemIntf galleryItem) {
		
		// 	First question, how do we map from a gallery item to a class that will add to its info?		
		// galleryItem.getType() -> something...maybe
		//	Second question, how can we make sure the class we create has enough info to add to the
		//		gallery item the way it wants to?
		// no idea...
		
		return galleryItem;		
	}
}
