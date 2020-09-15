package api.controllers;

import business.boundary.UserService;
import business.entities.Credentials;
import business.entities.Token;
import business.entities.User;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;

import static api.UriUtils.uri;
import static javax.ws.rs.core.Response.created;

@Path("/authentication")
public class AuthController {

    @Inject
    UserService userService;

    @POST
    @Path("/oauth")
    public Response registry(Credentials credentials) throws GeneralSecurityException, IOException {

        final String CLIENT_ID = "378964306736-438740200e8t5bl6481dmppnbs175neb.apps.googleusercontent.com";

        Client client = ClientBuilder.newClient();
        String target = "https://www.googleapis.com/oauth2/v2/tokeninfo?accessToken=" + credentials.getToken();
        Response response = client
                .target(target)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String value = response.readEntity(String.class);

        boolean isTokenVerified = value.contains(CLIENT_ID);
        if(isTokenVerified)
        {
            User currentUser = null;
            try {
                currentUser = userService.findUser(credentials.getLogin());
            } catch(EJBException exception) {

            }

            if(currentUser == null)
            {
                User user = new User(credentials.getLogin(),credentials.getPassword(),Arrays.asList(User.Roles.USER));
                userService.saveUser(user);
            }
            return authenticateUser(credentials);
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/registry")
    public Response registryUser(Credentials credentials) {
        User user = new User();
        user.setLogin(credentials.getLogin());
        ArrayList<String> roles = new ArrayList<String>();
        roles.add(User.Roles.USER);
        user.setRoles(roles);

        userService.saveUser(user);

        return created(uri(AuthController.class, "getUser", user.getId())).build();
    }

    @DELETE
    public void logoutUser(@QueryParam("token") String token) {
        userService.removeToken(token);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticateUser(Credentials credentials) {

        try {

            // Authenticate the user using the credentials provided
            User authenticatedUser = authenticate(credentials);

            // Issue a token for the user
            String token = issueToken(authenticatedUser);

            Token obj = new Token();
            obj.token = token;
            // Return the token on the response
            return Response.ok(obj).build();

        } catch(NullPointerException exception){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private User authenticate(Credentials credentials) throws Exception {
        // Authenticate against a database, LDAP, file or whatever
        // Throw an Exception if the credentials are invalid
        User authenticatedUser = null;
        try{
            authenticatedUser = userService.findUser(credentials.getLogin());
        }catch(Exception e) {
            throw new NullPointerException();
        }

        if(credentials.getToken() != null || (authenticatedUser != null && authenticatedUser.getPassword().equals(credentials.getPassword())))
        {
            return authenticatedUser;
        }
        else
        {
            throw new NullPointerException();
        }

    }

    private String issueToken(User user) {
        // Issue a token (can be a random String persisted to a database or a JWT token)
        // The issued token must be associated to a user
        // Return the issued token

        String token = createJWT(UUID.randomUUID(),user,365*24*60*60*100);
        userService.addToken(token,user.getLogin());

        return token;
    }

    public static String createJWT(UUID id, User user, long ttlMillis) {

        //The JWT signature algorithm we will be using to sign the token

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret

        //Let's set the JWT Claims
        JwtBuilder builder = null;
        try {
            builder = Jwts.builder()
                    .setId(id.toString())
                    .setIssuedAt(now)
                    .claim("roles",user.getRoles())
                    .setIssuer(user.getLogin())
                    .signWith(    SignatureAlgorithm.HS256,user.getPassword().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //if it has been specified, let's add the expiration
        if (ttlMillis > 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }
}

