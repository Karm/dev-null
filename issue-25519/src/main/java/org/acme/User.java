package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User extends PanacheEntity {
    public String username;

    public static void add(String username) {
        final User user = new User();
        user.username = username;
        user.persist();
    }

    @Override
    public String toString() {
        return username;
    }
}
