package com.danielgutierrez.fileFinder.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class Logger {
	
	private JTextComponent outPutComponent;
	private FixedQueue<String> logRecords;
	private int maxSizeOfRecordList = 200;

	public Logger(JTextComponent component) {
		this.outPutComponent = component;
		this.logRecords = new FixedQueue<>(maxSizeOfRecordList);
	}
	public void tryToWriteLog(String... text) {
		logRecords.addAll(Arrays.asList(text));
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
		
		public boolean addAll(List<E> list) {
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
			
			SwingUtilities.invokeLater(()->outPutComponent.setText(textToWrite));
		}

		/**
		 * Release the 10 older records
		 */
		private void freeSpace() {
			IntStream.range(0, 10).forEach((i->this.remove()));
		}
	}

}
