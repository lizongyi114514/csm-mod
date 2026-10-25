#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
gen_skills.py — 生成《电锯人》模组的招式数据（data/csm/skills/<school>/<name>.json）

设计约束（来自 skill.schema.json）：
  - id 必须等于 <modid>:<相对路径>
  - cast 总时长 < cooldown，否则会被 validate_skills.py 警告"可无缝连发"
  - 有前摇的重击，避免"无前摇高伤害"

数值口径：原版玩家 20 点血 = 10 颗心；普通怪 10~30 血，Boss 150~400 血。
"""
from __future__ import annotations

import json
import os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data", "csm", "skills")
MODID = "csm"

# school, name, tier, dmg, windup, active, recovery, cd, energy, shape, range, angle|width, kb, effects, anim, particle, sound, shake, requires, note
S = [
    # ---------------- 电次 Denji（电锯形态） ----------------
    ("denji", "chainsaw_rush", 1, 12, 6, 6, 10, 50, 12, "cone", 4.5, 60, 0.4,
     [], "atk_chainsaw_rush", "csm:chainsaw_spark", "csm:chainsaw_rev", 0.15,
     {"level": 1, "flag": "csm:form_chainsaw"}, "链锯冲锋：起手短、可接双锯斩"),
    ("denji", "double_saw", 2, 18, 8, 4, 12, 80, 18, "cone", 3.2, 90, 0.6,
     [{"id": "csm:bleeding", "duration": 100, "amplifier": 0}],
     "atk_double_saw", "csm:blood_spray", "csm:saw_hit", 0.25,
     {"level": 2, "flag": "csm:form_chainsaw"}, "双锯斩：主力输出，叠流血"),
    ("denji", "head_saw_lunge", 2, 16, 5, 5, 14, 90, 20, "ray", 6.0, 1.4, 0.8,
     [], "atk_head_lunge", "csm:chainsaw_trail", "csm:engine_roar", 0.3,
     {"level": 3, "flag": "csm:form_chainsaw"}, "头锯突进：位移+穿透"),
    ("denji", "saw_throw", 2, 14, 7, 3, 16, 110, 22, "ray", 14.0, 0.8, 0.3,
     [{"id": "csm:bleeding", "duration": 60, "amplifier": 0}],
     "atk_saw_throw", "csm:saw_spin", "csm:saw_throw", 0.2,
     {"level": 3, "flag": "csm:form_chainsaw"}, "飞锯：远程消耗"),
    ("denji", "saw_uppercut", 2, 15, 7, 3, 18, 120, 20, "cone", 3.0, 45, 1.4,
     [], "atk_saw_uppercut", "csm:spark_arc", "csm:saw_hit", 0.3,
     {"level": 4, "flag": "csm:form_chainsaw"}, "上挑锯：把目标挑空"),
    ("denji", "overdrive", 3, 0, 10, 1, 10, 400, 40, "sphere", 0.5, 0.0, 0,
     [{"id": "csm:overdrive", "duration": 160, "amplifier": 0}],
     "atk_overdrive", "csm:steam_burst", "csm:engine_overdrive", 0.35,
     {"level": 6, "flag": "csm:form_chainsaw"}, "全开·暴走：攻速/伤害提升 8 秒"),
    ("denji", "blood_guzzle", 2, 0, 20, 1, 20, 300, 0, "sphere", 0.5, 0.0, 0,
     [{"id": "csm:blood_regeneration", "duration": 120, "amplifier": 0}],
     "atk_drink", "csm:blood_mist", "csm:guzzle", 0,
     {"level": 5, "flag": "csm:form_chainsaw"}, "吸血再生：喝血回血，消耗血槽"),
    ("denji", "guts_finisher", 3, 34, 16, 4, 24, 360, 45, "cone", 3.5, 120, 1.0,
     [{"id": "csm:bleeding", "duration": 200, "amplifier": 1}],
     "atk_guts_finisher", "csm:blood_burst", "csm:finisher", 0.6,
     {"level": 8, "flag": "csm:form_chainsaw"}, "剖腹终结：大范围横斩"),

    # ---------------- 波奇塔 Pochita（英雄之恶魔形态） ----------------
    ("pochita", "hell_hero_dash", 2, 20, 6, 6, 12, 120, 25, "ray", 8.0, 2.0, 0.8,
     [], "atk_hero_dash", "csm:red_trail", "csm:hero_rev", 0.4,
     {"level": 10, "flag": "csm:form_pochita"}, "地狱英雄突进：四臂前冲"),
    ("pochita", "four_arm_flurry", 3, 26, 10, 8, 18, 200, 35, "sphere", 4.0, 0.0, 0.9,
     [], "atk_four_arm", "csm:slash_storm", "csm:four_saw", 0.5,
     {"level": 10, "flag": "csm:form_pochita"}, "四臂乱舞：环身多段"),
    ("pochita", "concept_erase", 3, 60, 20, 2, 30, 600, 60, "single", 2.5, 0.0, 0,
     [{"id": "csm:erased", "duration": 40, "amplifier": 0}],
     "atk_erase", "csm:erase_void", "csm:erase", 0.8,
     {"level": 12, "flag": "csm:form_pochita"}, "概念抹除：处决低血量目标，无掉落"),
    ("pochita", "engine_roar", 3, 6, 8, 2, 20, 300, 30, "sphere", 6.0, 0.0, 2.0,
     [{"id": "csm:terror", "duration": 200, "amplifier": 0}],
     "atk_roar", "csm:shockwave", "csm:roar", 0.6,
     {"level": 10, "flag": "csm:form_pochita"}, "引擎咆哮：击退+恐惧"),

    # ---------------- 帕瓦 Power（血之魔人） ----------------
    ("power", "blood_hammer", 2, 22, 12, 3, 20, 100, 25, "cone", 3.0, 90, 1.2,
     [], "atk_blood_hammer", "csm:blood_splash", "csm:blood_hammer", 0.45,
     {"level": 3, "flag": "csm:form_blood"}, "血锤：重击，破防"),
    ("power", "blood_spear", 2, 16, 6, 3, 14, 80, 18, "ray", 8.0, 1.0, 0.5,
     [{"id": "csm:bleeding", "duration": 120, "amplifier": 0}],
     "atk_blood_spear", "csm:blood_trail", "csm:blood_spear", 0.25,
     {"level": 2, "flag": "csm:form_blood"}, "血矛：直线穿透"),
    ("power", "blood_sword", 1, 11, 4, 3, 8, 40, 10, "cone", 2.8, 70, 0.3,
     [], "atk_blood_sword", "csm:blood_arc", "csm:sword_swing", 0.12,
     {"level": 1, "flag": "csm:form_blood"}, "血剑：平A三连的基础段"),
    ("power", "blood_shield", 2, 0, 6, 1, 8, 240, 20, "sphere", 0.5, 0.0, 0,
     [{"id": "csm:blood_shield", "duration": 200, "amplifier": 0}],
     "atk_blood_shield", "csm:blood_ring", "csm:shield_up", 0.1,
     {"level": 4, "flag": "csm:form_blood"}, "血盾：吸收伤害，破盾反噬"),
    ("power", "blood_rain", 3, 9, 10, 4, 16, 160, 28, "sphere", 5.0, 0.0, 0.2,
     [{"id": "csm:bleeding", "duration": 160, "amplifier": 0}, {"id": "minecraft:slowness", "duration": 80, "amplifier": 1}],
     "atk_blood_rain", "csm:blood_rain", "csm:rain", 0.2,
     {"level": 5, "flag": "csm:form_blood"}, "血雨：范围减速+流血"),
    ("power", "blood_regenerate", 2, 0, 18, 1, 18, 280, 0, "sphere", 0.5, 0.0, 0,
     [{"id": "csm:blood_regeneration", "duration": 100, "amplifier": 0}],
     "atk_blood_heal", "csm:red_mist", "csm:heal", 0,
     {"level": 3, "flag": "csm:form_blood"}, "血再生：把血槽转成生命"),

    # ---------------- 蕾塞 Reze（炸弹混血） ----------------
    ("reze", "bomb_kick", 1, 12, 6, 4, 12, 50, 12, "cone", 3.0, 70, 0.6,
     [], "atk_bomb_kick", "csm:spark_burst", "csm:bomb_kick", 0.3,
     {"level": 1, "flag": "csm:form_bomb"}, "爆炸踢：叠 1 层炸弹"),
    ("reze", "bomb_punch", 1, 9, 4, 3, 8, 40, 8, "ray", 3.5, 1.2, 0.3,
     [], "atk_bomb_punch", "csm:spark_small", "csm:bomb_punch", 0.2,
     {"level": 1, "flag": "csm:form_bomb"}, "爆破拳：快，叠 1 层"),
    ("reze", "detonate", 2, 6, 8, 2, 12, 90, 20, "sphere", 6.0, 0.0, 0.4,
     [], "atk_detonate", "csm:explosion_ring", "csm:detonate", 0.6,
     {"level": 4, "flag": "csm:form_bomb"}, "引爆：每层 +5 伤害"),
    ("reze", "air_burst", 2, 14, 10, 3, 16, 120, 22, "sphere", 4.0, 0.0, 0.9,
     [], "atk_air_burst", "csm:air_blast", "csm:air_burst", 0.4,
     {"level": 3, "flag": "csm:form_bomb"}, "空中引爆：位移 + 范围"),
    ("reze", "bomb_volley", 2, 8, 7, 3, 12, 80, 16, "ray", 12.0, 1.0, 0.3,
     [], "atk_bomb_volley", "csm:grenade_trail", "csm:bomb_throw", 0.25,
     {"level": 3, "flag": "csm:form_bomb"}, "投掷炸弹：远程叠层"),
    ("reze", "self_destruct", 3, 30, 20, 2, 20, 500, 45, "sphere", 8.0, 0.0, 1.2,
     [], "atk_self_destruct", "csm:mega_explosion", "csm:self_destruct", 0.9,
     {"level": 8, "flag": "csm:form_bomb"}, "自爆：大范围高伤"),
    ("reze", "regenerate", 2, 0, 18, 1, 18, 280, 0, "sphere", 0.5, 0.0, 0,
     [{"id": "csm:blood_regeneration", "duration": 100, "amplifier": 0}],
     "atk_regenerate", "csm:red_mist", "csm:heal", 0,
     {"level": 3, "flag": "csm:form_bomb"}, "再生：血转生命"),

    # ---------------- 早川秋 Aki ----------------
    ("aki", "katana_draw", 1, 15, 4, 2, 10, 60, 10, "ray", 5.0, 1.0, 0.4,
     [], "atk_katana_draw", "csm:slash_line", "csm:katana_draw", 0.2,
     {"level": 1, "flag": "csm:form_hunter"}, "拔刀：极短前摇"),
    ("aki", "katana_combo", 2, 9, 4, 6, 10, 70, 14, "cone", 3.0, 80, 0.2,
     [{"id": "csm:bleeding", "duration": 80, "amplifier": 0}],
     "atk_katana_combo", "csm:slash_arc", "csm:katana_swing", 0.15,
     {"level": 2, "flag": "csm:form_hunter"}, "三连斩：动作中段可取消"),
    ("aki", "fox_contract", 3, 24, 14, 4, 22, 300, 30, "cone", 5.0, 60, 1.0,
     [{"id": "csm:devoured", "duration": 40, "amplifier": 0}],
     "atk_fox", "csm:fox_bite", "csm:fox_howl", 0.5,
     {"level": 6, "flag": "csm:contract_fox", "contract": "fox"}, "狐之契约：巨型狐首咬合"),
    ("aki", "curse_nail", 2, 8, 8, 2, 12, 90, 12, "ray", 10.0, 0.8, 0,
     [{"id": "csm:curse_mark", "duration": 600, "amplifier": 0}],
     "atk_nail", "csm:curse_spark", "csm:nail_hit", 0.2,
     {"level": 4, "flag": "csm:contract_curse", "contract": "curse"}, "诅咒钉：叠三层触发诅咒"),
    ("aki", "future_sight", 2, 0, 6, 1, 10, 360, 15, "sphere", 0.5, 0.0, 0,
     [{"id": "csm:foresight", "duration": 120, "amplifier": 0}],
     "atk_foresight", "csm:eye_glow", "csm:foresight", 0,
     {"level": 5, "flag": "csm:contract_future", "contract": "future"}, "未来视：短时间完美闪避"),
    ("aki", "nail_volley", 2, 6, 8, 4, 12, 140, 18, "cone", 6.0, 70, 0.1,
     [{"id": "csm:curse_mark", "duration": 300, "amplifier": 0}],
     "atk_nail_volley", "csm:nail_rain", "csm:nail_volley", 0.15,
     {"level": 5, "flag": "csm:contract_curse", "contract": "curse"}, "钉雨：一次挂多个诅咒标记"),

    # ---------------- 玛奇玛 Makima（支配恶魔） ----------------
    ("makima", "chain_pull", 1, 6, 6, 2, 10, 80, 10, "ray", 12.0, 0.8, 0,
     [{"id": "csm:bound", "duration": 60, "amplifier": 0}],
     "atk_chain_pull", "csm:chain", "csm:chain_rattle", 0.1,
     {"level": 1, "flag": "csm:form_control"}, "锁链牵引：拉近/打断"),
    ("makima", "dominate", 2, 0, 10, 2, 14, 200, 20, "single", 3.0, 0.0, 0,
     [{"id": "csm:dominated", "duration": 240, "amplifier": 0}],
     "atk_dominate", "csm:chain_bind", "csm:dominate", 0.15,
     {"level": 4, "flag": "csm:form_control"}, "支配：把目标变成傀儡"),
    ("makima", "sacrifice", 3, 0, 8, 1, 12, 400, 35, "sphere", 0.5, 0.0, 0,
     [{"id": "csm:sacrifice_ward", "duration": 200, "amplifier": 0}],
     "atk_sacrifice", "csm:red_string", "csm:sacrifice", 0.2,
     {"level": 7, "flag": "csm:form_control"}, "替身：伤害转移给被支配者"),
    ("makima", "rat_swarm", 2, 7, 10, 4, 16, 140, 22, "sphere", 6.0, 0.0, 0.1,
     [{"id": "csm:bleeding", "duration": 80, "amplifier": 0}],
     "atk_rats", "csm:rat_swarm", "csm:rats", 0.12,
     {"level": 5, "flag": "csm:form_control"}, "鼠群：范围持续伤害"),
    ("makima", "snipe", 3, 30, 20, 1, 20, 400, 50, "ray", 24.0, 0.6, 0.5,
     [], "atk_snipe", "csm:bullet_line", "csm:snipe", 0.7,
     {"level": 8, "flag": "csm:form_control"}, "远程狙击：超远直线，需读条"),
    ("makima", "command", 3, 0, 12, 2, 16, 300, 25, "sphere", 8.0, 0.0, 0,
     [{"id": "csm:stunned", "duration": 100, "amplifier": 0}],
     "atk_command", "csm:command_ring", "csm:command", 0.3,
     {"level": 6, "flag": "csm:form_control"}, "命令：范围定身"),
]

TIER_COLOR = {1: "基础", 2: "主力", 3: "大招"}

# 少数招式需要在结算时做特殊处理（数据里用 "special" 表达）
SPECIALS = {
    "reze/bomb_kick": "prime",
    "reze/bomb_punch": "prime",
    "reze/bomb_volley": "prime",
    "reze/detonate": "detonate",
    "reze/self_destruct": "detonate",
}


def build(entry, slot=99):
    (school, name, tier, dmg, windup, active, recovery, cd, energy,
     shape, rng, second, kb, effects, anim, particle, sound, shake, requires, note) = entry

    hit = {"shape": shape, "damage": dmg, "knockback": kb, "damage_type": "csm:technique"}
    if shape == "cone":
        hit["range"] = rng
        hit["angle"] = second
    elif shape == "ray":
        hit["range"] = rng
        hit["width"] = second
    elif shape == "sphere":
        hit["range"] = rng
    else:
        hit["range"] = rng
    if effects:
        hit["effects"] = effects
    if dmg > 0:
        hit["once_per_target"] = True

    visual = {"animation_player": anim, "particle": particle, "sound": sound}
    if shake:
        visual["screen_shake"] = shake
    if shape in ("ray", "cone") and dmg >= 10:
        visual["trail"] = {"step": 0.4, "count": 24}

    return {
        "id": f"{MODID}:{school}/{name}",
        "slot": slot,
        "special": SPECIALS.get(f"{school}/{name}", ""),
        "note": note,
        "school": school,
        "tier": tier,
        "cost": {"energy": energy, "cooldown": cd},
        "cast": {"windup": windup, "active": active, "recovery": recovery,
                 "move_lock": 0.35 if windup >= 10 else 0.6,
                 "invulnerable": False, "interruptible": recovery > 0},
        "hit": hit,
        "visual": visual,
        "requires": requires,
    }


def main():
    rows = []
    used = {}
    for entry in S:
        school_name = entry[0]
        index = used.get(school_name, 0)
        used[school_name] = index + 1
        slot = index if index < 4 else 99
        defn = build(entry, slot)
        school, name = entry[0], entry[1]
        d = os.path.join(OUT, school)
        os.makedirs(d, exist_ok=True)
        with open(os.path.join(d, name + ".json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(defn, f, ensure_ascii=False, indent=2)
            f.write("\n")
        cast_total = entry[4] + entry[5] + entry[6]
        dps = (entry[3] / ((entry[7] + 20) / 20.0)) if entry[3] else 0
        rows.append((school, name, TIER_COLOR[entry[2]], entry[3], cast_total, entry[7], entry[8], round(dps, 2)))

    print(f"生成 {len(rows)} 个招式 -> {OUT}\n")
    print(f"{'school':10s} {'name':22s} {'tier':4s} {'dmg':>4s} {'cast':>4s} {'cd':>4s} {'mp':>3s} {'DPS':>5s}")
    for r in rows:
        print(f"{r[0]:10s} {r[1]:22s} {r[2]:4s} {r[3]:4d} {r[4]:4d} {r[5]:4d} {r[6]:3d} {r[7]:5.2f}")


if __name__ == "__main__":
    main()
