package movieticket.model;

import movieticket.util.AppLog;

/**
 * Abstract super class for every user of the system.
 * Demonstrates: abstraction, encapsulation, protected members, final method, constructor calling order.
 */
public abstract class Person {
    protected int id;
    protected String username;
    protected String fullName;
    protected String email;
    protected String phone;

    public Person(int id, String username, String fullName, String email, String phone) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        AppLog.log("1. Person constructor (super class) for " + username);
    }

    /** Every sub class must say what its role is (method overriding / dynamic dispatch). */
    public abstract String getRole();

    /** final: sub classes are NOT allowed to change how the display name is built. */
    public final String getDisplayName() {
        return fullName + " (" + getRole() + ")";
    }

    public String describe() {
        return "User: " + username + " | Email: " + email + " | Phone: " + phone;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
