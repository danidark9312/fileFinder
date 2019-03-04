package com.danielgutierrez.fileFinder.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import com.danielgutierrez.fileFinder.model.FileCacheModel;
import com.danielgutierrez.fileFinder.model.FileCacheRow;
import com.danielgutierrez.fileFinder.model.FileSizeCached;
import com.danielgutierrez.fileFinder.model.Filter;
import com.danielgutierrez.fileFinder.model.ResultsTable;
import com.danielgutierrez.fileFinder.presentation.FileFinderPresentation;
import com.danielgutierrez.fileFinder.util.Logger;
import com.danielgutierrez.fileFinder.workers.FindFilesWorker;



public class MainFrame implements LogWritter{

	/*Constant block*/
	public static final int threadsToUse = 8;
	public static final double mgBytesDifferenceToFindCandidates = 4;
	public static final boolean fullDebug = Boolean.FALSE;
	private static final String DEFAULTDIR = /*System.getProperty("user.home");*/"C:\\Users\\daniel\\Desktop";
//	private static final String DEFAULTDIR = /*System.getProperty("user.home");*/"C:\\Users\\daniel.gutierrez\\Desktop";
	private AtomicLong numberFilesFound = new AtomicLong(0);
	private AtomicLong numberFilesProcessed = new AtomicLong(0);
	private Filter filter = Filter.NONE;
	
	private JFrame frame;	
	private JTextField textFieldRootPath;
	private JTextArea logTextArea;
	private JButton btnProcessRepeatFiles;
	private ResultsTable resultTable;
	private JPanel centerPanel;
	private boolean logsIsShown = Boolean.TRUE;
	private JPanel panelResult;
	private JScrollPane scrollLog;
	private JToggleButton tglbtnToggleTablelogs;
	
	private Logger logger;
	
	private FileFinderPresentation fileFinderPresentation;
	private Map<String, List<FileSizeCached>> listFilesGroupByExt;
	private JButton btnClear;
	private JButton btnSmartSelect;
	private JButton btnDelete;
	private JSeparator separator;
	
	private JPanel pnlFilesRemaining;
	private JLabel lblTotalFiles;
	private JLabel lblRemainingFiles;
	private FindFilesWorker fileWorker;
	private JButton btnStopOperation;
	private JButton button;
	private JRadioButtonMenuItem jRadioFilterNoFilter;
	private JRadioButtonMenuItem jRadioFilterVideo;
	private JRadioButtonMenuItem jRadioFilterImage;
	private JRadioButtonMenuItem jRadioFilterMusic;
	
	public synchronized Map<String, List<FileSizeCached>> getListFilesGroupByExt() {
		return listFilesGroupByExt;
	}

	public synchronized void setListFilesGroupByExt(Map<String, List<FileSizeCached>> listFilesGroupByExt) {
		this.listFilesGroupByExt = listFilesGroupByExt;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame window = new MainFrame();
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainFrame() {
		this.initialize();
		this.initializeLogger();
		this.fileFinderPresentation = new FileFinderPresentation(this);
	}
	private void initializeLogger() {
		this.logger = new Logger(getLogTextArea());
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 576, 491);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		JPanel fileSearchPanel = new JPanel();
		frame.getContentPane().add(fileSearchPanel, BorderLayout.NORTH);
		fileSearchPanel.setLayout(new BoxLayout(fileSearchPanel, BoxLayout.X_AXIS));
		
		textFieldRootPath = new JTextField();
		textFieldRootPath.setText(MainFrame.DEFAULTDIR);
		textFieldRootPath.setColumns(4);
		
		JButton btnSearchAction = new JButton("Search Files");
		JButton btnOpenFileManager = new JButton(UIManager.getIcon("FileView.directoryIcon"));
		btnOpenFileManager.addActionListener((e)->{
			int returnVal  = fc.showOpenDialog(frame);
			 if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            MainFrame.this.getTextFieldRootPath().setText(file.getAbsolutePath());
		            writeLogs("Opening: " + file.getName());
		        } else {
		        	writeLogs("Open command cancelled by user.");
		        }
		});
		
		btnSearchAction.addActionListener((event)->{
			this.searchFilesEvent(textFieldRootPath.getText());
		});
		
