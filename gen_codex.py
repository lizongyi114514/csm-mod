#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
gen_codex.py — 生成游戏内图鉴内容（assets/csm/codex/entries_<lang>.json）。

数据来源：
  招式  <= data/csm/skills/**/*.json（伤害/冷却/血耗/note 全自动）
  名字  <= assets/csm/lang/<lang>.json（已由 gen_assets.py 生成）
  角色/契约/Boss 的文字在下面的表里手写。
"""
import glob
import json
import os

ROOT = os.path.dirname(os.path.abspath(__file__))
SKILLS = os.path.join(ROOT, "data", "csm", "skills")
RES = os.path.join(ROOT, "src", "main", "resources")
OUT = os.path.join(RES, "assets", "csm", "codex")

SCHOOL_ORDER = ["denji", "pochita", "power", "aki", "makima", "reze"]
SCHOOL_TITLE = {
    "denji": ("电次", "Denji"),
    "pochita": ("波奇塔", "Pochita"),
    "power": ("帕瓦", "Power"),
    "aki": ("早川秋", "Aki"),
    "makima": ("玛奇玛", "Makima"),
    "reze": ("蕾塞", "Reze"),
}

FORMS = [
    ("hunter", "恶魔猎人", "Devil Hunter",
     ["键位 1-4 对应四个技能槽，R 变身 / 解除，C 打开这本图鉴。",
      "血槽（数据层叫 energy）就是你的资源：放招要花，命中会回，脱战慢慢涨。"]),
    ("denji", "电锯", "Chainsaw",
     ["拉绳变身 60 秒，锯刃从头顶与双臂长出。",
      "变身期攻速与伤害都更高，但血耗也更快——记得留 F 位吸血。"]),
    ("pochita", "英雄之恶魔", "Hero of Hell",
     ["电锯形态下血量低于 20% 时按 R 才会出现，持续 25 秒。",
      "结束后直接回人形并虚弱 15 秒，是一次性的翻盘机会。"]),
    ("power", "血之魔人", "Blood Fiend",
     ["血锤破防、血矛穿透、血剑连段，血盾吃伤害。",
      "血雨是范围减速，适合把一群怪按在地上再慢慢收。"]),
    ("aki", "早川秋", "Aki",
     ["三条契约：狐狸（3 次）、诅咒（叠层）、未来（预支生命）。",
      "每次用狐狸都会永久掉攻速；第三次之后契约作废，技能直接变灰。"]),
    ("makima", "支配恶魔", "Control Devil",
     ["伤害低，但牵引 → 定身 → 狙击可以一个人打完一套。",
      "支配是控制链的起点：先把目标变成傀儡，再用替身把伤害转出去。"]),
    ("reze", "炸弹混血", "Bomb Hybrid",
     ["1/2 叠炸弹层数（最多 5 层），3 引爆，每层 +5 伤害。",
      "5 层引爆 = 31 点，但要 3 秒铺垫——这是她全部的取舍。"]),
]

CONTRACTS = [
    ("狐", "Fox",
     ["3 次。每次使用攻速 -10%。", "第 3 次之后契约作废，再额外 -20%，并且不能再召唤。"]),
    ("咒", "Curse",
     ["无限次。每层最大生命 -1。", "每叠到第 3 层，自己吃 6 点真实伤害。"]),
    ("未", "Future",
     ["无限次。每次预支 5 点最大生命，不恢复。", "换来 6 秒的完美闪避窗口——用来读 Boss 前摇。"]),
]

BOSSES = [
    ("武士刀男", "Katana Man",
     ["血量 200。150 / 120 是阶段线。",
      "一阶段居合：24 tick 前摇，12 格直线斩，砍完 16 tick 硬直。",
      "二阶段三连突进：每次 6 点，第三次后 20 tick 硬直。",
      "三阶段暴走：前摇压到 14 tick，追加 6 格刀风。",
      "打法：前摇时侧移，硬直时贴脸输出。"]),
    ("枪之恶魔", "Gun Devil",
     ["血量 400。240 / 120 是阶段线。",
      "一阶段单发点射：12 tick 前摇，24 格直线。",
      "二阶段三连扇形：8 tick 前摇，覆盖一个扇形。",
      "三阶段标记轰炸：40 tick 前摇，落点固定，走出 4 格就能躲。",
      "打法：每次开火后它会换弹——那是唯一的输出窗口。"]),
]


def load_names(lang: str) -> dict:
    path = os.path.join(RES, "assets", "csm", "lang", lang + ".json")
    if not os.path.exists(path):
        return {}
    return json.load(open(path, encoding="utf-8"))


def skill_entries(lang: str, names: dict):
    by_school = {}
    for path in glob.glob(os.path.join(SKILLS, "*", "*.json")):
        d = json.load(open(path, encoding="utf-8"))
        by_school.setdefault(d["school"], []).append(d)

    sections = []
    for school in SCHOOL_ORDER:
        skills = sorted(by_school.get(school, []), key=lambda s: (s.get("slot", 99), s["id"]))
        entries = []
        for s in skills:
            name_key = "skill.csm." + s["id"].split(":")[1].replace("/", ".")
            title = names.get(name_key, s["id"].split("/")[-1])
            slot = s.get("slot", 99)
            key_hint = f"键 {slot + 1}" if slot < 4 else "命令"
            stats = (f"{key_hint} · 伤害 {s['hit']['damage']:g} · 冷却 {s['cost']['cooldown'] / 20:g}s"
                     f" · 血耗 {s['cost']['energy']}")
            body = [stats]
            note = s.get("note", "") if lang == "zh_cn" else ""
            if note:
                body.append(note)
            if s.get("special") == "detonate":
                body.append("结算：按目标身上的炸弹层数加伤，并清空层数。")
            elif s.get("special") == "prime":
                body.append("结算：命中叠 1 层炸弹。")
            entries.append({"title": title, "body": body})
        sections.append({
            "title": SCHOOL_TITLE[school][0 if lang == "zh_cn" else 1],
            "entries": entries,
        })
    return sections


def main():
    os.makedirs(OUT, exist_ok=True)
    for lang in ("zh_cn", "en_us"):
        names = load_names(lang)
        zh = lang == "zh_cn"

        sections = []
        sections.append({
            "title": "总览" if zh else "Overview",
            "entries": [
                {"title": "怎么玩" if zh else "How to play",
                 "body": (["1-4 放技能，R 变身，C 打开图鉴。",
                           "形态决定你能用哪一套技能：/csm form <hunter|denji|pochita|power|makima|reze>。",
                           "伤害类型统一是 csm:technique，会被护甲与抗性正常减免。"]
                          if zh else
                          ["1-4 cast, R transform, C opens this codex.",
                           "Form decides your skill set: /csm form <hunter|denji|pochita|power|makima|reze>."])}
            ],
        })
        sections += skill_entries(lang, names)

        sections.append({
            "title": "契约" if zh else "Contracts",
            "entries": [
                {"title": (c[0] if zh else c[1]),
                 "body": (c[2] if zh else ["Costs are permanent.", "See the in-game tooltip for numbers."])}
                for c in CONTRACTS
            ],
        })
        sections.append({
            "title": "Boss",
            "entries": [
                {"title": (b[0] if zh else b[1]),
                 "body": (b[2] if zh else ["Read the telegraph, dodge sideways, punish the recovery."])}
                for b in BOSSES
            ],
        })
        sections.append({
            "title": "形态" if zh else "Forms",
            "entries": [
                {"title": (f[1] if zh else f[2]),
                 "body": (f[3] if zh else ["Form specific text is Chinese-only for now."])}
                for f in FORMS
            ],
        })

        payload = {"sections": sections}
        path = os.path.join(OUT, "entries_" + lang + ".json")
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            json.dump(payload, f, ensure_ascii=False, indent=2)
            f.write("\n")
        print(f"写入 {path}（{len(sections)} 个分区）")


if __name__ == "__main__":
    main()
