package org.students;

import java.io.*;
import java.net.URLConnection;
import java.util.*;

/**
 * This class includes all methods to work with FTP-server and the file on it
 */
public class ConsoleClient {
    private final ConnectionHandler connectionHandler;
    private final TextEditor textEditor;
    /**
     * Builds an FTP-link from the info provided by user in Main
     * @param connectionHandler Entity to interact with FTP-server
     */
    public ConsoleClient(ConnectionHandler connectionHandler){
        this.connectionHandler = connectionHandler;
        this.textEditor = new TextEditor();
    }

    /**
     * Calls methods according to the number of action provided by user
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection in any of methods
     * @see ConnectionHandler
     */
    public void menu(Scanner scanner) throws IOException {
        int action;

        System.out.println("1.\tGet list of students by name\n" +
                "2.\tGet student's info by id \n" +
                "3.\tAdd student to list\n" +
                "4.\tDelete student by id\n" +
                "5.\tExit\n"
        );

        do {
            System.out.println("Select an action:\n");
            action = scanner.nextInt();
            switch (action) {
                case 1: {
                    showSortedStudentList();
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
        } while (action != 5);
    }

    /**
     * Checks if list of students is empty. Prints out list or error message
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see ConnectionHandler
     */
    private void showSortedStudentList() throws IOException {
        String toReturn = getSortedListOfStudents();
        if (toReturn.equals("")) {
            System.out.println("No students found");
            return;
        }
        System.out.println(toReturn);
    }

    /**
     * Interacts with user to get ID of the student whose information is needed.
     * Calls getStudentInfo() method to get data of student with given ID.
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see ConnectionHandler
     */
    private void showStudentInfo() throws IOException {
        System.out.println("Please, enter student's ID:");
        Scanner scanner = new Scanner(System.in);
        String id = scanner.nextLine();
        System.out.println(getStudentInfo(id));
    }

    /**
     * Interacts with user to get information about the student that needs to be added to the file.
     * Calls addStudent() method to add student with given arguments.
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see ConnectionHandler
     * @see TextEditor
     */
    private void showAddStudent() throws IOException {
        System.out.println("Please, enter student's information:\n\n" +
                "Note: enter arguments and their values in pairs. " +
                "Pairs are divided by space and comma (', '). \n" +
                "Argument and its value are divided by space\n" +
                "For example: name Alex, age 20\n");
        Scanner scanner = new Scanner(System.in);
        String info = scanner.nextLine();
        if (!textEditor.isValidInput(info)) {
            System.out.println("Invalid input");
            return;
        }
        addStudent(info);
        System.out.println("Student successfully added");
    }

    /**
     * Interacts with user to get ID of the student that needs to be removed from the file.
     * Calls deleteStudent() method to remove student with given ID.
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see ConnectionHandler
     */
    private void showDeleteStudent() throws IOException {
        System.out.println("Please, enter student's ID");
        Scanner scanner = new Scanner(System.in);
        int idDelete = scanner.nextInt();
        deleteStudent(Integer.toString(idDelete));
        System.out.println("Student successfully removed");
    }

    /**
     * Sorts list of students in alphabetical order and formats it into string
     * @return String with numbered list
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     */
    public String getSortedListOfStudents() throws IOException {
        List<String> students = getListOfStudents();
        Collections.sort(students);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < students.size(); i++) {
            sb.append(i + 1).append(". ").append(students.get(i));
            if (i != students.size() - 1) sb.append(",\n");
        }
        return sb.toString();
    }

    /**
     * Finds student with specified ID if file and returns all information written in file
     * @param id ID of student
     * @return String with all information about the student in file
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see ConnectionHandler
     */
    public String getStudentInfo(String id) throws IOException {
        Map<String, String> data = new LinkedHashMap<>();

        findStudentGetInfo(id, data);
        if (data.isEmpty()) {
            return "No information found";
        }

        return data.toString().replaceAll("[{}]", "").replace("=", ": ");
    }

    /**
     * Writes information about student into file. Generates ID.
     * @param info String of specified format with information about student
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see ConnectionHandler
     * @see TextEditor
     */
    public void addStudent(String info) throws IOException {
        writeHeaderIfEmpty();

        StringBuilder text = new StringBuilder();
        writeStudentToSB(text, info);
        BufferedWriter writer = connectionHandler.getWriter();

        writer.write(text.toString());
        writer.close();
    }

    /**
     * Removes record of student with given ID by rewriting whole file and skipping unneeded part.
     * Changes IDs if necessary.
     * @param idToRemove ID of student
     * @throws IOException If is unable to get InputStream or OutputStream from URLConnection
     * @see ConnectionHandler
     * @see TextEditor
     */
    public void deleteStudent(String idToRemove) throws IOException {
        StringBuilder text = new StringBuilder();
        writeWithoutStudent(text, idToRemove);

        BufferedWriter writer = connectionHandler.getWriter();

        writer.write(text.toString());
        writer.close();
    }

    /**
     * Fills empty list with names found in file
     * @throws IOException If is unable to get InputStream from URLConnection
     * @see URLConnection
     * @see TextEditor
     */
    public List<String> getListOfStudents() throws IOException {
        List<String> students = new ArrayList<>();
        BufferedReader reader = connectionHandler.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(("\"name\":"))) {
                students.add(
                        textEditor.clean(line).replace("name:", "")
                );
            }
        }
        reader.close();
        return students;
    }

    /**
     * Finds student in file by provided ID and reads all its fields
     * @param idToFind Provided ID
     * @param data Map for arguments and their values
     * @throws IOException If is unable to get InputStream from URLConnection
     * @see URLConnection
     * @see TextEditor
     */
    private void findStudentGetInfo(String idToFind, Map<String, String> data) throws IOException {
        BufferedReader reader = connectionHandler.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            if (textEditor.isId(line)) {
                String s = String.valueOf(textEditor.parseId(line));
                if (s.equals(idToFind)) {
                    while (!line.matches("\\s+},?")) {
                        String[] info = textEditor.clean(line).split(":");
                        data.putIfAbsent(info[0], info[1]);
                        line = reader.readLine();
                    }
                }
            }
        }
        reader.close();
    }

    /**
     * Writes formatted information about student into StringBuilder
     * @param text Where to write information
     * @param info Information about student
     * @throws IOException If unable to work with BufferedReader
     */
    private void writeStudentToSB(StringBuilder text, String info) throws IOException {
        BufferedReader reader = connectionHandler.getReader();

        int maxId = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            if (textEditor.isId(line)) {
                // Max value of id read to this moment
                maxId = textEditor.parseId(line);
                textEditor.writeNewLine(text, line);
            } else if (textEditor.isEndOfLastEntry(line)) {
                // If it is last record insert new record with ID += 1
                maxId += 1;
                textEditor.writeNewLine(text, line + ",");
                textEditor.writeNewStudent(maxId, text, info);
                break;
            } else if (line.matches("\\s+]")) {
                maxId += 1;
                textEditor.writeNewStudent(maxId, text, info);
                break;
            }
            else textEditor.writeNewLine(text, line);
        }

        reader.close();
    }

    /**
     * Rewrites file into StringBuilder while ignoring student with idToRemove
     * @param text Where to write altered file
     * @param idToRemove ID of student that needs ti be removed
     * @throws IOException If unable to work with BufferedReader
     */
    private void writeWithoutStudent(StringBuilder text, String idToRemove) throws IOException {
        BufferedReader reader = connectionHandler.getReader();

        String line;
        StringBuilder studentBlock = new StringBuilder();
        boolean isRemoved = false;

        while ((line = reader.readLine()) != null) {
            if (textEditor.isBeginning(line)) {
                if (!textEditor.isBeginning(studentBlock.toString().replaceAll("\n", ""))) {
                    text.append(studentBlock);
                }
                studentBlock = new StringBuilder();
                textEditor.writeNewLine(studentBlock, line);
            } else if (textEditor.isEndOfLastEntry(line)) {
                textEditor.writeNewLine(studentBlock, line);
                text.append(studentBlock);
            } else if (textEditor.isId(line)) {
                isRemoved = analyseId(idToRemove, line, isRemoved, text, studentBlock, reader);
            } else if (textEditor.isField(line) || textEditor.isEndOfEntry(line)) {
                textEditor.writeNewLine(studentBlock, line);
            } else
                textEditor.writeNewLine(text, line);
        }

        reader.close();
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
        currId = textEditor.parseId(line);
        if (Integer.parseInt(idToRemove) == currId && !isRemoved) {
            skipStudent(line, reader, text);
            isRemoved = true;
        } else if (isRemoved) {
            currId -= 1;
            textEditor.writeNewLine(studentBlock, "\t\t\t\"id\": " + currId + ",");
        } else {
            textEditor.writeNewLine(studentBlock, line);
        }
        return isRemoved;
    }

    /**
     * Skips lines of file that contain fields of student entry
     * @param line Last line read by BufferedReader
     * @param reader BufferedReader of file
     * @param text StringBuilder with altered file
     */
    private void skipStudent(
            String line, BufferedReader reader, StringBuilder text
    ) throws IOException {
        while (textEditor.isField(line)) {
            line = reader.readLine();
        }
        if (line.matches(TextEditor.endOfLastEntryRegex)) {
            if (text.indexOf("},") != -1) {
                text.replace(text.length()-5, text.length()-1, "\t\t}");
            }
        }
    }

    /**
     * If file is empty writes empty json list "students" into file
     * @throws IOException If unable to read next line
     */
    private void writeHeaderIfEmpty() throws IOException {
        String header = getHeaderIfEmpty();
        if (!header.equals("")) {
            BufferedWriter writerHeader = connectionHandler.getWriter();
            writerHeader.write(header);
            writerHeader.flush();
            writerHeader.close();
        }
    }

    /**
     * Checks if file is empty, if so returns empty json list "students"
     * @throws IOException If unable to read next line
     */
    private String getHeaderIfEmpty() throws IOException {
        BufferedReader reader = connectionHandler.getReader();
        if (reader.readLine() == null) {
            return "{\n\t\"students\": [\n\t]\n}";
        }
        reader.close();
        return "";
    }
}
