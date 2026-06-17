-- V17__seed_timesheet_data.sql

-- ==========================================
-- 1. APPROVED LEAVE REQUESTS
-- ==========================================

-- Camille Roux (E0007) - Congés Payés (ANNUAL) du 8 au 10 Juin 2026
INSERT INTO leave_requests (id, start_date, end_date, type, status, reason, manager_comment, employee_profile_id)
VALUES (
    '87a2d48f-366a-4934-8c88-293e5069f201',
    '2026-06-08',
    '2026-06-10',
    'ANNUAL',
    'PROCESSED_HR',
    'Congés annuels d\'été',
    'Approuvé et traité en paie',
    (SELECT id FROM employee_profiles WHERE matricule = 'E0007')
);

-- Emma Petit (E0009) - Maladie (SICK) du 11 au 12 Juin 2026
INSERT INTO leave_requests (id, start_date, end_date, type, status, reason, manager_comment, employee_profile_id)
VALUES (
    'a9f1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c',
    '2026-06-11',
    '2026-06-12',
    'SICK',
    'PROCESSED_HR',
    'Arrêt maladie prescrit',
    'Validé par la DRH',
    (SELECT id FROM employee_profiles WHERE matricule = 'E0009')
);


-- ==========================================
-- 2. TIME TRACKINGS (PUNCHES)
-- ==========================================

DELIMITER //

CREATE PROCEDURE SeedTimeTrackings()
BEGIN
    DECLARE emp_id BIGINT;
    DECLARE day_idx INT;
    DECLARE cur_date DATE;
    DECLARE base_in TIMESTAMP;
    DECLARE base_out TIMESTAMP;

    -- Helper variables for matricules list
    -- E0001, E0002, E0003, E0004, E0006, E0007, E0008, E0009, E0010, E0011, E0012, E0013
    -- E0005 is processed separately for night shifts.

    -- Iterating through June 1st to June 15th, 2026
    SET day_idx = 1;
    WHILE day_idx <= 15 DO
        -- Construct the current date
        SET cur_date = DATE_ADD('2026-06-01', INTERVAL (day_idx - 1) DAY);

        -- Skip weekends (June 6-7, June 13-14)
        IF DAYOFWEEK(cur_date) <> 1 AND DAYOFWEEK(cur_date) <> 7 THEN

            -- E0001, E0002, E0003, E0004, E0012, E0013: STANDARD SHIFTS (08:00 - 16:00)
            -- E0001
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0001';
            IF emp_id IS NOT NULL THEN
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
            END IF;

            -- E0002
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0002';
            IF emp_id IS NOT NULL THEN
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
            END IF;

            -- E0003
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0003';
            IF emp_id IS NOT NULL THEN
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
            END IF;

            -- E0004
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0004';
            IF emp_id IS NOT NULL THEN
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
            END IF;

            -- E0012
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0012';
            IF emp_id IS NOT NULL THEN
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
            END IF;

            -- E0013
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0013';
            IF emp_id IS NOT NULL THEN
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
            END IF;


            -- E0006: OVERTIME SCENARIO (Clocking out late on Mon/Wed/Fri)
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0006';
            IF emp_id IS NOT NULL THEN
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                
                IF day_idx IN (1, 3, 5, 8, 10, 12, 15) THEN
                    -- Overtime check out
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 19:30:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                ELSE
                    -- Standard check out
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                END IF;
            END IF;


            -- E0008: OVERTIME SCENARIO (Clocking out late on Tue/Thu)
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0008';
            IF emp_id IS NOT NULL THEN
                INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                
                IF day_idx IN (2, 4, 9, 11) THEN
                    -- Late checkout 20:00
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 20:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                ELSE
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                END IF;
            END IF;


            -- E0007: PAID LEAVES (ANNUAL) - Works standard shifts on non-leave days
            IF cur_date NOT BETWEEN '2026-06-08' AND '2026-06-10' THEN
                SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0007';
                IF emp_id IS NOT NULL THEN
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                END IF;
            END IF;


            -- E0009: SICK LEAVES (SICK) - Works standard shifts on non-leave days
            IF cur_date NOT BETWEEN '2026-06-11' AND '2026-06-12' THEN
                SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0009';
                IF emp_id IS NOT NULL THEN
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                END IF;
            END IF;


            -- E0010: MISSING PUNCH SCENARIOS
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0010';
            IF emp_id IS NOT NULL THEN
                IF day_idx = 3 THEN
                    -- Missing Check-out (only CHECK_IN)
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                ELSEIF day_idx = 12 THEN
                    -- Missing Check-in (only CHECK_OUT)
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                ELSE
                    -- Standard shift
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                END IF;
            END IF;


            -- E0011: MISSING PUNCH SCENARIOS
            SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0011';
            IF emp_id IS NOT NULL THEN
                IF day_idx = 5 THEN
                    -- Missing Check-out on Friday (only CHECK_IN)
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                ELSE
                    -- Standard shift
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 08:00:00') AS DATETIME), 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
                    INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
                    VALUES (UUID(), CAST(CONCAT(cur_date, ' 16:00:00') AS DATETIME), 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
                END IF;
            END IF;

        END IF;

        SET day_idx = day_idx + 1;
    END WHILE;


    -- ==========================================
    -- E0005: NIGHT SHIFT SCENARIO (22:00 to 06:00)
    -- ==========================================
    SELECT id INTO emp_id FROM employee_profiles WHERE matricule = 'E0005';
    IF emp_id IS NOT NULL THEN
        -- Week 1 night shifts (Mon->Tue, Tue->Wed, Wed->Thu, Thu->Fri)
        -- June 1 22:00 -> June 2 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-01 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-02 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');

        -- June 2 22:00 -> June 3 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-02 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-03 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');

        -- June 3 22:00 -> June 4 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-03 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-04 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');

        -- June 4 22:00 -> June 5 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-04 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-05 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');

        -- Week 2 night shifts (Mon->Tue, Tue->Wed, Wed->Thu, Thu->Fri)
        -- June 8 22:00 -> June 9 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-08 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-09 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');

        -- June 9 22:00 -> June 10 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-09 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-10 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');

        -- June 10 22:00 -> June 11 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-10 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-11 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');

        -- June 11 22:00 -> June 12 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-11 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-12 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');

        -- June 15 22:00 -> June 16 06:00
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-15 22:00:00', 'CHECK_IN', 46.5802, 0.3404, emp_id, 'Normal');
        INSERT INTO time_trackings (id, timestamp, type, latitude, longitude, employee_profile_id, attendance_status)
        VALUES (UUID(), '2026-06-16 06:00:00', 'CHECK_OUT', 46.5802, 0.3404, emp_id, 'Normal');
    END IF;

END //

DELIMITER ;

-- Execute procedure and clean up
CALL SeedTimeTrackings();
DROP PROCEDURE SeedTimeTrackings;
