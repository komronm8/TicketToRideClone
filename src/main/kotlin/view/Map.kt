package view

import entity.Color
import entity.Game
import entity.Route

//Pair<Triple<x, y, rotation>, isTrain>
val mapRouteButtons: Array<Array<Any>> = arrayOf(
    arrayOf(
        Pair(Triple(208, 123, 295.5), false),
        Pair(Triple(226, 90, 304.0), false),
        Pair(Triple(249, 61, 316.7), true),
        Pair(Triple(279, 37, 324.7), true),
        0, Pair("Honningsvåg", "Tromsø")
    ),
    arrayOf(
        Pair(Triple(314, 45, 42.5), false),
        Pair(Triple(348, 67, 22.6), true),
        4, Pair("Honningsvåg", "Kirkenes")
    ),
    arrayOf(
        Pair(Triple(395, 47, 308.9), false),
        Pair(Triple(423, 35, 0.0), false),
        Pair(Triple(460, 39, 10.6), true),
        6, Pair("Kirkenes", "Murmansk")
    ),
    arrayOf(
        Pair(Triple(506, 64, 35.5), false),
        Pair(Triple(535, 88, 45.2), false),
        Pair(Triple(559, 116, 55.2), false),
        Pair(Triple(577, 148, 64.3), false),
        Pair(Triple(590, 182, 74.8), false),
        Pair(Triple(597, 219, 83.9), false),
        Pair(Triple(597, 257, 94.1), false),
        Pair(Triple(591, 293, 104.6), false),
        Pair(Triple(579, 327, 115.2), false),
        9, Pair("Murmansk", "Lieksa")
    ),
    arrayOf(
        Pair(Triple(397, 90, 40.0), false),
        Pair(Triple(422, 117, 53.9), false),
        Pair(Triple(434, 150, 85.0), false),
        Pair(Triple(428, 184, 113.9), false),
        Pair(Triple(409, 215, 128.6), false),
        18, Pair("Kirkenes", "Rovaniemi")
    ),
    arrayOf(
        Pair(Triple(168, 152, 334.7), true),
        Pair(Triple(147, 177, 284.4), false),
        Pair(Triple(153, 209, 234.3), false),
        23, Pair("Tromsø", "Narvik")
    ),
    arrayOf(
        Pair(Triple(195, 237, 19.0), false),
        26, Pair("Narvik", "Kiruna")
    ),
    arrayOf(
        Pair(Triple(190, 252, 19.0), false),
        27, Pair("Kiruna", "Narvik")
    ),
    arrayOf(
        Pair(Triple(240, 262, 55.7), false),
        Pair(Triple(270, 290, 29.8), false),
        Pair(Triple(308, 302, 5.5), false),
        28, Pair("Kiruna", "Boden")
    ),
    arrayOf(
        Pair(Triple(231, 276, 55.7), false),
        Pair(Triple(262, 303, 29.8), false),
        Pair(Triple(301, 316, 5.5), false),
        31, Pair("Boden", "Kiruna")
    ),
    arrayOf(
        Pair(Triple(361, 310, 351.1), false),
        34, Pair("Boden", "Tornio")
    ),
    arrayOf(
        Pair(Triple(403, 273, 67.0), false),
        Pair(Triple(418, 307, 67.0), false),
        35, Pair("Rovaniemi", "Oulu")
    ),
    arrayOf(
        Pair(Triple(379, 271, 91.2), false),
        37, Pair("Rovaniemi", "Tornio")
    ),
    arrayOf(
        Pair(Triple(401, 326, 45.0), false),
        38, Pair("Tornio", "Oulu")
    ),
    arrayOf(
        Pair(Triple(456, 340, 351.1), false),
        Pair(Triple(493, 334, 351.1), false),
        39, Pair("Oulu", "Kajaani")
    ),
    arrayOf(
        Pair(Triple(544, 348, 28.6), false),
        41, Pair("Kajaani", "Lieksa")
    ),
    arrayOf(
        Pair(Triple(557, 381, 105.0), false),
        42, Pair("Lieksa", "Kuopio")
    ),
    arrayOf(
        Pair(Triple(512, 358, 93.1), false),
        Pair(Triple(528, 397, 39.4), false),
        43, Pair("Kajaani", "Kuopio")
    ),
    arrayOf(
        Pair(Triple(449, 366, 32.8), false),
        Pair(Triple(480, 386, 32.8), false),
        Pair(Triple(511, 406, 32.8), false),
        45, Pair("Oulu", "Kuopio")
    ),
    arrayOf(
        Pair(Triple(147, 251, 315.1), true),
        Pair(Triple(129, 280, 290.0), true),
        Pair(Triple(125, 314, 265.3), false),
        Pair(Triple(136, 348, 239.4), false),
        48, Pair("Narvik", "Mo I Rana")
    ),
    arrayOf(
        Pair(Triple(160, 402, 270.0), true),
        Pair(Triple(160, 439, 270.0), true),
        Pair(Triple(160, 477, 270.0), false),
        Pair(Triple(160, 514, 270.0), false),
        Pair(Triple(160, 551, 270.0), false),
        52, Pair("Mo I Rana", "Trondheim")
    )
)