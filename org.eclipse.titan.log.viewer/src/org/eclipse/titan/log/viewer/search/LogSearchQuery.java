/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.search;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.readers.SequentialLogFileReader;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;


public class LogSearchQuery implements ISearchQuery {

	private final List<IFile> files;
	private final SearchPattern pattern;
	private final LogSearchResult result;

	public LogSearchQuery(final List<IFile> files, final SearchPattern pattern) {
		this.files = files;
		this.pattern = pattern;
		result = new LogSearchResult(this);
	}

	@Override
	public IStatus run(final IProgressMonitor monitor) {
		result.removeAll();

		int numOfRecords = 0;
		for (final IFile logFile : files) {
			final File indexFile = LogFileCacheHandler.getLogRecordIndexFileForLogFile(logFile);
			numOfRecords += LogFileCacheHandler.getNumberOfLogRecordIndexes(indexFile);
		}

		monitor.beginTask("Searching", numOfRecords);

		for (final IFile logFile : files) {
			if (monitor.isCanceled()) {
				break;
			}

			if (LogFileCacheHandler.hasLogFileChanged(logFile)
					&& !LogFileCacheHandler.processLogFile(logFile, new NullProgressMonitor(), true)) {
				continue;
			}

			final File indexFile = LogFileCacheHandler.getLogRecordIndexFileForLogFile(logFile);
			try {
				final LogRecordIndex[] indexes = LogFileCacheHandler.readLogRecordIndexFile(indexFile, 0, LogFileCacheHandler.getNumberOfLogRecordIndexes(indexFile));
				final SequentialLogFileReader reader = new SequentialLogFileReader(logFile.getLocationURI(), indexes);

				monitor.subTask("Filtering");
				filterRecords(monitor, logFile, reader);
				monitor.done();

			} catch (ParseException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(
						new TechnicalException(logFile.getName() + ": Could not parse the log file. Reason: " + e.getMessage()));
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(
						new TechnicalException(logFile.getName() + ": Error while searching. Reason: " + e.getMessage()));
			}
		}
		monitor.done();
		return new Status(IStatus.OK, Constants.PLUGIN_ID, IStatus.OK, "Search done.", null);
	}

	private void filterRecords(final IProgressMonitor monitor, final IFile logFile, final SequentialLogFileReader reader) throws ParseException, IOException {
		for (LogRecord record = reader.getNext(); reader.hasNext(); record = reader.getNext()) {
			if (pattern.match(record)) {
				result.addMatch(new Match(logFile, Match.UNIT_LINE, record.getRecordNumber(), 1));
			}
			if (monitor.isCanceled()) {
				break;
			}
			monitor.worked(1);
		}
	}

	@Override
	public String getLabel() {
		return "TITAN Log Search";
	}

	public SearchPattern getPattern() {
		return pattern;
	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public ISearchResult getSearchResult() {
		return result;
	}
}
