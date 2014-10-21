package name.hampton.mike.gallery;

import java.security.Principal;
import java.util.EventObject;
import java.util.List;

public class ConfigurationChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4998591344483797945L;
	
	private Principal principal;
	private List<String> changedKeys;
	
	public ConfigurationChangeEvent(Object source, Principal principal, List<String> changedKeys) {
		super(source);
		this.principal = principal;
		this.changedKeys = changedKeys;		
	}

	public Principal getPrincipal() {
		return principal;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public List<String> getChangedKeys() {
		return changedKeys;
	}

	public void setChangedKeys(List<String> changedKeys) {
		this.changedKeys = changedKeys;
	}
}
