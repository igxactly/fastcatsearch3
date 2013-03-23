package org.fastcatsearch.job.action;

import java.sql.Timestamp;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.object.IndexingResult;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.NodeFullIndexJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceException;

public class FullIndexRequest extends Job {

	@Override
	public JobResult doRun() throws JobException, ServiceException {
		
		String[] args = getStringArrayArgs();
		String collectionId = (String)args[0];
		DataStrategy dataStrategy = DataService.getInstance().getCollectionDataStrategy(collectionId);
		List<Node> nodeList = dataStrategy.indexNodes();
		if(nodeList == null || nodeList.size() == 0){
			throw new JobException("색인할 노드가 정의되어있지 않습니다.");
		}
		
		String indexingType = "F";
		//전체색인시는 증분색인 정보를 클리어해준다.
		DBService.getInstance().IndexingResult.delete(collectionId, "I");
		DBService.getInstance().IndexingResult.update(collectionId, indexingType, IndexingResult.STATUS_RUNNING, -1, -1, -1, isScheduled(), new Timestamp(System.currentTimeMillis()), null, 0);
		
		//
		//TODO 어느 노드로 색인할지 고른다.
		//현 버전에서는 일단 첫번째 노드로 색인.
		
		//선택된 노드로 색인 메시지를 전송한다.
		Node node =  nodeList.get(0);
		long st = System.currentTimeMillis();
		NodeFullIndexJob job = new NodeFullIndexJob(collectionId);
		ResultFuture resultFuture = NodeService.getInstance().sendRequest(node, job);
		long et = System.currentTimeMillis();
		Object result = resultFuture.take();
		if(!resultFuture.isSuccess()){
			
			DBService.getInstance().IndexingResult.updateOrInsert(collectionId, indexingType, IndexingResult.STATUS_FAIL, 0, 0, 0, isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
			DBService.getInstance().IndexingHistory.insert(collectionId, indexingType, false, 0, 0, 0, isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
			
			if(result instanceof Throwable){
				throw new JobException("색인노드에서 전체색인중 에러발생.", (Throwable) result);
			}else{
				throw new JobException("색인노드에서 전체색인중 에러발생.");
			}
		}
		
		//DBService를 통해 색인결과를 입력한다.
		//FIXME 차후 결과입력 로직은 해당 DAO가 가지고 있도록 한다.
		IndexingJobResult indexingJobResult = (IndexingJobResult) result;
		DBService.getInstance().IndexingResult.updateOrInsert(collectionId, indexingType, IndexingResult.STATUS_SUCCESS, indexingJobResult.docSize, indexingJobResult.updateSize, indexingJobResult.deleteSize, isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
		DBService.getInstance().IndexingHistory.insert(collectionId, indexingType, true, indexingJobResult.docSize, indexingJobResult.updateSize, indexingJobResult.deleteSize, isScheduled(), new Timestamp(st), new Timestamp(et), (int)(et-st));
		
		
		return new JobResult(true);
	}

}
