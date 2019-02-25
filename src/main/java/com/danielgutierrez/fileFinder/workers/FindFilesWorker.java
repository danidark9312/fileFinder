package com.danielgutierrez.fileFinder.workers;

import javax.swing.SwingWorker;

public class FindFilesWorker extends SwingWorker<Void, Void>{

	Runnable taskToDo;
	
	
	
	public FindFilesWorker(Runnable taskToDo) {
		super();
		this.taskToDo = taskToDo;
	}

	@Override
	protected Void doInBackground() throws Exception {
		taskToDo.run();
		return null;
	}
	
	@Override
	protected void done() {
		super.done();
	}

}
