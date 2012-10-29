package info.kghost.wow.protocol

case class Response(length: Int, status: Short, target: String, id: Int, data: Object) {
  if (status != 200)
    println("Got some error: " + this)
}