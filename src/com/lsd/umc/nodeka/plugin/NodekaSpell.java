package com.lsd.umc.nodeka.plugin;

import java.util.HashMap;

abstract class NodekaSpell {
    abstract HashMap<String, Boolean> getBuffs();
    abstract void setSpells(HashMap<String, Boolean> buffs);
    abstract void processSpells();
}
