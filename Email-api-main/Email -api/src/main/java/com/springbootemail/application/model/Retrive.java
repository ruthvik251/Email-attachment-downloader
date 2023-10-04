package com.springbootemail.application.model;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.mail.imap.IMAPMessage;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import javax.mail.*;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.Files.readAllBytes;

public class Retrive {
    private String saveDirectory;
    private String userName;
    private String password;
    private String domain;
    private String tenantid=null;
    private String clientid=null;
    private String client_secret=null;

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

   private Set<String> Email_set = new HashSet<String>();

    public Set<String> getEmail_set() {
        return Email_set;
    }

    public void setEmail_set(Set<String> email_set) {
        Email_set = email_set;
    }

    private String[] Email_list={"truthvikreddy251@gmail.com"};
    public Retrive() {
    }

    public String[] getEmail_list() {
        return Email_list;
    }

    public void setEmail_list(String[] email_list) {
        Email_list = email_list;
    }

    public Retrive(String saveDirectory, String userName, String password, String domain, String tenantid, String clientid, String client_secret) {
        this.saveDirectory = saveDirectory;
        this.userName = userName;
        this.password = password;
        this.domain = domain;
        this.tenantid = tenantid;
        this.clientid = clientid;
        this.client_secret = client_secret;
    }

    public String getSaveDirectory() {
        return saveDirectory;
    }

    public void setSaveDirectory(String saveDirectory) {
        this.saveDirectory = saveDirectory;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Retrive{" +
                "saveDirectory='" + saveDirectory + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", domain='" + domain + '\'' +
                ", tenantid='" + tenantid + '\'' +
                ", clientid='" + clientid + '\'' +
                ", client_secret='" + client_secret + '\'' +
                ", Email_list=" + Arrays.toString(Email_list) +
                '}';
    }

    public String textContent(Message message) throws MessagingException, IOException {

        Object content = message.getContent();

        String content_text = null;
        if (content instanceof String) {
            content_text = (String) content;
        } else {
            content_text = "no text message";
        }
        return content_text;
    }

    public void processMessageBody(Message message)
    {
        try {

            Object content = message.getContent();

            // check for string
            // then check for multipart

             if (content instanceof String)
            {
                System.out.println(content); }

            else if (content instanceof Multipart)
            { Multipart multiPart = (Multipart) content;
                processMultiPart(multiPart); }
            if (content instanceof InputStream )
            { InputStream inStream = (InputStream) content;
                int ch;
                while ((ch = inStream.read()) != -1)
                { System.out.write(ch); }
            }

        }
        catch (IOException e)
        { e.printStackTrace();
        }
        catch (MessagingException e)
        { e.printStackTrace(); }
    } public void processMultiPart(Multipart content)
    {
         Retrive retrive;
        try
        { for (int i = 0; i < content.getCount(); i++)
        {
            BodyPart bodyPart = content.getBodyPart(i);
            Object o;
            o = bodyPart.getContent();

            if (o instanceof String)
            { System.out.println("Text = " + o);
            } else if (null != bodyPart.getDisposition() && bodyPart.getDisposition().equalsIgnoreCase( Part.ATTACHMENT))
            { String fileName = bodyPart.getFileName();
                System.out.println("fileName = " + fileName);
                InputStream inStream = bodyPart.getInputStream();
                FileOutputStream outStream = new FileOutputStream(new File( getSaveDirectory()+ fileName));
                byte[] tempBuffer = new byte[4096];// 4 KB
                int numRead;
                while ((numRead = inStream.read(tempBuffer)) != -1)
                {
                    outStream.write(tempBuffer);
                }
                inStream.close();
                outStream.close(); }
            else if (null != bodyPart.getDisposition() && bodyPart.getDisposition().equalsIgnoreCase( Part.INLINE))
            { String fileName = bodyPart.getFileName();
                System.out.println("fileName = " + fileName);
                InputStream inStream = bodyPart.getInputStream();
                FileOutputStream outStream = new FileOutputStream(new File(getSaveDirectory()+ fileName));
                byte[] tempBuffer = new byte[4096];// 4 KB
                int numRead;
                while ((numRead = inStream.read(tempBuffer)) != -1)
                {
                    outStream.write(tempBuffer);
                }
                System.out.println("file sucessfully downloaded");
                inStream.close();
                outStream.close(); }
        }
        } catch (FileNotFoundException ex){
        ex.printStackTrace();
        System.out.println("could not determine file location");
    } catch (IOException e) {
            e.printStackTrace(); } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public String getAuthToken(String tanantId,String clientId,String client_secret) throws ClientProtocolException, IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost loginPost = new HttpPost("https://login.microsoftonline.com/" + tanantId + "/oauth2/v2.0/token");
        String scopes = "https://outlook.office365.com/.default";
        String encodedBody = "client_id=" + clientId + "&scope=" + scopes + "&client_secret=" + client_secret
                + "&grant_type=client_credentials";
        loginPost.setEntity(new StringEntity(encodedBody, ContentType.APPLICATION_FORM_URLENCODED));
        loginPost.addHeader(new BasicHeader("cache-control", "no-cache"));
        CloseableHttpResponse loginResponse = client.execute(loginPost);
        InputStream inputStream = loginResponse.getEntity().getContent();
        byte[] response = readAllBytes((Path) inputStream);
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType type = objectMapper.constructType(
                objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, String.class));
        Map<String, String> parsed = new ObjectMapper().readValue(response, type);
        return parsed.get("access_token");
    }




}
