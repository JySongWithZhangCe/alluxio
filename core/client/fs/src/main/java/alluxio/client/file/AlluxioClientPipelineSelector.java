package alluxio.client.file;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;

import java.nio.charset.Charset;
import java.util.List;
import java.util.ArrayList;

public class AlluxioClientPipelineSelector {

  private final FileSystemContext mContext;

  AlluxioClientPipelineSelector(FileSystemContext context) {
    this.mContext = context;
  }

  RaftClient getPipelineClient() {
    RaftPeer raftPeer = new RaftPeer(RaftPeerId.getRaftPeerId("p1"), "127.0.0.1:6000");
    List<RaftPeer> raftPeerList = new ArrayList<>();
    raftPeerList.add(raftPeer);
    RaftGroup raftGroup = RaftGroup.valueOf(
        RaftGroupId.valueOf(ByteString.copyFrom("1", Charset.defaultCharset())),raftPeerList);
    RaftProperties raftProperties = new RaftProperties();
    return RaftClient.newBuilder()
        .setProperties(raftProperties)
        .setRaftGroup(raftGroup).build();
  }
}
