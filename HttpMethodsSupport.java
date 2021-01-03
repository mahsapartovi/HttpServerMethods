/* The forth Assignment:Write a Http Server that can be handle GET & HEAD methods from clients
email : partovi_mahsa@yahoo.com
profile : BS student in Computer Engineering From IRAN, Urmia University
I explained each line with a comment :)
*/
package internet.en;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpMethodsSupport implements Runnable{

    // port that listen connection
    static final int PORT = 80;
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    static final File WEB = new File(".");
    static final String FILE_NOT_FOUND = "404.html";
    static final String DEFAULT_FILE = "index.html";

    // verbose mode
    static final boolean verbose = true;

    // Client Connection via Socket Class
    private final Socket connection;

    public HttpMethodsSupport(Socket c) {
        connection = c;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started!!! Welcome ...\nListening for connections on port : " + PORT + " ...\n");

            // we listen until user halts server execution
            while (true) {
                HttpMethodsSupport myServer = new HttpMethodsSupport(serverConnect.accept());

                if (verbose) {
                    System.out.println("Connection Opened!!!! :) " + new Date() );
                }
                // create thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage() + " :( ");
        }
    }
    public void run() {
        // here i want to manage client connection
        BufferedOutputStream dataOut = null;
        String fileRequested = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            // i read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            // i get character output stream to client for headers
            out = new PrintWriter(connection.getOutputStream());
            // get binary output stream to client for requested data
            dataOut = new BufferedOutputStream(connection.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();
            // here i want to parse the request with a string tokenizer
            StringTokenizer parser = new StringTokenizer(input);
            String method = parser.nextToken().toUpperCase(); // i get the HTTP method of the client in this line
            // i get file requested
            fileRequested = parser.nextToken().toLowerCase();

            // in this assignment we just support only HEAD and GET methods, I check
            if (!method.equals("HEAD")  && !method.equals("GET")) {
                if (verbose) {
                    System.out.println("501 Not Implemented : " + method + " method");
                }

                // i return the not supported file to the client
                File file = new File(WEB, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                //read content to return to client
                byte[] fileData = readFileData(file, fileLength);

                //send HTTP Headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from Partovi :))");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + fileLength);
                out.println();
                out.flush(); // flush character output stream buffer
                // file
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            } else {
                // GET or HEAD method
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                File file = new File(WEB, fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if (method.equals("GET")) { // GET method so we return content
                    byte[] fileData = readFileData(file, fileLength);

                    // send HTTP Headers
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Java HTTP Server from Partovi : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + fileLength);
                    out.println();
                    out.flush(); // flush character output stream buffer

                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                }

                if (verbose) {
                    System.out.println("File " + fileRequested + " of type " + content + " returned");
                }
            }
        } catch (FileNotFoundException fnfe) {
            try {
                fileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage() + " :( ");
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connection.close(); //close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }
            if (verbose) {
                System.out.println("Connection closed!! Thanks.\n");
            }
        }
    }
    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }
        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from Partovi :))");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose) {
            System.out.println("File " + fileRequested + " not found");
        }
    }
}
