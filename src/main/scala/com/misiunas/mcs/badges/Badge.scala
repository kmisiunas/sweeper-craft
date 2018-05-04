package com.misiunas.mcs.badges

import android.util.Log
import com.misiunas.mcs.R
import com.misiunas.mcs.game.Operator
import com.misiunas.mcs.game.config.{GameDifficulty, GameMode, GameSize}

/** Badge representation */
abstract class Badge(val googleId: String) extends Serializable {
  /** checks if necessary conditions were met for receiving this badge */
  def checkIfAchieved(action: String, operator: Operator): Boolean
  override def equals(o: Any): Boolean = o match {
    case b: Badge => b.googleId == this.googleId
    case _ => false
  }
}


case class BadgeScore(override val googleId: String,  difficulty: GameDifficulty, score: Int) extends Badge(googleId) {
  def checkIfAchieved(action: String, operator: Operator): Boolean =
    operator.mode != GameMode.Rescue && operator.difficulty == difficulty && score <= operator.getScore
}


case class BadgeAction(override val googleId: String, action: String) extends Badge(googleId) {
  override def checkIfAchieved(action: String, operator: Operator): Boolean = this.action == action
}


case class BadgeActionN(override val googleId: String, action: String, repeat: Int) extends Badge(googleId) {
  private var count: Int = 0
  override def checkIfAchieved(action: String, operator: Operator): Boolean = count match {
    case _ if repeat == count => true
    case _ if this.action == action =>
      Log.d("Badge", "Action +1: "+action)
      count = count + 1
      count == repeat
    case _ => false
  }
}


case class BadgeGoodVictory(override val googleId: String) extends Badge(googleId) {
  override def checkIfAchieved(action: String, operator: Operator): Boolean = (
    action == "uncovered_all_tiles" &&
      operator.difficulty != GameDifficulty.easy &&
      operator.mode != GameMode.Rescue &&
      (operator.size == GameSize.large || operator.size == GameSize.infinite)
    )
}


case class BadgeGreatVictory(override val googleId: String) extends Badge(googleId) {
  override def checkIfAchieved(action: String, operator: Operator): Boolean = (
    action == "uncovered_all_tiles" &&
      operator.difficulty == GameDifficulty.hard &&
      operator.mode != GameMode.Rescue &&
      operator.size == GameSize.infinite
    )

}


case class BadgeFast(override val googleId: String) extends Badge(googleId) {
  override def checkIfAchieved(action: String, operator: Operator): Boolean = (
    operator.difficulty != GameDifficulty.easy &&
      operator.mode != GameMode.Rescue &&
      operator.getScore()  > 500 &&
      operator.getScore() / operator.getTime * 1000 > 2.0
    )
}


case class BadgeInfiniteMap(override val googleId: String) extends Badge(googleId) {
  override def checkIfAchieved(action: String, operator: Operator): Boolean = (
    action == "uncovered_all_tiles" &&
      operator.mode != GameMode.Rescue &&
      operator.size == GameSize.infinite
    )
}



/** storage for Badge data */
case class BadgeUI(image: Int, title: Int, message: Int)


object Badge {

  import GameDifficulty._