		button = new JButton("");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.importData();
			}
		});
		button.setToolTipText("Import Process");
		button.setIcon(new ImageIcon(MainFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/minimize-pressed.gif")));
		fileSearchPanel.add(button);
		
		
		fileSearchPanel.add(btnOpenFileManager);
		fileSearchPanel.add(textFieldRootPath);
		fileSearchPanel.add(btnSearchAction);
		
		btnStopOperation = new JButton("");
		btnStopOperation.setVisible(Boolean.FALSE);
		ImageIcon imageIcon = new ImageIcon(MainFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/error.png"));
		
		Image image = imageIcon.getImage() ;  
		ImageIcon resizeImage = new ImageIcon( image.getScaledInstance( 15, 15,  java.awt.Image.SCALE_SMOOTH ) );
		btnStopOperation.setIcon(resizeImage);
		btnStopOperation.addActionListener(e->this.fileWorker.cancel(Boolean.FALSE));
		
		fileSearchPanel.add(btnStopOperation);
		
		centerPanel = new JPanel();
		frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout(0, 0));
		
		panelResult = new JPanel();
		
		panelResult.setLayout(new BorderLayout(0, 0));
		
		
		panelResult.add(getControlPanel(), BorderLayout.NORTH);
		
		
		resultTable = new ResultsTable(new FileCacheModel());
		resultTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					openFolder(e,resultTable);
				}else {
					checkRow(e,resultTable);
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(resultTable);
		panelResult.add(scrollPane, BorderLayout.CENTER);
		
		
		logTextArea = new JTextArea();
		logTextArea.setBackground(new Color(255, 255, 255));
		logTextArea.setEditable(false);
		
		scrollLog = new JScrollPane ( logTextArea );
		scrollLog.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		
		centerPanel.add(scrollLog, BorderLayout.CENTER);
//		centerPanel.add(panelResult, BorderLayout.CENTER);
		
		pnlFilesRemaining = new JPanel();
		scrollLog.setColumnHeaderView(pnlFilesRemaining);
		
		lblTotalFiles = new JLabel("0");
		lblRemainingFiles = new JLabel("0");
		
		pnlFilesRemaining.add(new JLabel("Total/Processed"));
		pnlFilesRemaining.add(lblTotalFiles);
		pnlFilesRemaining.add(new JLabel("/"));
		pnlFilesRemaining.add(lblRemainingFiles);
		
		JPanel southPanel = new JPanel();
		frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
		
		btnProcessRepeatFiles = new JButton("Find Duplicated Files");
		btnProcessRepeatFiles.setEnabled(false);
		btnProcessRepeatFiles.setHorizontalAlignment(SwingConstants.LEFT);
		southPanel.add(btnProcessRepeatFiles);
		
		tglbtnToggleTablelogs = new JToggleButton("Toggle Table/Logs");
		tglbtnToggleTablelogs.setEnabled(Boolean.FALSE);
		
		southPanel.add(tglbtnToggleTablelogs);
		tglbtnToggleTablelogs.addActionListener(e->MainFrame.this.toggleLogsByTableResult());
		btnProcessRepeatFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					logger.tryToWriteLog("looking for similar files, this may take several minutes");
					findDuplicatedFiles(MainFrame.this.listFilesGroupByExt);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					logger.tryToWriteLog(e1.getMessage());
				}
			}
		});
	}

	protected void openFolder(MouseEvent e, ResultsTable rt) {
		int rowAtPoint = rt.convertRowIndexToModel(rt.rowAtPoint(e.getPoint()));
		FileCacheModel cacheModel = rt.getModel();
		FileCacheRow row = cacheModel.getRow(rowAtPoint);
		try {
			Runtime.getRuntime().exec("explorer.exe /select," + row.getFileSizeCached().getFile());
		} catch (IOException e1) {
			System.out.println("Path could not be located");
			e1.printStackTrace();
		}
	}

	protected void checkRow(MouseEvent e, ResultsTable rt) {
		int rowAtPoint = rt.convertRowIndexToModel(rt.rowAtPoint(e.getPoint()));
		FileCacheModel cacheModel = rt.getModel();
		cacheModel.getRow(rowAtPoint).toggleSelected();
		cacheModel.fireTableDataChanged();

	}

	private JPanel getControlPanel() {
		JPanel controlPane = new JPanel();
		controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.X_AXIS));
		btnClear = new JButton("Clear");
		btnClear.addActionListener(e->getResultTable().getModel().clearSelection());
		
		JMenuBar mainMenu = buildMenu();
		controlPane.add(mainMenu);
		
		
		btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedRows();
			}
		});
		btnSmartSelect = new JButton("Smart Select");
		btnSmartSelect.setToolTipText("Select all the copies and leave just the original");
		btnSmartSelect.addActionListener((e)->{
			MainFrame.this.getResultTable().selectAllRowsButOldest();
		});
		
		separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		controlPane.add(separator);
		
		
		return controlPane;
	}
	
	private JMenuBar buildMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu jMenu = new JMenu("Menu");
		JMenu jMenuFilter = new JMenu("Filter");
		menuBar.add(jMenu);
		JMenuItem optionClearSelection = new JMenuItem("Clear Selection");
		optionClearSelection.addActionListener(e->getResultTable().getModel().clearSelection());
		
		JMenuItem optionSmartSelection = new JMenuItem("Smart Selection");
		optionSmartSelection.addActionListener(e->getResultTable().selectAllRowsButOldest());
		
		JMenuItem optionDelete = new JMenuItem("Delete Selected");
		optionDelete.addActionListener(e->this.deleteSelectedRows());
		
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem jRadioSortBySize = new JRadioButtonMenuItem("Sort by Size");
		jRadioSortBySize.setSelected(true);
		group.add(jRadioSortBySize);
		
		ButtonGroup filterGroup = new ButtonGroup();
		ActionListener c = (e)->filterButtonEvent(e);
		
		jRadioFilterNoFilter = new JRadioButtonMenuItem("No Filter");
		jRadioFilterVideo = new JRadioButtonMenuItem("Videos");
		jRadioFilterImage = new JRadioButtonMenuItem("Images");
		jRadioFilterMusic = new JRadioButtonMenuItem("Music");
		jRadioFilterNoFilter.setSelected(Boolean.TRUE);
		jRadioFilterNoFilter.addActionListener(c);
		jRadioFilterVideo.addActionListener(c);
		jRadioFilterImage.addActionListener(c);
		jRadioFilterMusic.addActionListener(c);
		filterGroup.add(jRadioFilterNoFilter);
		filterGroup.add(jRadioFilterVideo);
		filterGroup.add(jRadioFilterImage);
		filterGroup.add(jRadioFilterMusic);
		jRadioSortBySize.addActionListener(e->sortBySize());
		
		jMenuFilter.add(jRadioFilterNoFilter);
		jMenuFilter.add(jRadioFilterVideo);
		jMenuFilter.add(jRadioFilterImage);
		jMenuFilter.add(jRadioFilterMusic);
		
		JMenuItem optionExport = new JMenuItem("Export");
		optionExport.addActionListener(e->exportData());
		JMenuItem optionImport = new JMenuItem("Import");
		
		
		jMenu.add(optionClearSelection);
		jMenu.add(optionSmartSelection);
		jMenu.add(optionDelete);
		jMenu.addSeparator();
		jMenu.add(jRadioSortBySize);
		jMenu.addSeparator();
		jMenu.add(jMenuFilter);
		jMenu.addSeparator();
		jMenu.add(optionImport);
		jMenu.add(optionExport);
		
		return menuBar;
	}

	private void filterButtonEvent(ActionEvent e) {
		Object source = e.getSource();
		if(source == this.jRadioFilterNoFilter)
			disabledFilter();
		else if(source == this.jRadioFilterImage)
			filterByImage();
		else if(source == this.jRadioFilterMusic)
			filterByMusic();
		else if(source == this.jRadioFilterVideo)
			filterByVideo();
			
	}

	private void filterByVideo() {
		this.filter = Filter.VIDEO;
	}

	private void filterByMusic() {
		this.filter = Filter.MUSIC;		
	}

	private void filterByImage() {
		this.filter = Filter.IMAGE;		
	}

	private void disabledFilter() {
		this.filter = Filter.NONE;
	}

	private void exportData() {
		JFileChooser jFileChooser = new JFileChooser(DEFAULTDIR);
		jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File file = null;
		int returnVal  = jFileChooser.showSaveDialog(frame);
		 if (returnVal == JFileChooser.APPROVE_OPTION) {
	            file = jFileChooser.getSelectedFile();
	            writeLogs("Exporting command: " + file.getName());
	        } else {
	        	writeLogs("Open command cancelled by user.");
	        }
		
		FileCacheModel model = getResultTable().getModel();
		try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(file+".dat"))){
			ous.writeObject(model.getFileCacheList());
			JOptionPane.showMessageDialog(frame, "File save successfully");
			writeLogs("File saved successfully");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		};
	}
	private void importData() {
		JFileChooser jFileChooser = new JFileChooser(DEFAULTDIR);
		jFileChooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Data File (.dat)";
			}
			@Override
			public boolean accept(File f) {
				 if (f.isDirectory()) {
			            return true;
			        } else {
			        	return f.getName().toLowerCase().endsWith(".dat");
			        }
			}
		});
		
		File file = null;
		int returnVal  = jFileChooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = jFileChooser.getSelectedFile();
			
			FileCacheModel model = getResultTable().getModel();
			try (ObjectInputStream ius = new ObjectInputStream(new FileInputStream(file))){
				List<List<FileCacheRow>> files = (List<List<FileCacheRow>>) ius.readObject();
				
				resultTable.getModel().setFileCacheList(files);
				writeLogs("File loaded sucessfully");
				btnProcessRepeatFiles.setEnabled(Boolean.TRUE);
				MainFrame.this.getTglbtnToggleTablelogs().setEnabled(Boolean.TRUE);
				showResultTab();	
			} catch (IOException|ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			writeLogs("Importing command: " + file.getName());
		} else {
			writeLogs("Open command cancelled by user.");
		}
		
		
	}

	protected void deleteSelectedRows() {
		int showConfirmDialog = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete these files, operation can not be undone");
		if(showConfirmDialog == 0) {
			ResultsTable rt = getResultTable();
			rt.getModel().deleteSelected();
			JOptionPane.showMessageDialog(frame, "The files have been deleted");
		}
	}

	private void sortBySize() {
		getResultTable().getModel().sortBySize();
	}
	
	public void updateTotalFilesLabel() {
		SwingUtilities.invokeLater(()->{
			MainFrame.this.getLblTotalFiles().setText(""+MainFrame.this.numberFilesFound.get());
			MainFrame.this.getLblRemainingFiles().setText("0");
		});
	}
	
	public void sumFilesProcessed(int Qty) {
		long incrementAndGet = MainFrame.this.numberFilesProcessed.updateAndGet((value)->value+Qty);
		SwingUtilities.invokeLater(()->{
			MainFrame.this.getLblRemainingFiles().setText(""+incrementAndGet);
		});
	}

	private void searchFilesEvent(String rootPath) {
		if(!Files.exists(Paths.get(rootPath))){
			this.writeLogsf("Path not found %s",rootPath);
			return;
		}
		writeLogs("Initializing searching action");
		showLogTab();
		final Instant beforeStart = Instant.now();
			new FilesSearcherWorker(this, new Function<String, Map<String,List<FileSizeCached>>>() {
				@Override
				public Map<String, List<FileSizeCached>> apply(String t) {
					Map<String, List<FileSizeCached>> searchFiles = null;
					try {
						searchFiles = MainFrame.this.fileFinderPresentation.searchFiles(t, getFilter());
						MainFrame.this.numberFilesFound.set(searchFiles
								.entrySet()
								.stream()
								.flatMap((x)->x.getValue().stream())
								.count());
						MainFrame.this.updateTotalFilesLabel();
						MainFrame.this.writeLogsf("%d files were found",MainFrame.this.numberFilesFound.get());
						MainFrame.this.writeLogsf("Process performed in %f seconds",(ChronoUnit.MILLIS.between(beforeStart, Instant.now()))/1000.0);
					} catch (InterruptedException e) {
						e.printStackTrace();
						MainFrame.this.writeLogs(e.getMessage());
					}
					return searchFiles;
				}
			}, rootPath).execute();
		
		
	}
	
	protected String getFilter() {
		return filter.getRegex();
	}

	protected void findDuplicatedFiles(Map<String, List<FileSizeCached>> listFilesGroupByExt)throws InterruptedException {
		getResultTable().clearData();
		getBtnStopOperation().setVisible(Boolean.TRUE);
		showLogTab();
		this.numberFilesProcessed.set(0);
		fileWorker = new FindFilesWorker(()->{
			try {
				this.fileFinderPresentation.findDuplicatedFiles(listFilesGroupByExt, (list) -> {
					if (list != null && list.size() > 0) {
						loadResultsInTable(list);
						sortBySize();
						showResultTab();
						MainFrame.this.getTglbtnToggleTablelogs().setEnabled(Boolean.TRUE);
						MainFrame.this.writeLogsf("Files repeated found:\n %s",
								list
								.stream()
								.map(l->l.stream().map(FileSizeCached::toString)
										.reduce((a,b)->a.concat("\n".concat(b))).get())
								.reduce((a, b) -> a.concat(b).concat("\n\n"))
								.get()
								.concat("\n"));
						
					}
					getBtnStopOperation().setVisible(Boolean.FALSE);
					showResultTab();
				}, this.fileWorker);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		fileWorker.execute();

	}

	
	
	private void loadResultsInTable(Collection<Collection<FileSizeCached>> list) {
		getResultTable().loadData(list);
		
	}

	public void writeLogs(String... logs) {
		logger.tryToWriteLog(logs);
	}
	public void writeLogsf(String logs, Object... param) {
		logger.tryToWriteLogf(logs,param);
	}
	
	private void writeFileInLogger(Map<String, List<Path>> filesListByExt) {
		for(Map.Entry<String, List<Path>> entry : filesListByExt.entrySet()) {
			entry.getValue().stream().map(Path::toString).forEach(logger::tryToWriteLog);
		}
	}

	public JTextField getTextFieldRootPath() {
		return textFieldRootPath;
	}
	public JTextArea getLogTextArea() {
		return logTextArea;
	}
	public JButton getBtnProcessRepeatFiles() {
		return btnProcessRepeatFiles;
	}
	
	private class FilesSearcherWorker extends SwingWorker<Map<String, List<FileSizeCached>>, Void>{
		private MainFrame mainFrame;
		private Function<String,Map<String, List<FileSizeCached>>> functionToExcecute;
		private String rootPath;
		
		public FilesSearcherWorker(MainFrame mainFrame, Function<String, Map<String, List<FileSizeCached>>> functionToExcecute,
				String rootPath) {
			super();
			this.mainFrame = mainFrame;
			this.functionToExcecute = functionToExcecute;
			this.rootPath = rootPath;
		}


		@Override
		protected Map<String, List<FileSizeCached>> doInBackground() throws Exception {
			return this.functionToExcecute.apply(this.rootPath);
		}
		
		@Override
		protected void done() {
			try {
				Map<String, List<FileSizeCached>> fileList = get();
				if(fileList!=null && fileList.size()>0) {
					this.mainFrame.btnProcessRepeatFiles.setEnabled(Boolean.TRUE);
					this.mainFrame.setListFilesGroupByExt(fileList);
					if(MainFrame.fullDebug)
						this.mainFrame.getListOfTotalFiles()
						.stream()
						.map(x->x.getFile().toString()).forEach((str)->System.out.println(str));
				
					this.mainFrame.writeLogs("Searching action finalized");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				if(e.getCause() !=null 
						&& e.getCause().getCause()!=null 
						&& e.getCause().getCause() instanceof AccessDeniedException) {
					this.mainFrame.writeLogs("Could not write due permission","AccessDeniedException "+e.getCause().getCause().getMessage());
					e.getCause().getCause().printStackTrace();
				}else {
					e.printStackTrace();
				}
			}
			super.done();
		}
		
	}
	
	private List<FileSizeCached> getListOfTotalFiles() {
		return this.listFilesGroupByExt
				.entrySet()
				.stream()
				.flatMap(x->x.getValue().stream())
				.collect(Collectors.toList());
	}
	public JPanel getCenterPanel() {
		return centerPanel;
	}
	
	public JPanel getPanelResult() {
		return panelResult;
	}
	public JScrollPane getScrollLog() {
		return scrollLog;
	}
	
	private void toggleLogsByTableResult() {
		if(this.logsIsShown) {
			showResultTab();
		}else {
			showLogTab();
		}
	}
	
	private void showLogTab() {
		getCenterPanel().remove(getPanelResult());
		getCenterPanel().add(getScrollLog(),BorderLayout.CENTER);
		getCenterPanel().revalidate();
		getCenterPanel().repaint();
		this.logsIsShown = true;
	}

	private void showResultTab() {
		getCenterPanel().remove(getScrollLog());
		getCenterPanel().add(getPanelResult(),BorderLayout.CENTER);
		getCenterPanel().revalidate();
		getCenterPanel().repaint();
		this.logsIsShown = false;
	}

	public JToggleButton getTglbtnToggleTablelogs() {
		return tglbtnToggleTablelogs;
	}
	
	public ResultsTable getResultTable() {
		return this.resultTable;
	}
	public JLabel getLblTotalFiles() {
		return lblTotalFiles;
	}
	public JLabel getLblRemainingFiles() {
		return lblRemainingFiles;
	}
	public JButton getBtnStopOperation() {
		return btnStopOperation;
	}
}
