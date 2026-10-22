package movieticket.dao;

import java.sql.SQLException;
import java.util.Vector;

/** Generic CRUD contract: Create, Read, Update, Delete. */
public interface CrudDao<T> {
    int insert(T item) throws SQLException;

    T findById(int id) throws SQLException;

    Vector<T> findAll() throws SQLException;

    boolean update(T item) throws SQLException;

    boolean delete(int id) throws SQLException;
}
