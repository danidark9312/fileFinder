package com.danielgutierrez.fileFinder.presentation;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FileFinderPresentation {
	
	public void searchFiles(String rootPath,Consumer<Map<String, List<Path>>> callback) {
		try {
			Map<String, List<Path>> sortedGroupedFiles = this.getSortedGroupedFiles(Paths.get(rootPath));
			//Path[] array = (Path[]) Files.walk(Paths.get(rootPath)).filter(Files::isRegularFile).toArray(Path[]::new);
			
			callback.accept(sortedGroupedFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 private Map<String, List<Path>> getSortedGroupedFiles(Path rootPath) throws IOException {
			return Files.walk(rootPath)
				.filter(Files::isRegularFile)
				.collect(Collectors.groupingBy(FileFinderPresentation::getExtension))
				.entrySet()
				.stream()
				.sorted((e1,e2)->e2.getValue().size()-e1.getValue().size())
				.collect(Collectors
						.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                LinkedHashMap::new));
		
	}
	private static String getExtension(Path path) {
 	String fileName = path.getFileName().toString();
 	return fileName.substring(fileName.lastIndexOf('.') + 1);
 }
	

	private static boolean areSimilarFiles(Path file1, Path file2) throws IOException {
		final byte buffer1[] = new byte[1024];
		final byte buffer2[] = new byte[1024];
		final int kilobytesThreshold = 100; // Amount of kilobytes to be compared
		int cont = 0;
		try (FileInputStream br1 = new FileInputStream(file1.toFile());
				FileInputStream br2 = new FileInputStream(file2.toFile())) {
			while (br1.read(buffer1) != -1 && br2.read(buffer2) != -1 && cont++ < kilobytesThreshold) {
				if (!Arrays.equals(buffer1, buffer2)) {
					return false;
				}
			}
		}
		return true;
	}
}
