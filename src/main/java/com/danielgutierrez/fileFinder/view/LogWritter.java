package com.danielgutierrez.fileFinder.view;

public interface LogWritter {

	public void writeLogs(String... logs);
	public void writeLogsf(String logs, Object... param);
}
