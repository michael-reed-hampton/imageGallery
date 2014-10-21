package name.hampton.mike.gallery;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * This may get a lot more elaborate over time.  I want to be able to abstract the search engine 
 * from the query language.  This is my way to do it.
 *
 * Most frequent use cases:
 *  	searching in a specified directory.
 *		searching in a specified directory, and all those beneath it hierarchically.
 *  	a free text query.  This is searching in the entire set.
 * 
 * 
 * @author mike.hampton
 *
 */
@XmlRootElement
public class SearchCriteria {

	public static final String OR = "OR";
	public static final String AND = "AND";

	private String freeText;

	private int startIndex = -1;
	private int numberOfRowsToReturn = -1;
	private List<SortField> sortFields = null;

	/**
	 * The directory to start from.  If not specified, the root is assumed.
	 */
	private String directory = "";
	private boolean subdirectories = false;
	private String operator = OR;
	private Map<String, String> fields = null;
	private boolean flatten = false;
	
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public boolean isSubdirectories() {
		return subdirectories;
	}

	public void setSubdirectories(boolean subdirectories) {
		this.subdirectories = subdirectories;
	}
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public String getFreeText() {
		return freeText;
	}

	public void setFreeText(String freeText) {
		this.freeText = freeText;
	}

	public boolean isFlatten() {
		return flatten;
	}

	public void setFlatten(boolean flatten) {
		this.flatten = flatten;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getNumberOfRowsToReturn() {
		return numberOfRowsToReturn;
	}

	public void setNumberOfRowsToReturn(int numberOfRowsToReturn) {
		this.numberOfRowsToReturn = numberOfRowsToReturn;
	}

	public List<SortField> getSortFields() {
		return sortFields;
	}

	public void setSortFields(List<SortField> sortFields) {
		this.sortFields = sortFields;
	}

}