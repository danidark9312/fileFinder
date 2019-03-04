package com.danielgutierrez.fileFinder.model;

import java.awt.Color;
import java.io.Serializable;

public class FileCacheRow implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private FileSizeCached fileSizeCached;
	private boolean isSelected = Boolean.FALSE;
	private Color color;
	
	
	
	public FileCacheRow(FileSizeCached fileSizeCached, Color color) {
		super();
		this.fileSizeCached = fileSizeCached;
		this.color = color;
	}

	


	public Color getColor() {
		return color;
	}



	public void setColor(Color color) {
		this.color = color;
	}



	public FileCacheRow(FileSizeCached fileSizeCached) {
		super();
		this.fileSizeCached = fileSizeCached;
	}
	
	

	public FileCacheRow(FileSizeCached fileSizeCached, boolean isSelected) {
		super();
		this.fileSizeCached = fileSizeCached;
		this.isSelected = isSelected;
	}



	public FileSizeCached getFileSizeCached() {
		return fileSizeCached;
	}

	public void setFileSizeCached(FileSizeCached fileSizeCached) {
		this.fileSizeCached = fileSizeCached;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public void toggleSelected() {
		if(this.isSelected == Boolean.TRUE)
			this.isSelected = Boolean.FALSE;
		else
			this.isSelected = Boolean.TRUE;
	}
	
	

}
