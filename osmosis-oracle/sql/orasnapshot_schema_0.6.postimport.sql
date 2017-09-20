
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
