package info.kghost.wow.protocol
import scala.util.Random
import java.security.MessageDigest
import scala.Array.canBuildFrom

object Srp {
  val G = BigInt(2)

  val MODULUS_SIZE = 128
  val MODULUS = BigInt("86a7f6deeb306ce519770fe37d556f29944132554ded0bd68205e27f3231fef5a10108238a3150c59caf7b0b6478691c13a6acf5e1b5adafd4a943d4a21a142b800e8a55f8bfbac700eb77a7235ee5a609e350ea9fc19f10d921c2fa832e4461b7125d38d254a0be873dfc27858acb3f8b9f258461e4373bc3a6c2a9634324ab", 16)

  val SALT_SIZE = 32
  val HASH_SIZE = 32
  val SESSION_KEY_SIZE = HASH_SIZE * 2
}

class Srp(r: BigInt) {
  import Srp._

  def this() = this(BigInt(Srp.MODULUS_SIZE * 8, new Random()) % Srp.MODULUS)

  def bigIntToByteArray(i: BigInt) = (i.toByteArray dropWhile { _ == 0 }).reverse
  def byteArrayToBigInt(bs: Array[Byte]) = BigInt(Array[Byte](0) ++ bs.reverse)
  def pad(bs: Array[Byte], length: Int) = new Array[Byte](length - bs.length) ++ bs
  def modpow(a: BigInt, n: BigInt, m: BigInt) = a.modPow(n, m)
  def digest(bs: Array[Byte]): Array[Byte] = {
    val mesd = MessageDigest.getInstance("SHA-256");
    mesd.update(bs);
    mesd.digest();
  }
  def digest(s: String): Array[Byte] = digest(s.getBytes)

  lazy val hN_xor_hG = (digest(bigIntToByteArray(MODULUS)) zip digest(bigIntToByteArray(G))) map { case (x, y) => (x ^ y).toByte }
  lazy val k = byteArrayToBigInt(digest(bigIntToByteArray(MODULUS) ++ bigIntToByteArray(G)))

  lazy val a = modpow(G, r, MODULUS)
  lazy val as = bigIntToByteArray(a)

  class SrpSession(user: String, password: String, salt: Array[Byte], b: BigInt) {
    lazy val u = byteArrayToBigInt(digest(pad(bigIntToByteArray(a), MODULUS_SIZE) ++ pad(bigIntToByteArray(b), MODULUS_SIZE)))
    lazy val x = byteArrayToBigInt(digest(salt ++ digest(user + ":" + password)))
    lazy val s = modpow(b - k * modpow(G, x, MODULUS), r + u * x, MODULUS)

    lazy val auth1_proof =
      digest(hN_xor_hG ++ digest(user) ++ salt ++ pad(bigIntToByteArray(a), MODULUS_SIZE) ++ pad(bigIntToByteArray(this.b), MODULUS_SIZE) ++ session_key)

    lazy val session_key = {
      val sb = pad(bigIntToByteArray(s), MODULUS_SIZE)
      var l = sb.length

      val t1 = ((0 until l by 2) map { sb(_) }).toArray
      val t2 = ((1 until l by 2) map { sb(_) }).toArray

      (digest(t1) zip digest(t2)) flatMap { case (x, y) => Array(x, y) }
    }

    def authenticatorProofMask(counter: Byte) = digest(Array(counter) ++ session_key)
  }

  def feed(user: String, password: String, salt: Array[Byte], b: Array[Byte]) = new SrpSession(user, password, pad(salt, SALT_SIZE), byteArrayToBigInt(b))
  def feed(user: String, password: String, salt: Array[Byte], b: BigInt) = new SrpSession(user, password, pad(salt, SALT_SIZE), b)
}

