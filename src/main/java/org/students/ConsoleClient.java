package org.students;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class ConsoleClient {
    String link;
    String JSONName;
    public ConsoleClient(String[] loginInfo, String filename){
        link = "ftp://" + loginInfo[0] + ":" + loginInfo[1] + "@" + loginInfo[2] + "/";
        JSONName = filename;
    }

    private URLConnection setConnection(String filename) {
        try {
            URL url = new URL(link + filename);
            URLConnection connection = url.openConnection();
            connection.connect();
            return connection;
        } catch (IOException e) {
//            System.out.println("Unable to connect to server");
            e.printStackTrace();
        }
        return null;
    }
    public void menu(Integer action, Scanner scanner) throws IOException {
        switch (action) {
            case 1: {
                List<String> listOfStudents = getListOfStudents();
                listOfStudents.forEach(System.out::println);
                break;
            }
            case 2: {
                System.out.println("Please, student's ID:");
                String id = scanner.next();
                System.out.println(getStudentInfo(id));
                break;
            }
            case 3: {
                System.out.println("Please, enter student's name:");
                String name = scanner.next();
                if (addStudent(name)) {
                    System.out.println("Student successfully added");
                }
                break;
            }
            case 4: {
                System.out.println("Please, enter student's ID");
                int idDelete = scanner.nextInt();
                if (deleteStudent(Integer.toString(idDelete))) {
                    System.out.println("Student successfully removed");
                }
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
    public List<String> getListOfStudents() throws IOException {
        URLConnection connection = setConnection(JSONName);
        assert connection != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        List<String> students = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (line.contains("name")) {
                students.add(
                        line.replaceAll("[\\s:\"]","").replace("name", "")
                );
            }
        }
        connection.getInputStream().close();
        Collections.sort(students);
        return students;
    }
    public String getStudentInfo(String id) throws IOException {
        URLConnection connection = setConnection(JSONName);
        assert connection != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        String toFind = "id" + id;
        while ((line = reader.readLine()) != null) {
            if (line.contains("id")) {
                String s =  line.replaceAll("[\\s:\"{},]","");
                if (s.equals(toFind)) {
                    return reader.readLine().replaceAll(
                            "[\\s:\"{},]","").replace("name", ""
                    );
                }
            }
        }
        connection.getInputStream().close();
        return "No information found";
    }
    public boolean addStudent(String info) throws IOException {
        URLConnection connectionUpload = setConnection(JSONName);
        URLConnection connection = setConnection(JSONName);
        assert connection != null;
        assert connectionUpload != null;

        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(inputStreamReader);

        OutputStream outputStream = connectionUpload.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        String line;
        StringBuilder text = new StringBuilder();
        int maxId = 0;

        while ((line = reader.readLine()) != null) {
            if (line.matches("\\s+\"id\": \\d+,")) {
                maxId = Integer.parseInt(
                        line.replaceAll("[\\s:\"{},]", "").replace("id", "")
                );
                text.append(line).append("\n");
            } else if (line.matches("\\s+}")) {
                maxId += 1;
                text.append(line).append(",\n");
                text.append("    {\n      \"id\": ")
                        .append(maxId).append(",\n      \"name\": \"")
                        .append(info).append("\"\n    }\n  ]\n}");
                break;
            } else text.append(line).append("\n");
        }

        writer.write(text.toString());
        writer.close();
        reader.close();
        return true;
    }
    public boolean deleteStudent(String id) throws IOException {
        URLConnection connectionUpload = setConnection(JSONName);
        URLConnection connection = setConnection(JSONName);

        assert connection != null;
        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(inputStreamReader);

        assert connectionUpload != null;
        OutputStream outputStream = connectionUpload.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        String line;
        StringBuilder text = new StringBuilder();
        StringBuilder studentBlock = new StringBuilder();
        Integer currId;
        boolean isRemoved = false;
        while ((line = reader.readLine()) != null) {
            if (line.matches("\\s+\\{")) {
                text.append(studentBlock);
                studentBlock = new StringBuilder();
                studentBlock.append(line).append("\n");
            } else if (line.matches("\\s+\"name\": .+") || line.matches("\\s+},")) {
                studentBlock.append(line).append("\n");
            } else if (line.matches("\\s+}")) {
                studentBlock.append(line).append("\n");
                text.append(studentBlock);
            } else if (line.matches("\\s+\"id\": \\d+,")) {
                currId = Integer.valueOf(
                        line.replaceAll("[\\s:\"{},]", "").replace("id", "")
                );
                if (isRemoved) {
                    currId -= 1;
                    studentBlock.append("      \"id\": ").append(currId).append(",\n");
                } else if (Integer.parseInt(id) == currId) {
                    for (int i = 0; i < 2; i++) {
                        line = reader.readLine();
                    }
                    if (line.matches("\\s+}")) {
                        if (text.indexOf("},") != -1) {
                            text.replace(text.length()-5, text.length()-1, "}");
                        }
                        text.append("  ]\n}");
                        isRemoved = true;
                        break;
                    }
                    reader.readLine();
                    isRemoved = true;
                } else {
                    studentBlock.append(line).append("\n");
                }
            } else text.append(line).append("\n");
        }
        writer.write(text.toString());
        writer.close();
        reader.close();
        return isRemoved;
    }
}
