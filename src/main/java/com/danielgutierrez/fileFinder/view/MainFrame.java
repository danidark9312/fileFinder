package com.danielgutierrez.fileFinder.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import com.danielgutierrez.fileFinder.presentation.FileFinderPresentation;
import com.danielgutierrez.fileFinder.util.Logger;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;



public class MainFrame implements LogWritter{

	/*Constant block*/
	public static final int threadsToUse = 8;
	public static final int mgBytesDifferenceToFindCandidates = 4;
	public static final boolean fullDebug = Boolean.TRUE;
	
	private JFrame frame;	
	private JTextField textFieldRootPath;
	private Logger logger;
	
	private FileFinderPresentation fileFinderPresentation = new FileFinderPresentation();
	private Map<String, List<Path>> listFilesGroupByExt;
	private JTextArea logTextArea;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame window = new MainFrame();
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
		textFieldRootPath.setText("C:\\Users\\daniel.gutierrez\\Desktop");
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
			MainFrame.this.listFilesGroupByExt = this.searchFilesEvent(textFieldRootPath.getText());
		});
		fileSearchPanel.add(btnOpenFileManager);
		fileSearchPanel.add(textFieldRootPath);
		fileSearchPanel.add(btnSearchAction);
		
		JPanel cennterPanel = new JPanel();
		frame.getContentPane().add(cennterPanel, BorderLayout.CENTER);
		cennterPanel.setLayout(new BorderLayout(0, 0));
		
		logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		
		JScrollPane scroll = new JScrollPane ( logTextArea );
		scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		
		cennterPanel.add(scroll,BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
		
		JButton btnProcessRepeatFiles = new JButton("Find Duplicated Files");
		btnProcessRepeatFiles.setHorizontalAlignment(SwingConstants.LEFT);
		southPanel.add(btnProcessRepeatFiles);
		btnProcessRepeatFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					findDuplicatedFiles(MainFrame.this.listFilesGroupByExt);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					logger.tryToWriteLog(e1.getMessage());
				}
			}
		});
	}

	protected void findDuplicatedFiles(Map<String, List<Path>> listFilesGroupByExt) throws InterruptedException {
		this.fileFinderPresentation.findDuplicatedFiles(listFilesGroupByExt, 
			(list)->{
				if(list != null && list.size()>0) {
					MainFrame.this.writeLogsf("Files repeated found for %s",list);
				}else {
					MainFrame.this.writeLogs("No files repeated found");
				}
			}
		);
		
	}

	private Map<String, List<Path>> searchFilesEvent(String rootPath) {
		writeLogs("Initializing searching action");
		Map<String, List<Path>> filesListByExt = null;
		try {
			filesListByExt = fileFinderPresentation.searchFiles(rootPath);
			writeLogsf("%d files were found in the path %s",filesListByExt.size(),rootPath);			
			if(fullDebug)
				writeFileInLogger(filesListByExt);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		writeLogs("Finalizing searching action");
		return filesListByExt;
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
}
