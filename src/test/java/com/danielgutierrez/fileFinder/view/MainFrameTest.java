package com.danielgutierrez.fileFinder.view;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.danielgutierrez.fileFinder.model.FileSizeCached;

public class MainFrameTest {
	
	 	@Test
	    public void testFindDuplicatedFiles() {
	 		MainFrame mainFrame = new MainFrame();
	 		try {
				mainFrame.findDuplicatedFiles(getListPath2());
				assertTrue(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
				assertTrue(false);
			}
	        
	    }

		private Map<String, List<Path>> getListPath() {
			Map<String, List<Path>> result = new HashMap<>();
			result.put("swf", Arrays
					.asList(
							Paths.get("C:\\Users\\daniel.gutierrez\\Desktop\\2019-01-11_1439 - Copy.swf"),
							Paths.get("C:\\Users\\daniel.gutierrez\\Desktop\\2019-01-11_1439.swf"),
							Paths.get("C:\\Users\\daniel.gutierrez\\Desktop\\2019-01-11_1455_1.swf"),
							Paths.get("C:\\Users\\daniel.gutierrez\\Desktop\\perfil.png"),
							Paths.get("C:\\Users\\daniel.gutierrez\\Desktop\\Real-Time Dashboard for AWS Account Activity.pdf")
							));
			
			
			return result;
		}
		private Map<String, List<FileSizeCached>> getListPath2() {
			Map<String, List<FileSizeCached>> result = new HashMap<>();
			result.put("swf", Arrays
					.asList(
							Paths.get("C:\\Users\\daniel\\Desktop\\Conciliacion Brian\\fotos apartamento\\IMG_4417.jpg"),
							Paths.get("C:\\Users\\daniel\\Desktop\\Conciliacion Brian\\fotos apartamento\\IMG_4418.jpg"),
							Paths.get("C:\\Users\\daniel\\Desktop\\Conciliacion Brian\\fotos apartamento\\IMG_4419.jpg"),
							Paths.get("C:\\Users\\daniel\\Desktop\\Conciliacion Brian\\fotos apartamento\\IMG_4417c.jpg")
							).stream().map(FileSizeCached::new).collect(Collectors.toList())
					);
			
			
			return result;
		}
		
		
}
