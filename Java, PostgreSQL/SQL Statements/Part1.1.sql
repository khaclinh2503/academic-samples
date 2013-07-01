-- Philp Scuderi
-- Part 1, Question 1

DROP TABLE IF EXISTS EventTypes, RegionTypes, Users, Events;

CREATE TABLE EventTypes
(
	EventTypeID	serial PRIMARY KEY,
	EventTypeDesc	varchar(64)
);

CREATE TABLE RegionTypes
(
	RegionID	serial	PRIMARY KEY,
	RegionDesc	varchar(64)
);

CREATE TABLE Users
(
	UserID		char(25) PRIMARY KEY,
	RegionID	integer,
	FOREIGN KEY(RegionID) REFERENCES RegionTypes(RegionID)
);

CREATE TABLE Events
(
	EventID		serial PRIMARY KEY,
	UserID		char(25),
	EventTypeID	integer,
	EventTime	timestamp without time zone,
	UNIQUE (UserID, EventTypeID, EventTime),
	FOREIGN KEY(UserID) REFERENCES Users(UserID),
	FOREIGN KEY(EventTypeID) REFERENCES EventTypes(EventTypeID)
);

INSERT INTO EventTypes(EventTypeDesc) VALUES('impression');
INSERT INTO EventTypes(EventTypeDesc) VALUES('activity');

INSERT INTO RegionTypes(RegionID, RegionDesc) VALUES(1000, 'Northeast');
INSERT INTO RegionTypes(RegionID, RegionDesc) VALUES(2000, 'Northwest');
INSERT INTO RegionTypes(RegionID, RegionDesc) VALUES(3000, 'Southeast');
INSERT INTO RegionTypes(RegionID, RegionDesc) VALUES(4000, 'Southwest');
INSERT INTO RegionTypes(RegionID, RegionDesc) VALUES(5000, 'Middle of Nowhere');

