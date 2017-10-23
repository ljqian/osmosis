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

CREATE OR REPLACE TYPE OSM_ID_ARRAY AS VARRAY(1048576) OF number(19);
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
    nodes OSM_ID_ARRAY
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
    member_type varchar2(20) NOT NULL,
    member_role varchar2(200),
    sequence_id int NOT NULL
);


-- Configure the schema version.
INSERT INTO schema_info (version) VALUES (6);


commit;
/
