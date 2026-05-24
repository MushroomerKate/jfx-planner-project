-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema planner
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema planner
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `planner` DEFAULT CHARACTER SET utf8 ;
USE `planner` ;

-- -----------------------------------------------------
-- Table `planner`.`Notebooks`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `planner`.`Notebooks` (
  `id` INT NOT NULL,
  `current_date` DATE NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `planner`.`Item_types`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `planner`.`Item_types` (
  `type_id` VARCHAR(20) NOT NULL,
  `type_description` TEXT NULL,
  PRIMARY KEY (`type_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `planner`.`Items`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `planner`.`Items` (
  `id` INT NOT NULL,
  `notebook_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `item_type` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Items_Notebooks_idx` (`notebook_id` ASC) VISIBLE,
  INDEX `fk_Items_Item_types_idx` (`item_type` ASC) VISIBLE,
  CONSTRAINT `fk_Items_Notebooks`
    FOREIGN KEY (`notebook_id`)
    REFERENCES `planner`.`Notebooks` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_Items_Item_types`
    FOREIGN KEY (`item_type`)
    REFERENCES `planner`.`Item_types` (`type_id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `planner`.`Notes`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `planner`.`Notes` (
  `item_id` INT NOT NULL,
  `text` TEXT NULL,
  PRIMARY KEY (`item_id`),
  CONSTRAINT `fk_Notes_Items`
    FOREIGN KEY (`item_id`)
    REFERENCES `planner`.`Items` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `planner`.`Tasks`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `planner`.`Tasks` (
  `item_id` INT NOT NULL,
  `parent_task_id` INT NULL,
  `deadline` DATE NULL,
  `is_complete` TINYINT NULL,
  PRIMARY KEY (`item_id`),
  INDEX `fk_Task_parent_idx` (`parent_task_id` ASC) VISIBLE,
  CONSTRAINT `fk_Tasks_Items`
    FOREIGN KEY (`item_id`)
    REFERENCES `planner`.`Items` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_Task_parent`
    FOREIGN KEY (`parent_task_id`)
    REFERENCES `planner`.`Tasks` (`item_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
