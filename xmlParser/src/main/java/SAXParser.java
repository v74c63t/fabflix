import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SAXParser extends DefaultHandler {
    HashMap<String, Movies> myMovies;

    private String tempVal;

    private String director;

    private Movie tempMovie;
    private Star tempStar;
    // private Stars_in_Movies tempSIM;

    private int movieDupe = 0;
    private int starDupe = 0;

    private int movieInconsistent = 0;
    private int moviesNotFound = 0;

    private int starsNotFound = 0;

    private boolean isConsistent = true;

    private boolean isDuplicate = false;

    private int availableGenreId;

    private int availableStarId

    private HashMap<String, String> catToGenreMap = new HashMap<String, String>() {{
        put("susp", "Thriller");
        put("cnr", "Crime");
        put("cnrb", "Crime");
        put("dram", "Drama");
        put("west", "Western");
        put("myst", "Mystery");
        put("s.f.", "Sci-Fi");
        put("scfi", "Sci-Fi");
        put("advt", "Adventure");
        put("horr", "Horror");
        put("romt", "Romance");
        put("comd", "Comedy");
        put("romt comd", "Romantic Comedy");
        put("musc", "Musical");
        put("stage musical", "Musical");
        put("docu", "Documentary");
        put("porn", "Adult");
        put("noir", "Noir");
        put("biop", "Biography");
        put("bio", "Biography");
        put("tv", "TV Show");
        put("tvs", "TV Series");
        put("tvm", "TV Miniseries");
        put("actn", "Action");
        put("cart", "Cartoon");
        put("crim", "Crime");
        put("faml", "Family");
        put("fant", "Fantasy");
        put("hist", "History");
    }};

    private HashMap<String, int> existingGenres = new HashMap<String, int>();
    private HashMap<String, int> newGenres = new HashMap<String, int>();

    private DataSource dataSource;

    try {
        dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
    } catch (NamingException e) {
        e.printStackTrace();
    }

    try (Connection conn = dataSource.getConnection()) {

        // Construct a query with parameter represented by g"?"
        String query = "SELECT * FROM genres;";

        Statement statement = conn.createStatement();

        ResultSet rs = statement.executeQuery(query);

        while(rs.next()) {
            existingGenres.put(rs.getString("name"), rs.getInt("id"));
        }
        rs.close();

        query = "SELECT * FROM availableint;";

        ResultSet rs2 = statement.executeQuery(query);

        if (rs2.next()) {
            availableGenreId = rs2.getInt("genre");
            availableStarId = rs2.getInt("star");
        }
        rs2.close();


        statement.close();
    } catch (Exception e) {
        e.printStackTrace();
    }

    public SAXParser() {
        myMovies = new ArrayList<Movie>();
    }

    public void runExample() {
        parseDocument();
        printData(); // write to csv file
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("/xmlParser/stanford-movies/mains243.xml", this);
            sp.parse("/xmlParser/stanford-movies/actors63.xml", this);
            sp.parse("/xmlParser/stanford-movies/casts243.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {
        // depending on when we load csv we may want diff print functions for each file
        // if we load after we finish each file might need diff functions
        // if we load after all files are done parsing can just keep one

        // also need ot print dupes/inconsistencies
        // in this set write to csv file i guess
        Iterator<Employee> it = myMovies.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
        // then load data
        System.out.println("No of Inserted Movies: " + myMovies.size());
        System.out.println("No of Inserted Genres: " + newGenres.size());
        System.out.println("No of Records Inserted into Genres_in_Movies: " + myMovies.size());
        System.out.println("No of Duplicated Movies: " + movieDupe);
        System.out.println("No of Movie Inconsistencies: " + movieInconsistent);
        System.out.println("No of Inserted Stars: " + myStars.size());
        System.out.println("No of Duplicated Stars: " + starDupe);
        System.out.println("No of Records Inserted into Stars_in_Movies: " + myEmpls.size());
        System.out.println("No of Missing Movies: " + moviesNotFound);
        System.out.println("No of Missing Stars: " + starsNotFound);
    }

    public void writeToTextFile( String fileName, String content ) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write(content);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Since the Mains/Movie object holds an array of genres
        // we can probably remove "fileName" from paramter
        // and write to a preset file after check the object type
            // Movie -
                // movies.csv
            // genres
                // genres.csv --> update availableInt procedure
                // ? get availableint and make hashmap from genreName -> id ?
            // genres_in_movies
                // for each Movie,
                    // genreId lookup via name
                    // add Movie.getId(), genreId
            // Genre Hashmaps
                // for existing genres in db -> genre name: genre id
                // for new genres found while parsing -> genre name: genre id
                // for genres in cat -> genre abbr: actual genre defined by cats def page or corresponding equivalent in db
                    // first get actual genre here then check against existing and new genres
                    // if no equivalent found here and fails above check just add as is to new genres
    public void movieToCSV( String fileName, ArrayList<Movie> movieArray ) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            // should write a header
            writer.write("id");
            writer.write(",");
            writer.write("title");
            writer.write(",");
            writer.write("year");
            writer.write(",");
            writer.write("director");
            writer.newLine();
            for (Movie movie : movieArray) { // will prob have to change so it writes properly to csv file currently doesn't
                writer.write(movie.getId());
                writer.write(",");
                writer.write(movie.getTitle());
                writer.write(",");
                writer.write(movie.getYear());
                writer.write(",");
                writer.write(movie.getDirector());
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    public void genreToCSV( String fileName, HashMap<String, int> genreMap ) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write("id");
            writer.write(",");
            writer.write("name");
            writer.newLine();
            for (Map.entry<String, int> entry : genreMap.entrySet()) { // will prob have to change so it writes properly to csv file currently doesnt
                writer.write(Integer.toString(entry.getValue()));
                writer.write(",");
                writer.write(entry.getKey());
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        // not sure what to put here for mains
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of employee
            // figure out how to store everything in hashmap i guess
            tempMovie = new Movie();
        } else if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of employee
            // figure out how to store everything in hashmap i guess
            tempStar = new Star();
        } else if (qName.equalsIgnoreCase("m")) { // not too sure
            //create a new instance of employee
            // figure out how to store everything in hashmap i guess
            tempSIM = new Stars_In_Movies();
        }

        // Think

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if ( qName.equalsIgnoreCase("dirname")) {
            // Check for inconsistency ( "Unknown" / "" )
            if ( tempVal.toLowerCase().strip().startsWith("unknown") ) {
                isConsistent = false;
            } else if (tempVal.strip() == "") {
                isConsistent = false;
            } else { // Set director variable
                director = tempVal.strip();
            }

        } else if (qName.equalsIgnoreCase("directorfilms")) { // checks for closing element for <directorfilms>
            director = null; //reset director name

        } else if (qName.equalsIgnoreCase("film")) {
            tempMovie.setDirector(director);
            if( !isConsistent ) { // If data is inconsistent
                movieInconsistent++;
                writeToTextFile("/xmlParser/MovieInconsistent.txt", tempMovie.toString());
                isConsistent = true;
            } else if(isDuplicate) {
                movieDupe++;
                writeToTextFile("/xmlParser/MovieDuplicate.txt", tempMovie.toString());
                isDuplicate = false;
            }else {
                if(myMovies.getGenres.size() == 0) {
                    // since some dont have <cat> tag only have empty <cats>
                    // not sure if it will actually hit the null check if the tag doesnt exist
                    // so check here to be safe
                    movieInconsistent++;
                    writeToTextFile("/xmlParser/MovieInconsistent.txt", tempMovie.toString());
                }
                else if (director == null) {
                    movieInconsistent++;
                    writeToTextFile("/xmlParser/MovieInconsistent.txt", tempMovie.toString());
                }
                else { // Add to myMovies HashMap
                    myMovies.put(tempMovie.getId(), tempMovie);
                }
            }

        } else if (qName.equalsIgnoreCase("fid")) {

            if( myMovies.containsKey(tempVal.strip()) ){ // check if dupe
                isDuplicate = true;
            }
            tempMovie.setId(tempVal.strip()); // add id to current tempMovie

        } else if (qName.equalsIgnoreCase("t")) {
            // store for dupe checking
            if(tempVal.strip() == "") { // i mean title is required not to be null so this should be a fair assumption?
                isConsistent = false;
            }
            else if(tempVal == null) {
                isConsistent = false;
            }
            else {
                // same as above, probably not needed since we need the title (<t>) to add to dup file

                // add to tempMovie
                 tempMovie.setTitle(tempVal.strip());
            }

        }else if (qName.equalsIgnoreCase("year")) {
            // there are some with invalid ints ex: 199x, 19yy, etc
            // dk how to deal with these b/c we cant set as null since tables require them to be not null
            // maybe report as inconsistent????
            // store for dupe checking
            if(tempVal.strip() == "") { // i mean year is required not to be null so this should be a fair assumption?
                isConsistent = false;
            }
            else if(tempVal == null) {
                isConsistent = false;
            }
            else {
                try (Integer.parseInt(tempVal.strip())) {
                    // add to tempMovie
                } catch (Exception e) {
                    // report inconsistent i guess
                    isConsistent = false;
                }
                tempMovie.setYear(tempVal.strip())
            }
//            tempEmp.setName(tempVal);
        } else if(qName.equalsIgnoreCase("cat")) {
            // need to do substring matching to check for if exists in db
            // need to use .lower() b/c 'dram', 'DRam', etc
            // also may need to combine similar genres together ex: adult
            // store in list?
            // if empty report inconsistent
            // some have trailing spaces so use .strip()
            // some are combined into one tag ex: 'Romt Comd', 'Dram Docu'
            // use .strip() then .split()
            // refer to http://infolab.stanford.edu/pub/movies/doc.html#CATS
            // to figure out what each cat stands for
            // maybe create a map for this as well?
            // Susp -> Thriller
            // CnR -> Cops and Robbers -> Crime??? or new cat
            // some of them are added as CnRb
            // Dram -> Drama
            // West -> Western
            // Myst -> Mystery
            // S.F. -> Sci-Fi
            // but some of them are written as ScFi so idk
            // Advt -> Adventure
            // Horr -> Horror
            // Romt -> Romantic -> Romance
            // Comd -> Comedy
            // some are combined for some reason ex: Romt Comd
            // Musc -> Musical
            // Docu -> Documentary
            // Porn -> Adult
            // Noir -> black (just store as noir i guess)
            // BioP -> biographical Picture -> Biography? idrk what to consider this as
            // TV -> TV show
            // TVs -> TV series
            // TVm -> TV miniseries

            // some that are not listed on that page
            // Actn -> Action
            // Cart -> Cartoon
            // Bio -> Biography
            // Crim -> Crime
            // Faml -> Family
            // Fant -> Fantasy
            // Hist -> History

            // for rest just add as new

//            if( cat tag doesnt exist ) {
            if(tempVal.strip() == "") { // check if this is correct // in case trailing spaces
                isConsistent = false;
            } else if(tempVal == null) {
                isConsistent = false;
            } else {
                // add to tempMovie
                // have to check genre before adding
                // have to .strip() b/c trailing spaces and .lower()
                if(catToGenreMap.containsKey(tempVal.strip().toLowerCase())){
                    String genre = catToGenreMap.get(tempVal.strip().toLowerCase()));
                    if(existingGenres.containsKey(genre)) {
                        tempMovie.addGenre(genre);
                    }
                    else if(newGenres.containsKey(genre)) {
                        tempMovie.addGenre(genre);
                    }
                    else {
                        // get available int and assign to new genre
                        newGenres.put(genre.toLowerCase(), availableGenreId);
                        availableGenreId++;
                        tempMovie.addGenre(genre);
                    }
                }
                else {
                    for(String g: tempVal.strip().toLowerCase().split("\\s+")) {
                        if (catToGenreMap.containsKey(g)) {
                            String genre = catToGenreMap.get(g);
                            if (existingGenres.containsKey(genre)) {
                                tempMovie.addGenre(genre);
                            } else if (newGenres.containsKey(genre)) {
                                tempMovie.addGenre(genre);
                            } else {
                                // get available int and assign to new genre
                                newGenres.put(genre.toLowerCase(), availableGenreId);
                                availableGenreId++;
                                tempMovie.addGenre(genre);
                            }
                        } else {
                            if (existingGenres.containsKey(g)) {
                                tempMovie.addGenre(g);
                            } else if (newGenres.containsKey(g)) {
                                tempMovie.addGenre(g);
                            } else {
                                // get available int and assign to new genre
                                newGenres.put(g.toLowerCase(), availableGenreId);
                                availableGenreId++;
                                tempMovie.addGenre(g);
                            }
                        }
                    }
                }
                // have to .split() in case combined genres
                // refer to comments above to get correct genre
                // have to add to somewhere so can insert into genres in movie
                // maybe a hashmap with movie id as key and list of genre ids as values
                // if new genre add to another hashmap? for genre table key: name, val: id
                // and tehn keep id
                // tempMovie.addGenre(tempVal.strip());
            }

        } else if (qName.equalsIgnoreCase("actor")) {
            //add it to the list
            // check if dupe
            // if dupe write to stars dupe file the name and birthyear of star (make sure to check with db info too)
            // if not add to hashmap
            if(isDuplicate) {
                starDupe++;
                //write to file
                writeToTextFile("/xmlParser/StarDuplicate.txt", tempStar.toString());
                isDuplicate = false;
            }
            else {
                myStars.add(tempStar);
            }

        } else if (qName.equalsIgnoreCase("stagename")) {
            // might have to .lower() and .strip() to check if dupe? not sure if
            // everythign is capitalized properly
            if( already exists previous in file or exists in db ) {
                isDuplicate = true;
                //write to star dupe file
            }
            else {
                // generate an id for star using available int remember to update the id afterwards
                // store somewhere for dupe checking
                // tempStar.setName(tempVal.strip());
            }
//            tempEmp.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            // if empty set null
            // if not valid set null?? not too sure dont rly get what to report for inconsistency stuff
            try (Integer.parseInt(tempVal.strip())) {
                // tempStar.setBirthYear(tempVal.strip());
            }
            catch(Exception e) {
                // set null
                // tempStar.setBirthYear(null);
            }
//            tempEmp.setId(Integer.parseInt(tempVal));
        } else if (qName.equalsIgnoreCase("filmc")) {
            //add it to the list
            // check if dupe
            // if dupe write to movies dupe file id title director year
            // if not add to hashmap
            // according to the demo vid its added to inconsistent file if there are movies with the same name but everything
            // else diff (id, director name, year)? not sure abt this

            // get smth to tell this to skip record if movie or star not found
            // maybe another boolean var
            myEmpls.add(tempEmp);

        } else if (qName.equalsIgnoreCase("f")) {
            // check if exists
            // if not report as missing
            if ( !myMovies.containsKey(tempVal.strip()) ) {
                moviesNotFound++;
                // write to movie missing file
                // should we continue checking in this case?
                // get something to tell it not to add this entry
            }
        }else if (qName.equalsIgnoreCase("a")) {
            // check if exists
            // if not report as missing
            // if exists find id
            // ignore if 's a'
            if ( a not found ) {
                starsNotFound++;
                // write to star missing file
            }
            else {
                String starId = //get star id
                // add to list
            }

        }
//        else if (qName.equalsIgnoreCase("Age")) {
//            tempEmp.setAge(Integer.parseInt(tempVal));
//        }

    }

    public static void main(String[] args) {
        SAXParser sp = new SAXParser();
        sp.runExample();
    }

}