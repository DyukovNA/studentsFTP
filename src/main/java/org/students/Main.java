package org.students;

import java.io.Console;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
/*        Console console = System.console();
        Scanner scanner = new Scanner(console.reader());
        console.writer().println("Please, enter your username, password and the server's IP-address");
        String[] loginInfo = scanner.nextLine().split(" ");
        ConsoleClient consoleClient = new ConsoleClient(loginInfo);
        console.writer().println("Connection successful");
        console.writer().println("1.\tGet list of students by name\n" +
                "2.\tGet student info by id \n" +
                "3.\tAdd student to list\n" +
                "4.\tDelete student by id\n" +
                "5.\tExit\n"
        );
        Integer action = scanner.nextInt();
        if (action < 5 && scanner.hasNext()) {
            consoleClient.menu(action, scanner.next());
        } else if (action == 5) {
            console.writer().println("Exiting program");
        } else {
            console.writer().println("Unexpected input");

        }*/
        try {
            String[] loginInfo = "TestUser 1234 0.0.0.0".split(" ");
            ConsoleClient consoleClient = new ConsoleClient(loginInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}