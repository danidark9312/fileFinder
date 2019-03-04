package com.danielgutierrez.fileFinder.model;


import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSizeCached implements Comparable<FileSizeCached>,Serializable{
	private static final long serialVersionUID = 1L;
	transient private Path file;
	private long size;

	public FileSizeCached(Path file) {
		super();
		this.file = file;
		try {
			this.size = Files.size(file);
		} catch (IOException e) {
			System.out.printf("space for %s could not be calculated", this.file.toString());
			e.printStackTrace();
		}
	}

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	public double getSizeMB() {
		//DecimalFormat decimalFormat = new DecimalFormat("###.###,###");
		double sizeMB = size/1024.0/1024.0;
		return  (double)Math.round(sizeMB * 100d) / 100d;
	}

	@Override
	public String toString() {
		return "[file=" + file + ", size=" + size + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (size ^ (size >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileSizeCached other = (FileSizeCached) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	@Override
	public int compareTo(FileSizeCached o) {
		return (int) (this.getSize()-o.getSize());
	}

	private void writeObject(java.io.ObjectOutputStream out)
			  throws IOException{
				out.defaultWriteObject();
			    out.writeUTF(getFile().toString());
			  }
	
	 private void readObject(java.io.ObjectInputStream stream)
	            throws IOException, ClassNotFoundException {
	        stream.defaultReadObject();
	        this.file = Paths.get(stream.readUTF());
	    }
	

}
