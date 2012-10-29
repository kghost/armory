package info.kghost.wow.protocol

import info.kghost.wow.auction.Charactor
import info.kghost.wow.auction.Item

object RequestBuilder {
  def authenticate1(id: Int, a: Bytes, account_name: String) =
    Request("/authenticate1", id,
      Map("screenRes" -> "PHONE_HIGH",
        "device" -> "Android",
        "deviceSystemVersion" -> "4.1.1",
        "deviceModel" -> "Galaxy Nexus",
        "appV" -> "5.0.0",
        "deviceTime" -> (System.currentTimeMillis: java.lang.Long),
        "deviceTimeZoneId" -> "America/New_York",
        "clientA" -> a,
        "appId" -> "Armory",
        "device_uuid" -> "949339fa-8146-46e1-a7d4-ded3d2e1b644",
        "accountName" -> account_name.toUpperCase,
        "deviceTimeZone" -> "-14400000",
        "locale" -> "en_US"))

  def authenticate2(id: Int, clientProof: Bytes) =
    Request("/authenticate2", id, Map("clientProof" -> clientProof))

  def hard1(id: Int, a: Bytes, account_name: String) =
    Request("/hardLogin1", id,
      Map("screenRes" -> "PHONE_HIGH",
        "device" -> "Android",
        "deviceSystemVersion" -> "4.1.1",
        "deviceModel" -> "Galaxy Nexus",
        "appV" -> "5.0.0",
        "deviceTime" -> (System.currentTimeMillis: java.lang.Long),
        "deviceTimeZoneId" -> "America/New_York",
        "clientA" -> a,
        "appId" -> "Armory",
        "device_uuid" -> "949339fa-8146-46e1-a7d4-ded3d2e1b644",
        "accountName" -> account_name.toUpperCase,
        "deviceTimeZone" -> "-14400000",
        "locale" -> "en_US"))

  def hard2(id: Int, clientProof: Bytes, authenticatorProof: Bytes) =
    Request("/hardLogin2", id, Map("clientProof" -> clientProof, "authenticatorProof" -> authenticatorProof))

  def setMain(id: Int, c: Charactor) =
    Request("/setMain", id, Map("r" -> c.r, "n" -> c.n))

  def ah_mail(id: Int, c: Charactor) =
    Request("/ah-mail", id, Map("r" -> c.r, "cn" -> c.n))

  def ah_auctions(id: Int, c: Charactor, faction: Int) =
    Request("/ah-auctions", id, Map("r" -> c.r, "cn" -> c.n, "f" -> (faction: java.lang.Integer)))

  def ah_inventory(id: Int, c: Charactor) =
    Request("/ah-inventory", id, Map("r" -> c.r, "cn" -> c.n))

  def ah_search(id: Int, faction: Int, item: Item) =
    Request("/ah-search", id, Map("r" -> item.char.r, "cn" -> item.char.n, "f" -> (faction: java.lang.Integer),
      "id" -> (item.id: java.lang.Integer), "maxResult" -> (5: java.lang.Integer), "sort" -> "unitbuyout"))

  def ah_deposit(id: Int, faction: Int, item: Item, duration: Int, stacks: Int, quan: Int) =
    Request("/ah-deposit", id, Map("id" -> (item.id: java.lang.Integer), "f" -> (faction: java.lang.Integer),
      "duration" -> (duration: java.lang.Integer),
      "stacks" -> (stacks: java.lang.Integer), "quan" -> (quan: java.lang.Integer)))

  def ah_create(id: Int, faction: Int, item: Item, ticket: String, duration: Int, quan: Int, buyout: Long, bid: Long) =
    Request("/ah-create", id, Map("f" -> (faction: java.lang.Integer), "id" -> (item.id: java.lang.Integer),
      "guid" -> (item.guid: java.lang.Long), "source" -> (item.source: java.lang.Integer), "ticket" -> ticket,
      "duration" -> (duration: java.lang.Integer), "quan" -> (quan: java.lang.Integer),
      "buyout" -> (buyout: java.lang.Long), "bid" -> (bid: java.lang.Long)))

  def ah_cancel(id: Int, faction: Int, item: Item) =
    Request("/ah-cancel", id, Map("r" -> item.char.r, "cn" -> item.char.n, "f" -> (faction: java.lang.Integer), "auc" -> item.auc))
}