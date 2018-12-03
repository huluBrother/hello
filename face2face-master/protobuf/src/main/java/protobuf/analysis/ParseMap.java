package protobuf.analysis;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * 这个类是用来注册消息的
 * 但是没有寻找到如何成功的
 */
public class ParseMap {
    private static final Logger logger = LoggerFactory.getLogger(ParseMap.class);

    @FunctionalInterface
    public interface Parsing{
        //FunctionalInterface 注解只能标注在有且仅有一个抽象方法的接口上
        Message  process(byte[] bytes) throws IOException;
    }

    public static HashMap<Integer, ParseMap.Parsing> parseMap = new HashMap<>();
    public static HashMap<Class<?>, Integer> msg2ptoNum = new HashMap<>();

    public static void register(int ptoNum, ParseMap.Parsing parse, Class<?> cla) {
        if (parseMap.get(ptoNum) == null)
            parseMap.put(ptoNum, parse);
        else {
            logger.error("pto has been registered in parseMap, ptoNum: {}", ptoNum);
            return;
        }

        if(msg2ptoNum.get(cla) == null)
            msg2ptoNum.put(cla, ptoNum);
        else {
            logger.error("pto has been registered in msg2ptoNum, ptoNum: {}", ptoNum);
            return;
        }
    }

    /**
     * 字节解析
     * @param ptoNum
     * @param bytes
     * @return
     * @throws IOException
     */
    public static Message getMessage(int ptoNum, byte[] bytes) throws IOException {
        Parsing parser = parseMap.get(ptoNum);
        if(parser == null) {
            logger.error("UnKnown Protocol Num: {}", ptoNum);
        }
        Message msg = parser.process(bytes);

        return msg;
    }

    public static Integer getPtoNum(Message msg) {
        return getPtoNum(msg.getClass());
    }

    public static Integer getPtoNum(Class<?> clz) {
        return msg2ptoNum.get(clz);
    }

}
