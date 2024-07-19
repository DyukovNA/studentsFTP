package org.students;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            String JSONName = "students.json"; // Имя файла на сервере
            Console console = System.console();
            Scanner scanner = new Scanner(console.reader());
            console.writer().println("Please, enter your username, password and the server's IP-address:");
            String[] loginInfo = scanner.nextLine().split(" ");
            ConsoleClient consoleClient = new ConsoleClient(loginInfo, JSONName);
            console.writer().println("Connection successful");
            console.writer().println("1.\tGet list of students by name\n" +
                    "2.\tGet student info by id \n" +
                    "3.\tAdd student to list\n" +
                    "4.\tDelete student by id\n" +
                    "5.\tExit\n"
            );
            int action;
            do {
                System.out.println("Select an action:");
                action = scanner.nextInt();
                consoleClient.menu(action, scanner);
            } while (action != 5);
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}