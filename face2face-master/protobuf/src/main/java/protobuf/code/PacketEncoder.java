package protobuf.code;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.analysis.ParseMap;

/**
 * Created by Administrator on 2016/1/29.
 */
public class PacketEncoder extends MessageToByteEncoder<Message> {
    private static final Logger logger = LoggerFactory.getLogger(PacketEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out)
            throws Exception {
        byte[] bytes = msg.toByteArray();// 将对象转换为byte
        int ptoNum = ParseMap.msg2ptoNum.get(msg);//转成消息号
        int length = bytes.length;//数组长度

        /* 加密消息体
        ThreeDES des = ctx.channel().attr(ClientAttr.ENCRYPT).get();
        byte[] encryptByte = des.encrypt(bytes);
        int length = encryptByte.length;*/

        ByteBuf buf = Unpooled.buffer(8 + length);
        buf.writeInt(length);//先写长度，4位
        buf.writeInt(ptoNum);//在写消息号，4位
        buf.writeBytes(bytes);//最后写内容
        out.writeBytes(buf);

       // logger.info("GateServer Send Message, remoteAddress: {}, content length {}, ptoNum: {}", ctx.channel().remoteAddress(), length, ptoNum);

    }
}
