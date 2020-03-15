# -*- coding: utf-8 -*-
"""Author: Lukasz Czerwinski, czerwinskilukasz1 at gmail dot com"""

# Find grid from nonograms-box.png.

import bisect
import cv2
import numpy as np
import logging
import math
import sys
from matplotlib import pyplot as plt


if sys.version_info.major < 3:
    logging.error("Requires Python 3 or greater to run. Aborting execution.")
    sys.exit(1)


SHOW_WINDOWS = True
PROBABILISTIC = True


def window_show(win_name, img):
    if SHOW_WINDOWS:
        cv2.namedWindow(win_name, cv2.WINDOW_NORMAL)
        height = img.shape[0]
        width = img.shape[1]
        cv2.resizeWindow(win_name, width, height)
        cv2.imshow(win_name, img)


def save_image(filename, img_dest):
    cv2.imwrite("c:\\Users\\Lukasz\\Downloads\\{}".format(filename), img_dest)


def run():
    box = cv2.imread("nonograms-box.png", cv2.IMREAD_GRAYSCALE)
    box_blurred = cv2.GaussianBlur(box, (3, 3), 0)
    laplacian = cv2.Laplacian(box_blurred, cv2.CV_8U)

    if PROBABILISTIC:
        # TODO: fill in params
        lines = cv2.HoughLinesP(laplacian, 1, np.pi/180, 80)
    else:
        lines = cv2.HoughLines(laplacian, 1, np.pi/180, 100, 100, 10)

    print("Lines count: {}".format(len(lines)))

    box_color = cv2.cvtColor(box, cv2.COLOR_GRAY2BGR)
    for line in lines:
        if PROBABILISTIC:
            (x1, y1, x2, y2) = line[0]
            x3 = x1 + 10*(x2-x1)
            y3 = y1 + 10*(y2-y1)
            cv2.line(box, (x1, y1), (x3, y3), (255, 0, 0), 3)
        else:
            (rho, theta) = line[0]
            a = np.cos(theta)
            b = np.sin(theta)
            x0 = a*rho
            y0 = b*rho
            x1 = int(x0 + 1000*(-b))
            y1 = int(y0 + 1000*(a))
            x2 = int(x0 - 1000*(-b))
            y2 = int(y0 - 1000*(a))

        cv2.line(box_color, (x1, y1), (x2, y2), (0, 0, 255), 3)

    window_show("laplacian", laplacian)
    save_image("laplacian.png", laplacian)
    window_show("box_color", box_color)
    cv2.waitKey()


run()

