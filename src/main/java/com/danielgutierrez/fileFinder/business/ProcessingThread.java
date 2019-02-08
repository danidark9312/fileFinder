package com.danielgutierrez.fileFinder.business;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.danielgutierrez.fileFinder.view.MainFrame;

public class ProcessingThread {
	
	private int nThreads = MainFrame.threadsToUse;
	public static final int mgBytesDifferenceToFindCandidates = MainFrame.mgBytesDifferenceToFindCandidates;

	public void process(Map<String,? extends Collection<Path>> actionsGroup
			, Function<Collection<Path>,Collection<Collection<Path>>> compareProcess
			, Consumer<Collection<Collection<Path>>> callback) 
					throws InterruptedException {
		
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(nThreads);
		List<Callable<Collection<Collection<Path>>>> Callables = actionsGroup
		.entrySet()
		.stream()
		.map(Map.Entry::getValue)
		.map(list->createAction(list,compareProcess))
		.collect(Collectors.toList());
		
		List<Future<Collection<Collection<Path>>>> futures = newFixedThreadPool.invokeAll(Callables);
		for(Future<Collection<Collection<Path>>> future : futures) {
			try {
				Collection<Collection<Path>> resultList = future.get();
				callback.accept(resultList);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Callable<Collection<Collection<Path>>> createAction(Collection<Path> list,Function<Collection<Path>,Collection<Collection<Path>>> callback){
		return new Callable<Collection<Collection<Path>>>() {
			@Override
			public Collection<Collection<Path>> call() throws Exception {
				return callback.apply(list);
			}
			
		};
	}
	
	public Collection<Collection<Path>> getSimilarFiles(Collection<Path> listInput) throws IOException{
		Set<Collection<Path>> similarFilesSet = new HashSet<>();
		LinkedList<Path> stack = new LinkedList<>(listInput);
		Path temp1 = null;
		Path temp2 = null;
		while((temp1 = stack.poll())!=null) {
			List<Path> repeatedList = new ArrayList<>();
			repeatedList.add(temp1);
			similarFilesSet.add(repeatedList);
			int con = 0;
			while(con < stack.size()) {
				temp2 = stack.get(con);
				if(areCandidates(temp1,stack.get(con)) && areSimilarFiles(temp1, temp2)) {
					stack.remove(temp2);
					repeatedList.add(temp2);
				}else {
					con++;
				}
			}
			/*remove single record list*/
			if(repeatedList.size() == 1)
				similarFilesSet.remove(repeatedList);
		}
		
		return similarFilesSet;
	}
	
	private boolean areCandidates(Path temp1, Path temp2) throws IOException {
		if(Math.abs(Files.size(temp1)-Files.size(temp2))<(ProcessingThread.mgBytesDifferenceToFindCandidates*1024*1024)) {
			return true;
		}
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
