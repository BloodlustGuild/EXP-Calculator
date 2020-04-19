package xyz.bloodlust.tools.expcalc

import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle
import xyz.bloodlust.api.hypixel.resources.HypixelResources
import zone.nora.slothpixel.Slothpixel
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val player = when {
        args.isEmpty() -> {
            println("You have to put a player, idiot.")
            exitProcess(0)
        }
        args.contains("--player") -> args[args.indexOf("--player")+1]
        else -> args[0]
    }
    val detail = args.contains("--detail")
    var questExp = 0
    var achievementExp = 0
    var deliveryManExp = 0

    val pb1 = ProgressBar("Getting player data", 4, ProgressBarStyle.ASCII)
    val playerQuests = try {
        Slothpixel().getPlayerQuests(player)
    } catch (e: Exception) {
        error("Failed to get player data... Is this a real player?")
    }
    val challengeExp = playerQuests.challengesCompleted * 2400 // average challenge exp; they were worth 1200 for a while
    pb1.step()

    val hypixelPlayer = Slothpixel().getPlayer(player)
    pb1.step()
    val totalExp = hypixelPlayer.exp

    val achievementRewards = Slothpixel().getPlayerAchievements(hypixelPlayer.uuid).rewards.keys
    pb1.step()

    val quests = HypixelResources.getQuests()
    pb1.step()
    pb1.close()

    val pb2 = ProgressBar("Calculating quest data", playerQuests.completions.size.toLong(), ProgressBarStyle.ASCII)
    playerQuests.completions.keys.forEach { questName ->
        quests.values.forEach { it.forEach { quest ->
            if (quest.id == questName) {
                for (reward in quest.rewards) questExp += if (reward.type == "MultipliedExperienceReward") reward.amount * playerQuests.completions[questName]!!.size else 0
            }
        }}
        pb2.step()
    }
    pb2.close()

    achievementRewards.forEach {
        val amount = it.toInt()
        achievementExp += when {
            amount < 2500 -> 5000
            amount < 5000 -> 15000
            amount < 10000 -> 35000
            amount < 15000 -> 65000
            amount < 18000 -> 100000
            amount < 20000 -> 150000
            amount < 23000 -> 250000
            else -> 300000
        }
    }

    deliveryManExp += hypixelPlayer.voting.totalVotes * 5000
    deliveryManExp += hypixelPlayer.rewards.claimed * 2000

    if (detail) {
        println("----------\nBreakdown:")
        println("Quest EXP: $questExp")
        println("Challenge EXP: $challengeExp")
        println("Delivery Man EXP: $deliveryManExp")
        println("Achievement Reward EXP: $achievementExp")
        println("----------")
    }
    val legitExp = questExp + challengeExp + deliveryManExp + achievementExp
    println("\"Legit\" EXP: $legitExp")
    println("Total EXP: $totalExp")
    println("${"%.3f".format(legitExp.toDouble() / totalExp * 100)}% of ${hypixelPlayer.username}'s EXP is \"legitimate\".")
}