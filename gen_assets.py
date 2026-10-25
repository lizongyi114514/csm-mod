#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
gen_assets.py — 从 data/csm/skills 的 JSON 反推生成：
  assets/csm/lang/en_us.json / zh_cn.json   （技能名、形态名、键位、提示）
  assets/csm/sounds.json                    （每个招式音效映射到原版音效文件，先能出声）

跑完还会把 data/csm/skills 复制进 src/main/resources。
"""
import json
import os
import shutil

ROOT = os.path.dirname(os.path.abspath(__file__))
SKILLS = os.path.join(ROOT, "data", "csm", "skills")
RES = os.path.join(ROOT, "src", "main", "resources")

NAMES = {
    "denji/chainsaw_rush": ("链锯冲锋", "Chainsaw Rush"),
    "denji/double_saw": ("双锯斩", "Double Saw"),
    "denji/head_saw_lunge": ("头锯突进", "Head Saw Lunge"),
    "denji/saw_throw": ("飞锯", "Saw Throw"),
    "denji/saw_uppercut": ("上挑锯", "Saw Uppercut"),
    "denji/overdrive": ("全开·暴走", "Overdrive"),
    "denji/blood_guzzle": ("吸血再生", "Blood Guzzle"),
    "denji/guts_finisher": ("剖腹终结", "Guts Finisher"),
    "pochita/hell_hero_dash": ("地狱英雄突进", "Hell Hero Dash"),
    "pochita/four_arm_flurry": ("四臂乱舞", "Four-Arm Flurry"),
    "pochita/concept_erase": ("概念抹除", "Concept Erasure"),
    "pochita/engine_roar": ("引擎咆哮", "Engine Roar"),
    "power/blood_hammer": ("血锤", "Blood Hammer"),
    "power/blood_spear": ("血矛", "Blood Spear"),
    "power/blood_sword": ("血剑", "Blood Sword"),
    "power/blood_shield": ("血盾", "Blood Shield"),
    "power/blood_rain": ("血雨", "Blood Rain"),
    "power/blood_regenerate": ("血再生", "Blood Regeneration"),
    "aki/katana_draw": ("拔刀", "Iai Draw"),
    "aki/katana_combo": ("三连斩", "Triple Slash"),
    "aki/fox_contract": ("狐之契约", "Fox Contract"),
    "aki/curse_nail": ("诅咒钉", "Curse Nail"),
    "aki/future_sight": ("未来视", "Future Sight"),
    "aki/nail_volley": ("钉雨", "Nail Volley"),
    "makima/chain_pull": ("锁链牵引", "Chain Pull"),
    "makima/dominate": ("支配", "Domination"),
    "makima/sacrifice": ("替身", "Sacrifice"),
    "makima/rat_swarm": ("鼠群", "Rat Swarm"),
    "makima/snipe": ("远程狙击", "Sniping"),
    "makima/command": ("命令", "Command"),
    "reze/bomb_kick": ("爆炸踢", "Bomb Kick"),
    "reze/bomb_punch": ("爆破拳", "Bomb Punch"),
    "reze/detonate": ("引爆", "Detonate"),
    "reze/air_burst": ("空中引爆", "Air Burst"),
    "reze/bomb_volley": ("投掷炸弹", "Bomb Volley"),
    "reze/self_destruct": ("自爆", "Self Destruct"),
    "reze/regenerate": ("再生", "Regeneration"),
}

FORMS = {
    "hunter": ("恶魔猎人", "Devil Hunter"),
    "denji": ("电锯", "Chainsaw"),
    "pochita": ("英雄之恶魔", "Hero of Hell"),
    "power": ("血之魔人", "Blood Fiend"),
    "makima": ("支配恶魔", "Control Devil"),
    "reze": ("炸弹混血", "Bomb Hybrid"),
}

# 关键词 -> 原版音效文件（sounds.json 的 name 就是文件路径）
SOUND_MAP = [
    ("saw", "minecraft:block.grindstone.use"),
    ("rev", "minecraft:block.grindstone.use"),
    ("engine", "minecraft:block.piston.extend"),
    ("erase", "minecraft:entity.enderman.teleport"),
    ("roar", "minecraft:entity.ender_dragon.growl"),
    ("hammer", "minecraft:block.anvil.land"),
    ("blood", "minecraft:entity.player.hurt"),
    ("katana", "minecraft:entity.player.attack.sweep"),
    ("sword", "minecraft:entity.player.attack.sweep"),
    ("slash", "minecraft:entity.player.attack.sweep"),
    ("nail", "minecraft:block.chain.break"),
    ("curse", "minecraft:block.chain.break"),
    ("chain", "minecraft:block.chain.break"),
    ("fox", "minecraft:entity.wolf.howl"),
    ("snipe", "minecraft:entity.arrow.shoot"),
    ("rats", "minecraft:entity.rabbit.ambient"),
    ("rain", "minecraft:weather.rain"),
    ("shield", "minecraft:item.shield.block"),
    ("dominate", "minecraft:entity.elder_guardian.curse"),
    ("command", "minecraft:entity.evoker.prepare_wololo"),
    ("sacrifice", "minecraft:entity.witch.drink"),
    ("heal", "minecraft:block.brewing_stand.brew"),
    ("guzzle", "minecraft:entity.generic.drink"),
    ("throw", "minecraft:entity.snowball.throw"),
    ("dash", "minecraft:entity.ravager.roar"),
    ("four", "minecraft:block.grindstone.use"),
    ("spark", "minecraft:block.grindstone.use"),
]

BASE = {
    "key.csm": ("电锯人", "Chainsaw Man"),
    "key.csm.skill1": ("技能 1", "Skill 1"),
    "key.csm.skill2": ("技能 2", "Skill 2"),
    "key.csm.skill3": ("技能 3", "Skill 3"),
    "key.csm.skill4": ("技能 4", "Skill 4"),
    "key.csm.form": ("变身 / 解除", "Transform / Revert"),
    "csm.message.requirement": ("条件不足：形态、等级或契约未满足", "Requirements not met: form, level or contract"),
    "csm.message.no_energy": ("血液不足", "Not enough blood"),
    "death.attack.csm.technique": ("%1$s 被锯成了两半", "%1$s was cut in half"),
    "item.csm.chainsaw": ("电锯", "Chainsaw"),
    "csm.contract.fox": ("狐", "Fox"),
    "csm.contract.curse": ("咒", "Curse"),
    "csm.contract.future": ("未", "Future"),
    "csm.contract.fox.paid": ("狐狸咬下了一口（已用 %s/3，剩 %s 次）", "The fox takes its bite (%s/3 used, %s left)"),
    "csm.contract.fox.revoked": ("狐之契约已作废", "The fox contract is spent"),
    "csm.contract.curse.backlash": ("诅咒反噬：第 %s 层", "Curse backlash: stack %s"),
    "csm.contract.future.paid": ("未来被预支了一次（最大生命 %s）", "A future was spent (max health %s)"),
    "csm.contract.unavailable": ("契约次数已用尽", "This contract is spent"),
    "csm.contract.reset": ("契约账本已重置", "Contract ledger reset"),
    "entity.csm.katana_man": ("武士刀男", "Katana Man"),
    "key.csm.codex": ("图鉴", "Codex"),
    "csm.codex.title": ("电锯人图鉴", "Chainsaw Man Codex"),
    "csm.codex.hint": ("点击左侧分区 · 滚轮翻页 · Esc 关闭", "Click a section · scroll · Esc to close"),
    "entity.csm.gun_devil": ("枪之恶魔", "Gun Devil"),
    "csm.boss.katana.frenzy": ("武士刀男：刀身开始发烫……", "Katana Man: the blade is heating up..."),
    "itemGroup.csm": ("电锯人", "Chainsaw Man"),
}


def pick_sound(name: str) -> str:
    for key, value in SOUND_MAP:
        if key in name:
            return value
    return "minecraft:entity.player.attack.sweep"


def main():
    zh = {k: v[0] for k, v in BASE.items()}
    en = {k: v[1] for k, v in BASE.items()}
    sounds = {}
    skills_src = os.path.join(RES, "data", "csm", "skills")

    count = 0
    for school in sorted(os.listdir(SKILLS)):
        folder = os.path.join(SKILLS, school)
        if not os.path.isdir(folder):
            continue
        for fn in sorted(os.listdir(folder)):
            if not fn.endswith(".json"):
                continue
            data = json.load(open(os.path.join(folder, fn), encoding="utf-8"))
            key_path = f"{school}/{fn[:-5]}"
            zh_name, en_name = NAMES.get(key_path, (fn[:-5], fn[:-5]))
            key = "skill.csm." + key_path.replace("/", ".")
            zh[key] = zh_name
            en[key] = en_name
            snd = data.get("visual", {}).get("sound")
            if snd:
                short = snd.split(":")[-1].replace("/", ".")
                key = snd.split(":")[-1]
                sounds[key] = {"subtitle": "subtitles.csm." + short,
                               "sounds": [{"name": pick_sound(snd)}]}
                zh.setdefault("subtitles.csm." + short, zh_name)
                en.setdefault("subtitles.csm." + short, en_name)
            count += 1

    for form, (zh_name, en_name) in FORMS.items():
        zh["csm.form." + form] = zh_name
        en["csm.form." + form] = en_name

    os.makedirs(os.path.join(RES, "assets", "csm", "lang"), exist_ok=True)
    for lang, data in (("zh_cn", zh), ("en_us", en)):
        with open(os.path.join(RES, "assets", "csm", "lang", lang + ".json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, ensure_ascii=False, indent=2, sort_keys=True)
            f.write("\n")

    with open(os.path.join(RES, "assets", "csm", "sounds.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(sounds, f, ensure_ascii=False, indent=2, sort_keys=True)
        f.write("\n")

    # 复制技能数据进 resources
    if os.path.isdir(skills_src):
        shutil.rmtree(skills_src)
    shutil.copytree(SKILLS, skills_src)

    print(f"技能 {count} 条，语言键 {len(zh)} 条，音效 {len(sounds)} 条")
    print("写入:", os.path.join(RES, "assets", "csm"))


if __name__ == "__main__":
    main()
