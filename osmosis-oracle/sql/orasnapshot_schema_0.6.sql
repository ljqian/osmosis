-- Database creation script for the snapshot Oracle schema.

-- Note: the database must support extended column size for Varchar2 type.

-- Drop all tables if they exist.
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE actions PURGE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE users PURGE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE nodes PURGE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE ways PURGE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE way_nodes PURGE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE relations PURGE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE relation_members PURGE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE schema_info PURGE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/


-- Drop all stored procedures if they exist.
-- DROP FUNCTION IF EXISTS osmosisUpdate();


-- Create a table which will contain a single row defining the current schema version.
CREATE TABLE schema_info (
    version integer NOT NULL
);


-- Create a table for users.
CREATE TABLE users (
    id int NOT NULL,
    name varchar2(4000) NOT NULL
);


-- Create a table for nodes.
CREATE TABLE nodes (
    id number(19) NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp NOT NULL,
    changeset_id number(19) NOT NULL,
    point sdo_geometry,
    tags varchar2 (32767)
    CONSTRAINT ensure_nodestags_json CHECK (tags IS JSON)
);

CREATE OR REPLACE TYPE OSM_NODEID_ARRAY AS VARRAY(1048576) OF number(19);
/

-- Create a table for ways.
CREATE TABLE ways (
    id number(19) NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp NOT NULL,
    changeset_id number(19) NOT NULL,
    linestring sdo_geometry,
    bbox sdo_geometry,
    tags varchar2 (32767) CONSTRAINT ensure_waystags_json CHECK (tags IS JSON),
    nodes OSM_NODEID_ARRAY
);


-- Create a table for representing way to node relationships.
CREATE TABLE way_nodes (
    way_id number(19) NOT NULL,
    node_id number(19) NOT NULL,
    sequence_id int NOT NULL
);


-- Create a table for relations.
CREATE TABLE relations (
    id number(19) NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp NOT NULL,
    changeset_id number(19) NOT NULL,
    tags varchar2 (32767)
    CONSTRAINT ensure_reltags_json CHECK (tags IS JSON)
);

-- Create a table for representing relation member relationships.
CREATE TABLE relation_members (
    relation_id number(19) NOT NULL,
    member_id number(19) NOT NULL,
    member_type char(1) NOT NULL,
    member_role clob NOT NULL,
    sequence_id int NOT NULL
);


-- Configure the schema version.
INSERT INTO schema_info (version) VALUES (6);


-- Add primary keys to tables.
ALTER TABLE schema_info ADD CONSTRAINT pk_schema_info PRIMARY KEY (version);

ALTER TABLE users ADD CONSTRAINT pk_users PRIMARY KEY (id);

ALTER TABLE nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id);

ALTER TABLE ways ADD CONSTRAINT pk_ways PRIMARY KEY (id);

ALTER TABLE way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id);

ALTER TABLE relations ADD CONSTRAINT pk_relations PRIMARY KEY (id);

ALTER TABLE relation_members ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, sequence_id);

commit;
/
