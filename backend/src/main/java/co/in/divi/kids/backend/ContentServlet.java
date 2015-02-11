package co.in.divi.kids.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Indra on 1/1/2015.
 */
public class ContentServlet extends HttpServlet {
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
        resp.setContentType("text/json");
        resp.getWriter().println("");
        BufferedReader br;
        if (reqModel.groupId.equalsIgnoreCase("toddler1")) {
            br = new BufferedReader(new InputStreamReader(getServletContext().getResourceAsStream("/WEB-INF/toddler_content.json"), "UTF-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(getServletContext().getResourceAsStream("/WEB-INF/kg1_content.json"), "UTF-8"));
        }
        String line2;
        while ((line2 = br.readLine()) != null) {
            resp.getWriter().println(line2);
        }
        br.close();
    }

    public static class RequestModel {
        public String userId;
        public String groupId;
    }
}
