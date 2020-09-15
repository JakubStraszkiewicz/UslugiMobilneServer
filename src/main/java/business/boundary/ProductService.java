package business.boundary;

import business.entities.Product;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
@PermitAll
public class ProductService implements Serializable {

    @PersistenceContext
    EntityManager em;

    public List<Product> findAllProducts() {
        return em.createNamedQuery(Product.Queries.FIND_ALL, Product.class).getResultList();
    }

    public Product findProduct(UUID id) {
        return em.find(Product.class, id);
    }

    @Transactional
    public void removeProduct(Product product) {
        product = em.merge(product);
        em.remove(product);
    }

    @Transactional
    public Product saveProduct(Product product) {
        if (product.getId() == null) {
            em.persist(product);
        } else {
            Product tmpProduct = findProduct(product.getId());
            if(tmpProduct == null)
                return null;
            product = em.merge(product);
        }

        return product;
    }

    public void showAllProducts() {
        List<Product> products = em.createNamedQuery(Product.Queries.FIND_ALL, Product.class).getResultList();

        System.out.println();
        System.out.println();
        for(Product product : products) {
            System.out.println(product.toString());
        }
        System.out.println();
        System.out.println();
    }
}
