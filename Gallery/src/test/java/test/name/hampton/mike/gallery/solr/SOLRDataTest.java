package test.name.hampton.mike.gallery.solr;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import name.hampton.mike.gallery.GalleryItem;
import name.hampton.mike.gallery.SearchCriteria;
import name.hampton.mike.gallery.exception.InvalidPathException;

import org.apache.solr.client.solrj.SolrServerException;


public class SOLRDataTest
//extends TestCase 
{
	/*

	private static final String HTTP_LOCALHOST_8983_SOLR = "http://localhost:8983/solr";
	
	public SOLRDataTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSetSOLRURL() {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		assertEquals("Url is not set properly.", solrData.getUrl(), HTTP_LOCALHOST_8983_SOLR);
		solrData = null;
	}

	public void testSetBaseDir() throws InvalidPathException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		assertEquals("Core not set properly.", solrData.getBaseDir(), searchPath);
		solrData = null;
	}

	// should be the same as testSearchEmpty - with my current test setup, it should return two things:
	// the 000.jpg and the dir C:/Users/mike.hampton/Pictures/temp/xmen
	public void testSearchNull() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		GalleryItem result = (GalleryItem)solrData.search(null);
		assertEquals("Did not get expected results.", 2, result.getChildren().size());
		solrData = null;
	}

	// should be the same as testSearchNull - with my current test setup, it should return two things:
	// the 000.jpg and the dir C:/Users/mike.hampton/Pictures/temp/xmen
	public void testSearchEmpty() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 2, result.getChildren().size());
		solrData = null;
	}

	// with my current test setup, it should return 8 things
	public void testSearchEmptyWithSubDir() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		searchCriteria.setSubdirectories(true);
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 8, result.getChildren().size() );
		solrData = null;
	}

	// with my current test setup, it should return 1 thing
	public void testSearchGetDirectories() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("dir", "true");
		searchCriteria.setFields(fields );
		
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 1, result.getChildren().size() );
		solrData = null;
	}

	// with my current test setup, it should return 3 things
	// Note that it returns the directory you asked for subdirectories of as well.
	public void testSearchGetAllSubDirectories() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		searchCriteria.setSubdirectories(true);
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("dir", "true");
		searchCriteria.setFields(fields );
		
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 3, result.getChildren().size() );
		solrData = null;
	}

	// with my current test setup, it should return 1 thing
	public void testSearchImagesOnly() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("content_type", "image/*");
		searchCriteria.setFields(fields );
		
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 1, result.getChildren().size() );
		solrData = null;
	}

	// with my current test setup, it should return 5 things
	public void testSearchImagesOnlySubDirIncluded() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		searchCriteria.setSubdirectories(true);
		
		Map<String, String> fields = new HashMap<String, String>();
		fields.put("content_type", "image/*");
		searchCriteria.setFields(fields );
		
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 5, result.getChildren().size() );
		solrData = null;
	}

	// with my current test setup, it should return 1 thing
	public void testSearchFreeText() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		
		searchCriteria.setFreeText("Author");
		
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 1, result.getChildren().size() );
		solrData = null;
	}

	// with my current test setup, it should return 0 things
	public void testSearchFreeTextNEG() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		
		searchCriteria.setFreeText("NotPresent");
		
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 0, result.getChildren().size() );
		solrData = null;
	}

	// with my current test setup, it should return 2 things
	public void testSearchFreeTextWithSubDir() throws SolrServerException, InvalidPathException, IOException {
		SOLRData solrData = new SOLRData();
		solrData.setUrl(HTTP_LOCALHOST_8983_SOLR);
		File searchPath = new File("C:\\Users\\mike.hampton\\Pictures\\temp");
		solrData.setBaseDir(searchPath);
		SearchCriteria searchCriteria = new SearchCriteria();
		searchCriteria.setSubdirectories(true);
		
		searchCriteria.setFreeText("Author");
		
		GalleryItem result = (GalleryItem)solrData.search(searchCriteria);
		assertEquals("Did not get expected results.", 2, result.getChildren().size() );
		solrData = null;
	}
*/
}
