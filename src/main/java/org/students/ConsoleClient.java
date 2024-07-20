package org.students;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * This class includes all methods to work with FTP-server and the file on it
 */
public class ConsoleClient {
    /**
     * Fields for an FTP-link and filename
     */
    public final String link;
    public final String JSONName;
    /**
     * Regexes that are used in class to parse the .json file
     */
    private static final String idRegex = "\\s+\"id\": \\d+,";
    private static final String argumentRegex = "\\s+\"[\\w\\d]+\":\\s+\"?[\\w\\d\\s]+\"?,?";
    private static final String symbolsRegex = "[\\s:\"{},]";
    private static final String endOfLastBlockRegex = "\\s+}";
    private static final String beginOfNewBoxRegex = "\\s+\\{";
    private static final String multipleInputRegex = "\\w+ [\\w\\d]+((, \\w+ [\\w\\d]+)+)?";

    /**
     * Builds an FTP-link from the info provided by user in Main
     * @param loginInfo Information provided by user
     * @param filename Name of the .json file
     */
    public ConsoleClient(String[] loginInfo, String filename){
        link = "ftp://" + loginInfo[0] + ":" + loginInfo[1] + "@" + loginInfo[2] + "/";
        JSONName = filename;
    }

    /**
     * Calls methods according to the number of action provided by user
     * @param action Number corresponding to action
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection in any of methods
     * @see URLConnection
     */
    public void menu(Integer action) throws IOException, InputMismatchException {
        switch (action) {
            case 1: {
                showListOfStudents();
                break;
            }
            case 2: {
                showStudentInfo();
                break;
            }
            case 3: {
                showAddStudent();
                break;
            }
            case 4: {
                showDeleteStudent();
                break;
            }
            case 5: {
                System.out.println("Exiting session...");
                break;
            }
            default: {
                System.out.println("Unknown command");
                break;
            }
        }
    }

    /**
     * Interacts with user to get ID of the student that needs to be removed from the file.
     * Calls deleteStudent() method to remove student with given ID.
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see URLConnection
     */
    private void showDeleteStudent() throws IOException {
        System.out.println("Please, enter student's ID");
        Scanner scanner = new Scanner(System.in);
        int idDelete = scanner.nextInt();
        deleteStudent(Integer.toString(idDelete));
        System.out.println("Student successfully removed");
    }

    /**
     * Interacts with user to get information about the student that needs to be added to the file.
     * Calls addStudent() method to add student with given arguments.
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see URLConnection
     */
    private void showAddStudent() throws IOException, InputMismatchException {
        System.out.println("Please, enter student's information:\n" +
                "Note: enter arguments and their values in pairs. " +
                "Pairs are divided by space and comma (', ')." +
                "Argument and its value are divided by space\n" +
                "For example: name Alex, age 20");
        Scanner scanner = new Scanner(System.in);
        String info = scanner.next();
        if (!info.matches(multipleInputRegex)) {
            System.out.println("Invalid input");
            return;
        }
        addStudent(info);
        System.out.println("Student successfully added");
    }

    /**
     * Interacts with user to get ID of the student whose information is needed.
     * Calls getStudentInfo() method to get data of student with given ID.
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see URLConnection
     */
    private void showStudentInfo() throws IOException {
        System.out.println("Please, student's ID:");
        Scanner scanner = new Scanner(System.in);
        String id = scanner.nextLine();
        System.out.println(getStudentInfo(id));
    }

    /**
     * Calls getSortedListOfStudents() method to get list of students in alphabetical order and prints it out.
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see URLConnection
     */
    private void showListOfStudents() throws IOException {
        List<String> listOfStudents = getSortedListOfStudents();
        listOfStudents.forEach(System.out::println);
    }

