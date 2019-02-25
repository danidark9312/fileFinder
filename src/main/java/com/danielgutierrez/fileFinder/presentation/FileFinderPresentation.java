package com.danielgutierrez.fileFinder.presentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.SwingWorker;

import com.danielgutierrez.fileFinder.business.ProcessingThread;
import com.danielgutierrez.fileFinder.model.FileSizeCached;
import com.danielgutierrez.fileFinder.view.MainFrame;

public class FileFinderPresentation {
	
	private MainFrame mainFrame = null;
	private ProcessingThread processingThread;
	
	public FileFinderPresentation(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		this.processingThread = new ProcessingThread(this.mainFrame);
	}
	
	
	public Map<String, List<FileSizeCached>> searchFiles(String rootPath,Consumer<Map<String, List<FileSizeCached>>> peek, String filter) throws InterruptedException {
		Map<String, List<FileSizeCached>> sortedGroupedFiles = null;
		try {
			sortedGroupedFiles = this.getSortedGroupedFiles(Paths.get(rootPath), filter);
			if(peek!=null)
				peek.accept(sortedGroupedFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sortedGroupedFiles;
	}
	public Map<String, List<FileSizeCached>> searchFiles(String rootPath, String filter) throws InterruptedException {
		return this.searchFiles(rootPath,null, filter);
	}
	
	 private Map<String, List<FileSizeCached>> getSortedGroupedFiles(Path rootPath, String filter) throws IOException {
			return Files.walk(rootPath)
				.filter(Files::isRegularFile)
				.map(FileSizeCached::new)
				.filter(file->file.getSize()>0)
				.filter(getFilterByPattern(filter))
				.collect(Collectors.groupingBy(FileFinderPresentation::getExtension))
				.entrySet()
				.stream()
				.sorted((e1,e2)->e2.getValue().size()-e1.getValue().size())
				.collect(Collectors
						.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                LinkedHashMap::new));
		
	}

	private Predicate<? super FileSizeCached> getFilterByPattern(String filter) {
		if(filter == null)
			return (file)->true;
		else
			return file->{
				String extension = FileFinderPresentation.getExtension(file).toUpperCase();
				Matcher matcher = Pattern.compile(filter).matcher(extension);
				return matcher.matches();
			};
	}

	public static void main(String[] args) {
		
	}

	private static String getExtension(FileSizeCached fileSizeCached) {
		String fileName = fileSizeCached.getFile().getFileName().toString();
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}
	
	
	public void findDuplicatedFiles(Map<String
			, List<FileSizeCached>> listFilesGroupByExt
			,Consumer<Collection<Collection<FileSizeCached>>> callback, SwingWorker worker) throws InterruptedException {
		
		Function<Collection<FileSizeCached>, Collection<Collection<FileSizeCached>>> compareProcess = fileList -> {
			try {
				return this.processingThread.getSimilarFiles(fileList, worker);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		};
		this.processingThread.process(listFilesGroupByExt, compareProcess, callback, worker);
	}
	
}
