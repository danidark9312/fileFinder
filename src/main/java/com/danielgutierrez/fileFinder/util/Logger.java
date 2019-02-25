package com.danielgutierrez.fileFinder.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.danielgutierrez.fileFinder.view.MainFrame;

public class Logger {
	
	private JTextComponent outPutComponent;
	private FixedQueue<String> logRecords;
	private int maxSizeOfRecordList = 200;

	public Logger(JTextComponent component) {
		this.outPutComponent = component;
		this.logRecords = new FixedQueue<>(maxSizeOfRecordList);
	}
	public void tryToWriteLog(String... text) {
		if(MainFrame.fullDebug)
			System.out.println(Arrays.stream(text).reduce((a,b)->a.concat(b).concat("\n")).get());
		
		List<String> lines = Arrays
		.asList(text)
		.stream()
		.map(line->line.split("\n"))
		.flatMap(Arrays::stream)
		.collect(Collectors.toList());
		
		new Thread(()->logRecords.addAll(lines)).start();
	}
	public void tryToWriteLogf(String text,Object... param) {
		this.tryToWriteLog(String.format(text, param));
	}
	
	private class FixedQueue<E> extends LinkedList<E>{
		private int maxSize;

		public FixedQueue(int maxSize) {
			super();
			this.maxSize = maxSize;
		}
		
		public boolean add(E e) {
			if(this.size()>this.maxSize)
				freeSpace();
			return this.addAll(Arrays.asList(e));
		}
		
		
		public synchronized boolean addAll(List<E> list) {
			if(list.size()>this.maxSize)
				list = list.subList(list.size()-this.maxSize, list.size());
			while(this.size()+list.size()>this.maxSize)
				freeSpace();
			boolean recordsAdded= super.addAll(list);
			if(recordsAdded)
				notifyListUpdate();
			return recordsAdded;
		}
		
		private void notifyListUpdate() {
			String textToWrite = this.stream()
				.map(Object::toString)
				.reduce((a,b)->a+"\n"+b)
				.get();
			try {
				SwingUtilities.invokeAndWait(()->outPutComponent.setText(textToWrite));
			} catch (InvocationTargetException|InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Release the 10 older records
		 */
		private void freeSpace() {
			IntStream.range(0, 10).forEach((i->this.remove()));
		}
	}

}
