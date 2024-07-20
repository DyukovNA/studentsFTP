/**
 *
 */
package org.students;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class ConsoleClient {
    String link;
    String JSONName;
    private static final String idRegex = "\\s+\"id\": \\d+,";
    private static final String argumentRegex = "\\s+\"[\\w\\d]+\":\\s+\"?[\\w\\d\\s]+\"?,?";
    private static final String symbolsRegex = "[\\s:\"{},]";
    private static final String endOfLastBlockRegex = "\\s+}";
    private static final String beginOfNewBoxRegex = "\\s+\\{";
    public ConsoleClient(String[] loginInfo, String filename){
        link = "ftp://" + loginInfo[0] + ":" + loginInfo[1] + "@" + loginInfo[2] + "/";
        JSONName = filename;
    }

    public void menu(Integer action, Console console) throws IOException {
        switch (action) {
            case 1: {
                showListOfStudents();
                break;
            }
            case 2: {
                showStudentInfo(console);
                break;
            }
            case 3: {
                showAddStudent(console);
                break;
            }
            case 4: {
                showDeleteStudent(console);
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

    private void showDeleteStudent(Console console) throws IOException {
        System.out.println("Please, enter student's ID");
        Scanner scanner = new Scanner(console.reader());
        int idDelete = scanner.nextInt();
        if (deleteStudent(Integer.toString(idDelete))) {
            System.out.println("Student successfully removed");
        }
    }

    private void showAddStudent(Console console) throws IOException {
        System.out.println("Please, enter student's information:\n" +
                "Note: enter arguments and their values in pairs. " +
                "Pairs are divided by space and comma (', ')." +
                "Argument and its value are divided by space\n" +
                "For example: name Alex, age 20");
        Scanner scanner = new Scanner(console.reader());
        String info = scanner.nextLine();
        if (!(info.matches("\\w+ [\\w\\d]+(, \\w+ [\\w\\d]+)+") || info.matches("\\w+ [\\w\\d]+"))) {
            System.out.println("Invalid input");
            return;
        }
        if (addStudent(info)) {
            System.out.println("Student successfully added");
        } else System.out.println("Unexpected error occurred");
    }

    private void showStudentInfo(Console console) throws IOException {
        System.out.println("Please, student's ID:");
        Scanner scanner = new Scanner(console.reader());
        String id = scanner.nextLine();
        System.out.println(getStudentInfo(id));
    }

    private void showListOfStudents() throws IOException {
        List<String> listOfStudents = getSortedListOfStudents();
        listOfStudents.forEach(System.out::println);
    }

    private BufferedReader getReader() throws IOException {
        URLConnection connection = setConnection(JSONName);
        assert connection != null;
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    private BufferedWriter getWriter() throws IOException {
        URLConnection connection = setConnection(JSONName);
        assert connection != null;
        OutputStream outputStream = connection.getOutputStream();
        return new BufferedWriter(new OutputStreamWriter(outputStream));
    }

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

    public List<String> getSortedListOfStudents() throws IOException {
        List<String> students = new ArrayList<>();
        getListOfStudents(students);

        Collections.sort(students);
        return students;
    }

    public String getStudentInfo(String id) throws IOException {
        String idToFind = "id" + id;
        Map<String, String> data = new LinkedHashMap<>();

        findStudentGetInfo(idToFind, data);
        if (data.isEmpty()) {
            return "No information found";
        }

        return data.toString().replaceAll("[{}]", "");
    }

    public boolean addStudent(String info) throws IOException {
        BufferedReader reader = getReader();
        BufferedWriter writer = getWriter();

        int maxId = 0;
        String line;
        StringBuilder text = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.matches(idRegex)) {
                maxId = parseId(line);
                writeNewLine(text, line);
            } else if (line.matches(endOfLastBlockRegex)) {
                maxId += 1;
                writeNewStudent(maxId, text, line, info);
                break;
            } else writeNewLine(text, line);
        }

        writer.write(text.toString());
        writer.close();
        reader.close();
        return true;
    }

    public boolean deleteStudent(String idToRemove) throws IOException {
        BufferedReader reader = getReader();
        BufferedWriter writer = getWriter();

        String line;
        StringBuilder text = new StringBuilder();
        StringBuilder studentBlock = new StringBuilder();
        boolean isRemoved = false;
        
        while ((line = reader.readLine()) != null) {
            if (line.matches(beginOfNewBoxRegex)) {
                text.append(studentBlock);
                studentBlock = new StringBuilder();
                writeNewLine(studentBlock, line);
            } else if (line.matches(endOfLastBlockRegex)) {
                writeNewLine(studentBlock, line);
                text.append(studentBlock);
            } else if (line.matches(idRegex)) {
                isRemoved = analyseId(idToRemove, line, isRemoved, text, studentBlock, reader);
            } else if (
                    line.matches(argumentRegex) || line.matches("\\s+},")
            ) {
                writeNewLine(studentBlock, line);
            } else
                writeNewLine(text, line);
        }
        
        writer.write(text.toString());
        writer.close();
        reader.close();
        return isRemoved;
    }

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

    private void writeNewStudent(int maxId, StringBuilder text, String line, String info) {
        String[] infoPairs = info.split(", ");
        int i = infoPairs.length;

        text.append(line).append(",\n\t\t{\n");
        text.append("\t\t\t\"id\": ").append(maxId).append(",\n");

        for (String infoPair: infoPairs) {
            i-=1;
            writeNewEntry(i, infoPair, text);
        }
        text.append("\t\t}\n\t]\n}");
    }

    private boolean analyseId(
            String idToRemove, String line, boolean isRemoved, StringBuilder text,
            StringBuilder studentBlock, BufferedReader reader
    ) throws IOException {
        int currId;
        currId = parseId(line);
        if (isRemoved) {
            currId -= 1;
            writeNewLine(studentBlock, "\t\t\t\"id\": " + currId + ",");
        } else if (Integer.parseInt(idToRemove) == currId) {
            isRemoved = skipStudent(line, reader, text);
        } else {
            writeNewLine(studentBlock, line);
        }
        return isRemoved;
    }

    private boolean skipStudent(
            String line, BufferedReader reader, StringBuilder text
    ) throws IOException {
        while (line.matches(argumentRegex)) {
            line = reader.readLine();
        }
        if (line.matches(endOfLastBlockRegex)) {
            if (text.indexOf("},") != -1) {
                text.replace(text.length()-5, text.length()-1, "\t}");
            }
//            text.append("\t]\n}");
            return true;
        }
        reader.readLine();
        return true;
    }

    private void readStudentInfo(BufferedReader reader, Map<String, String> data) throws IOException {
        String line;
        while (!(line = reader.readLine()).matches("\\s+},?")) {
            String[] info = line.replaceAll("[\\s\"{},]","").split(":");
            data.putIfAbsent(info[0], info[1]);
        }
    }

    private void writeNewEntry(int i, String infoPair, StringBuilder text) {
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

    private void writeNewLine(StringBuilder sb, String line) {
        sb.append(line).append("\n");
    }

    private int parseId(String line) {
        return Integer.parseInt(
                line.replaceAll(symbolsRegex, "").replace("id", "")
        );
    }
}
