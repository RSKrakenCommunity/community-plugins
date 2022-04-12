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
import kraken.plugin.api.Widgets

class SpringEventPlugin : KotlinPlugin("Spring Event") {

    private val skill: Skill
        get() {
            val value = CacheHelper.getVarbitValue(49157)
            return if (value == 1) {
                Skill.COOKING
            } else Skill.HERBLORE
        }
    private val skillOptions = arrayOf("Cooking", "Herblore")
    private var selectedSkill = 0

    private var currentCauldronId = getCauldron()?.id ?: -1

    override suspend fun loop(): Int {
        selectSkill()
        val obj = getCauldron()
        if (obj != null && obj.interact(ObjectAction.OBJECT1)) {
            currentCauldronId = obj.id
            delayUntil(300000) {
                currentCauldronId != getCauldron()?.id
            }
        }
        return 600
    }

    private fun getDirection(): String {
        if (Widgets.isOpen(596)) {
            val text = Widgets.getGroupById(596).getWidget(0).getChild(1).text
            Debug.log(text)
            return text.split(":")[1].trim().lowercase()
        } else {
            Debug.log("Interface not open.")
        }
        return "south"
    }

    private fun getCauldron(): WorldObject? {
        return when (getDirection()) {
            "north" -> WorldHelper.closestObject { it.id == 119626 }
            "south" -> WorldHelper.closestObject { it.id == 119628 }
            "east" -> WorldHelper.closestObject { it.id == 119627 }
            else -> null
        }
    }

    private suspend fun selectSkill() {
        if (Widgets.isOpen(596)) {
            if(selectedSkill == 0 && skill === Skill.HERBLORE) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059472)
                delayUntil { skill === Skill.COOKING }
            } else if(selectedSkill == 1 && skill == Skill.COOKING) {
                ActionHelper.menu(MenuAction.WIDGET, 1, -1, 39059468)
                delayUntil { skill === Skill.HERBLORE }
            }
        }
    }

    override fun onLoad() {
    }

    override suspend fun paint() {
        selectedSkill = ImGui.combo("Skill", skillOptions, selectedSkill)
    }

    override suspend fun paintOverlay() {
    }
}