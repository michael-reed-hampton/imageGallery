package name.hampton.mike.gallery.solr;

import java.io.File;
import java.io.IOException;

import org.apache.solr.client.solrj.util.ClientUtils;

public class SOLRUtilities {

	public static String getCoreIDForPath(String pathString) throws IOException {
		// Hash the directory to a name. This needs to be an algorithm that
		// deterministically returns a string from a directory that is unique.
		File pathStringFile = new File(pathString);
		pathString = pathStringFile.getCanonicalPath();

		// This is not great, but it is not that interesting a problem...
		String coreID = pathString.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
		return coreID;
	}
	
	// Lucene supports escaping special characters that are part of the query syntax. The current list special characters are
	//  + - && || ! ( ) { } [ ] ^ " ~ * ? : \
	public static String escapeQueryChars(String input){
		return ClientUtils.escapeQueryChars(input);
	}

}
