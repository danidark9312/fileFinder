package com.danielgutierrez.fileFinder.business;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ProcessingThread {
	
	private int nThreads;

	public <T>void process(Map<String,? extends Collection<T>> actionsGroup, UnaryOperator<Collection<T>> callback) throws InterruptedException {
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(nThreads);
		List<Callable<Collection<T>>> Callables = actionsGroup
		.entrySet()
		.stream()
		.map(Map.Entry::getValue)
		.map(list->createAction(list,callback))
		.collect(Collectors.toList());
		
		List<Future<Collection<T>>> futures = newFixedThreadPool.invokeAll(Callables);
		for(Future<Collection<T>> future : futures) {
			try {
				Collection<T> resultList = future.get();
				callback.apply(resultList);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private <V>Callable<Collection<V>> createAction(Collection<V> list,UnaryOperator<Collection<V>> callback){
		return new Callable<Collection<V>>() {
			@Override
			public Collection<V> call() throws Exception {
				return callback.apply(list);
			}
			
		};
	}

}
