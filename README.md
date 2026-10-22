# Movie Ticket Booking System (Core Java + Swing + JDBC)

A desktop application where customers browse shows, pick seats on a seat map, pay and receive a ticket,
and an admin manages movies, shows and reports. Written in plain Java using only the topics of the syllabus.

## Why it fits the project guidelines
| Guideline | Where it is met |
|---|---|
| (i) Accepts considerable user input | Login, registration (6 fields), movie form (5), show form (5), seat selection, payment details, search box |
| (ii) Stores data permanently | SQLite database file `movieticket.db` (tables: users, movies, shows, bookings, booked_seats), accessed with JDBC |
| (iii) Processes user + stored data and displays output | Price calculation (class rates, loyalty discount, GST), seat availability from stored bookings, booking history, refunds, admin revenue report |

## Requirements
* JDK 11 or newer (`javac -version`)
* SQLite JDBC driver: download `sqlite-jdbc-3.x.x.jar` from https://github.com/xerial/sqlite-jdbc/releases
  and put it in the **lib** folder. (No database server needs to be installed.)

## Compile and run (command line)
Linux / macOS: `./compile.sh` then `./run.sh`
Windows: `compile.bat` then `run.bat`

Manual equivalent (shows the CLASSPATH idea):
```
javac -d out $(find src -name "*.java")
java -cp "out:lib/*" movieticket.Main          (Windows: -cp "out;lib/*")
```
**Command line arguments:** `--reset` recreates the database with demo data, `--debug` prints the
constructor calling order and other log lines.

**IDE (Eclipse / IntelliJ / NetBeans):** create a Java project, copy the `src` folder, add the jar from `lib`
to the build path / libraries, and run `movieticket.Main`.

## Demo logins
| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Customer | demo | demo123 |

## Project structure (packages)
```
movieticket
 |-- Main.java                     entry point, command line arguments
 |-- model      Person (abstract), Customer, PremiumCustomer, Admin, Discountable (interface),
 |              Movie, Show, Booking, SeatMap
 |-- exception  BookingException, SeatUnavailableException, PaymentFailedException,
 |              AuthenticationException (checked), InvalidInputException (unchecked)
 |-- util       DatabaseConnection (Singleton), Validator, PriceCalculator, PasswordUtil, DbUtil, AppLog
 |-- dao        CrudDao, UserRepository, BookingRepository (interfaces); UserDao, MovieDao, ShowDao, BookingDao (JDBC)
 |-- payment    PaymentProcessor (interface), CardPayment, UpiPayment, LegacyBankGateway, NetBankingAdapter (Adapter)
 |-- service    AuthService, BookingService
 |-- ui         LoginFrame, RegisterDialog, MainFrame, SeatSelectionDialog, AdminFrame, SimpleTableModel
```

