-- Migration V14: Add attendance_status to time_trackings
ALTER TABLE time_trackings ADD COLUMN attendance_status VARCHAR(50) NULL;
