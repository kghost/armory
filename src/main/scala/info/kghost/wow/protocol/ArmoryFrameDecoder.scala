package info.kghost.wow.protocol

import scala.util.continuations.cpsParam
import scala.util.continuations.reset
import scala.util.continuations.shift

import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.frame.FrameDecoder

import info.kghost.wow.cps.CpsIterable._

class ArmoryFrameDecoder extends FrameDecoder {
  private var continuation: PausableBuffer => Object @cpsParam[java.lang.Object, java.lang.Object] = null

  class PausableBuffer(buffer: ChannelBuffer) {
    def readLong: Long @cpsParam[Object, Object] =
      if (buffer.readableBytes >= 8)
        buffer.readLong
      else shift { k: (Long => Object) =>
        continuation = { b => continuation = null; k(b.readLong) }
        null
      }

    def readInt: Int @cpsParam[Object, Object] =
      if (buffer.readableBytes >= 4)
        buffer.readInt
      else shift { k: (Int => Object) =>
        continuation = { b => continuation = null; k(b.readInt) }
        null
      }

    def readShort: Short @cpsParam[Object, Object] =
      if (buffer.readableBytes >= 2)
        buffer.readShort
      else shift { k: (Short => Object) =>
        continuation = { b => continuation = null; k(b.readShort) }
        null
      }

    def readByte: Byte @cpsParam[Object, Object] =
      if (buffer.readableBytes >= 1)
        buffer.readByte
      else shift { k: (Byte => Object) =>
        continuation = { b => continuation = null; k(b.readByte) }
        null
      }

    def readBytes(count: Int): Array[Byte] @cpsParam[Object, Object] =
      if (buffer.readableBytes >= count) {
        val bs = new Array[Byte](count)
        buffer.readBytes(bs)
        bs
      } else shift { k: (Array[Byte] => Object) =>
        continuation = { b => continuation = null; k(b.readBytes(count)) }
        null
      }

    def readByteArray = readBytes(readInt)
    def readString = new String(readBytes(readInt))

    def readObject: Object @cpsParam[Object, Object] = readByte match {
      case 1 => {
        val count = 0 until readInt
        (count.cps foldLeft Map.empty[String, Object]) { (m, i) => m + ((readString, readObject)) }
      }
      case 2 => {
        val count = 0 until readInt
        count.cps map { i => readObject }
      }
      case 3 => new java.lang.Integer(readInt)
      case 4 => readByteArray
      case 5 => readString
      case 6 => new java.lang.Boolean(readByte == 1)
      case 7 => new java.lang.Long(readLong)
    }
  }

  override def decode(ctx: ChannelHandlerContext, channel: Channel, buffer: ChannelBuffer): Object = reset {
    val b = new PausableBuffer(buffer)
    if (continuation != null)
      continuation(b)
    else
      Response(b.readInt, b.readShort, b.readString, b.readInt, b.readObject)
  }
}