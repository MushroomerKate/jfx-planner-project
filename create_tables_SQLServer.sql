-- Create the database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'planner')
BEGIN
    CREATE DATABASE [planner];
END
GO

USE [planner];
GO

-- Create the schema
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'planner')
BEGIN
    EXEC('CREATE SCHEMA [planner]');
END
GO

-- 1. Table: Notebooks
CREATE TABLE [planner].[Notebooks] (
    [id] INT NOT NULL PRIMARY KEY, -- Add IDENTITY(1,1) if you want auto-increment
    [current_date] DATE NULL
);

-- 2. Table: Item_types
CREATE TABLE [planner].[Item_types] (
    [type_id] NVARCHAR(20) NOT NULL PRIMARY KEY,
    [type_description] NVARCHAR(MAX) NULL
);

-- 3. Table: Items
CREATE TABLE [planner].[Items] (
    [id] INT NOT NULL PRIMARY KEY,
    [notebook_id] INT NOT NULL,
    [name] NVARCHAR(45) NOT NULL,
    [item_type] NVARCHAR(20) NOT NULL,
    CONSTRAINT [fk_Items_Notebooks] FOREIGN KEY ([notebook_id]) 
        REFERENCES [planner].[Notebooks] ([id]) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT [fk_Items_Item_types] FOREIGN KEY ([item_type]) 
        REFERENCES [planner].[Item_types] ([type_id]) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- 4. Table: Notes
CREATE TABLE [planner].[Notes] (
    [item_id] INT NOT NULL PRIMARY KEY,
    [text] NVARCHAR(MAX) NULL,
    CONSTRAINT [fk_Notes_Items] FOREIGN KEY ([item_id]) 
        REFERENCES [planner].[Items] ([id]) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 5. Table: Tasks
CREATE TABLE [planner].[Tasks] (
    [item_id] INT NOT NULL PRIMARY KEY,
    [parent_task_id] INT NULL,
    [deadline] DATE NULL,
    [is_complete] TINYINT NULL,
    CONSTRAINT [fk_Tasks_Items] FOREIGN KEY ([item_id]) 
        REFERENCES [planner].[Items] ([id]) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT [fk_Task_parent] FOREIGN KEY ([parent_task_id]) 
        REFERENCES [planner].[Tasks] ([item_id]) -- SQL Server does not allow ON DELETE CASCADE on self-referencing FKs
);