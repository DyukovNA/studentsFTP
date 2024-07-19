package org.students;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;

public class ConsoleClient {
//    URLConnection connection;
    String link;
    String JSONName = "students.json";
    public ConsoleClient(String[] loginInfo){
        link = "ftp://" + loginInfo[0] + ":" + loginInfo[1] + "@" + loginInfo[2] + "/";
        System.out.println(link);
        try {
            deleteStudent("1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private URLConnection setConnection() {
        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.connect();
//            System.out.println(getStudentInfo("2"));
            return connection;
        } catch (IOException e) {
//            System.out.println("Unable to connect to server");
            e.printStackTrace();
        }
        return null;
    }
    private URLConnection setConnectionFile(String filename) {
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
    public void menu(Integer action, String data) {
        try {
            URLConnection connection = setConnectionFile(JSONName);
            switch (action) {
                case 1:
                    getListOfStudents();
                    connection.getInputStream().close();
                case 2:
                    getStudentInfo(data);
                    connection.getInputStream().close();
                case 3:
                    addStudent(data);
                    connection.getInputStream().close();
                case 4:
                    deleteStudent(data);
                    connection.getInputStream().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public List<String> getListOfStudents() throws IOException {
        URLConnection connection = setConnectionFile(JSONName);
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
        URLConnection connection = setConnectionFile(JSONName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = null;
        String toFind = "id" + id;
        while ((line = reader.readLine()) != null) {
            if (line.contains("id")) {
                String s =  line.replaceAll("[\\s:\"{},]","");
                if (s.equals(toFind)) {
                    return reader.readLine().replaceAll("[\\s:\"{},]","").replace("name", "");
                }
            }
        }
        connection.getInputStream().close();
        return "No information found";
    }
    public boolean addStudent(String info) throws IOException {
        URLConnection connectionUpload = setConnectionFile(JSONName);
        URLConnection connection = setConnectionFile(JSONName);

        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(inputStreamReader);

        OutputStream outputStream = connectionUpload.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        String line = null;
        String text = "";
        String pattern = "\\s+\"id\": \\d+,";
        Integer maxId = 0;
        while ((line = reader.readLine()) != null) {
            if (Pattern.matches(pattern, line)) {
                maxId = Integer.valueOf(
                        line.replaceAll("[\\s:\"{},]","").replace("id", "")
                );
                text += line + "\n";
            } else if (line.matches("\\s+}")) {
                maxId += 1;
                text += line + ",\n";
                text += "    {\n      \"id\": " + maxId + ",\n      \"name\": "
                        + '\"' + info + '\"' + "\n    }\n  ]\n}";
                break;
            } else text += line + "\n";;
        }

        writer.write(text);
        writer.close();
        reader.close();
        return true;
    }
    public boolean deleteStudent(String id) throws IOException {
        URLConnection connectionUpload = setConnectionFile(JSONName);
        URLConnection connection = setConnectionFile(JSONName);

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
                    line = reader.readLine();
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
