package com.example.csm.skill;

import java.util.List;

/** 键位槽 -> 招式。槽位由 JSON 的 "slot" 字段决定，数据说了算。 */
public final class SkillSlots {

    public static final int SLOTS = 4;

    private SkillSlots() {
    }

    public static List<SkillDefinition> skills(String school) {
        return SkillManager.bySchool(school);
    }

    public static SkillDefinition slot(String school, int index) {
        if (index < 0 || index >= SLOTS) {
            return null;
        }
        for (SkillDefinition definition : SkillManager.bySchool(school)) {
            if (definition.slot() == index) {
                return definition;
            }
        }
        return null;
    }
}
