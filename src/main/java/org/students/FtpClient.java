package org.students;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

class FtpClient {
    String link = "";
    public FtpClient(String[] loginInfo) {
        link = "ftp:"+loginInfo[0]+":"+loginInfo[1]+"@"+loginInfo[2]+"/";
    }
    public URLConnection setConn()  throws IOException {
        URL url = new URL(link);
        return url.openConnection();
    }
}
