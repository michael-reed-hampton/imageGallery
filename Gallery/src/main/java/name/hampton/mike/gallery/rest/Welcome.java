package name.hampton.mike.gallery.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Welcome {
	
	private String domain;
	private String message;

	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
