import cv2
import pyocr
from PIL import Image
import pyocr.builders

tools = pyocr.get_available_tools()
tool = tools[0]

def ocrTest():
    cap = cv2.VideoCapture(0)

    while True:

        ret, frame = cap.read()

        Height, Width = frame.shape[:2]

        img = cv2.resize(frame,(int(Width),int(Height)))

        # OCRで読み取りたい領域を赤枠で囲む
        cv2.rectangle(img, (100, 100), (Width-200, Height-200), (0, 0, 255), 10)

        ocr(img, Width,Height)

        cv2.imshow('Ocr Test', img)

        cv2.waitKey(100)

    cap.release()
    cv2.destroyAllWindows()

def ocr(img,Width,Height):
    dst = img[100:Height-200,100:Width-200] #OCRで読みたい領域を切り出す
    PIL_Image=Image.fromarray(dst)
    text = tool.image_to_string(
        PIL_Image,
        lang='eng',
        builder=pyocr.builders.TextBuilder())

    if(text != ""):
        print(text)

ocrTest()
