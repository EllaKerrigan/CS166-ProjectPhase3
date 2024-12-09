/* Replace the location to where you saved the data files*/
COPY Users
FROM '/home/csmajs/ekerr003/cs166_project_phase3/data'
WITH DELIMITER ',' CSV HEADER;

COPY Items
FROM '/home/csmajs/ekerr003/cs166_project_phase3/data'
WITH DELIMITER ',' CSV HEADER;

COPY Store
FROM '/home/csmajs/ekerr003/cs166_project_phase3/data'
WITH DELIMITER ',' CSV HEADER;

COPY FoodOrder
FROM '/home/csmajs/ekerr003/cs166_project_phase3/data'
WITH DELIMITER ',' CSV HEADER;

COPY ItemsInOrder
FROM '/home/csmajs/ekerr003/cs166_project_phase3/data'
WITH DELIMITER ',' CSV HEADER;
