package org.fastcatsearch.plugin.analysis.basic;

import java.io.File;
import java.util.Map;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.fastcatsearch.ir.analysis.DefaultAnalyzerFactory;
import org.fastcatsearch.ir.analysis.PrimaryWordAnalyzer;
import org.fastcatsearch.ir.dic.Dictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;
import org.fastcatsearch.plugin.analysis.AnalyzerInfo;


public class BasicAnalysisPlugin extends AnalysisPlugin<CharVector, PreResult<CharVector>> {

	public BasicAnalysisPlugin(File pluginDir, PluginSetting pluginSetting) {
		super(pluginDir, pluginSetting);
	}

	@Override
	protected void loadAnalyzerFactory(Map<String, AnalyzerInfo> analyzerFactoryMap) {
		//extract entire word 
		analyzerFactoryMap.put("keyword", new AnalyzerInfo("Keyword Analyzer", new DefaultAnalyzerFactory(KeywordAnalyzer.class)));
		//lucene StandardAnalyzer
		analyzerFactoryMap.put("standard", new AnalyzerInfo("Standard Analyzer", new DefaultAnalyzerFactory(StandardAnalyzer.class)));
		
		analyzerFactoryMap.put("primary", new AnalyzerInfo("Primary Word Analyzer", new DefaultAnalyzerFactory(PrimaryWordAnalyzer.class)));
		
		analyzerFactoryMap.put("whitespace", new AnalyzerInfo("Whitespace Analyzer", new DefaultAnalyzerFactory(WhitespaceAnalyzer.class)));
	}

	@Override
	protected Dictionary<CharVector, PreResult<CharVector>> loadSystemDictionary(DictionarySetting dictionarySetting) {
		return null;
	}

}