/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    
    public static final int PORT = 9999;
    
    public static final String WEB_DIR = "web";

    
    public static void main(String[] args) {
        boolean running = true;
        try (ServerSocket ss = new ServerSocket(PORT);) {
            while (running) {
                try (
                    Socket s = ss.accept();
                    Reader sr = new InputStreamReader(s.getInputStream(), "UTF-8");
                    BufferedReader br = new BufferedReader(sr);
                    Writer sw = new OutputStreamWriter(s.getOutputStream(), "UTF-8");
                    BufferedWriter bw = new BufferedWriter(sw);
                ) {
//                    String line;
//                    do {
//                        line = br.readLine();
//                        if (line != null) {
//                            System.out.println(line); 
//                        }
//                    } while (line != null && !"".equals(line));
                    String firstLine = br.readLine();
                    if (firstLine != null) {
                        String[] parts = firstLine.split(" ");
                        if (parts.length != 3 || !"HTTP/1.1".equals(parts[2])) {
                            bw.write("HTTP/1.1 400 Bad Request\r\n");
                            bw.write("\r\n");
                        } else {
                            if ("/end".equals(parts[1])) {
                                bw.write("HTTP/1.1 200 OK\r\n");
//                                bw.write("Content-Type: text/html");
                                bw.write("\r\n");
                                bw.write("<html><body><h1>Bye.Server is down.</h1></body></html>");
                                running = false;
                            } else {
                                String fileName = WEB_DIR + parts[1];
                                File f = new File(fileName);
                                if (f.exists()) {
                                    if (f.isDirectory()) {
                                        String content = "<html><body>";
                                        content += "<h1><i>Directory " + parts[1] + "</i></h1>";
                                        if (!"/".equals(parts[1])) {
                                            content += "<a href=\"..\">DIR UP</a><br>\r\n";
                                        }
                                        for (File file : f.listFiles()) {
                                            String path = file.getAbsolutePath().substring(new File(WEB_DIR).getAbsolutePath().length());
                                            content += "<a href=\"" + path + "\">" + file.getName() + "</a><br>\r\n";  
                                        }
                                        content += "</body></html>";
                                        bw.write("HTTP/1.1 200 OK\r\n");
                                        bw.write("\r\n");
                                        bw.write(content);
                                        System.out.println(content);
                                        bw.flush();
                                    } else {
                                        try (
                                            FileInputStream fis = new FileInputStream(f);
                                            Reader fr = new InputStreamReader(fis, "UTF-8");
                                            BufferedReader fbr = new BufferedReader(fr);
                                        ){
                                            String content = "";
                                            String fileLine;
                                            while ((fileLine = fbr.readLine()) != null) {
                                                content += fileLine + "\r\n";
                                            }
                                            bw.write("HTTP/1.1 200 OK\r\n");
//                                          bw.write("Content-Type: text/html\r\n");
                                            bw.write("\r\n");
                                            bw.write(content);
                                            bw.flush();
                                        }
                                        catch (Exception ex) {
                                            bw.write("HTTP/1.1 500 Internal Server Error\r\n");
                                            bw.write("\r\n");  
                                        }
                                    }
                                } else {
                                    bw.write("HTTP/1.1 404 Not Found\r\n");
                                    bw.write("\r\n"); 
                                }
                            }      
                        }
                    } else {
                        bw.write("HTTP/1.1 400 Bad Request\r\n");
                        bw.write("\r\n");    
                    }
                }
                catch (IOException ex) {
                    System.out.println(ex);
                }
            }
        }
        catch (IOException ex) {
            System.out.println("Port" + PORT + "already in use");
        }
    }
}
