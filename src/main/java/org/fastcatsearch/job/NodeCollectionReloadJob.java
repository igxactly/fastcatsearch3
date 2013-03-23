package org.fastcatsearch.job;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentInfo;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.ServiceException;

public class NodeCollectionReloadJob extends StreamableJob {
	long startTime;
	String collectionId;
	int dataSequence;
	int segmentNumber;
	
	public NodeCollectionReloadJob(){ }
	
	public NodeCollectionReloadJob(long startTime, String collectionId, int dataSequence, int segmentNumber){ 
		this.startTime = startTime;
		this.collectionId = collectionId;
		this.dataSequence = dataSequence;
		this.segmentNumber = segmentNumber;
	}
	
	@Override
	public JobResult doRun() throws JobException, ServiceException {
		try{
			CollectionHandler newHandler = new CollectionHandler(collectionId, dataSequence);
			int[] updateAndDeleteSize = newHandler.addSegment(segmentNumber, null);
	//		updateAndDeleteSize[1] += writer.getDuplicateDocCount();//중복문서 삭제카운트
	//		logger.info("== SegmentStatus ==");
	//		newHandler.printSegmentStatus();
			
			newHandler.saveDataSequenceFile();
			
			IRService irService = IRService.getInstance();
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, newHandler);
			if(oldCollectionHandler != null){
				logger.info("## Close Previous Collection Handler");
				oldCollectionHandler.close();
			}
			
			SegmentInfo si = newHandler.getLastSegmentInfo();
			logger.info(si.toString());
			int docSize = si.getDocCount();
			
			/*
			 * indextime 파일 업데이트.
			 */
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String startDt = sdf.format(startTime);
			String endDt = sdf.format(new Date());
			int duration = (int) (System.currentTimeMillis() - startTime);
			String durationStr = Formatter.getFormatTime(duration);
			IRSettings.storeIndextime(collectionId, "FULL", startDt, endDt, durationStr, docSize);
			
			/*
			 * 5초후에 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob(5000));
			return new JobResult(true);
			
		}catch(Exception e){
			throw new JobException(e);
		}
		
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		startTime = input.readLong();
		collectionId = input.readString();
		dataSequence = input.readInt();
		segmentNumber = input.readInt();
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeLong(startTime);
		output.writeString(collectionId);
		output.writeInt(dataSequence);
		output.writeInt(segmentNumber);
	}

}
