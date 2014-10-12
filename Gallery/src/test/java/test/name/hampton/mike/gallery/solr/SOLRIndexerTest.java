package test.name.hampton.mike.gallery.solr;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import junit.framework.TestCase;
import name.hampton.mike.gallery.solr.SOLRIndexer;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOLRIndexerTest extends TestCase {
	
	// look at http://lucidworks.com/blog/indexing-with-solrj/
	static Logger logger = LoggerFactory.getLogger(SOLRIndexerTest.class.getName());

	public void testWatchDir() throws SolrServerException, IOException{
	}
	
	public static void main(String s[]) throws IOException {
		final String pathString = "C:\\Users\\mike.hampton\\Pictures\\temp";
		final SOLRIndexer indexer = new SOLRIndexer("http://localhost:8983/solr", pathString);
		
		Observer observer = new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				logger.debug("Observable update: " + arg);
			}
		};
		indexer.addObserver(observer);		
		
		JFrame frame = new JFrame("JFrame Source Demo");
		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		// This is an empty content area in the frame
		JLabel jlbempty = new JLabel("Test of SOLRIndexer Watching a directory");
		frame.getContentPane().add(jlbempty, BorderLayout.NORTH);
		final JButton button = new JButton("Start Indexer, and Watcher");
		frame.getContentPane().add(button, BorderLayout.SOUTH);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(button.getText().equals("Start Indexer, and Watcher")){
					button.setText("It's running...");
					try {
						indexer.reIndexDir(pathString);
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (SolrServerException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		final JTextArea textArea = new JTextArea(20, 100);
		Observer observer2 = new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				textArea.append("Observable update: " + arg + "\n");
			}
		};
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
		indexer.addObserver(observer2);		
		frame.pack();
		frame.setVisible(true);
	}
}