import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 10L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Get instance of current session
        HttpSession session = request.getSession();

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get the most recent result page url
        String resultUrl = (String) session.getAttribute("resultUrl");
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

//        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        HashMap<String, HashMap<String,Double>> itemCart = (HashMap<String, HashMap<String,Double>>) session.getAttribute("itemCart");
//        if (previousItems == null) {
//            previousItems = new ArrayList<String>();
//        }
        if (itemCart == null) {
            itemCart = new HashMap<>();
        }

            try (Connection conn = dataSource.getConnection()) {

            // Log to localhost log
            request.getServletContext().log("getting " + itemCart.size() + " items");
            JsonArray previousItemsJsonArray = new JsonArray();
//            previousItems.forEach(previousItemsJsonArray::add);
//            for ( int i = 0; i < previousItems.size(); i++) {
            for(String movieId: itemCart.keySet()){
//                String movieId = itemCart.keySet();
                System.out.println(movieId);
                Statement statement = conn.createStatement();
                String query = String.join("",
                        "SELECT * ",
                        "FROM movies as m ",
                        "WHERE m.id = '", movieId, "';");
                System.out.println(query);
                ResultSet rs = statement.executeQuery(query);
                if(rs.next()) {
                    String movie_title = rs.getString("title");
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("movie_quantity", itemCart.get(movieId).get("quantity"));
                    jsonObject.addProperty("movie_price", itemCart.get(movieId).get("price"));
                    jsonObject.addProperty("resultUrl", resultUrl);
                    jsonArray.add(jsonObject);
                }
                rs.close();
                statement.close();
            }
            responseJsonObject.add("previousItems", jsonArray);

            // write all the data into the jsonObject
//            response.getWriter().write(responseJsonObject.toString());
            response.getWriter().write(jsonArray.toString());

        }catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("item");
        Double quantity = Double.valueOf(request.getParameter("quantity"));

        System.out.println(item + quantity);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        HashMap<String, HashMap<String,Double>> itemCart = (HashMap<String, HashMap<String,Double>>) session.getAttribute("itemCart");
        if (itemCart == null) {
            itemCart = new HashMap<>();
            HashMap<String, Double> detail = new HashMap<>();
            double price = Math.floor(Math.random() * (10000 - 100) + 100) / 100;
            detail.put("quantity", quantity);
            detail.put("price", price);
            itemCart.put(item, detail);
            session.setAttribute("itemCart", itemCart);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (itemCart) {
                if ( itemCart.containsKey(item) ) {
                    // increment quantity by 1
                    itemCart.get(item).put("quantity", itemCart.get(item).get("quantity") + quantity);
                    itemCart.get(item).put("price", itemCart.get(item).get("price"));
                }else {
                    HashMap<String, Double> detail = new HashMap<>();
                    detail.put("quantity", quantity);
                    double price = Math.floor(Math.random() * (10000 - 100) + 100) / 100;
                    detail.put("price", price);
                    itemCart.put(item, detail);
                }

            }
        }

//        JsonObject responseJsonObject = Json.createObjectBuilder(itemCart).build();

//        JsonArray previousItemsJsonArray = new JsonArray();
        ArrayList<Map.Entry<String,HashMap<String,Double>>> itemArray = new ArrayList<>(itemCart.entrySet());
        Gson gson = new Gson();
        String responseJsonObject = gson.toJson(itemArray);
//        System.out.println(responseJsonObject);
//        itemCart.forEach(previousItemsJsonArray::add);
//        responseJsonObject.add("previousItems", previousItemsJsonArray);

        response.getWriter().write(responseJsonObject);
    }
}
