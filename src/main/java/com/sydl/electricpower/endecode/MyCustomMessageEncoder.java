package com.sydl.electricpower.endecode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MyCustomMessageEncoder  extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 要发送的数据
        // 这里如果是自定义的类型，msg即为自定义的类型，需要转为byte[]
        byte[] body = ((ByteBuf)msg).array();

        // 数据长度
        int dataLength = body.length;
        // 缓冲区先写入数据长度
        out.writeInt(dataLength);
        // 再写入数据
        out.writeBytes(body);
    }
}