    /**
     * Creates BufferedReader to read the file from the connection
     * @return BufferedReader to read the file
     * @throws IOException If is unable to get InputStream from URLConnection
     * @see URLConnection
     */
    private BufferedReader getReader() throws IOException {
        URLConnection connection = setConnection(JSONName);
        assert connection != null;
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    /**
     * Creates BufferedWriter to write to the file from connection
     * @return BufferedWriter to write to the file
     * @throws IOException If is unable to get OutputStream from URLConnection
     * @see URLConnection If is unable to get InputStream or OutputStream from URLConnection
     */
    private BufferedWriter getWriter() throws IOException {
        URLConnection connection = setConnection(JSONName);
        assert connection != null;
        OutputStream outputStream = connection.getOutputStream();
        return new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    /**
     * Establishes connection with FTP-server and file on it.
     * @param filename Name of .json file on server
     * @return Connection to FTP-server
     * @see URLConnection If is unable to get InputStream or OutputStream from URLConnection
     */
    private URLConnection setConnection(String filename) {
        try {
            URL url = new URL(link + filename);
            URLConnection connection = url.openConnection();
            connection.connect();
            return connection;
        } catch (IOException e) {
            System.out.println("Unable to connect to server");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets list of names from file using getListOfStudents() and sorts it
     * @return Sorted list of students' names
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     */
    private List<String> getSortedListOfStudents() throws IOException {
        List<String> students = new ArrayList<>();
        getListOfStudents(students);

        Collections.sort(students);
        return students;
    }

    /**
     * Finds student with specified ID if file and returns all information written in file
     * @param id ID of student
     * @return String with all information about the student in file
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     */
    private String getStudentInfo(String id) throws IOException {
        String idToFind = "id" + id;
        Map<String, String> data = new LinkedHashMap<>();

        findStudentGetInfo(idToFind, data);
        if (data.isEmpty()) {
            return "No information found";
        }

        return data.toString().replaceAll("[{}]", "");
    }

    /**
     * Writes information about student into file. Generates ID.
     * @param info String of specified format with information about student
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     */
    private void addStudent(String info) throws IOException {
        BufferedReader reader = getReader();
        BufferedWriter writer = getWriter();

        int maxId = 0;
        String line;
        StringBuilder text = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.matches(idRegex)) {
                // Max value of id read to this moment
                maxId = parseId(line);
                writeNewLine(text, line);
            } else if (line.matches(endOfLastBlockRegex)) {
                // If it is last record insert new record with ID += 1
                maxId += 1;
                writeNewStudent(maxId, text, line, info);
                break;
            } else writeNewLine(text, line);
        }

        writer.write(text.toString());
        writer.close();
        reader.close();
    }

    /**
     * Removes record of student with given ID by rewriting whole file and skipping unneeded part.
     * Changes IDs if necessary.
     * @param idToRemove ID of student
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     */
    private void deleteStudent(String idToRemove) throws IOException {
        BufferedReader reader = getReader();
        BufferedWriter writer = getWriter();

        String line;
        StringBuilder text = new StringBuilder();
        StringBuilder studentBlock = new StringBuilder();
        boolean isRemoved = false;
        
        while ((line = reader.readLine()) != null) {
            if (line.matches(beginOfNewBoxRegex)) {
                if (!studentBlock.toString().matches("\\s+\\{\n")) {
                    text.append(studentBlock);
                }
                studentBlock = new StringBuilder();
                writeNewLine(studentBlock, line);
            } else if (line.matches(endOfLastBlockRegex)) {
                writeNewLine(studentBlock, line);
                text.append(studentBlock);
            } else if (line.matches(idRegex)) {
                isRemoved = analyseId(idToRemove, line, isRemoved, text, studentBlock, reader);
            } else if (line.matches(argumentRegex) || line.matches("\\s+},")) {
                writeNewLine(studentBlock, line);
            } else
                writeNewLine(text, line);
        }
        
        writer.write(text.toString());
        writer.close();
        reader.close();
    }

    /**
     * Fills empty list with names found in file
     * @param students Previously created empty list
     * @throws IOException If is unable to get InputStream from URLConnection
     * @see URLConnection
     */
    private void getListOfStudents(List<String> students) throws IOException {
        BufferedReader reader = getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("\"name\"")) {
                students.add(
                        line.replace("\"name\"", "").replaceAll(symbolsRegex,"")
                );
            }
        }
        reader.close();
    }

