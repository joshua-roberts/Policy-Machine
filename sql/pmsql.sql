-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.7.12-log - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL Version:             9.4.0.5125
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dumping database structure for pmwsdb
DROP DATABASE IF EXISTS `pmwsdb`;
CREATE DATABASE IF NOT EXISTS `pmwsdb` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `pmwsdb`;

-- Dumping structure for view pmwsdb.acl_entry_view
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `acl_entry_view` (
	`node_id` INT(11) NOT NULL,
	`user` VARCHAR(100) NULL COLLATE 'utf8_general_ci',
	`allowed_ops` VARCHAR(500) NULL COLLATE 'utf8_general_ci',
	`obj_id` INT(11) NOT NULL,
	`obj_name` VARCHAR(100) NULL COLLATE 'utf8_general_ci'
) ENGINE=MyISAM;

-- Dumping structure for view pmwsdb.acl_view
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `acl_view` (
	`obj_name` VARCHAR(100) NULL COLLATE 'utf8_general_ci',
	`group_concat(user,'-',allowed_ops)` TEXT NULL COLLATE 'utf8_general_ci'
) ENGINE=MyISAM;

-- Dumping structure for function pmwsdb.add_script
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `add_script`(script_name varchar(50)) RETURNS int(11)
BEGIN
                declare script_id_var int;
                insert into ob_script(script_name, count) values (script_name, 0);
                select MAX(script_id) into script_id_var from ob_script;
RETURN script_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.allowed_operations
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `allowed_operations`(ua_id_in int(11), oa_id_in int(11)) RETURNS varchar(500) CHARSET utf8
BEGIN

DECLARE policy_id_in int;
DECLARE opset_id int;
DECLARE opsets_count int;
DECLARE finished INTEGER DEFAULT 0;
DECLARE p_finished INTEGER DEFAULT 0;
DECLARE no_allowed_ops varchar(1);
DECLARE allowed_ops varchar(5000);
DECLARE done boolean DEFAULT FALSE;
DECLARE names VARCHAR(8000);
DECLARE policies CURSOR FOR select distinct a.start_node_id as policy_id from assignment a where get_node_type(a.start_node_id) = 2 and end_node_id = oa_id_in;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET p_finished = 1;
  OPEN policies;
  SET no_allowed_ops='';
  SET allowed_ops='';
  ploicy_loop: LOOP
    FETCH policies INTO policy_id_in;
    IF p_finished = 1 THEN 
            LEAVE ploicy_loop;
    END IF;
    BEGIN
      DECLARE opsets CURSOR FOR SELECT distinct a.opset_id from association as a where is_member(ua_id_in, a.ua_id) and is_member(oa_id_in, a.oa_id) and is_member(ua_id_in, policy_id_in);
      DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;
      open opsets;
      opset_loop: loop
        FETCH opsets INTO opset_id;
        IF finished = 1 THEN 
            LEAVE opset_loop;
        END IF;
        set allowed_ops = CONCAT(allowed_ops,',', cast(opset_id as char));
      end loop opset_loop;
      CLOSE opsets;
    END;
  END LOOP ploicy_loop;
  CLOSE policies;
  IF substring(allowed_ops FROM 1 FOR 1) = ',' THEN
     SET allowed_ops = substring(allowed_ops,2);
  END IF;

  SELECT group_concat(concat(name) separator ',') into names from operation as o, operation_set_details as osd
  where o.operation_id = osd.operation_id
  and osd.operation_set_details_node_id in (allowed_ops);

RETURN Names;
END//
DELIMITER ;

