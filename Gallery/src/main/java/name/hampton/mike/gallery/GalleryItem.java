package name.hampton.mike.gallery;

import java.util.Date;
import java.util.List;

public class GalleryItem implements GalleryItemIntf{

	String name;
	String path;
	String type;
	Date lastModified;
	long size;
	boolean allowsChildren = false;
	List<GalleryItemIntf> children;

	public GalleryItem() {
		super();
	}

	@Override
	public String toString() {
		return "GalleryItem ["
				+  "\n name=" + name 
				+ ",\n path=" + path 
				+ ",\n type=" + type 
				+ ",\n lastModified=" + lastModified 
				+ ",\n size=" + size
				+ ",\n allowsChildren=" + allowsChildren 
				+ ",\n children="	+ children + 
				"\n]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isAllowsChildren() {
		return allowsChildren;
	}

	public void setAllowsChildren(boolean allowsChildren) {
		this.allowsChildren = allowsChildren;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	protected void copy(GalleryItemIntf galleryItem){
		this.setAllowsChildren(galleryItem.isAllowsChildren());
		this.setLastModified(galleryItem.getLastModified());
		this.setName(galleryItem.getName());
		this.setPath(galleryItem.getPath());
		this.setSize(galleryItem.getSize());
		this.setType(galleryItem.getType());
	}


	@Override
	public List<GalleryItemIntf> getChildren() {
		return children;
	}

	@Override
	public void setChildren(List<GalleryItemIntf> children) {
		this.children = children;
	}
}