package com.danielgutierrez.fileFinder.view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JTextField;

import com.danielgutierrez.fileFinder.presentation.FileFinderPresentation;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.awt.event.ActionEvent;

public class MainFrame {

	private JFrame frame;	
	private JTextField textFieldRootPath;
	
	FileFinderPresentation fileFinderPresentation = new FileFinderPresentation(); 

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
		initialize();
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
			fileFinderPresentation.searchFiles(textFieldRootPath.getText(), (pathsmaps)->{
				pathsmaps
				.entrySet()
				.stream()
				.forEach(entry->System.out.println("["+entry.getValue().size()+"]["+entry.getKey()+"]"
						+"\n "+entry.getValue().stream()
						.map(Path::getFileName)
						.map(Path::toString)
						.reduce((a,b)->a.concat("\n ").concat(b)).get()));
			});
		});
		panel.add(btnSearchFiles);
	}

	public JTextField getTextFieldRootPath() {
		return textFieldRootPath;
	}
}
