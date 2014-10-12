package name.hampton.mike.gallery.solr;

import java.util.EventObject;

public class SOLRIndexingEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7683639164934217299L;
	
	public static enum EventType {
		CORE_CREATED, CORE_CREATE_FAILED, INDEXING_START, INDEXING_COMPLETE, INDEXING_ERROR, INDEXING_PROGRESS, ITEM_INDEXED, ITEM_DELETED,
	};

	private EventType eventType;
	private int status = 0;
	private Object error;
	private int count = -1;
	private String pathString;
	
	public SOLRIndexingEvent(Object source, EventType eventType) {
		super(source);
		this.eventType = eventType;
	}

	public SOLRIndexingEvent(Object source, String pathString,
			EventType coreCreated) {
		this(source, coreCreated);
		setPathString(pathString);		
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Object getError() {
		return error;
	}

	public void setError(Object error) {
		this.error = error;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public String getPathString() {
		return pathString;
	}

	public void setPathString(String pathString) {
		this.pathString = pathString;
	}

	@Override
	public String toString() {
		return "SOLRIndexingEvent [source=" + source + ", eventType=" + eventType + ", pathString=" + pathString + ", status="
				+ status + ", error=" + error + ", count=" + count + "]";
	}

}
