package com.sydl.electricpower.endecode;

import com.sun.org.apache.bcel.internal.classfile.ConstantValue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 请求解码器
 * <pre>
 * 数据包格式
 * +——----——+——-----——+——----——+——----——+——-----——+
 * | 包头          | 模块号        | 命令号      |  长度        |   数据       |
 * +——----——+——-----——+——----——+——----——+——-----——+
 * </pre>
 * 包头4字节
 * 模块号2字节short
 * 命令号2字节short
 * 长度4字节(描述数据部分字节长度)
 **/
public class CustomMessageDecoder2 extends ByteToMessageDecoder {

    /**
     * 数据包基本长度
     */
    public static int BASE_LENTH = 4 + 2 + 2 + 4;

    //ChannelBuffer里面有一个读指针和写指针。读指针和写指针初始值是0，写多少数据写指针就移动多少
    //调用readShort方法，readInt方法就会移动读指针， 0 =< readerIndex =< writerIndex
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        //是否小于最小的长度,否则返回等待下一次
        if(in.readableBytes() >= BASE_LENTH){
            //防止socket字节流攻击
            if(in.readableBytes() > 2048){
                in.skipBytes(in.readableBytes());
            }
            //记录包头开始的index
            int beginReader;

            while(true){//循环读取，直到包头读取完毕
                beginReader = in.readerIndex();//获取读指针
                in.markReaderIndex();
                if(in.readInt() == ConstantValue.FLAG){
                    break;
                }

                //未读到包头，略过一个字节
                buffer.resetReaderIndex();
                buffer.readByte();

                //长度又变得不满足
                if(buffer.readableBytes() < BASE_LENTH){
                    return null;
                }
            }











        }else{
            return;
        }











    }

    private Object convertToObj(byte[] body) {
        return new String(body,0,body.length);
    }
}
