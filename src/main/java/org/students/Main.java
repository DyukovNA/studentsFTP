/**
 * Test task for an internship at Infotecs.
 * <p>Console application, that provides access to a .json file on an FTP-server.
 * Allows to alter the file and get information about the students listed in it. </p>
 */
package org.students;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Main class. Prints out menu for user to select an action. Once an action is selected calls menu method from
 * ConsoleClient. Handles exceptions that ConsoleClient throws.
 * <p>The wording of the task states, that app takes as arguments only information to connect to an FTP-server.
 * Name of the .json file is not one of the arguments provided to the app. As a result the name is hardcoded.</p>
 * @see ConsoleClient
 */
public class Main {
    /**
     * Name of the file on an FTP-server.
     */
    private static final String JSONName = "students.json"; // Имя файла на сервере
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please, enter your username, password and the server's IP-address:");
            // TestUser 1234 0.0.0.0
            String[] loginInfo = scanner.nextLine().split(" ");
            ConsoleClient consoleClient = new ConsoleClient(loginInfo, JSONName);
            System.out.println("Connection successful");
            System.out.println("1.\tGet list of students by name\n" +
                    "2.\tGet student's info by id \n" +
                    "3.\tAdd student to list\n" +
                    "4.\tDelete student by id\n" +
                    "5.\tExit\n"
            );
            int action;
            do {
                System.out.println("Select an action:");
                action = scanner.nextInt();
                consoleClient.menu(action);
            } while (action != 5);
        }  catch (IOException e) {
            System.out.println("An error occurred");
            e.printStackTrace();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input");
        }
    }
}