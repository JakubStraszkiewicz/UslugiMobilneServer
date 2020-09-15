package business.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.UUID;

@Entity
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Getter
@Setter
@NamedQueries({
        @NamedQuery(name = Permission.Queries.FIND_ONE, query = "select p from Permission p where p.operation = :operation and p.role = :role")
})
public class Permission implements Serializable {

    public static class Queries {
        public static final String FIND_ONE = "PERMISSION_FIND_ONE";
    }

    public static class Level {
        public static final String GRANTED = "GRANTED";
        public static final String DENIED = "DENIED";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    private String role;

    @NotBlank
    private String operation;

    @NotBlank
    private String permission;

    public Permission(String role, String operation, String permission) {
        this.role= role;
        this.operation = operation;
        this.permission = permission;
    }
}
