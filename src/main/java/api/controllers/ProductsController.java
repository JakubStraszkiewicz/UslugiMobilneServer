package api.controllers;

import api.Secured;
import business.boundary.ProductService;
import business.entities.Operation;
import business.entities.Product;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static api.UriUtils.uri;
import static javax.ws.rs.core.Response.*;

@Path("/products")
@Secured
public class ProductsController {

    @Inject
    ProductService productService;

    @GET
    public Collection<Product> getAllProducts() {
        Collection<Product> products = productService.findAllProducts();
        return products;
    }

    @POST
    public Response saveProduct(Product product) {
        Product tmpProduct = productService.saveProduct(product);
        if(tmpProduct == null)
            return Response.noContent().entity(product).tag(Operation.Operations.UPDATE).build();
        return created(uri(ProductsController.class, "getProduct", product.getId())).build();
    }

    @GET
    @Path("/{product}")
    public Product getProduct(@PathParam("product") Product product) {
        return product;
    }

    @DELETE
    @Path("/{product}")
    public Response deleteProduct(@PathParam("product") Product product) {
        productService.removeProduct(product);
        return ok().build();
    }

    @PUT
    @Path("/{product}/increase")
    public Response increaseQuantity(@PathParam("product") Product product,@QueryParam("difference") int difference)
    {
        if(product != null) {
            Product tmpProduct = productService.findProduct(product.getId());

            if(tmpProduct == null)
                return Response.noContent().entity(product).tag(Operation.Operations.INCREASE).build();

            product = tmpProduct;
            int actualQuantity = product.getQuantity();
            product.setQuantity(actualQuantity + difference);
        } else {
            return status(Response.Status.BAD_REQUEST).build();
        }

        productService.saveProduct(product);
        return ok().build();
    }

    @PUT
    @Path("/{product}/decrease")
    public Response decreaseQuantity(@PathParam("product") Product product,@QueryParam("difference") int difference)
    {
        if(product != null) {
            Product tmpProduct = productService.findProduct(product.getId());

            if(tmpProduct == null)
                return Response.noContent().entity(product).tag(Operation.Operations.DECREASE).build();

            product = tmpProduct;
            int actualQuantity = product.getQuantity();
            if(actualQuantity - difference >= 0)
                product.setQuantity(actualQuantity - difference);
            else
                return Response.notModified().entity(product).tag(Operation.Operations.DECREASE).build();
        } else {
            return status(Response.Status.BAD_REQUEST).tag(Operation.Operations.DECREASE).build();
        }

        productService.saveProduct(product);
        return ok().build();
    }

    @PUT
    @Path("/{product}")
    public Response updateProduct(@PathParam("product") Product originalProduct, Product updatedProduct) {
        if (!originalProduct.getId().equals(updatedProduct.getId())) {
            return status(Response.Status.BAD_REQUEST).build();
        }

        productService.saveProduct(updatedProduct);
        return ok().build();
    }

    @PUT
    @Path("/buy")
    public ArrayList<Response> buyProducts(ArrayList<Product> cart) {
        ArrayList<Response> responses = new ArrayList<>();
        boolean isPossible = true;

        for (Product cartProduct : cart) {
            Product product  = productService.findProduct(cartProduct.getId());

            if(product == null) {
                responses.add(Response.noContent().entity(cartProduct).tag(Operation.Operations.INCREASE).build());
                isPossible = false;
            }
        }

        if(isPossible)
        {
            responses.clear();
            for (Product cartProduct : cart)
                responses.add(increaseQuantity(cartProduct, cartProduct.getQuantity()));
        }


        return responses;
    }

    @PUT
    @Path("/sell")
    public ArrayList<Response> sellProducts(ArrayList<Product> cart) {
        ArrayList<Response> responses = new ArrayList<>();
        boolean isPossible = true;

        for (Product cartProduct : cart) {
            Product product  = productService.findProduct(cartProduct.getId());

            if(product == null) {
                responses.add(Response.noContent().entity(cartProduct).tag(Operation.Operations.INCREASE).build());
                isPossible = false;
            }
            else
            {
                int actualQuantity = product.getQuantity();
                if(actualQuantity - cartProduct.getQuantity() < 0) {
                    responses.add(Response.notModified().entity(product).tag(Operation.Operations.INCREASE).build());
                    isPossible = false;
                }
            }
        }

        if(isPossible)
        {
            responses.clear();
            for (Product cartProduct : cart)
                responses.add(decreaseQuantity(cartProduct, cartProduct.getQuantity()));
        }


        return responses;
    }


    @POST
    @Path("/synchronize")
    public ArrayList<Response> synchronizeProducts(ArrayList<Operation> operations) {

        ArrayList<Response> responses = new ArrayList<>();
        ArrayList<Operation> operationToRetry = new ArrayList<>();
        Response response = null;

        for(Operation operation : operations) {
            UUID tmpId = null;
            if(operation.getName().equals(Operation.Operations.SAVE)) {
                tmpId = operation.getProduct().getId();
                response = synchronize(operation);

                for(Operation tmpOperation : operations) {
                    if(tmpOperation.getProduct().getId().equals(tmpId))
                        tmpOperation.getProduct().setId(operation.getProduct().getId());
                }
            }else{
                response = synchronize(operation);
            }

            if(response.getStatusInfo() == Status.OK || response.getStatusInfo() == Status.CREATED)
                responses.add(response);
            else
                operationToRetry.add(operation);
        }

        Collections.sort(operationToRetry);

        for(Operation operation : operationToRetry) {
            response = synchronize(operation);

            responses.add(response);
        }

        productService.showAllProducts();

        return responses;
    }

    private Response synchronize(Operation operation) {
        Response response = null;
        switch (operation.getName()){
            case Operation.Operations.REMOVE:
                response = this.deleteProduct(operation.getProduct());
                break;
            case Operation.Operations.SAVE:
                operation.getProduct().setId(null);
                response = this.saveProduct(operation.getProduct());
                break;
            case Operation.Operations.UPDATE:
                response = this.saveProduct(operation.getProduct());
                break;
            case Operation.Operations.INCREASE:
                response = this.increaseQuantity(operation.getProduct(),operation.getDifference());
                break;
            case Operation.Operations.DECREASE:
                response = this.decreaseQuantity(operation.getProduct(),operation.getDifference());
                break;
        }
        return response;
    }
}

