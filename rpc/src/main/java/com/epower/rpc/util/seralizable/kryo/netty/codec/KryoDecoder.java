package com.epower.rpc.util.seralizable.kryo.netty.codec;

import com.epower.rpc.util.seralizable.kryo.KryoUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 请求对象反序列化
 * @author xielf
 *
 */
public class KryoDecoder extends LengthFieldBasedFrameDecoder {
	
	
	public KryoDecoder() {
		super(1024*1024, 0, 4, 0, 4);
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
		if (frame == null) {
			return null;
		}
		try {
			return KryoUtils.decode(frame);
		} finally {
			if (null != frame) {
				frame.release();
			}
		}
	}
	

}