  val ui: Map[Badge, BadgeUI] = Map(
    // EASY SCORE
    BadgeScore("CgkIvta9yaMFEAIQCw", easy, 200) -> BadgeUI(R.drawable.badges_e1, R.string.badge_e1_title, R.string.badge_e1_message),
    BadgeScore("CgkIvta9yaMFEAIQDA", easy, 1000) -> BadgeUI(R.drawable.badges_e2, R.string.badge_e2_title, R.string.badge_e2_message),
    // MEDIUM SCORE
    BadgeScore("CgkIvta9yaMFEAIQDQ", medium, 200) -> BadgeUI(R.drawable.badges_m1, R.string.badge_m1_title, R.string.badge_m1_message),
    BadgeScore("CgkIvta9yaMFEAIQDg", medium, 1000) -> BadgeUI(R.drawable.badges_m2, R.string.badge_m2_title, R.string.badge_m2_message),
    BadgeScore("CgkIvta9yaMFEAIQDg", medium, 20000) -> BadgeUI(R.drawable.badges_m3, R.string.badge_m3_title, R.string.badge_m3_message),
    // HARD SCORE
    BadgeScore("CgkIvta9yaMFEAIQEA", hard, 200) -> BadgeUI(R.drawable.badges_h1, R.string.badge_h1_title, R.string.badge_h1_message),
    BadgeScore("CgkIvta9yaMFEAIQEQ", hard, 2000) -> BadgeUI(R.drawable.badges_h2, R.string.badge_h2_title, R.string.badge_h2_message),
    BadgeScore("CgkIvta9yaMFEAIQEg", hard, 5000) -> BadgeUI(R.drawable.badges_h3, R.string.badge_h3_title, R.string.badge_h3_message),
    BadgeScore("CgkIvta9yaMFEAIQEw", hard, 20000) -> BadgeUI(R.drawable.badges_h4, R.string.badge_h4_title, R.string.badge_h4_message),

    // Actions
    BadgeAction("CgkIvta9yaMFEAIQFA", "made_sword") -> BadgeUI(R.drawable.badge_to_arms, R.string.badge_to_arms_title, R.string.badge_to_arms_message),
    BadgeAction("CgkIvta9yaMFEAIQFQ", "explosives_boom") -> BadgeUI(R.drawable.badge_tnt, R.string.badge_tnt_title, R.string.badge_tnt_message),
    BadgeAction("CgkIvta9yaMFEAIQFg", "found_treasure_sword") -> BadgeUI(R.drawable.badge_lucky_loot, R.string.badge_lucky_loot_title, R.string.badge_lucky_loot_message),
    BadgeAction("CgkIvta9yaMFEAIQFw", "found_treasure_gold") -> BadgeUI(R.drawable.badge_gold_fever, R.string.badge_gold_fever_title, R.string.badge_gold_fever_message),
    BadgeAction("CgkIvta9yaMFEAIQGA", "found_treasure_map") -> BadgeUI(R.drawable.badge_treasure_hunt, R.string.badge_treasure_hunt_title, R.string.badge_treasure_hunt_message),

    // Action - do many times
    BadgeActionN("CgkIvta9yaMFEAIQGQ", "diamond_sword_kill_zombie", 20) -> BadgeUI(R.drawable.badge_zombie_slayer, R.string.badge_zombie_slayer_title, R.string.badge_zombie_slayer_message),
    BadgeActionN("CgkIvta9yaMFEAIQGg", "diamond_sword_kill_enderman", 5) -> BadgeUI(R.drawable.badge_braveness, R.string.badge_braveness_title, R.string.badge_braveness_message),
    BadgeActionN("CgkIvta9yaMFEAIQGw", "found_diamond", 4+7+8+5+2+3) -> BadgeUI(R.drawable.badge_diamond, R.string.badge_diamond_title, R.string.badge_diamond_message),
    BadgeActionN("CgkIvta9yaMFEAIQHA", "explosives_boom",20) -> BadgeUI(R.drawable.badge_boom, R.string.badge_boom_title, R.string.badge_boom_message),
    BadgeActionN("CgkIvta9yaMFEAIQHQ", "ate_pie", 20) -> BadgeUI(R.drawable.badge_honeydew_appetite, R.string.badge_honeydew_appetite_title, R.string.badge_honeydew_appetite_message),
    BadgeActionN("CgkIvta9yaMFEAIQHg", "uncovered_all_tiles", 20) -> BadgeUI(R.drawable.badge_addictive, R.string.badge_addictive_title, R.string.badge_addictive_message),

    // Special
    BadgeFast("CgkIvta9yaMFEAIQHw") -> BadgeUI(R.drawable.badge_quick, R.string.badge_quick_title , R.string.badge_quick_message ),
    BadgeGoodVictory("CgkIvta9yaMFEAIQIA") -> BadgeUI(R.drawable.badge_adventurer, R.string.badge_adventurer_title, R.string.badge_adventurer_message),
    BadgeInfiniteMap("CgkIvta9yaMFEAIQIQ") -> BadgeUI(R.drawable.badge_lord_of_unknown, R.string.badge_lord_of_unknown_title, R.string.badge_lord_of_unknown_message),
    BadgeGreatVictory("CgkIvta9yaMFEAIQIg") -> BadgeUI(R.drawable.badge_pure_awesome, R.string.badge_pure_awesome_title, R.string.badge_pure_awesome_message)
  )

  val all: Set[Badge] = ui.keySet

}
