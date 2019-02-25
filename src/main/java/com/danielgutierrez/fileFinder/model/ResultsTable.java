package com.danielgutierrez.fileFinder.model;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class ResultsTable extends JTable {
	
	private static Color selectedRow = new Color(247,41,41);
	
	
	List<Color> list = new ArrayList<>(Arrays.asList(
			new Color(216, 191, 216),
			new Color(173, 216, 230),
			Color.LIGHT_GRAY,
			new Color(245, 216, 173)
			));
	Iterator<Color> colorIterator = list.iterator();
	
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
	 
	 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ResultsTable(FileCacheModel fileCacheModel) {
		super(fileCacheModel);
		String[] colHeadings = {"Path","Size(MB)","Select"};
		int numRows = 0 ;
		FileCacheModel model = new FileCacheModel() ;
//		TableRowSorter trs = new TableRowSorter(model);
		setModel(model);
//		trs.setComparator(1, new Comparator<Long>() {
//			@Override
//			public int compare(Long o1, Long o2) {
//				return o1.compareTo(o2);
//			}
//			
//			@Override
//			public boolean equals(Object obj) {
//				return this.equals(obj);
//			}
//		});
//		setRowSorter(trs);
//		setAutoCreateRowSorter(Boolean.FALSE);
		
		TableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer()
		{
		    @Override
		    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		    	int modelIndex = table.convertRowIndexToModel(row);
		        final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		        FileCacheModel rdm = (FileCacheModel) table.getModel();
		        FileCacheRow fileCache = rdm.getRow(modelIndex);
		        if(fileCache.isSelected()) {
		        	c.setBackground(selectedRow);
		        	c.setForeground(Color.white);
		        }else {
		        	c.setBackground(fileCache.getColor());
		        	c.setForeground(Color.black);
		        }
		        	
		        return c;
		    }
		};
		
		setDefaultRenderer(Long.class, defaultTableCellRenderer);
		setDefaultRenderer(String.class, defaultTableCellRenderer);
		setDefaultRenderer(Boolean.class, new BooleanRenderer());
		
		
		TableColumn column = null;
	    for (int i = 0; i < 3; i++) {
	        column = getColumnModel().getColumn(i);
	        if (i == 0) {
	            column.setPreferredWidth(100); //sport column is bigger
	        } else if(i == 1){
	            column.setMaxWidth(80);
	        }else if(i == 2) {
	        	column.setMaxWidth(80);
	        }
	    }    
	}
	
	public void clearData() {
		getModel().clear();	
	}
	 
	

	public void loadData(Collection<Collection<FileSizeCached>> list) {
		list.stream().forEach(this::addGroupRow);
		/*list.stream().flatMap(Collection::stream)
		.map(FileCacheRow::new)
		.forEach(row->ResultsTable.this.getModel().addRow(row));*/
	}
	
	/*private void addRow(FileCacheRow row) {
		FileCacheModel model = (FileCacheModel) ResultsTable.this.getModel();
		model.addRow(row);
	}*/
	
	private void addGroupRow(Collection<FileSizeCached> rowGroup) {
		Color color = getNextAvailableColor();
		List<FileCacheRow> fileRow = rowGroup.stream().map(file->new FileCacheRow(file, color)).collect(Collectors.toList());
		ResultsTable.this.getModel().addGroup(fileRow);
		//rowGroup.forEach(file->addRow(new FileCacheRow(file, color)));
	}
	
	
	private synchronized Color getNextAvailableColor() {
		Color color = null;
		if((colorIterator.hasNext())) {
			color = colorIterator.next();
		}else {
			colorIterator = list.iterator();
			color = getNextAvailableColor();
		}
		return color;
		
	}

	@Override
	public FileCacheModel getModel() {
		return (FileCacheModel) super.getModel();
	}
	
	/*private FileCacheRow getFileSizeCachedAsRow(FileSizeCached fileSizeCached) {
		return new Object[] {fileSizeCached.getFile().toString(),fileSizeCached.getSizeMB(),Boolean.FALSE};
	}*/
	
	
	public void selectAllRowsButOldest() {
		getModel().selectAllRowsButOldest();
		
	}
	
	public static class BooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource {

        public BooleanRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            //setBorderPainted(true);
            //setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
        	int modelIndex = table.convertRowIndexToModel(row);
	        FileCacheModel rdm = (FileCacheModel) table.getModel();
	        FileCacheRow fileCache = rdm.getRow(modelIndex);
	        if(fileCache.isSelected()) {
	        	setBackground(selectedRow);
	        	setForeground(Color.white);
	        	setSelected(Boolean.TRUE);
	        }else {
	        	setBackground(fileCache.getColor());
	        	setForeground(Color.black);
	        	setSelected(Boolean.FALSE);
	        }
	        return this;
        }
    }
	
 }



