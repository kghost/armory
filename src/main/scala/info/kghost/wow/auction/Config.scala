package info.kghost.wow.auction

object Config {
  private val mats = Map(
    76131 -> 350000, // Primordial Ruby (Red)
    76132 -> 6000000, // Primal Diamond
    76138 -> 200000, // River's Heart (Blue)
    76139 -> 1300000, // Wild Jade (Green)
    76140 -> 1450000, // Vermilion Onyx (Orange)
    76141 -> 650000, // Imperial Amethyst (Purple)
    76142 -> 100000, // Sun's Radiance (Yellow)

    38682 -> 1000, // Enchanting Vellum
    74247 -> 750000, // Ethereal Shard http://www.wowhead.com/item=74247
    74248 -> 3000000, // Sha Crystal http://www.wowhead.com/item=74248
    74249 -> 50000, // Spirit Dust http://www.wowhead.com/item=74249
    74250 -> 200000 // Mysterious Essence http://www.wowhead.com/item=74250
    )

  private val recips = Map(
    76637 -> List((76138, 1)), // Stormy River's Heart
    76653 -> List((76139, 1)), // Regal Wild Jade
    76654 -> List((76139, 1)), // Forceful Wild Jade
    76660 -> List((76140, 1)), // Potent Vermilion Onyx
    76664 -> List((76140, 1)), // Stalwart Vermilion Onyx
    76670 -> List((76140, 1)), // Adept Vermilion Onyx
    76673 -> List((76140, 1)), // Fine Vermilion Onyx
    76677 -> List((76140, 1)), // Willful Vermilion Onyx
    76684 -> List((76141, 1)), // Etched Imperial Amethyst
    76692 -> List((76131, 1)), // Delicate Primordial Ruby
    76693 -> List((76131, 1)), // Precise Primordial Ruby
    76694 -> List((76131, 1)), // Brilliant Primordial Ruby
    76695 -> List((76131, 1)), // Flashing Primordial Ruby
    76697 -> List((76142, 1)), // Smooth Sun's Radiance
    76698 -> List((76142, 1)), // Subtle Sun's Radiance
    76699 -> List((76142, 1)), // Quick Sun's Radiance
    76700 -> List((76142, 1)), // Fractured Sun's Radiance
    76701 -> List((76142, 1)), // Mystic Sun's Radiance
    76879 -> List((76132, 1)), // Ember Primal Diamond
    76884 -> List((76132, 1)), // Agile Primal Diamond
    76885 -> List((76132, 1)), // Burning Primal Diamond
    76886 -> List((76132, 1)), // Reverberating Primal Diamond
    76888 -> List((76132, 1)), // Revitalizing Primal Diamond
    76890 -> List((76132, 1)), // Destructive Primal Diamond
    76891 -> List((76132, 1)), // Powerful Primal Diamond
    76892 -> List((76132, 1)), // Enigmatic Primal Diamond
    76893 -> List((76132, 1)), // Impassive Primal Diamond
    76894 -> List((76132, 1)), // Forlorn Primal Diamond
    76895 -> List((76132, 1)), // Austere Primal Diamond
    76896 -> List((76132, 1)), // Eternal Primal Diamond
    76897 -> List((76132, 1)), // Effulgent Primal Diamond

    74700 -> List((74249, 4), (38682, 1)), // Enchant Bracer - Mastery
    74701 -> List((74249, 8), (74250, 2), (38682, 1)), // Enchant Bracer - Major Dodge
    74706 -> List((74249, 3), (74250, 1), (38682, 1)), // Enchant Chest - Super Resilience
    74707 -> List((74249, 4), (38682, 1)), // Enchant Chest - Mighty Spirit
    74708 -> List((74249, 2), (74250, 3), (38682, 1)), // Enchant Chest - Glorious Stats
    74709 -> List((74249, 4), (74250, 1), (38682, 1)), // Enchant Chest - Superior Stamina
    74710 -> List((74249, 7), (38682, 1)), // Enchant Cloak - Accuracy
    74711 -> List((74247, 2), (38682, 1)), // Enchant Cloak - Greater Protection
    74712 -> List((74249, 3), (74250, 3), (38682, 1)), // Enchant Cloak - Superior Intellect
    74713 -> List((74250, 1), (38682, 1)), // Enchant Cloak - Superior Critical Strike
    74715 -> List((74249, 2), (74250, 1), (38682, 1)), // Enchant Boots - Greater Haste
    74716 -> List((74249, 2), (74250, 1), (38682, 1)), // Enchant Boots - Greater Precision
    74717 -> List((74247, 2), (38682, 1)), // Enchant Boots - Blurred Speed
    74718 -> List((74249, 4), (74250, 3), (38682, 1)), // Enchant Boots - Pandaren's Step
    74719 -> List((74249, 4), (38682, 1)), // Enchant Gloves - Greater Haste
    74720 -> List((74250, 2), (38682, 1)), // Enchant Gloves - Superior Expertise
    74721 -> List((38682, 1), (74249, 3), (74250, 1), (74247, 1)), // Enchant Gloves - Super Strength
    74722 -> List((74250, 3), (38682, 1)), // Enchant Gloves - Superior Mastery
    74723 -> List((74249, 12), (74247, 1), (38682, 1)), // Enchant Weapon - Windsong
    74725 -> List((74250, 3), (38682, 1)), // Enchant Weapon - Elemental Force
    74727 -> List((74247, 3), (38682, 1)), // Enchant Weapon - Colossus
    74729 -> List((74250, 3), (38682, 1)), // Enchant Off-Hand - Major Intellect
    89737 -> List((38682, 1), (74247, 1), (74250, 3)) // Enchant Shield - Greater Parry
    )

  val user = "zealot0630@gmail.com"
  val pass = "whatever"
  val realm = "Frostmourne"
  val faction = 0 // 0 -> alliance, 1 -> horde, 2 -> goblin

  val interval = 10 * 60 * 1000
  val keepalive = 40 * 1000
  val scanIventory = 1 // How many runs we do a full inventory scan
  val postInterval = 5 * 1000 // pause 2 sec between post auctions
  val cancelInterval = 2 * 1000 // pause 2 sec after cancel

  val priceLowRate = 1.2
  val priceResetRate = 3.0
  val priceResetBid = 0.95
  val matsBuyRate = 0.6

  val enchanting = List(74700, 74701, 74706, 74707, 74708, 74709, 74710, 74711, 74712, 74713, 74715, 74716, 74717, 74718, 74719, 74720, 74721, 74722, 74723, 74725, 74727, 74729, 89737)
  val jewel = List(76637, 76653, 76654, 76660, 76664, 76670, 76673, 76677, 76684, 76692, 76693, 76694, 76695, 76697, 76698, 76699, 76700, 76701, 76879, 76884, 76885, 76886, 76888, 76890, 76891, 76892, 76893, 76894, 76895, 76896, 76897)
  val items = enchanting ++ jewel

  def price(item: Item) = (recips(item.id) foldLeft 0) {
    case (sum, (mat, count)) => sum + (mats(mat) * count)
  }
}
