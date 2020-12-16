package alluxio.worker.grpc;

import alluxio.network.protocol.databuffer.DataBuffer;
import alluxio.network.protocol.databuffer.NioDataBuffer;
import alluxio.security.authentication.AuthenticatedUserInfo;
import alluxio.worker.WorkerProcess;
import alluxio.worker.block.BlockWorker;
import org.apache.ratis.proto.RaftProtos;
import org.apache.ratis.protocol.RaftClientRequest;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.statemachine.impl.SimpleStateMachineStorage;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class BlockStoreStateMachine extends BaseStateMachine {
  private final SimpleStateMachineStorage storage = new SimpleStateMachineStorage();
  private final AuthenticatedUserInfo mUserInfo;
  private final WorkerProcess mWorkerProcess;

  BlockStoreStateMachine(WorkerProcess workerProcess, AuthenticatedUserInfo userInfo) {
    this.mUserInfo = userInfo;
    this.mWorkerProcess = workerProcess;
  }

  @Override
  public TransactionContext startTransaction(RaftClientRequest request) throws IOException {
    return TransactionContext.newBuilder()
        .setStateMachine(this)
        .setClientRequest(request)
        .setStateMachineData(request.getMessage().getContent())
        .build();
  }

  @Override
  public CompletableFuture<Long> write(RaftProtos.LogEntryProto entry) {
    // 问题：此处共享writeRequest的逻辑是错误的。因为block有四个副本，每个raft member使用相同的blockId
    DataBuffer buffer = null;
    try {
      // 取出状态机数据
      final RaftProtos.StateMachineLogEntryProto smLog = entry.getStateMachineLogEntry();
      final ByteString raftData = smLog.getLogData();
      // 解析writeRequest
      alluxio.grpc.WriteRequest writeRequest = alluxio.grpc.WriteRequest.parseFrom(raftData.toByteArray());
      // 使用BlockWriteHandler实现写入逻辑
      BlockWriteHandler writeHandler = new BlockWriteHandler(mWorkerProcess.getWorker(BlockWorker.class),
          null, mUserInfo, true);
      BlockWriteRequestContext context = writeHandler.createRequestContext(writeRequest);
      // 写入的数据内容从writeRequest中取出
      com.google.protobuf.ByteString data = writeRequest.getChunk().getData();
      buffer = new NioDataBuffer(data.asReadOnlyByteBuffer(), data.size());
      writeHandler.writeBuf(context, null, buffer, context.getPos());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CompletableFuture.completedFuture(buffer == null ? 0 : buffer.getLength());
  }
}
