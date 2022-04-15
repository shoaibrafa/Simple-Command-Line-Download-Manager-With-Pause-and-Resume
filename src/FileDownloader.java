import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class FileDownloader {

    private Long remoteFileSize;
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
        String fileName = FILE_URL.substring(FILE_URL.lastIndexOf("/") + 1);
        file = new File(fileName);
        if (file.exists()) {
            Long localFileSize = file.length();
            return true;
        }
        return false;
    }

    public void startNewDownload() {
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream())){
                start(bufferedInputStream, fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resumeDownload() {
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setRequestProperty("Range", "bytes=" + file.length() + "-");
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
            byte[] dataBuffer = new byte[1024];
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
            System.out.print("Exiting....!");
            System.out.print("Closing Connections...!");
            urlConnection.disconnect();
        }
    }
}
