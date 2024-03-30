-- User: admin Password: admin
INSERT INTO mascompetition.user (id, email, hashed_password)
VALUES ('478c1e22-fd05-429b-9787-d7db67efa2d1', 'admin',
        '$2a$10$7hZKRE1GM3dLBw23sIl4qOqb6Ze8OijNGqzHFBs3kfdGQKHOJ4AeC');

-- User: john Password: admin
INSERT INTO mascompetition.user (id, email, hashed_password)
VALUES ('bcee1438-46f4-4420-a1b3-4cecb773c2a8', 'john',
        '$2a$10$7hZKRE1GM3dLBw23sIl4qOqb6Ze8OijNGqzHFBs3kfdGQKHOJ4AeC');

INSERT INTO mascompetition.team (id, name)
VALUES ('f93045bd-b8de-42ca-8196-b51efac3e85e', 'Default Team');

UPDATE mascompetition.user
SET team_id='f93045bd-b8de-42ca-8196-b51efac3e85e'
WHERE ISNULL(team_id);

INSERT INTO mascompetition.authority (id, role)
VALUES ('0e584495-133f-44d3-a883-63f1cc775045', 'ROLE_ADMIN');

INSERT INTO mascompetition.user_authority(user_id, authority_id)
VALUES ('478c1e22-fd05-429b-9787-d7db67efa2d1', '0e584495-133f-44d3-a883-63f1cc775045');

