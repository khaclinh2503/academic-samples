-- Philip Scuderi
-- Part 2

DROP TABLE IF EXISTS Purposes, Regions, PhoneNumbers, Persons, Organizations, PersonsToPhoneNumbers, PersonsToOrganizations, OrganizationsToPhoneNumbers;

CREATE TABLE Purposes
(
	PurposeID	serial PRIMARY KEY,
	PurposeDesc	varchar(64)
);

CREATE TABLE Regions
(
	RegionID	serial PRIMARY KEY,
	RegionDesc	varchar(64)
);

CREATE TABLE PhoneNumbers
(
	PhoneNumberID	serial PRIMARY KEY,
	PhoneNumber	varchar(32) NOT NULL,
	PurposeID	integer,
	UNIQUE(PhoneNumber),
	FOREIGN KEY(PurposeID) REFERENCES Purposes(PurposeID)
);

CREATE TABLE Persons
(
	PersonID	serial PRIMARY KEY,
	FirstName	varchar(64),
	LastName	varchar(64),
	Occupation	varchar(64)
);

CREATE TABLE Organizations
(
	OrganizationID		serial PRIMARY KEY,
	OrganizationName	varchar(128) NOT NULL,
	RegionID		integer,
	ContactEmployee		integer,
	FOREIGN KEY(RegionID) REFERENCES Regions(RegionID)
);

CREATE TABLE PersonsToPhoneNumbers
(
	PersonID	integer,
	PhoneNumberID	integer,
	PRIMARY KEY(PersonID, PhoneNumberID),
	FOREIGN KEY(PersonID) REFERENCES Persons(PersonID),
	FOREIGN KEY(PhoneNumberID) REFERENCES PhoneNumbers(PhoneNumberID)
);

CREATE TABLE PersonsToOrganizations
(
	PersonID	integer,
	OrganizationID	integer,
	BusinessTitle	varchar(64),
	PRIMARY KEY(PersonID, OrganizationID),
	FOREIGN KEY(PersonID) REFERENCES Persons(PersonID),
	FOREIGN KEY(OrganizationID) REFERENCES Organizations(OrganizationID)
);

CREATE TABLE OrganizationsToPhoneNumbers
(
	OrganizationID	integer,
	PhoneNumberID	integer,
	PRIMARY KEY(OrganizationID, PhoneNumberID),
	FOREIGN KEY(OrganizationID) REFERENCES Organizations(OrganizationID),
	FOREIGN KEY(PhoneNumberID) REFERENCES PhoneNumbers(PhoneNumberID)
);

ALTER TABLE Organizations ADD FOREIGN KEY(ContactEmployee, OrganizationID) REFERENCES PersonsToOrganizations(PersonID, OrganizationID);

