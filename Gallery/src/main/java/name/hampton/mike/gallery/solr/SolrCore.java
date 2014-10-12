package name.hampton.mike.gallery.solr;

import java.io.File;

/**
 * Utility bean class
 * 
 * @author mike.hampton
 *
 */
public class SolrCore {
	private String name;
	private boolean isDefaultCore;
	private File instanceDir;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDefaultCore() {
		return isDefaultCore;
	}

	public void setDefaultCore(boolean isDefaultCore) {
		this.isDefaultCore = isDefaultCore;
	}

	public File getInstanceDir() {
		return instanceDir;
	}

	public void setInstanceDir(File instanceDir) {
		this.instanceDir = instanceDir;
	}
}

