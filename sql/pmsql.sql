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

-- Dumping data for table pmwsdb.assignment: ~3 rows (approximately)
/*!40000 ALTER TABLE `assignment` DISABLE KEYS */;
INSERT INTO `assignment` (`assignment_id`, `start_node_id`, `end_node_id`, `depth`, `assignment_path_id`) VALUES
	(2, -2, -3, 1, 2),
	(1, -1, -2, 1, 1),
	(3, -1, -3, 2, 2);
/*!40000 ALTER TABLE `assignment` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.assignment_path
CREATE TABLE IF NOT EXISTS `assignment_path` (
  `assignment_path_id` int(11) NOT NULL AUTO_INCREMENT,
  `assignment_node_id` int(11) NOT NULL,
  PRIMARY KEY (`assignment_path_id`),
  KEY `fk_assignment_node_id` (`assignment_node_id`),
  CONSTRAINT `fk_assignment_node_id` FOREIGN KEY (`assignment_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.assignment_path: ~8 rows (approximately)
/*!40000 ALTER TABLE `assignment_path` DISABLE KEYS */;
INSERT INTO `assignment_path` (`assignment_path_id`, `assignment_node_id`) VALUES
	(2, -3),
	(99, -3),
	(303, -3),
	(734, -3),
	(1, -2),
	(62, -2),
	(244, -2),
	(604, -2);
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
  IF node_id_in is null THEN
    INSERT INTO NODE (NODE_TYPE_ID, NAME) VALUES (node_type_id_in,node_name);
  ELSE
    INSERT INTO NODE (NODE_ID, NODE_TYPE_ID, NAME) VALUES (node_id_in, node_type_id_in,node_name);
  END IF;
  SELECT MAX(NODE_ID) INTO inserted_node_id FROM NODE;
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
  PRIMARY KEY (`email_node_id`),
  CONSTRAINT `email_object_id` FOREIGN KEY (`email_node_id`) REFERENCES `node` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table to hold information for emails.  sender, recipient, etc';

-- Dumping data for table pmwsdb.email_detail: ~0 rows (approximately)
/*!40000 ALTER TABLE `email_detail` DISABLE KEYS */;
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

-- Dumping data for table pmwsdb.node: ~3 rows (approximately)
/*!40000 ALTER TABLE `node` DISABLE KEYS */;
INSERT INTO `node` (`node_id`, `node_type_id`, `name`, `description`) VALUES
	(-3, 4, 'super', NULL),
	(-2, 3, 'super', NULL),
	(-1, 2, 'Super PC', NULL);
/*!40000 ALTER TABLE `node` ENABLE KEYS */;

-- Dumping structure for table pmwsdb.node_property
CREATE TABLE IF NOT EXISTS `node_property` (
  `property_node_id` int(11) NOT NULL DEFAULT '0',
  `property_key` varchar(50) NOT NULL,
  `property_value` varchar(300) NOT NULL,
  PRIMARY KEY (`property_node_id`,`property_key`),
  CONSTRAINT `fk_property_node_id` FOREIGN KEY (`property_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table pmwsdb.node_property: ~2 rows (approximately)
/*!40000 ALTER TABLE `node_property` DISABLE KEYS */;
INSERT INTO `node_property` (`property_node_id`, `property_key`, `property_value`) VALUES
	(-3, 'password', '100a7fc75da46aa90b44cf30e5175f976b3eac52e27c2e6cf78865cbed3a3a4b71172bb15329d7066a042af98287254f5d6e3f4fb7dbc4cbd8941885f7100c8f7a2c552edb8e3c9f68769720965d6b56a23'),
	(-2, 'namespace', 'super');
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

-- Dumping data for table pmwsdb.session: ~1 rows (approximately)
/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` (`session_id`, `session_name`, `user_node_id`, `start_time`, `host_id`) VALUES
	('572937EB02454B5B828044386B592E2B', NULL, -3, '2018-07-25 15:27:36', NULL);
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
