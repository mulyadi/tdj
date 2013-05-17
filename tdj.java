import static org.kohsuke.args4j.ExampleMode.ALL;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * tdj connects to and runs a query on Teradata. It needs Teradata JDBC jar files to run.
 * Teradata JDBC jar files (tdgssconfig.jar, terajdbc4.jar) can be downloaded from Teradata website.
 * <br><br>
 * Type the following to run the program:
 * <br>
 * java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj -H hostname -u username -p password [-o file] [-d delimiter] [-f format] [-r] sql
 * <br>
 * Example: java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj -H tdhostname -u johndoe -p mypassword -o current_date.txt -d "|" -f text -r 'select current_date'
 * <br><br>
 * Copyright (c) 2013 Mulyadi Kurniawan
 * <br><br>
 * @author Mulyadi Kurniawan
 * <br>
 * @version 1.0.0
*/

public class tdj { // beginning of tdj class
    /** The username. */
    @Option(name="-u", aliases="--username", usage="connect as this username", metaVar="[your username]", required=true)
    private String username;

    /** The password. */
    @Option(name="-p", aliases="--password", usage="connect with this password", metaVar="[your password]", required=true)
    private String password;

    /** The hostname. */
    @Option(name="-H", aliases="--hostname", usage="connect to this hostname (default localhost)", metaVar="[server hostname]", required=true)
    private String hostname = "localhost";

    /** The query. */
    @Argument(usage="the SQL to run", metaVar="query", required=true)
    private String query;
    
    /** The database connection. */
    private Connection con;

    /** The prepared statement. */
    private PreparedStatement pstmt;

    /** The result set. */
    private ResultSet rslt;

    /** The result set meta data. */
    private ResultSetMetaData rsltmeta;

    /** The output file. */
    @Option(name="-o", aliases="--output", usage="write output to this file (default is write to stdout)", metaVar="[output file name]")
    private String outputFile = "";

    /** The output format. */
    @Option(name="-f", aliases="--format", usage="format text, csv (default tab-separated text)", metaVar="[text or csv]")
    private String outputFormat = "text";

    /** The header boolean. */
    @Option(name="-r", aliases="--header", usage="print column names")
    private boolean header;

    /** The help boolean. */
    @Option(name="-h", aliases="--help", usage="display this help text and exit")
    private boolean help;

    /** The delimiter string. */
    @Option(name="-d", aliases="--delimiter", usage="character to be used as delimiter, override format option (default tab)", metaVar="[delimiter character]")
    private String delimiter = "";

    /** The command line parser. */
    private CmdLineParser parser;

    /** The update commands. */
    private String[] updates = {"commit", "create", "delete", "drop", "insert", "update"};

    /**
     * The tdj constructor.
    */
    public tdj() { // beginning of tdj constructor
    } // end of tdj constructor

