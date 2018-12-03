package logic.handler;

import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import logic.HandlerManager;
import logic.IMHandler;
import logic.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.analysis.ParseMap;
import protobuf.generate.internal.Internal;

/**
 * Logic 的 Handle处理
 */
public class LogicServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(LogicServerHandler.class);
    private static ChannelHandlerContext _gateLogicConnection;
    private static ChannelHandlerContext _authLogicConnection;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        /**
         * question 1 z Message的子类是如何确定且读取的
         */
        Internal.GTransfer gt = (Internal.GTransfer) message;


        int ptoNum = gt.getPtoNum();
        Message msg = ParseMap.getMessage(ptoNum, gt.getMsg().toByteArray());

        /**
         * question 2 这里消息又怎么变得不确定了呢
         */
        IMHandler handler;
        if(msg instanceof Internal.Greet) {
            handler = HandlerManager.getHandler(ptoNum, gt.getUserId(), gt.getNetId(), msg, channelHandlerContext);
        } else {
            handler = HandlerManager.getHandler(ptoNum, gt.getUserId(), gt.getNetId(), msg, getGateLogicConnection());
        }

        /**
         * 提交给任务队列
         */
        Worker.dispatch(gt.getUserId(), handler);

    }

    /**
     * gate 服务上下文的set 和 get 方法
     * @param ctx
     */
    public static void setGateLogicConnection(ChannelHandlerContext ctx) {
        _gateLogicConnection = ctx;
    }

    public static ChannelHandlerContext getGateLogicConnection() {
        if(_gateLogicConnection != null) {
            return _gateLogicConnection;
        } else {
            return null;
        }
    }

    /**
     * auth 服务上下文的 set和get 方法
     * @param ctx
     */
    public static void setAuthLogicConnection(ChannelHandlerContext ctx) {
        _authLogicConnection = ctx;
    }

    public static ChannelHandlerContext getAuthLogicConnection() {
        if(_authLogicConnection != null) {
            return _authLogicConnection;
        } else {
            return null;
        }
    }
}

