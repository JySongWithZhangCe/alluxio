package alluxio.client.file;

import alluxio.AlluxioURI;
import alluxio.client.AlluxioStorageType;
import alluxio.client.UnderStorageType;
import alluxio.client.block.stream.BlockOutStream;
import alluxio.client.file.options.OutStreamOptions;
import com.google.common.io.Closer;
import org.apache.ratis.client.RaftClient;

import java.io.IOException;
import java.util.List;

public class AlluxioFileRaftOutStream extends FileOutStream {

  /** Used to manage closeable resources. */
  private final Closer mCloser = null;
  private final long mBlockSize = 1;
  private final AlluxioStorageType mAlluxioStorageType = null;
  private final UnderStorageType mUnderStorageType = null;
  private final FileSystemContext mContext = null;
  private final OutStreamOptions mOptions = null;

  private boolean mCanceled;
  private boolean mClosed;
  private boolean mShouldCacheCurrentBlock;
  private BlockOutStream mCurrentBlockOutStream = null;
  private final List<BlockOutStream> mPreviousBlockOutStreams = null;

  protected final AlluxioURI mUri = null;

  @Override
  public void write(int b) throws IOException {
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    // 1.将b, off, len三个参数封装成写入请求
    // 2.由mRaftClient写入数据
    RaftClient raftClient = null;
  }
}
