package com.example.csm.form;

/**
 * 形态 = 招式学校（school）。招式 JSON 的 "school" 必须与这里的 school 一致，
 * "requires.flag" 必须与这里带 csm: 前缀的 flag 一致。
 */
public enum Form {

    HUNTER("hunter", "aki", "csm:form_hunter", 100, false, 0),
    DENJI("denji", "denji", "csm:form_chainsaw", 100, true, 1200),
    POCHITA("pochita", "pochita", "csm:form_pochita", 120, true, 500),
    POWER("power", "power", "csm:form_blood", 80, false, 0),
    MAKIMA("makima", "makima", "csm:form_control", 100, false, 0),
    REZE("reze", "reze", "csm:form_bomb", 100, false, 0);

    private final String id;
    private final String school;
    private final String flag;
    private final int maxEnergy;
    private final boolean timed;
    private final int duration;

    Form(String id, String school, String flag, int maxEnergy, boolean timed, int duration) {
        this.id = id;
        this.school = school;
        this.flag = flag;
        this.maxEnergy = maxEnergy;
        this.timed = timed;
        this.duration = duration;
    }

    public String id() {
        return id;
    }

    public String school() {
        return school;
    }

    public String flag() {
        return flag;
    }

    public int maxEnergy() {
        return maxEnergy;
    }

    public boolean timed() {
        return timed;
    }

    public int duration() {
        return duration;
    }

    public static Form byId(String id) {
        for (Form form : values()) {
            if (form.id.equals(id)) {
                return form;
            }
        }
        return HUNTER;
    }
}
