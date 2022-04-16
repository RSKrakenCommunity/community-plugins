package com.rshub.plugin

import com.rshub.api.actions.ActionHelper
import com.rshub.api.actions.MenuAction
import com.rshub.api.actions.ObjectAction
import com.rshub.api.coroutines.delayUntil
import com.rshub.api.definitions.CacheHelper
import com.rshub.api.entities.objects.WorldObject
import com.rshub.api.plugin.KotlinPlugin
import com.rshub.api.skills.Skill
import com.rshub.api.world.WorldHelper
import kraken.plugin.api.Debug
import kraken.plugin.api.ImGui
import kraken.plugin.api.Players
import kraken.plugin.api.Widgets
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class SpringEventPlugin : KotlinPlugin("Spring Event") {

    private val skill: Skill
        get() {
            return when(selectedSkill) {
                0, 1 -> {
                    val value = CacheHelper.getVarbitValue(49157)
                    if(value == 1) Skill.COOKING else Skill.HERBLORE
                }
                2, 3 -> {
                    val value = CacheHelper.getVarbitValue(49158)
                    if(value == 1) Skill.PRAYER else Skill.AGILITY
                }
                4, 5 -> {
                    val value = CacheHelper.getVarbitValue(49160)
                    if(value == 1) Skill.CRAFTING else Skill.FLETCHING
                }
                else -> Skill.HERBLORE
            }
        }

    private val rotation: Int get() = CacheHelper.getVarbitValue(49159)

    private val skillOptions = arrayOf("Cooking", "Herblore", "Prayer", "Agility", "Crafting", "Fletching")
    private var selectedSkill = 0

    private var currentCauldronId = getCauldron()?.id ?: -1
    private var currentStumpId = getStump()?.id ?: -1

    @Transient var start: Boolean = false

    override suspend fun loop(): Int {
        if(!start) {
            return 600
        }
        selectSkill()
        when (skill) {
            Skill.AGILITY, Skill.PRAYER -> {
                updateRotation()
                val plr = Players.self()
                if (plr != null && !plr.isMoving) {
                    WorldHelper.closestObjectIgnoreClip { it.id == 119638 }?.interact(ObjectAction.OBJECT1)
                    delayUntil { Players.self()?.isMoving ?: true }
                }
            }
            Skill.HERBLORE, Skill.COOKING -> {
                val obj = getCauldron()
                if (obj != null && obj.interact(ObjectAction.OBJECT1)) {
                    currentCauldronId = obj.id
                    delayUntil(300000) {
                        currentCauldronId != getCauldron()?.id
                    }
                }
            }
            Skill.FLETCHING, Skill.CRAFTING -> {
                val obj = getStump()
                if (obj != null && obj.interact(ObjectAction.OBJECT1)) {
                    currentStumpId = obj.id
                    delayUntil(300000) {
                        currentStumpId != getStump()?.id
                    }
                }
            }
            else -> { Debug.log("Unsupported Skill ${skill.name}") }
        }
        return 600
    }

    private fun getCauldronDirection(): String {
        if (Widgets.isOpen(596)) {
            val text = Widgets.getGroupById(596).getWidget(0).getChild(1).text
            Debug.log(text)
            return text.split(":")[1].trim().lowercase()
        } else {
            Debug.log("Interface not open.")
        }
        return "south"
    }

    private fun getStumpDirection(): String {
        if (Widgets.isOpen(596)) {
            val text = Widgets.getGroupById(596).getWidget(0).getChild(1).text
            Debug.log(text)
            return text.split(":")[1].trim().lowercase()
        } else {
            Debug.log("Interface not open.")
        }
        return "south"
    }

    private fun getRotation(): String {
        if (Widgets.isOpen(596)) {
            val text = Widgets.getGroupById(596).getWidget(0).getChild(1).text
            return text.lowercase().split(":")[1].trim()
        }
        return "clockwise"
    }

    private suspend fun updateRotation() {
        val rot = rotation
        when (getRotation()) {
            "clockwise" -> {
                if (rot == 1) {
                    ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059478)
                    delayUntil { rotation == 0 }
                }
            }
            "anti-clockwise" -> {
                if (rot == 0) {
                    ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059483)
                    delayUntil { rotation == 1 }
                }
            }
        }
    }

    private fun getCauldron(): WorldObject? {
        return when (getCauldronDirection()) {
            "north" -> WorldHelper.closestObject { it.id == 119626 }
            "south" -> WorldHelper.closestObject { it.id == 119628 }
            "east" -> WorldHelper.closestObject { it.id == 119627 }
            else -> null
        }
    }

    private fun getStump(): WorldObject? {
        return when (getStumpDirection()){
            "north" -> WorldHelper.closestObject { it.name.startsWith("guthixian", true)}
            "south" -> WorldHelper.closestObject { it.name.startsWith("automaton", true) }
            "west" -> WorldHelper.closestObject { it.name.startsWith("guthix icon", true) }
            else -> null
        }
    }

    private suspend fun selectSkill() {
        if (Widgets.isOpen(596)) {
            if (selectedSkill == 0 && skill === Skill.HERBLORE) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059472)
                delayUntil { skill === Skill.COOKING }
            } else if (selectedSkill == 1 && skill == Skill.COOKING) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059468)
                delayUntil { skill === Skill.HERBLORE }
            } else if (selectedSkill == 2 && skill === Skill.AGILITY) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059472)
                delayUntil { skill === Skill.PRAYER }
            } else if(selectedSkill == 3 && skill === Skill.PRAYER) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059468)
                delayUntil { skill === Skill.AGILITY }
            } else if (selectedSkill == 4 && skill === Skill.CRAFTING) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059472)
                delayUntil { skill === Skill.CRAFTING }
            } else if (selectedSkill == 5 && skill === Skill.FLETCHING) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059468)
            }

        }
    }

    private fun loadData() {
        val stream = ByteArrayInputStream(context.persistentData)
        val input = DataInputStream(stream)
        selectedSkill = input.readUnsignedByte()
        start = input.readBoolean()
    }

    private fun writeData() {
        val stream = ByteArrayOutputStream()
        val out = DataOutputStream(stream)
        out.writeByte(selectedSkill)
        out.writeBoolean(start)
        context.persistentData = stream.toByteArray()
    }

    override fun onLoad() {
        loadData()
    }

    override suspend fun paint() {
        if(start) {
            if(ImGui.button("Stop")) {
                start = false
                writeData()
            }
        } else {
            if(ImGui.button("Start")) {
                start = true
                writeData()
            }
        }
        val selectedSkill = ImGui.combo("Skill", skillOptions, selectedSkill)
        if (selectedSkill != this.selectedSkill) {
            this.selectedSkill = selectedSkill
            writeData()
        }
    }

    override suspend fun paintOverlay() {
    }
}package com.rshub.plugin

