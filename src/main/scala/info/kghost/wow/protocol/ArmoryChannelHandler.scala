package info.kghost.wow.protocol

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelHandler

class ArmoryChannelHandler extends SimpleChannelHandler {
  private val pending = scala.collection.mutable.Map.empty[Int, (Response => Unit)]

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
    val response = e.getMessage.asInstanceOf[Response]
    pending.remove(response.id) match {
      case Some(continuation) => continuation(response)
      case None => println("Error, no continuation found for response " + response.id)
    }
  }

  override def writeRequested(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
    val (continuation, request) = e.getMessage.asInstanceOf[((Response => Unit), Request)]
    pending(request.id) = continuation
    super.writeRequested(ctx, e)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    e.getCause.printStackTrace
    e.getChannel.close
  }
}