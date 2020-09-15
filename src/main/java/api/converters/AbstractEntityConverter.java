package api.converters;


import business.boundary.ProductService;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractEntityConverter<V> implements ParamConverterProvider {

    @Inject
    ProductService productService;

    Class<V> entityClass;

    private BiFunction<ProductService, UUID, V> retrieveFunction;

    private Function<V, UUID> idExtractor;

    AbstractEntityConverter(Class<V> entityClass, Function<V, UUID> idExtractor, BiFunction<ProductService, UUID, V> retrieveFunction) {
        this.entityClass = entityClass;
        this.retrieveFunction = retrieveFunction;
        this.idExtractor = idExtractor;
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType != entityClass) {
            return null;
        }

        return (ParamConverter<T>) new ParamConverter<V>() {
            @Override
            public V fromString(String value) {
                V entity = retrieveFunction.apply(productService, UUID.fromString(value));

                if (entity == null) {
                    throw new NotFoundException();
                }

                return entity;
            }

            @Override
            public String toString(V book) {
                return idExtractor.apply(book).toString();
            }
        };
    }
}

