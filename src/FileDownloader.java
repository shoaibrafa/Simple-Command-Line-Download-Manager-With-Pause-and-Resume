import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class FileDownloader {

    private Long remoteFileSize;
    private Long localFileSize;
    private String fileName;
    private File file;
    private String FILE_URL;
    private HttpURLConnection urlConnection;
    private URL url;
    private boolean retry = true;

    public void getURL(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter File Url: ");
        this.FILE_URL = scanner.nextLine();
        System.out.println("Starting Download...");
        startProcess();
    }
    public void startProcess() {
        try {
            url = new URL(FILE_URL);
            if (getFileInfo()) {
                resumeDownload();
            } else {
                startNewDownload();
            }
        } catch (MalformedURLException e) {
            System.out.println("Bad URL...!");
        }
    }

    public boolean getFileInfo() {
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            remoteFileSize = urlConnection.getContentLengthLong();
            urlConnection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileName = FILE_URL.substring(FILE_URL.lastIndexOf("/") + 1);
        file = new File(fileName);
        if (file.exists()) {
            localFileSize = file.length();
            return true;
        }
        return false;
    }

    public void startNewDownload() {
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(1000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File Does Not Exist...!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unknown Error Occurred...!");
        }

        try(
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream())){
                start(bufferedInputStream, fileOutputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resumeDownload() {
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(1000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setRequestProperty("Range", "bytes=" + file.length() + "-");

            urlConnection.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream())) {
            start(bufferedInputStream, fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void start(BufferedInputStream bufferedInputStream, FileOutputStream fileOutputStream) {
        try {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                System.out.print(((file.length() * 100) / remoteFileSize) + "%" + " Bytes Read: " + bytesRead);
                System.out.print("\r");
            }
            System.out.println("Download Finished!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            System.out.printf("Exiting....!");
            System.out.printf("Closing Connections...!");
            urlConnection.disconnect();
        }
    }
}
