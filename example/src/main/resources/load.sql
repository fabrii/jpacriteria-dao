INSERT INTO INSTITUTIONS(name) VALUES ('Inst 1');
INSERT INTO INSTITUTIONS(name) VALUES ('Inst 2');

INSERT INTO EXECUTING_UNITS(institution_id, name) VALUES (1, 'EU 1 (inst 1)');
INSERT INTO EXECUTING_UNITS(institution_id, name) VALUES (1, 'EU 2 (inst 1)');
INSERT INTO EXECUTING_UNITS(institution_id, name) VALUES (2, 'EU 3 (inst 2)');

INSERT INTO EXAMPLE_ENTITIES(executing_unit_id, name) VALUES (1, 'Example entity 1');
INSERT INTO EXAMPLE_ENTITIES(executing_unit_id, name) VALUES (1, 'Example entity 2');
INSERT INTO EXAMPLE_ENTITIES(executing_unit_id, name) VALUES (1, 'Example entity 3');
INSERT INTO EXAMPLE_ENTITIES(executing_unit_id, name) VALUES (2, 'Example entity 4');
INSERT INTO EXAMPLE_ENTITIES(executing_unit_id, name) VALUES (3, 'Example entity 5');