## Syllabus coverage map
| Syllabus topic | Where to find it |
|---|---|
| Structure of a Java program, packages, naming conventions | every file; PascalCase classes, camelCase methods, UPPER_CASE constants, lowercase packages |
| Compiler, JVM, command line and IDE | README (compile/run), `compile.sh`, `run.sh` |
| Primitive and wrapper types, casting, autoboxing | `NetBankingAdapter` (double to int), `AdminFrame`/`MainFrame` table rows (autoboxing), `Integer.parseInt`, `Double.parseDouble` |
| Arrays (1-D and 2-D) | `SeatSelectionDialog.seatButtons[][]`, `PriceCalculator.calculate`, `DatabaseConnection.seedData` |
| Strings | `Validator`, `Booking.getSeatArray()`, `StringBuilder` receipt in `SeatSelectionDialog` |
| Vector class | used for every list: `Vector<Movie>`, `Vector<Show>`, `Vector<Booking>`, selected seats |
| Operators: arithmetic, bitwise, relational, logical, assignment, ternary | Bitwise: `SeatMap` (`\|`, `&`, `~`, `<<`, `>>>`) and `PasswordUtil` (`& 0xff`); ternary: `PriceCalculator.seatClass`, `Customer.getDiscountRate`; arithmetic: `Movie.getDurationText` (`/`, `%`); logical: `Validator` |
| Control statements | switch: `PriceCalculator.seatPrice`, `NetBankingAdapter`; for/while: `SeatMap.countBooked`, DAOs; jump statements: `break` in switch, `return` in loops |
| Functions, variable length arguments | `Validator.allFilled(String...)`, `DbUtil.close(AutoCloseable...)` |
| Command line arguments | `Main.main(String[] args)` handles `--reset`, `--debug` |
| Classes, abstract classes, interfaces | `Person` (abstract), `Discountable`, `PaymentProcessor`, `CrudDao`, `BookingRepository extends CrudDao` |
| Declaring objects, references, methods, constructors, access modifiers, `this` | all model classes (`this.id = id;`, `this(...)` constructor chaining, private/protected/public) |
| Method overloading, objects as parameters, returning objects | `PriceCalculator.calculate` (2 overloads), `BookingService.bookTickets(...)`, `loadSeatMap()` |
| Recursion | `PriceCalculator.sumPrices(double[], int)` |
| Static members, final variables, inner classes | `BookingService.sequence`, constants like `MAX_SEATS_PER_BOOKING`; non-static inner class `ReferenceGenerator`; static nested class `PriceCalculator.Breakdown`; anonymous inner classes in `ui` |
| Inheritance: super/sub class, types, `super`, `protected`, constructor order | Hierarchical: `Customer` and `Admin` extend `Person`; multilevel: `PremiumCustomer` extends `Customer`; run with `--debug` to see the constructor order |
| Overriding, dynamic dispatch, `final` with inheritance | `getRole()`, `describe()`, `getDiscountRate()`; `final` method `Person.getDisplayName()`, `final` class `PriceCalculator` |
| Packages, CLASSPATH, access protection, importing | seven packages, `-cp "out:lib/*"` in `run.sh` |
| Interfaces vs abstract classes | `Person` (state + partial code) vs `Discountable` (pure contract) |
| Exception handling | checked/unchecked, try/catch, multiple catch (`SeatSelectionDialog.PayListener`), nested try + finally + rethrow (`BookingService.bookTickets`), throw/throws, built-in (`SQLException`, `NumberFormatException`), 5 custom exceptions |
| Design patterns | **Singleton**: `DatabaseConnection`. **Adapter**: `NetBankingAdapter` adapts `LegacyBankGateway` to `PaymentProcessor` |
| SOLID | **S**: DAO / service / ui split. **O**: add a payment method by writing a new `PaymentProcessor`, no change to `BookingService`. **L**: `Customer`, `PremiumCustomer`, `Admin` can be used wherever `Person` is expected. **I**: tiny `Discountable` interface. **D**: `BookingService` and `AuthService` depend on interfaces (`BookingRepository`, `UserRepository`, `PaymentProcessor`) |
| Swing: JFrame, JLabel, buttons, JTextField, layouts, containers | `LoginFrame` (also JPasswordField), `MainFrame` (JTabbedPane, JTable), `SeatSelectionDialog` (JToggleButton, JRadioButton), `AdminFrame` (JComboBox); BorderLayout, GridLayout, FlowLayout |
| MVC | Model: `SimpleTableModel` and the model classes; View: JTable and panels; Controller: the listeners |
| Event handling, delegation event model | sources (buttons, tables, text fields), event classes (`ActionEvent`, `ItemEvent`, `KeyEvent`, `ListSelectionEvent`), listener interfaces (`ActionListener`, `ItemListener`, `ListSelectionListener`), adapter `KeyAdapter` |
| JDBC: steps, components, connection, SQL, ResultSet, CRUD | `DatabaseConnection` (driver, connection, DDL), DAOs (`PreparedStatement`, `ResultSet`, INSERT/SELECT/UPDATE/DELETE, joins, transaction with commit/rollback) |

## Theory topics (for viva / report)
**Procedural vs object oriented:** a procedural version of this program would be a set of functions
(`bookSeat()`, `calculatePrice()`) sharing global arrays of movies and bookings. In the OOP version the data and the
behaviour that belongs to it are together (`SeatMap` owns the seat bits and the methods to change them), so the data
is protected by encapsulation and new types (a new payment method, a new user type) are added without touching
existing code.

**Microservices:** this project is a *monolith* (one program, one database). In a microservices design the same
system would be split into small independently deployed services, for example User Service, Movie/Show Service,
Booking Service and Payment Service, each with its own database and talking over HTTP/REST. The packages here
(`dao`, `service`, `payment`) already separate those responsibilities, so they show where the cut lines would be.

**Four OOP pillars in this project:** *abstraction* (`Person`, `PaymentProcessor`), *encapsulation* (private fields
with getters), *inheritance* (`Person` -> `Customer` -> `PremiumCustomer`), *polymorphism* (`getRole()`,
`pay()`, `getDiscountRate()` behave according to the real object).

## Features
Customer: register / login, search shows, seat map with Silver / Gold / Platinum pricing, up to 6 seats per booking,
payment by card, UPI or net banking (simulated), loyalty discount (10% after 3 bookings, 20% after 10), GST,
booking history, cancellation with 90% refund.
Admin: add / update / delete movies, add / delete shows, all bookings, revenue summary, cancel any booking.
Safety: passwords stored as SHA-256 hashes, `UNIQUE(show_id, seat_label)` prevents double booking even if two people
choose the same seat, booking and seats are saved in one transaction.

## Notes
* Payments are simulated (validation only), no real money is processed.
* Deleting a movie or show that already has bookings is refused by the database (foreign key); cancel the bookings first.
* The database file `movieticket.db` is created in the folder you run the program from.
