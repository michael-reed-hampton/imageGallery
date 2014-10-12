package name.hampton.mike.gallery;

import java.util.Date;
import java.util.List;



public interface GalleryItemIntf {

	public String getName();
	public void setName(String value);
	
	public String getPath();
	public void setPath(String value);

	public String getType();
	public void setType(String value);

	public boolean isAllowsChildren();
	public void setAllowsChildren(boolean allowsChildren);

	public Date getLastModified();
	public void setLastModified(Date date);
	
	public long getSize();
	public void setSize(long length);
	
	public void setChildren(List<GalleryItemIntf> galleryItems);
	public List<GalleryItemIntf> getChildren();
}
