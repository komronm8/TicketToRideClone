package view

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
        Pair(Triple(131, 392, 319.5), true),
        Pair(Triple(110, 419, 294.9), true),
        Pair(Triple(101, 453, 274.7), false),
        Pair(Triple(101, 490, 264.9), false),
        Pair(Triple(110, 523, 244.3), false),
        Pair(Triple(131, 551, 220.4), false),
        52, Pair("Trondheim", "Mo I Rana")
    ),
    arrayOf(
        Pair(Triple(160, 402, 270.0), true),
        Pair(Triple(160, 439, 270.0), true),
        Pair(Triple(160, 477, 270.0), false),
        Pair(Triple(160, 514, 270.0), false),
        Pair(Triple(160, 551, 270.0), false),
        58, Pair("Mo I Rana", "Trondheim")
    ),
    arrayOf(
        Pair(Triple(336, 336, 82.5), false),
        Pair(Triple(341, 373, 82.5), false),
        Pair(Triple(346, 410, 82.5), false),
        63, Pair("Boden", "Umeå")
    ),
    arrayOf(
        Pair(Triple(321, 338, 82.5), false),
        Pair(Triple(326, 375, 82.5), false),
        Pair(Triple(331, 412, 82.5), false),
        66, Pair("Umeå", "Boden")
    ),
    arrayOf(
        Pair(Triple(375, 458, 31.9), true),
        69, Pair("Umeå", "Vaasa")
    ),
    arrayOf(
        Pair(Triple(342, 461, 102.8), false),
        Pair(Triple(333, 497, 102.8), false),
        Pair(Triple(324, 535, 102.8), false),
        70, Pair("Umeå", "Sundsvall")
    ),
    arrayOf(
        Pair(Triple(326, 458, 102.8), false),
        Pair(Triple(317, 495, 102.8), false),
        Pair(Triple(308, 533, 102.8), false),
        73, Pair("Sundsvall", "Umeå")
    ),
    arrayOf(
        Pair(Triple(417, 370, 102.3), false),
        Pair(Triple(409, 407, 102.3), false),
        Pair(Triple(401, 443, 102.3), false),
        76, Pair("Oulu", "Vaasa")
    ),
    arrayOf(
        Pair(Triple(547, 439, 100.0), false),
        Pair(Triple(535, 472, 118.1), false),
        Pair(Triple(530, 514, 75.8), false),
        79, Pair("Kuopio", "Lahti")
    ),
    arrayOf(
        Pair(Triple(429, 472, 337.7), false),
        Pair(Triple(463, 458, 337.7), false),
        Pair(Triple(497, 444, 337.7), false),
        Pair(Triple(531, 430, 337.7), false),
        82, Pair("Kuopio", "Vaasa")
    ),
    arrayOf(
        Pair(Triple(356, 562, 351.9), true),
        Pair(Triple(393, 542, 311.7), false),
        Pair(Triple(406, 503, 268.7), false),
        86, Pair("Vaasa", "Sundsvall")
    ),
    arrayOf(
        Pair(Triple(427, 503, 39.6), false),
        Pair(Triple(456, 527, 39.6), false),
        89, Pair("Vaasa", "Tampere")
    ),
    arrayOf(
        Pair(Triple(179, 586, 348.2), false),
        Pair(Triple(216, 570, 324.9), false),
        91, Pair("Trondheim", "Östersund")
    ),
    arrayOf(
        Pair(Triple(249, 571, 39.9), false),
        Pair(Triple(291, 580, 344.7), false),
        93, Pair("Östersund", "Sundsvall")
    ),
    arrayOf(
        Pair(Triple(581, 429, 23.6), false),
        Pair(Triple(602, 454, 72.2), false),
        95, Pair("Kuopio", "Imatra")
    ),
    arrayOf(
        Pair(Triple(569, 526, 318.0), false),
        Pair(Triple(596, 501, 318.0), false),
        97, Pair("Imatra", "Lahti")
    ),
    arrayOf(
        Pair(Triple(619, 512, 287.3), false),
        Pair(Triple(606, 548, 294.9), false),
        Pair(Triple(580, 580, 322.7), false),
        99, Pair("Imatra", "Helsinki")
    ),
    arrayOf(
        Pair(Triple(516, 547, 0.0), false),
        102, Pair("Tampere", "Lahti")
    ),
    arrayOf(
        Pair(Triple(538, 569, 90.0), false),
        103, Pair("Lahti", "Helsinki")
    ),
    arrayOf(
        Pair(Triple(510, 576, 45.0), false),
        104, Pair("Tampere", "Helsinki")
    ),
    arrayOf(
        Pair(Triple(479, 571, 90.0), false),
        105, Pair("Tampere", "Turku")
    ),
    arrayOf(
        Pair(Triple(511, 606, 0.0), false),
        106, Pair("Turku", "Helsinki")
    ),
    arrayOf(
        Pair(Triple(120, 586, 0.0), true),
        Pair(Triple(104, 602, 270.0), false),
        107, Pair("Trondheim", "Åndalsnes")
    ),
    arrayOf(
        Pair(Triple(162, 609, 45.0), false),
        Pair(Triple(180, 638, 75.1), false),
        Pair(Triple(179, 674, 105.7), false),
        109, Pair("Trondheim", "Lillehammer")
    ),
    arrayOf(
        Pair(Triple(119, 655, 45.0), false),
        Pair(Triple(146, 682, 45.0), false),
        112, Pair("Åndalsnes", "Lillehammer")
    ),
    arrayOf(
        Pair(Triple(72, 632, 0.0), true),
        Pair(Triple(48, 645, 301.3), true),
        Pair(Triple(32, 679, 286.9), false),
        Pair(Triple(27, 714, 271.6), false),
        Pair(Triple(35, 749, 241.6), false),
        114, Pair("Åndalsnes", "Bergen")
    ),
    arrayOf(
        Pair(Triple(189, 721, 43.5), false),
        Pair(Triple(206, 750, 78.9), false),
        119, Pair("Åndalsnes", "Bergen")
    ),
    arrayOf(
        Pair(Triple(74, 764, 328.7), false),
        Pair(Triple(110, 750, 348.4), false),
        Pair(Triple(150, 750, 14.4), false),
        Pair(Triple(186, 766, 33.3), false),
        121, Pair("Bergen", "Oslo")
    ),
    arrayOf(
        Pair(Triple(82, 778, 328.7), false),
        Pair(Triple(113, 765, 348.4), false),
        Pair(Triple(147, 766, 14.4), false),
        Pair(Triple(178, 780, 33.3), false),
        125, Pair("Oslo", "Bergen")
    ),
    arrayOf(
        Pair(Triple(246, 771, 340.3), false),
        Pair(Triple(282, 758, 340.3), false),
        129, Pair("Oslo", "Örebro")
    ),
    arrayOf(
        Pair(Triple(251, 785, 340.3), false),
        Pair(Triple(287, 772, 340.3), false),
        131, Pair("Örebro", "Oslo")
    ),
    arrayOf(
        Pair(Triple(336, 739, 340.3), false),
        Pair(Triple(372, 726, 340.3), false),
        133, Pair("Örebro", "Stockholm")
    ),
    arrayOf(
        Pair(Triple(341, 753, 340.3), false),
        Pair(Triple(377, 740, 340.3), false),
        135, Pair("Stockholm", "Örebro")
    ),
    arrayOf(
        Pair(Triple(301, 600, 91.2), false),
        Pair(Triple(301, 637, 91.2), false),
        Pair(Triple(300, 674, 91.2), false),
        Pair(Triple(299, 711, 91.2), false),
        137, Pair("Sundsvall", "Örebro")
    ),
    arrayOf(
        Pair(Triple(336, 585, 61.7), false),
        Pair(Triple(354, 618, 61.7), false),
        Pair(Triple(372, 651, 61.7), false),
        Pair(Triple(390, 684, 61.7), false),
        141, Pair("Sundsvall", "Stockholm")
    ),
    arrayOf(
        Pair(Triple(322, 593, 61.7), false),
        Pair(Triple(340, 626, 61.7), false),
        Pair(Triple(358, 659, 61.7), false),
        Pair(Triple(376, 692, 61.7), false),
        145, Pair("Stockholm", "Sundsvall")
    ),
    arrayOf(
        Pair(Triple(424, 680, 286.1), true),
        Pair(Triple(439, 648, 305.4), false),
        Pair(Triple(464, 622, 324.3), false),
        149, Pair("Turku", "Stockholm")
    ),
    arrayOf(
        Pair(Triple(437, 698, 320.3), true),
        Pair(Triple(466, 674, 320.3), false),
        Pair(Triple(495, 650, 320.3), false),
        Pair(Triple(523, 627, 320.3), false),
        152, Pair("Stockholm", "Helsinki")
    ),
    arrayOf(
        Pair(Triple(447, 710, 320.3), true),
        Pair(Triple(476, 686, 320.3), true),
        Pair(Triple(505, 662, 320.3), false),
        Pair(Triple(533, 639, 320.3), false),
        156, Pair("Helsinki", "Stockholm")
    ),
    arrayOf(
        Pair(Triple(568, 626, 42.6), true),
        Pair(Triple(589, 652, 312.2), false),
        160, Pair("Helsinki", "Tallinn")
    ),
    arrayOf(
        Pair(Triple(442, 736, 0.0), true),
        Pair(Triple(480, 731, 348.1), true),
        Pair(Triple(517, 715, 327.0), false),
        Pair(Triple(547, 692, 317.4), false),
        162, Pair("Tallinn", "Stockholm")
    ),
    arrayOf(
        Pair(Triple(39, 815, 85.4), true),
        Pair(Triple(56, 853, 47.7), false),
        166, Pair("Bergen", "Stavanger")
    ),
    arrayOf(
        Pair(Triple(114, 871, 2.8), false),
        Pair(Triple(150, 873, 2.8), false),
        168, Pair("Stavanger", "Kristiansand")
    ),
    arrayOf(
        Pair(Triple(92, 900, 50.4), true),
        Pair(Triple(126, 914, 1.5), false),
        Pair(Triple(162, 899, 312.6), false),
        170, Pair("Kristiansand", "Stavanger")
    ),
    arrayOf(
        Pair(Triple(199, 812, 308.1), true),
        Pair(Triple(186, 844, 275.5), false),
        173, Pair("Oslo", "Kristiansand")
    ),
    arrayOf(
        Pair(Triple(182, 903, 62.9), true),
        Pair(Triple(211, 932, 25.9), false),
        175, Pair("Kristiansand", "Ålborg")
    ),
    arrayOf(
        Pair(Triple(228, 825, 261.8), false),
        Pair(Triple(233, 862, 261.8), false),
        Pair(Triple(238, 896, 261.8), true),
        177, Pair("Oslo", "Ålborg")
    ),
    arrayOf(
        Pair(Triple(238, 820, 49.9), false),
        Pair(Triple(262, 850, 49.9), false),
        180, Pair("Oslo", "Göteborg")
    ),
    arrayOf(
        Pair(Triple(269, 932, 0.0), true),
        Pair(Triple(290, 900, 276.9), false),
        182, Pair("Ålborg", "Göteborg")
    ),
    arrayOf(
        Pair(Triple(250, 955, 60.1), false),
        184, Pair("Ålborg", "Århus")
    ),
    arrayOf(
        Pair(Triple(298, 980, 0.0), true),
        185, Pair("Århus", "København")
    ),
    arrayOf(
        Pair(Triple(330, 781, 26.4), false),
        Pair(Triple(370, 789, 357.6), false),
        186, Pair("Örebro", "Norrköping")
    ),
    arrayOf(
        Pair(Triple(312, 795, 283.2), false),
        Pair(Triple(303, 832, 283.2), false),
        188, Pair("Örebro", "Göteborg")
    ),
    arrayOf(
        Pair(Triple(319, 857, 322.8), false),
        Pair(Triple(349, 834, 322.8), false),
        Pair(Triple(379, 811, 322.8), false),
        190, Pair("Göteborg", "Norrköping")
    ),
    arrayOf(
        Pair(Triple(301, 902, 71.7), true),
        Pair(Triple(313, 937, 71.7), false),
        193, Pair("Göteborg", "København")
    ),
    arrayOf(
        Pair(Triple(406, 757, 279.2), false),
        195, Pair("Stockholm", "Norrköping")
    ),
    arrayOf(
        Pair(Triple(421, 759, 279.2), false),
        196, Pair("Norrköping", "Stockholm")
    ),
    arrayOf(
        Pair(Triple(410, 819, 262.5), false),
        Pair(Triple(415, 855, 262.5), false),
        Pair(Triple(420, 892, 262.5), false),
        197, Pair("Norrköping", "Karlskrona")
    ),
    arrayOf(
        Pair(Triple(424, 817, 262.5), false),
        Pair(Triple(429, 853, 262.5), false),
        Pair(Triple(434, 890, 262.5), false),
        200, Pair("Karlskrona", "Norrköping")
    ),
    arrayOf(
        Pair(Triple(362, 954, 336.5), true),
        Pair(Triple(396, 939, 336.5), false),
        203, Pair("Karlskrona", "København")
    ),
    arrayOf(
        Pair(Triple(369, 967, 336.5), true),
        Pair(Triple(403, 952, 336.5), false),
        205, Pair("København", "Karlskrona")
    )
)