    /**
     * Finds student in file by provided ID and reads all its fields
     * @param idToFind Provided ID
     * @param data Map for arguments and their values
     * @throws IOException If is unable to get InputStream from URLConnection
     * @see URLConnection
     */
    private void findStudentGetInfo(String idToFind, Map<String, String> data) throws IOException {
        BufferedReader reader = getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches(idRegex)) {
                String s =  line.replaceAll(symbolsRegex,"");
                if (s.equals(idToFind)) {
                    readStudentInfo(reader, data);
                }
            }
        }
        reader.close();
    }

    /**
     * Writes new student entry into file
     * @param id ID of new student
     * @param text StringBuilder with altered file
     * @param line Last line read by BufferedReader
     * @param info String of specified format with information about student
     */
    private void writeNewStudent(int id, StringBuilder text, String line, String info) {
        String[] infoPairs = info.split(", ");
        int i = infoPairs.length;

        text.append(line).append(",\n\t\t{\n");
        text.append("\t\t\t\"id\": ").append(id).append(",\n");

        for (String infoPair: infoPairs) {
            i-=1;
            writeNewField(i, infoPair, text);
        }
        text.append("\t\t}\n\t]\n}");
    }

    /**
     * Is called on every line that contains ID. If entry with idToRemove is already removed writes line to text with
     * ID lowered by 1. If ID in line matches idToRemove calls skipStudent() so that student won't be written into
     * studentBlock. In any other cases writes line into studentBlock.
     * @param idToRemove ID of student
     * @param line Last line read by BufferedReader
     * @param isRemoved Indicator of if the student was removed
     * @param text StringBuilder with altered file
     * @param studentBlock StringBuilder with .json list entry
     * @param reader BufferedReader of file
     * @return Is student entry skipped
     * @throws IOException If error occurred in skipStudent
     */
    private boolean analyseId(
            String idToRemove, String line, boolean isRemoved, StringBuilder text,
            StringBuilder studentBlock, BufferedReader reader
    ) throws IOException {
        int currId;
        currId = parseId(line);
        if (Integer.parseInt(idToRemove) == currId && !isRemoved) {
            skipStudent(line, reader, text);
            isRemoved = true;
        } else if (isRemoved) {
            currId -= 1;
            writeNewLine(studentBlock, "\t\t\t\"id\": " + currId + ",");
        } else {
            writeNewLine(studentBlock, line);
        }
        return isRemoved;
    }

    /**
     * Skips lines of file that contain fields of student entry
     * @param line Last line read by BufferedReader
     * @param reader BufferedReader of file
     * @param text StringBuilder with altered file
     * @throws IOException If unable to read next line
     */
    private void skipStudent(
            String line, BufferedReader reader, StringBuilder text
    ) throws IOException {
        while (line.matches(argumentRegex)) {
            line = reader.readLine();
        }
        if (line.matches(endOfLastBlockRegex)) {
            if (text.indexOf("},") != -1) {
                text.replace(text.length()-5, text.length()-1, "\t\t}");
            }
        }
    }

    /**
     * Reads all fields of entry and places them into Map
     * @param reader BufferedReader of file
     * @param data Map for arguments and their values
     * @throws IOException If unable to read next line
     */
    private void readStudentInfo(BufferedReader reader, Map<String, String> data) throws IOException {
        String line;
        while (!(line = reader.readLine()).matches("\\s+},?")) {
            String[] info = line.replaceAll("[\\s\"{},]","").split(":");
            data.putIfAbsent(info[0], info[1]);
        }
    }

    /**
     * Writes into text new field which key and value are from infoPair
     * @param i Counter of how many fields is left
     * @param infoPair Contains key and value divided by space
     * @param text StringBuilder with altered file
     */
    private void writeNewField(int i, String infoPair, StringBuilder text) {
        String argumentDgt = "\t\t\t\"%s\": %s";
        String argumentStr = "\t\t\t\"%s\": \"%s\"";

        String[] entry = infoPair.split(" ");

        if (entry[1].matches("\\d+")) {
            text.append(String.format(argumentDgt, entry[0], entry[1]));
        } else text.append(String.format(argumentStr, entry[0], entry[1]));
        if (i == 0) {
            text.append("\n");
        } else text.append(",\n");
    }

    /**
     * Writes line with newline at the end into StringBuilder
     * @param sb StringBuilder
     * @param line String to write
     */
    private void writeNewLine(StringBuilder sb, String line) {
        sb.append(line).append("\n");
    }

    /**
     * Parses ID from string of specified format
     * @param line String of specified format
     * @return ID
     */
    private int parseId(String line) {
        return Integer.parseInt(
                line.replaceAll(symbolsRegex, "").replace("id", "")
        );
    }
}
