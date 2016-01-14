package org.fastcatsearch.job.indexing;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.datasource.reader.DefaultDataSourceReader;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.action.service.indexing.IndexDocumentsAction;
import org.fastcatsearch.http.action.service.indexing.JSONRequestReader;
import org.fastcatsearch.http.action.service.indexing.MapDocument;
import org.fastcatsearch.ir.CollectionAddIndexer;
import org.fastcatsearch.ir.CollectionDynamicIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by swsong on 2016. 1. 14..
 */
public class NodeIndexFileDocumentJob extends Job implements Streamable {

    private String collectionId;
    private String documents;

    public NodeIndexFileDocumentJob() {
    }

    public NodeIndexFileDocumentJob(String collectionId, String documents) {
        this.collectionId = collectionId;
        this.documents = documents;
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {
        try {
            IRService irService = ServiceManager.getInstance().getService(IRService.class);
            CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
            DefaultDataSourceReader sourceReader = new DefaultDataSourceReader(collectionHandler.schema().schemaSetting());
            CollectionDynamicIndexer indexer = null;
            try {
                indexer = new CollectionDynamicIndexer(collectionHandler);

                List<MapDocument> jsonList = new JSONRequestReader().readMapDocuments(documents);
                for (MapDocument doc : jsonList) {
                    String type = String.valueOf(doc.getType());
                    Map<String, Object> sourceMap = doc.getsourceMap();
                    Document document = sourceReader.createDocument(sourceMap);

                    if (type.equals(IndexDocumentsAction.INSERT_TYPE)) {
                        indexer.addDocument(document);
                    } else if (type.equals(IndexDocumentsAction.UPDATE_TYPE)) {
                        indexer.updateDocument(document);
                    } else if (type.equals(IndexDocumentsAction.DELETE_TYPE)) {
                        indexer.deleteDocument(document);
                    }
                }
            } finally {
                if (indexer != null) {
                    indexer.close();
                }
            }
        } catch (Exception e) {
            logger.error("node dynamic index error!", e);
        }

        return new JobResult();
    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();
        documents = input.readString();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeString(collectionId);
        output.writeString(documents);
    }
}
