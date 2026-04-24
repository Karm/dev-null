package quarkus.orm.db1;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "db1entity")
public class DB1Entity extends PanacheEntity {
    public String field;
    public String employeeId;
    public String fullName;
    public String email;
    public String city;
    public String clearanceLevel;
    @Lob // database-native large object
    public byte[] profileImageJpeg;
}
