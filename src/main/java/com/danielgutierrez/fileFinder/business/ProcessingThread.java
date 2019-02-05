package com.danielgutierrez.fileFinder.business;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class ProcessingThread {
	
	private int nThreads;

	public <T>void process(Map<String,? extends Collection<T>> actionsGroup, UnaryOperator<Collection<T>> callback) {
		Collection<T> collection = actionsGroup.get("BLN");
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(nThreads);
		actionsGroup
		.entrySet()
		.stream()
		//.flatMap((map)->map.getValue().stream())
		.map(Map.Entry::getValue)
		.map(list->{
			return new Callable<Collection<T>>() {
				@Override
				public Collection<T> call() throws Exception {
					return callback.apply(list);
				}
			};
		})
		;
		
		
		
		//newFixedThreadPool.invoksubmit(()->callback.)
		//callback.accept(files);
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
