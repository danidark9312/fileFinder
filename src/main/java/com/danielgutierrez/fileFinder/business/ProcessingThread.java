package com.danielgutierrez.fileFinder.business;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.SwingWorker;

import com.danielgutierrez.fileFinder.model.FileSizeCached;
import com.danielgutierrez.fileFinder.view.MainFrame;

public class ProcessingThread {
	
	private MainFrame mainFrame = null;
	
	public ProcessingThread(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
	}

	public void process(Map<String,? extends Collection<FileSizeCached>> actionsGroup
			, Function<Collection<FileSizeCached>,Collection<Collection<FileSizeCached>>> compareProcess
			, Consumer<Collection<Collection<FileSizeCached>>> callback, SwingWorker worker) 
					throws InterruptedException {
		final Instant beforeProcessing = Instant.now();
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(MainFrame.threadsToUse);
		List<Callable<Collection<Collection<FileSizeCached>>>> Callables = actionsGroup
		.entrySet()
		.stream()
		.map(Map.Entry::getValue)
		.map(list->createAction(list,compareProcess))
		.collect(Collectors.toList());
		
		List<Future<Collection<Collection<FileSizeCached>>>> futures = newFixedThreadPool.invokeAll(Callables);
		for(Future<Collection<Collection<FileSizeCached>>> future : futures) {
			try {
				Collection<Collection<FileSizeCached>> resultList = future.get();
				callback.accept(resultList);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		this.mainFrame.writeLogsf("Processing time [%d seconds]", ChronoUnit.MILLIS.between(beforeProcessing, Instant.now())/1_000);
		
		;
	}
	
	private Callable<Collection<Collection<FileSizeCached>>> createAction(Collection<FileSizeCached> list,Function<Collection<FileSizeCached>,Collection<Collection<FileSizeCached>>> callback){
		return new Callable<Collection<Collection<FileSizeCached>>>() {
			@Override
			public Collection<Collection<FileSizeCached>> call() throws Exception {
				return callback.apply(list);
			}
			
		};
	}
	
	public Collection<Collection<FileSizeCached>> getSimilarFiles(Collection<FileSizeCached> listInput, SwingWorker worker) throws IOException{
		Set<Collection<FileSizeCached>> similarFilesSet = new HashSet<>();
		LinkedList<FileSizeCached> stack = new LinkedList<>(listInput);
		Collections.sort(stack);
		FileSizeCached temp1 = null;
		FileSizeCached temp2 = null;
		while((temp1 = stack.poll())!=null && !worker.isCancelled()) {
			List<FileSizeCached> repeatedList = new ArrayList<>();
			repeatedList.add(temp1);
			similarFilesSet.add(repeatedList);
			//int con = 0;
			
			int indexOfCandidate = 0;
			Queue<FileSizeCached> possibleMatchs = new LinkedList<>();
			while((indexOfCandidate = Collections.binarySearch(stack, temp1))>=0) {
				temp2 = stack.get(indexOfCandidate);
				stack.remove(temp2);
				if(/*areCandidates(temp1,temp2) && */areSimilarFiles(temp1.getFile(), temp2.getFile())) {
					repeatedList.add(temp2);
				}else {
					possibleMatchs.add(temp2);
				}
			}
			if(possibleMatchs.size()>0) {
				stack.addAll(possibleMatchs);
				possibleMatchs.clear();	
			}

			/*remove single record list*/
			if (repeatedList.size() == 1) {
				System.out.println("No repeated files for["+repeatedList.get(0).toString()+"]");
				if (MainFrame.fullDebug)
					this.mainFrame.writeLogs("No repeated files for["+repeatedList.get(0).toString()+"]");
				similarFilesSet.remove(repeatedList);
			}else {
				System.out.println("Repeated files for["+repeatedList.get(0).toString()+"]");
			}
			updateUIProcessFiles(repeatedList.size());
		}
		
		return similarFilesSet;
	}
	
	private void updateUIProcessFiles(int sumValue) {
		this.mainFrame.sumFilesProcessed(sumValue);
	}
	
	private boolean areCandidates(FileSizeCached temp1, FileSizeCached temp2) throws IOException {
		//if(Math.abs(temp1.getSize()-temp2.getSize())<(MainFrame.mgBytesDifferenceToFindCandidates*1024*1024)) {
		if(temp1.getSize()-temp2.getSize()==0)
			return true;
		return false;
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
