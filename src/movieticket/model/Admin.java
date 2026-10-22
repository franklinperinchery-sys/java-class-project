package movieticket.model;

import movieticket.util.AppLog;

/** Hierarchical inheritance: Admin and Customer both extend Person. */
public class Admin extends Person {

    public Admin(int id, String username, String fullName, String email, String phone) {
        super(id, username, fullName, email, phone);
        AppLog.log("2. Admin constructor (sub class)");
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
