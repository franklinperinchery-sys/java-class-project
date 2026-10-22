-- Reference copy of the database schema (the program creates these tables automatically).
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    role TEXT NOT NULL CHECK (role IN ('ADMIN','CUSTOMER')));

CREATE TABLE IF NOT EXISTS movies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    genre TEXT,
    language TEXT,
    duration_min INTEGER NOT NULL,
    rating REAL DEFAULT 0);

CREATE TABLE IF NOT EXISTS shows (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    screen TEXT NOT NULL,
    show_date TEXT NOT NULL,
    show_time TEXT NOT NULL,
    price REAL NOT NULL);

CREATE TABLE IF NOT EXISTS bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    reference TEXT NOT NULL UNIQUE,
    user_id INTEGER NOT NULL REFERENCES users(id),
    show_id INTEGER NOT NULL REFERENCES shows(id),
    seats TEXT NOT NULL,
    total REAL NOT NULL,
    payment_method TEXT NOT NULL,
    status TEXT NOT NULL,
    booked_at TEXT DEFAULT CURRENT_TIMESTAMP);

-- UNIQUE(show_id, seat_label) makes double booking of a seat impossible.
CREATE TABLE IF NOT EXISTS booked_seats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id INTEGER NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    show_id INTEGER NOT NULL,
    seat_label TEXT NOT NULL,
    UNIQUE (show_id, seat_label));
