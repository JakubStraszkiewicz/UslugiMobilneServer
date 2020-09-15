package api;

import business.boundary.UserService;
import business.entities.Permission;
import business.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String REALM = "example";
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    private List<String> roles;
    private String method;

    @Inject
    UserService userService;

    @PersistenceContext
    EntityManager em;

    @Context
    ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        this.method = info.getResourceMethod().getName();
        String token = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!isTokenBasedAuthentication(token)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        try {
            validateToken(token);
        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
            return;
        }

        try {
            checkPermission();
        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
        }
    }

    private void checkPermission() throws Exception {
        ArrayList<String> userPermissions = new ArrayList<>();

        for(String role : roles) {
            userPermissions.add(getPermission(method,role).getPermission());
        }

        if(userPermissions.contains(Permission.Level.GRANTED))
            return;
        else
            throw new Exception();
    }

    private Permission getPermission(String operation, String role) {

        TypedQuery<Permission> query =
                em.createNamedQuery(Permission.Queries.FIND_ONE,Permission.class);

        query.setParameter("operation", operation);
        query.setParameter("role", role);
        return query.getSingleResult();
    }


    private boolean isTokenBasedAuthentication(String authorizationHeader) {

        // Check if the Authorization header is valid
        // It must not be null and must be prefixed with "Bearer" plus a whitespace
        // The authentication scheme comparison must be case-insensitive
        return authorizationHeader != null;
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {

        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .build());
    }

    private void validateToken(String token) throws Exception {
        // Check if the token was issued by the server and if it's not expired
        // Throw an Exception if the token is invalid
        User user = userService.findUserByToken(token);

        if(user == null)
            throw new Exception();

        Claims claims = Jwts.parser()
                .setSigningKey(user.getPassword().getBytes("UTF-8"))
                .parseClaimsJws(token).getBody();

        List<String> tokens = user.getTokens();
        if(!tokens.contains(token))
            throw new Exception();

        this.roles = (List<String>)(claims.get("roles"));
    }
}
