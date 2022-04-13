# coding=utf-8
from PIL import Image
import pyocr

# OCRエンジンを取得
engines = pyocr.get_available_tools()
engine = engines[0]

# 画像の文字を読み込む
txt = engine.image_to_string(Image.open('2022-03-04-14-04-37.jpg'), lang="eng")
print(txt) # 「Test Message」が出力される