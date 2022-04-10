package com.rshub.plugin

import com.rshub.api.actions.NpcAction
import com.rshub.api.coroutines.delayUntil
import com.rshub.api.plugin.KotlinPlugin
import com.rshub.api.world.WorldHelper
import kraken.plugin.api.ImGui
import kraken.plugin.api.Npcs
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class QoLPlugin : KotlinPlugin("Quality of Life") {
    val collecting = mutableMapOf(
        "Seren Spirit" to (true to 0),
        "Divine Blessing" to (true to 0)
    )

    override suspend fun loop(): Int {
        for (npcName in collecting.keys) {
            val (collect, count) = collecting[npcName]!!
            if(collect) {
                val npc = WorldHelper.closestNpc { it.name.equals(npcName, true) }
                if(npc != null && npc.interact(NpcAction.NPC1) && delayUntil(30000) { Npcs.byServerIndex(npc.serverIndex) == null }) {
                    collecting[npcName] = collect to (count + 1)
                    continue
                }
            }
        }
        return 600
    }

    override fun onLoad() {
        readData()
    }

    private fun readData() {
        val stream = ByteArrayInputStream(context.persistentData)
        val input = DataInputStream(stream)
        val size = input.readUnsignedByte()
        repeat(size) {
            val name = input.readUTF()
            val collect = input.readBoolean()
            val count = input.readInt()
            collecting[name] = collect to count
        }
    }

    private fun writeData() {
        val stream = ByteArrayOutputStream()
        val out = DataOutputStream(stream)
        val size = collecting.size
        out.writeByte(size)
        collecting.forEach { (t, u) ->
            out.writeUTF(t)
            out.writeBoolean(u.first)
            out.writeInt(u.second)
        }
        context.persistentData = stream.toByteArray()
    }

    override suspend fun paint() {
        collecting.entries.toList().forEach {
            val (name, value) = it
            ImGui.label("$name:\t")
            ImGui.label("Collected: ${value.second}")
            val collect = ImGui.checkbox("Collect", value.first)
            ImGui.label("-----------------------------------------------")
            if(value.first != collect) {
                collecting[name] = collect to value.second
                writeData()
            }
        }
    }

    override suspend fun paintOverlay() {}
}