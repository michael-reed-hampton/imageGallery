package name.hampton.mike.gallery;

import java.io.File;

import name.hampton.mike.gallery.exception.InvalidPathException;

public abstract class DirectoryDisplay {
	
	protected abstract File getBaseDir() throws InvalidPathException;

	public DisplayData search(SearchCriteria searchCriteria) {
		if(null == searchCriteria)
		{
			searchCriteria = new SearchCriteria();
		}
		DisplayData displayData = new DisplayData();
		displayData.setStatus(new Status());

		try {
			File baseDir = getBaseDir();
			File directory = ValidationTools.validateDirectory(new File(baseDir,
						searchCriteria.getDirectory()));
			if (null != directory) {
				// TODO: Filter based on handlers available.
				// TODO: This needs to be the search implementation.
				File[] files = directory.listFiles();
				
				GalleryItemIntf[] galleryItems = new GalleryItemIntf[files.length];
				
				int index = 0;
				for(File file : files){
					GalleryItemIntf galleryItem = new GalleryItem();
					
					galleryItem.setName(file.getName());
					
					String path = file.getAbsolutePath();
					String relativePath = path.substring(baseDir.getAbsolutePath().length());
					String swapSlashesPath = relativePath.replace("\\", "/");
					galleryItem.setPath(swapSlashesPath);
					
					if(file.isDirectory()){
						galleryItem.setType("folder");
					}
					else
					{
						int lastDot = file.getName().lastIndexOf(".");
						if((-1 == lastDot) || lastDot == file.getName().length()){
							galleryItem.setType("unknown");
						}
						else
						{
							galleryItem.setType(file.getName().substring(lastDot+1));
						}
					}
						
					
					galleryItems[index++] = galleryItem;
				}
				displayData.setData(galleryItems);

				displayData.getStatus().setSuccess(true);
				displayData.getStatus().setDescription("Simple return");
			}
		} 
		catch (InvalidPathException ipe) {
			ipe.printStackTrace();
			displayData.getStatus().setSuccess(false);
			displayData.getStatus().setDescription("Application is configured incorrectly, contact administrator. " + 
					ipe.getMessage() + 
					", invalid path = '" +  ipe.getInput() + "'");
		}
		catch (Exception ex) {
			ex.printStackTrace();
			displayData.getStatus().setSuccess(false);
			displayData.getStatus().setDescription(ex.getMessage());
		}
		return displayData;
	};
}
