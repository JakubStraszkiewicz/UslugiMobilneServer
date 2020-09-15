package api.converters;

import business.boundary.ProductService;
import business.entities.Product;

import javax.ws.rs.ext.Provider;

@Provider
public class ProductConverter extends AbstractEntityConverter<Product> {

    public ProductConverter() {
        super(Product.class, Product::getId, ProductService::findProduct);
    }
}
