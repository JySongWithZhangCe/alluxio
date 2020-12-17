package alluxio.client.file;

import alluxio.AlluxioURI;
import alluxio.client.file.options.OutStreamOptions;
import alluxio.util.CommonUtils;
import com.google.common.base.Preconditions;
import com.google.common.io.Closer;
import com.google.protobuf.ByteString;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.protocol.Message;

import java.io.IOException;

public class AlluxioFileRaftOutStream extends FileOutStream {

  /** Used to manage closeable resources. */
  private final Closer mCloser;
  private final long mBlockSize;
  private final FileSystemContext mContext;
  private final OutStreamOptions mOptions;
  private final AlluxioClientPipelineSelector mClientSelector;

  protected final AlluxioURI mUri;

  /**
   * Creates a new file output stream.
   *
   * @param path the file path
   * @param options the client options
   * @param context the file system context
   */
  public AlluxioFileRaftOutStream(AlluxioURI path, OutStreamOptions options, FileSystemContext context)
      throws IOException {
    mCloser = Closer.create();
    mContext = context;
    mCloser.register(mContext.blockReinit());
    try {
      mUri = Preconditions.checkNotNull(path, "path");
      mBlockSize = options.getBlockSizeBytes();
      mOptions = options;
      mClientSelector = new AlluxioClientPipelineSelector(mContext);
    }  catch (Throwable t) {
      throw CommonUtils.closeAndRethrow(mCloser, t);
    }
  }

  @Override
  public void write(int b) throws IOException {
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    // 1.将b, off, len三个参数封装成写入请求
    alluxio.grpc.Chunk chunk = alluxio.grpc.Chunk.newBuilder()
        .setData(ByteString.copyFrom(b)).build();
    alluxio.grpc.WriteRequest writeRequest =
        alluxio.grpc.WriteRequest.newBuilder().setChunk(chunk).build();
    Message message = Message.valueOf(writeRequest.toString());
    // 2.由mRaftClient写入数据
    RaftClient raftClient = mClientSelector.getPipelineClient();
    raftClient.send(message);
  }
}
