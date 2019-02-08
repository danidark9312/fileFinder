package com.danielgutierrez.fileFinder.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.danielgutierrez.fileFinder.presentation.FileFinderPresentation;
import com.danielgutierrez.fileFinder.util.Logger;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;

public class MainFrame {

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
		frame.setBounds(100, 100, 572, 492);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		textFieldRootPath = new JTextField();
		panel.add(textFieldRootPath);
		textFieldRootPath.setText("C:\\Users\\daniel.gutierrez\\Downloads");
		textFieldRootPath.setColumns(10);
		
		JButton btnSearchFiles = new JButton("Search Files");
		btnSearchFiles.addActionListener((event)->{
			MainFrame.this.listFilesGroupByExt = this.searchFilesEvent(textFieldRootPath.getText());
		});
		panel.add(btnSearchFiles);
		
		JPanel cennterPanel = new JPanel();
		frame.getContentPane().add(cennterPanel, BorderLayout.CENTER);
		cennterPanel.setLayout(new BorderLayout(0, 0));
		
		logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		
		JScrollPane scroll = new JScrollPane ( logTextArea );
		scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		
		cennterPanel.add(scroll,BorderLayout.CENTER);
		
		
		
	}

	private Map<String, List<Path>> searchFilesEvent(String rootPath) {
		logger.tryToWriteLog("Initializing searching action");
		Map<String, List<Path>> filesListByExt = null;
		try {
			filesListByExt = fileFinderPresentation.searchFiles(rootPath);
			writeFileInLogger(filesListByExt);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.tryToWriteLog("Finalizing searching action");
		return filesListByExt;
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
