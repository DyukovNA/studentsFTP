package org.students;

import java.io.IOException;
import java.net.URLConnection;

public class ConsoleClient {
    URLConnection connection;
    String JSONName = "Students.json";
    public ConsoleClient(String[] loginInfo) {
        FtpClient ftp = new FtpClient(loginInfo);
        try {
            connection = ftp.setConn();
            connection.connect();
        } catch (IOException e) {
            System.out.println("Unable to connect to server");
        }
    }
    public void menu(Integer action, String data) {
        switch (action) {
            case 1:
                getListOfStudents(data);
            case 2:
                getStudentInfo(data);
            case 3:
                addStudent(data);
            case 4:
                deleteStudent(data);
        }
    }
    public void getListOfStudents(String id) {

    }
    public void getStudentInfo(String id) {

    }
    public void addStudent(String info) {

    }
    public void deleteStudent(String id) {

    }
    private void getJSON() {

    }
}
