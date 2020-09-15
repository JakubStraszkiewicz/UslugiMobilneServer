package business.control;

import business.entities.Permission;
import business.entities.Product;
import business.entities.User;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.Server;

import static java.util.Arrays.asList;

@ApplicationScoped
public class InitialFixture {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) throws SQLException {

        Product product1 = new Product("Samsung","Galaxy S9",4000.99, 10);
        Product product2 = new Product("Samsung","Galaxy S7",2000.99, 3);
        Product product3 = new Product("Samsung","Galaxy S7",2000.99, 3, "Poland");


        em.persist(product1);
        em.persist(product2);
        em.persist(product3);

        ArrayList<String> roles1 = new ArrayList<String>();
        roles1.add(User.Roles.ADMIN);

        ArrayList<String> roles2 = new ArrayList<String>();
        roles2.add(User.Roles.USER);

        ArrayList<String> roles3 = new ArrayList<String>();
        roles3.add(User.Roles.USER);
        roles3.add(User.Roles.ADMIN);

        User user1 = new User("Admin","Admin", roles1);
        User user2 = new User("User","User", roles2);
        User user3 = new User("User2","User2", roles3);


        em.persist(user1);
        em.persist(user2);
        em.persist(user3);

        List<Permission> permissions = asList(
                new Permission(User.Roles.USER,"getAllProducts", Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "getAllProducts", Permission.Level.GRANTED),
                new Permission(User.Roles.USER,"saveProduct",Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "saveProduct", Permission.Level.GRANTED),

                new Permission(User.Roles.USER,"getProduct", Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "getProduct", Permission.Level.GRANTED),
                new Permission(User.Roles.USER,"deleteProduct",Permission.Level.DENIED),
                new Permission(User.Roles.ADMIN, "deleteProduct", Permission.Level.GRANTED),

                new Permission(User.Roles.USER,"increaseQuantity", Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "increaseQuantity", Permission.Level.GRANTED),
                new Permission(User.Roles.USER,"updateProduct",Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "updateProduct", Permission.Level.GRANTED),
                new Permission(User.Roles.USER,"decreaseQuantity",Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "decreaseQuantity",Permission.Level.GRANTED),
                new Permission(User.Roles.USER,"synchronizeProducts",Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "synchronizeProducts",Permission.Level.GRANTED),
                new Permission(User.Roles.USER,"buyProducts",Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "buyProducts",Permission.Level.GRANTED),
                new Permission(User.Roles.USER,"sellProducts",Permission.Level.GRANTED),
                new Permission(User.Roles.ADMIN, "sellProducts",Permission.Level.GRANTED)
                );

        permissions.forEach(permission -> em.persist(permission));
        em.flush();


    }
}
