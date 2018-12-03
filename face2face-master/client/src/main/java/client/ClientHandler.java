package client;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Utils;
import protobuf.generate.cli2srv.chat.Chat;
import protobuf.generate.cli2srv.login.Auth;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Dell on 2016/2/15.
 * 模拟客户端聊天：自己给自己发消息
 */
public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    /**
     * question 1
     * 当前管道的上下文信息有什么用
     */
    public static ChannelHandlerContext _gateClientConnection;

    String _userId = "";
    boolean _verify = false;

    /**
     * question 3
     * 这个计数是干什么的
     */
    private static int count = 0;

    public static AtomicLong increased = new AtomicLong(1);//本地的表示，这个应该有服务器来决定

    /**
     * 连接被激活
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
       //保存当前的上下文
        _gateClientConnection = ctx;


        String passwd = "123";
        _userId = Long.toString(increased.getAndIncrement());

        //请求注册
        sendCRegister(ctx, _userId, passwd);
        //请求登陆
        sendCLogin(ctx, _userId, passwd);
    }

    /**
     * 发送注册消息
     * @param ctx
     * @param userid
     * @param passwd
     */
    void sendCRegister(ChannelHandlerContext ctx, String userid, String passwd) {
        //login.proto
        Auth.CRegister.Builder cb = Auth.CRegister.newBuilder();
        cb.setUserid(userid);
        cb.setPasswd(passwd);

        //probobuf 消息 打包成 Netty 传递的消息
        ByteBuf byteBuf = Utils.pack2Client(cb.build());
        //发送
        ctx.writeAndFlush(byteBuf);
        logger.info("send CRegister userid:{}", _userId);
    }

    /**
     * 发送登陆消息
     * @param ctx
     * @param userid
     * @param passwd
     */
    void sendCLogin(ChannelHandlerContext ctx, String userid, String passwd) {
        Auth.CLogin.Builder loginInfo = Auth.CLogin.newBuilder();
        loginInfo.setUserid(userid);
        loginInfo.setPasswd(passwd);
        loginInfo.setPlatform("ios");
        loginInfo.setAppVersion("1.0.0");

        ByteBuf byteBuf = Utils.pack2Client(loginInfo.build());
        ctx.writeAndFlush(byteBuf);
        logger.info("send CLogin userid:{}", _userId);
    }

    /**
     * 读到消息了（消息是怎么被转换的？？？？？）
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) throws Exception {
        logger.info("received message: {}", msg.getClass());
        if(msg instanceof Auth.SResponse) {
            Auth.SResponse sp = (Auth.SResponse) msg;
            int code = sp.getCode();
            String desc = sp.getDesc();
            switch (code) {
                //登录成功
                case Common.VERYFY_PASSED:
                    logger.info("Login succeed, description: {}", desc);
                    _verify = true;
                    break;
                //登录账号不存在
                case Common.ACCOUNT_INEXIST:
                    logger.info("Account inexsit, description: {}", desc);
                    break;
                //登录账号或密码错误
                case Common.VERYFY_ERROR:
                    logger.info("Account or passwd Error, description: {}", desc);
                    break;
                //账号已被注册
                case Common.ACCOUNT_DUMPLICATED:
                    logger.info("Dumplicated registry, description: {}", desc);
                    break;
                //注册成功
                case Common.REGISTER_OK:
                    logger.info("User registerd successd, description: {}", desc);
                    break;
                case Common.Msg_SendSuccess:
                    logger.info("Chat Message Send Successed, description: {}", desc);
                default:
                    logger.info("Unknow code: {}", code);
            }
        } else if(msg instanceof Chat.SPrivateChat) {
            logger.info("{} receiced chat message: {}.Total:{}", _userId, ((Chat.SPrivateChat) msg).getContent(), ++count);
        }

        //这样设置的原因是，防止两方都阻塞在输入上
        if(_verify) {
            sendMessage();
            Thread.sleep(Client.frequency);
        }
    }

    /**
     * 发送消息
     */
    void sendMessage() {
//        logger.info("WelCome To Face2face Chat Room, You Can Say Something Now: ");
//        Scanner sc = new Scanner(System.in);
//        String content = sc.nextLine();
          String content = "Hello world!";
//        logger.info("{} Send Message: {} to {}", _userId, content, _friend);

        Chat.CPrivateChat.Builder cp = Chat.CPrivateChat.newBuilder();
        cp.setContent(content);
        cp.setSelf(_userId);
        cp.setDest(_userId);

        ByteBuf byteBuf = Utils.pack2Client(cp.build());
        _gateClientConnection.writeAndFlush(byteBuf);
    }

    /**
     * 读取完成
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //ctx.flush();
    }

    /**
     * 管道异常
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
