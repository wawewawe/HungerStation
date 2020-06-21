import io.qameta.allure.Description;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.xml.parsers.ParserConfigurationException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.qameta.allure.Step;



public class Controller {

    public static String statusCode;
    public static int stCode;
    public static String stPayload;
    public static String payload;
    public static String ReqType;
    public static String apiURL;
    public static List<String> ExceptionArray = new ArrayList<String>();
    public static String ReqDescr;
    public static String appType;
    public static String boundary;


    @Test
    @Description("Smoke Test")
    public void prepareAPIRequests()
            throws BiffException, IOException, JSONException, SAXException, ParserConfigurationException {

        String FilePath = "src/api.xls";
        FileInputStream fs = new FileInputStream(FilePath);
        Workbook wb = Workbook.getWorkbook(fs);

        // TO get the access to the sheet
        Sheet sh = wb.getSheet("Live");

        // To get the number of rows present in sheet
        int totalNoOfRows = sh.getRows();

        // To get the number of columns present in sheet
        int totalNoOfCols = sh.getColumns();

        for (int row = 0 + 1; row <= totalNoOfRows - 1; row++) {
            for (int col = 0; col < totalNoOfCols; col++) {
                switch (col) {
                    case 0:
                        apiURL = sh.getCell(col, row).getContents();
                        break;
                    case 1:
                        ReqType = sh.getCell(col, row).getContents();
                        break;
                    case 2:
                        payload = sh.getCell(col, row).getContents().replaceAll("^\"|\"$", "");
                        break;
                    case 3:
                        statusCode = sh.getCell(col, row).getContents();
                        break;
                    case 4:
                        ReqDescr = sh.getCell(col, row).getContents();
                        break;

                }
            }

            try {
                getResponseCode();
            } catch (java.net.ConnectException e) {
                ExceptionArray.add(ReqDescr + " : FAILED" + " And the response code is : " + stCode +  " instead of " + statusCode );
            }
            if (stCode == Integer.parseInt(statusCode)) {
                System.out.println(ReqDescr + " : Passed" + " And the expected response code is : " + statusCode + "\n");
            } else {
                ExceptionArray.add(ReqDescr + " : FAILED" + " And the expected response code is : " + statusCode);
                System.out.println(ReqDescr + " : FAILED" + " And the response code is : " + stCode +  " instead of " + statusCode + "\n");
            }
        }
    }

    @Step("Type {Controller.apiURL} / {Controller.ReqDescr}.")
    public static void getResponseCode() throws MalformedURLException, IOException, JSONException {

        try {

            URL url = new URL(apiURL);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setDoInput(true);
            huc.setDoOutput(true);
            huc.setRequestMethod(ReqType);

            // Header for applications based on app type

            huc.setRequestProperty("Accept","application/json");

            if (ReqType.matches("GET")) {
                System.out.println("I am a get request for " + ReqDescr );
            }

            if (ReqType.matches("POST")) {
                huc.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                System.out.println("I am a post request for " + ReqDescr);
                OutputStreamWriter writer = new OutputStreamWriter(huc.getOutputStream(), "UTF-8");
                writer.write(payload);
                writer.close();


            }
            if (ReqType.matches("PUT")) {
                huc.setRequestProperty("Content-Type",
                        "application/json");
                System.out.println("I am a put request " + ReqDescr);
                OutputStreamWriter writer = new OutputStreamWriter(huc.getOutputStream(), "UTF-8");
                writer.write(payload);

                writer.close();

            }

            huc.connect();
            System.setProperty("http.keepAlive", "false");

            JSONObject obj = new JSONObject();
            stCode = huc.getResponseCode();

            obj.put("status code", stCode);
            stPayload = huc.getResponseMessage();

            obj.put("Error", stPayload);
            obj.put("Method", huc.getRequestMethod());

            List<String> response = new ArrayList<String>();


            // checks server's status code first
            int status = huc.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        huc.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                }
                reader.close();
                huc.disconnect();
                System.out.println(response);
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());

        }


    }

}