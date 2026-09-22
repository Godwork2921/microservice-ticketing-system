-- PostgreSQL creates this file only when the data volume is initialized.
-- Each service receives a separate logical database while sharing the local server.
SELECT format('CREATE DATABASE %I OWNER %I', database_name, current_user)
FROM (VALUES
    ('event_db'),
    ('venue_db'),
    ('reservation_db'),
    ('payment_db'),
    ('ticket_db'),
    ('notification_db'),
    ('analytics_db'),
    ('user_db'),
    ('keycloak_db')
) AS databases(database_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = databases.database_name
)\gexec
