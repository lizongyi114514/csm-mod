#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
gen_player_anims.py — 为每个招式的 visual.animation_player 生成一份 playeranimator 动作。

每种判定形状对应一套"通用动作"（挥砍 / 突刺 / 砸地 / 指认 / 蓄力），
动作时长 = (windup + active + recovery) / 20 秒。
正式美术接手时，按文件名逐个替换即可，不用改代码。
"""
import glob
import json
import os

ROOT = os.path.dirname(os.path.abspath(__file__))
SKILLS = os.path.join(ROOT, "data", "csm", "skills")
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "csm", "player_animation")


def body(kind: str, length: float):
    if kind == "slash":
        return {
            "right_arm": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.15:.3f}": {"vector": [-150, 0, -20]},
                    f"{length * 0.45:.3f}": {"vector": [-20, 0, 15]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
            "torso": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.25:.3f}": {"vector": [0, -18, 0]},
                    f"{length * 0.5:.3f}": {"vector": [0, 22, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
        }
    if kind == "thrust":
        return {
            "right_arm": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.2:.3f}": {"vector": [-90, 0, 0]},
                    f"{length * 0.45:.3f}": {"vector": [-95, 0, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
            "left_arm": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.2:.3f}": {"vector": [-80, 0, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
            "torso": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.3:.3f}": {"vector": [-12, 0, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
        }
    if kind == "slam":
        return {
            "right_arm": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.25:.3f}": {"vector": [-175, 0, 0]},
                    f"{length * 0.5:.3f}": {"vector": [-10, 0, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
            "left_arm": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.25:.3f}": {"vector": [-175, 0, 0]},
                    f"{length * 0.5:.3f}": {"vector": [-10, 0, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
            "torso": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.25:.3f}": {"vector": [-14, 0, 0]},
                    f"{length * 0.5:.3f}": {"vector": [26, 0, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
        }
    if kind == "point":
        return {
            "right_arm": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.2:.3f}": {"vector": [-88, 0, 0]},
                    f"{length * 0.75:.3f}": {"vector": [-88, 0, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
            "head": {
                "rotation": {
                    "0.0": {"vector": [0, 0, 0]},
                    f"{length * 0.25:.3f}": {"vector": [-6, 0, 0]},
                    f"{length:.3f}": {"vector": [0, 0, 0]},
                }
            },
        }
    # channel：双手下垂、身体后仰
    return {
        "right_arm": {
            "rotation": {
                "0.0": {"vector": [0, 0, 0]},
                f"{length * 0.3:.3f}": {"vector": [20, 0, -35]},
                f"{length * 0.7:.3f}": {"vector": [15, 0, -30]},
                f"{length:.3f}": {"vector": [0, 0, 0]},
            }
        },
        "left_arm": {
            "rotation": {
                "0.0": {"vector": [0, 0, 0]},
                f"{length * 0.3:.3f}": {"vector": [20, 0, 35]},
                f"{length:.3f}": {"vector": [0, 0, 0]},
            }
        },
        "torso": {
            "rotation": {
                "0.0": {"vector": [0, 0, 0]},
                f"{length * 0.4:.3f}": {"vector": [10, 0, 0]},
                f"{length:.3f}": {"vector": [0, 0, 0]},
            }
        },
    }


def kind_for(skill: dict) -> str:
    hit = skill.get("hit", {})
    if hit.get("shape") == "sphere" and hit.get("range", 3) <= 1.0:
        return "channel"
    return {
        "cone": "slash",
        "ray": "thrust",
        "sphere": "slam",
        "single": "point",
    }.get(hit.get("shape", "single"), "slash")


def main():
    os.makedirs(OUT, exist_ok=True)
    written = 0
    names = set()

    for path in glob.glob(os.path.join(SKILLS, "*", "*.json")):
        skill = json.load(open(path, encoding="utf-8"))
        name = skill.get("visual", {}).get("animation_player", "")
        if not name:
            continue
        cast = skill["cast"]
        length = max(0.3, min(1.5, (cast["windup"] + cast["active"] + cast["recovery"]) / 20.0))
        anim = {
            "format_version": "1.8.0",
            "animations": {
                name: {
                    "animation_length": round(length, 3),
                    "loop": False,
                    "bones": body(kind_for(skill), length),
                }
            },
        }
        with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(anim, f, ensure_ascii=False, indent=2)
            f.write("\n")
        names.add(name)
        written += 1

    # 变身动作
    transform = {
        "format_version": "1.8.0",
        "animations": {
            "transform_pull": {
                "animation_length": 0.6,
                "loop": False,
                "bones": {
                    "right_arm": {
                        "rotation": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.2": {"vector": [-70, 0, -10]},
                            "0.45": {"vector": [-60, 0, -10]},
                            "0.6": {"vector": [0, 0, 0]},
                        }
                    },
                    "torso": {
                        "rotation": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.2": {"vector": [8, 0, 0]},
                            "0.45": {"vector": [-6, 0, 0]},
                            "0.6": {"vector": [0, 0, 0]},
                        }
                    },
                },
            }
        },
    }
    with open(os.path.join(OUT, "transform_pull.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(transform, f, ensure_ascii=False, indent=2)
        f.write("\n")

    print(f"生成 {written} 个招式动作（{len(names)} 个唯一文件名）+ transform_pull")


if __name__ == "__main__":
    main()
