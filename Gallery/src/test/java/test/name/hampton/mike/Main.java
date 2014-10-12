package test.name.hampton.mike;

import java.io.File;
import java.io.IOException;

public class Main {

	public Main() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IOException {
		
		File file = new File("C:\\Users\\mike.hampton");
		
		File[] files = file.listFiles();
		
		for(File fileX : files)
		{
			file = fileX;
			System.out.println(file.toPath() + " = "+java.nio.file.Files.probeContentType(file.toPath()));
		}
	}
}
