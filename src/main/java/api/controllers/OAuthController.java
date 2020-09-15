package api.controllers;

import business.boundary.UserService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Profile;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.ws.rs.core.Response;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

@Path("/oauth")
public class OAuthController {

    @Inject
    UserService userService;

    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "C:\\Users\\user\\Desktop\\JEE\\uslugiMobilne\\client_secret.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @GET
    public Response authenticate() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the labels in the user's account.
        String user = "me";
        Profile profile = service.users().getProfile("me").execute();

        return Response.ok(profile.getEmailAddress()).build();
    }

   /* @POST
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
            AuthController au = new AuthController();

                if(currentUser == null)
                {
                    User user = new User(credentials.getLogin(),null,Arrays.asList(User.Roles.USER));
                    userService.saveUser(user);
                }
            return au.authenticateUser(credentials);
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }*/
}
