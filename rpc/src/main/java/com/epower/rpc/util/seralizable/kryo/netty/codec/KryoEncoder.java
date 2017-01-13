package com.epower.rpc.util.seralizable.kryo.netty.codec;

import com.epower.rpc.util.seralizable.kryo.KryoUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 传输对象序列化
 * @author xielf
 *
 */
public class KryoEncoder extends MessageToByteEncoder<Object> {
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		
		int startIdx = out.writerIndex();
		KryoUtils.encode(out, msg);
		int endIdx = out.writerIndex();
		out.setInt(startIdx, endIdx - startIdx - 4);
	}

}
