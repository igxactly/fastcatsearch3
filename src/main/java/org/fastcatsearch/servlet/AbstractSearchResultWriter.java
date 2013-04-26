package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.Writer;

import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 검색결과를 response stream에 기록하는 클래스.
 * */
public abstract class AbstractSearchResultWriter {
	protected static Logger logger = LoggerFactory.getLogger(AbstractSearchResultWriter.class);
	protected Writer writer;
	
	public AbstractSearchResultWriter(Writer writer){
		this.writer = writer;
	}
	public abstract void writeResult(Object obj, ResultStringer rStringer, long searchTime, boolean isSuccess) throws StringifyException, IOException;
}