import com.rshub.api.actions.ActionHelper
import com.rshub.api.actions.MenuAction
import com.rshub.api.actions.ObjectAction
import com.rshub.api.coroutines.delayUntil
import com.rshub.api.definitions.CacheHelper
import com.rshub.api.entities.objects.WorldObject
import com.rshub.api.plugin.KotlinPlugin
import com.rshub.api.skills.Skill
import com.rshub.api.world.WorldHelper
import kraken.plugin.api.Debug
import kraken.plugin.api.ImGui
import kraken.plugin.api.Players
import kraken.plugin.api.Widgets
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class SpringEventPlugin : KotlinPlugin("Spring Event") {

    private val skill: Skill
        get() {
            return when(selectedSkill) {
                0, 1 -> {
                    val value = CacheHelper.getVarbitValue(49157)
                    if(value == 1) Skill.COOKING else Skill.HERBLORE
                }
                2, 3 -> {
                    val value = CacheHelper.getVarbitValue(49158)
                    if(value == 1) Skill.PRAYER else Skill.AGILITY
                }
                4, 5 -> {
                    val value = CacheHelper.getVarbitValue(49160)
                    if(value == 1) Skill.CRAFTING else Skill.FLETCHING
                }
                else -> Skill.HERBLORE
            }
        }

    private val rotation: Int get() = CacheHelper.getVarbitValue(49159)

    private val skillOptions = arrayOf("Cooking", "Herblore", "Prayer", "Agility", "Crafting", "Fletching")
    private var selectedSkill = 0

    private var currentCauldronId = getCauldron()?.id ?: -1
    private var currentStumpId = getStump()?.id ?: -1

    @Transient var start: Boolean = false

    override suspend fun loop(): Int {
        if(!start) {
            return 600
        }
        selectSkill()
        when (skill) {
            Skill.AGILITY, Skill.PRAYER -> {
                updateRotation()
                val plr = Players.self()
                if (plr != null && !plr.isMoving) {
                    WorldHelper.closestObjectIgnoreClip { it.id == 119638 }?.interact(ObjectAction.OBJECT1)
                    delayUntil { Players.self()?.isMoving ?: true }
                }
            }
            Skill.HERBLORE, Skill.COOKING -> {
                val obj = getCauldron()
                if (obj != null && obj.interact(ObjectAction.OBJECT1)) {
                    currentCauldronId = obj.id
                    delayUntil(300000) {
                        currentCauldronId != getCauldron()?.id
                    }
                }
            }
            Skill.FLETCHING, Skill.CRAFTING -> {
                val obj = getStump()
                if (obj != null && obj.interact(ObjectAction.OBJECT1)) {
                    currentStumpId = obj.id
                    delayUntil(300000) {
                        currentStumpId != getStump()?.id
                    }
                }
            }
            else -> { Debug.log("Unsupported Skill ${skill.name}") }
        }
        return 600
    }

    private fun getCauldronDirection(): String {
        if (Widgets.isOpen(596)) {
            val text = Widgets.getGroupById(596).getWidget(0).getChild(1).text
            Debug.log(text)
            return text.split(":")[1].trim().lowercase()
        } else {
            Debug.log("Interface not open.")
        }
        return "south"
    }

    private fun getStumpDirection(): String {
        if (Widgets.isOpen(596)) {
            val text = Widgets.getGroupById(596).getWidget(0).getChild(1).text
            Debug.log(text)
            return text.split(":")[1].trim().lowercase()
        } else {
            Debug.log("Interface not open.")
        }
        return "south"
    }

    private fun getRotation(): String {
        if (Widgets.isOpen(596)) {
            val text = Widgets.getGroupById(596).getWidget(0).getChild(1).text
            return text.lowercase().split(":")[1].trim()
        }
        return "clockwise"
    }

    private suspend fun updateRotation() {
        val rot = rotation
        when (getRotation()) {
            "clockwise" -> {
                if (rot == 1) {
                    ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059478)
                    delayUntil { rotation == 0 }
                }
            }
            "anti-clockwise" -> {
                if (rot == 0) {
                    ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059483)
                    delayUntil { rotation == 1 }
                }
            }
        }
    }

    private fun getCauldron(): WorldObject? {
        return when (getCauldronDirection()) {
            "north" -> WorldHelper.closestObject { it.id == 119626 }
            "south" -> WorldHelper.closestObject { it.id == 119628 }
            "east" -> WorldHelper.closestObject { it.id == 119627 }
            else -> null
        }
    }

    private fun getStump(): WorldObject? {
        return when (getStumpDirection()){
            "north" -> WorldHelper.closestObject { it.name.startsWith("guthixian", true)}
            "south" -> WorldHelper.closestObject { it.name.startsWith("automaton", true) }
            "west" -> WorldHelper.closestObject { it.name.startsWith("guthix icon", true) }
            else -> null
        }
    }

    private suspend fun selectSkill() {
        if (Widgets.isOpen(596)) {
            if (selectedSkill == 0 && skill === Skill.HERBLORE) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059472)
                delayUntil { skill === Skill.COOKING }
            } else if (selectedSkill == 1 && skill == Skill.COOKING) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059468)
                delayUntil { skill === Skill.HERBLORE }
            } else if (selectedSkill == 2 && skill === Skill.AGILITY) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059472)
                delayUntil { skill === Skill.PRAYER }
            } else if(selectedSkill == 3 && skill === Skill.PRAYER) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059468)
                delayUntil { skill === Skill.AGILITY }
            } else if (selectedSkill == 4 && skill === Skill.CRAFTING) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059472)
                delayUntil { skill === Skill.CRAFTING }
            } else if (selectedSkill == 5 && skill === Skill.FLETCHING) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059468)
            }

        }
    }

    private fun loadData() {
        val stream = ByteArrayInputStream(context.persistentData)
        val input = DataInputStream(stream)
        selectedSkill = input.readUnsignedByte()
        start = input.readBoolean()
    }

    private fun writeData() {
        val stream = ByteArrayOutputStream()
        val out = DataOutputStream(stream)
        out.writeByte(selectedSkill)
        out.writeBoolean(start)
        context.persistentData = stream.toByteArray()
    }

    override fun onLoad() {
        loadData()
    }

    override suspend fun paint() {
        if(start) {
            if(ImGui.button("Stop")) {
                start = false
                writeData()
            }
        } else {
            if(ImGui.button("Start")) {
                start = true
                writeData()
            }
        }
        val selectedSkill = ImGui.combo("Skill", skillOptions, selectedSkill)
        if (selectedSkill != this.selectedSkill) {
            this.selectedSkill = selectedSkill
            writeData()
        }
    }

    override suspend fun paintOverlay() {
    }
}