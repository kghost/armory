package info.kghost.wow.protocol

case class Request(target: String, id: Int, data: Map[String, Object])