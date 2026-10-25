#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
gen_textures.py — 生成两张占位贴图（64x64，索引色风格）：
  assets/csm/textures/item/chainsaw.png         GeckoLib 物品模型用
  assets/csm/textures/entity/chainsaw_form.png  玩家变身层用（UV 与 ChainsawFormLayer 对应）

正式美术接手时直接替换这两张图即可，尺寸必须保持 64x64。
"""
import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "csm", "textures")

METAL_DARK = (86, 92, 99, 255)
METAL = (154, 162, 171, 255)
METAL_LIGHT = (206, 212, 220, 255)
HANDLE = (58, 44, 38, 255)
BLOOD = (124, 20, 20, 255)
BLOOD_DARK = (78, 12, 12, 255)


def teeth(draw, x0, y0, w, h, vertical=True):
    """画锯齿：交替的浅色小格"""
    step = 3
    if vertical:
        for y in range(y0, y0 + h, step):
            draw.rectangle([x0, y, x0 + w - 1, y + 1], fill=METAL_LIGHT)
    else:
        for x in range(x0, x0 + w, step):
            draw.rectangle([x, y0, x + 1, y0 + h - 1], fill=METAL_LIGHT)


def item_texture(path):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # 手柄（左下）
    d.rectangle([2, 30, 12, 60], fill=HANDLE)
    d.rectangle([4, 34, 10, 56], fill=(92, 70, 58, 255))
    # 机身
    d.rectangle([14, 26, 40, 46], fill=METAL_DARK)
    d.rectangle([16, 28, 38, 36], fill=METAL)
    d.rectangle([16, 30, 38, 32], fill=BLOOD)
    # 导板
    d.rectangle([40, 32, 62, 38], fill=METAL)
    teeth(d, 42, 33, 18, 4, vertical=False)
    # 血迹
    d.rectangle([40, 39, 62, 41], fill=BLOOD_DARK)
    d.rectangle([44, 41, 46, 48], fill=BLOOD)
    img.save(path)


def form_texture(path):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # head_saw: uv(0,0) 尺寸 2x6x3 -> w=10 h=9
    d.rectangle([0, 0, 9, 8], fill=METAL_DARK)
    d.rectangle([1, 1, 8, 4], fill=METAL)
    # head_blade: uv(0,10) 尺寸 3x8x1 -> w=8 h=9
    d.rectangle([0, 10, 7, 18], fill=METAL)
    teeth(d, 1, 11, 6, 7, vertical=True)
    d.rectangle([0, 19, 7, 20], fill=BLOOD)

    # right_saw: uv(16,0) 尺寸 2x3x8 -> w=20 h=11
    d.rectangle([16, 0, 35, 10], fill=METAL_DARK)
    d.rectangle([17, 1, 34, 5], fill=METAL)
    # right_blade: uv(16,12) 尺寸 2.4x1.8x4 -> w~12 h~6
    d.rectangle([16, 12, 27, 17], fill=METAL_LIGHT)
    teeth(d, 17, 13, 10, 4, vertical=False)

    # left_saw: uv(32,0)
    d.rectangle([32, 0, 51, 10], fill=METAL_DARK)
    d.rectangle([33, 1, 50, 5], fill=METAL)
    # left_blade: uv(32,12)
    d.rectangle([32, 12, 43, 17], fill=METAL_LIGHT)
    teeth(d, 33, 13, 10, 4, vertical=False)

    # 血
    d.rectangle([0, 22, 7, 23], fill=BLOOD_DARK)
    img.save(path)


def katana_man_texture(path):
    """64x64 玩家皮肤布局：黑西装 + 白衬衫 + 红领带"""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    skin = (222, 182, 152, 255)
    hair = (24, 20, 20, 255)
    suit = (28, 28, 32, 255)
    shirt = (236, 236, 236, 255)
    tie = (150, 20, 30, 255)

    d.rectangle([0, 0, 31, 15], fill=hair)
    d.rectangle([8, 8, 15, 15], fill=skin)
    d.rectangle([8, 8, 15, 9], fill=hair)
    d.rectangle([10, 11, 11, 12], fill=(20, 20, 20, 255))
    d.rectangle([13, 11, 14, 12], fill=(20, 20, 20, 255))

    d.rectangle([16, 16, 39, 31], fill=suit)
    d.rectangle([20, 20, 27, 31], fill=shirt)
    d.rectangle([23, 21, 24, 27], fill=tie)

    d.rectangle([40, 16, 55, 31], fill=suit)
    d.rectangle([32, 48, 47, 63], fill=suit)
    d.rectangle([0, 16, 15, 31], fill=suit)
    d.rectangle([16, 48, 31, 63], fill=suit)

    # 刀（geo 里 katana 骨头的 uv = [56, 0]，2x20x1 -> 6x21）
    d.rectangle([56, 0, 61, 16], fill=(196, 202, 210, 255))
    d.rectangle([56, 17, 61, 20], fill=(92, 62, 40, 255))
    img.save(path)


def gun_devil_texture(path):
    """128x64：左半沿用玩家皮肤布局，右半放另一侧肢体与炮管"""
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    metal = (74, 78, 84, 255)
    dark = (40, 42, 46, 255)
    red = (200, 30, 30, 255)
    bone = (208, 200, 186, 255)

    d.rectangle([0, 0, 31, 15], fill=metal)
    d.rectangle([8, 8, 15, 15], fill=dark)
    d.rectangle([10, 10, 11, 11], fill=red)
    d.rectangle([13, 10, 14, 11], fill=red)

    d.rectangle([16, 16, 39, 31], fill=metal)
    d.rectangle([20, 20, 27, 31], fill=bone)
    d.rectangle([22, 24, 25, 30], fill=dark)

    d.rectangle([40, 16, 55, 31], fill=metal)
    d.rectangle([32, 48, 47, 63], fill=metal)
    d.rectangle([0, 16, 15, 31], fill=dark)
    d.rectangle([16, 48, 31, 63], fill=dark)

    # 右半张：另一侧手臂 / 腿
    d.rectangle([64, 0, 79, 19], fill=metal)
    d.rectangle([64, 32, 83, 62], fill=dark)

    # 炮管（uv = [0, 48]，4x4x10 -> 28x14）
    d.rectangle([0, 48, 27, 61], fill=(90, 94, 100, 255))
    d.rectangle([0, 52, 27, 56], fill=(52, 54, 58, 255))
    img.save(path)


def particle_textures():
    """8x8 粒子贴图：血点 / 火花 / 爆炸环"""
    out = os.path.join(ASSETS, "particle")
    os.makedirs(out, exist_ok=True)

    def dot(path, color, mode="ball"):
        img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        for x in range(8):
            for y in range(8):
                dx, dy = x - 3.5, y - 3.5
                dist2 = dx * dx + dy * dy
                if mode == "ball" and dist2 <= 9:
                    d.point((x, y), fill=color)
                elif mode == "ring" and 2.2 <= dist2 <= 11:
                    d.point((x, y), fill=color)
        if mode == "cross":
            d.rectangle([3, 1, 4, 6], fill=color)
            d.rectangle([1, 3, 6, 4], fill=color)
        img.save(path)

    dot(os.path.join(out, "blood_spark.png"), (168, 24, 24, 255), "ball")
    dot(os.path.join(out, "chain_spark.png"), (255, 176, 72, 255), "cross")
    dot(os.path.join(out, "blast_ring.png"), (255, 128, 64, 220), "ring")


def main():
    os.makedirs(os.path.join(ASSETS, "item"), exist_ok=True)
    os.makedirs(os.path.join(ASSETS, "entity"), exist_ok=True)
    item_texture(os.path.join(ASSETS, "item", "chainsaw.png"))
    form_texture(os.path.join(ASSETS, "entity", "chainsaw_form.png"))
    katana_man_texture(os.path.join(ASSETS, "entity", "katana_man.png"))
    gun_devil_texture(os.path.join(ASSETS, "entity", "gun_devil.png"))
    particle_textures()
    print("写入:", os.path.join(ASSETS, "item", "chainsaw.png"))
    print("写入:", os.path.join(ASSETS, "entity", "chainsaw_form.png"))
    print("写入:", os.path.join(ASSETS, "entity", "katana_man.png"))
    print("写入:", os.path.join(ASSETS, "entity", "gun_devil.png"))


if __name__ == "__main__":
    main()
