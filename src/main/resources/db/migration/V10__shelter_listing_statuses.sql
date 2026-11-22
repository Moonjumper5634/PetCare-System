-- The local XAMPP/MariaDB installation reports the MySQL 5.5 protocol and does not
-- consistently enforce CHECK constraints. ShelterPetService validates this value.
-- PostgreSQL receives the enforced constraint in its corresponding migration.
SELECT 1;
