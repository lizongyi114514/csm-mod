package com.example.csm.contract;

/** 三条契约。maxUses <= 0 表示"次数无限，但每次都要付账"。 */
public enum Contract {

    FOX("fox", 3),
    CURSE("curse", -1),
    FUTURE("future", -1);

    private final String id;
    private final int maxUses;

    Contract(String id, int maxUses) {
        this.id = id;
        this.maxUses = maxUses;
    }

    public String id() {
        return id;
    }

    public int maxUses() {
        return maxUses;
    }

    public static Contract byId(String id) {
        for (Contract contract : values()) {
            if (contract.id.equals(id)) {
                return contract;
            }
        }
        return null;
    }
}
