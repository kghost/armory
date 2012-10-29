package info.kghost.wow

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import scala.Array.canBuildFrom
import scala.Option.option2Iterable
import scala.collection.mutable.Queue
import scala.util.continuations.cpsParam
import scala.util.continuations.reset
import scala.util.continuations.shift
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelFuture
import org.jboss.netty.channel.ChannelFutureListener
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.logging.InternalLoggerFactory
import org.jboss.netty.logging.Log4JLoggerFactory
import info.kghost.wow.auction.Auction
import info.kghost.wow.auction.Charactor
import info.kghost.wow.auction.Config
import info.kghost.wow.auction.Item
import info.kghost.wow.cps.CpsIterableUnit.to
import info.kghost.wow.protocol.ArmoryChannelHandler
import info.kghost.wow.protocol.ArmoryEncoder
import info.kghost.wow.protocol.ArmoryFrameDecoder
import info.kghost.wow.protocol.Bytes
import info.kghost.wow.protocol.Request
import info.kghost.wow.protocol.RequestBuilder
import info.kghost.wow.protocol.Response
import info.kghost.wow.protocol.Srp
import java.util.Date

/**
 * @author ${user.name}
 */
object App {
  def main(args: Array[String]) {
    InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());

    val queue = new Queue[Runnable]

