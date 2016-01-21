/*
 * Music store: Java Interface with chinook.db
 */
package musicstore;

import java.sql.*;
import java.util.*;

/**
 *
 * @author Raymond
 */
public class MusicStore {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        dbConnection();
        getUserinput();
    }

    /**
     * Tests if the database connects correctly
     */
    public static void dbConnection() {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    /**
     * This method gets input from user and calls the correct method according
     * to user input
     */
    public static void getUserinput() {
        String cont = "Y";
        while (cont.equalsIgnoreCase("y")) {
            //displaying options
            System.out.print("Please select an operation by entering the "
                    + "corresponding number\n1. Albums by Artist\n2. Tracks of "
                    + "an album title\n3. Purchase history of a Customer\n"
                    + "4. Track price-individual\n5. Track price-batch\n6. Market Population"
                    + "\n7. Track Recommender\n8. Top Seller by Revenue\n"
                    + "9. Top Seller by Volume\n0. Exit\n Enter Operation: ");
            Scanner scanner = new Scanner(System.in);
            /*checking which operation the user wants and calling the 
             *corresponding method
             */
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input please try again: ");
                scanner.next();

            }
            int op = scanner.nextInt();
            switch (op) {
                case 1:
                    albumByArtist();
                    cont = newOp();
                    break;
                case 2:
                    tracksOfAlbum();
                    cont = newOp();
                    break;
                case 3:
                    purchaseHistory();
                    cont = newOp();
                    break;
                case 4:
                    trackPriceIndivdual();
                    cont = newOp();
                    break;
                case 5:
                    trackPriceBatch();
                    cont = newOp();
                    break;
                case 6:
                    marketPopulation();
                    cont = newOp();
                    break;
                case 7:
                    trackRecommender();
                    cont = newOp();
                    break;
                case 8:
                    topSellerRevenue();
                    cont = newOp();
                    break;
                case 9:
                    topSellerVolume();
                    cont = newOp();
                    break;
                case 0:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    /**
     * This method asks if the user would like to redo the same operation
     *
     * @return input This is the user's answer
     */
    public static String again(String msg) {
        String input = " ";
        while (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
            Scanner scanner = new Scanner(System.in);
            System.out.print(msg);
            input = scanner.next();
            if (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
                System.out.println("Invalid input");
            }
        }
        return input;
    }

    /**
     * This method asks the user if they would like to use another operation
     *
     * @return input This is user's answer
     */
    public static String newOp() {
        String input = " ";
        while (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Would you like to do another operation?(Y/N): ");
            input = scanner.next();
            if (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
                System.out.println("Invalid input");
            }
        }
        return input;
    }

    /**
     * This method allows user to Album titles based on artist names
     */
    public static void albumByArtist() {
        String again = "y";
        System.out.println("This operation finds albums by artist.");
        while (again.equalsIgnoreCase("y")) {
            //getting user input
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please enter artist's name: ");
            String artist = scanner.nextLine();
            // db connection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();
                //db query
                ResultSet rs = stmt.executeQuery("SELECT A.AlbumId, A.Title,"
                        + " AT.ArtistID FROM ALBUM A, Artist AT WHERE AT.ArtistId = A."
                        + "ArtistId AND AT.Name = \"" + artist + "\" COLLATE NOCASE;");
                //checking if query returned empty
                if (!(rs.next())) {
                    System.out.println("No Information Found");
                    //lets user retry if empty results
                    String redo = again("Do you want to try again?(Y/N): ");
                    if (redo.equalsIgnoreCase("y")) {
                        albumByArtist();
                    } else {
                        rs.close();
                        stmt.close();
                        c.close();
                        System.out.println("GoodBye");
                        System.exit(0);
                    }
                } else {
                    do {
                        //setting returned data to variables
                        int artistId = rs.getInt("artistid");
                        int albumId = rs.getInt("albumid");
                        String title = rs.getString("title");
                        // displaying return data to users
                        System.out.println("\nArtistId = " + artistId);
                        System.out.println("AlbumId = " + albumId);
                        System.out.println("Title = " + title + "\n");
                    } while (rs.next());

                }
                //closing connection
                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Operation done successfully");
            //loop if user wants to do the same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }

    }

    /**
     * Allows user to find track information by album title and gives option for
     * user to buy tracks found
     */
    public static void tracksOfAlbum() {
        String again = "y";// for looping operation
        String purchase = "y"; // to keep track of whether not user wants to buy
        Boolean correctId = false; // to check if track id matches result
        System.out.println("This operation finds Track info by album and gives "
                + "the option to purchase tracks.");
        while (again.equalsIgnoreCase("y")) {
            Scanner scanner = new Scanner(System.in);
            Vector<Integer> tracks = new Vector<>();// store track id results
            System.out.print("Please enter album's title: ");
            String album = scanner.nextLine();
            // db connection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();
                //query
                ResultSet rs = stmt.executeQuery("SELECT A.AlbumId, T.Name AS "
                        + "tname, T.TrackId, G.Name AS gname, T.UnitPrice "
                        + "FROM ALBUM A, Track T,Genre G WHERE T.GenreId = "
                        + "G.GenreId AND T.AlbumId = A.AlbumId AND A.Title = \""
                        + album + "\" COLLATE NOCASE;");
                //checking for empty results
                if (!(rs.next())) {
                    //lets users redo operation if returned empty results
                    System.out.println("No Information Found");
                    String redo = again("Do you want to try again?(Y/N): ");
                    if (redo.equalsIgnoreCase("y")) {
                        tracksOfAlbum();
                    } else {
                        rs.close();
                        stmt.close();
                        c.close();
                        System.out.println("GoodBye");
                        System.exit(0);

                    }
                } else {
                    do {
                        //setting results to variables
                        int trackId = rs.getInt("trackid");
                        int albumId = rs.getInt("albumid");
                        String trackName = rs.getString("gname");
                        String genreName = rs.getString("gname");
                        String unitPrice = rs.getString("unitprice");
                        tracks.addElement(trackId);
                        //dislaying results
                        System.out.println("\nTrackId = " + trackId);
                        System.out.println("Track Name = " + trackName);
                        System.out.println("Unit Price = $" + unitPrice);
                        System.out.println("AlbumId = " + albumId);
                        System.out.println("Genre = " + genreName + "\n");

                    } while (rs.next());

                }
                //option to purchase or not
                purchase = again("Would you like purchase any of these"
                        + " tracks?(Y/N): ");
                while (purchase.equalsIgnoreCase("y")) {
                    System.out.print("Please enter ID of the Track you would like"
                            + " to purchase: ");
                    while (!scanner.hasNextInt()) {
                        System.out.print("Invalid input please try again: ");
                        scanner.next();

                    }
                    int trackId = scanner.nextInt();
                    //checking if track id entered matches results stored in vector
                    for (int i = 0; i < tracks.size(); i++) {
                        if (tracks.get(i) == trackId) {
                            correctId = true;
                        } else {
                            correctId = false;
                        }
                    }
                    if (correctId) {
                        //checks again since user might have looped multiple times
                        for (int i = 0; i < tracks.size(); i++) {
                            if (tracks.get(i) == trackId) {
                                correctId = true;
                            } else {
                                correctId = false;
                            }
                        }
                        int quanity;

                        System.out.print("Please enter quanity you would like to"
                                + " purchase: ");

                        while (!scanner.hasNextInt()) {
                            System.out.print("Invalid input please try again: ");
                            scanner.next();

                        }
                        quanity = scanner.nextInt();
                        //making sure quantity is greater than 0
                        while (quanity <= 0) {
                            System.out.print("Quantity must be greater than 0"
                                    + ", Please try again:  ");
                            quanity = scanner.nextInt();
                        }
                        try {
                            //getting invoice id of Victor Stevens
                            rs = stmt.executeQuery("SELECT I.InvoiceId FROM "
                                    + "Invoice I WHERE I.CustomerId = 25;");
                            while (rs.next()) {
                                int invoiceId = rs.getInt("invoiceid");
                                //updating purchase
                                String sql = "UPDATE INVOICELINE set QUANTITY "
                                        + "= " + quanity + " where INVOICEID "
                                        + "= " + invoiceId + " AND TRACKID "
                                        + "= " + trackId + ";";
                                stmt.executeUpdate(sql);
                                c.commit();
                            }

                        } catch (Exception e) {
                            System.err.println(e.getClass().getName() + ": " + e.getMessage());
                            System.exit(0);
                        }
                        System.out.println("Operation done successfully");
                        purchase = again("Would you like to purchase more?(Y/N): ");

                    } else {
                        //lets user try again if track id did not match results
                        purchase = again("You did not enter a vaild track ID,"
                                + "would you like to try again?(Y/N): ");
                    }

                }

                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Operation done successfully");
            //lets user loop same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }

    }

    /**
     * This method lets user view selected customer's purchase history
     */
    public static void purchaseHistory() {
        String again = "y";
        System.out.println("This operation finds customer purchase history.");
        while (again.equalsIgnoreCase("y")) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please enter customer ID: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input please try again: ");
                scanner.next();

            }
            int cid = scanner.nextInt();
            //db connection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();
                //db query
                ResultSet rs = stmt.executeQuery("SELECT T.Name AS tname, A.Title,"
                        + " IL.Quantity, I.InvoiceDate FROM ALBUM A, Invoice I,"
                        + " InvoiceLine IL, Track T WHERE T.TrackId"
                        + " = IL.TrackID AND I.InvoiceId = IL.InvoiceId "
                        + "AND I.CustomerId = " + cid + ";");
                //checking for empty results
                if (!(rs.next())) {
                    System.out.println("No Information Found");
                    //lets user retry if empty results
                    String redo = again("Do you want to try again?(Y/N): ");
                    if (redo.equalsIgnoreCase("y")) {
                        purchaseHistory();
                    } else {
                        rs.close();
                        stmt.close();
                        c.close();
                        System.out.println("GoodBye");
                        System.exit(0);
                    }
                } else {
                    do {
                        //setting results to variables
                        int quantity = rs.getInt("quantity");
                        String title = rs.getString("title");
                        String trackName = rs.getString("tname");
                        String invoiceDate = rs.getString("invoicedate");
                        //displaying results
                        System.out.println("\nAlbum Title = " + title);
                        System.out.println("Track Name = " + trackName);
                        System.out.println("Quantity = " + quantity);
                        System.out.println("Date = " + invoiceDate + "\n");
                    } while (rs.next());

                }

                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Operation done successfully");
            //lets user loop the same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }
    }

    /**
     * This method allows user to change unit price of selected track
     */
    public static void trackPriceIndivdual() {
        String again = "y";
        System.out.println("This operation allows user to change unit"
                + " price of selected track.");
        while (again.equalsIgnoreCase("y")) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please enter Track ID: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input please try again: ");
                scanner.next();

            }
            int tid = scanner.nextInt();
            //db conection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT T.UnitPrice "
                        + "FROM ALBUM A, Track T WHERE T.TrackId = " + tid + ";");
                //checking for empty results
                if (!(rs.next())) {
                    System.out.println("No Information Found");
                    String redo = again("Do you want to try again?(Y/N): ");
                    //lets user redo if empty results
                    if (redo.equalsIgnoreCase("y")) {
                        purchaseHistory();
                    } else {
                        rs.close();
                        stmt.close();
                        c.close();
                        System.out.println("GoodBye");
                        System.exit(0);
                    }
                } else {
                    do {
                        //displaying current price
                        String unitPrice = rs.getString("unitprice");
                        System.out.println("\nCurrent Unit Price = " + unitPrice);
                        System.out.print("Enter new unit price: ");
                        //getting new price and updating current unit price
                        while (!scanner.hasNextDouble()) {
                            System.out.print("Invalid input please try again: ");
                            scanner.next();

                        }
                        double newPrice = scanner.nextDouble();
                        String sql = "UPDATE TRACK set UNITPRICE ="
                                + " '" + newPrice + "' where TRACKID = " + tid + ";";
                        stmt.executeUpdate(sql);
                        c.commit();
                        System.out.println("\nNew Unit Price = " + newPrice);
                    } while (rs.next());

                }

                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Operation done successfully");
            // for user to loop the same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }
    }

    /**
     * This method allows users to change unit price of selected track by
     * percentage
     */
    public static void trackPriceBatch() {
        String again = "y";
        System.out.println("This operation allows user to change unit"
                + " price of selected track by percentage.");
        while (again.equalsIgnoreCase("y")) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please enter Track ID: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input please try again: ");
                scanner.next();

            }
            int tid = scanner.nextInt();
            //db connection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();
                //db query
                ResultSet rs = stmt.executeQuery("SELECT T.UnitPrice "
                        + "FROM ALBUM A, Track T WHERE T.TrackId = " + tid + ";");
                //checking for empty results
                if (!(rs.next())) {
                    System.out.println("No Information Found");
                    //lets user redo if empty results
                    String redo = again("Do you want to try again?(Y/N): ");
                    if (redo.equalsIgnoreCase("y")) {
                        purchaseHistory();
                    } else {
                        rs.close();
                        stmt.close();
                        c.close();
                        System.out.println("GoodBye");
                        System.exit(0);
                    }
                } else {
                    do {
                        //setting results to variables
                        String unitPrice = rs.getString("unitprice");
                        System.out.println("\nCurrent Unit Price = " + unitPrice);
                        //getting desired percentage change
                        System.out.print("Enter percentage change (-100 to 100): ");
                        while (!scanner.hasNextDouble()) {
                            System.out.print("Invalid input please try again: ");
                            scanner.next();

                        }
                        double percentage = scanner.nextDouble();
                        //checking to see if percentage is valid
                        while (percentage <= -100 || percentage >= 100) {
                            System.out.print("Invalid percentage, try again: ");
                            while (!scanner.hasNextDouble()) {
                                System.out.print("Invalid input please try again: ");
                                scanner.next();

                            }
                            percentage = scanner.nextDouble();
                        }
                        // converting percentage to decimal for calculations
                        percentage /= 100;
                        double current = Double.parseDouble(unitPrice);
                        //calcualtions for unit price change
                        double change = percentage * current;
                        double newPrice = current + change;
                        //updating current unit price to new unit price
                        String sql = "UPDATE TRACK set UNITPRICE ="
                                + " '" + newPrice + "' where TRACKID = " + tid + ";";
                        stmt.executeUpdate(sql);
                        c.commit();
                        System.out.println("\nNew Unit Price = " + newPrice);
                    } while (rs.next());

                }

                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Operation done successfully");
            //for user to loop same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }
    }

    /**
     * This method allows users to see which state did not sell 1 or tracks
     */
    public static void marketPopulation() {
        String again = "y";
        System.out.println("This operation finds information about purchases in "
                + "specfied state.");
        while (again.equalsIgnoreCase("y")) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please enter State Initials (e.g. CA): ");
            String state = scanner.next();
            state.toUpperCase();// so initals of state is always capitalized 
            //db connection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();
                //db query
                ResultSet rs = stmt.executeQuery("SELECT PT.PlaylistId, P.Name, A.title, "
                        + "C.CustomerId, C.FirstName, C.lastName FROM Customer C, "
                        + "Playlist P, PlaylistTrack PT, Track T, Invoice I, "
                        + "InvoiceLine IL, Album A WHERE A.AlbumId = T.AlbumId AND "
                        + "C.CustomerId = I.CustomerId AND "
                        + "I.InvoiceId = IL.InvoiceId AND T.TrackId = IL.TrackId"
                        + " AND PT.TrackId = T.TrackId AND PT.PlaylistId = P.PlaylistId"
                        + " AND C.State <> '" + state + "' GROUP BY PT.TrackId"
                        + " HAVING COUNT(*) >= 1;");
                //checking for empty results
                if (!(rs.next())) {
                    System.out.println("No Information Found");
                    //lets user retry if empty results
                    String redo = again("Do you want to try again?(Y/N): ");
                    if (redo.equalsIgnoreCase("y")) {
                        purchaseHistory();
                    } else {
                        rs.close();
                        stmt.close();
                        c.close();
                        System.out.println("GoodBye");
                        System.exit(0);
                    }
                } else {
                    do {
                        //setting results to variables
                        int cid = rs.getInt("customerid");
                        int pid = rs.getInt("playlistid");
                        String firstName = rs.getString("firstname");
                        String lastName = rs.getString("lastname");
                        String playlistName = rs.getString("name");
                        String title = rs.getString("title");
                        //displaying results
                        System.out.println("\nCustomer ID: " + cid);
                        System.out.println("Customer Name: " + firstName + " "
                                + lastName);
                        System.out.println("Album title: " + title);
                        System.out.println("Playlist Name: " + playlistName);
                        System.out.println("Playlist ID: " + pid + "\n");
                    } while (rs.next());

                }

                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Operation done successfully");
            //lets user loop the same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }

    }

    /**
     * This method recommends tracks to customers if they bought 3 or more
     * distinct tracks from an album
     */
    public static void trackRecommender() {
        String again = "y";
        System.out.println("This operation recommends tracks to specified customer");
        while (again.equalsIgnoreCase("y")) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please enter Customer ID: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input please try again: ");
                scanner.next();

            }
            int cid = scanner.nextInt();

            //db connection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();
                //db query
                ResultSet rs = stmt.executeQuery("SELECT A.Title, T.Name, "
                        + "IL.InvoiceId FROM Invoice I, InvoiceLine IL, Track T, "
                        + "Album A WHERE I.InvoiceId = IL.InvoiceId AND A.AlbumId"
                        + "= T.AlbumId AND IL.trackId = T.TrackID AND I.CustomerID"
                        + "= " + cid + " GROUP BY IL.InvoiceId HAVING COUNT"
                        + "(DISTINCT IL.TrackId)>= 3;");
                //checking for empty results
                if (!(rs.next())) {
                    System.out.println("No Information Found");
                    //lets user retry if empty results
                    String redo = again("Do you want to try again?(Y/N): ");
                    if (redo.equalsIgnoreCase("y")) {
                        purchaseHistory();
                    } else {
                        rs.close();
                        stmt.close();
                        c.close();
                        System.out.println("GoodBye");
                        System.exit(0);
                    }
                } else {
                    do {
                        //setting results to variables
                        String tName = rs.getString("name");
                        String albumTitle = rs.getString("title");

                        //displaying results
                        System.out.println("\nRecommended Track: " + tName);
                        System.out.println("From Album: " + albumTitle);

                    } while (rs.next());

                }

                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Operation done successfully");
            //lets user loop the same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }

    }

    /**
     * This method displays artist who have top sellers by revenue
     */
    public static void topSellerRevenue() {
        String again = "y";
        while (again.equalsIgnoreCase("y")) {
            //db connection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();
                //db query
                ResultSet rs = stmt.executeQuery("SELECT  DISTINCT A.Name, (IL.UnitPrice *"
                        + "IL.Quantity) AS Revenue FROM Artist A, Album AL, Track T,"
                        + " InvoiceLine IL WHERE A.ArtistId = AL.ArtistId AND"
                        + " AL.AlbumId = T.AlbumId AND T.Trackid = IL.TrackId AND"
                        + " Revenue in (SELECT MAX(IL.UnitPrice * IL.Quantity)"
                        + " FROM InvoiceLine IL) ;");
                //checking for empty results
                System.out.println("\nTop Sellers by Revenue:\n");
                while (rs.next()) {
                    //setting results to variables
                    String aName = rs.getString("name");
                    //displaying results
                    System.out.println(aName);

                }

                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }

            System.out.println("Operation done successfully");
            //lets user loop the same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }

    }

    /**
     * This method displays all artists with top sellers by volume
     */
    public static void topSellerVolume() {
        String again = "y";
        while (again.equalsIgnoreCase("y")) {
            //db connection
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:chinook.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
                stmt = c.createStatement();
                //db query
                ResultSet rs = stmt.executeQuery("SELECT  DISTINCT A.Name,"
                        + " IL.Quantity FROM Artist A, Album AL, Track T,"
                        + " InvoiceLine IL WHERE A.ArtistId = AL.ArtistId AND"
                        + " AL.AlbumId = T.AlbumId AND T.Trackid = IL.TrackId AND"
                        + " IL.Quantity in (SELECT MAX(IL.Quantity)"
                        + " FROM InvoiceLine IL) ;");
                //checking for empty results
                System.out.println("\nTop Sellers by Volume:\n");
                while (rs.next()) {
                    //setting results to variables
                    String aName = rs.getString("name");
                    //displaying results
                    System.out.println(aName);

                }

                rs.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }

            System.out.println("Operation done successfully");
            //lets user loop the same operation
            again = again("Would you like to do the same operation again?(Y/N): ");
        }

    }

}
