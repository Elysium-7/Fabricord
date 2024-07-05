package net.rk4z.fabricord.discord

import net.rk4z.beacon.EventHandler
import net.rk4z.beacon.IEventHandler
import net.rk4z.beacon.handler
import net.rk4z.fabricord.events.PlayerDeathEvent
import net.rk4z.fabricord.events.PlayerGrantCriterionEvent
import net.rk4z.fabricord.events.PlayerJoinEvent
import net.rk4z.fabricord.events.PlayerLeaveEvent

@Suppress("unused")
@EventHandler
class DiscordPlayerEventHandler : IEventHandler {

    val onPlayerJoin = handler<PlayerJoinEvent> { event ->
        val player = event.player
        DiscordEmbed.sendPlayerJoinEmbed(player)
    }

    val onPlayerLeave = handler<PlayerLeaveEvent> { event ->
        val player = event.player
        DiscordEmbed.sendPlayerLeftEmbed(player)
    }

    val onPlayerDeath = handler<PlayerDeathEvent> { event ->
        val player = event.player

        // The deathMessage Language is based on the server language.
        // I want to change it to the other language.
        // If you know how to change the language, please let me know.
        val deathMessage = player.damageTracker.deathMessage

        DiscordEmbed.sendPlayerDeathEmbed(player, deathMessage)
    }

    val onPlayerGrantCriterion = handler<PlayerGrantCriterionEvent> { event ->
        val player = event.player
        val criterion = event.criterion
        DiscordEmbed.sendPlayerGrantCriterionEmbed(player, criterion)
    }
}