    val bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor, Executors.newSingleThreadExecutor));

    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      override def getPipeline: ChannelPipeline = {
        val pipeline = Channels.pipeline()
        //pipeline.addLast("logging", new LoggingHandler(InternalLogLevel.DEBUG))
        pipeline.addLast("frameDecoder", new ArmoryFrameDecoder)
        pipeline.addLast("customEncoder", new ArmoryEncoder)
        pipeline.addLast("handler", new ArmoryChannelHandler)
        pipeline
      }
    });

    def pausable(future: ChannelFuture): () => Channel @cpsParam[Unit, Unit] = {
      var continuation: (Channel => Unit) = null
      var ret: Channel = null

      future.addListener(new ChannelFutureListener {
        def operationComplete(f: ChannelFuture): Unit = synchronized {
          future.removeListener(this)
          ret = f.getChannel
          queue.synchronized {
            queue.enqueue(new Runnable {
              override def run: Unit = if (continuation != null) {
                val tmp = continuation
                continuation = null
                tmp(ret)
              }
            })
            queue.notify
          }
        }
      })

      () => if (ret != null)
        ret
      else shift { k: (Channel => Unit) =>
        continuation = k
      }
    }

    def rpc(conn: Channel, request: Request): () => Response @cpsParam[Unit, Unit] = {
      var continuation: (Response => Unit) = null
      var ret: Response = null

      conn.write(({ response: Response =>
        ret = response
        queue.synchronized {
          queue.enqueue(new Runnable {
            override def run: Unit = if (continuation != null) {
              val tmp = continuation
              continuation = null
              tmp(ret)
            }
          })
          queue.notify
        }
      }, request))

      () => if (ret != null)
        ret
      else shift { k: (Response => Unit) =>
        continuation = k
      }
    }

    type M = Map[String, Object]
    def req[T](connection: Channel, request: Request, fail: => T)(succ: M => T @cpsParam[Unit, Unit]): T @cpsParam[Unit, Unit] = {
      val req = rpc(connection, request)
      req() match {
        case Response(_, 200, _, _, data: M) => succ(data)
        case r: Response => fail
      }
    }

    var id = new Function0[Int] {
      var start = 0
      def apply(): Int = {
        start += 1
        start
      }
    }

    def setMain[T](connection: Channel, char: Charactor, fail: => T)(succ: => T @cpsParam[Unit, Unit]): T @cpsParam[Unit, Unit] = {
      val att = connection.getAttachment
      if (att != null && att.asInstanceOf[Charactor] == char)
        succ
      else {
        req(connection, RequestBuilder.setMain(id(), char), fail) { x =>
          connection.setAttachment(char)
          succ
        }
      }
    }

    def mapToItem(item: M, char: Charactor, source: Int) =
      Item(item("n").asInstanceOf[String],
        item("id").asInstanceOf[java.lang.Integer],
        item("guid").asInstanceOf[java.lang.Long],
        item.get("quan") match {
          case Some(i: java.lang.Integer) => i
          case None => 1
        },
        char, source,
        item.get("auc") match {
          case Some(i: java.lang.Long) => i
          case None => null
        })

    reset {
      val srp = new Srp
      val conn = pausable(bootstrap.connect(new InetSocketAddress("m.us.wowarmory.com", 8780)))
      req(conn(), RequestBuilder.authenticate1(id(), Bytes(srp.as), Config.user), {}) { login =>
        val loginSrpSession = srp.feed(new String(login("user").asInstanceOf[Array[Byte]]), Config.pass,
          login("salt").asInstanceOf[Array[Byte]], login("B").asInstanceOf[Array[Byte]])
        req(conn(), RequestBuilder.authenticate2(id(), Bytes(loginSrpSession.auth1_proof)), {}) { x =>
          req(conn(), RequestBuilder.authenticate2(id(), Bytes(Array[Byte]())), {}) { info =>
            val cs = info("cs").asInstanceOf[Vector[Object]] map {
              case m: M => Charactor(m("r").toString, m("n").toString)
            } filter { _.r == Config.realm }

            req(conn(), RequestBuilder.hard1(id(), Bytes(srp.as), Config.user), {}) { login2 =>
              var counter = 0
              var hard = false
              while (!hard) {
                println("Please input auth key: ")
                val key = readInt

                val hardSrpSession = srp.feed(new String(login2("user").asInstanceOf[Array[Byte]]), Config.pass,
                  login2("salt").asInstanceOf[Array[Byte]], login2("B").asInstanceOf[Array[Byte]])

                val proof = Bytes(hardSrpSession.auth1_proof)
                val authenticatorProof = Bytes((key.toString.getBytes zip loginSrpSession.authenticatorProofMask(counter.toByte)) map {
                  case (x, y) => (x ^ y).toByte
                })
                counter += 1

                req(conn(), RequestBuilder.hard2(id(), proof, authenticatorProof), {}) { x =>
                  hard = true
                }
              }

              while (true) {
                // get what we can post
                println("Fetching inventory ...")
                val itemset = {
                  val items = for {
                    (c, mail, inventory, ah) <- for {
                      c <- cs.cps
                      r <- {
                        setMain(conn(), c, Nil: List[(Charactor, Option[M], Option[M], Option[M])]) {
                          val mail = rpc(conn(), RequestBuilder.ah_mail(id(), c))
                          val inventory = rpc(conn(), RequestBuilder.ah_inventory(id(), c))
                          val ah = rpc(conn(), RequestBuilder.ah_auctions(id(), c, Config.faction))
                          val m = mail()
                          val i = inventory()
                          val a = ah()
                          List((c,
                            if (m.status == 200) Some(m.data.asInstanceOf[M]) else None,
                            if (i.status == 200) Some(i.data.asInstanceOf[M]) else None,
                            if (a.status == 200) Some(a.data.asInstanceOf[M]) else None))
                        }
                      }
                    } yield r
                    (source, item) <- (for {
                      ifmail <- mail.toList
                      endedMail <- ifmail.get("ended").toList
                      mail <- endedMail.asInstanceOf[Vector[M]]
                      items <- mail.get("items").toList
                      item <- items.asInstanceOf[Vector[M]]
                    } yield (3, item)) ++ (for {
                      ifinventory <- inventory.toList
                      mail <- (for {
                        items <- ifinventory.get("inventory").toList
                        item <- items.asInstanceOf[Vector[M]]
                      } yield (1, item)) ++ (for {
                        items <- ifinventory.get("bank").toList
                        item <- items.asInstanceOf[Vector[M]]
                      } yield (2, item))
                    } yield mail) ++ (for {
                      ifah <- ah.toList
                      items <- ifah.get("auctions").toList
                      item <- items.asInstanceOf[Vector[M]]
                    } yield (-1, item))
                  } yield mapToItem(item, c, source)

                  (items foldLeft Map.empty[Int, List[Item]].withDefaultValue(Nil)) { (m, i) =>
                    m + (i.id -> (i :: m(i.id)))
                  }
                }

                ((0 until Config.scanIventory).cps foldLeft itemset) { (itemset, i) =>
                  println("Scanning ...")

                  var newset = itemset
                  // check what we are going to post
                  val reqs2 = (for {
                    item_to_check <- Config.items.cps
                    avail <- (if (itemset.contains(item_to_check))
                      List(itemset(item_to_check))
                    else {
                      println("Out of stock: [" + item_to_check + "]")
                      Nil
                    }).cps
                  } yield (avail, rpc(conn(), RequestBuilder.ah_search(id(), 0, avail.head))))

                  val posts = for {
                    (avail, auc) <- for {
                      x <- reqs2.cps
                    } yield (x._1, x._2() match {
                      case Response(_, 200, _, _, data: M) =>
                        data.asInstanceOf[M]("auctions").asInstanceOf[Vector[M]] flatMap { auc =>
                          if (auc.contains("buy"))
                            Some(Auction(auc("id").asInstanceOf[java.lang.Integer], auc("guid").asInstanceOf[java.lang.Long],
                              auc("n").asInstanceOf[String], auc("seller").asInstanceOf[String],
                              auc("quan").asInstanceOf[java.lang.Integer],
                              auc("buy").asInstanceOf[java.lang.Long], auc("bid").asInstanceOf[java.lang.Long]))
                          else
                            None
                        }
                      case r: Response => {
                        // XXX: what to do with fetch auction fail ?
                        Vector.empty[Auction]
                      }
                    })
                    post <- {
                      val (lowPrice, lowAuc) = (auc foldLeft ((Long.MaxValue, null: Auction))) { (m, a) =>
                        val price = a.buy / a.quan
                        if (m._1 < price) m else (price, a)
                      }
                      val thresh = Config.price(avail.head) * Config.priceLowRate
                      if (lowAuc != null && (cs exists { _.n == lowAuc.seller })) {
                        println("Not posting [" + avail.head.name + "], not undercutted")
                        Nil
                      } else if (thresh >= lowPrice) {
                        println("Not posting [" + avail.head.name + "], threshold " + thresh + ", competitor " + lowPrice + " (" + lowAuc + ")")
                        Nil
                      } else
                        List(((avail max (new Ordering[Item] {
                          override def compare(x: Item, y: Item): Int = x.source - y.source
                        })), lowAuc))
                    }
                  } yield post

                  // do post !!!
                  println("Posting ...")

                  def postItem(item: Item, auc: Auction): Unit @cpsParam[Unit, Unit] = {
                    setMain(conn(), item.char, {}) {
                      if (item.source >= 0) {
                        // item in inventory/mail/bank post directly
                        req(conn(), RequestBuilder.ah_deposit(id(), Config.faction, item, 0, 1, 1), {}) { deposit =>
                          val (bid, buyout) = if (auc != null) {
                            (auc.bid - 1, auc.buy - 1)
                          } else {
                            val fallback = Config.price(item) * Config.priceResetRate
                            ((fallback * Config.priceResetBid).toLong, fallback.toLong)
                          }
                          val duration = 0
                          val quan = 1
                          req(conn(), RequestBuilder.ah_create(id(), Config.faction, item,
                            deposit("ticket").asInstanceOf[String], duration, quan, buyout, bid), {}) { x =>
                            newset = newset + (item.id -> (newset(item.id) flatMap {
                              case i @ Item(a, b, item.guid, iquan, d, e, f) =>
                                if (iquan == quan)
                                  None
                                else
                                  Some(Item(a, b, item.guid, iquan - quan, d, e, f))
                              case i => Some(i)
                            }))
                            println("Created auction: [" + item.name + "](1) at (" + bid + "," + buyout + ")")
                          }
                        }
                      } else {
                        // item in ah cancel then post
                        req(conn(), RequestBuilder.ah_cancel(id(), Config.faction, item), {}) { x =>
                          println("Cancelled: " + item)
                          Thread.sleep(Config.cancelInterval)
                          req(conn(), RequestBuilder.ah_mail(id(), item.char), {}) { mail =>
                            for {
                              endedMail <- mail.get("ended").toList.cps
                              mail <- endedMail.asInstanceOf[Vector[M]].cps
                              items <- mail.get("items").toList.cps
                              mailitem <- items.asInstanceOf[Vector[M]].cps
                            } {
                              val newitem = mapToItem(mailitem, item.char, 3)
                              if (newitem.id == item.id) postItem(newitem, auc)
                            }
                          }
                        }
                      }
                    }
                  }

                  posts.cps foreach {
                    case (item, auc) => {
                      postItem(item, auc)
                      Thread.sleep(Config.postInterval)
                    }
                  }

                  // schedule next run
                  val next = {
                    val now = System.currentTimeMillis
                    now + Config.interval - (now % Config.interval)
                  }

                  println("Done, schedule next run at: " + new Date(next))

                  while (System.currentTimeMillis < next) {
                    Thread.sleep(math.min(Config.keepalive, next - System.currentTimeMillis))
                    rpc(conn(), RequestBuilder.setMain(id(), cs.head))
                  }
                  newset
                }
              }
            }
          }
        }
      }
    }
    while (true) {
      val job = queue.synchronized {
        while (queue.isEmpty) queue.wait
        queue.dequeue
      }
      job.run
    }

    // Shut down thread pools to exit.
    bootstrap.releaseExternalResources;
  }
}