    /**
     * This method determines if the output is standard output or file
     * @param outputFileName The output file name
    */
    Writer getOutputStream(String outputFileName) {
        if (!outputFileName.equalsIgnoreCase("")) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(outputFileName);
            } catch(FileNotFoundException exc) {
                System.err.println("Cannot open output file.");
                System.err.println("Please try again.");
                System.exit(1);
            }
            return pw;
        } else {
            return new OutputStreamWriter(System.out);
        }
    }

    /** This method connects to the database and run the query. */
    private void runQuery() { // beginning of runQuery method

        // Set delimiter string.
        if (delimiter.equalsIgnoreCase("")) {
          if (outputFormat.equalsIgnoreCase("text") || outputFormat.equalsIgnoreCase("tsv")) {
              delimiter = "\t";
          } else if (outputFormat.equalsIgnoreCase("csv")) {
              delimiter = ",";
          }
        }

        // Determine and open the output.
        BufferedWriter out;
        out = new BufferedWriter(getOutputStream(outputFile));

        // Construct database link.
        String databaseLink = "jdbc:teradata://" + hostname + "/TMODE=ANSI,CHARSET=UTF8";

        // Initiate JDBC driver.
        try {
            Class.forName("com.teradata.jdbc.TeraDriver");
        } catch(ClassNotFoundException exc) {
            System.err.println("Cannot find JDBC.");
            System.err.println("Please try again.");
            System.exit(1);
        }

        // Connect to the Teradata database specified in the database link
        // and submit username and password.
        try {
            //System.out.println("Connecting to " + databaseLink);
            con = DriverManager.getConnection(databaseLink, username, password);
            //System.out.println("Established successful connection.");
        } catch(SQLException exc) {
            System.err.println("Cannot connect to database.");
            System.err.println("Please try again.");
            System.exit(1);
        }

        // Execute query and print result.
        // Exceptions are printed in the catch block.
        try {
            pstmt = con.prepareStatement(query);

            for (int i = 0; i < updates.length; i++) { // Check for update commands.
                if (query.startsWith(updates[i])) {
                    int updateQueryResult = pstmt.executeUpdate();
                    out.write(updates[i] + " is executed. " + updateQueryResult + " rows affected.");
                    out.newLine();
                    out.flush();
                    out.close();
                    System.exit(0);
                }
            }

            rslt = pstmt.executeQuery();
            rsltmeta = rslt.getMetaData();
            int columnCount = rsltmeta.getColumnCount();

            // Print column label.
            if (header == true) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = rsltmeta.getColumnLabel(i);
                    out.write(columnLabel);
                    if (i < columnCount) {
                        out.write(delimiter);
                    }
                }
                out.newLine();
            }

            // Print data.
            while (rslt.next() == true) {
                for (int i = 1; i <= columnCount; i++) {
                    String resultString = rslt.getString(i);
                    try { // Catch null result string.
                        out.write(resultString);
                    } catch(NullPointerException exc) {
                        out.write("");
                    }
                    if (i < columnCount) {
                        out.write(delimiter);
                    }
                }
                out.newLine();
            }
            rslt.close();
        } catch(SQLException exc) {
            while (exc != null) {
                System.err.println(exc);
                System.err.println("Please try again.");
                exc = exc.getNextException();
            }
        } catch(IOException exc) {
            System.err.println(exc);
            System.err.println("Please try again.");
        }

        // Close output stream.
        try {
        out.flush();
        out.close();
        } catch(IOException exc) {
            System.err.println("Cannot close output file.");
            System.err.println("Please try again.");
            System.exit(1);
        }

        // Close connection.
        try {
            con.close();
            //System.out.println("Disconnected.");
        } catch (SQLException exc) {
            System.err.println("Cannot close connection to database.");
            System.err.println("Please try again.");
            System.exit(1);
        }

    } // end of runQuery method

    /**
     * This method parse command line arguments and assign them to variables.
     * @param args Arguments from main method.
    */
    private void parseArgument(String[] args) { // beginning of parseArgument method
        // Read command line arguments.
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(Integer.MAX_VALUE);
        try {
            parser.parseArgument(args);

            if (help == true) {
                System.out.println("java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj [options] arguments");

                // Print the list of available options.
                parser.printUsage(System.out);
                System.out.println();

                // Print option sample. This is useful some time.
                System.out.println(" Example: java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj" + parser.printExample(ALL) + " query");

                System.exit(0);
            }
        } catch(CmdLineException exc) {
            System.err.println(exc.getMessage());
            System.err.println("java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj [options] arguments");

            // Print the list of available options.
            parser.printUsage(System.err);
            System.err.println();

            // Print option sample. This is useful some time.
            System.err.println(" Example: java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj" + parser.printExample(ALL) + " query");

            System.exit(1);
        }
    } // end of parseArgument method

    /**
     * The tdj's main method.
     * @param args Command line arguments.
    */
    public static void main(String[] args) { // beginning of main method

        tdj ts = new tdj();
        ts.parseArgument(args);
        ts.runQuery();

    } // end of main method
} // end of tdj class

