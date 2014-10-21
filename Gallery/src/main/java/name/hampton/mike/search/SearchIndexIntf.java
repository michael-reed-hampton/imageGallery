package name.hampton.mike.search;

import java.io.IOException;

import name.hampton.mike.ObservableIntf;

/**
 * Note that the passed "item" in each method may be the actual item, or may
 * represent some type of 'key' that the implementation will know how to use
 * to reference the item.
 * 
 * @author mike.hampton
 *
 */
public interface SearchIndexIntf extends ObservableIntf {
	
	// Create
	public int indexItem(Object item) throws SearchException, IOException;
	// Read is housed in the SearchIntf
	// Update	
	public int reIndexItem(Object item) throws SearchException, IOException;
	// Delete
	public int deleteItem(Object item) throws SearchException, IOException;
}
