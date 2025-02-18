package net.ririfa.fabricord.translation

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.ririfa.langman.def.MessageProviderDefault

// Based https://github.com/SwiftStorm-Studio/SwiftBase/blob/main/integrations/fabric/src/main/kotlin/net/rk4z/s1/swiftbase/fabric/FabricPlayer.kt
class FabricordMessageProvider(val player: ServerPlayerEntity) : MessageProviderDefault<FabricordMessageProvider, Text>(Text::class.java) {
	override fun getLanguage(): String {
		// https://maven.fabricmc.net/docs/yarn-1.21.4+build.8/net/minecraft/network/packet/c2s/common/SyncedClientOptions.html
		// comp_1951 -> Language (from this doc)
		// en_US -> en
		return player.clientOptions.comp_1951.split("_")[0]
	}
}

fun ServerPlayerEntity.adapt(): FabricordMessageProvider {
	return FabricordMessageProvider(this)
}