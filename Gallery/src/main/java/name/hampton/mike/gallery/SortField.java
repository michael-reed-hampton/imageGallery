package name.hampton.mike.gallery;

public class SortField {
	
	private boolean descending = true;
	private String fieldName;
	public boolean isDescending() {
		return descending;
	}
	public void setDescending(boolean descending) {
		this.descending = descending;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	@Override
	public String toString() {
		return "SortField [descending=" + descending + ", fieldName="
				+ fieldName + "]";
	}
}
