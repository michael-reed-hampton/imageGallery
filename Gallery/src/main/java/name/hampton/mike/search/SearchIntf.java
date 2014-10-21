package name.hampton.mike.search;

import java.io.IOException;

import name.hampton.mike.gallery.SearchCriteria;
import name.hampton.mike.gallery.exception.InvalidPathException;

public interface SearchIntf {
	
	public Object search(SearchCriteria searchCriteria) throws InvalidPathException, IOException, SearchException;
}
