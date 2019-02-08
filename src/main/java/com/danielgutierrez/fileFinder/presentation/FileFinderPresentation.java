package com.danielgutierrez.fileFinder.presentation;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.danielgutierrez.fileFinder.business.ProcessingThread;

public class FileFinderPresentation {
	
	ProcessingThread processingThread = new ProcessingThread();
	
	public Map<String, List<Path>> searchFiles(String rootPath,Consumer<Map<String, List<Path>>> peek) throws InterruptedException {
		Map<String, List<Path>> sortedGroupedFiles = null;
		try {
			sortedGroupedFiles = this.getSortedGroupedFiles(Paths.get(rootPath));
			if(peek!=null)
				peek.accept(sortedGroupedFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sortedGroupedFiles;
	}
	public Map<String, List<Path>> searchFiles(String rootPath) throws InterruptedException {
		return this.searchFiles(rootPath,null);
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
	
	
	public void findDuplicatedFiles(Map<String, List<Path>> listFilesGroupByExt,
			Consumer<Collection<Collection<Path>>> callback) throws InterruptedException {
		Function<Collection<Path>,Collection<Collection<Path>>> compareProcess = t -> {
			try {
				return this.processingThread.getSimilarFiles(t);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		};
		this.processingThread.process(listFilesGroupByExt, compareProcess, callback);
	}
}