-- Dumping structure for table pmwsdb.application
CREATE TABLE IF NOT EXISTS `application` (
  `application_id` int(11) NOT NULL AUTO_INCREMENT,
  `host_id` int(11) NOT NULL,
  `application_name` varchar(50) NOT NULL,
  `application_main_class` varchar(200) NOT NULL,
  `application_path` varchar(2000) NOT NULL,
  `application_prefix` varchar(50) NOT NULL,
  PRIMARY KEY (`application_id`),
  KEY `fk_application_host_id_idx` (`host_id`),
  KEY `idx_application_application_name` (`application_name`),
  CONSTRAINT `fk_application_host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`host_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='application';

-- Dumping data for table pmwsdb.application: ~0 rows (approximately)
/*!40000 ALTER TABLE `application` DISABLE KEYS */;
/*!40000 ALTER TABLE `application` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.assignment
CREATE TABLE IF NOT EXISTS `assignment` (
  `assignment_id` int(11) NOT NULL AUTO_INCREMENT,
  `start_node_id` int(11) NOT NULL,
  `end_node_id` int(11) NOT NULL,
  `depth` int(11) DEFAULT NULL,
  `assignment_path_id` int(2) DEFAULT NULL,
  PRIMARY KEY (`assignment_id`),
  KEY `end_node_id_idx` (`end_node_id`),
  KEY `fk_start_node_id_idx` (`start_node_id`),
  KEY `idx_all_columns` (`start_node_id`,`depth`,`assignment_path_id`,`end_node_id`),
  CONSTRAINT `fk_endnode` FOREIGN KEY (`end_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_startnode` FOREIGN KEY (`start_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores assignment relations';

-- Dumping data for table pmwsdb.assignment: ~341 rows (approximately)
/*!40000 ALTER TABLE `assignment` DISABLE KEYS */;
INSERT INTO `assignment` (`assignment_id`, `start_node_id`, `end_node_id`, `depth`, `assignment_path_id`) VALUES
	(1, -2, -3, 1, 1),
	(2, -1, -2, 1, 2),
	(95, 5, 14, 1, 83),
	(179, 5, 10, 1, 147),
	(78, 6, 15, 1, 70),
	(174, 9, 10, 1, 144),
	(52, 13, 15, 1, 50),
	(138, 13, 14, 1, 114),
	(18, 17, 19, 1, 18),
	(19, 17, 20, 1, 19),
	(7, 26, 29, 1, 7),
	(127, 26, 27, 1, 105),
	(233, 26, 34, 2, 182),
	(340, 26, 78, 2, 249),
	(373, 26, 54, 2, 270),
	(33, 27, 68, 1, 33),
	(151, 28, 33, 1, 127),
	(232, 29, 34, 1, 182),
	(339, 29, 78, 1, 249),
	(372, 29, 54, 1, 270),
	(27, 30, 75, 1, 27),
	(79, 30, 71, 1, 71),
	(96, 30, 51, 1, 84),
	(302, 30, 43, 1, 228),
	(176, 30, 78, 2, 145),
	(313, 30, 77, 2, 234),
	(76, 39, 42, 1, 68),
	(46, 67, 69, 1, 46),
	(175, 75, 78, 1, 145),
	(312, 75, 77, 1, 234),
	(247, 80, 83, 1, 189),
	(139, 81, 86, 1, 115),
	(318, 81, 102, 1, 236),
	(130, 82, 111, 1, 108),
	(111, 83, 100, 1, 95),
	(143, 83, 180, 1, 119),
	(193, 84, 161, 1, 157),
	(274, 84, 169, 1, 210),
	(259, 109, 111, 1, 197),
	(167, 133, 134, 1, 139),
	(208, 133, 135, 1, 170),
	(351, 133, 136, 1, 257),
	(307, 137, 138, 1, 231),
	(264, 145, 148, 1, 202),
	(56, 153, 154, 1, 52),
	(64, 153, 155, 1, 58),
	(169, 173, 175, 1, 141),
	(345, 177, 178, 1, 253),
	(48, 186, 196, 1, 48),
	(349, 186, 195, 1, 255),
	(262, 187, 202, 1, 200),
	(123, 189, 218, 1, 101),
	(195, 193, 222, 1, 159),
	(34, 194, 223, 1, 34),
	(225, 195, 224, 1, 179),
	(202, 197, 212, 1, 164),
	(154, 201, 208, 1, 130),
	(178, 201, 211, 1, 146),
	(181, 201, 207, 1, 149),
	(270, 235, 248, 1, 206),
	(21, 236, 243, 1, 21),
	(32, 236, 255, 1, 32),
	(204, 236, 267, 1, 166),
	(157, 237, 240, 1, 133),
	(135, 267, 271, 1, 111),
	(338, 273, 278, 1, 248),
	(344, 273, 275, 1, 252),
	(31, 282, 293, 1, 31),
	(141, 286, 290, 1, 117),
	(59, 291, 293, 1, 55),
	(200, 291, 294, 1, 162),
	(168, 296, 300, 1, 140),
	(186, 296, 331, 2, 153),
	(210, 296, 760, 2, 171),
	(223, 296, 532, 2, 178),
	(227, 296, 655, 2, 180),
	(230, 296, 772, 2, 181),
	(238, 296, 493, 2, 185),
	(241, 296, 556, 2, 186),
	(279, 296, 544, 2, 214),
	(282, 296, 646, 2, 215),
	(289, 296, 394, 2, 220),
	(300, 296, 574, 2, 227),
	(309, 296, 328, 2, 232),
	(323, 296, 529, 2, 240),
	(326, 296, 397, 2, 241),
	(329, 296, 691, 2, 242),
	(347, 296, 559, 2, 254),
	(367, 296, 625, 2, 266),
	(355, 296, 762, 3, 259),
	(43, 297, 299, 1, 43),
	(50, 297, 642, 2, 49),
	(54, 297, 534, 2, 51),
	(61, 297, 444, 2, 56),
	(72, 297, 555, 2, 65),
	(84, 297, 786, 2, 75),
	(87, 297, 588, 2, 76),
	(99, 297, 819, 2, 86),
	(113, 297, 639, 2, 96),
	(119, 297, 507, 2, 98),
	(133, 297, 681, 2, 110),
	(160, 297, 756, 2, 135),
	(191, 297, 381, 2, 156),
	(197, 297, 546, 2, 160),
	(219, 297, 750, 2, 176),
	(245, 297, 396, 2, 188),
	(249, 297, 831, 2, 190),
	(253, 297, 552, 2, 192),
	(267, 297, 636, 2, 204),
	(292, 297, 333, 2, 221),
	(305, 297, 678, 2, 230),
	(316, 297, 528, 2, 235),
	(333, 297, 615, 2, 244),
	(362, 297, 783, 2, 263),
	(3, 298, 590, 1, 3),
	(4, 298, 308, 1, 4),
	(24, 298, 380, 1, 24),
	(45, 298, 479, 1, 45),
	(70, 298, 497, 1, 64),
	(75, 298, 812, 1, 67),
	(93, 298, 803, 1, 81),
	(97, 298, 533, 1, 85),
	(136, 298, 719, 1, 112),
	(144, 298, 503, 1, 120),
	(147, 298, 338, 1, 123),
	(182, 298, 302, 1, 150),
	(205, 298, 698, 1, 167),
	(206, 298, 377, 1, 168),
	(213, 298, 710, 1, 173),
	(256, 298, 482, 1, 194),
	(260, 298, 617, 1, 198),
	(295, 298, 797, 1, 223),
	(303, 298, 524, 1, 229),
	(331, 298, 428, 1, 243),
	(352, 298, 815, 1, 258),
	(365, 298, 743, 1, 265),
	(11, 299, 600, 1, 11),
	(17, 299, 606, 1, 17),
	(23, 299, 513, 1, 23),
	(25, 299, 540, 1, 25),
	(49, 299, 642, 1, 49),
	(53, 299, 534, 1, 51),
	(60, 299, 444, 1, 56),
	(71, 299, 555, 1, 65),
	(83, 299, 786, 1, 75),
	(86, 299, 588, 1, 76),
	(98, 299, 819, 1, 86),
	(112, 299, 639, 1, 96),
	(118, 299, 507, 1, 98),
	(132, 299, 681, 1, 110),
	(159, 299, 756, 1, 135),
	(190, 299, 381, 1, 156),
	(196, 299, 546, 1, 160),
	(218, 299, 750, 1, 176),
	(244, 299, 396, 1, 188),
	(248, 299, 831, 1, 190),
	(252, 299, 552, 1, 192),
	(266, 299, 636, 1, 204),
	(291, 299, 333, 1, 221),
	(304, 299, 678, 1, 230),
	(315, 299, 528, 1, 235),
	(332, 299, 615, 1, 244),
	(361, 299, 783, 1, 263),
	(38, 300, 658, 1, 38),
	(58, 300, 823, 1, 54),
	(63, 300, 571, 1, 57),
	(68, 300, 526, 1, 62),
	(90, 300, 460, 1, 78),
	(101, 300, 388, 1, 87),
	(106, 300, 745, 1, 90),
	(109, 300, 505, 1, 93),
	(126, 300, 457, 1, 104),
	(185, 300, 331, 1, 153),
	(209, 300, 760, 1, 171),
	(222, 300, 532, 1, 178),
	(226, 300, 655, 1, 180),
	(229, 300, 772, 1, 181),
	(237, 300, 493, 1, 185),
	(240, 300, 556, 1, 186),
	(278, 300, 544, 1, 214),
	(281, 300, 646, 1, 215),
	(288, 300, 394, 1, 220),
	(299, 300, 574, 1, 227),
	(308, 300, 328, 1, 232),
	(322, 300, 529, 1, 240),
	(325, 300, 397, 1, 241),
	(328, 300, 691, 1, 242),
	(346, 300, 559, 1, 254),
	(366, 300, 625, 1, 266),
	(116, 300, 461, 2, 97),
	(354, 300, 762, 2, 259),
	(74, 310, 311, 1, 66),
	(35, 322, 323, 1, 35),
	(82, 325, 327, 1, 74),
	(13, 331, 332, 1, 13),
	(357, 337, 338, 1, 261),
	(44, 343, 344, 1, 44),
	(145, 376, 377, 1, 121),
	(162, 400, 401, 1, 136),
	(20, 409, 410, 1, 20),
	(137, 412, 414, 1, 113),
	(94, 421, 422, 1, 82),
	(335, 424, 426, 1, 245),
	(14, 433, 435, 1, 14),
	(131, 439, 440, 1, 109),
	(115, 460, 461, 1, 97),
	(92, 487, 489, 1, 80),
	(173, 487, 488, 1, 143),
	(184, 538, 539, 1, 152),
	(263, 553, 555, 1, 201),
	(251, 559, 561, 1, 191),
	(40, 577, 578, 1, 40),
	(5, 589, 590, 1, 5),
	(107, 592, 593, 1, 91),
	(121, 598, 599, 1, 99),
	(29, 601, 603, 1, 29),
	(336, 604, 606, 1, 246),
	(343, 604, 605, 1, 251),
	(150, 625, 627, 1, 126),
	(321, 631, 632, 1, 239),
	(125, 646, 648, 1, 103),
	(188, 652, 654, 1, 154),
	(275, 670, 671, 1, 211),
	(287, 676, 677, 1, 219),
	(108, 697, 698, 1, 92),
	(149, 715, 717, 1, 125),
	(371, 733, 735, 1, 269),
	(273, 742, 744, 1, 209),
	(6, 745, 747, 1, 6),
	(353, 760, 762, 1, 259),
	(105, 772, 774, 1, 89),
	(57, 778, 779, 1, 53),
	(66, 793, 795, 1, 60),
	(257, 820, 821, 1, 195),
	(129, 826, 828, 1, 107),
	(155, 829, 831, 1, 131),
	(42, 841, 846, 1, 42),
	(171, 841, 863, 2, 142),
	(215, 841, 851, 2, 174),
	(28, 843, 856, 1, 28),
	(243, 843, 864, 1, 187),
	(277, 844, 849, 1, 213),
	(294, 844, 857, 1, 222),
	(170, 846, 863, 1, 142),
	(214, 846, 851, 1, 174),
	(189, 847, 848, 1, 155),
	(15, 851, 852, 1, 15),
	(158, 855, 858, 1, 134),
	(8, 863, 866, 1, 8),
	(337, 867, 869, 1, 247),
	(183, 873, 894, 1, 151),
	(364, 873, 898, 1, 264),
	(110, 875, 900, 1, 94),
	(297, 877, 879, 1, 225),
	(369, 877, 880, 1, 267),
	(199, 885, 888, 1, 161),
	(152, 897, 899, 1, 128),
	(286, 903, 920, 1, 218),
	(296, 904, 913, 1, 224),
	(272, 905, 922, 1, 208),
	(163, 915, 917, 1, 137),
	(9, 927, 929, 1, 9),
	(236, 936, 942, 1, 184),
	(261, 937, 994, 1, 199),
	(212, 939, 996, 1, 172),
	(284, 939, 964, 1, 216),
	(12, 940, 981, 1, 12),
	(298, 940, 989, 1, 226),
	(319, 941, 982, 1, 237),
	(39, 942, 999, 1, 39),
	(142, 944, 985, 1, 118),
	(255, 944, 961, 1, 193),
	(165, 944, 992, 2, 138),
	(217, 945, 950, 1, 175),
	(269, 977, 979, 1, 205),
	(65, 985, 986, 1, 59),
	(91, 985, 988, 1, 79),
	(164, 985, 992, 1, 138),
	(122, 993, 997, 1, 100),
	(342, 1002, 1004, 1, 250),
	(16, 1003, 1039, 1, 16),
	(140, 1003, 1025, 1, 116),
	(180, 1004, 1026, 1, 148),
	(221, 1004, 1040, 1, 177),
	(276, 1004, 1012, 1, 212),
	(320, 1004, 1019, 1, 238),
	(36, 1005, 1048, 1, 36),
	(271, 1007, 1029, 1, 207),
	(128, 1008, 1058, 1, 106),
	(80, 1009, 1052, 1, 72),
	(258, 1009, 1024, 1, 196),
	(156, 1010, 1014, 1, 132),
	(201, 1024, 1026, 1, 163),
	(356, 1031, 1033, 1, 260),
	(67, 1038, 1039, 1, 61),
	(22, 1052, 1058, 1, 22),
	(81, 11010, 11023, 1, 73),
	(194, 11015, 11008, 1, 158),
	(89, 11016, 11036, 1, 77),
	(30, 11020, 191, 1, 30),
	(153, 11039, 61, 1, 129),
	(148, 11040, 65, 1, 124),
	(207, 11045, 11066, 1, 169),
	(350, 11051, 11097, 1, 256),
	(10, 11072, 212, 1, 10),
	(124, 11072, 213, 1, 102),
	(77, 11077, 225, 1, 69),
	(26, 11079, 1019, 1, 26),
	(41, 11079, 1015, 1, 41),
	(265, 11079, 1012, 1, 203),
	(235, 11086, 271, 1, 183),
	(69, 11088, 11084, 1, 63),
	(37, 11093, 11094, 1, 37),
	(203, 11093, 11095, 1, 165),
	(377, 11093, 11111, 2, 272),
	(381, 11093, 11112, 2, 274),
	(385, 11093, 11113, 2, 276),
	(389, 11093, 11114, 2, 278),
	(393, 11093, 11115, 2, 280),
	(397, 11093, 11116, 2, 282),
	(376, 11095, 11111, 1, 272),
	(380, 11095, 11112, 1, 274),
	(384, 11095, 11113, 1, 276),
	(388, 11095, 11114, 1, 278),
	(392, 11095, 11115, 1, 280),
	(396, 11095, 11116, 1, 282),
	(146, 11099, 11104, 1, 122),
	(360, 11099, 264, 2, 262),
	(47, 11100, 11104, 1, 47),
	(103, 11100, 277, 2, 88),
	(359, 11100, 264, 2, 262),
	(285, 11101, 11103, 1, 217),
	(102, 11104, 277, 1, 88),
	(358, 11104, 264, 1, 262),
	(375, 11107, 11111, 1, 271),
	(379, 11107, 11112, 1, 273),
	(383, 11107, 11113, 1, 275),
	(387, 11107, 11114, 1, 277),
	(391, 11107, 11115, 1, 279),
	(395, 11107, 11116, 1, 281),
	(370, 11108, 11084, 1, 268);
/*!40000 ALTER TABLE `assignment` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.assignment_path
CREATE TABLE IF NOT EXISTS `assignment_path` (
  `assignment_path_id` int(11) NOT NULL AUTO_INCREMENT,
  `assignment_node_id` int(11) NOT NULL,
  PRIMARY KEY (`assignment_path_id`),
  KEY `fk_assignment_node_id` (`assignment_node_id`),
  CONSTRAINT `fk_assignment_node_id` FOREIGN KEY (`assignment_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.assignment_path: ~282 rows (approximately)
/*!40000 ALTER TABLE `assignment_path` DISABLE KEYS */;
INSERT INTO `assignment_path` (`assignment_path_id`, `assignment_node_id`) VALUES
	(1, -3),
	(2, -2),
	(233, 2),
	(144, 10),
	(147, 10),
	(83, 14),
	(114, 14),
	(50, 15),
	(70, 15),
	(18, 19),
	(19, 20),
	(105, 27),
	(7, 29),
	(127, 33),
	(182, 34),
	(68, 42),
	(228, 43),
	(84, 51),
	(270, 54),
	(129, 61),
	(124, 65),
	(33, 68),
	(46, 69),
	(71, 71),
	(27, 75),
	(234, 77),
	(145, 78),
	(249, 78),
	(189, 83),
	(115, 86),
	(95, 100),
	(236, 102),
	(108, 111),
	(197, 111),
	(139, 134),
	(170, 135),
	(257, 136),
	(231, 138),
	(202, 148),
	(52, 154),
	(58, 155),
	(157, 161),
	(210, 169),
	(141, 175),
	(253, 178),
	(119, 180),
	(30, 191),
	(255, 195),
	(48, 196),
	(200, 202),
	(149, 207),
	(130, 208),
	(146, 211),
	(10, 212),
	(164, 212),
	(102, 213),
	(101, 218),
	(159, 222),
	(34, 223),
	(179, 224),
	(69, 225),
	(133, 240),
	(21, 243),
	(206, 248),
	(32, 255),
	(262, 264),
	(166, 267),
	(111, 271),
	(183, 271),
	(252, 275),
	(88, 277),
	(248, 278),
	(117, 290),
	(31, 293),
	(55, 293),
	(162, 294),
	(43, 299),
	(140, 300),
	(150, 302),
	(4, 308),
	(66, 311),
	(35, 323),
	(74, 327),
	(232, 328),
	(153, 331),
	(13, 332),
	(221, 333),
	(123, 338),
	(261, 338),
	(44, 344),
	(121, 377),
	(168, 377),
	(24, 380),
	(156, 381),
	(87, 388),
	(220, 394),
	(188, 396),
	(241, 397),
	(136, 401),
	(20, 410),
	(113, 414),
	(82, 422),
	(245, 426),
	(243, 428),
	(14, 435),
	(109, 440),
	(56, 444),
	(104, 457),
	(78, 460),
	(97, 461),
	(45, 479),
	(194, 482),
	(143, 488),
	(80, 489),
	(185, 493),
	(64, 497),
	(120, 503),
	(93, 505),
	(98, 507),
	(23, 513),
	(229, 524),
	(62, 526),
	(235, 528),
	(240, 529),
	(178, 532),
	(85, 533),
	(51, 534),
	(152, 539),
	(25, 540),
	(214, 544),
	(160, 546),
	(192, 552),
	(65, 555),
	(201, 555),
	(186, 556),
	(254, 559),
	(191, 561),
	(57, 571),
	(227, 574),
	(40, 578),
	(76, 588),
	(3, 590),
	(5, 590),
	(91, 593),
	(99, 599),
	(11, 600),
	(29, 603),
	(251, 605),
	(17, 606),
	(246, 606),
	(244, 615),
	(198, 617),
	(266, 625),
	(126, 627),
	(239, 632),
	(204, 636),
	(96, 639),
	(49, 642),
	(215, 646),
	(103, 648),
	(154, 654),
	(180, 655),
	(38, 658),
	(211, 671),
	(219, 677),
	(230, 678),
	(110, 681),
	(242, 691),
	(92, 698),
	(167, 698),
	(173, 710),
	(125, 717),
	(112, 719),
	(269, 735),
	(265, 743),
	(209, 744),
	(90, 745),
	(6, 747),
	(176, 750),
	(135, 756),
	(171, 760),
	(259, 762),
	(181, 772),
	(89, 774),
	(53, 779),
	(263, 783),
	(75, 786),
	(60, 795),
	(223, 797),
	(81, 803),
	(67, 812),
	(258, 815),
	(86, 819),
	(195, 821),
	(54, 823),
	(107, 828),
	(131, 831),
	(190, 831),
	(42, 846),
	(155, 848),
	(213, 849),
	(174, 851),
	(15, 852),
	(28, 856),
	(222, 857),
	(134, 858),
	(142, 863),
	(187, 864),
	(8, 866),
	(247, 869),
	(225, 879),
	(267, 880),
	(161, 888),
	(151, 894),
	(264, 898),
	(128, 899),
	(94, 900),
	(224, 913),
	(137, 917),
	(218, 920),
	(208, 922),
	(9, 929),
	(184, 942),
	(175, 950),
	(193, 961),
	(216, 964),
	(205, 979),
	(12, 981),
	(237, 982),
	(118, 985),
	(59, 986),
	(79, 988),
	(226, 989),
	(138, 992),
	(199, 994),
	(172, 996),
	(100, 997),
	(39, 999),
	(250, 1004),
	(203, 1012),
	(212, 1012),
	(132, 1014),
	(41, 1015),
	(26, 1019),
	(238, 1019),
	(196, 1024),
	(116, 1025),
	(148, 1026),
	(163, 1026),
	(207, 1029),
	(260, 1033),
	(16, 1039),
	(61, 1039),
	(177, 1040),
	(36, 1048),
	(72, 1052),
	(22, 1058),
	(106, 1058),
	(158, 11008),
	(73, 11023),
	(77, 11036),
	(169, 11066),
	(63, 11084),
	(268, 11084),
	(37, 11094),
	(165, 11095),
	(256, 11097),
	(217, 11103),
	(47, 11104),
	(122, 11104),
	(271, 11111),
	(272, 11111),
	(273, 11112),
	(274, 11112),
	(275, 11113),
	(276, 11113),
	(277, 11114),
	(278, 11114),
	(279, 11115),
	(280, 11115),
	(281, 11116),
	(282, 11116);
/*!40000 ALTER TABLE `assignment_path` ENABLE KEYS */;

-- Dumping structure for view pmwsdb.assignment_view
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `assignment_view` (
	`start_node_id` INT(11) NOT NULL,
	`start_node_name` VARCHAR(100) NULL COLLATE 'utf8_general_ci',
	`end_node_id` INT(11) NOT NULL,
	`end_node_name` VARCHAR(100) NULL COLLATE 'utf8_general_ci',
	`depth` INT(11) NULL,
	`assignment_path_id` INT(2) NULL
) ENGINE=MyISAM;

-- Dumping structure for view pmwsdb.association
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `association` (
	`ua_id` BIGINT(11) NULL,
	`opset_id` INT(11) NOT NULL,
	`oa_id` INT(11) NOT NULL
) ENGINE=MyISAM;

-- Dumping structure for table pmwsdb.audit_information
CREATE TABLE IF NOT EXISTS `audit_information` (
  `SESS_ID` varchar(32) NOT NULL,
  `USER_ID` varchar(32) NOT NULL,
  `USER_NAME` varchar(80) NOT NULL,
  `HOST_NAME` varchar(80) NOT NULL,
  `TS` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ACTION` varchar(80) DEFAULT NULL,
  `RESULT_SUCCESS` tinyint(1) DEFAULT NULL,
  `DESCRIPTION` varchar(300) DEFAULT NULL,
  `OBJ_ID` varchar(80) DEFAULT NULL,
  `OBJ_NAME` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.audit_information: ~0 rows (approximately)
/*!40000 ALTER TABLE `audit_information` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_information` ENABLE KEYS */;

-- Dumping structure for procedure pmwsdb.create_assignment
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_assignment`(start_node int, end_node int, OUT error_msg varchar(1000))
BEGIN
DECLARE node_type_id_in int;
DECLARE path_id int;
DECLARE new_assignment_id int;
  IF EXISTS (SELECT NODE_ID FROM NODE WHERE NODE_ID = start_node) THEN
     IF EXISTS  (SELECT NODE_ID FROM NODE WHERE NODE_ID = END_NODE) THEN
      IF start_node <> end_node THEN
        IF get_node_type(start_node) <> 7 and get_node_type(end_node) <> 7 THEN
          IF NOT EXISTS (SELECT START_NODE_ID FROM ASSIGNMENT WHERE START_NODE_ID = START_NODE AND END_NODE_ID = END_NODE AND depth = 1) THEN
            -- create path_id
            INSERT INTO ASSIGNMENT_PATH (ASSIGNMENT_NODE_ID) VALUES (END_NODE);
            SELECT LAST_INSERT_ID() INTO path_id;
            -- Insert in assignment table
            INSERT INTO ASSIGNMENT (start_node_id, end_node_id, depth, assignment_path_id)
                                   (SELECT start_node, end_node, 1, path_id FROM DUAL)
                                    UNION
                                   (SELECT DISTINCT start_node_id, end_node, depth+1, path_id
                                    FROM ASSIGNMENT
                                    WHERE end_node_id = start_node
                                    AND assignment_path_id > 0
                                    AND depth > 0);
          ELSE
            SELECT 'ASSIGNMENT EXISTS' INTO error_msg;
            --  INSERT INTO ASSIGNMENT (start_node_id, end_node_id, depth) values (start_node, end_node, 1 );
          END IF;
          SELECT LAST_INSERT_ID() INTO new_assignment_id;
          IF new_assignment_id = 0 THEN
             SELECT 'Error in creating assignment' INTO error_msg;
          END IF;
        ELSE  
          SELECT 'Node can not be an operation set' INTO error_msg;
        END IF;
      ELSE
        SELECT 'Start node is the same as the end node' INTO error_msg;
      END IF;
      IF get_node_type(end_node) <> 2 THEN 
          DELETE FROM assignment WHERE START_NODE_ID = 1 AND END_NODE_ID = end_node AND depth = 1;
      END IF;
    ELSE 
      SELECT 'start_node not valid' INTO error_msg;
    END IF;
  ELSE 
    SELECT 'end_node not valid' INTO error_msg;
  END IF;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.create_association
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_association`(ua_node int, oa_node int, operations varchar(1000), OUT error_msg varchar(1000))
BEGIN
DECLARE node_id int;
DECLARE opset_id int;
DECLARE opset_name varchar(100);
    if ua_node is not null AND oa_node is not null and oa_node <> ua_node then
      IF not exists ( SELECT opset_id FROM association WHERE ua_id = ua_node and oa_id = oa_node ) THEN
          SELECT concat('opset',ua_node,oa_node) INTO opset_name;
          SELECT create_opset(operations,opset_name) INTO opset_id;
          INSERT INTO ASSIGNMENT (start_node_id, end_node_id, depth) values (oa_node, opset_id, 1);
          INSERT INTO ASSIGNMENT (start_node_id, end_node_id, depth) values (opset_id, ua_node, 1);
      ELSE 
          SELECT 'Operation set exists between nodes ' INTO error_msg;
      END IF;
    ELSE 
      SELECT 'Invalid ua_node or oa_node' INTO error_msg;
    END IF;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.create_deny
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_deny`(name varchar(50), deny_type varchar(2), operations varchar(1000), intersection char(1), object_complement varchar(1000), user_attribute_id int(11), process_id_in int(11), OUT error_msg varchar(1000))
BEGIN
DECLARE new_deny_id int;
DECLARE op_id int;
DECLARE object_id_in varchar(11);
DECLARE complement_in int(1);
DECLARE complement varchar(10);
DECLARE deny_type_id_in int(11);
DECLARE inc INT DEFAULT 0; 
DECLARE str VARCHAR(255);
-- insert in deny
   IF NOT EXISTS (SELECT DENY_ID FROM DENY WHERE UPPER(DENY_NAME) = name) THEN
     
     SELECT deny_type_id into deny_type_id_in from deny_type where upper(abbreviation)=upper(deny_type);
     INSERT INTO DENY (deny_name, deny_type_id, is_intersection, process_id, user_attribute_id)
     VALUES (name, deny_type_id_in, intersection, process_id_in, user_attribute_id);
     SELECT LAST_INSERT_ID() INTO new_deny_id;
      -- insert in deny_object_attribute
      SET inc=0;
      object_complement_loop: LOOP
         SET inc=inc+1;
         SET str=SPLIT_STR(object_complement,",",inc);
         IF str='' THEN
            LEAVE object_complement_loop;
         END IF;
         SET object_id_in = SPLIT_STR(str,"-",1);
         IF object_id_in is NULL THEN
            SELECT 'Object attribute is null';
         END IF;
         SET complement = SPLIT_STR(str,"-",2);
         IF complement = 'true' THEN
            SET complement_in = 1;
         ELSE
            SET complement_in = 0;
         END IF;
         INSERT INTO deny_obj_attribute (deny_id, object_attribute_id, object_complement)
         VALUES (new_deny_id, object_id_in, complement_in);
      END LOOP object_complement_loop;
      
      -- insert in deny_operation
      SET inc=0;
      deny_operation_loop: LOOP
         SET inc=inc+1;
         SET str=SPLIT_STR(operations,",",inc);
         IF str='' THEN
            LEAVE deny_operation_loop;
         END IF;
         
         SELECT get_operation_id(str) INTO op_id;
         INSERT INTO deny_operation (deny_id, deny_operation_id) VALUES (new_deny_id, op_id);
      END LOOP deny_operation_loop;
   ELSE   
      SELECT 'Deny exists' INTO error_msg;
   END IF;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.create_host
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_host`(user_id int, hostname varchar(50), domain_controller_ind varchar(1), path varchar(200))
BEGIN
                IF NOT EXISTS (SELECT HOST_ID FROM HOST WHERE UPPER(HOST_NAME) = UPPER(HOSTNAME)) THEN
                                INSERT INTO HOST (HOST_NAME, IS_DOMAIN_CONTROLLER, WORKAREA_PATH)
                                                VALUES(hostname, domain_controller_ind, path);
                END IF;

END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_node_fun
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_node_fun`(node_id_in int, node_name varchar(200), node_type_name varchar(20)) RETURNS int(11)
BEGIN
DECLARE node_type_id_in int;
DECLARE inserted_node_id int;
  -- Insert in Node table
  SELECT NODE_TYPE_ID INTO node_type_id_in FROM NODE_TYPE WHERE UPPER(NAME) = UPPER(node_type_name);
  IF node_type_id_in IS NULL THEN 
     RETURN 0;
  END IF;
  IF node_id_in is null OR node_id_in = 0 THEN
    INSERT INTO NODE (NODE_TYPE_ID, NAME) VALUES (node_type_id_in,node_name);
  ELSE
    INSERT INTO NODE (NODE_ID, NODE_TYPE_ID, NAME) VALUES (node_id_in, node_type_id_in,node_name);
  END IF;
  SELECT LAST_INSERT_ID() INTO inserted_node_id FROM NODE;
  -- create self assignment
  -- INSERT INTO ASSIGNMENT (start_node_id, end_node_id, depth,assignment_path_id) VALUES (inserted_node_id, inserted_node_id,0,0);
  -- add assignment to the given base node
  -- IF base_node_id is not NULL THEN
  --   CALL create_assignment(base_node_id,inserted_node_id);
  -- END IF;
  RETURN inserted_node_id;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.create_object_class
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_object_class`(object_class_name varchar(45), object_class_id_in int, class_object_description varchar(100))
BEGIN
-- Insert in object_class table
                IF not exists ( SELECT OBJECT_CLASS_ID FROM OBJECT_CLASS WHERE object_class_id = object_class_id_in ) THEN
                                INSERT INTO OBJECT_CLASS (object_class_id, name, description) values (object_class_id_in, object_class_name, class_object_description);
                END IF;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.create_object_detail
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_object_detail`(object_id int, original_obj_node_id int, object_class_id_in int, host_id_in int, obj_path varchar(200), include_ascedants_in int(1), template_id_in int(11))
BEGIN
DECLARE new_object_id int;
  IF object_id is not null THEN
                IF exists ( SELECT node_id FROM NODE WHERE node_id = object_id) THEN
                                INSERT INTO OBJECT_DETAIL (object_node_id, original_node_id, object_class_id, host_id, path, include_ascedants, template_id)
                                                    VALUES (object_id, original_obj_node_id, object_class_id_in, host_id_in, obj_path, include_ascedants_in, template_id_in);
      SELECT MAX(OBJECT_NODE_ID) INTO new_object_id from object_detail;
                END IF;
  END IF;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_ob_cont_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_cont_spec`(event_pattern_id_in varchar(50), node_type_in varchar(50),
                cont_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare cont_spec_id_var int;
                insert into ob_cont_spec (event_pattern_id, cont_spec_type, cont_spec_value)
                                values (event_pattern_id_in, get_node_type_id(node_type_in), cont_spec_value_in);
                select MAX(cont_spec_id) into cont_spec_id_var from ob_cont_spec;
RETURN cont_spec_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_ob_obj_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_obj_spec`(event_pattern_id_in varchar(50), node_type_in varchar(50),
                obj_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare obj_spec_id_var int;
                insert into ob_obj_spec (event_pattern_id, obj_spec_type, obj_spec_value) values (event_pattern_id_in, get_node_type_id(node_type_in), obj_spec_value_in);
                select MAX(obj_spec_id) into obj_spec_id_var from ob_obj_spec;
RETURN obj_spec_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_ob_op_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_op_spec`(event_pattern_id_in varchar(50), node_type_in varchar(50),
                op_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare op_spec_id_var int;
                insert into ob_op_spec (event_pattern_id, op_spec_event_id)
                                values (event_pattern_id_in, get_op_spec_type_id(op_spec_value_in));
                select MAX(op_spec_id) into op_spec_id_var from ob_op_spec;
RETURN op_spec_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_ob_pc_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_pc_spec`(event_pattern_id_in varchar(50), node_type_in varchar(50),
                pc_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare pc_spec_id_var int;
                insert into ob_policy_spec (event_pattern_d, policy_spec_type, policy_spec_value) values (event_pattern_id_in, get_node_type_id(node_type_in), pc_spec_value_in);
                select MAX(policy_spec_id) into pc_spec_id_var from ob_policy_spec;
RETURN pc_spec_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_ob_user_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_user_spec`(node_type_in varchar(50),
                user_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare user_spec_id_var int;
                insert into ob_user_spec (user_spec_type, user_spec_value) values (get_node_type_id(node_type_in), user_spec_value_in);
                select MAX(user_spec_id) into user_spec_id_var from ob_user_spec;
RETURN user_spec_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_operand
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_operand`(operand_type varchar(200), op_num int(2),
                is_function tinyint(1), is_subgraph tinyint(1), is_compliment tinyint(2), expression varchar(300),
                expression_id varchar(50), action_id varchar(50), parent_function varchar(50)) RETURNS int(11)
BEGIN
DECLARE node_type_id_in int;
DECLARE inserted_operand_id int;
-- Insert in Node table
    insert into ob_operand (operand_type,operand_num,is_function,is_subgraph,is_compliment,
                                expression,expression_id,action_id,parent_function)
                values (get_operand_type_id(operand_type),op_num,is_function,is_subgraph,is_compliment,
    expression,expression_id,action_id,parent_function);

                SELECT MAX(operand_id) INTO inserted_operand_id FROM ob_operand;
  RETURN inserted_operand_id;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.create_operation
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_operation`(operation_name varchar(45), object_class_id_in int)
BEGIN
-- Insert in object_class table
                IF not exists ( SELECT OPERATION_ID FROM OPERATION WHERE UPPER(NAME) = UPPER(operation_name) ) THEN
                                INSERT INTO OPERATION (operation_type_id, name, description, object_class_id) values (1, operation_name, operation_name, object_class_id_in);
                END IF;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_opset
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_opset`(operations varchar(1000), opset_name varchar(100)) RETURNS int(11)
BEGIN
DECLARE op_id int;
DECLARE new_opset_id int;
DECLARE op_list varchar(1000);
    -- Insert in node table
    INSERT INTO NODE (NODE_TYPE_ID, NAME, description) VALUES (7, opset_name, opset_name);
    -- Insert in operation_set_details table
    SELECT  LAST_INSERT_ID() INTO new_opset_id;
    SELECT formatCSL(operations) INTO op_list;
    SET @separator = ',';
    SET @separatorLength = CHAR_LENGTH(@separator);

    WHILE op_list != '' DO
          SET @currentValue = SUBSTRING_INDEX(op_list, @separator, 1);
          SELECT get_operation_id(@currentValue) INTO op_id;
          INSERT INTO operation_set_details (operation_set_details_node_id, operation_id) VALUES (new_opset_id, op_id);
          SET op_list = SUBSTRING(op_list, CHAR_LENGTH(@currentValue) + @separatorLength + 1);
    END WHILE;
    return new_opset_id;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.create_opset_detail
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_opset_detail`(opset_id int, operation varchar(50))
BEGIN
DECLARE op_id int;
                IF exists ( SELECT NODE_ID FROM NODE WHERE NODE_ID = opset_id) THEN
                                IF EXISTS (SELECT OPERATION_ID FROM OPERATION WHERE UPPER(NAME) = UPPER(operation)) THEN
                                                SELECT OPERATION_ID INTO op_id FROM OPERATION WHERE UPPER(NAME) = UPPER(operation);
                                                INSERT INTO OPERATION_SET_DETAILS (OPERATION_SET_DETAILS_NODE_ID, OPERATION_ID) VALUES (opset_id, op_id);
                                END IF;
                END IF;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_user_detail_fun
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_user_detail_fun`(user_id int, user_name varchar(20), full_name varchar(50), user_password varchar(1000), email_address varchar(100),
                user_host_id INT(11), user_property_in VARCHAR(200)) RETURNS int(11)
BEGIN
-- Insert in USER_DETAIL table
                IF exists ( SELECT NODE_ID FROM NODE WHERE NODE_ID = user_id) THEN
                                                INSERT INTO USER_DETAIL (USER_NODE_ID,USER_NAME, FULL_NAME, PASSWORD, EMAIL_ADDRESS, HOST_ID) VALUES (user_id,user_name,full_name,user_password,email_address,user_host_id);
                                END IF;
    RETURN USER_ID;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.create_user_fun
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_user_fun`(user_id int, user_name varchar(20), full_name varchar(50), user_description varchar(200), base_id int(11), user_password varchar(100), email_address varchar(100),
                user_host_id INT(11), user_property_in VARCHAR(200)) RETURNS int(11)
BEGIN
DECLARE new_node_id int(11);
                IF not exists ( SELECT NODE_ID FROM NODE WHERE NODE_ID = user_id) THEN
      -- Insert into NODE table
      SELECT create_node_fun(user_id, user_name, 'u', user_description, base_id) INTO new_node_id FROM DUAL;
                                                -- Insert into USER_DETAIL table
      INSERT INTO USER_DETAIL (USER_NODE_ID,USER_NAME, FULL_NAME, PASSWORD, EMAIL_ADDRESS, HOST_ID) VALUES (new_node_id,user_name,full_name,user_password,email_address,user_host_id);
                                END IF;
    RETURN USER_ID;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.delete_assignment
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_assignment`(start_node int, end_node int, OUT error_msg varchar(1000))
BEGIN
DECLARE path_id int;
declare cnt int;
    IF EXISTS (SELECT START_NODE_ID FROM ASSIGNMENT WHERE START_NODE_ID = START_NODE AND END_NODE_ID = END_NODE AND depth = 1) THEN
        -- get path_id
        SELECT ASSIGNMENT_PATH_ID INTO path_id FROM ASSIGNMENT
        WHERE START_NODE_ID = start_node
        AND END_NODE_ID = end_node;

        IF path_id is not null THEN
          DELETE FROM ASSIGNMENT WHERE ASSIGNMENT_PATH_ID = path_id;
        ELSE
          DELETE FROM ASSIGNMENT
          WHERE START_NODE_ID = start_node
          AND END_NODE_ID = end_node;
        END IF;
        -- if end_node is not assigned to any other node, assign it to the connector
        SELECT COUNT(*) INTO cnt FROM ASSIGNMENT WHERE end_node_id = end_node
        AND depth = 1;
        IF cnt = 0 THEN
          CALL create_assignment(1,end_node);
        END IF;
    ELSE
      SELECT 'Assignment does not exist' INTO error_msg;
    END IF;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.delete_association
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_association`(ua_node int, oa_node int, OUT error_msg varchar(1000))
BEGIN
DECLARE node_id int;
DECLARE opset_id_in int;
    BEGIN
      if ua_node is not null AND oa_node is not null then
        SELECT opset_id INTO opset_id_in FROM association WHERE ua_id = ua_node and oa_id = oa_node;
        IF opset_id_in is Null THEN
           SELECT 'Association does not exist' INTO error_msg;
        END IF;
        DELETE FROM ASSIGNMENT where start_node_id = oa_node and end_node_id = opset_id_in;
        DELETE FROM ASSIGNMENT where start_node_id = opset_id_in and end_node_id = ua_node;
      ELSE
        SELECT 'Null Node' INTO error_msg;
      END IF;
    END;
    DELETE FROM NODE WHERE NODE_ID = opset_id_in;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.delete_deny
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_deny`(deny_id_in int(11))
BEGIN
    DELETE FROM DENY WHERE deny_id = deny_id_in;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.delete_property
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_property`(property_in varchar(200))
BEGIN
      DELETE FROM NODE_PROPERTY WHERE UPPER(property_key) = UPPER(property_in);
END//
DELIMITER ;

-- Dumping structure for table pmwsdb.deny
CREATE TABLE IF NOT EXISTS `deny` (
  `deny_id` int(11) NOT NULL AUTO_INCREMENT,
  `deny_name` varchar(50) NOT NULL,
  `deny_type_id` int(11) NOT NULL,
  `user_attribute_id` int(11) DEFAULT NULL,
  `process_id` int(9) DEFAULT NULL,
  `is_intersection` int(1) DEFAULT NULL,
  PRIMARY KEY (`deny_id`),
  UNIQUE KEY `deny_type_id` (`deny_type_id`,`user_attribute_id`),
  KEY `user_attribute_id_idx` (`user_attribute_id`),
  KEY `deny_user_attribute_id_idx` (`user_attribute_id`),
  KEY `deny_type_id_idx` (`deny_type_id`),
  KEY `idx_deny_deny_name` (`deny_name`),
  CONSTRAINT `fk_deny_type_id` FOREIGN KEY (`deny_type_id`) REFERENCES `deny_type` (`deny_type_id`),
  CONSTRAINT `fk_deny_user_attribute_node_id` FOREIGN KEY (`user_attribute_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Deny';

-- Dumping data for table pmwsdb.deny: ~0 rows (approximately)
/*!40000 ALTER TABLE `deny` DISABLE KEYS */;
/*!40000 ALTER TABLE `deny` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.deny_obj_attribute
CREATE TABLE IF NOT EXISTS `deny_obj_attribute` (
  `deny_id` int(11) NOT NULL,
  `object_attribute_id` int(11) NOT NULL,
  `object_complement` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`deny_id`,`object_attribute_id`),
  KEY `fk_deny_obj_attr` (`object_attribute_id`),
  CONSTRAINT `fk_deny_id` FOREIGN KEY (`deny_id`) REFERENCES `deny` (`deny_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_deny_obj_attr` FOREIGN KEY (`object_attribute_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.deny_obj_attribute: ~0 rows (approximately)
/*!40000 ALTER TABLE `deny_obj_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `deny_obj_attribute` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.deny_operation
CREATE TABLE IF NOT EXISTS `deny_operation` (
  `deny_id` int(11) NOT NULL,
  `deny_operation_id` int(11) NOT NULL,
  PRIMARY KEY (`deny_id`,`deny_operation_id`),
  KEY `fk_deny_op_id` (`deny_operation_id`),
  CONSTRAINT `fk_deny_op_id` FOREIGN KEY (`deny_operation_id`) REFERENCES `operation` (`operation_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_deny_id` FOREIGN KEY (`deny_id`) REFERENCES `deny` (`deny_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.deny_operation: ~0 rows (approximately)
/*!40000 ALTER TABLE `deny_operation` DISABLE KEYS */;
/*!40000 ALTER TABLE `deny_operation` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.deny_type
CREATE TABLE IF NOT EXISTS `deny_type` (
  `deny_type_id` int(11) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `abbreviation` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`deny_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Deny types';

-- Dumping data for table pmwsdb.deny_type: ~3 rows (approximately)
/*!40000 ALTER TABLE `deny_type` DISABLE KEYS */;
INSERT INTO `deny_type` (`deny_type_id`, `name`, `abbreviation`) VALUES
	(1, 'user id', NULL),
	(2, 'user set', NULL),
	(3, 'process', NULL);
/*!40000 ALTER TABLE `deny_type` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.email_attachment
CREATE TABLE IF NOT EXISTS `email_attachment` (
  `email_node_id` int(11) NOT NULL,
  `attachment_node_id` int(11) NOT NULL,
  PRIMARY KEY (`email_node_id`,`attachment_node_id`),
  KEY `fk_att_node_id_idx` (`attachment_node_id`),
  CONSTRAINT `fk_att_node_id` FOREIGN KEY (`attachment_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='stores email attachments\n';

-- Dumping data for table pmwsdb.email_attachment: ~0 rows (approximately)
/*!40000 ALTER TABLE `email_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `email_attachment` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.email_detail
CREATE TABLE IF NOT EXISTS `email_detail` (
  `email_node_id` int(11) NOT NULL,
  `sender` varchar(254) NOT NULL,
  `recipient` varchar(254) NOT NULL,
  `timestamp` datetime NOT NULL,
  `email_subject` varchar(200) NOT NULL,
  `email_body` varchar(10000) DEFAULT NULL,
  PRIMARY KEY (`email_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table to hold information for emails.  sender, recipient, etc';

-- Dumping data for table pmwsdb.email_detail: ~1 rows (approximately)
/*!40000 ALTER TABLE `email_detail` DISABLE KEYS */;
INSERT INTO `email_detail` (`email_node_id`, `sender`, `recipient`, `timestamp`, `email_subject`, `email_body`) VALUES
	(0, 'bob', 'alice', '1970-01-18 12:59:46', 'TEST', 'TEST');
/*!40000 ALTER TABLE `email_detail` ENABLE KEYS */;

-- Dumping structure for function pmwsdb.formatCSL
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `formatCSL`(
_text TEXT
) RETURNS text CHARSET utf8
    NO SQL
BEGIN

IF _text IS NULL THEN
    RETURN NULL;
END IF;

SET _text = TRIM(_text);

WHILE INSTR(_text, ' ,') DO
    SET _text = REPLACE(_text, ' ,', ',');
END WHILE;

WHILE INSTR(_text, ', ') DO
    SET _text = REPLACE(_text, ', ', ',');
END WHILE;

RETURN _text;

END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.get_ACLs
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `get_ACLs`()
BEGIN
DECLARE finished INTEGER DEFAULT 0;
DECLARE obj_id_in INTEGER DEFAULT 0;
DECLARE objects CURSOR FOR (select node_id as obj_id FROM node where node_type_id in (5, 6));
DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;
  OPEN objects;
    objects_loop: LOOP
        FETCH objects INTO obj_id_in;
        IF finished = 1 THEN 
                LEAVE objects_loop;
        END IF;
        select n1.node_id, get_node_name(n1.node_id) as u_ua_id, allowed_operations(n1.node_id, obj_id_in) as allowed_ops, obj_id_in, get_node_name(obj_id_in)
        from node n1
        where n1.node_type_id in (3,4)
        and allowed_operations(n1.node_id, obj_id_in) is  not null;
    END LOOP objects_loop;
  CLOSE objects;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_action_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_action_type_id`(action_type_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE action_type_id_var int(11);
                SELECT action_type_id INTO action_type_id_var FROM ob_action_type
                WHERE UPPER(action_type_name) = UPPER(action_type_in);
RETURN action_type_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_action_type_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_action_type_name`(action_type_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE action_type_name_var varchar(50);
                SELECT action_type_name INTO action_type_name_var FROM ob_action_type
                WHERE UPPER(action_type_id) = UPPER(action_type_id_in);
RETURN action_type_name_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_cond_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_cond_type_id`(cond_type_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE cond_type_id_var int(11);
                SELECT cond_type_id INTO cond_type_id_var FROM ob_condition_type
                WHERE UPPER(cond_type) = UPPER(cond_type_in);
RETURN cond_type_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_cond_type_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_cond_type_name`(cond_type_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE cond_type_name_var varchar(50);
                SELECT cond_type INTO cond_type_name_var FROM ob_condition_type
                WHERE UPPER(cond_type_id) = UPPER(cond_type_id_in);
RETURN cond_type_name_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_cont_spec_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_cont_spec_type_id`(cont_spec_type_name_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE cont_spec_type_id_var int(11);
                SELECT cont_spec_type_id INTO cont_spec_type_id_var FROM ob_cont_spec_type
                WHERE UPPER(cont_spec_type) = UPPER(cont_spec_type_name_in);
RETURN cont_spec_type_id_var;
END//
DELIMITER ;

-- Dumping structure for procedure pmwsdb.get_denied_ops
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `get_denied_ops`(process_id_in int(9), user_id int, obj_id int)
BEGIN
    SELECT get_operation_name(DENY_OPERATION_ID) FROM DENY_OPERATION DO, DENY D
    WHERE DO.DENY_ID = D.deny_id
    AND (IS_ASCENDANT_OF(user_id, D.USER_ATTRIBUTE_ID) OR ifnull(process_id_in, D.process_id)=D.process_id)
    AND is_object_in_deny(obj_id, D.DENY_ID, D.IS_INTERSECTION);
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_deny_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_deny_type_id`(deny_type_name_in varchar(45)) RETURNS int(11)
BEGIN
DECLARE deny_type_id_out INT;
                SELECT deny_type.deny_type_id INTO deny_type_id_out FROM deny_type
                WHERE UPPER(name) = UPPER(deny_type_name_in);
RETURN deny_type_id_out;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_host_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_host_id`(hostname varchar(50)) RETURNS int(11)
BEGIN
DECLARE hostid int;
                SELECT HOST_ID INTO hostid FROM HOST WHERE UPPER(HOST_NAME) = UPPER(HOSTNAME);
RETURN hostid;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_host_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_host_name`(host_id_in int(11)) RETURNS varchar(63) CHARSET utf8
BEGIN
DECLARE host_name_out varchar(63);
                SELECT host_name INTO host_name_out FROM HOST WHERE host_id = host_id_in;
RETURN host_name_out;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.GET_HOST_PATH
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `GET_HOST_PATH`(host_id_in int(11)) RETURNS varchar(300) CHARSET utf8
BEGIN
DECLARE workarea_path_out varchar(300);
                SELECT workarea_path INTO workarea_path_out FROM HOST WHERE host_id = host_id_in;
RETURN workarea_path_out;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_node_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_node_id`(node_name varchar(200), node_type varchar(50)) RETURNS int(11)
BEGIN
DECLARE node int;

SELECT DISTINCT NODE_ID
INTO node
FROM NODE
JOIN node_type
on node.node_type_id=node_type.node_type_id
WHERE UPPER(node.NAME) = UPPER(NODE_NAME)
AND UPPER(node_type.name) = UPPER(node_type);

RETURN node;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_node_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_node_name`(node_id_in int(11)) RETURNS varchar(100) CHARSET utf8
BEGIN
DECLARE node_name varchar(100);

SELECT name INTO node_name FROM NODE WHERE node_id = node_id_in;
RETURN node_name;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_node_type
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_node_type`(node_id_in int(11)) RETURNS int(11)
BEGIN
DECLARE type_id INT;
                SELECT node_type_id INTO type_id FROM NODE
                WHERE node_id = node_id_in;
RETURN type_id;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_node_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_node_type_id`(node_type_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE node_type_id_var int(11);
                SELECT node_type_id INTO node_type_id_var FROM node_type
                WHERE UPPER(name) = UPPER(node_type_in);
RETURN node_type_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_node_type_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_node_type_name`(node_type_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE type_name varchar(50);
                /* SELECT node_type.name INTO type_name FROM node, node_type
                WHERE node.node_type_id = node_type.node_type_id*/
                select node_type.name into type_name from node_type where node_type_id=node_type_id_in;
RETURN type_name;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_object_class_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_object_class_name`(obj_class_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE class_name varchar(50);
                SELECT object_class.name INTO class_name FROM object_class
                WHERE object_class.object_class_id = obj_class_id_in;
RETURN class_name;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_operand_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operand_type_id`(operand_type_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE operand_type_id_var int(11);
                SELECT operand_type_id INTO operand_type_id_var FROM ob_operand_type
                WHERE UPPER(operand_type) = UPPER(operand_type_in);
RETURN operand_type_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_operand_type_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operand_type_name`(operand_type_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE operand_type_name_var varchar(11);
                SELECT operand_type INTO operand_type_name_var FROM ob_operand_type
                WHERE UPPER(operand_type_id) = UPPER(operand_type_id_in);
RETURN operand_type_name_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_operations
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operations`(opset_id_in int) RETURNS varchar(500) CHARSET utf8
BEGIN
DECLARE ops_of_opset VARCHAR(500);
                SELECT group_concat(o.name SEPARATOR ',') into ops_of_opset
                from operation_set_details os, operation o
                where os.operation_id = o.operation_id
                GROUP BY os.operation_set_details_node_id
                having os.operation_set_details_node_id = opset_id_in;

                RETURN ops_of_opset;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_operation_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operation_id`(operation_name varchar(45)) RETURNS int(11)
BEGIN
DECLARE op_id INT;
                SELECT operation_id INTO op_id FROM operation
                WHERE UPPER(name) = UPPER(operation_name);
RETURN op_id;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_operation_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operation_name`(operation_id_in int(11)) RETURNS varchar(45) CHARSET utf8
BEGIN
DECLARE op_name varchar(45);
                SELECT name INTO op_name FROM operation
                WHERE UPPER(operation_id) = UPPER(operation_id_in);
RETURN op_name;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_op_spec_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_op_spec_type_id`(op_spec_type_name_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE op_spec_type_id_var int(11);
                SELECT event_id INTO op_spec_type_id_var FROM ob_op_spec_events
                WHERE UPPER(event_name) = UPPER(op_spec_type_name_in);
RETURN op_spec_type_id_var;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.get_user_spec_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_user_spec_type_id`(user_spec_type_name_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE user_spec_type_id_var int(11);
                SELECT user_spec_type_id INTO user_spec_type_id_var FROM ob_user_spec_type
                WHERE UPPER(user_spec_type) = UPPER(user_spec_type_name_in);
RETURN user_spec_type_id_var;
END//
DELIMITER ;

-- Dumping structure for table pmwsdb.host
CREATE TABLE IF NOT EXISTS `host` (
  `host_id` int(11) NOT NULL AUTO_INCREMENT,
  `host_name` varchar(63) NOT NULL,
  `workarea_path` varchar(300) NOT NULL,
  PRIMARY KEY (`host_id`),
  KEY `idx_host_host_name` (`host_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='host machine info';

-- Dumping data for table pmwsdb.host: ~2 rows (approximately)
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` (`host_id`, `host_name`, `workarea_path`) VALUES
	(1, 'Dummy_host', 'dummy');
/*!40000 ALTER TABLE `host` ENABLE KEYS */;

-- Dumping structure for function pmwsdb.isValidCSL
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `isValidCSL`(
_textIn TEXT
) RETURNS tinyint(1)
    NO SQL
BEGIN

RETURN _textIn IS NOT NULL && (_textIn = '' || _textIn REGEXP '^([1-9][0-9]{2},)*[1-9][0-9]{2}?$');

END//
DELIMITER ;

-- Dumping structure for function pmwsdb.is_accessible
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `is_accessible`(ua_id_in int(11), oa_id_in int(11)) RETURNS tinyint(1)
BEGIN
DECLARE is_accessible boolean;
DECLARE policy_id_in int;
DECLARE done boolean DEFAULT FALSE;
DECLARE policies CURSOR FOR select a.start_node_id as policy_id from assignment a where get_node_type(a.start_node_id) = 2 and end_node_id = oa_id_in;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  OPEN policies;

  check_loop: LOOP
    FETCH policies INTO policy_id_in;
    IF done THEN
      LEAVE check_loop;
    END IF;
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END into is_accessible
    from association as a
    where is_member(ua_id_in, a.ua_id)
    and is_member(oa_id_in, a.oa_id)
    and is_member(ua_id_in, policy_id_in);
    IF not is_accessible then
                                leave check_loop;
                END IF;
  END LOOP;

  CLOSE policies;
RETURN is_accessible;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.is_ascendant_of
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `is_ascendant_of`(ascendant_node_id int,descendant_node_id int) RETURNS tinyint(4)
BEGIN
DECLARE cnt INT;

SELECT COUNT(*) INTO cnt FROM ASSIGNMENT A
WHERE A.START_NODE_ID = descendant_node_id
AND A.END_NODE_ID = ascendant_node_id;

IF cnt > 0 THEN
  RETURN TRUE;
ELSE RETURN FALSE;
END IF;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.is_member
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `is_member`(member_id_in int(11), container_id_in int(11)) RETURNS tinyint(1)
BEGIN
DECLARE is_member boolean;
                if member_id_in = container_id_in then
    return true;
    end if;
                SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END into is_member
    from assignment
    where start_node_id = container_id_in
    and end_node_id = member_id_in;
    return is_member;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.is_object_in_deny
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `is_object_in_deny`(obj_id int,deny_id_in int, is_intersection int) RETURNS tinyint(1)
BEGIN
DECLARE row_cnt INT;
DECLARE deny_obj_cnt INT;
SELECT COUNT(*) INTO deny_obj_cnt
FROM DENY_OBJ_ATTRIBUTE
WHERE DENY_ID = deny_id_in;
IF is_intersection THEN
    SELECT COUNT(*) INTO row_cnt FROM DENY_OBJ_ATTRIBUTE D
    WHERE D.deny_id = deny_id_in
    
    AND ((is_ascendant_of(obj_id,D.object_attribute_id) AND NOT object_complement)
                OR (!is_ascendant_of(obj_id,D.object_attribute_id) AND object_complement));
    IF row_cnt = 0 OR row_cnt < deny_obj_cnt THEN
      RETURN FALSE;
    ELSE
      RETURN TRUE;
    END IF;
ELSE  
    SELECT COUNT(*) INTO row_cnt FROM DENY_OBJ_ATTRIBUTE D
    WHERE D.deny_id = deny_id_in
    AND ((is_ascendant_of(obj_id,D.object_attribute_id) AND NOT object_complement)
    OR (!is_ascendant_of(obj_id,D.object_attribute_id) AND object_complement));
    IF row_cnt > 0 THEN
      RETURN TRUE;
    ELSE
      RETURN FALSE;
    END IF;
END IF;
END//
DELIMITER ;

-- Dumping structure for table pmwsdb.keystore
CREATE TABLE IF NOT EXISTS `keystore` (
  `host_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_node_id` int(11) NOT NULL,
  `keystore_path` varchar(300) DEFAULT NULL,
  `truststore_path` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`host_id`,`user_node_id`),
  KEY `user_id_idx` (`user_node_id`),
  CONSTRAINT `fk_host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`host_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_node_id` FOREIGN KEY (`user_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='host machine info';

-- Dumping data for table pmwsdb.keystore: ~0 rows (approximately)
/*!40000 ALTER TABLE `keystore` DISABLE KEYS */;
/*!40000 ALTER TABLE `keystore` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.node
CREATE TABLE IF NOT EXISTS `node` (
  `node_id` int(11) NOT NULL AUTO_INCREMENT,
  `node_type_id` int(11) NOT NULL,
  `name` varchar(200) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`node_id`),
  KEY `node_type_id_idx` (`node_type_id`),
  CONSTRAINT `fk_node_type_id` FOREIGN KEY (`node_type_id`) REFERENCES `node_type` (`node_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table contains all the nodes in the graph';

-- Dumping data for table pmwsdb.node: ~1,170 rows (approximately)
/*!40000 ALTER TABLE `node` DISABLE KEYS */;
INSERT INTO `node` (`node_id`, `node_type_id`, `name`, `description`) VALUES
	(-3, 4, 'super', NULL),
	(-2, 3, 'super', NULL),
	(-1, 2, 'Super PC', NULL),
	(1, 2, 'pm_health', NULL),
	(2, 5, 'pm_health', NULL),
	(3, 5, 'diagnoses', NULL),
	(4, 5, 'Columns', NULL),
	(5, 5, 'patient_id', NULL),
	(6, 5, 'visit_id', NULL),
	(7, 5, 'diagnosis', NULL),
	(8, 5, 'Rows', NULL),
	(9, 5, '1+6', NULL),
	(10, 6, '66a20189-5b25-4a70-98a9-8b967ee3cf5e', NULL),
	(11, 6, 'd5321905-b050-40ce-92dd-c1235ef5288d', NULL),
	(12, 6, '2b8451e7-2934-4371-9b64-ad7903714ab9', NULL),
	(13, 5, '1+7', NULL),
	(14, 6, '470bfebf-f975-469f-8355-d71f2db5abff', NULL),
	(15, 6, '313de9f8-7901-48a0-bca4-66e1a24d953c', NULL),
	(16, 6, 'b4a6266c-63fc-406d-bca0-905780c2f50a', NULL),
	(17, 5, '1+8', NULL),
	(18, 6, 'ea9b4cfa-8209-416e-a73e-43f9c2547a60', NULL),
	(19, 6, 'd27b310c-4957-466c-b8c0-33627771d203', NULL),
	(20, 6, 'ecef620d-407e-49ba-800a-1005b9d2e271', NULL),
	(21, 5, '2+9', NULL),
	(22, 6, 'd1e125ab-57ca-4349-81c5-05678dbea483', NULL),
	(23, 6, 'd2d05499-92ae-4ed8-b81e-ac9a737435be', NULL),
	(24, 6, '9cc3c6fc-c0fe-4188-a05f-1f015db031bf', NULL),
	(25, 5, 'links', NULL),
	(26, 5, 'Columns', NULL),
	(27, 5, 'link_id', NULL),
	(28, 5, 'name', NULL),
	(29, 5, 'type', NULL),
	(30, 5, 'Rows', NULL),
	(31, 5, '1+Patients', NULL),
	(32, 6, 'a5954027-42cc-4b87-9bc3-2f3cc7fc650d', NULL),
	(33, 6, '6945c4fe-004c-4584-ae64-e7fab149931b', NULL),
	(34, 6, 'ccaa37ea-7dcc-4366-94aa-243322b9dd62', NULL),
	(35, 5, '3+Medicines', NULL),
	(36, 6, '308f326a-94e3-4287-8e41-9cef85623a80', NULL),
	(37, 6, '7efd705f-e235-4568-8091-130782465b15', NULL),
	(38, 6, 'fc41906a-c582-487b-9021-50f34c639cc4', NULL),
	(39, 5, '4+My Record', NULL),
	(40, 6, '4a783d67-dbd5-48c3-8f27-d12aac0229a0', NULL),
	(41, 6, '715a538a-c372-4052-847c-ec44a39dc5b5', NULL),
	(42, 6, 'b5c2cf75-e5d2-41b9-a7a2-7b2a9f72115c', NULL),
	(43, 5, '5+Messages', NULL),
	(44, 6, '8eb94cd2-6771-4dd9-ba39-062749fb173a', NULL),
	(45, 6, 'c7450cbb-be89-4162-a90d-b7f33be3d24d', NULL),
	(46, 6, 'd822ac8a-e2bf-40da-9483-39ea37e1041d', NULL),
	(47, 5, '6+doctor home', NULL),
	(48, 6, '3144cade-d7c5-4ae3-b103-f6f8ff2e1806', NULL),
	(49, 6, '0fc96978-93eb-421c-bf22-881b5d290c01', NULL),
	(50, 6, '25403331-1ff1-4846-8d2e-9431d9f70674', NULL),
	(51, 5, '7+patient home', NULL),
	(52, 6, 'd86e3460-18be-47bc-bbf4-edb11ddfd766', NULL),
	(53, 6, 'e1577555-3f3d-434b-aa63-35e5f0360acf', NULL),
	(54, 6, '9606b258-038c-4d54-81a5-2715a4754910', NULL),
	(55, 5, '8+nurse home', NULL),
	(56, 6, '12973d11-334b-438f-99d9-fae5f9cc0914', NULL),
	(57, 6, '014dfa17-b79a-4040-8509-5f427d76dc3f', NULL),
	(58, 6, '13624ac7-2095-47c9-adf7-7f1127cbf0d4', NULL),
	(59, 5, '9+clerk home', NULL),
	(60, 6, '4dc1375f-d891-450e-a0ec-b8617f45ac3a', NULL),
	(61, 6, '9476fe5c-d56f-4586-9d89-7f145c2b856e', NULL),
	(62, 6, 'd6da46e2-f900-47ed-9cb5-8bffb61709a8', NULL),
	(63, 5, '10+Start A Visit', NULL),
	(64, 6, '1d6ff2ce-94cb-4b1a-a766-990ba60ed6af', NULL),
	(65, 6, 'ac4f51de-c108-4e9a-aeea-70c9110914dc', NULL),
	(66, 6, '90c7e283-9c56-4b96-8507-67a7528665b4', NULL),
	(67, 5, '11+Delegate Record', NULL),
	(68, 6, '1c1ae0ee-f461-44ab-95ef-6b03636ca827', NULL),
	(69, 6, '17651b47-2659-4148-ad81-28eabed8e269', NULL),
	(70, 6, '3a64a3f2-8545-4f5c-97c0-955ed7840b6e', NULL),
	(71, 5, '12+Request To Delegate', NULL),
	(72, 6, '67a7749d-9584-426d-bf65-023203bc288d', NULL),
	(73, 6, 'fd3a938b-ba68-4f03-b55f-d1372b68ceac', NULL),
	(74, 6, '107a6778-1cd9-47a0-8449-c6975b1e3535', NULL),
	(75, 5, '13+Delegations', NULL),
	(76, 6, 'c171d77c-65dc-49fe-9192-258891b11459', NULL),
	(77, 6, '9fe03dca-89aa-48df-a8c6-25547e55ef63', NULL),
	(78, 6, '24d005f1-27aa-4c94-8c5a-0cd07ebd920b', NULL),
	(79, 5, 'medicines', NULL),
	(80, 5, 'Columns', NULL),
	(81, 5, 'med_id', NULL),
	(82, 5, 'name', NULL),
	(83, 5, 'dosage', NULL),
	(84, 5, 'Rows', NULL),
	(85, 5, '21818', NULL),
	(86, 6, 'c7776133-62fb-4e66-a724-589a151ba9bd', NULL),
	(87, 6, 'cbacf1b6-f9d5-4795-afd5-286c522d35a2', NULL),
	(88, 6, '12b847fb-ac87-4b53-83da-460d96ee841e', NULL),
	(89, 5, '21819', NULL),
	(90, 6, 'd942170b-df9c-4623-ae11-46eeaaa4ef32', NULL),
	(91, 6, '59c52f76-0f49-47c8-a6b0-ab7de06eca07', NULL),
	(92, 6, '523e0d48-58dc-460c-bd13-5869fb25c49d', NULL),
	(93, 5, '21823', NULL),
	(94, 6, '000e38da-c668-46b4-843e-0167099b1d03', NULL),
	(95, 6, 'ccec3323-c54f-4653-a741-0dda7986fa9d', NULL),
	(96, 6, '911a633f-834c-4f7d-b499-778773c0f563', NULL),
	(97, 5, '21831', NULL),
	(98, 6, 'df3f980a-014d-449c-9775-cfddb921c45b', NULL),
	(99, 6, 'a849a010-8a16-4cf7-8273-f4c6290637cd', NULL),
	(100, 6, '72871258-cb8a-46fa-99c2-836ab1975477', NULL),
	(101, 5, '21833', NULL),
	(102, 6, 'ac01ca38-8ae0-4d3c-8143-bcd232f512bc', NULL),
	(103, 6, '22e0a0dc-b5d8-48f2-b436-5c85b2bef265', NULL),
	(104, 6, '082e0d81-1fda-46a2-b3c7-39835d90f600', NULL),
	(105, 5, '21837', NULL),
	(106, 6, '8b9e7049-ff68-46fc-b171-25b071a2a220', NULL),
	(107, 6, '86d30aef-b657-4c1e-89f9-4292cf4efc0d', NULL),
	(108, 6, 'e418de86-a442-4670-a69f-149e87349afb', NULL),
	(109, 5, '21838', NULL),
	(110, 6, 'ae33fdb6-b4af-4316-8a47-40e035d39344', NULL),
	(111, 6, 'ddee09fc-d0cb-4764-8078-1b09ed5ca892', NULL),
	(112, 6, '5028928f-5547-489a-990b-0bb27dc9a797', NULL),
	(113, 5, '21839', NULL),
	(114, 6, '3ccb766b-bb9a-4f3f-91de-3c9d26b29591', NULL),
	(115, 6, '2b96c26b-644a-41d9-8c2a-1074fb15b2ea', NULL),
	(116, 6, 'b16bdc38-4bf3-4d41-96c8-0938773cd905', NULL),
	(117, 5, '21840', NULL),
	(118, 6, '8c9805d0-6f73-4ac9-a35a-8fed5d1c8e74', NULL),
	(119, 6, 'b5dddad4-c053-47e1-a4f2-4b27f8e87647', NULL),
	(120, 6, '2939def3-975b-4028-892f-8f169acc3f1d', NULL),
	(121, 5, '21843', NULL),
	(122, 6, '181ff1de-867b-45c8-8540-be56785042a5', NULL),
	(123, 6, '64f4dc2d-3632-481c-92f1-672a39e0623c', NULL),
	(124, 6, '13515db4-3e7b-4b0a-a420-287791a3187c', NULL),
	(125, 5, '21844', NULL),
	(126, 6, '95175091-df57-440d-88ef-9b8a0cbd2230', NULL),
	(127, 6, 'cedea5c2-97c4-4f78-b38b-f48be188dfc1', NULL),
	(128, 6, '814c59c0-0d2a-474e-b4ff-32b0518e24ff', NULL),
	(129, 5, '21845', NULL),
	(130, 6, '69ea51c9-bcc8-464e-89c0-d062f4fdf7aa', NULL),
	(131, 6, '0ec2128e-b5c2-44f4-89f6-a03f1e06cebd', NULL),
	(132, 6, '280e93ee-0ae1-4a44-b9c3-42780c463c8f', NULL),
	(133, 5, '21846', NULL),
	(134, 6, 'b5ab95a5-8369-47ad-892a-862487d13384', NULL),
	(135, 6, '6cb5d34a-1f86-4870-9848-c599d93bddd3', NULL),
	(136, 6, '11eab774-3a2f-4128-9cf2-8ea2a5cc3806', NULL),
	(137, 5, '21849', NULL),
	(138, 6, '5fbee992-fd95-48de-86be-65c328e8fec1', NULL),
	(139, 6, '22fd9929-4d23-4da2-89d5-c6dd3513192e', NULL),
	(140, 6, '4c8248f6-0d4a-4a66-a8b4-9dd6105b87f6', NULL),
	(141, 5, '21850', NULL),
	(142, 6, '87314dfd-e5b6-4751-82f5-f272f9eba6de', NULL),
	(143, 6, '6b27dd96-cc97-4e1d-8000-aff82747e1a9', NULL),
	(144, 6, '9e96c472-9a27-4719-b365-69d5d619c4c0', NULL),
	(145, 5, '21851', NULL),
	(146, 6, '2483d5d8-68e9-42b8-a4a0-f333c8b942ee', NULL),
	(147, 6, '644f6e29-58d8-4231-8d88-c7601af05812', NULL),
	(148, 6, '2ad0d672-04cc-470b-a3fc-776fba70f440', NULL),
	(149, 5, '21852', NULL),
	(150, 6, '4320b524-554e-453f-a3c3-e3927f167dc9', NULL),
	(151, 6, 'b9b027d5-7f54-4d36-944a-629e375f1dba', NULL),
	(152, 6, '60ced0fd-46e7-4f94-b0d7-2bc2230c26f6', NULL),
	(153, 5, '21853', NULL),
	(154, 6, 'c66adc32-2837-40fb-8682-bc4948c65732', NULL),
	(155, 6, '3c1fec43-0593-4b7d-a596-d996a2cedb4c', NULL),
	(156, 6, '759aa9b1-112c-4d68-9b1c-86284851f238', NULL),
	(157, 5, '21856', NULL),
	(158, 6, 'ae86cabd-5a78-4d29-8ee6-fab18e2e4ffe', NULL),
	(159, 6, '6faaeee1-4814-442f-bf43-56a0b1c168d9', NULL),
	(160, 6, '42a00267-efc2-4378-a695-3f635afe4f16', NULL),
	(161, 5, '21857', NULL),
	(162, 6, 'b0412b1a-6e1a-48c9-9f38-291f2961ad6d', NULL),
	(163, 6, '6c26a706-efd4-4add-879f-c74fb21c752a', NULL),
	(164, 6, 'dff0f1a3-9ca7-4e43-a46c-0ed5e2b5d657', NULL),
	(165, 5, '21858', NULL),
	(166, 6, '868759e0-aed0-429c-a301-5f17fe19fd39', NULL),
	(167, 6, 'd9aeb5d4-530d-4475-a145-21ed05ba4080', NULL),
	(168, 6, 'aca57a2f-acd0-47c3-8279-c2f386907c2e', NULL),
	(169, 5, '21859', NULL),
	(170, 6, '4258e8a9-4b52-46f0-b3be-989b70bdbb20', NULL),
	(171, 6, '313dde21-c5aa-499e-891e-a8f98beb4a63', NULL),
	(172, 6, '29337e26-a662-4c4f-a11d-5df15dcd98b6', NULL),
	(173, 5, '21860', NULL),
	(174, 6, 'ab572b35-ffa0-431d-9826-757c9ce8deff', NULL),
	(175, 6, 'e1bfdc14-7b20-40dc-9287-96feb9974dfe', NULL),
	(176, 6, '20a766c3-abf8-4237-b276-447b2139109b', NULL),
	(177, 5, '21861', NULL),
	(178, 6, '5c16689e-abd6-4244-b9e3-958e0ff81486', NULL),
	(179, 6, '636e4ea7-631d-46bc-874a-3cb3aeeb4678', NULL),
	(180, 6, '81db910d-4953-4767-aafe-4d3685f377b6', NULL),
	(181, 5, '21862', NULL),
	(182, 6, '0f12f50f-b487-4cbb-9a76-f5901678b720', NULL),
	(183, 6, '9891bf88-858f-42c7-93ea-03b50565179d', NULL),
	(184, 6, '07b183ff-17e1-4fd6-bd76-cdcc5c4c76a3', NULL),
	(185, 5, 'patient_info', NULL),
	(186, 5, 'Columns', NULL),
	(187, 5, 'patient_id', NULL),
	(188, 5, 'user_id', NULL),
	(189, 5, 'name', NULL),
	(190, 5, 'dob', NULL),
	(191, 5, 'gender', NULL),
	(192, 5, 'ssn', NULL),
	(193, 5, 'race', NULL),
	(194, 5, 'marital_status', NULL),
	(195, 5, 'cell_phone', NULL),
	(196, 5, 'work_phone', NULL),
	(197, 5, 'home_phone', NULL),
	(198, 5, 'email', NULL),
	(199, 5, 'address', NULL),
	(200, 5, 'Rows', NULL),
	(201, 5, '1', NULL),
	(202, 6, 'cbb084c8-d1f0-44b0-a7bd-0e5034668813', NULL),
	(203, 6, '5e095e0e-dfd3-4cf0-8302-c009135c72f6', NULL),
	(204, 6, 'ac3d0ea9-5c6d-48b5-ac53-f198acb76f07', NULL),
	(205, 6, '85701586-f904-485c-80de-2ff483851229', NULL),
	(206, 6, 'a5963478-82a8-4807-b55e-5cd5783ba259', NULL),
	(207, 6, 'f7da75ef-2dbc-4278-8648-c25236a5ff5c', NULL),
	(208, 6, '08839500-7fa0-4fd2-b31f-398c8c774a05', NULL),
	(209, 6, '75d77385-afee-4b16-aaa6-2f2de26a9b37', NULL),
	(210, 6, '45007175-5bb4-4aa8-bb3f-eed6079e3863', NULL),
	(211, 6, '466e848c-a0e2-4f8b-959f-943c9297b96e', NULL),
	(212, 6, '8c48158e-7a96-48f0-968a-a467b467b653', NULL),
	(213, 6, 'ec002db2-d541-492f-a7d8-7db16aa05a45', NULL),
	(214, 6, '11624719-83e6-4cf1-bf77-5c06eaaf9a5d', NULL),
	(215, 5, '2', NULL),
	(216, 6, '4d07142a-960c-4bc1-8d4d-f422cddc2fb6', NULL),
	(217, 6, '5e6a5eb7-28e0-4222-bfc9-d2227edc7f50', NULL),
	(218, 6, 'fa5752c3-a3fc-486e-be06-241db0421fc3', NULL),
	(219, 6, '03b2ff44-2ed4-437a-9ed0-7cfe00d1f30a', NULL),
	(220, 6, '01847bee-6e77-4ba2-b1ac-202abee1d7f1', NULL),
	(221, 6, '002d9114-dc41-49f1-8a2e-56750f9755aa', NULL),
	(222, 6, '0ebc2a76-54c2-46e9-8df1-d098b2a99b8f', NULL),
	(223, 6, 'b54d5868-c20a-44df-a413-a9df01f12a54', NULL),
	(224, 6, '05552b8c-8fa3-4a14-9aee-c9c1fead9f1a', NULL),
	(225, 6, '4a0bd32e-bbd6-4104-8f5f-32d2c140393e', NULL),
	(226, 6, '81ed8d18-14ec-420a-87d0-7475420d3a54', NULL),
	(227, 6, '067f6128-2410-48cc-8f7c-abee3151a0cb', NULL),
	(228, 6, 'ac4c2306-5c15-4449-982e-60f9c5b7e11d', NULL),
	(229, 5, 'prescriptions', NULL),
	(230, 5, 'Columns', NULL),
	(231, 5, 'prescription_id', NULL),
	(232, 5, 'visit_id', NULL),
	(233, 5, 'medicine', NULL),
	(234, 5, 'dosage', NULL),
	(235, 5, 'duration', NULL),
	(236, 5, 'Rows', NULL),
	(237, 5, '5', NULL),
	(238, 6, '84927b98-b690-4570-8e61-2f245dbb67cd', NULL),
	(239, 6, '5e5c0a7e-feb2-4a5d-823e-100191af934c', NULL),
	(240, 6, '516d434f-927d-4500-906e-ef8dbcc75d31', NULL),
	(241, 6, '58ffe57c-58e8-4608-8444-526f93d65837', NULL),
	(242, 6, '8693b472-263a-465c-9828-bb3061a6fe4a', NULL),
	(243, 5, '6', NULL),
	(244, 6, '73b995df-4daf-4f0a-b641-1541255ea411', NULL),
	(245, 6, 'ce232478-9bce-4509-afb4-cf6ef3e58edf', NULL),
	(246, 6, '055c49e7-17fd-4c55-9acc-36cabc131eb6', NULL),
	(247, 6, '01ef1cef-7179-49a4-91b8-908ccd31740b', NULL),
	(248, 6, '2cc88458-cb02-40b9-bfff-507468ef5f4a', NULL),
	(249, 5, '7', NULL),
	(250, 6, '75593f9d-380d-4860-a452-f8d717aaa6b2', NULL),
	(251, 6, 'eac60c1e-46b7-4fec-976e-ec63274aa261', NULL),
	(252, 6, '95305954-1238-430c-8ced-38df0b3bd2a0', NULL),
	(253, 6, '9b545108-30a1-499d-8c7e-504853248128', NULL),
	(254, 6, '9801cfd1-e53d-4bba-9cec-428691b232b6', NULL),
	(255, 5, '8', NULL),
	(256, 6, '2a1ae2af-61e9-4ae9-8f57-7b14e63ae217', NULL),
	(257, 6, '936a969a-54c4-42ad-8553-fc9943a6a794', NULL),
	(258, 6, '3c0c5d30-e947-4cdc-8ca8-fdfa8fd95de4', NULL),
	(259, 6, '65872022-11a7-4b61-9700-9234c8cbdb44', NULL),
	(260, 6, 'f3010d05-620d-42e7-86ac-70ffe33b15af', NULL),
	(261, 5, '9', NULL),
	(262, 6, '7d4599c6-b3c4-4220-9c7b-7439dd82b01a', NULL),
	(263, 6, 'b7c5e9e8-e8af-47a2-b7e3-2eae5b7d771f', NULL),
	(264, 6, 'af610aa2-024c-4b77-8cce-38e259b9bebb', NULL),
	(265, 6, '03c03b2e-69de-468b-8013-8d6e9306fe0e', NULL),
	(266, 6, 'a781acbd-509f-4993-beae-3e9bc4cc0828', NULL),
	(267, 5, '10', NULL),
	(268, 6, 'c88b701f-911b-451a-89e8-b328fd34ad29', NULL),
	(269, 6, '63af3730-79a6-4a4a-908a-4ee73136d82f', NULL),
	(270, 6, '1d05063b-cbbd-4a49-873e-f4a19cb8d31f', NULL),
	(271, 6, '880073a5-7b53-4a0f-9bf0-4975bd4a617c', NULL),
	(272, 6, '2610bc22-ae93-4b0e-9e58-d2910b8bfec7', NULL),
	(273, 5, '11', NULL),
	(274, 6, '11ebc1e1-310f-4f54-977a-27d59015a2b3', NULL),
	(275, 6, '57dd2a40-5e3c-4a7a-8921-ce20e207c1b2', NULL),
	(276, 6, '083ad5dd-f7c1-4a72-9f24-bdde5d16ed46', NULL),
	(277, 6, '6a4f8fdf-71af-44e0-a6a9-ff6cca41fdc1', NULL),
	(278, 6, '4faa684d-73b2-4de9-88da-886ed0d99811', NULL),
	(279, 5, 'requests', NULL),
	(280, 5, 'Columns', NULL),
	(281, 5, 'request_id', NULL),
	(282, 5, 'subject', NULL),
	(283, 5, 'target', NULL),
	(284, 5, 'approved', NULL),
	(285, 5, 'Rows', NULL),
	(286, 5, '1', NULL),
	(287, 6, '565ea5d9-1095-4a44-8eda-c065dd9542e7', NULL),
	(288, 6, 'be1fbbfd-c30e-4387-b456-3a8e640aa865', NULL),
	(289, 6, '2bf2b6f9-8146-4f3f-9174-27481699ed91', NULL),
	(290, 6, '860d1993-4f6f-44b1-8e3a-8e11bc3d7435', NULL),
	(291, 5, '2', NULL),
	(292, 6, '8aa292d7-5d7d-499a-b952-a2c3ea2af032', NULL),
	(293, 6, '963663fc-191c-4992-a4d9-d0e1c790bda0', NULL),
	(294, 6, '59295472-14b9-4cad-8f4a-557b6d5af2cc', NULL),
	(295, 6, 'fea0daf8-2f8a-45af-a246-7f52ee951a6e', NULL),
	(296, 5, 'sessions', NULL),
	(297, 5, 'Columns', NULL),
	(298, 5, 'session_id', NULL),
	(299, 5, 'user_id', NULL),
	(300, 5, 'Rows', NULL),
	(301, 5, '002125bb35d945e694cc1e588f34d109', NULL),
	(302, 6, '50dc0410-bb40-43bb-93fc-ffd719d82e05', NULL),
	(303, 6, '37f5ef86-4ce1-490b-9670-df09333894dc', NULL),
	(304, 5, '02b702d1a8714bceac445f90c7bae6f8', NULL),
	(305, 6, 'ac52b960-4fa7-4f63-9a6c-7a90ffcb2111', NULL),
	(306, 6, '3d43a85e-a089-4b90-8850-dad6c4b0dd10', NULL),
	(307, 5, '050853b17844495cb01ff9ab0862aa36', NULL),
	(308, 6, 'd02eb149-ced6-4482-91cd-520d18b43f14', NULL),
	(309, 6, 'b20b83a1-a351-457b-a727-b627937e5716', NULL),
	(310, 5, '05da0eae325f4612a3db8d3ddcb7af47', NULL),
	(311, 6, 'ef4aff65-16c5-419d-bfa8-c8bb4b900244', NULL),
	(312, 6, '173ab4cc-c9dd-4d0d-8802-891645e63778', NULL),
	(313, 5, '0670ea09a3ef4ef39218fe9f35e0caf9', NULL),
	(314, 6, 'bb8663cf-7d50-4a4c-b21e-a40d75e4df9f', NULL),
	(315, 6, 'f6d43eb2-9464-4ba7-bf65-1be5665837ce', NULL),
	(316, 5, '0a77581e36674bb3a8989781827ff343', NULL),
	(317, 6, '00d44a83-b57f-40a0-9849-502b38391ed6', NULL),
	(318, 6, '70909f56-fc3b-4fbf-8b1b-1e449017efd5', NULL),
	(319, 5, '0e3dc92c8c9942cbaa761520ecefa128', NULL),
	(320, 6, 'dc69e735-18c9-4c4b-ad6d-c72c6e73cad0', NULL),
	(321, 6, '6e13e0e2-feab-468f-9aee-73988d379ac1', NULL),
	(322, 5, '0ff6150476eb4151aca2c80033539cf0', NULL),
	(323, 6, '7626e3bc-47b0-461b-b485-d46c0173a9ff', NULL),
	(324, 6, 'cc874475-3625-4752-a34a-bc6e262e56c3', NULL),
	(325, 5, '103020324c9647f6bbf5c895cdc5fc77', NULL),
	(326, 6, '3f00e07a-238f-47ca-9ece-4a5dc130fcef', NULL),
	(327, 6, 'df67784e-0499-498d-9aa4-961d5c23b50b', NULL),
	(328, 5, '123e226e5e7e43d3abb53ef94fd51559', NULL),
	(329, 6, '928ccb2f-8ab2-4437-9e98-098b386d5aea', NULL),
	(330, 6, '2dbf3010-d596-4f4b-8bcb-e6d6dd8bb56b', NULL),
	(331, 5, '13dd1b2a863445ffb3e952b76a062cbd', NULL),
	(332, 6, 'c2742c12-8e95-422b-b6d9-dc0d64459138', NULL),
	(333, 6, '6b3ca2d6-45c0-4670-bd56-e9a7e414e713', NULL),
	(334, 5, '1596e16e11394988a8c5ce99ba7cbed1', NULL),
	(335, 6, 'e1076dc7-90eb-4bef-97c1-2d5f373f37c3', NULL),
	(336, 6, '2c2f2e64-0a03-43ac-a0b8-a655c622b3f6', NULL),
	(337, 5, '189795ad0eef4dabb362fb00bc24fb0c', NULL),
	(338, 6, '4c37babf-7b46-4992-93d6-205e4619be5c', NULL),
	(339, 6, 'b435aeaa-8843-4a1a-9f76-6bd998f7b63d', NULL),
	(340, 5, '1b231b17ff894de5898575031e3aaf50', NULL),
	(341, 6, '5d0ee9fa-ec85-43b3-a619-ab683ba173b6', NULL),
	(342, 6, 'f8a939a3-162e-4926-80b1-dcbfd7686721', NULL),
	(343, 5, '1cb9d90a52b646bb93d3da539076d5e8', NULL),
	(344, 6, 'e8e7c6aa-e92b-4de4-a8ea-59bd357aee4f', NULL),
	(345, 6, '76881bba-160c-4072-88f6-c24524776166', NULL),
	(346, 5, '1ce288e2e82c49f889d8e80e16bd2b28', NULL),
	(347, 6, '2c318206-061d-4ef8-b5dc-ff22c3b06a30', NULL),
	(348, 6, '175118bd-b69c-4f05-abc7-a73b717dfc4e', NULL),
	(349, 5, '1d56fbb08a34432b8ad746ed81988765', NULL),
	(350, 6, 'c5916c05-25fa-433c-9e7f-076177ec375e', NULL),
	(351, 6, '85e88d00-ca8d-4349-8976-29ebba411920', NULL),
	(352, 5, '1dda1d9859c544169e0921fd5a156727', NULL),
	(353, 6, '90ab970d-0837-4e60-8518-a66870427b3c', NULL),
	(354, 6, 'cf60b1e9-8054-41a8-991f-a838a661819c', NULL),
	(355, 5, '1fe815e3a47c465a8a6ba5a17b416abd', NULL),
	(356, 6, '2bdec9e5-65c9-4a2c-a4e2-e6b0e9683ec7', NULL),
	(357, 6, '432a188e-1437-4093-9620-1790ed4145bf', NULL),
	(358, 5, '21dd80ecec4442d2bfb4ea03596bc623', NULL),
	(359, 6, '2234e013-1de0-44c4-9295-7415c4a7fb0b', NULL),
	(360, 6, 'c3243660-2a5e-480c-ac7c-525dd00c5296', NULL),
	(361, 5, '25cedd9efa724073b191d18e290af207', NULL),
	(362, 6, '58635c09-bdb1-414c-842b-1116c7b8cb79', NULL),
	(363, 6, 'd4f271f6-eb78-4cba-8fda-e372dce439cd', NULL),
	(364, 5, '269c83202df346ef92108667a7e22c7e', NULL),
	(365, 6, 'f49ce480-1037-4474-972c-c0b4f2c83d93', NULL),
	(366, 6, '9acfdff3-f54a-4146-ae5a-20923fd4baa6', NULL),
	(367, 5, '272c91751be84981a7917ce0b0deb011', NULL),
	(368, 6, '632906ca-7e20-456b-a712-b4a201d18adc', NULL),
	(369, 6, 'b86b5bbe-3dee-4037-8c4f-dde4f61df0a5', NULL),
	(370, 5, '2af79e0b3577422db99107d5ebc3274d', NULL),
	(371, 6, '991ee27d-39d2-4043-b218-8cd408d0cc72', NULL),
	(372, 6, 'a5043aa6-a663-44a1-a4c5-712c85c82932', NULL),
	(373, 5, '2d5921504d524a4881f448fbf38f5f72', NULL),
	(374, 6, '107a163c-1af6-4d6f-a655-93af5acc8ba5', NULL),
	(375, 6, '33006ad6-d1e0-417e-9748-172196e20dea', NULL),
	(376, 5, '2f86d4d1c47a4c0c8600872212c271a6', NULL),
	(377, 6, '79f3f4e7-2f92-4e20-b664-e571c438ccb6', NULL),
	(378, 6, '88c202c8-ca82-47c0-a5c3-fbe73ee1d6d0', NULL),
	(379, 5, '314ce43e89674d8fbc1c5578fd8df4af', NULL),
	(380, 6, '90092795-b6c2-4223-b3b4-3e8935833c2b', NULL),
	(381, 6, 'cd93785e-280d-4728-b624-8195ca34e199', NULL),
	(382, 5, '3342f6b892d14a1abae21c90f5dc6046', NULL),
	(383, 6, 'e47093bb-48ed-416f-b87c-4e99bea8caf2', NULL),
	(384, 6, '271c78fb-aed8-4185-bc5e-5c7b5ebece5f', NULL),
	(385, 5, '3632183bad744048b39da63cb68fbfc7', NULL),
	(386, 6, '66696d25-6d6e-4256-a361-ec7b96968a8f', NULL),
	(387, 6, 'a9e414b4-1fd2-4ecf-9645-2cc019585970', NULL),
	(388, 5, '37061df04ba34ae682cb363462e4e3f8', NULL),
	(389, 6, '2ab6ee98-4052-4027-885e-8a60c39333fe', NULL),
	(390, 6, '9f1ba5e9-f102-41ca-a125-ccea1e75a972', NULL),
	(391, 5, '3788f55a133f4fef915f663c76ff34ec', NULL),
	(392, 6, 'c5f413fa-dc0c-45f4-804f-419ac1f1299c', NULL),
	(393, 6, '5df06064-43dc-4e1b-bd91-bcdbf3a8d6cc', NULL),
	(394, 5, '3c6a90c2dfd24ee9ad4a86393874d9f3', NULL),
	(395, 6, '9066b70b-1e01-4481-9c5d-33808200208c', NULL),
	(396, 6, '6ab206d9-ed0f-4cc6-be53-d62d4c90c17c', NULL),
	(397, 5, '40694379c4a34bcc8ebe191a1145e2c9', NULL),
	(398, 6, 'a581c47c-924a-461b-964f-cf80cda50242', NULL),
	(399, 6, 'd7b7c70c-9fb2-4626-a7e8-289793669f65', NULL),
	(400, 5, '40ef95d963974380ac487f0c0f1922ec', NULL),
	(401, 6, '37dfc45c-4b3a-4f84-bd3a-e5d46c5177a7', NULL),
	(402, 6, '3ee139ae-5b0e-4366-a63f-40d2551075dd', NULL),
	(403, 5, '41105578e7d84fbf995b312bed078d2d', NULL),
	(404, 6, '6b8fcada-7122-45a2-a781-a9abaaf5a8f9', NULL),
	(405, 6, 'e5951ad6-e5b4-4cc4-9c3c-e5b9c405245e', NULL),
	(406, 5, '42319261560f46f68990aec7eeaae7e8', NULL),
	(407, 6, 'e0b6c794-339f-4ffd-9c50-3a7860e14a70', NULL),
	(408, 6, '7b106ba3-04dd-4577-8116-3c1bcbb9d275', NULL),
	(409, 5, '43a7f75bfef84fd9b4600cbec0e60677', NULL),
	(410, 6, 'a9542de4-746d-4556-a78c-dc027e4262b0', NULL),
	(411, 6, 'aaa93314-f512-4c23-b387-3801e6db044b', NULL),
	(412, 5, '43f679461c2a4bb792f9a6d2559353c1', NULL),
	(413, 6, '30297fd0-bed1-4f75-adc8-79119d7f2e2c', NULL),
	(414, 6, '442ab6ea-a4bc-4e9c-8e8e-94369fa154bd', NULL),
	(415, 5, '44bf1ab2c84145569af480fe86bb0ca9', NULL),
	(416, 6, 'ac3333eb-97ac-4e03-b125-afe68638bdf8', NULL),
	(417, 6, '5e6fc83c-376f-4396-96c8-7acfc329ffec', NULL),
	(418, 5, '455c732b9b2a40379bb2e4f22d5ba64c', NULL),
	(419, 6, 'f0773c37-55b7-4f4b-86b1-373c78e0b1f8', NULL),
	(420, 6, 'bebc96fe-87e3-4c2f-b4cc-463fa243095f', NULL),
	(421, 5, '45aaa8313ae54eb192c23782cfcc4d42', NULL),
	(422, 6, '33198941-3f8d-4b02-a380-6f6ec37fb174', NULL),
	(423, 6, '6227dcfa-abdc-4ed6-b9e8-47bf35732791', NULL),
	(424, 5, '49325a79fc314e6f81b2d4af1c64ea76', NULL),
	(425, 6, '5b6e5b30-ea67-4267-892a-29052a6f1eb7', NULL),
	(426, 6, '37cf71ba-a2a2-45ca-b967-7bbbd62b8575', NULL),
	(427, 5, '49d7a42b45bb463a9cf15e69dfb9b1d6', NULL),
	(428, 6, '45ac4dfc-e68e-489a-96f2-8c5e64e4c8da', NULL),
	(429, 6, 'ee1f2b97-6c81-4592-a125-fe98b5eab84c', NULL),
	(430, 5, '4ad3834ab84b4fa5af1e169f7350d161', NULL),
	(431, 6, '4425ce54-04f5-4712-9c71-ea97baf55727', NULL),
	(432, 6, 'd9d031b5-b440-4994-a88b-60697a7d0529', NULL),
	(433, 5, '4c7fd67674574075ae25389493525e1f', NULL),
	(434, 6, 'd55bfee2-c327-4323-9d3b-d65c78e98c95', NULL),
	(435, 6, '9e9bf15d-e897-4995-86e6-b4cd0e153051', NULL),
	(436, 5, '4d38f60c40334af7bb91a3b8a4067769', NULL),
	(437, 6, '87637614-62a3-4136-8159-fd84c2924830', NULL),
	(438, 6, '30a02b97-0049-4141-a4b2-33c282d2701c', NULL),
	(439, 5, '4ffe4b94a97c45aab01e66fba95a4b33', NULL),
	(440, 6, '04157cad-d196-498c-bddd-2369efb4cfcc', NULL),
	(441, 6, 'c3c36e7d-3e27-4e18-834c-2c3758df54db', NULL),
	(442, 5, '5032084c63e24cb78fbbff93700ca8fd', NULL),
	(443, 6, 'ea7a243a-7e3f-4fdc-b677-d847269a4b41', NULL),
	(444, 6, 'baa879db-3dc0-4eb6-be53-4b9497f97f88', NULL),
	(445, 5, '50dc10848ce54ef581b49fb45ecfdf28', NULL),
	(446, 6, '66985f0e-9089-45c4-bd6f-619f66de1682', NULL),
	(447, 6, 'd41b5508-1ece-4aff-8bbf-354a55255342', NULL),
	(448, 5, '50fec68fa8b44cb0aa978fd97b26561c', NULL),
	(449, 6, 'aa7f4cf6-2cb2-4db9-ae1b-5e97e94520b3', NULL),
	(450, 6, '40dcdc86-ffa3-4f71-a94f-1dd0cef4ca9f', NULL),
	(451, 5, '514e3273a5374d56ac5e4e992c4406d1', NULL),
	(452, 6, '95c72476-bf4e-4422-b5fc-787c46eca5a8', NULL),
	(453, 6, '54e125b9-be5a-452b-86da-fec041f2e89d', NULL),
	(454, 5, '526e8f2660fa4e6289bbeb1b7c27d337', NULL),
	(455, 6, '44fd0772-af9a-487e-999f-b0f5fa180075', NULL),
	(456, 6, '945ff6b0-176e-41d1-8523-0d14f4969fba', NULL),
	(457, 5, '52767d07c3c04d1eb9901e8bbb484eb7', NULL),
	(458, 6, 'c95f6df5-05cb-46df-b2c7-7a57bea5f520', NULL),
	(459, 6, '6f707ef2-225f-412c-badc-3c96d0d76438', NULL),
	(460, 5, '52d731b2d91c4d719c28648256ce2bec', NULL),
	(461, 6, 'e99338bd-3cd7-43ee-a470-2caf248e89c6', NULL),
	(462, 6, '762640eb-7e3a-4e31-82f7-670f9a727365', NULL),
	(463, 5, '5382169ff8af46ee9e89d2ac1cb7fad1', NULL),
	(464, 6, '9d8a29ca-79a0-4193-b1ea-28d8d23eb8b9', NULL),
	(465, 6, '1ad225a3-47d2-4794-8cbe-be7588c0915e', NULL),
	(466, 5, '54d8f6ae21884398877ea5e6888e8e63', NULL),
	(467, 6, 'd88359d9-81ab-4923-81e6-0de17034f6c3', NULL),
	(468, 6, '3bf1cb71-22e2-46ee-9297-88ec9ecd51d0', NULL),
	(469, 5, '54f888cb03fc4443a16d253cf8673fa6', NULL),
	(470, 6, '2c8899a0-c17d-4c91-98a1-6c3df6ab7c69', NULL),
	(471, 6, 'dffebbaf-792a-4bc9-a26a-ef3a8897c4b7', NULL),
	(472, 5, '5838fba2be554a5b9ecc70447c4e06be', NULL),
	(473, 6, '142cfc6d-e207-463f-a0e8-2c4247b03676', NULL),
	(474, 6, '004d4397-fbaf-4037-8a94-fb7367b18aeb', NULL),
	(475, 5, '587427d70c064bd0b55450749136004b', NULL),
	(476, 6, '1795c2db-c724-424f-96b1-023826813dc2', NULL),
	(477, 6, '3a2d7295-c03e-47d4-9917-30caf236e111', NULL),
	(478, 5, '5bb27681808140e9a85b610965c3ce40', NULL),
	(479, 6, '50a843f2-1c43-4dbc-ba03-4f5ede42c130', NULL),
	(480, 6, 'b5499568-7f9b-4596-b3e6-594588b8f426', NULL),
	(481, 5, '5c1588050d8c45d6a1a670f0774a8a29', NULL),
	(482, 6, 'e69f8908-b4e8-4e27-8c66-4646025ccf4b', NULL),
	(483, 6, '00cd6e86-d0bd-4af5-bcc4-dbbf50b082ce', NULL),
	(484, 5, '5c2eab1c372e45d3b4b96f9df171bbbf', NULL),
	(485, 6, 'a047273b-d251-4388-b5e6-29a86ad79b50', NULL),
	(486, 6, '0dc538a6-25ae-4d82-ae2a-5fd442f9f272', NULL),
	(487, 5, '5dafa8584c24437f9287427468f23b91', NULL),
	(488, 6, '178ac6c4-7419-4fe9-8b85-895460131c12', NULL),
	(489, 6, '8fb545ae-7105-430c-955a-d08e3a87d71a', NULL),
	(490, 5, '5e765a7690b945f9835722c49dec0381', NULL),
	(491, 6, '116b6174-aaeb-43df-b337-be853be25d77', NULL),
	(492, 6, 'c54b4a2e-e978-45be-92fc-d9c3d0b08639', NULL),
	(493, 5, '5ea7cc581418423ebb2aa0adf643c8c6', NULL),
	(494, 6, 'e268b1f2-7ff4-40c9-8a2f-94bd9aa01d20', NULL),
	(495, 6, 'bed14bdf-ce3a-4b3b-8116-8d8bd2c70ffe', NULL),
	(496, 5, '60a75f000899452989fcce35da7e7258', NULL),
	(497, 6, '13725719-6f06-4e32-8e12-4a38500785bd', NULL),
	(498, 6, 'b0a47321-ef8b-4dc0-86b1-fdff72b8ee94', NULL),
	(499, 5, '6193fef152554e6cbb27da6659f45a5b', NULL),
	(500, 6, '0aa039b9-7449-44f3-9728-8026d9a1c1cf', NULL),
	(501, 6, '6d0ce023-919a-4575-a3ac-07e670e0a613', NULL),
	(502, 5, '64e98924c1ab454cabac0df700c6a702', NULL),
	(503, 6, '4b180034-14e9-41b0-a6fe-a81bd76edda9', NULL),
	(504, 6, 'e7c70815-71c0-451b-a14d-ba735fd004a7', NULL),
	(505, 5, '656616a3edac42d3a94a8d50a527d069', NULL),
	(506, 6, '5c7b9144-bbc8-4444-ae75-3d88134b6497', NULL),
	(507, 6, 'c5b59799-58c8-4b47-95f8-c52e34565c07', NULL),
	(508, 5, '6797dbb8618442a1a37513e53a132639', NULL),
	(509, 6, '33e067bd-3962-4a8b-a1ca-6185f3023f15', NULL),
	(510, 6, '87d646f1-07b3-4a2e-8503-945682774e95', NULL),
	(511, 5, '6825d7cd9e08483ca95cd8940a61ee8b', NULL),
	(512, 6, '72e999ec-312b-43c0-a8a9-e3f88d8b6472', NULL),
	(513, 6, '2c4a7128-eba1-4b73-b7c6-71d8eb94d0d9', NULL),
	(514, 5, '6a1038cc6146441cad15c87fdd8c7a6c', NULL),
	(515, 6, '212c0052-ae25-4697-9277-8bbceaabf071', NULL),
	(516, 6, '4a6696ae-058b-4aa0-9815-5895fff5cfd7', NULL),
	(517, 5, '6c84aba618bd4b82a8e50b91de1b8c6e', NULL),
	(518, 6, '2e3a235f-2ce6-43bc-a3fb-ebe574dad26e', NULL),
	(519, 6, 'fc04080f-57c6-47b8-9422-ebb6ab94fc69', NULL),
	(520, 5, '6e24f669432a48eaa74e265cdc25fd3e', NULL),
	(521, 6, '51b8a892-cd60-4454-b5e1-c4fa99b8693a', NULL),
	(522, 6, '4e9be642-44f3-41b7-b4b6-00490295d8a6', NULL),
	(523, 5, '6ee6f47aa61c4a19aca123cc442ea15f', NULL),
	(524, 6, 'd7a53c96-a955-4c53-a9b2-7a6d2644ca39', NULL),
	(525, 6, 'c5c7deae-3e84-466c-b41e-d6643ec8977b', NULL),
	(526, 5, '6f549051967e442a912ca9e7cf903618', NULL),
	(527, 6, 'fcbc5a8e-cb7e-47d1-ab59-f3ec5b81b8bf', NULL),
	(528, 6, '56bbf837-907d-4025-a015-24b8315837af', NULL),
	(529, 5, '6f614044673243c6a4dd53d0201caf3b', NULL),
	(530, 6, '21bbe7ea-ce09-4245-a02f-0c145df10fd5', NULL),
	(531, 6, 'e63a406e-1ab9-48e8-9ae8-53217635e7ee', NULL),
	(532, 5, '7121533453e84ab8a5afc58adcf151cf', NULL),
	(533, 6, '08948402-ac8b-4a63-890d-804306d9b7b3', NULL),
	(534, 6, '7326e6e9-e4ee-4433-b120-f3ab2ea6cad5', NULL),
	(535, 5, '71652396194b4bb3b59c8190674c0120', NULL),
	(536, 6, '45927f5d-f545-4b5f-99ae-5ee37fed34be', NULL),
	(537, 6, '64499974-d6e3-4901-adff-ba06cdfe9e7e', NULL),
	(538, 5, '71d8b3085eb84fde95c58f62cd8602b0', NULL),
	(539, 6, 'e3ca7c56-2638-4201-9d9b-05db527efe1c', NULL),
	(540, 6, 'b6cd3bda-e633-446e-bf06-15493830550f', NULL),
	(541, 5, '738f836638344b8892f70e1851b6a9d2', NULL),
	(542, 6, '14713b55-0e26-4283-b770-8306b99a355e', NULL),
	(543, 6, 'c103d5e8-d89a-40cc-818d-5f1db97fa484', NULL),
	(544, 5, '74c1e7da0d88434ab952e8f759c0548a', NULL),
	(545, 6, 'b3f4240e-5b0c-46c3-b7df-ed197d0e3758', NULL),
	(546, 6, 'd8c7a97e-221b-4c8e-a9db-a2189250d3ba', NULL),
	(547, 5, '7626f780bedd49b7bf6966d6361370d4', NULL),
	(548, 6, 'fc796fe0-1c22-4912-aff9-b712a07707f6', NULL),
	(549, 6, '755fa3a1-5300-45c1-bbb1-f6478513cde5', NULL),
	(550, 5, '771399f2701c4b1d8b0e28fdcb346238', NULL),
	(551, 6, '28ff207a-36f9-4427-b8dd-3ffc5b542d94', NULL),
	(552, 6, '74723677-18e1-423b-86a2-221485fbaf19', NULL),
	(553, 5, '795192e776fe47668fda4941da8292e0', NULL),
	(554, 6, '5994fa70-9c6c-49fc-8495-1ddde10cf3b0', NULL),
	(555, 6, 'b1893838-317d-4e0d-a686-1a6fdb2d4e7c', NULL),
	(556, 5, '7a5482f85a394670a7f5f93337b91abe', NULL),
	(557, 6, 'c44f06c8-4f09-4123-b29e-f09f418e1d88', NULL),
	(558, 6, '351dabf7-40ca-44fc-890c-7ce18561bb60', NULL),
	(559, 5, '7c2b89ab2c764884815d328f96d99b8a', NULL),
	(560, 6, '7f5abf98-f2bc-456b-9082-3297a2fa2a45', NULL),
	(561, 6, '69f2b101-3bc2-43c0-a531-ed946c6ee139', NULL),
	(562, 5, '7c6e0d9565b24ff8a0fc190cffe92c11', NULL),
	(563, 6, 'daffa6d0-0c63-471d-9f18-7c58d978f8c8', NULL),
	(564, 6, 'e202890d-7c0d-41bd-9331-3b6c6adef767', NULL),
	(565, 5, '7cb0030d98c44375ade1da1d84448a89', NULL),
	(566, 6, '2f47b87d-72c9-401b-a44b-7330cf2537ca', NULL),
	(567, 6, 'e0970f95-5690-4fa9-9264-3cd7af87c032', NULL),
	(568, 5, '7ce69c88a0a64c0d879f1cd822bb4e83', NULL),
	(569, 6, 'a5c1c680-9f46-446e-9beb-0084d31e9b92', NULL),
	(570, 6, 'a2ac0b54-57a6-47fb-b795-5a74933c8d73', NULL),
	(571, 5, '7e677756fc8344a3bc3961c0dcda5ee5', NULL),
	(572, 6, 'dd0858ca-e156-432f-9448-79d24c6664f0', NULL),
	(573, 6, 'f722368a-5dbe-4c05-b7b5-018bc609b92b', NULL),
	(574, 5, '7ee6aca26d25482f8df482bf82716ae0', NULL),
	(575, 6, 'c3296a41-a52c-4d1b-9c86-be12787f97a3', NULL),
	(576, 6, 'e2493221-0d23-4252-bf7b-35ec7e398748', NULL),
	(577, 5, '7f45a95ca9944a73a1cbb1f30dd55e00', NULL),
	(578, 6, '0c640e7a-2067-46a7-99e8-bcb385c442ce', NULL),
	(579, 6, 'f2eb5eae-ef75-431e-9aa9-ee2f6052f34e', NULL),
	(580, 5, '805c4c2be1cf4b689479e7f111380d88', NULL),
	(581, 6, '9f12fdb2-f3e3-4ac0-91d2-e1e8f7ba3612', NULL),
	(582, 6, 'd11bb94b-6a20-4d63-9a1f-30567f5d5919', NULL),
	(583, 5, '86935d6310f1491bb1d49d5d3a836063', NULL),
	(584, 6, '51200c32-b203-4a47-822f-a7ec7eaf4fec', NULL),
	(585, 6, '2da7ed0b-2a3b-4b2f-97c8-e08ef97940d9', NULL),
	(586, 5, '8860b7d3f7b242ef881d76ce0b077067', NULL),
	(587, 6, 'a3347c87-314f-4057-9a35-792731d671c7', NULL),
	(588, 6, '19189a4d-abe0-46ec-b008-087858755b34', NULL),
	(589, 5, '8a175562ef744dbf82db0d9498bbcb1d', NULL),
	(590, 6, '4195f1f1-d748-4338-be82-122abdba3b88', NULL),
	(591, 6, '287ff150-0e67-41f0-9a4f-5b4d4b3e30c2', NULL),
	(592, 5, '8a4dfd16c3c24a2f900484f2d08913f5', NULL),
	(593, 6, '236c8fe1-3d09-43de-8977-69a639478866', NULL),
	(594, 6, '9e80ab87-d41f-4664-ad33-b51417e90708', NULL),
	(595, 5, '8a5996b85a1340fe8393e112ebc5253d', NULL),
	(596, 6, '0f135fb9-a5de-454f-9c4e-08705ac0b75f', NULL),
	(597, 6, '45b55075-ff6b-48f8-835b-1f94309cbec5', NULL),
	(598, 5, '8ad33e469b6d424f845cbf3b973b9b97', NULL),
	(599, 6, 'e61eb69c-ac2f-4f10-acf7-b2ab2d7c33e8', NULL),
	(600, 6, 'e247d358-c9ff-40ff-ab0f-0e8feaad3054', NULL),
	(601, 5, '8c38d2773a6e4232805bd9557f5714d2', NULL),
	(602, 6, '18924425-0e77-4cc3-8ff7-93d08e6abc02', NULL),
	(603, 6, '4692ea5f-f7a1-4686-be4b-ec5399d616b7', NULL),
	(604, 5, '906f64fdaacd47e9b408dd0de1e3451e', NULL),
	(605, 6, 'f31123cf-9fb1-4930-aa3a-33197c52d768', NULL),
	(606, 6, '1917e19f-a3da-4892-8bdb-64118d4c356c', NULL),
	(607, 5, '913994a9a8384ce1b73cef6f7db09bda', NULL),
	(608, 6, 'a0882404-efaa-4dd9-b934-92372ca5e876', NULL),
	(609, 6, '1657e99e-2765-4973-8635-1707903f2572', NULL),
	(610, 5, '9205de6c8e37413e9a10744b33568407', NULL),
	(611, 6, 'c409f846-9442-4e1c-82a6-e7c5f7093ea4', NULL),
	(612, 6, '4cebe0df-14cd-4f85-98fa-6e844947b8b1', NULL),
	(613, 5, '96b3a199d12a4c94885faf6fc45b7578', NULL),
	(614, 6, '552268a5-993c-462d-ade1-ef64563fb157', NULL),
	(615, 6, '5511433a-56cf-4533-bf40-4603fe2dcb6c', NULL),
	(616, 5, '96e9c533ca0b457881a96e78bf6ab7ab', NULL),
	(617, 6, 'da65ab4e-2eba-41a2-ad4d-0d474f479017', NULL),
	(618, 6, 'f6f655bd-50ef-4519-9159-b232271f5d5e', NULL),
	(619, 5, '9b268054e88a472e9340783e230dfad9', NULL),
	(620, 6, '2c82efff-b3ae-408d-9aa0-5689d67cc0b6', NULL),
	(621, 6, '1485689e-0d26-4a77-bca7-f5154c66bd7f', NULL),
	(622, 5, '9bcaebc9aa474ad7a7c134fcd7edc152', NULL),
	(623, 6, '92b7c275-ad9e-44c7-b52a-2c2740b6bb2b', NULL),
	(624, 6, '85fdd488-a908-48ce-b591-2f221718b68c', NULL),
	(625, 5, '9e80331694034f9f94502423227c07cd', NULL),
	(626, 6, '001d0182-2427-456e-9ec5-090e39ad2e7b', NULL),
	(627, 6, 'da811064-7e28-43d7-b4e6-b46f3893f6ee', NULL),
	(628, 5, '9ec9bf03d3f348dbbb3d86d0c1d41226', NULL),
	(629, 6, '215229d0-3f11-4d8e-a7ec-cce7f3b878e4', NULL),
	(630, 6, '2bb7e07f-0657-45a1-8e4d-97722d7f9209', NULL),
	(631, 5, 'a0e86ae35b9a4b4bb7e1b1a2f44de7b6', NULL),
	(632, 6, '54e338ea-fff3-4ee6-b2f1-3f81b4627b03', NULL),
	(633, 6, '63038e6a-d4e7-4fae-a342-b0cdd403519f', NULL),
	(634, 5, 'a104d4709b8643abb1368bc50fb6f65e', NULL),
	(635, 6, '187ec8b3-c487-4e86-adcc-8344fd8abd4b', NULL),
	(636, 6, 'a5c4d4f2-8bfc-4afe-bb37-bab9cdf5e49e', NULL),
	(637, 5, 'a13249b918e244fd9369a5878ac119e2', NULL),
	(638, 6, 'af64f5e5-c350-45a8-a917-122006cbabe6', NULL),
	(639, 6, 'cd583c7b-cd02-4f6c-ab9e-39b03c5780cd', NULL),
	(640, 5, 'a2098080c1594fe38a76dbc622350bea', NULL),
	(641, 6, '52747b35-a076-4dc1-8de0-b1c9f72c2e49', NULL),
	(642, 6, 'dc271573-affe-4cbd-adde-7822a93a2647', NULL),
	(643, 5, 'a2f59e0ed3dc4b54bed4eda6bcb91ee7', NULL),
	(644, 6, '4110db8e-aea6-423d-b2b7-f795d441e46b', NULL),
	(645, 6, 'fe8678af-b9e3-40c6-ba14-efacbc9147ca', NULL),
	(646, 5, 'a32f06ce2b404735b0689278149bb348', NULL),
	(647, 6, '6dc1b8bd-4f12-43da-bbe6-f55525b343fc', NULL),
	(648, 6, '1f371080-21cb-47fc-ba65-79c921889b3d', NULL),
	(649, 5, 'a69ed0e1aa5548b3965a632ecd576847', NULL),
	(650, 6, 'd4f94a74-7cee-45df-81ce-d1486650ea81', NULL),
	(651, 6, '2779e0cf-9ec2-4756-b8f5-4164c6835ba4', NULL),
	(652, 5, 'a796e0d4c4264f40996f50459147ab9e', NULL),
	(653, 6, '441e6b29-b443-4e97-bca7-72551771c413', NULL),
	(654, 6, '7bad30c0-328d-48f1-856f-3d2d832a17ff', NULL),
	(655, 5, 'a9af272b5cd2433190740709f91326aa', NULL),
	(656, 6, 'cbacbf1b-97ed-45e2-ab29-3f76d3962a1d', NULL),
	(657, 6, '1fba06a6-3cb0-455c-b8ad-76715ef4d38c', NULL),
	(658, 5, 'aae5de1c7b504679b4193ebd486c5ad6', NULL),
	(659, 6, '38e857e4-d79a-473f-a48d-b4cb46e47194', NULL),
	(660, 6, 'dbfafb2e-e5a8-4c7a-848c-61662da064c8', NULL),
	(661, 5, 'ab0325b3d7764f12bf79b8e6f06fdeaf', NULL),
	(662, 6, '0d3ae63c-61fa-4862-a3c8-0070776e0470', NULL),
	(663, 6, 'afb277ce-4807-4924-9175-f76ae61611a5', NULL),
	(664, 5, 'ab3b3e44101f43f789506304dab02f68', NULL),
	(665, 6, 'c8be65b4-5956-4548-95f9-6856ef45b991', NULL),
	(666, 6, '91df1be2-e630-4455-91fa-bd828f41bdaf', NULL),
	(667, 5, 'abc4e2aa58fa4e1395e0f1f0f973fc93', NULL),
	(668, 6, 'a29d7d51-34a7-457d-8d49-dce0c5caabec', NULL),
	(669, 6, '5a1bc98e-4bc4-496e-a395-3db7871dc157', NULL),
	(670, 5, 'abd4bf4fda9748179b1e570583fa0248', NULL),
	(671, 6, '08caa51c-99a4-4612-a2a6-3c06524be1a9', NULL),
	(672, 6, '1a8b14b5-d192-43d4-b0b5-e24f5887b486', NULL),
	(673, 5, 'acdb9d18aa6a4c849a181c952ecb545f', NULL),
	(674, 6, 'fd33d125-4138-4b24-a4b8-510b8e470d4d', NULL),
	(675, 6, 'f15a138b-0bfa-4d48-a553-c790f3f9fece', NULL),
	(676, 5, 'b1202c5a7d5b47ccbfa08c9984cad955', NULL),
	(677, 6, '60902e92-7e4c-4a85-a73e-48cab4ddb9bb', NULL),
	(678, 6, '24902df6-a90f-4a92-946f-26f945305d7d', NULL),
	(679, 5, 'b14a80c958c544a49c9e34e899b2854c', NULL),
	(680, 6, '258b041a-11d8-4c9e-8489-8381004ab4f1', NULL),
	(681, 6, 'cf27f54b-624d-493f-a42f-3baa4f8d9ed9', NULL),
	(682, 5, 'b25b96df11fd454ea9806ef26acd65ad', NULL),
	(683, 6, '55577623-6b5c-481f-b415-74a0682be2e8', NULL),
	(684, 6, '99e3d0b5-70d1-4eae-b546-628417981b56', NULL),
	(685, 5, 'b42177b4b08f48c78d4157074099a701', NULL),
	(686, 6, '94eab83d-467b-4d24-a994-83e406b86744', NULL),
	(687, 6, '5806b1de-9e77-4e7a-91ba-c69931404332', NULL),
	(688, 5, 'b79133bde79f4d98bd70bdd14fd0449e', NULL),
	(689, 6, '2979d57a-6547-4439-bc24-f3f3f2f77c9f', NULL),
	(690, 6, '19a3e8b6-c7a3-4766-8e7b-674ea4769d2d', NULL),
	(691, 5, 'b8ab520d16cc4384a8f39a79b075efe9', NULL),
	(692, 6, '390ca02e-8ba4-4a57-a899-f318a28a4a7e', NULL),
	(693, 6, 'bc274785-e692-4451-a6d9-aa88811a6bd6', NULL),
	(694, 5, 'bf3742cd34f14407a0b1398baf9def03', NULL),
	(695, 6, 'abb1b13d-b534-4fb3-a735-7aee66ad239d', NULL),
	(696, 6, '4fa300e8-9507-4e34-98fb-a7542d99b990', NULL),
	(697, 5, 'bf39d1617aad416a8bdf37c5cc1c276f', NULL),
	(698, 6, '46f5c012-8ca4-41ae-b5c7-15bc10cb6cc4', NULL),
	(699, 6, 'fd3cf99f-1b35-4b97-b6a2-6a4336b84619', NULL),
	(700, 5, 'bf526c2e1e6e4ec993fe9d728eda0f98', NULL),
	(701, 6, '3ac2be4e-d251-4f17-8dc5-539a40927c02', NULL),
	(702, 6, 'f4d91f03-1c61-4046-ba96-2fcd49ff0936', NULL),
	(703, 5, 'bf75772b9b264ad89069a4d1e75d3485', NULL),
	(704, 6, '8bd56d0a-622a-4d4f-8bdc-1f44f47c1ee9', NULL),
	(705, 6, '26b733b1-e1a4-4a72-ba3d-5b5854f39f98', NULL),
	(706, 5, 'c1662752302b4ae1a910eb1ac54b2896', NULL),
	(707, 6, '443f18d6-2b64-41d2-b4ba-cf2c3ce630bd', NULL),
	(708, 6, 'a30f8645-a3a5-4e6c-8388-c56ceec12c27', NULL),
	(709, 5, 'c2fbfff3168048f4aa3e3af5b15000e8', NULL),
	(710, 6, '55a010dd-691f-42a5-aaa6-d2c57e351834', NULL),
	(711, 6, '8e060b44-143f-44bb-8ca9-45fdcbe55ca2', NULL),
	(712, 5, 'c3c0eb7a1c4a43a086f965f1ffc0ca80', NULL),
	(713, 6, '2ec4a485-ac57-434f-8fa9-36bff1c0d5b7', NULL),
	(714, 6, '5b692d78-60b1-4625-89f5-cfee0cfdbe3a', NULL),
	(715, 5, 'c4a12dd0771e4118a2f71c7b9772ab7c', NULL),
	(716, 6, 'd4895a11-2331-4ed8-afdd-28e648cf8e1b', NULL),
	(717, 6, '6e3ee97a-ad59-4251-b3b6-adae68c07664', NULL),
	(718, 5, 'c4c1929bd86c4ef1b96fb2385b26bf36', NULL),
	(719, 6, 'd1e8aa33-b1d6-4e07-a975-a1dbcdda5f85', NULL),
	(720, 6, '1fafc05e-50ed-49bd-af3d-483b112d2e2b', NULL),
	(721, 5, 'c54996f985fe4314a84255aeb6dc13a3', NULL),
	(722, 6, '2e69b261-8d5b-4efb-abec-78983c41cec8', NULL),
	(723, 6, '34114421-a867-4ae3-95af-9696b9f93e84', NULL),
	(724, 5, 'c7a14e835ca54c2b9dd95c858236ca2d', NULL),
	(725, 6, '69849233-508c-47d6-aea1-89b5868671bd', NULL),
	(726, 6, '25ccf77c-d79e-4bd3-8578-020ee9010ef8', NULL),
	(727, 5, 'c87d0460b38b4861a429c83c605ba24e', NULL),
	(728, 6, 'ab4e1a6b-9807-4b4d-9cbe-966e540c5118', NULL),
	(729, 6, '348eb37b-8d13-4bfa-b329-d6f3aa9075b1', NULL),
	(730, 5, 'c8a16cbcd76e47689f5a6a04b6539cb3', NULL),
	(731, 6, '4260cf5d-a31b-40c2-b7e1-abcab77f0fd8', NULL),
	(732, 6, '702b7b18-ee1b-4869-8706-315bfaab4d47', NULL),
	(733, 5, 'cb1049ce105a42c5964347c43dc5ad11', NULL),
	(734, 6, 'a2ccf63f-8c2d-4df1-9d7a-5bb873b131b4', NULL),
	(735, 6, 'af36cbc1-faf7-40f4-be75-9cae774dee83', NULL),
	(736, 5, 'cead96e4b6d141d6888955ea3955dac7', NULL),
	(737, 6, '4a8ae195-8ba1-4221-aa6f-67b5ceca678c', NULL),
	(738, 6, '661d943a-86d5-438a-afd1-db16085b02e4', NULL),
	(739, 5, 'cec0c2bed9de4d1db483114aa8c3f5a4', NULL),
	(740, 6, '69be926b-c5e0-4a17-9d97-5c421399f747', NULL),
	(741, 6, '2ca7f067-3ffd-42e2-bb12-d2340c56201b', NULL),
	(742, 5, 'cf56cb478d8346ed8844c60ab56dca7c', NULL),
	(743, 6, '35f5d11c-f130-410e-b5e5-cdd9918307e2', NULL),
	(744, 6, 'd87b11f1-42a8-45ec-8461-24a9af71099e', NULL),
	(745, 5, 'cf89ddf62500471e8810300f8ee31d61', NULL),
	(746, 6, '2e3a40e2-78ba-4ac6-b9e5-2c9ff9a8918a', NULL),
	(747, 6, 'd160e4a1-1857-46a6-947c-0895c156a589', NULL),
	(748, 5, 'd0f10d53442d4eb6a75f15372d9a7ad4', NULL),
	(749, 6, '7b0d65c1-5c29-4a2a-8802-c8cb532d8d91', NULL),
	(750, 6, 'ba9cedcd-f9e7-476d-a10b-61732681ae6b', NULL),
	(751, 5, 'd841a77a974541c4a09edd8b17c7dd22', NULL),
	(752, 6, 'b84d372c-eedd-411f-b769-b259fc5d39c9', NULL),
	(753, 6, '27e0a322-8759-40fe-aa5f-31778d8feaab', NULL),
	(754, 5, 'd9383d82729b4e80a3242d9008bf961c', NULL),
	(755, 6, 'd19dc602-872c-4c03-b47b-51a124b3e871', NULL),
	(756, 6, '3b931bfe-cbcc-4c2d-800b-75b2979eb457', NULL),
	(757, 5, 'dac7ab19082947019e1c6a5d05517e12', NULL),
	(758, 6, '6129756b-b8a3-41b3-b269-5d91f7e2f4e2', NULL),
	(759, 6, '62f21ef4-008a-45d5-bd59-73e284615b16', NULL),
	(760, 5, 'dbc3a373340a4c6887dad76c684c81f7', NULL),
	(761, 6, '8ccbbf55-d4df-4d6b-917d-b03845ef2f0f', NULL),
	(762, 6, '3a666ccf-0445-4d61-bbc6-ae6bba469d4b', NULL),
	(763, 5, 'dc62fe77f997413196dcec55cc94bea8', NULL),
	(764, 6, '49843fe6-29c9-4440-aef0-a37117b6001f', NULL),
	(765, 6, 'c27cb761-805a-4062-b188-ab0645c20fc0', NULL),
	(766, 5, 'dc874c2ad53847f4bbea3b76d244fdb2', NULL),
	(767, 6, 'df3fc81b-0824-4704-99da-b52d9aa978f7', NULL),
	(768, 6, '6552b701-89a8-40af-a7f7-af2f01d5fe20', NULL),
	(769, 5, 'dccbf8d4ddc04cbd90e0bbabab35576c', NULL),
	(770, 6, 'f7463524-89f1-4356-a2d7-33d3a7d89852', NULL),
	(771, 6, 'c1b66f82-5229-4566-ab1e-707728f04aac', NULL),
	(772, 5, 'dd92204c8fb54d55a3f829cdc3edcc3a', NULL),
	(773, 6, '69041033-ce39-4a80-bddd-eac835f2058d', NULL),
	(774, 6, '141747cc-c8ce-41b4-8f48-69cf347a3bba', NULL),
	(775, 5, 'de11f963a0e844a8a91a1bc0e6030e3a', NULL),
	(776, 6, '65316c54-87d7-4176-a705-84dac3265caf', NULL),
	(777, 6, 'a740616c-e664-4908-8d97-805b900dd8e1', NULL),
	(778, 5, 'e078cf2bc53746a0857e813e62535a40', NULL),
	(779, 6, '1f690ec9-6dc7-4eeb-bf87-5fb113314f35', NULL),
	(780, 6, 'dead1345-3944-4a4d-acd7-711c1f970083', NULL),
	(781, 5, 'e344bc65087c481b8d3a5c049395b46f', NULL),
	(782, 6, 'd00025f0-d15e-484e-8faf-f95132e907ea', NULL),
	(783, 6, '79034ea1-1e5e-412e-969d-d0b1cff98752', NULL),
	(784, 5, 'e53f4c671e2b4bc295fd59c254c2ac93', NULL),
	(785, 6, '843ef752-a74d-4da5-9883-d1849d4b45b0', NULL),
	(786, 6, '8cf88ede-95eb-4eb4-9f29-e36b7315b123', NULL),
	(787, 5, 'e57a59ac9ad6496987a72a91d8a928cb', NULL),
	(788, 6, '59bf32f1-666b-466b-b745-e64cf9461a09', NULL),
	(789, 6, 'fe355f8e-ac56-421f-9b71-334532290c66', NULL),
	(790, 5, 'e9d6632182d2492bb722322dd113f121', NULL),
	(791, 6, '1ca291f1-b753-4cbc-bec3-d110bfcf9891', NULL),
	(792, 6, 'ab4e0aa7-e435-4ab1-9a1f-e54eeda3056a', NULL),
	(793, 5, 'ea9b6f622c2c4b18a3563c60c2d9675d', NULL),
	(794, 6, '5a87bce3-7d78-4ac8-90e0-4442414219d5', NULL),
	(795, 6, '3a9fad63-f68c-450a-b331-ecb9accccf36', NULL),
	(796, 5, 'eb93622ebd7a4c59af0b0bdd27232920', NULL),
	(797, 6, '93209e13-b910-454e-b62d-0356e34a4a89', NULL),
	(798, 6, 'a20840f9-62d3-4006-9c3f-fd408496b1c3', NULL),
	(799, 5, 'ef8c8c65824641f98f12f0fb4cd4201a', NULL),
	(800, 6, '178a90c8-53d7-474a-bf90-adea91fd9321', NULL),
	(801, 6, 'a962350d-c9bd-45c8-85e2-ac909bdfa5e8', NULL),
	(802, 5, 'f00957c1310c4faa91557beb150a742c', NULL),
	(803, 6, 'c89116f3-e29b-4063-bd34-10a44c2ea6d6', NULL),
	(804, 6, '1f7c6c27-3fb1-4945-a621-7d361522cd27', NULL),
	(805, 5, 'f01616f0d38a4cb9ad654f39903bffe0', NULL),
	(806, 6, '001cd56f-2c1f-4f69-8e0a-7c276b1ebdf0', NULL),
	(807, 6, 'd61cd692-8ccd-4c8b-87a0-2633eebd8d5f', NULL),
	(808, 5, 'f0342a6936dd4b3bb513219e41fd8bf6', NULL),
	(809, 6, 'd0153b8d-22bd-4ed1-9773-f0db546bb30f', NULL),
	(810, 6, '36c159d2-f383-4c87-a287-c8b9c2a355b5', NULL),
	(811, 5, 'f0d7d47ac3b34de2acfc46a8bb6e6942', NULL),
	(812, 6, '546d76cf-0149-431d-8c72-ed645c744763', NULL),
	(813, 6, '047680c5-0def-4843-b61a-e8c80eab1656', NULL),
	(814, 5, 'f10f3bd50f0048b393811b7744758d91', NULL),
	(815, 6, '825547e1-c206-412c-998f-b04d73806d6c', NULL),
	(816, 6, 'a1fc2d2e-27c3-4864-8c03-31faa9d3ce71', NULL),
	(817, 5, 'f12b33f569644b0b8d9e09c0db85c757', NULL),
	(818, 6, '3c6d86ca-1965-4d17-acff-5d0b6c9ea1f6', NULL),
	(819, 6, '5d549c9e-60f4-449f-889d-2e47fbf8159d', NULL),
	(820, 5, 'f2e2c1b735d04703b69abf00244d1805', NULL),
	(821, 6, '8e10f6c7-ef15-4972-9c9e-0800d226f36a', NULL),
	(822, 6, '572170b3-9fda-4815-a01d-1d5806179b03', NULL),
	(823, 5, 'f56df245e6614556ac39e51860816812', NULL),
	(824, 6, 'e4b44010-d576-4640-a219-b1daa556eb0a', NULL),
	(825, 6, '4827f839-5304-4138-a5cf-19723913e80f', NULL),
	(826, 5, 'f922034f47cd4e3e866e06ae1f1e6154', NULL),
	(827, 6, 'c35a712d-e881-463f-9076-173b10aa56b0', NULL),
	(828, 6, 'ae868f4f-8ac5-4647-a883-d2a6074d502d', NULL),
	(829, 5, 'f9d5cd7212c94e0c9069efcfac628ff8', NULL),
	(830, 6, '4ef9785c-fbaf-4e75-9aae-a7a1fd3e33ca', NULL),
	(831, 6, '4d7a796b-808d-4916-9310-dc1c6f2e4b06', NULL),
	(832, 5, 'faa254d3dc504a8a9f37ef23e0ad8d3b', NULL),
	(833, 6, 'f72ae763-7d42-4ce5-88ba-2782f1bb8b21', NULL),
	(834, 6, 'faed2ee3-2638-41f3-96bd-6a4551d832a8', NULL),
	(835, 5, 'fc654da72a424adca7ea2a31bab0d2af', NULL),
	(836, 6, '13f8b379-b8c1-4ab7-8ad2-25b037e6c53a', NULL),
	(837, 6, '46ddcfd3-e7f0-4400-815e-0e3e8a75dfd3', NULL),
	(838, 5, 'fcb1852582634844b11d58094af0f7d8', NULL),
	(839, 6, 'c84ad855-1a24-4baf-814f-58436a790ecb', NULL),
	(840, 6, 'ec479fc6-54c8-41e9-a43d-dd31bc67396c', NULL),
	(841, 5, 'treatments', NULL),
	(842, 5, 'Columns', NULL),
	(843, 5, 'patient_id', NULL),
	(844, 5, 'visit_id', NULL),
	(845, 5, 'treatment', NULL),
	(846, 5, 'Rows', NULL),
	(847, 5, '1+6', NULL),
	(848, 6, '557254c7-ca98-42ab-990c-60a9c7fa613d', NULL),
	(849, 6, '9107f735-84d3-44b8-9480-a4fded3c8cbf', NULL),
	(850, 6, '2ee7e8cf-a87a-4d0a-8918-b5842bbaf31f', NULL),
	(851, 5, '1+7', NULL),
	(852, 6, '9c06a9c5-83e0-46aa-98e6-c71ee82e6214', NULL),
	(853, 6, '87a8a5aa-ff93-4d66-be07-347ea01590f0', NULL),
	(854, 6, 'db8940e0-4f3a-4c76-bba3-0470fdbcfc24', NULL),
	(855, 5, '1+8', NULL),
	(856, 6, 'e06b4dc2-7f07-4b42-9740-733d38aa3081', NULL),
	(857, 6, 'd7cf21c4-8ef5-4e98-8b66-935728212b2d', NULL),
	(858, 6, '685bd63d-6a77-414c-a1bc-e03414e59c71', NULL),
	(859, 5, '2+10', NULL),
	(860, 6, '1ab35dbf-d04c-4aa6-ba1e-4330fa8b99b9', NULL),
	(861, 6, 'e17d4ffc-f779-403f-9156-69988b85a77b', NULL),
	(862, 6, '957f6361-5e3d-4876-aa15-6b24f4841b10', NULL),
	(863, 5, '2+11', NULL),
	(864, 6, '717d0be0-48f4-481d-9057-7e329dd2513e', NULL),
	(865, 6, '27d4792a-f0f8-4e1e-94a9-055ab77ff52c', NULL),
	(866, 6, '8f25e8a4-ec5f-4792-8985-dcd0082341e7', NULL),
	(867, 5, '2+12', NULL),
	(868, 6, '78bb1c7b-a735-4cdd-a903-bb89487a2522', NULL),
	(869, 6, 'ff372571-7a65-4d9f-962a-b1757a135ab6', NULL),
	(870, 6, '055f143a-fea4-4087-862a-e05c608415a7', NULL),
	(871, 5, 'users', NULL),
	(872, 5, 'Columns', NULL),
	(873, 5, 'user_id', NULL),
	(874, 5, 'username', NULL),
	(875, 5, 'password', NULL),
	(876, 5, 'Rows', NULL),
	(877, 5, '1', NULL),
	(878, 6, 'fe974139-24f2-4ee5-8ae8-e6d87245b0db', NULL),
	(879, 6, 'd52dcda4-1993-4238-989f-51efeb383419', NULL),
	(880, 6, 'e2a34843-55f0-49d1-b410-9570e86ebeba', NULL),
	(881, 5, '2', NULL),
	(882, 6, 'a42798b3-b651-4eb0-8079-e97631a87d43', NULL),
	(883, 6, '6399162e-6c15-4a4d-80cb-67a9a5bb223d', NULL),
	(884, 6, 'f015a41e-b111-4504-8d46-52b23d0b1d9d', NULL),
	(885, 5, '3', NULL),
	(886, 6, 'a8bbf515-d128-4f62-bd23-1040c2247f09', NULL),
	(887, 6, '5422d473-8450-4dab-aeb6-e044a8c29cd5', NULL),
	(888, 6, '2c081eb5-583f-49d6-b14d-1600acd558ad', NULL),
	(889, 5, '4', NULL),
	(890, 6, 'af1fcc3d-fa32-4cf7-847c-79ebc148500b', NULL),
	(891, 6, '15b9ede4-2940-4e58-ba4d-09670c96c0b2', NULL),
	(892, 6, '8703d4e0-8fbc-4e1d-920f-a534b167a1e7', NULL),
	(893, 5, '5', NULL),
	(894, 6, 'd1b7321c-8d07-40b0-9235-5bab08f919dd', NULL),
	(895, 6, '3dd6fb8d-4b75-4994-a06f-a78c99e84eb8', NULL),
	(896, 6, 'c631adbd-e7fa-4378-b3d1-72b71ebbbc9a', NULL),
	(897, 5, '6', NULL),
	(898, 6, 'f879f549-06f3-442a-b6d5-356da9ad3ee0', NULL),
	(899, 6, 'bd1adab6-9982-4534-bae0-234ba3075b3d', NULL),
	(900, 6, '7dc31d66-382e-4e4d-8f1d-71394cb9bd0a', NULL),
	(901, 5, 'visit_notes', NULL),
	(902, 5, 'Columns', NULL),
	(903, 5, 'visit_note_id', NULL),
	(904, 5, 'visit_id', NULL),
	(905, 5, 'note', NULL),
	(906, 5, 'Rows', NULL),
	(907, 5, '3', NULL),
	(908, 6, 'cc5b9383-b943-4fae-a12e-da82c396f79a', NULL),
	(909, 6, '8cff117d-cb3a-4d47-b9dd-322a09297e66', NULL),
	(910, 6, '6d6e559b-1055-41bc-b337-b231271ef02a', NULL),
	(911, 5, '5', NULL),
	(912, 6, '0414c264-af2d-4740-9ab6-98167d82df49', NULL),
	(913, 6, '28c3f9de-eb46-4483-8c22-ddbc2324ba71', NULL),
	(914, 6, '75abeac2-c2cf-4f52-b88e-b971cb35af71', NULL),
	(915, 5, '6', NULL),
	(916, 6, 'd142c85c-54c1-4b31-add5-7808f5ca95ca', NULL),
	(917, 6, 'af3759a9-df0f-4218-8d71-187c250dc785', NULL),
	(918, 6, '224f1c30-b306-4ef2-b7b8-d29610030181', NULL),
	(919, 5, '7', NULL),
	(920, 6, 'ff968a2d-8cdf-498e-acb0-b11c9bef31ba', NULL),
	(921, 6, '0bb538e2-5a25-4ab7-a454-23e7cd6f4116', NULL),
	(922, 6, '5a1fccde-df31-452c-8283-6719bc85a344', NULL),
	(923, 5, '9', NULL),
	(924, 6, '0f43813b-9c7c-41e3-a662-b55a743820d1', NULL),
	(925, 6, 'aef0be24-b739-4939-bae9-b9cec6608779', NULL),
	(926, 6, '27e5b911-8a35-4629-9bbc-94bfc5e56e3e', NULL),
	(927, 5, '12', NULL),
	(928, 6, '32e7c0e4-6f25-4040-b661-e9e48eb3d9c5', NULL),
	(929, 6, '909868f4-10c7-4634-917a-e9633ac47cb0', NULL),
	(930, 6, '2916cb35-49c6-43e5-8800-396e1912cf9c', NULL),
	(931, 5, '13', NULL),
	(932, 6, '5da861f5-d254-4fcc-a11a-c3c0f99fc7a4', NULL),
	(933, 6, 'f1dea1cb-7ed6-4596-9e45-2af7415158bd', NULL),
	(934, 6, '5a22152e-d0a2-4d5d-85f4-36e22a57b0b7', NULL),
	(935, 5, 'visits', NULL),
	(936, 5, 'Columns', NULL),
	(937, 5, 'visit_id', NULL),
	(938, 5, 'patient_id', NULL),
	(939, 5, 'admission_date', NULL),
	(940, 5, 'discharge_date', NULL),
	(941, 5, 'reason', NULL),
	(942, 5, 'result', NULL),
	(943, 5, 'notes', NULL),
	(944, 5, 'Rows', NULL),
	(945, 5, '6', NULL),
	(946, 6, 'fc5fb841-24f5-47ab-94dd-dd59d6211b9b', NULL),
	(947, 6, '4c596698-6a9a-4874-acea-71d0d9b07cd8', NULL),
	(948, 6, '16b62698-c0cd-42af-b0bb-e833f02c93f2', NULL),
	(949, 6, 'da0ed622-03da-4a67-9937-2c808d26b315', NULL),
	(950, 6, '3c0b20be-88b4-4064-b651-c7767255513c', NULL),
	(951, 6, 'c0ac3858-1dfa-49af-8d95-cee6930f1462', NULL),
	(952, 6, '4529aea9-6b78-4b03-bce0-bdde458cdd68', NULL),
	(953, 5, '7', NULL),
	(954, 6, '57b53199-849a-4974-821b-782dec1ca905', NULL),
	(955, 6, 'e3084a1b-0e90-4186-81bf-2c8c021fbb5b', NULL),
	(956, 6, '95916b23-0340-40b3-ad67-a3afa9771060', NULL),
	(957, 6, '3102dc33-0df9-4298-966f-3dc7924db5ad', NULL),
	(958, 6, '6289fc23-4ef7-4d61-b7fc-726d0b38988e', NULL),
	(959, 6, '62625d70-67c3-4be7-a56e-5a16502f1995', NULL),
	(960, 6, '02e87229-1a00-4f34-b90a-d0fc0bd20786', NULL),
	(961, 5, '8', NULL),
	(962, 6, '41371809-d4e1-4d08-afc2-f5a2d8ecb18b', NULL),
	(963, 6, '061a63e7-684e-4371-bf12-e3174b2939f3', NULL),
	(964, 6, '863bbfbe-ee74-4af5-8418-6999ae1d7f32', NULL),
	(965, 6, 'c8980582-1729-47ad-b329-4f3826615d95', NULL),
	(966, 6, '984faba9-e12b-434a-854d-0cdb325daa73', NULL),
	(967, 6, 'fc2f0995-22b0-46ac-baac-00a25d1ade44', NULL),
	(968, 6, '0426ab36-7df3-4e94-b553-c7af30c78012', NULL),
	(969, 5, '9', NULL),
	(970, 6, 'f847a667-662b-492d-8073-8929cc639776', NULL),
	(971, 6, '6be7346a-59f0-452d-9f88-81be4403a89a', NULL),
	(972, 6, '0de48ca4-1c59-4f89-805e-ec46b1fbb359', NULL),
	(973, 6, 'd5dca28c-cba6-4bf8-a5eb-fe61c49a7c9b', NULL),
	(974, 6, '612dff21-f149-4a9f-8cac-74745895f0bd', NULL),
	(975, 6, '15f7a099-fe3f-45d1-b36f-a867f9d7123a', NULL),
	(976, 6, '164468ff-81f0-479f-9396-d292f54ded05', NULL),
	(977, 5, '10', NULL),
	(978, 6, '02e172fc-403e-4fd3-832b-d837797e1ad0', NULL),
	(979, 6, 'eb3970de-3ca7-46a4-be2a-da6f156079f9', NULL),
	(980, 6, '940153ad-7431-4565-bfd7-0357ed41b9e5', NULL),
	(981, 6, 'e7a7806e-32ee-4d17-a27e-f0b599507d40', NULL),
	(982, 6, '9bea27bc-37c6-40f8-9247-f4884ad980cc', NULL),
	(983, 6, 'a7487f00-d7ca-4922-a3df-95aba72976b4', NULL),
	(984, 6, '1b18f6e9-3090-4fcd-bd82-bee4c46b2a21', NULL),
	(985, 5, '11', NULL),
	(986, 6, '1b0ccc27-a9fd-4dc0-a750-433bf08b5843', NULL),
	(987, 6, 'aed18492-4777-4119-b971-812e20097f08', NULL),
	(988, 6, 'df1689dd-155b-4e13-a207-503f6e75351a', NULL),
	(989, 6, '88d36931-cc91-4ecf-8808-273484445613', NULL),
	(990, 6, '8a1812b6-d385-4307-bcb6-ca66124a98f5', NULL),
	(991, 6, 'a2971374-21a7-4ee7-b3ff-985fbb7bbd9e', NULL),
	(992, 6, '6d42cd4e-334c-4243-9d7e-7adfa96b551a', NULL),
	(993, 5, '12', NULL),
	(994, 6, '55eccfb3-31ed-478b-a471-50f24a30c7e3', NULL),
	(995, 6, '0ffe58b6-470c-4d0c-9c67-98a9d7f8d9ee', NULL),
	(996, 6, 'e352405e-ccdf-46a2-b8fc-cacd7422d94d', NULL),
	(997, 6, 'a59949e1-ee2a-4e6e-96ac-a826cc518c24', NULL),
	(998, 6, '866d90c8-1426-40ae-b8ba-888ba4165cd6', NULL),
	(999, 6, '21187c4f-14cf-443f-aa5e-97ddfb9b032c', NULL),
	(1000, 6, '8b36bedf-e40d-4268-b51c-1df1aa5b7039', NULL),
	(1001, 5, 'vitals', NULL),
	(1002, 5, 'Columns', NULL),
	(1003, 5, 'visit_id', NULL),
	(1004, 5, 'height', NULL),
	(1005, 5, 'weight', NULL),
	(1006, 5, 'temperature', NULL),
	(1007, 5, 'pulse', NULL),
	(1008, 5, 'blood_pressure', NULL),
	(1009, 5, 'Rows', NULL),
	(1010, 5, '6', NULL),
	(1011, 6, 'e5c4dd2e-0124-4479-87ee-e13af77fa4a3', NULL),
	(1012, 6, 'b82c9628-4c9c-4390-8599-e093cd3a1f04', NULL),
	(1013, 6, 'ba59730c-8a5e-492e-9934-0c9cfd691680', NULL),
	(1014, 6, '4a466bc2-c94f-427e-b7de-f103fc801b2f', NULL),
	(1015, 6, 'afc49427-ee08-4b46-b70a-d857a34934e7', NULL),
	(1016, 6, '38deb8cd-369a-4f8b-b21b-503a1599f1c9', NULL),
	(1017, 5, '7', NULL),
	(1018, 6, 'd3395571-2f8c-4064-a21e-7e29736e4f9b', NULL),
	(1019, 6, '68bf26f0-485f-4530-bb2a-f08889a9ca00', NULL),
	(1020, 6, 'c9ae72ff-96b7-4bf8-b441-7771130530fb', NULL),
	(1021, 6, '41f6a50f-75b0-4c6e-84cb-8bfeee270aa4', NULL),
	(1022, 6, '749b356f-68f5-4660-a9ae-175a33891750', NULL),
	(1023, 6, '62fc6c03-9360-4510-8364-4b74f0d179a7', NULL),
	(1024, 5, '8', NULL),
	(1025, 6, '4eb516b2-84ea-4fd8-857e-2ddb4487bb8f', NULL),
	(1026, 6, 'd384710d-e320-4c39-9626-aa8b4e732387', NULL),
	(1027, 6, 'cb841ebe-c149-47f9-baa4-cf87baf10d85', NULL),
	(1028, 6, '23dbcb86-3686-4ee7-b360-7cb35c598d00', NULL),
	(1029, 6, '109e8c61-d348-495b-9eea-1bcf0a94b957', NULL),
	(1030, 6, '99208462-9744-4d57-a62e-bcb7a5a0cbfc', NULL),
	(1031, 5, '9', NULL),
	(1032, 6, '4f86c23f-548b-4a3d-bb5d-ba60da25eea1', NULL),
	(1033, 6, '0d2a64a1-63b7-4822-a908-d3ff3a87ab69', NULL),
	(1034, 6, '879c46d3-8691-4d49-bcf0-e7f20f72b57b', NULL),
	(1035, 6, '69de0e1c-7fef-497b-889c-aa2f871717c5', NULL),
	(1036, 6, '0d4145c7-9926-49e7-8128-be38c153886d', NULL),
	(1037, 6, 'ee2f583e-c18c-4d24-be00-6409792dc890', NULL),
	(1038, 5, '10', NULL),
	(1039, 6, '1898d671-f304-42d2-9182-161082af76fc', NULL),
	(1040, 6, 'eff5ef6a-c755-41a2-9dee-cb5957ba5998', NULL),
	(1041, 6, 'da350970-f6e6-47c2-a39f-5c5320869c04', NULL),
	(1042, 6, '01442813-7ef7-4b5b-a3c5-13423a778cfd', NULL),
	(1043, 6, 'fe57eb5d-aa9b-4398-bfbc-cd090bae5145', NULL),
	(1044, 6, '8307228d-2c3a-43f3-9305-8ad0a0200b3f', NULL),
	(1045, 5, '11', NULL),
	(1046, 6, '7f9afa34-9fe1-4493-b231-b95018c03463', NULL),
	(1047, 6, '6f86f75a-9a2a-461c-b6d8-9ed9df848043', NULL),
	(1048, 6, 'c5480a0f-1d87-4c18-8c21-b16781596e73', NULL),
	(1049, 6, '839179df-46de-4671-af7e-307fa23a80d8', NULL),
	(1050, 6, '0a425e19-02b5-43d8-943b-5f262be449f0', NULL),
	(1051, 6, '0cc9edd9-372c-4346-8285-5b53ed469121', NULL),
	(1052, 5, '12', NULL),
	(1053, 6, '6d7087d9-2b2b-414b-bafe-cb55152b47f1', NULL),
	(1054, 6, 'ea8d7129-fba5-49ec-8b72-adcec2dc173f', NULL),
	(1055, 6, '95b37807-c6d6-4214-aba6-e1a7c2d2307d', NULL),
	(1056, 6, 'e7d01787-148f-4f21-9787-d90e01d4e2d9', NULL),
	(1057, 6, '1897ca21-377a-446c-8876-023acd9691bc', NULL),
	(1058, 6, '0f966de1-4332-445d-9e73-c05d651be6c4', NULL),
	(1060, 3, 'RBAC admin', NULL),
	(1061, 5, 'RBAC', NULL),
	(1062, 3, 'DAC admin', NULL),
	(1063, 5, 'DAC', NULL),
	(1064, 3, 'MLS admin', NULL),
	(1065, 5, 'MLS', NULL),
	(1066, 3, 'pm_health admin', NULL),
	(11004, 4, 'bob', NULL),
	(11005, 4, 'alice', NULL),
	(11006, 4, 'emily', NULL),
	(11007, 4, 'lucy', NULL),
	(11008, 4, 'chris', NULL),
	(11009, 4, 'betty', NULL),
	(11010, 2, 'RBAC', NULL),
	(11011, 3, 'Doctor', NULL),
	(11012, 3, 'Patient', NULL),
	(11013, 3, 'Nurse', NULL),
	(11014, 3, 'Clerk', NULL),
	(11015, 3, 'pm_health users', NULL),
	(11016, 5, 'links RBAC', NULL),
	(11017, 5, 'diagnoses', NULL),
	(11018, 5, 'patient info', NULL),
	(11019, 5, 'sensitive', NULL),
	(11020, 5, 'non sensitive', NULL),
	(11021, 5, 'treatments', NULL),
	(11022, 5, 'visit notes', NULL),
	(11023, 5, 'visits', NULL),
	(11024, 5, 'vitals', NULL),
	(11025, 5, '1234', NULL),
	(11026, 5, '12345', NULL),
	(11027, 5, '123455', NULL),
	(11028, 5, '1234555', NULL),
	(11029, 5, 'prescriptions', NULL),
	(11030, 5, 'actions RBAC', NULL),
	(11031, 5, 'homes RBAC', NULL),
	(11032, 5, 'patients link', NULL),
	(11033, 5, 'medicines link', NULL),
	(11034, 5, 'my record link', NULL),
	(11035, 5, 'messages link', NULL),
	(11036, 5, 'doctor home', NULL),
	(11037, 5, 'patient home', NULL),
	(11038, 5, 'nurse home', NULL),
	(11039, 5, 'clerk home', NULL),
	(11040, 5, 'start a visit action', NULL),
	(11041, 5, 'delegate record action', NULL),
	(11042, 5, 'request to delegate action', NULL),
	(11043, 5, 'delegations link', NULL),
	(11044, 3, 'delegators', NULL),
	(11045, 2, 'DAC', NULL),
	(11046, 5, 'bob home', NULL),
	(11047, 5, 'alice home', NULL),
	(11048, 5, 'chrishome', NULL),
	(11049, 5, 'chris home', NULL),
	(11050, 5, 'betty home', NULL),
	(11051, 5, 'lucy home', NULL),
	(11052, 5, 'emily home', NULL),
	(11053, 5, 'requests', NULL),
	(11056, 5, 'accepted requests', NULL),
	(11062, 5, 'accepted requests', NULL),
	(11063, 3, 'Bob', NULL),
	(11064, 3, 'Alice', NULL),
	(11065, 3, 'Chris', NULL),
	(11066, 3, 'Betty', NULL),
	(11067, 3, 'Emily', NULL),
	(11068, 3, 'Lucy', NULL),
	(11069, 5, 'accepted requests', NULL),
	(11070, 5, 'chris record', NULL),
	(11071, 5, 'chris diagnoses', NULL),
	(11072, 5, 'chris info', NULL),
	(11073, 5, 'chris notes', NULL),
	(11074, 5, 'chris treatments', NULL),
	(11075, 5, 'chris prescriptions', NULL),
	(11076, 5, 'chris visits', NULL),
	(11077, 5, 'betty info', NULL),
	(11078, 5, 'betty visits', NULL),
	(11079, 5, 'chris vitals', NULL),
	(11080, 5, 'betty vitals', NULL),
	(11081, 5, 'betty diagnoses', NULL),
	(11082, 5, 'betty treatments', NULL),
	(11083, 5, 'betty notes', NULL),
	(11084, 5, 'betty record', NULL),
	(11086, 5, 'betty prescriptions', NULL),
	(11087, 5, 'bob medical records', NULL),
	(11088, 5, 'alice medical records', NULL),
	(11093, 5, 'inboxes', NULL),
	(11094, 5, 'bob inbox', NULL),
	(11095, 5, 'alice inbox', NULL),
	(11096, 5, 'emily inbox', NULL),
	(11097, 5, 'lucy inbox', NULL),
	(11098, 3, 'DAC uattrs', NULL),
	(11099, 2, 'MLS', NULL),
	(11100, 5, 'Top Secret OA', NULL),
	(11101, 3, 'Secret UA', NULL),
	(11102, 5, 'Secret OA', NULL),
	(11103, 3, 'Top Secret UA', NULL),
	(11104, 5, 'TS betty prescriptions', NULL),
	(11105, 3, 'TS betty UA', NULL),
	(11106, 5, 'outboxes', NULL),
	(11107, 5, 'bob outbox', NULL),
	(11108, 5, 'alice outbox', NULL),
	(11109, 5, 'emily outbox', NULL),
	(11110, 5, 'lucy outbox', NULL),
	(11111, 5, 'bob to alicenull', NULL),
	(11112, 5, 'bob to alicenull', NULL),
	(11113, 5, 'bob to alice1970-01-18 12:59:45.515', NULL),
	(11114, 5, 'bob to alice1970-01-18 12:59:45.767', NULL),
	(11115, 5, 'bob to alice1970-01-18 12:59:45.969', NULL),
	(11116, 5, 'bob to alice1970-01-18 12:59:46.164', NULL);
/*!40000 ALTER TABLE `node` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.node_property
CREATE TABLE IF NOT EXISTS `node_property` (
  `property_node_id` int(11) NOT NULL DEFAULT '0',
  `property_key` varchar(50) NOT NULL,
  `property_value` varchar(300) NOT NULL,
  PRIMARY KEY (`property_node_id`,`property_key`),
  CONSTRAINT `fk_property_node_id` FOREIGN KEY (`property_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.node_property: ~1,380 rows (approximately)
/*!40000 ALTER TABLE `node_property` DISABLE KEYS */;
INSERT INTO `node_property` (`property_node_id`, `property_key`, `property_value`) VALUES
	(-3, 'password', 'super'),
	(-2, 'namespace', 'super'),
	(1, 'schema_comp', 'schema'),
	(2, 'schema_comp', 'schema'),
	(3, 'namespace', 'diagnoses'),
	(3, 'schema', 'pm_health'),
	(3, 'schema_comp', 'table'),
	(4, 'namespace', 'diagnoses'),
	(5, 'namespace', 'diagnoses'),
	(6, 'namespace', 'diagnoses'),
	(7, 'namespace', 'diagnoses'),
	(8, 'namespace', 'diagnoses'),
	(9, 'namespace', 'diagnoses'),
	(9, 'schema_comp', 'row'),
	(10, 'namespace', 'diagnoses'),
	(11, 'namespace', 'diagnoses'),
	(12, 'namespace', 'diagnoses'),
	(13, 'namespace', 'diagnoses'),
	(13, 'schema_comp', 'row'),
	(14, 'namespace', 'diagnoses'),
	(15, 'namespace', 'diagnoses'),
	(16, 'namespace', 'diagnoses'),
	(17, 'namespace', 'diagnoses'),
	(17, 'schema_comp', 'row'),
	(18, 'namespace', 'diagnoses'),
	(19, 'namespace', 'diagnoses'),
	(20, 'namespace', 'diagnoses'),
	(21, 'namespace', 'diagnoses'),
	(21, 'schema_comp', 'row'),
	(22, 'namespace', 'diagnoses'),
	(23, 'namespace', 'diagnoses'),
	(24, 'namespace', 'diagnoses'),
	(25, 'namespace', 'links'),
	(25, 'schema', 'pm_health'),
	(25, 'schema_comp', 'table'),
	(26, 'namespace', 'links'),
	(27, 'namespace', 'links'),
	(28, 'namespace', 'links'),
	(29, 'namespace', 'links'),
	(30, 'namespace', 'links'),
	(31, 'namespace', 'links'),
	(31, 'schema_comp', 'row'),
	(32, 'namespace', 'links'),
	(33, 'namespace', 'links'),
	(34, 'namespace', 'links'),
	(35, 'namespace', 'links'),
	(35, 'schema_comp', 'row'),
	(36, 'namespace', 'links'),
	(37, 'namespace', 'links'),
	(38, 'namespace', 'links'),
	(39, 'namespace', 'links'),
	(39, 'schema_comp', 'row'),
	(40, 'namespace', 'links'),
	(41, 'namespace', 'links'),
	(42, 'namespace', 'links'),
	(43, 'namespace', 'links'),
	(43, 'schema_comp', 'row'),
	(44, 'namespace', 'links'),
	(45, 'namespace', 'links'),
	(46, 'namespace', 'links'),
	(47, 'namespace', 'links'),
	(47, 'schema_comp', 'row'),
	(48, 'namespace', 'links'),
	(49, 'namespace', 'links'),
	(50, 'namespace', 'links'),
	(51, 'namespace', 'links'),
	(51, 'schema_comp', 'row'),
	(52, 'namespace', 'links'),
	(53, 'namespace', 'links'),
	(54, 'namespace', 'links'),
	(55, 'namespace', 'links'),
	(55, 'schema_comp', 'row'),
	(56, 'namespace', 'links'),
	(57, 'namespace', 'links'),
	(58, 'namespace', 'links'),
	(59, 'namespace', 'links'),
	(59, 'schema_comp', 'row'),
	(60, 'namespace', 'links'),
	(61, 'namespace', 'links'),
	(62, 'namespace', 'links'),
	(63, 'namespace', 'links'),
	(63, 'schema_comp', 'row'),
	(64, 'namespace', 'links'),
	(65, 'namespace', 'links'),
	(66, 'namespace', 'links'),
	(67, 'namespace', 'links'),
	(67, 'schema_comp', 'row'),
	(68, 'namespace', 'links'),
	(69, 'namespace', 'links'),
	(70, 'namespace', 'links'),
	(71, 'namespace', 'links'),
	(71, 'schema_comp', 'row'),
	(72, 'namespace', 'links'),
	(73, 'namespace', 'links'),
	(74, 'namespace', 'links'),
	(75, 'namespace', 'links'),
	(75, 'schema_comp', 'row'),
	(76, 'namespace', 'links'),
	(77, 'namespace', 'links'),
	(78, 'namespace', 'links'),
	(79, 'namespace', 'medicines'),
	(79, 'schema', 'pm_health'),
	(79, 'schema_comp', 'table'),
	(80, 'namespace', 'medicines'),
	(81, 'namespace', 'medicines'),
	(82, 'namespace', 'medicines'),
	(83, 'namespace', 'medicines'),
	(84, 'namespace', 'medicines'),
	(85, 'namespace', 'medicines'),
	(85, 'schema_comp', 'row'),
	(86, 'namespace', 'medicines'),
	(87, 'namespace', 'medicines'),
	(88, 'namespace', 'medicines'),
	(89, 'namespace', 'medicines'),
	(89, 'schema_comp', 'row'),
	(90, 'namespace', 'medicines'),
	(91, 'namespace', 'medicines'),
	(92, 'namespace', 'medicines'),
	(93, 'namespace', 'medicines'),
	(93, 'schema_comp', 'row'),
	(94, 'namespace', 'medicines'),
	(95, 'namespace', 'medicines'),
	(96, 'namespace', 'medicines'),
	(97, 'namespace', 'medicines'),
	(97, 'schema_comp', 'row'),
	(98, 'namespace', 'medicines'),
	(99, 'namespace', 'medicines'),
	(100, 'namespace', 'medicines'),
	(101, 'namespace', 'medicines'),
	(101, 'schema_comp', 'row'),
	(102, 'namespace', 'medicines'),
	(103, 'namespace', 'medicines'),
	(104, 'namespace', 'medicines'),
	(105, 'namespace', 'medicines'),
	(105, 'schema_comp', 'row'),
	(106, 'namespace', 'medicines'),
	(107, 'namespace', 'medicines'),
	(108, 'namespace', 'medicines'),
	(109, 'namespace', 'medicines'),
	(109, 'schema_comp', 'row'),
	(110, 'namespace', 'medicines'),
	(111, 'namespace', 'medicines'),
	(112, 'namespace', 'medicines'),
	(113, 'namespace', 'medicines'),
	(113, 'schema_comp', 'row'),
	(114, 'namespace', 'medicines'),
	(115, 'namespace', 'medicines'),
	(116, 'namespace', 'medicines'),
	(117, 'namespace', 'medicines'),
	(117, 'schema_comp', 'row'),
	(118, 'namespace', 'medicines'),
	(119, 'namespace', 'medicines'),
	(120, 'namespace', 'medicines'),
	(121, 'namespace', 'medicines'),
	(121, 'schema_comp', 'row'),
	(122, 'namespace', 'medicines'),
	(123, 'namespace', 'medicines'),
	(124, 'namespace', 'medicines'),
	(125, 'namespace', 'medicines'),
	(125, 'schema_comp', 'row'),
	(126, 'namespace', 'medicines'),
	(127, 'namespace', 'medicines'),
	(128, 'namespace', 'medicines'),
	(129, 'namespace', 'medicines'),
	(129, 'schema_comp', 'row'),
	(130, 'namespace', 'medicines'),
	(131, 'namespace', 'medicines'),
	(132, 'namespace', 'medicines'),
	(133, 'namespace', 'medicines'),
	(133, 'schema_comp', 'row'),
	(134, 'namespace', 'medicines'),
	(135, 'namespace', 'medicines'),
	(136, 'namespace', 'medicines'),
	(137, 'namespace', 'medicines'),
	(137, 'schema_comp', 'row'),
	(138, 'namespace', 'medicines'),
	(139, 'namespace', 'medicines'),
	(140, 'namespace', 'medicines'),
	(141, 'namespace', 'medicines'),
	(141, 'schema_comp', 'row'),
	(142, 'namespace', 'medicines'),
	(143, 'namespace', 'medicines'),
	(144, 'namespace', 'medicines'),
	(145, 'namespace', 'medicines'),
	(145, 'schema_comp', 'row'),
	(146, 'namespace', 'medicines'),
	(147, 'namespace', 'medicines'),
	(148, 'namespace', 'medicines'),
	(149, 'namespace', 'medicines'),
	(149, 'schema_comp', 'row'),
	(150, 'namespace', 'medicines'),
	(151, 'namespace', 'medicines'),
	(152, 'namespace', 'medicines'),
	(153, 'namespace', 'medicines'),
	(153, 'schema_comp', 'row'),
	(154, 'namespace', 'medicines'),
	(155, 'namespace', 'medicines'),
	(156, 'namespace', 'medicines'),
	(157, 'namespace', 'medicines'),
	(157, 'schema_comp', 'row'),
	(158, 'namespace', 'medicines'),
	(159, 'namespace', 'medicines'),
	(160, 'namespace', 'medicines'),
	(161, 'namespace', 'medicines'),
	(161, 'schema_comp', 'row'),
	(162, 'namespace', 'medicines'),
	(163, 'namespace', 'medicines'),
	(164, 'namespace', 'medicines'),
	(165, 'namespace', 'medicines'),
	(165, 'schema_comp', 'row'),
	(166, 'namespace', 'medicines'),
	(167, 'namespace', 'medicines'),
	(168, 'namespace', 'medicines'),
	(169, 'namespace', 'medicines'),
	(169, 'schema_comp', 'row'),
	(170, 'namespace', 'medicines'),
	(171, 'namespace', 'medicines'),
	(172, 'namespace', 'medicines'),
	(173, 'namespace', 'medicines'),
	(173, 'schema_comp', 'row'),
	(174, 'namespace', 'medicines'),
	(175, 'namespace', 'medicines'),
	(176, 'namespace', 'medicines'),
	(177, 'namespace', 'medicines'),
	(177, 'schema_comp', 'row'),
	(178, 'namespace', 'medicines'),
	(179, 'namespace', 'medicines'),
	(180, 'namespace', 'medicines'),
	(181, 'namespace', 'medicines'),
	(181, 'schema_comp', 'row'),
	(182, 'namespace', 'medicines'),
	(183, 'namespace', 'medicines'),
	(184, 'namespace', 'medicines'),
	(185, 'namespace', 'patient_info'),
	(185, 'schema', 'pm_health'),
	(185, 'schema_comp', 'table'),
	(186, 'namespace', 'patient_info'),
	(187, 'namespace', 'patient_info'),
	(188, 'namespace', 'patient_info'),
	(189, 'namespace', 'patient_info'),
	(190, 'namespace', 'patient_info'),
	(191, 'namespace', 'patient_info'),
	(192, 'namespace', 'patient_info'),
	(193, 'namespace', 'patient_info'),
	(194, 'namespace', 'patient_info'),
	(195, 'namespace', 'patient_info'),
	(196, 'namespace', 'patient_info'),
	(197, 'namespace', 'patient_info'),
	(198, 'namespace', 'patient_info'),
	(199, 'namespace', 'patient_info'),
	(200, 'namespace', 'patient_info'),
	(201, 'namespace', 'patient_info'),
	(201, 'schema_comp', 'row'),
	(202, 'namespace', 'patient_info'),
	(203, 'namespace', 'patient_info'),
	(204, 'namespace', 'patient_info'),
	(205, 'namespace', 'patient_info'),
	(206, 'namespace', 'patient_info'),
	(207, 'namespace', 'patient_info'),
	(208, 'namespace', 'patient_info'),
	(209, 'namespace', 'patient_info'),
	(210, 'namespace', 'patient_info'),
	(211, 'namespace', 'patient_info'),
	(212, 'namespace', 'patient_info'),
	(213, 'namespace', 'patient_info'),
	(214, 'namespace', 'patient_info'),
	(215, 'namespace', 'patient_info'),
	(215, 'schema_comp', 'row'),
	(216, 'namespace', 'patient_info'),
	(217, 'namespace', 'patient_info'),
	(218, 'namespace', 'patient_info'),
	(219, 'namespace', 'patient_info'),
	(220, 'namespace', 'patient_info'),
	(221, 'namespace', 'patient_info'),
	(222, 'namespace', 'patient_info'),
	(223, 'namespace', 'patient_info'),
	(224, 'namespace', 'patient_info'),
	(225, 'namespace', 'patient_info'),
	(226, 'namespace', 'patient_info'),
	(227, 'namespace', 'patient_info'),
	(228, 'namespace', 'patient_info'),
	(229, 'namespace', 'prescriptions'),
	(229, 'schema', 'pm_health'),
	(229, 'schema_comp', 'table'),
	(230, 'namespace', 'prescriptions'),
	(231, 'namespace', 'prescriptions'),
	(232, 'namespace', 'prescriptions'),
	(233, 'namespace', 'prescriptions'),
	(234, 'namespace', 'prescriptions'),
	(235, 'namespace', 'prescriptions'),
	(236, 'namespace', 'prescriptions'),
	(237, 'namespace', 'prescriptions'),
	(237, 'schema_comp', 'row'),
	(238, 'namespace', 'prescriptions'),
	(239, 'namespace', 'prescriptions'),
	(240, 'namespace', 'prescriptions'),
	(241, 'namespace', 'prescriptions'),
	(242, 'namespace', 'prescriptions'),
	(243, 'namespace', 'prescriptions'),
	(243, 'schema_comp', 'row'),
	(244, 'namespace', 'prescriptions'),
	(245, 'namespace', 'prescriptions'),
	(246, 'namespace', 'prescriptions'),
	(247, 'namespace', 'prescriptions'),
	(248, 'namespace', 'prescriptions'),
	(249, 'namespace', 'prescriptions'),
	(249, 'schema_comp', 'row'),
	(250, 'namespace', 'prescriptions'),
	(251, 'namespace', 'prescriptions'),
	(252, 'namespace', 'prescriptions'),
	(253, 'namespace', 'prescriptions'),
	(254, 'namespace', 'prescriptions'),
	(255, 'namespace', 'prescriptions'),
	(255, 'schema_comp', 'row'),
	(256, 'namespace', 'prescriptions'),
	(257, 'namespace', 'prescriptions'),
	(258, 'namespace', 'prescriptions'),
	(259, 'namespace', 'prescriptions'),
	(260, 'namespace', 'prescriptions'),
	(261, 'namespace', 'prescriptions'),
	(261, 'schema_comp', 'row'),
	(262, 'namespace', 'prescriptions'),
	(263, 'namespace', 'prescriptions'),
	(264, 'namespace', 'prescriptions'),
	(265, 'namespace', 'prescriptions'),
	(266, 'namespace', 'prescriptions'),
	(267, 'namespace', 'prescriptions'),
	(267, 'schema_comp', 'row'),
	(268, 'namespace', 'prescriptions'),
	(269, 'namespace', 'prescriptions'),
	(270, 'namespace', 'prescriptions'),
	(271, 'namespace', 'prescriptions'),
	(272, 'namespace', 'prescriptions'),
	(273, 'namespace', 'prescriptions'),
	(273, 'schema_comp', 'row'),
	(274, 'namespace', 'prescriptions'),
	(275, 'namespace', 'prescriptions'),
	(276, 'namespace', 'prescriptions'),
	(277, 'namespace', 'prescriptions'),
	(278, 'namespace', 'prescriptions'),
	(279, 'namespace', 'requests'),
	(279, 'schema', 'pm_health'),
	(279, 'schema_comp', 'table'),
	(280, 'namespace', 'requests'),
	(281, 'namespace', 'requests'),
	(282, 'namespace', 'requests'),
	(283, 'namespace', 'requests'),
	(284, 'namespace', 'requests'),
	(285, 'namespace', 'requests'),
	(286, 'namespace', 'requests'),
	(286, 'schema_comp', 'row'),
	(287, 'namespace', 'requests'),
	(288, 'namespace', 'requests'),
	(289, 'namespace', 'requests'),
	(290, 'namespace', 'requests'),
	(291, 'namespace', 'requests'),
	(291, 'schema_comp', 'row'),
	(292, 'namespace', 'requests'),
	(293, 'namespace', 'requests'),
	(294, 'namespace', 'requests'),
	(295, 'namespace', 'requests'),
	(296, 'namespace', 'sessions'),
	(296, 'schema', 'pm_health'),
	(296, 'schema_comp', 'table'),
	(297, 'namespace', 'sessions'),
	(298, 'namespace', 'sessions'),
	(299, 'namespace', 'sessions'),
	(300, 'namespace', 'sessions'),
	(301, 'namespace', 'sessions'),
	(301, 'schema_comp', 'row'),
	(302, 'namespace', 'sessions'),
	(303, 'namespace', 'sessions'),
	(304, 'namespace', 'sessions'),
	(304, 'schema_comp', 'row'),
	(305, 'namespace', 'sessions'),
	(306, 'namespace', 'sessions'),
	(307, 'namespace', 'sessions'),
	(307, 'schema_comp', 'row'),
	(308, 'namespace', 'sessions'),
	(309, 'namespace', 'sessions'),
	(310, 'namespace', 'sessions'),
	(310, 'schema_comp', 'row'),
	(311, 'namespace', 'sessions'),
	(312, 'namespace', 'sessions'),
	(313, 'namespace', 'sessions'),
	(313, 'schema_comp', 'row'),
	(314, 'namespace', 'sessions'),
	(315, 'namespace', 'sessions'),
	(316, 'namespace', 'sessions'),
	(316, 'schema_comp', 'row'),
	(317, 'namespace', 'sessions'),
	(318, 'namespace', 'sessions'),
	(319, 'namespace', 'sessions'),
	(319, 'schema_comp', 'row'),
	(320, 'namespace', 'sessions'),
	(321, 'namespace', 'sessions'),
	(322, 'namespace', 'sessions'),
	(322, 'schema_comp', 'row'),
	(323, 'namespace', 'sessions'),
	(324, 'namespace', 'sessions'),
	(325, 'namespace', 'sessions'),
	(325, 'schema_comp', 'row'),
	(326, 'namespace', 'sessions'),
	(327, 'namespace', 'sessions'),
	(328, 'namespace', 'sessions'),
	(328, 'schema_comp', 'row'),
	(329, 'namespace', 'sessions'),
	(330, 'namespace', 'sessions'),
	(331, 'namespace', 'sessions'),
	(331, 'schema_comp', 'row'),
	(332, 'namespace', 'sessions'),
	(333, 'namespace', 'sessions'),
	(334, 'namespace', 'sessions'),
	(334, 'schema_comp', 'row'),
	(335, 'namespace', 'sessions'),
	(336, 'namespace', 'sessions'),
	(337, 'namespace', 'sessions'),
	(337, 'schema_comp', 'row'),
	(338, 'namespace', 'sessions'),
	(339, 'namespace', 'sessions'),
	(340, 'namespace', 'sessions'),
	(340, 'schema_comp', 'row'),
	(341, 'namespace', 'sessions'),
	(342, 'namespace', 'sessions'),
	(343, 'namespace', 'sessions'),
	(343, 'schema_comp', 'row'),
	(344, 'namespace', 'sessions'),
	(345, 'namespace', 'sessions'),
	(346, 'namespace', 'sessions'),
	(346, 'schema_comp', 'row'),
	(347, 'namespace', 'sessions'),
	(348, 'namespace', 'sessions'),
	(349, 'namespace', 'sessions'),
	(349, 'schema_comp', 'row'),
	(350, 'namespace', 'sessions'),
	(351, 'namespace', 'sessions'),
	(352, 'namespace', 'sessions'),
	(352, 'schema_comp', 'row'),
	(353, 'namespace', 'sessions'),
	(354, 'namespace', 'sessions'),
	(355, 'namespace', 'sessions'),
	(355, 'schema_comp', 'row'),
	(356, 'namespace', 'sessions'),
	(357, 'namespace', 'sessions'),
	(358, 'namespace', 'sessions'),
	(358, 'schema_comp', 'row'),
	(359, 'namespace', 'sessions'),
	(360, 'namespace', 'sessions'),
	(361, 'namespace', 'sessions'),
	(361, 'schema_comp', 'row'),
	(362, 'namespace', 'sessions'),
	(363, 'namespace', 'sessions'),
	(364, 'namespace', 'sessions'),
	(364, 'schema_comp', 'row'),
	(365, 'namespace', 'sessions'),
	(366, 'namespace', 'sessions'),
	(367, 'namespace', 'sessions'),
	(367, 'schema_comp', 'row'),
	(368, 'namespace', 'sessions'),
	(369, 'namespace', 'sessions'),
	(370, 'namespace', 'sessions'),
	(370, 'schema_comp', 'row'),
	(371, 'namespace', 'sessions'),
	(372, 'namespace', 'sessions'),
	(373, 'namespace', 'sessions'),
	(373, 'schema_comp', 'row'),
	(374, 'namespace', 'sessions'),
	(375, 'namespace', 'sessions'),
	(376, 'namespace', 'sessions'),
	(376, 'schema_comp', 'row'),
	(377, 'namespace', 'sessions'),
	(378, 'namespace', 'sessions'),
	(379, 'namespace', 'sessions'),
	(379, 'schema_comp', 'row'),
	(380, 'namespace', 'sessions'),
	(381, 'namespace', 'sessions'),
	(382, 'namespace', 'sessions'),
	(382, 'schema_comp', 'row'),
	(383, 'namespace', 'sessions'),
	(384, 'namespace', 'sessions'),
	(385, 'namespace', 'sessions'),
	(385, 'schema_comp', 'row'),
	(386, 'namespace', 'sessions'),
	(387, 'namespace', 'sessions'),
	(388, 'namespace', 'sessions'),
	(388, 'schema_comp', 'row'),
	(389, 'namespace', 'sessions'),
	(390, 'namespace', 'sessions'),
	(391, 'namespace', 'sessions'),
	(391, 'schema_comp', 'row'),
	(392, 'namespace', 'sessions'),
	(393, 'namespace', 'sessions'),
	(394, 'namespace', 'sessions'),
	(394, 'schema_comp', 'row'),
	(395, 'namespace', 'sessions'),
	(396, 'namespace', 'sessions'),
	(397, 'namespace', 'sessions'),
	(397, 'schema_comp', 'row'),
	(398, 'namespace', 'sessions'),
	(399, 'namespace', 'sessions'),
	(400, 'namespace', 'sessions'),
	(400, 'schema_comp', 'row'),
	(401, 'namespace', 'sessions'),
	(402, 'namespace', 'sessions'),
	(403, 'namespace', 'sessions'),
	(403, 'schema_comp', 'row'),
	(404, 'namespace', 'sessions'),
	(405, 'namespace', 'sessions'),
	(406, 'namespace', 'sessions'),
	(406, 'schema_comp', 'row'),
	(407, 'namespace', 'sessions'),
	(408, 'namespace', 'sessions'),
	(409, 'namespace', 'sessions'),
	(409, 'schema_comp', 'row'),
	(410, 'namespace', 'sessions'),
	(411, 'namespace', 'sessions'),
	(412, 'namespace', 'sessions'),
	(412, 'schema_comp', 'row'),
	(413, 'namespace', 'sessions'),
	(414, 'namespace', 'sessions'),
	(415, 'namespace', 'sessions'),
	(415, 'schema_comp', 'row'),
	(416, 'namespace', 'sessions'),
	(417, 'namespace', 'sessions'),
	(418, 'namespace', 'sessions'),
	(418, 'schema_comp', 'row'),
	(419, 'namespace', 'sessions'),
	(420, 'namespace', 'sessions'),
	(421, 'namespace', 'sessions'),
	(421, 'schema_comp', 'row'),
	(422, 'namespace', 'sessions'),
	(423, 'namespace', 'sessions'),
	(424, 'namespace', 'sessions'),
	(424, 'schema_comp', 'row'),
	(425, 'namespace', 'sessions'),
	(426, 'namespace', 'sessions'),
	(427, 'namespace', 'sessions'),
	(427, 'schema_comp', 'row'),
	(428, 'namespace', 'sessions'),
	(429, 'namespace', 'sessions'),
	(430, 'namespace', 'sessions'),
	(430, 'schema_comp', 'row'),
	(431, 'namespace', 'sessions'),
	(432, 'namespace', 'sessions'),
	(433, 'namespace', 'sessions'),
	(433, 'schema_comp', 'row'),
	(434, 'namespace', 'sessions'),
	(435, 'namespace', 'sessions'),
	(436, 'namespace', 'sessions'),
	(436, 'schema_comp', 'row'),
	(437, 'namespace', 'sessions'),
	(438, 'namespace', 'sessions'),
	(439, 'namespace', 'sessions'),
	(439, 'schema_comp', 'row'),
	(440, 'namespace', 'sessions'),
	(441, 'namespace', 'sessions'),
	(442, 'namespace', 'sessions'),
	(442, 'schema_comp', 'row'),
	(443, 'namespace', 'sessions'),
	(444, 'namespace', 'sessions'),
	(445, 'namespace', 'sessions'),
	(445, 'schema_comp', 'row'),
	(446, 'namespace', 'sessions'),
	(447, 'namespace', 'sessions'),
	(448, 'namespace', 'sessions'),
	(448, 'schema_comp', 'row'),
	(449, 'namespace', 'sessions'),
	(450, 'namespace', 'sessions'),
	(451, 'namespace', 'sessions'),
	(451, 'schema_comp', 'row'),
	(452, 'namespace', 'sessions'),
	(453, 'namespace', 'sessions'),
	(454, 'namespace', 'sessions'),
	(454, 'schema_comp', 'row'),
	(455, 'namespace', 'sessions'),
	(456, 'namespace', 'sessions'),
	(457, 'namespace', 'sessions'),
	(457, 'schema_comp', 'row'),
	(458, 'namespace', 'sessions'),
	(459, 'namespace', 'sessions'),
	(460, 'namespace', 'sessions'),
	(460, 'schema_comp', 'row'),
	(461, 'namespace', 'sessions'),
	(462, 'namespace', 'sessions'),
	(463, 'namespace', 'sessions'),
	(463, 'schema_comp', 'row'),
	(464, 'namespace', 'sessions'),
	(465, 'namespace', 'sessions'),
	(466, 'namespace', 'sessions'),
	(466, 'schema_comp', 'row'),
	(467, 'namespace', 'sessions'),
	(468, 'namespace', 'sessions'),
	(469, 'namespace', 'sessions'),
	(469, 'schema_comp', 'row'),
	(470, 'namespace', 'sessions'),
	(471, 'namespace', 'sessions'),
	(472, 'namespace', 'sessions'),
	(472, 'schema_comp', 'row'),
	(473, 'namespace', 'sessions'),
	(474, 'namespace', 'sessions'),
	(475, 'namespace', 'sessions'),
	(475, 'schema_comp', 'row'),
	(476, 'namespace', 'sessions'),
	(477, 'namespace', 'sessions'),
	(478, 'namespace', 'sessions'),
	(478, 'schema_comp', 'row'),
	(479, 'namespace', 'sessions'),
	(480, 'namespace', 'sessions'),
	(481, 'namespace', 'sessions'),
	(481, 'schema_comp', 'row'),
	(482, 'namespace', 'sessions'),
	(483, 'namespace', 'sessions'),
	(484, 'namespace', 'sessions'),
	(484, 'schema_comp', 'row'),
	(485, 'namespace', 'sessions'),
	(486, 'namespace', 'sessions'),
	(487, 'namespace', 'sessions'),
	(487, 'schema_comp', 'row'),
	(488, 'namespace', 'sessions'),
	(489, 'namespace', 'sessions'),
	(490, 'namespace', 'sessions'),
	(490, 'schema_comp', 'row'),
	(491, 'namespace', 'sessions'),
	(492, 'namespace', 'sessions'),
	(493, 'namespace', 'sessions'),
	(493, 'schema_comp', 'row'),
	(494, 'namespace', 'sessions'),
	(495, 'namespace', 'sessions'),
	(496, 'namespace', 'sessions'),
	(496, 'schema_comp', 'row'),
	(497, 'namespace', 'sessions'),
	(498, 'namespace', 'sessions'),
	(499, 'namespace', 'sessions'),
	(499, 'schema_comp', 'row'),
	(500, 'namespace', 'sessions'),
	(501, 'namespace', 'sessions'),
	(502, 'namespace', 'sessions'),
	(502, 'schema_comp', 'row'),
	(503, 'namespace', 'sessions'),
	(504, 'namespace', 'sessions'),
	(505, 'namespace', 'sessions'),
	(505, 'schema_comp', 'row'),
	(506, 'namespace', 'sessions'),
	(507, 'namespace', 'sessions'),
	(508, 'namespace', 'sessions'),
	(508, 'schema_comp', 'row'),
	(509, 'namespace', 'sessions'),
	(510, 'namespace', 'sessions'),
	(511, 'namespace', 'sessions'),
	(511, 'schema_comp', 'row'),
	(512, 'namespace', 'sessions'),
	(513, 'namespace', 'sessions'),
	(514, 'namespace', 'sessions'),
	(514, 'schema_comp', 'row'),
	(515, 'namespace', 'sessions'),
	(516, 'namespace', 'sessions'),
	(517, 'namespace', 'sessions'),
	(517, 'schema_comp', 'row'),
	(518, 'namespace', 'sessions'),
	(519, 'namespace', 'sessions'),
	(520, 'namespace', 'sessions'),
	(520, 'schema_comp', 'row'),
	(521, 'namespace', 'sessions'),
	(522, 'namespace', 'sessions'),
	(523, 'namespace', 'sessions'),
	(523, 'schema_comp', 'row'),
	(524, 'namespace', 'sessions'),
	(525, 'namespace', 'sessions'),
	(526, 'namespace', 'sessions'),
	(526, 'schema_comp', 'row'),
	(527, 'namespace', 'sessions'),
	(528, 'namespace', 'sessions'),
	(529, 'namespace', 'sessions'),
	(529, 'schema_comp', 'row'),
	(530, 'namespace', 'sessions'),
	(531, 'namespace', 'sessions'),
	(532, 'namespace', 'sessions'),
	(532, 'schema_comp', 'row'),
	(533, 'namespace', 'sessions'),
	(534, 'namespace', 'sessions'),
	(535, 'namespace', 'sessions'),
	(535, 'schema_comp', 'row'),
	(536, 'namespace', 'sessions'),
	(537, 'namespace', 'sessions'),
	(538, 'namespace', 'sessions'),
	(538, 'schema_comp', 'row'),
	(539, 'namespace', 'sessions'),
	(540, 'namespace', 'sessions'),
	(541, 'namespace', 'sessions'),
	(541, 'schema_comp', 'row'),
	(542, 'namespace', 'sessions'),
	(543, 'namespace', 'sessions'),
	(544, 'namespace', 'sessions'),
	(544, 'schema_comp', 'row'),
	(545, 'namespace', 'sessions'),
	(546, 'namespace', 'sessions'),
	(547, 'namespace', 'sessions'),
	(547, 'schema_comp', 'row'),
	(548, 'namespace', 'sessions'),
	(549, 'namespace', 'sessions'),
	(550, 'namespace', 'sessions'),
	(550, 'schema_comp', 'row'),
	(551, 'namespace', 'sessions'),
	(552, 'namespace', 'sessions'),
	(553, 'namespace', 'sessions'),
	(553, 'schema_comp', 'row'),
	(554, 'namespace', 'sessions'),
	(555, 'namespace', 'sessions'),
	(556, 'namespace', 'sessions'),
	(556, 'schema_comp', 'row'),
	(557, 'namespace', 'sessions'),
	(558, 'namespace', 'sessions'),
	(559, 'namespace', 'sessions'),
	(559, 'schema_comp', 'row'),
	(560, 'namespace', 'sessions'),
	(561, 'namespace', 'sessions'),
	(562, 'namespace', 'sessions'),
	(562, 'schema_comp', 'row'),
	(563, 'namespace', 'sessions'),
	(564, 'namespace', 'sessions'),
	(565, 'namespace', 'sessions'),
	(565, 'schema_comp', 'row'),
	(566, 'namespace', 'sessions'),
	(567, 'namespace', 'sessions'),
	(568, 'namespace', 'sessions'),
	(568, 'schema_comp', 'row'),
	(569, 'namespace', 'sessions'),
	(570, 'namespace', 'sessions'),
	(571, 'namespace', 'sessions'),
	(571, 'schema_comp', 'row'),
	(572, 'namespace', 'sessions'),
	(573, 'namespace', 'sessions'),
	(574, 'namespace', 'sessions'),
	(574, 'schema_comp', 'row'),
	(575, 'namespace', 'sessions'),
	(576, 'namespace', 'sessions'),
	(577, 'namespace', 'sessions'),
	(577, 'schema_comp', 'row'),
	(578, 'namespace', 'sessions'),
	(579, 'namespace', 'sessions'),
	(580, 'namespace', 'sessions'),
	(580, 'schema_comp', 'row'),
	(581, 'namespace', 'sessions'),
	(582, 'namespace', 'sessions'),
	(583, 'namespace', 'sessions'),
	(583, 'schema_comp', 'row'),
	(584, 'namespace', 'sessions'),
	(585, 'namespace', 'sessions'),
	(586, 'namespace', 'sessions'),
	(586, 'schema_comp', 'row'),
	(587, 'namespace', 'sessions'),
	(588, 'namespace', 'sessions'),
	(589, 'namespace', 'sessions'),
	(589, 'schema_comp', 'row'),
	(590, 'namespace', 'sessions'),
	(591, 'namespace', 'sessions'),
	(592, 'namespace', 'sessions'),
	(592, 'schema_comp', 'row'),
	(593, 'namespace', 'sessions'),
	(594, 'namespace', 'sessions'),
	(595, 'namespace', 'sessions'),
	(595, 'schema_comp', 'row'),
	(596, 'namespace', 'sessions'),
	(597, 'namespace', 'sessions'),
	(598, 'namespace', 'sessions'),
	(598, 'schema_comp', 'row'),
	(599, 'namespace', 'sessions'),
	(600, 'namespace', 'sessions'),
	(601, 'namespace', 'sessions'),
	(601, 'schema_comp', 'row'),
	(602, 'namespace', 'sessions'),
	(603, 'namespace', 'sessions'),
	(604, 'namespace', 'sessions'),
	(604, 'schema_comp', 'row'),
	(605, 'namespace', 'sessions'),
	(606, 'namespace', 'sessions'),
	(607, 'namespace', 'sessions'),
	(607, 'schema_comp', 'row'),
	(608, 'namespace', 'sessions'),
	(609, 'namespace', 'sessions'),
	(610, 'namespace', 'sessions'),
	(610, 'schema_comp', 'row'),
	(611, 'namespace', 'sessions'),
	(612, 'namespace', 'sessions'),
	(613, 'namespace', 'sessions'),
	(613, 'schema_comp', 'row'),
	(614, 'namespace', 'sessions'),
	(615, 'namespace', 'sessions'),
	(616, 'namespace', 'sessions'),
	(616, 'schema_comp', 'row'),
	(617, 'namespace', 'sessions'),
	(618, 'namespace', 'sessions'),
	(619, 'namespace', 'sessions'),
	(619, 'schema_comp', 'row'),
	(620, 'namespace', 'sessions'),
	(621, 'namespace', 'sessions'),
	(622, 'namespace', 'sessions'),
	(622, 'schema_comp', 'row'),
	(623, 'namespace', 'sessions'),
	(624, 'namespace', 'sessions'),
	(625, 'namespace', 'sessions'),
	(625, 'schema_comp', 'row'),
	(626, 'namespace', 'sessions'),
	(627, 'namespace', 'sessions'),
	(628, 'namespace', 'sessions'),
	(628, 'schema_comp', 'row'),
	(629, 'namespace', 'sessions'),
	(630, 'namespace', 'sessions'),
	(631, 'namespace', 'sessions'),
	(631, 'schema_comp', 'row'),
	(632, 'namespace', 'sessions'),
	(633, 'namespace', 'sessions'),
	(634, 'namespace', 'sessions'),
	(634, 'schema_comp', 'row'),
	(635, 'namespace', 'sessions'),
	(636, 'namespace', 'sessions'),
	(637, 'namespace', 'sessions'),
	(637, 'schema_comp', 'row'),
	(638, 'namespace', 'sessions'),
	(639, 'namespace', 'sessions'),
	(640, 'namespace', 'sessions'),
	(640, 'schema_comp', 'row'),
	(641, 'namespace', 'sessions'),
	(642, 'namespace', 'sessions'),
	(643, 'namespace', 'sessions'),
	(643, 'schema_comp', 'row'),
	(644, 'namespace', 'sessions'),
	(645, 'namespace', 'sessions'),
	(646, 'namespace', 'sessions'),
	(646, 'schema_comp', 'row'),
	(647, 'namespace', 'sessions'),
	(648, 'namespace', 'sessions'),
	(649, 'namespace', 'sessions'),
	(649, 'schema_comp', 'row'),
	(650, 'namespace', 'sessions'),
	(651, 'namespace', 'sessions'),
	(652, 'namespace', 'sessions'),
	(652, 'schema_comp', 'row'),
	(653, 'namespace', 'sessions'),
	(654, 'namespace', 'sessions'),
	(655, 'namespace', 'sessions'),
	(655, 'schema_comp', 'row'),
	(656, 'namespace', 'sessions'),
	(657, 'namespace', 'sessions'),
	(658, 'namespace', 'sessions'),
	(658, 'schema_comp', 'row'),
	(659, 'namespace', 'sessions'),
	(660, 'namespace', 'sessions'),
	(661, 'namespace', 'sessions'),
	(661, 'schema_comp', 'row'),
	(662, 'namespace', 'sessions'),
	(663, 'namespace', 'sessions'),
	(664, 'namespace', 'sessions'),
	(664, 'schema_comp', 'row'),
	(665, 'namespace', 'sessions'),
	(666, 'namespace', 'sessions'),
	(667, 'namespace', 'sessions'),
	(667, 'schema_comp', 'row'),
	(668, 'namespace', 'sessions'),
	(669, 'namespace', 'sessions'),
	(670, 'namespace', 'sessions'),
	(670, 'schema_comp', 'row'),
	(671, 'namespace', 'sessions'),
	(672, 'namespace', 'sessions'),
	(673, 'namespace', 'sessions'),
	(673, 'schema_comp', 'row'),
	(674, 'namespace', 'sessions'),
	(675, 'namespace', 'sessions'),
	(676, 'namespace', 'sessions'),
	(676, 'schema_comp', 'row'),
	(677, 'namespace', 'sessions'),
	(678, 'namespace', 'sessions'),
	(679, 'namespace', 'sessions'),
	(679, 'schema_comp', 'row'),
	(680, 'namespace', 'sessions'),
	(681, 'namespace', 'sessions'),
	(682, 'namespace', 'sessions'),
	(682, 'schema_comp', 'row'),
	(683, 'namespace', 'sessions'),
	(684, 'namespace', 'sessions'),
	(685, 'namespace', 'sessions'),
	(685, 'schema_comp', 'row'),
	(686, 'namespace', 'sessions'),
	(687, 'namespace', 'sessions'),
	(688, 'namespace', 'sessions'),
	(688, 'schema_comp', 'row'),
	(689, 'namespace', 'sessions'),
	(690, 'namespace', 'sessions'),
	(691, 'namespace', 'sessions'),
	(691, 'schema_comp', 'row'),
	(692, 'namespace', 'sessions'),
	(693, 'namespace', 'sessions'),
	(694, 'namespace', 'sessions'),
	(694, 'schema_comp', 'row'),
	(695, 'namespace', 'sessions'),
	(696, 'namespace', 'sessions'),
	(697, 'namespace', 'sessions'),
	(697, 'schema_comp', 'row'),
	(698, 'namespace', 'sessions'),
	(699, 'namespace', 'sessions'),
	(700, 'namespace', 'sessions'),
	(700, 'schema_comp', 'row'),
	(701, 'namespace', 'sessions'),
	(702, 'namespace', 'sessions'),
	(703, 'namespace', 'sessions'),
	(703, 'schema_comp', 'row'),
	(704, 'namespace', 'sessions'),
	(705, 'namespace', 'sessions'),
	(706, 'namespace', 'sessions'),
	(706, 'schema_comp', 'row'),
	(707, 'namespace', 'sessions'),
	(708, 'namespace', 'sessions'),
	(709, 'namespace', 'sessions'),
	(709, 'schema_comp', 'row'),
	(710, 'namespace', 'sessions'),
	(711, 'namespace', 'sessions'),
	(712, 'namespace', 'sessions'),
	(712, 'schema_comp', 'row'),
	(713, 'namespace', 'sessions'),
	(714, 'namespace', 'sessions'),
	(715, 'namespace', 'sessions'),
	(715, 'schema_comp', 'row'),
	(716, 'namespace', 'sessions'),
	(717, 'namespace', 'sessions'),
	(718, 'namespace', 'sessions'),
	(718, 'schema_comp', 'row'),
	(719, 'namespace', 'sessions'),
	(720, 'namespace', 'sessions'),
	(721, 'namespace', 'sessions'),
	(721, 'schema_comp', 'row'),
	(722, 'namespace', 'sessions'),
	(723, 'namespace', 'sessions'),
	(724, 'namespace', 'sessions'),
	(724, 'schema_comp', 'row'),
	(725, 'namespace', 'sessions'),
	(726, 'namespace', 'sessions'),
	(727, 'namespace', 'sessions'),
	(727, 'schema_comp', 'row'),
	(728, 'namespace', 'sessions'),
	(729, 'namespace', 'sessions'),
	(730, 'namespace', 'sessions'),
	(730, 'schema_comp', 'row'),
	(731, 'namespace', 'sessions'),
	(732, 'namespace', 'sessions'),
	(733, 'namespace', 'sessions'),
	(733, 'schema_comp', 'row'),
	(734, 'namespace', 'sessions'),
	(735, 'namespace', 'sessions'),
	(736, 'namespace', 'sessions'),
	(736, 'schema_comp', 'row'),
	(737, 'namespace', 'sessions'),
	(738, 'namespace', 'sessions'),
	(739, 'namespace', 'sessions'),
	(739, 'schema_comp', 'row'),
	(740, 'namespace', 'sessions'),
	(741, 'namespace', 'sessions'),
	(742, 'namespace', 'sessions'),
	(742, 'schema_comp', 'row'),
	(743, 'namespace', 'sessions'),
	(744, 'namespace', 'sessions'),
	(745, 'namespace', 'sessions'),
	(745, 'schema_comp', 'row'),
	(746, 'namespace', 'sessions'),
	(747, 'namespace', 'sessions'),
	(748, 'namespace', 'sessions'),
	(748, 'schema_comp', 'row'),
	(749, 'namespace', 'sessions'),
	(750, 'namespace', 'sessions'),
	(751, 'namespace', 'sessions'),
	(751, 'schema_comp', 'row'),
	(752, 'namespace', 'sessions'),
	(753, 'namespace', 'sessions'),
	(754, 'namespace', 'sessions'),
	(754, 'schema_comp', 'row'),
	(755, 'namespace', 'sessions'),
	(756, 'namespace', 'sessions'),
	(757, 'namespace', 'sessions'),
	(757, 'schema_comp', 'row'),
	(758, 'namespace', 'sessions'),
	(759, 'namespace', 'sessions'),
	(760, 'namespace', 'sessions'),
	(760, 'schema_comp', 'row'),
	(761, 'namespace', 'sessions'),
	(762, 'namespace', 'sessions'),
	(763, 'namespace', 'sessions'),
	(763, 'schema_comp', 'row'),
	(764, 'namespace', 'sessions'),
	(765, 'namespace', 'sessions'),
	(766, 'namespace', 'sessions'),
	(766, 'schema_comp', 'row'),
	(767, 'namespace', 'sessions'),
	(768, 'namespace', 'sessions'),
	(769, 'namespace', 'sessions'),
	(769, 'schema_comp', 'row'),
	(770, 'namespace', 'sessions'),
	(771, 'namespace', 'sessions'),
	(772, 'namespace', 'sessions'),
	(772, 'schema_comp', 'row'),
	(773, 'namespace', 'sessions'),
	(774, 'namespace', 'sessions'),
	(775, 'namespace', 'sessions'),
	(775, 'schema_comp', 'row'),
	(776, 'namespace', 'sessions'),
	(777, 'namespace', 'sessions'),
	(778, 'namespace', 'sessions'),
	(778, 'schema_comp', 'row'),
	(779, 'namespace', 'sessions'),
	(780, 'namespace', 'sessions'),
	(781, 'namespace', 'sessions'),
	(781, 'schema_comp', 'row'),
	(782, 'namespace', 'sessions'),
	(783, 'namespace', 'sessions'),
	(784, 'namespace', 'sessions'),
	(784, 'schema_comp', 'row'),
	(785, 'namespace', 'sessions'),
	(786, 'namespace', 'sessions'),
	(787, 'namespace', 'sessions'),
	(787, 'schema_comp', 'row'),
	(788, 'namespace', 'sessions'),
	(789, 'namespace', 'sessions'),
	(790, 'namespace', 'sessions'),
	(790, 'schema_comp', 'row'),
	(791, 'namespace', 'sessions'),
	(792, 'namespace', 'sessions'),
	(793, 'namespace', 'sessions'),
	(793, 'schema_comp', 'row'),
	(794, 'namespace', 'sessions'),
	(795, 'namespace', 'sessions'),
	(796, 'namespace', 'sessions'),
	(796, 'schema_comp', 'row'),
	(797, 'namespace', 'sessions'),
	(798, 'namespace', 'sessions'),
	(799, 'namespace', 'sessions'),
	(799, 'schema_comp', 'row'),
	(800, 'namespace', 'sessions'),
	(801, 'namespace', 'sessions'),
	(802, 'namespace', 'sessions'),
	(802, 'schema_comp', 'row'),
	(803, 'namespace', 'sessions'),
	(804, 'namespace', 'sessions'),
	(805, 'namespace', 'sessions'),
	(805, 'schema_comp', 'row'),
	(806, 'namespace', 'sessions'),
	(807, 'namespace', 'sessions'),
	(808, 'namespace', 'sessions'),
	(808, 'schema_comp', 'row'),
	(809, 'namespace', 'sessions'),
	(810, 'namespace', 'sessions'),
	(811, 'namespace', 'sessions'),
	(811, 'schema_comp', 'row'),
	(812, 'namespace', 'sessions'),
	(813, 'namespace', 'sessions'),
	(814, 'namespace', 'sessions'),
	(814, 'schema_comp', 'row'),
	(815, 'namespace', 'sessions'),
	(816, 'namespace', 'sessions'),
	(817, 'namespace', 'sessions'),
	(817, 'schema_comp', 'row'),
	(818, 'namespace', 'sessions'),
	(819, 'namespace', 'sessions'),
	(820, 'namespace', 'sessions'),
	(820, 'schema_comp', 'row'),
	(821, 'namespace', 'sessions'),
	(822, 'namespace', 'sessions'),
	(823, 'namespace', 'sessions'),
	(823, 'schema_comp', 'row'),
	(824, 'namespace', 'sessions'),
	(825, 'namespace', 'sessions'),
	(826, 'namespace', 'sessions'),
	(826, 'schema_comp', 'row'),
	(827, 'namespace', 'sessions'),
	(828, 'namespace', 'sessions'),
	(829, 'namespace', 'sessions'),
	(829, 'schema_comp', 'row'),
	(830, 'namespace', 'sessions'),
	(831, 'namespace', 'sessions'),
	(832, 'namespace', 'sessions'),
	(832, 'schema_comp', 'row'),
	(833, 'namespace', 'sessions'),
	(834, 'namespace', 'sessions'),
	(835, 'namespace', 'sessions'),
	(835, 'schema_comp', 'row'),
	(836, 'namespace', 'sessions'),
	(837, 'namespace', 'sessions'),
	(838, 'namespace', 'sessions'),
	(838, 'schema_comp', 'row'),
	(839, 'namespace', 'sessions'),
	(840, 'namespace', 'sessions'),
	(841, 'namespace', 'treatments'),
	(841, 'schema', 'pm_health'),
	(841, 'schema_comp', 'table'),
	(842, 'namespace', 'treatments'),
	(843, 'namespace', 'treatments'),
	(844, 'namespace', 'treatments'),
	(845, 'namespace', 'treatments'),
	(846, 'namespace', 'treatments'),
	(847, 'namespace', 'treatments'),
	(847, 'schema_comp', 'row'),
	(848, 'namespace', 'treatments'),
	(849, 'namespace', 'treatments'),
	(850, 'namespace', 'treatments'),
	(851, 'namespace', 'treatments'),
	(851, 'schema_comp', 'row'),
	(852, 'namespace', 'treatments'),
	(853, 'namespace', 'treatments'),
	(854, 'namespace', 'treatments'),
	(855, 'namespace', 'treatments'),
	(855, 'schema_comp', 'row'),
	(856, 'namespace', 'treatments'),
	(857, 'namespace', 'treatments'),
	(858, 'namespace', 'treatments'),
	(859, 'namespace', 'treatments'),
	(859, 'schema_comp', 'row'),
	(860, 'namespace', 'treatments'),
	(861, 'namespace', 'treatments'),
	(862, 'namespace', 'treatments'),
	(863, 'namespace', 'treatments'),
	(863, 'schema_comp', 'row'),
	(864, 'namespace', 'treatments'),
	(865, 'namespace', 'treatments'),
	(866, 'namespace', 'treatments'),
	(867, 'namespace', 'treatments'),
	(867, 'schema_comp', 'row'),
	(868, 'namespace', 'treatments'),
	(869, 'namespace', 'treatments'),
	(870, 'namespace', 'treatments'),
	(871, 'namespace', 'users'),
	(871, 'schema', 'pm_health'),
	(871, 'schema_comp', 'table'),
	(872, 'namespace', 'users'),
	(873, 'namespace', 'users'),
	(874, 'namespace', 'users'),
	(875, 'namespace', 'users'),
	(876, 'namespace', 'users'),
	(877, 'namespace', 'users'),
	(877, 'schema_comp', 'row'),
	(878, 'namespace', 'users'),
	(879, 'namespace', 'users'),
	(880, 'namespace', 'users'),
	(881, 'namespace', 'users'),
	(881, 'schema_comp', 'row'),
	(882, 'namespace', 'users'),
	(883, 'namespace', 'users'),
	(884, 'namespace', 'users'),
	(885, 'namespace', 'users'),
	(885, 'schema_comp', 'row'),
	(886, 'namespace', 'users'),
	(887, 'namespace', 'users'),
	(888, 'namespace', 'users'),
	(889, 'namespace', 'users'),
	(889, 'schema_comp', 'row'),
	(890, 'namespace', 'users'),
	(891, 'namespace', 'users'),
	(892, 'namespace', 'users'),
	(893, 'namespace', 'users'),
	(893, 'schema_comp', 'row'),
	(894, 'namespace', 'users'),
	(895, 'namespace', 'users'),
	(896, 'namespace', 'users'),
	(897, 'namespace', 'users'),
	(897, 'schema_comp', 'row'),
	(898, 'namespace', 'users'),
	(899, 'namespace', 'users'),
	(900, 'namespace', 'users'),
	(901, 'namespace', 'visit_notes'),
	(901, 'schema', 'pm_health'),
	(901, 'schema_comp', 'table'),
	(902, 'namespace', 'visit_notes'),
	(903, 'namespace', 'visit_notes'),
	(904, 'namespace', 'visit_notes'),
	(905, 'namespace', 'visit_notes'),
	(906, 'namespace', 'visit_notes'),
	(907, 'namespace', 'visit_notes'),
	(907, 'schema_comp', 'row'),
	(908, 'namespace', 'visit_notes'),
	(909, 'namespace', 'visit_notes'),
	(910, 'namespace', 'visit_notes'),
	(911, 'namespace', 'visit_notes'),
	(911, 'schema_comp', 'row'),
	(912, 'namespace', 'visit_notes'),
	(913, 'namespace', 'visit_notes'),
	(914, 'namespace', 'visit_notes'),
	(915, 'namespace', 'visit_notes'),
	(915, 'schema_comp', 'row'),
	(916, 'namespace', 'visit_notes'),
	(917, 'namespace', 'visit_notes'),
	(918, 'namespace', 'visit_notes'),
	(919, 'namespace', 'visit_notes'),
	(919, 'schema_comp', 'row'),
	(920, 'namespace', 'visit_notes'),
	(921, 'namespace', 'visit_notes'),
	(922, 'namespace', 'visit_notes'),
	(923, 'namespace', 'visit_notes'),
	(923, 'schema_comp', 'row'),
	(924, 'namespace', 'visit_notes'),
	(925, 'namespace', 'visit_notes'),
	(926, 'namespace', 'visit_notes'),
	(927, 'namespace', 'visit_notes'),
	(927, 'schema_comp', 'row'),
	(928, 'namespace', 'visit_notes'),
	(929, 'namespace', 'visit_notes'),
	(930, 'namespace', 'visit_notes'),
	(931, 'namespace', 'visit_notes'),
	(931, 'schema_comp', 'row'),
	(932, 'namespace', 'visit_notes'),
	(933, 'namespace', 'visit_notes'),
	(934, 'namespace', 'visit_notes'),
	(935, 'namespace', 'visits'),
	(935, 'schema', 'pm_health'),
	(935, 'schema_comp', 'table'),
	(936, 'namespace', 'visits'),
	(937, 'namespace', 'visits'),
	(938, 'namespace', 'visits'),
	(939, 'namespace', 'visits'),
	(940, 'namespace', 'visits'),
	(941, 'namespace', 'visits'),
	(942, 'namespace', 'visits'),
	(943, 'namespace', 'visits'),
	(944, 'namespace', 'visits'),
	(945, 'namespace', 'visits'),
	(945, 'schema_comp', 'row'),
	(946, 'namespace', 'visits'),
	(947, 'namespace', 'visits'),
	(948, 'namespace', 'visits'),
	(949, 'namespace', 'visits'),
	(950, 'namespace', 'visits'),
	(951, 'namespace', 'visits'),
	(952, 'namespace', 'visits'),
	(953, 'namespace', 'visits'),
	(953, 'schema_comp', 'row'),
	(954, 'namespace', 'visits'),
	(955, 'namespace', 'visits'),
	(956, 'namespace', 'visits'),
	(957, 'namespace', 'visits'),
	(958, 'namespace', 'visits'),
	(959, 'namespace', 'visits'),
	(960, 'namespace', 'visits'),
	(961, 'namespace', 'visits'),
	(961, 'schema_comp', 'row'),
	(962, 'namespace', 'visits'),
	(963, 'namespace', 'visits'),
	(964, 'namespace', 'visits'),
	(965, 'namespace', 'visits'),
	(966, 'namespace', 'visits'),
	(967, 'namespace', 'visits'),
	(968, 'namespace', 'visits'),
	(969, 'namespace', 'visits'),
	(969, 'schema_comp', 'row'),
	(970, 'namespace', 'visits'),
	(971, 'namespace', 'visits'),
	(972, 'namespace', 'visits'),
	(973, 'namespace', 'visits'),
	(974, 'namespace', 'visits'),
	(975, 'namespace', 'visits'),
	(976, 'namespace', 'visits'),
	(977, 'namespace', 'visits'),
	(977, 'schema_comp', 'row'),
	(978, 'namespace', 'visits'),
	(979, 'namespace', 'visits'),
	(980, 'namespace', 'visits'),
	(981, 'namespace', 'visits'),
	(982, 'namespace', 'visits'),
	(983, 'namespace', 'visits'),
	(984, 'namespace', 'visits'),
	(985, 'namespace', 'visits'),
	(985, 'schema_comp', 'row'),
	(986, 'namespace', 'visits'),
	(987, 'namespace', 'visits'),
	(988, 'namespace', 'visits'),
	(989, 'namespace', 'visits'),
	(990, 'namespace', 'visits'),
	(991, 'namespace', 'visits'),
	(992, 'namespace', 'visits'),
	(993, 'namespace', 'visits'),
	(993, 'schema_comp', 'row'),
	(994, 'namespace', 'visits'),
	(995, 'namespace', 'visits'),
	(996, 'namespace', 'visits'),
	(997, 'namespace', 'visits'),
	(998, 'namespace', 'visits'),
	(999, 'namespace', 'visits'),
	(1000, 'namespace', 'visits'),
	(1001, 'namespace', 'vitals'),
	(1001, 'schema', 'pm_health'),
	(1001, 'schema_comp', 'table'),
	(1002, 'namespace', 'vitals'),
	(1003, 'namespace', 'vitals'),
	(1004, 'namespace', 'vitals'),
	(1005, 'namespace', 'vitals'),
	(1006, 'namespace', 'vitals'),
	(1007, 'namespace', 'vitals'),
	(1008, 'namespace', 'vitals'),
	(1009, 'namespace', 'vitals'),
	(1010, 'namespace', 'vitals'),
	(1010, 'schema_comp', 'row'),
	(1011, 'namespace', 'vitals'),
	(1012, 'namespace', 'vitals'),
	(1013, 'namespace', 'vitals'),
	(1014, 'namespace', 'vitals'),
	(1015, 'namespace', 'vitals'),
	(1016, 'namespace', 'vitals'),
	(1017, 'namespace', 'vitals'),
	(1017, 'schema_comp', 'row'),
	(1018, 'namespace', 'vitals'),
	(1019, 'namespace', 'vitals'),
	(1020, 'namespace', 'vitals'),
	(1021, 'namespace', 'vitals'),
	(1022, 'namespace', 'vitals'),
	(1023, 'namespace', 'vitals'),
	(1024, 'namespace', 'vitals'),
	(1024, 'schema_comp', 'row'),
	(1025, 'namespace', 'vitals'),
	(1026, 'namespace', 'vitals'),
	(1027, 'namespace', 'vitals'),
	(1028, 'namespace', 'vitals'),
	(1029, 'namespace', 'vitals'),
	(1030, 'namespace', 'vitals'),
	(1031, 'namespace', 'vitals'),
	(1031, 'schema_comp', 'row'),
	(1032, 'namespace', 'vitals'),
	(1033, 'namespace', 'vitals'),
	(1034, 'namespace', 'vitals'),
	(1035, 'namespace', 'vitals'),
	(1036, 'namespace', 'vitals'),
	(1037, 'namespace', 'vitals'),
	(1038, 'namespace', 'vitals'),
	(1038, 'schema_comp', 'row'),
	(1039, 'namespace', 'vitals'),
	(1040, 'namespace', 'vitals'),
	(1041, 'namespace', 'vitals'),
	(1042, 'namespace', 'vitals'),
	(1043, 'namespace', 'vitals'),
	(1044, 'namespace', 'vitals'),
	(1045, 'namespace', 'vitals'),
	(1045, 'schema_comp', 'row'),
	(1046, 'namespace', 'vitals'),
	(1047, 'namespace', 'vitals'),
	(1048, 'namespace', 'vitals'),
	(1049, 'namespace', 'vitals'),
	(1050, 'namespace', 'vitals'),
	(1051, 'namespace', 'vitals'),
	(1052, 'namespace', 'vitals'),
	(1052, 'schema_comp', 'row'),
	(1053, 'namespace', 'vitals'),
	(1054, 'namespace', 'vitals'),
	(1055, 'namespace', 'vitals'),
	(1056, 'namespace', 'vitals'),
	(1057, 'namespace', 'vitals'),
	(1058, 'namespace', 'vitals'),
	(11004, 'password', '10085d0e213d42613649638b4cf2d7c8578a8fa2c4ff860a3b234206c1b2c95e00283cca6efb7b2063832030137437462d633ab57405a7e097c42c61e08af7be49b471c5c7834053e5b626f66ad089db2ec'),
	(11005, 'password', '100fc7ae32739b92bc1516a730f7cc49a2180c27c8d599bfeffbc38c8e2d95456ac693230eb5ec2f94dc841c3fc60cda9a8e01e39e68a66b477d85fe1bd16bc589268b35c1b9176c88b75a88a919302ae79'),
	(11006, 'password', '100b4526a27e4f78446ce119f97d43b1822cb2e950048a48a101f9dc490a07238201eb6f5c96499563f9a6868eb041e04e3415217f230fddbd0f9d7414ea4b82f22854bb6bf1fddb6d24a7640be1e953b1e'),
	(11007, 'password', '1004f1e7bebbe98831adcc2038a42eb0fc41307ea2bf07ab6a7e62e0cd2f0b6147d9b7063985a76bd49c76ea3c1a4d1cc26154d111ebdce652f394aad04a9f31f08e1d2849bd559a151605f329d9e79ff48'),
	(11008, 'password', '100aad9fb12e2a66ed9606f4e04eeeea51bfc3badfceb330cfe2685eaab6f6a4830227a6886a0b35bdc143610953304c24d70bcf99b7b56c7cab56eabf537184a742dae4ef7e9a65d9366beb732c3df0763'),
	(11009, 'password', '1009178381cb66f5f04b6a5bd13e8d8f1befe8c99cebdb7f2f1c79273ee2d1186f6d77b4595155aafb84dae11c07de544f0bcafdf422d99a408a2b2a2f8d4a58d790d5fc8c2000e4532bacde3a0550cee61'),
	(11017, 'namespace', 'RBAC'),
	(11018, 'namespace', 'RBAC'),
	(11019, 'namespace', 'RBAC'),
	(11020, 'namespace', 'RBAC'),
	(11021, 'namespace', 'RBAC'),
	(11022, 'namespace', 'RBAC'),
	(11023, 'namespace', 'RBAC'),
	(11024, 'namespace', 'RBAC'),
	(11029, 'namespace', 'RBAC'),
	(11052, 'namespace', 'emily'),
	(11053, 'namespace', 'chris'),
	(11062, 'namespace', 'bob'),
	(11069, 'namespace', 'chris'),
	(11070, 'patient', 'chris'),
	(11070, 'patient_id', '1'),
	(11084, 'patient', 'betty'),
	(11084, 'patient_id', '2'),
	(11094, 'inbox', 'bob'),
	(11095, 'inbox', 'alice'),
	(11096, 'inbox', 'emily'),
	(11097, 'inbox', 'lucy'),
	(11107, 'outbox', 'bob'),
	(11108, 'outbox', 'alice'),
	(11109, 'outbox', 'emily'),
	(11110, 'outbox', 'lucy');
/*!40000 ALTER TABLE `node_property` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.node_type
CREATE TABLE IF NOT EXISTS `node_type` (
  `node_type_id` int(11) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`node_type_id`),
  KEY `idx_node_type_description` (`description`),
  KEY `idx_node_type_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table contains node types';

-- Dumping data for table pmwsdb.node_type: ~9 rows (approximately)
/*!40000 ALTER TABLE `node_type` DISABLE KEYS */;
INSERT INTO `node_type` (`node_type_id`, `name`, `description`) VALUES
	(1, 'c', 'Connector'),
	(2, 'pc', 'Policy Class'),
	(3, 'ua', 'User Attribute'),
	(4, 'u', 'User'),
	(5, 'oa', 'Object Attribute'),
	(6, 'o', 'Object'),
	(7, 'os', 'Operation Set'),
	(8, 'd', 'Deny'),
	(9, 's', 'Session');
/*!40000 ALTER TABLE `node_type` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.object_class
CREATE TABLE IF NOT EXISTS `object_class` (
  `object_class_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`object_class_id`),
  KEY `idx_object_class_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Object Class';

-- Dumping data for table pmwsdb.object_class: ~11 rows (approximately)
/*!40000 ALTER TABLE `object_class` DISABLE KEYS */;
INSERT INTO `object_class` (`object_class_id`, `name`, `description`) VALUES
	(1, 'class', 'Class of all object classes'),
	(2, 'File', 'Class of files'),
	(3, 'Directory', 'Class of directories'),
	(4, 'User', 'Class of PM users'),
	(5, 'User attribute', 'Class of PM user attributes'),
	(6, 'Object', 'Class of PM objects'),
	(7, 'Object attribute', 'Class of PM object attributes'),
	(8, 'Connector', 'Class of the PM connector node'),
	(9, 'Policy class', 'Class of PM policy classes'),
	(10, 'Operation set', 'Class of PM operation sets'),
	(11, '*', 'Class any class');
/*!40000 ALTER TABLE `object_class` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.object_detail
CREATE TABLE IF NOT EXISTS `object_detail` (
  `object_node_id` int(11) NOT NULL AUTO_INCREMENT,
  `original_node_id` int(11) DEFAULT NULL,
  `object_class_id` int(11) DEFAULT NULL,
  `host_id` int(11) DEFAULT NULL,
  `path` varchar(300) DEFAULT NULL,
  `include_ascedants` int(1) DEFAULT '0',
  `template_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`object_node_id`),
  KEY `object_type_id_idx` (`object_class_id`),
  KEY `fk_object_host_id_idx` (`host_id`),
  KEY `fk_original_node_id_idx` (`original_node_id`),
  KEY `fk_obj_detail_tpl_id_idx` (`template_id`),
  CONSTRAINT `fk_obj_det_object_class_id` FOREIGN KEY (`object_class_id`) REFERENCES `object_class` (`object_class_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_obj_detail_tpl_id` FOREIGN KEY (`template_id`) REFERENCES `template` (`template_id`) ON DELETE SET NULL ON UPDATE NO ACTION,
  CONSTRAINT `fk_object_host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`host_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_object_node_id` FOREIGN KEY (`object_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_original_node_id` FOREIGN KEY (`original_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Object Details';

-- Dumping data for table pmwsdb.object_detail: ~0 rows (approximately)
/*!40000 ALTER TABLE `object_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `object_detail` ENABLE KEYS */;

-- Dumping structure for view pmwsdb.object_view
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `object_view` (
	`obj_id` INT(11) NOT NULL
) ENGINE=MyISAM;

-- Dumping structure for table pmwsdb.open_object
CREATE TABLE IF NOT EXISTS `open_object` (
  `session_id` varchar(150) NOT NULL,
  `object_node_id` int(11) NOT NULL,
  `count` int(2) DEFAULT NULL,
  PRIMARY KEY (`session_id`,`object_node_id`),
  KEY `fk_object_id_oo_idx` (`object_node_id`),
  CONSTRAINT `fk_object_id_oo` FOREIGN KEY (`object_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_sessid_oo` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table for open objects';

-- Dumping data for table pmwsdb.open_object: ~0 rows (approximately)
/*!40000 ALTER TABLE `open_object` DISABLE KEYS */;
/*!40000 ALTER TABLE `open_object` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.operation
CREATE TABLE IF NOT EXISTS `operation` (
  `operation_id` int(11) NOT NULL AUTO_INCREMENT,
  `operation_type_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `object_class_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`operation_id`),
  UNIQUE KEY `operation_id_UNIQUE` (`operation_id`),
  KEY `operation_type_id_idx` (`operation_type_id`),
  KEY `fk_operation_object_class_id_idx` (`object_class_id`),
  KEY `idx_operation_name` (`name`),
  CONSTRAINT `fk_operation_object_class_id` FOREIGN KEY (`object_class_id`) REFERENCES `object_class` (`object_class_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_operation_type_id` FOREIGN KEY (`operation_type_id`) REFERENCES `operation_type` (`operation_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Operation';

-- Dumping data for table pmwsdb.operation: ~65 rows (approximately)
/*!40000 ALTER TABLE `operation` DISABLE KEYS */;
INSERT INTO `operation` (`operation_id`, `operation_type_id`, `name`, `description`, `object_class_id`) VALUES
	(1, 1, 'Class create class', 'Class create class', 1),
	(2, 1, 'Class delete class', 'Class delete class', 2),
	(3, 1, '*', '*', 11),
	(4, 1, 'File modify', 'File modify', 2),
	(5, 1, 'File read and execute', 'File read and execute', 2),
	(6, 1, 'File read', 'File read', 2),
	(7, 1, 'File write', 'File write', 2),
	(8, 1, 'Dir modify', 'Dir modify', 3),
	(9, 1, 'Dir read and execute', 'Dir read and execute', 3),
	(10, 1, 'Dir list contents', 'Dir list contents', 3),
	(11, 1, 'Dir read', 'Dir read', 3),
	(12, 1, 'Dir write', 'Dir write', 3),
	(13, 1, 'User create user attribute', 'User create user attribute', 4),
	(14, 1, 'User assign', 'User assign', 4),
	(15, 1, 'User delete', 'User delete', 4),
	(16, 1, 'User delete assign', 'User delete assign', 4),
	(17, 1, 'Entity represent', 'Entity represent', 4),
	(18, 1, 'User attribute create user attribute', 'User attribute create user attribute', 5),
	(19, 1, 'User attribute create user', 'User attribute create user', 5),
	(20, 1, 'User attribute delete user', 'User attribute delete user', 5),
	(21, 1, 'User attribute create operation set', 'User attribute create operation set', 5),
	(22, 1, 'User attribute assign to operation set', 'User attribute assign to operation set', 5),
	(23, 1, 'User attribute assign', 'User attribute assign', 5),
	(24, 1, 'User attribute assign to', 'User attribute assign to', 5),
	(25, 1, 'User attribute delete', 'User attribute delete', 5),
	(26, 1, 'User attribute delete assign', 'User attribute delete assign', 5),
	(27, 1, 'User attribute delete assign to', 'User attribute delete assign to', 5),
	(28, 1, 'Object delete', 'Object delete', 6),
	(29, 1, 'Object attribute create object', 'Object attribute create object', 7),
	(30, 1, 'Object attribute delete object', 'Object attribute delete object', 7),
	(31, 1, 'Object attribute create object attribute', 'Object attribute create object attribute', 7),
	(32, 1, 'Object attribute delete object attribute', 'Object attribute delete object attribute', 7),
	(33, 1, 'Object attribute create operation set', 'Object attribute create operation set', 7),
	(34, 1, 'Object attribute assign', 'Object attribute assign', 7),
	(35, 1, 'Object attribute assign to', 'Object attribute assign to', 7),
	(36, 1, 'Object attribute delete', 'Object attribute delete', 7),
	(37, 1, 'Object attribute delete assign', 'Object attribute delete assign', 7),
	(38, 1, 'Object attribute delete assign to', 'Object attribute delete assign to', 7),
	(39, 1, 'Policy class create user attribute', 'Policy class create user attribute', 9),
	(40, 1, 'Policy class delete user attribute', 'Policy class delete user attribute', 9),
	(41, 1, 'Policy class create object attribute', 'Policy class create object attribute', 9),
	(42, 1, 'Policy class delete object attribute', 'Policy class delete object attribute', 9),
	(43, 1, 'Policy class create object', 'Policy class create object', 9),
	(44, 1, 'Policy class assign', 'Policy class assign', 9),
	(45, 1, 'Policy class assign to', 'Policy class assign to', 9),
	(46, 1, 'Policy class delete', 'Policy class delete', 9),
	(47, 1, 'Policy class delete assign', 'Policy class delete assign', 9),
	(48, 1, 'Policy class delete assign to', 'Policy class delete assign to', 9),
	(49, 1, 'Operation set assign', 'Operation set assign', 10),
	(50, 1, 'Operation set assign to', 'Operation set assign to', 10),
	(51, 1, 'Operation set delete', 'Operation set delete', 10),
	(52, 1, 'Operation set delete assign', 'Operation set delete assign', 10),
	(53, 1, 'Operation set delete assign to', 'Operation set delete assign to', 10),
	(54, 1, 'Connector create policy class', 'Connector create policy class', 8),
	(55, 1, 'Connector delete policy class', 'Connector delete policy class', 8),
	(56, 1, 'Connector create user', 'Connector create user', 8),
	(57, 1, 'Connector delete user', 'Connector delete user', 8),
	(58, 1, 'Connector create user attribute', 'Connector create user attribute', 8),
	(59, 1, 'Connector delete user attribute', 'Connector delete user attribute', 8),
	(60, 1, 'Connector create object attribute', 'Connector create object attribute', 8),
	(61, 1, 'Connector delete object attribute', 'Connector delete object attribute', 8),
	(62, 1, 'Connector create object', 'Connector create object', 8),
	(63, 1, 'Connector create operation set', 'Connector create operation set', 8),
	(64, 1, 'Connector assign to', 'Connector assign to', 8),
	(65, 1, 'Connector delete assign to', 'Connector delete assign to', 8);
/*!40000 ALTER TABLE `operation` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.operation_set_details
CREATE TABLE IF NOT EXISTS `operation_set_details` (
  `operation_set_details_node_id` int(11) NOT NULL,
  `operation_id` int(11) NOT NULL,
  PRIMARY KEY (`operation_set_details_node_id`,`operation_id`),
  KEY `fk_op_set_operation_id_idx` (`operation_id`),
  CONSTRAINT `fk_op_set_operation_id` FOREIGN KEY (`operation_id`) REFERENCES `operation` (`operation_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_operation_set_details_node_id` FOREIGN KEY (`operation_set_details_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table contains the information for User operation node';

-- Dumping data for table pmwsdb.operation_set_details: ~0 rows (approximately)
/*!40000 ALTER TABLE `operation_set_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `operation_set_details` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.operation_type
CREATE TABLE IF NOT EXISTS `operation_type` (
  `operation_type_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`operation_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Operation types';

-- Dumping data for table pmwsdb.operation_type: ~2 rows (approximately)
/*!40000 ALTER TABLE `operation_type` DISABLE KEYS */;
INSERT INTO `operation_type` (`operation_type_id`, `name`) VALUES
	(1, 'Resource Operations'),
	(2, 'Admin Operations');
/*!40000 ALTER TABLE `operation_type` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.record_components
CREATE TABLE IF NOT EXISTS `record_components` (
  `record_node_id` int(11) NOT NULL,
  `record_component_id` int(11) NOT NULL,
  `order` int(11) DEFAULT NULL,
  PRIMARY KEY (`record_node_id`,`record_component_id`),
  KEY `fk_record_component_id_idx` (`record_component_id`),
  CONSTRAINT `fk_record_component_id` FOREIGN KEY (`record_component_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_record_node_id` FOREIGN KEY (`record_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table to store the components of a record';

-- Dumping data for table pmwsdb.record_components: ~0 rows (approximately)
/*!40000 ALTER TABLE `record_components` DISABLE KEYS */;
/*!40000 ALTER TABLE `record_components` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.record_key
CREATE TABLE IF NOT EXISTS `record_key` (
  `record_node_id` int(11) NOT NULL,
  `record_key` varchar(20) NOT NULL,
  `record_value` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`record_node_id`,`record_key`),
  CONSTRAINT `object_key_node_id` FOREIGN KEY (`record_node_id`) REFERENCES `object_detail` (`object_node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.record_key: ~0 rows (approximately)
/*!40000 ALTER TABLE `record_key` DISABLE KEYS */;
/*!40000 ALTER TABLE `record_key` ENABLE KEYS */;

-- Dumping structure for procedure pmwsdb.reset_data
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `reset_data`(session_id_in INT(11))
BEGIN

SET SQL_SAFE_UPDATES = 0;

delete from node where node_id > 0;
delete from session where session_id <> session_id_in;

SET SQL_SAFE_UPDATES = 1;
END//
DELIMITER ;

-- Dumping structure for table pmwsdb.session
CREATE TABLE IF NOT EXISTS `session` (
  `session_id` varchar(150) NOT NULL,
  `session_name` varchar(20) DEFAULT NULL,
  `user_node_id` int(11) NOT NULL,
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `host_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`session_id`),
  KEY `fk_session_user_node_id_idx` (`user_node_id`),
  KEY `idx_session_host_id` (`host_id`),
  CONSTRAINT `fk_session_user_node_id` FOREIGN KEY (`user_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores sessions created for users. This will be temperory data and rows will be deleted from this table depending on retention policy. ';

-- Dumping data for table pmwsdb.session: ~4 rows (approximately)
/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` (`session_id`, `session_name`, `user_node_id`, `start_time`, `host_id`) VALUES
	('6EB9B27474874ED8AF57E2712D552F64', NULL, 11004, '2018-08-06 16:09:12', NULL),
	('7D3507E9B3CB41E9B1E60E65E62218CD', NULL, 11004, '2018-08-06 16:02:39', NULL),
	('B630E73F8E3D467984E2F6563BBD83EF', NULL, 11004, '2018-08-06 15:41:53', NULL),
	('FF670ADE27174E4A8E2A2E2AE16E1088', NULL, 11004, '2018-08-06 15:57:31', NULL);
/*!40000 ALTER TABLE `session` ENABLE KEYS */;

-- Dumping structure for procedure pmwsdb.set_property
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `set_property`(property_in varchar(200), property_value_in varchar(200), node_id int)
BEGIN
DECLARE count int;

                SELECT count(*) INTO count FROM NODE_PROPERTY WHERE UPPER(property) = UPPER(property_in) and PROPERTY_NODE_ID = node_id;
    IF count > 0 THEN
      UPDATE NODE_PROPERTY P SET P.PROPERTY_VALUE = property_value_in WHERE P.PROPERTY_NODE_ID = node_id;
    ELSE
      INSERT INTO NODE_PROPERTY (PROPERTY, PROPERTY_VALUE, PROPERTY_NODE_ID) VALUES (property_in, property_value_in, node_id);
    END IF;
END//
DELIMITER ;

-- Dumping structure for function pmwsdb.SPLIT_STR
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `SPLIT_STR`(
  x VARCHAR(255),
  delim VARCHAR(12),
  pos INT
) RETURNS varchar(255) CHARSET utf8
RETURN REPLACE(SUBSTRING(SUBSTRING_INDEX(x, delim, pos),
       LENGTH(SUBSTRING_INDEX(x, delim, pos -1)) + 1),
       delim, '')//
DELIMITER ;

-- Dumping structure for table pmwsdb.template
CREATE TABLE IF NOT EXISTS `template` (
  `template_id` int(11) NOT NULL AUTO_INCREMENT,
  `template_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.template: ~0 rows (approximately)
/*!40000 ALTER TABLE `template` DISABLE KEYS */;
/*!40000 ALTER TABLE `template` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.template_component
CREATE TABLE IF NOT EXISTS `template_component` (
  `template_id` int(11) NOT NULL,
  `template_component_id` int(11) NOT NULL,
  `order` int(11) DEFAULT NULL,
  PRIMARY KEY (`template_id`,`template_component_id`),
  KEY `fk_cont_id_idx` (`template_component_id`),
  CONSTRAINT `fk_templ_cmpnt_id` FOREIGN KEY (`template_component_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_template_id` FOREIGN KEY (`template_id`) REFERENCES `template` (`template_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.template_component: ~0 rows (approximately)
/*!40000 ALTER TABLE `template_component` DISABLE KEYS */;
/*!40000 ALTER TABLE `template_component` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.template_key
CREATE TABLE IF NOT EXISTS `template_key` (
  `template_id` int(11) NOT NULL,
  `template_key` varchar(50) NOT NULL,
  PRIMARY KEY (`template_id`,`template_key`),
  KEY `fk_tpl_key_node_id_idx` (`template_key`),
  CONSTRAINT `fk_tpl_id` FOREIGN KEY (`template_id`) REFERENCES `template` (`template_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.template_key: ~0 rows (approximately)
/*!40000 ALTER TABLE `template_key` DISABLE KEYS */;
/*!40000 ALTER TABLE `template_key` ENABLE KEYS */;

-- Dumping structure for function pmwsdb.update_opset
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `update_opset`(ua_id_in int(11), oa_id_in int(11), operations varchar(1000)) RETURNS int(11)
BEGIN
DECLARE op_id int;
DECLARE opset_id_in int;
DECLARE op_list varchar(1000);
    -- Insert in node table
    IF EXISTS (SELECT opset_id FROM association WHERE ua_id_in = ua_id and oa_id = oa_id_in) THEN
      SELECT opset_id INTO opset_id_in FROM association WHERE ua_id_in = ua_id and oa_id = oa_id_in;
      DELETE FROM operation_set_details where operation_set_details_node_id = opset_id_in;
      -- Insert in operation_set_details table
      SELECT formatCSL(operations) INTO op_list;
      SET @separator = ',';
      SET @separatorLength = CHAR_LENGTH(@separator);

      WHILE operations != '' DO
            SET @currentValue = SUBSTRING_INDEX(operations, @separator, 1);
            SELECT get_operation_id(@currentValue) INTO op_id;
            INSERT INTO operation_set_details (operation_set_details_node_id, operation_id) VALUES (opset_id_in, op_id);
            SET operations = SUBSTRING(operations, CHAR_LENGTH(@currentValue) + @separatorLength + 1);
      END WHILE;
    END IF;
    return opset_id_in;
END//
DELIMITER ;

-- Dumping structure for table pmwsdb.user_detail
CREATE TABLE IF NOT EXISTS `user_detail` (
  `user_node_id` int(11) NOT NULL,
  `user_name` varchar(20) NOT NULL,
  `full_name` varchar(50) DEFAULT NULL,
  `password` varchar(1000) DEFAULT NULL,
  `email_address` varchar(254) DEFAULT NULL,
  `host_id` int(11) DEFAULT NULL,
  `pop_server` varchar(100) DEFAULT NULL,
  `smtp_server` varchar(100) DEFAULT NULL,
  `account_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`user_node_id`),
  UNIQUE KEY `user_name_UNIQUE` (`user_name`),
  KEY `fk_user_host_id_idx` (`host_id`),
  CONSTRAINT `fk_user_detail_node_id` FOREIGN KEY (`user_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`host_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='user - U';

-- Dumping data for table pmwsdb.user_detail: ~0 rows (approximately)
/*!40000 ALTER TABLE `user_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_detail` ENABLE KEYS */;

-- Dumping structure for view pmwsdb.acl_entry_view
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `acl_entry_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `acl_entry_view` AS select `n1`.`node_id` AS `node_id`,`get_node_name`(`n1`.`node_id`) AS `user`,`allowed_operations`(`n1`.`node_id`,`n2`.`obj_id`) AS `allowed_ops`,`n2`.`obj_id` AS `obj_id`,`get_node_name`(`n2`.`obj_id`) AS `obj_name` from (`node` `n1` join `object_view` `n2`) where ((`n1`.`node_type_id` in (3,4)) and (`allowed_operations`(`n1`.`node_id`,`n2`.`obj_id`) is not null));

-- Dumping structure for view pmwsdb.acl_view
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `acl_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `acl_view` AS select `acl_entry_view`.`obj_name` AS `obj_name`,group_concat(`acl_entry_view`.`user`,'-',`acl_entry_view`.`allowed_ops` separator ',') AS `group_concat(user,'-',allowed_ops)` from `acl_entry_view` group by `acl_entry_view`.`obj_name` order by `acl_entry_view`.`obj_name`;

-- Dumping structure for view pmwsdb.assignment_view
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `assignment_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `assignment_view` AS select `assignment`.`start_node_id` AS `start_node_id`,`GET_NODE_NAME`(`assignment`.`start_node_id`) AS `start_node_name`,`assignment`.`end_node_id` AS `end_node_id`,`GET_NODE_NAME`(`assignment`.`end_node_id`) AS `end_node_name`,`assignment`.`depth` AS `depth`,`assignment`.`assignment_path_id` AS `assignment_path_id` from `assignment` where ((`GET_NODE_TYPE`(`assignment`.`start_node_id`) <> 7) and (`GET_NODE_TYPE`(`assignment`.`end_node_id`) <> 7) and (`assignment`.`depth` > 0)) order by `assignment`.`assignment_path_id`,`assignment`.`depth`;

-- Dumping structure for view pmwsdb.association
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `association`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `association` AS select (select `b`.`end_node_id` from `assignment` `b` where ((`b`.`start_node_id` = `a`.`end_node_id`) and isnull(`b`.`assignment_path_id`) and (`b`.`depth` = 1) and (`GET_NODE_TYPE`(`b`.`start_node_id`) = 7))) AS `ua_id`,`a`.`end_node_id` AS `opset_id`,`a`.`start_node_id` AS `oa_id` from `assignment` `a` where (isnull(`a`.`assignment_path_id`) and (`a`.`depth` = 1) and (`GET_NODE_TYPE`(`a`.`end_node_id`) = 7));

-- Dumping structure for view pmwsdb.object_view
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `object_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `object_view` AS select `node`.`node_id` AS `obj_id` from `node` where (`node`.`node_type_id` in (5,6));

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
