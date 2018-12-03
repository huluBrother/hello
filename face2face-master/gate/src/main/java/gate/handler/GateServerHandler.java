package gate.handler;

import com.google.protobuf.Message;
import gate.ClientMessage;
import gate.utils.ClientConnection;
import gate.utils.ClientConnectionMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 仅负责 client 和gate 之间的连接
 */
public class GateServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(GateServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //保存客户端连接
        ClientConnectionMap.addClientConnection(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {

        ClientConnection conn = ClientConnectionMap.getClientConnection(channelHandlerContext);

        ClientMessage.processTransferHandler(message, conn);
        //TODO 最好加一个通知客户端收到消息的通知
    }

    /**
     * 连接断开
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ClientConnectionMap.removeClientConnection(ctx);
    }
}
