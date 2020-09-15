package business.entities;

import java.io.Serializable;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

@Entity
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Getter
@Setter
@NamedQueries({
        @NamedQuery(name = Product.Queries.FIND_ALL, query = "select p from Product p")
})
public class Product implements Serializable {

    public static class Queries {
        public static final String FIND_ALL = "PRODUCT_FIND_ALL";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "To pole jest wymagane")
    private String manufactureName;

    @NotNull(message = "To pole jest wymagane")
    private String modelName;

    @NotNull(message = "To pole jest wymagane")
    @Min(value = 0, message = "Wartość musi być dodatnia")
    private double prize;

    @NotNull(message = "To pole jest wymagane")
    @Min(value = 0, message = "Wartość musi być dodatnia")
    private int quantity;

    private String country = "";

    public Product(String manufactureName, String modelName, double prize, int quantity)
    {
        this.manufactureName = manufactureName;
        this.modelName = modelName;
        this.prize = prize;
        this.quantity = quantity;
    }

    public Product(String manufactureName, String modelName, double prize, int quantity, String country)
    {
        this.manufactureName = manufactureName;
        this.modelName = modelName;
        this.prize = prize;
        this.quantity = quantity;
        this.country = country;
    }

    @Override
    public String toString()
    {
        return "id: " + this.id + " " +
                "manufactureName: " + this.manufactureName + " " +
                "modelName: " + this.modelName + " " +
                "quantity: " + this.quantity + " " +
                "prize: " + this.prize;
    }
}
