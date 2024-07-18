package org.students;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

class FtpClientTest {

    @Test
    void setConn() throws IOException {
        String line = "TestUser 1234 localhost";
        String[] args = line.split(" ");
        FtpClient client = new FtpClient(args);
        Assertions.assertNotNull(client);
    }
}