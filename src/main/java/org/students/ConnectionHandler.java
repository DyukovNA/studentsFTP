package org.students;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class provides methods for interaction with FTP-server
 */
public class ConnectionHandler {
    /**
     * Fields for an FTP-link and filename
     */
    public final String link;

    /**
     * Creates FTP-link using user input and name of the file that should be stored on server
     * @param input User input with login information
     * @param JSONName Name of file
     */
    public ConnectionHandler(String input, String JSONName) {
        String[] loginInfo = input.split(" ");
        this.link = "ftp://" + loginInfo[0] + ":" + loginInfo[1] + "@" + loginInfo[2] + "/" + JSONName;
        try {
            BufferedReader reader = getReader();
            reader.close();
        } catch (IOException e) {
            System.out.println("\nConnection error. Check your login information and try again\n");
        }
    }
    /**
     * Creates BufferedReader to read the file from the connection.
     * @return BufferedReader to read the file
     * @throws IOException If is unable to get InputStream from URLConnection
     * @see URLConnection
     */
    public BufferedReader getReader() throws IOException {
        URLConnection connection = setConnection();
        assert connection != null;
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    /**
     * Creates BufferedWriter to write to the file from connection.
     * @return BufferedWriter to write to the file
     * @throws IOException If is unable to get OutputStream from URLConnection
     * @see URLConnection If is unable to get InputStream or OutputStream from URLConnection
     */
    public BufferedWriter getWriter() throws IOException {
        URLConnection connection = setConnection();
        assert connection != null;
        OutputStream outputStream = connection.getOutputStream();
        return new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    /**
     * Establishes connection with FTP-server and file on it. Creates
     * @return Connection to FTP-server
     * @see URLConnection If is unable to get InputStream or OutputStream from URLConnection
     */
    public URLConnection setConnection() {
        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.connect();
            return connection;
        } catch (IOException e) {
            System.out.println("Unable to connect to server");
            e.printStackTrace();
        }
        return null;
    }
}
