package co.in.divi.kids.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

import javax.servlet.http.*;

public class LoginServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Please use the form to POST to this url");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Logger.getLogger("Blah").log(Level.INFO, "test logging!");

        BufferedReader reader = req.getReader();
        StringBuffer res = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            res.append(line);
        }
        reader.close();
        Logger.getLogger("Blah").log(Level.INFO, res.toString());

        RequestModel reqModel = new Gson().fromJson(res.toString(), RequestModel.class);

        Key userEntityKey = KeyFactory.createKey("User", "google:" + reqModel.googleId);
        Date date = new Date();
        Entity userEntity = new Entity(userEntityKey);
        userEntity.setProperty("googleId", reqModel.googleId);
        userEntity.setProperty("deviceId", reqModel.deviceId);
        userEntity.setProperty("name", reqModel.name);
        userEntity.setProperty("email", reqModel.email);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(userEntity);

        resp.setContentType("text/json");
        resp.getWriter().println("{}");
    }

    public static class RequestModel {
        public String deviceId;

        public String googleId;
        public String name;
        public String email;
    }
}
