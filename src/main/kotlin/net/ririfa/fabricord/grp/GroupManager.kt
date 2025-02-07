@file:Suppress("DuplicatedCode")

package net.ririfa.fabricord.grp

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.ririfa.fabricord.Fabricord
import net.ririfa.fabricord.Fabricord.Companion.grpFile
import net.ririfa.fabricord.discord.DiscordBotManager.server
import net.ririfa.fabricord.translation.FabricordMessageKey
import net.ririfa.fabricord.translation.FabricordMessageProvider
import net.ririfa.fabricord.utils.ShortUUID
import net.ririfa.fabricord.utils.ShortUUIDTypeAdapter
import net.ririfa.fabricord.utils.UUIDTypeAdapter
import net.ririfa.langman.LangMan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.*

object GroupManager {
	private val logger: Logger = LoggerFactory.getLogger(this::class.simpleName)
	lateinit var langMan: LangMan<FabricordMessageProvider, Text>

	private val groups = mutableMapOf<ShortUUID, Group>()
	val playerInGroupedChat = mutableMapOf<UUID, ShortUUID>()

	private val gson: Gson = GsonBuilder()
		.registerTypeAdapter(UUID::class.java, UUIDTypeAdapter())
		.registerTypeAdapter(ShortUUID::class.java, ShortUUIDTypeAdapter())
		.setPrettyPrinting()
		.create()

	fun initialize() {
		if (!Files.exists(grpFile)) Files.createFile(grpFile)

		langMan = Fabricord.instance.langMan

		if (Files.exists(grpFile)) {
			logger.info(langMan.getSysMessage(FabricordMessageKey.System.GRP.LoadingGroupFile))

			try {
				val jsonString = Files.readString(grpFile)
				val type = object : TypeToken<Map<ShortUUID, Group>>() {}.type
				val loaded = gson.fromJson<Map<ShortUUID, Group>>(jsonString, type)
				groups.clear()
				groups.putAll(loaded)
				logger.info(langMan.getSysMessage(FabricordMessageKey.System.GRP.LoadedGroups, groups.size))
			} catch (e: Exception) {
				logger.error(langMan.getSysMessage(FabricordMessageKey.System.GRP.FailedToLoadGroupFile), e)
			}
		} else {
			logger.warn(langMan.getSysMessage(FabricordMessageKey.System.GRP.GroupFileNotFound))
		}
	}

	fun save() {
		try {
			val jsonString = gson.toJson(groups)
			Files.writeString(grpFile, jsonString)
			logger.info(langMan.getSysMessage(FabricordMessageKey.System.GRP.GroupFileSaved, grpFile))
		} catch (e: Exception) {
			logger.error(langMan.getSysMessage(FabricordMessageKey.System.GRP.FailedToSaveGroupFile), e)
		}
	}

	fun createGroup(name: String, owner: UUID, open: Boolean = false, addMembers: List<UUID> = emptyList()): Group {
		val id = ShortUUID.randomUUID()
		val group = Group(id, name, owner, mutableSetOf(owner).also { it.addAll(addMembers) }, open)
		groups[id] = group
		save()
		return group
	}

	fun getGroupById(id: ShortUUID): Group? {
		return groups[id]
	}

	fun getGroupsByName(name: String): List<Group> {
		return groups.values.filter { it.name == name }
	}

	fun deleteGroup(id: ShortUUID) {
		groups.remove(id)
		save()
	}

	object Command {
		//TODO: Use LangMan
		internal fun joinGroup(player: ServerPlayerEntity, group: Group, source: ServerCommandSource) {
			if (group.members.contains(player.uuid)) {
				source.sendMessage(Text.literal("You are already a member of this group."))
				return
			}

			if (group.open) {
				group.members.add(player.uuid)
				source.sendMessage(Text.literal("Joined group: ${group.name} (ID: ${group.id.toShortString()})"))
			} else {
				if (group.joinRequests.contains(player.uuid)) {
					source.sendMessage(Text.literal("You have already requested to join this group."))
				} else {
					group.joinRequests.add(player.uuid)
					source.sendMessage(Text.literal("Join request sent to ${group.name}. The group owner must approve your request."))
				}
			}
		}

		fun showHelp(source: ServerCommandSource) {
			var msg = Component.text("Group commands:")
				.append(Component.text("/grp create <groupName> [<players...>]"))
				.append(Component.text("/grp join <groupNameOrID>"))
				.append(Component.text("/grp del <groupIdOrName>"))

			val s = GsonComponentSerializer.gson().serialize(msg)
			source.sendMessage(Text.Serialization.fromJson(s, server?.registryManager ?: return))
		}

		internal fun findPlayerUUIDByName(name: String): UUID? {
			val p = server?.playerManager?.playerList?.firstOrNull { it.name.string.equals(name, ignoreCase = true) } ?: return null
			return p.uuid
		}
	}
}