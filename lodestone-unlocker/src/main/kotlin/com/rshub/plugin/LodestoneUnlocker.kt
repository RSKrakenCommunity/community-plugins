package com.rshub.plugin

import com.rshub.api.actions.ObjectAction
import com.rshub.api.coroutines.delayUntil
import com.rshub.api.lodestone.Lodestones
import com.rshub.api.pathing.walking.Traverse
import com.rshub.api.plugin.KotlinPlugin
import com.rshub.api.world.WorldHelper
import kraken.plugin.api.Debug
import kraken.plugin.api.ImGui
import kraken.plugin.api.Players
import java.util.*

class LodestoneUnlocker : KotlinPlugin("Lodestone Unlocker") {

    var start: Boolean = false
    var isMembers: Boolean = false

    override suspend fun loop(): Int {
        if (start) {
            val player = Players.self() ?: return 600
            val lodestonesList = if(!isMembers) {
                LinkedList(Lodestones.LODESTONES.
                toList()
                    .sortedBy { it.dest.distance(player.globalPosition) }
                    .filterNot { it.isMembers })
            } else {
                LinkedList(Lodestones.LODESTONES
                    .sortedBy { it.dest.distance(player.globalPosition) }
                    .toList())
            }
            while (lodestonesList.isNotEmpty()) {
                val lodestone = lodestonesList.poll() ?: continue
                if (lodestone.isUnlocked() || lodestone === Lodestones.CANIFIS) {
                    continue
                }
                Debug.log("Unlocking: ${lodestone.name}")
                if (Traverse.walkTo(lodestone.dest)) {
                    val obj = WorldHelper.closestObject { it.name.endsWith("lodestone", true) }
                    if (obj != null && obj.interact(
                            ObjectAction.OBJECT1,
                            false
                        ) && delayUntil(60000) { lodestone.isUnlocked() }
                    ) {
                        continue
                    } else {
                        Debug.log("Couldn't activate ${lodestone.name}")
                    }
                } else {
                    Debug.log("Failed to walk to ${lodestone.dest}")
                }
            }
            start = false
        }
        return 600
    }

    override fun onLoad() {}

    override suspend fun paint() {
        isMembers = ImGui.checkbox("Unlock Members", isMembers)
        if (ImGui.button("Start Unlocking")) {
            start = true
        }
        for (lodestone in Lodestones.LODESTONES) {
            if (lodestone.isMembers && !isMembers) {
                continue
            }
            val name = lodestone.name.lowercase().capitalize()
                .replace('_', ' ')
            ImGui.label("$name - ${if (lodestone.isUnlocked()) "Unlocked" else "Locked"}")
        }
    }

    override suspend fun paintOverlay() {}

}