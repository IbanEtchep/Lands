ALTER TABLE land_lands
    ADD COLUMN idL INT;

INSERT INTO land_lands (idL, name, type, owner_id, created_at)
SELECT idL,
       libelleL,
       (SELECT libelleTL FROM sc_land_types WHERE idTL = sc_lands.idTL),
       uuid,
       createdAt
FROM sc_lands;

INSERT INTO land_bans (land_id, uuid)
SELECT (SELECT id FROM land_lands WHERE idL = sc_lands_bans.idL), uuid
FROM sc_lands_bans;

INSERT INTO land_trusts (land_id, uuid, permission)
SELECT (SELECT id FROM land_lands WHERE idL = sc_trusts.idL),
       uuid,
       (SELECT libelleLP FROM sc_land_permissions WHERE idLP = sc_trusts.idLP) -- Assuming permission names match.
FROM sc_trusts;

INSERT INTO land_flags (land_id, flag)
SELECT (SELECT id FROM land_lands WHERE idL = sc_flags.idL),
       (SELECT libelleTF FROM sc_land_flags WHERE idTF = sc_flags.idTF) -- Assuming flag names match.
FROM sc_flags;

INSERT INTO land_links (land_id, linked_land_id, link_type)
SELECT ll1.id, ll2.id, sc_lands_links.LinkType
FROM sc_lands_links
         JOIN land_lands ll1 ON sc_lands_links.idL = ll1.idL
         JOIN land_lands ll2 ON sc_lands_links.idLW = ll2.idL;

INSERT INTO land_chunks (`server`, `world`, x, z, land_id, created_at)
SELECT `server`,
       `world`,
       x,
       z,
       (SELECT id FROM land_lands WHERE idL = sc_chunks.idL),
       createdAt
FROM sc_chunks
ON DUPLICATE KEY UPDATE land_id = land_id;

INSERT INTO land_world_default_land_lands (`server`, `world`, land_id, created_at)
SELECT `server`,
       `world`,
       (SELECT id FROM land_lands WHERE idL = sc_lands_world_default_lands.idL),
       createdAt
FROM sc_lands_world_default_lands;


ALTER TABLE land_lands
    DROP COLUMN idL;
