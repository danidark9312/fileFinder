package com.danielgutierrez.fileFinder.model;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

public class FileCacheModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {"Path","Size(MB)","Select"};
	private List<List<FileCacheRow>> fileCacheList;

	public FileCacheRow getRow(int row) {
		return getAsFlat().get(row);
	}
	
	public List<FileCacheRow> getAsFlat() {
		return this.fileCacheList.stream().flatMap(List::stream).collect(Collectors.toList());
	}
	
	public FileCacheModel() {
		super();
	}


	public FileCacheModel(List<List<FileCacheRow>> bkList) {
		fileCacheList = bkList;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		int size;
		if (fileCacheList == null) {
			size = 0;
		} else {
			size = fileCacheList.size();
		}
		return size;
	}

	public Object getValueAt(int row, int col) {
		Object temp = null;
		FileCacheRow fileCacheRow = getAsFlat().get(row);
		FileSizeCached fileSizeCached = fileCacheRow.getFileSizeCached();
		if (col == 0) {
			temp = fileSizeCached.getFile().toString();
		} else if (col == 1) {
			temp = fileSizeCached.getSize();
		} else if (col == 2) {
			temp = fileCacheRow.isSelected();
		}
		return temp;
	}
	
	public void addGroup(List<FileCacheRow> group) {
		if(this.fileCacheList == null)
			this.fileCacheList = new ArrayList<>();
		this.fileCacheList.add(group);
	}
	
	

	public List<List<FileCacheRow>> getFileCacheList() {
		return fileCacheList;
	}

	public void setFileCacheList(List<List<FileCacheRow>> fileCacheList) {
		this.fileCacheList = fileCacheList;
		fireTableDataChanged();
	}

	// needed to show column names in JTable
	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	public void selectAllRowsButOldest() {
		this.fileCacheList.stream().forEach(row->{
			FileCacheRow fileCacheRow = row.stream().max(new Comparator<FileCacheRow>() {
				@Override
				public int compare(FileCacheRow o1, FileCacheRow o2) {
					int compareTo = 0;
					try {
						BasicFileAttributes file1CreatedDate = Files.readAttributes(o1.getFileSizeCached().getFile(), BasicFileAttributes.class);
						BasicFileAttributes file2CreatedDate = Files.readAttributes(o2.getFileSizeCached().getFile(), BasicFileAttributes.class);
						compareTo = file2CreatedDate.creationTime().compareTo(file1CreatedDate.creationTime());
					} catch (IOException e) {
						e.printStackTrace();
					}
					return compareTo;
				}
	
			}).get();
			row.stream().forEach(file->{
				if(!file.equals(fileCacheRow))
					file.setSelected(Boolean.TRUE);
					
			});
		});
		this.fireTableDataChanged();
	}

	@Override
	public Class getColumnClass(int column) {
		switch (column) {
		case 0:
			return String.class;
		case 1:
			return Long.class;
		case 2:
			return Boolean.class;
		}
		return getValueAt(0, column).getClass();
	}

	public void clearSelection() {
		getAsFlat().forEach(file->file.setSelected(Boolean.FALSE));
		fireTableDataChanged();
		
	}

	public void clear() {
		if(this.fileCacheList!=null) {
			this.fileCacheList.clear();
			fireTableDataChanged();
		}
		
	}

	public void deleteSelected() {
		getAsFlat().forEach(file->{
			if(file.isSelected()) {
				try {
					if(Files.deleteIfExists(file.getFileSizeCached().getFile())) {
						System.out.println(file.getFileSizeCached()+": Deleted");
						removeFileFromModel(file);
						fireTableDataChanged();	
					}
					} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	private void removeFileFromModel(FileCacheRow file) {
		for(List<FileCacheRow> group : this.fileCacheList) {
			for(int i = 0; i < group.size();i++) {
				if(group.get(i) == file) {
					group.remove(i);
					if(group.size()==1)
						group.get(0).setColor(Color.YELLOW);
					break;
					
				}
			}
		}
	}
	
	public void sortBySize() {
		this.fileCacheList.sort(new Comparator<List<FileCacheRow>>() {
			@Override
			public int compare(List<FileCacheRow> o1, List<FileCacheRow> o2) {
				return (int) (o2.stream()
						.mapToLong(file->file.getFileSizeCached().getSize())
						.sum()
						-o1.stream()
						.mapToLong(file->file.getFileSizeCached().getSize())
						.sum());
			}
		});
		fireTableDataChanged();
	}
	
	
}







