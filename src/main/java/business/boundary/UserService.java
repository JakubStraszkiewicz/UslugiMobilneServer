package business.boundary;

import business.entities.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Stateless
public class UserService {

    @PersistenceContext
    EntityManager em;

    public User findUser(String login) {
        return findUserByLogin(login);
    }

    @Transactional
    public User saveUser(User user) {
        if (user.getId() == null) {
            em.persist(user);
        } else {
            user = em.merge(user);
        }

        return user;
    }

    public void removeToken(String token) {
        User user = findUserByToken(token);
        List<String> tokens = user.getTokens();
        tokens.remove(token);
        user.setTokens(tokens);
        em.merge(user);
    }

    private User findUserByLogin(String login) {
        TypedQuery<User> query = em.createNamedQuery(User.Queries.FIND_BY_LOGIN, User.class);
        query.setParameter("login", login);
        return query.getSingleResult();
    }

    public User findUserByToken(String token) {
        TypedQuery<User> query = em.createNamedQuery(User.Queries.FIND_BY_TOKEN, User.class);
        query.setParameter("token", token);
        return query.getSingleResult();
    }

    public void addToken(String token, String login) {
        User user = findUserByLogin(login);

        if(user != null) {
            List<String> tokens = user.getTokens();
            tokens.add(token);
            user.setTokens(tokens);

            em.merge(user);
        }
    }
}

