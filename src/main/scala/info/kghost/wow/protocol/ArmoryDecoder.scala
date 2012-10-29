package info.kghost.wow.protocol

import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder

class ArmoryDecoder extends OneToOneDecoder {
  override protected def decode(ctx: ChannelHandlerContext, channel: Channel, msg: Object): Object = msg
}