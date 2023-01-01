package org.example.auslanderbehorde.formfiller.business;

import okhttp3.*;
import okio.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.auslanderbehorde.appointmentfinder.business.AppointmentFinder;
import org.example.auslanderbehorde.formfiller.enums.SeleniumProcessEnum;
import org.example.auslanderbehorde.formfiller.enums.SeleniumProcessResultEnum;
import org.example.auslanderbehorde.formfiller.enums.VisaEnum;
import org.example.auslanderbehorde.formfiller.exceptions.ElementNotFoundTimeoutException;
import org.example.auslanderbehorde.formfiller.exceptions.InteractionFailedException;
import org.example.auslanderbehorde.formfiller.model.FormInputs;
import org.example.auslanderbehorde.sessionfinder.business.SessionFinder;
import org.example.auslanderbehorde.sessionfinder.model.SessionInfo;
import org.example.notifications.Helper;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static org.example.auslanderbehorde.appointmentfinder.business.AppointmentFinder.foundAppointmentCount;
import static org.example.auslanderbehorde.appointmentfinder.business.AppointmentFinder.handledAppointmentCount;
import static org.example.auslanderbehorde.formfiller.business.FormFillerUtils.TIMEOUT_FOR_GETTING_ELEMENT_IN_SECONDS;
import static org.example.auslanderbehorde.formfiller.business.FormFillerUtils.logInfo;
import static org.example.auslanderbehorde.formfiller.enums.FormParameterEnum.*;

/**
 * Business Access Layer for filling the form
 */
public class FormFillerBAL extends TimerTask {

    private final Logger logger = LogManager.getLogger(FormFillerBAL.class);
    public static final int FORM_REFRESH_PERIOD_MILLISECONDS = 1000;

    private final String citizenshipValue;
    private final String applicantNumber;
    private final String familyStatus;
    private final VisaEnum visaEnum;

    private SessionInfo sessionInfo;
    private static int searchCount = 0;
    private static int succesfullyFormSentCount = 0;

    private RemoteWebDriver driver;
    private final Timer timer = new Timer(true);
    String winHandleBefore;

    public void startScanning() {
        logger.info(String.format("Scheduled the task at rate: %s", FORM_REFRESH_PERIOD_MILLISECONDS));
        timer.scheduleAtFixedRate(this, 2000, FORM_REFRESH_PERIOD_MILLISECONDS);
    }

    public FormFillerBAL(FormInputs formInputs, SessionInfo sessionInfo, RemoteWebDriver remoteWebDriver) {
        this.sessionInfo = sessionInfo;
        this.driver = remoteWebDriver;
        this.citizenshipValue = formInputs.getCitizenshipValue();
        this.applicantNumber = formInputs.getApplicationsNumber();
        this.familyStatus = formInputs.getFamilyStatus();
        this.visaEnum = formInputs.getVisaEnum();
    }

    @Override
    public void run() {
        int initSessionTryCount = 0;
        try {
            while (true) {
                initSessionTryCount++;
                initNewSession();
                break;
            }
        } catch (Exception e) {
            logger.error("Session initialization had failed. initSessionTryCount: {} Exception: ", initSessionTryCount, e);
            driver.quit();
            driver = Helper.initDriverHeadless();
        }
        try {
            /*
            if (sessionInfo == null) {
                logger.info("Session is not created.");
                initNewSession();
            }
             */
            initNewSession();
            fillForm();
/*
            double remainingMinute = getRemainingTime();

            if (remainingMinute <= 1) {
                logger.warn("Time is up");
                initNewSession();
                fillForm();
            }
 */
            sendForm();

            if (isCalenderOpened()) {
                succesfullyFormSentCount++;
                Thread.sleep(1000);
                AppointmentFinder appointmentFinder = new AppointmentFinder(driver);
                appointmentFinder.handleFindingAppointment();
            }
            clickToSelectService();
            searchCount++;
            String msg = String.format("Completed search count: %s. SuccessfullyFormSenCount:%s, HandledAppoi.Count:%s, Found count: %s", searchCount, succesfullyFormSentCount, handledAppointmentCount, foundAppointmentCount);
            logger.info(msg);

        } catch (Exception e) {
            logger.warn("Some error occurred. Reason ", e);
            //driver.close();
            try {
                initNewSession();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    public void getFormPage(String requestId, String dswid, String dsrid) {
        String hostUrl = "https://otv.verwalt-berlin.de/ams/TerminBuchen/wizardng";
        String targetUrl = hostUrl + "/" + requestId + "?dswid=" + dswid + "&dsrid=" + dsrid;
        logger.info(String.format("Getting the URL: %s", targetUrl));
        winHandleBefore = driver.getWindowHandle();
        System.out.println("WindowHandle" + winHandleBefore);
        Set<String> handle = driver.getWindowHandles();
        handle.forEach((asd) -> System.out.println("handle" + asd));
        //driver.switchTo().window(handle.stream().toList().get(0));
        driver.get(targetUrl);
    }

    private void selectCitizenshipValue() throws InterruptedException, ElementNotFoundTimeoutException {
        String elementName = COUNTRY.getId();
        String elementDescription = COUNTRY.name();
        WebElement element = FormFillerUtils.getElementById(elementName, elementDescription, driver);
        Select select = new Select(element);
        select.selectByValue(citizenshipValue);
        WebElement option = select.getFirstSelectedOption();
        String selectValue = option.getText();
        logInfo(elementDescription, SeleniumProcessEnum.SELECTING_OPTION, "Successful", "value" + selectValue);
    }

    private void selectApplicantsCount() throws InterruptedException, ElementNotFoundTimeoutException, InteractionFailedException {
        String elementId = APPLICANT_COUNT.getId();
        String elementDescription = APPLICANT_COUNT.name();
        WebElement element = FormFillerUtils.getElementById(elementId, elementDescription, driver);
        FormFillerUtils.selectOptionByValue(element, elementDescription, applicantNumber);
        //Select select = new Select(element);
        //select.selectByValue(applicantNumber);
        //WebElement option = select.getFirstSelectedOption();
        //String selectValue = option.getText();
        //logInfo(elementDescription, SeleniumProcessEnum.SELECTING_OPTION, "Successful", "value" + selectValue);
    }

    private void selectFamilyStatus() throws InterruptedException, ElementNotFoundTimeoutException, InteractionFailedException {
        String elementId = FAMILY_STATUS.getId();
        String elementDescription = FAMILY_STATUS.name();
        WebElement element = FormFillerUtils.getElementById(elementId, elementDescription, driver);
        FormFillerUtils.selectOptionByValue(element, elementDescription, familyStatus);
        //Select select = new Select(element);
        //select.selectByValue(familyStatus);
        //WebElement option = select.getFirstSelectedOption();
        //String selectValue = option.getText();
        // logInfo(elementDescription, SeleniumProcessEnum.SELECTING_OPTION, "Successful", "value" + selectValue);
    }

    private void clickServiceType() throws InterruptedException, ElementNotFoundTimeoutException, InteractionFailedException {
        String elementXPath = "//*[@id=\"xi-div-30\"]/div[1]/label/p";
        String elementDescription = "serviceType".toUpperCase();
        WebElement element = FormFillerUtils.getElementByXPath(elementXPath, elementDescription, driver);
        FormFillerUtils.clickToElement(element, elementDescription);
        //element.click();
        //logInfo(elementDescription, SeleniumProcessEnum.CLICKING_TO_ELEMENT, SeleniumProcessResultEnum.SUCCESSFUL.name());
    }

    private void clickVisaGroup() throws InterruptedException, ElementNotFoundTimeoutException, InteractionFailedException {
        String elementXpath = "//*[@id=\"inner-163-0-1\"]/div/div[3]/label";
        String elementDescription = "visaGroup".toUpperCase();
        WebElement element = FormFillerUtils.getElementByXPath(elementXpath, elementDescription, driver);
        FormFillerUtils.clickToElement(element, elementDescription);
        //element.click();
        //logInfo(elementDescription, SeleniumProcessEnum.CLICKING_TO_ELEMENT, SeleniumProcessResultEnum.SUCCESSFUL.name());
    }

    private void clickToVisa() throws InterruptedException, ElementNotFoundTimeoutException, InteractionFailedException {
        String elementId = visaEnum.getId();
        String elementDescription = visaEnum.getDataTag0();
        WebElement element = FormFillerUtils.getElementById(elementId, elementDescription, driver);
        FormFillerUtils.clickToElement(element, elementDescription);
        //element.click();
        //logInfo(elementDescription, SeleniumProcessEnum.CLICKING_TO_ELEMENT, SeleniumProcessResultEnum.SUCCESSFUL.name());
    }

    private void sendForm() throws InterruptedException, ElementNotFoundTimeoutException, InteractionFailedException {
        String elementXpath = "//*[@id=\"applicationForm:managedForm:proceed\"]";
        String elementDescription = "clickButton".toUpperCase();
        WebElement element = FormFillerUtils.getElementByXPath(elementXpath, elementDescription, driver);
        FormFillerUtils.clickToElement(element, elementDescription);
        //element.click();
        //logInfo(elementDescription, SeleniumProcessEnum.CLICKING_TO_ELEMENT, SeleniumProcessResultEnum.SUCCESSFUL.name());
    }

    private void clickToSelectService() throws ElementNotFoundTimeoutException, InterruptedException, InteractionFailedException {
        String xpath = "//*[@id=\"main\"]/div[2]/div[4]/div[2]/div/div[1]/ul/li[1]";
        String elementDescription = "Select Service";
        WebElement element = FormFillerUtils.getElementByXPath(xpath, elementDescription, driver);
        FormFillerUtils.clickToElement(element, elementDescription);
        //String alert = driver.switchTo().alert().getText();
        //driver.switchTo().alert().dismiss();
        //logger.info("Clicked to dismiss");
    }

    protected boolean isCalenderOpened() throws InteractionFailedException {
        String stageXPath = ".//ul/li[2]/span";
        String elementDescription = "activeSectionTab".toUpperCase();
        int i = 1;
        String stageText;
        while (i <= TIMEOUT_FOR_GETTING_ELEMENT_IN_SECONDS) {
            try {
                WebElement element = FormFillerUtils.getElementByXPath(stageXPath, elementDescription, driver);
                stageText = element.getText();
                logInfo(elementDescription, SeleniumProcessEnum.GETTING_TEXT, SeleniumProcessResultEnum.SUCCESSFUL.name(), String.format("Value: %s", stageText));
                String asd = "//*[@id=\"xi-div-1\"]/div[3]";
                try {
                    String elementDescription1 = "calender".toUpperCase();
                    WebElement calender = FormFillerUtils.getElementByXPathCalender(asd, elementDescription1, driver);
                    FormFillerUtils.saveSourceCodeToFile(driver.getPageSource(), "dateSelection_in");
                    FormFillerUtils.saveScreenshot(driver, "dateSelection_in");
                    return true;
                    //return stageText.contains("Terminauswahl") && calender.isDisplayed();
                } catch (ElementNotFoundTimeoutException e) {
                    return false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } catch (StaleElementReferenceException | InterruptedException | ElementNotFoundTimeoutException e) {
                //logWarn(elementDescription, SeleniumProcessEnum.GETTING_TEXT.name(), SeleniumProcessResultEnum.FAILED.name(), e);
            }
            i++;
        }
        if (i > TIMEOUT_FOR_GETTING_ELEMENT_IN_SECONDS) {
            logger.warn("Element: {}. Process: Getting by elementXPath. Result: Failed. Reason: Couldn't get the element within timeout", elementDescription);
            throw new InteractionFailedException(elementDescription);

        }
        return false;
    }

    private double getRemainingTime() {
        String elementXpath = "//*[@id=\"progressBar\"]/div";
        String elementDescription = "remainingTime".toUpperCase();
        int remainingMinute = 0;
        try {
            WebElement element = FormFillerUtils.getElementByXPath(elementXpath, elementDescription, driver);
            String timeStr = element.getText();
            if (timeStr != null) {
                remainingMinute = Integer.parseInt(timeStr.split(":")[0]);
            }
        } catch (Exception e) {
            logger.info("Element: {}. Process: Getting time. Status: Failed", elementDescription);
        }
        return remainingMinute;
    }

    private void initNewSession() throws InterruptedException {
        logger.info("initiating a new auslaenderbehorde session");
        driver.switchTo().newWindow(WindowType.TAB);
        SessionFinder sessionFinder = new SessionFinder(driver);
        sessionInfo = sessionFinder.findAndGetSession();
        winHandleBefore = driver.getWindowHandle();
        System.out.println("WindowHandle" + winHandleBefore);
        Set<String> handle = driver.getWindowHandles();
        handle.forEach((asd) -> System.out.println("handle" + asd));
        driver.switchTo().window(handle.stream().toList().get(0)).close();
        driver.switchTo().window(winHandleBefore);
    }

    private String makeGetCall(String s) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request request = new Request.Builder()
                .url(s)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillForm() throws ElementNotFoundTimeoutException, InterruptedException, InteractionFailedException {
        logger.info("Starting to fill the form");
        getFormPage(sessionInfo.getRequestId(), sessionInfo.getDswid(), sessionInfo.getDsrid());
        selectCitizenshipValue();
        selectApplicantsCount();
        selectFamilyStatus();
        clickServiceType();
        clickVisaGroup();
        clickToVisa();
    }

    private WebSocket makeSocketConnection(String websocketUrl) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request request = new Request.Builder()
                .url(websocketUrl)
                .build();

        EchoWebSocketListener listener = new EchoWebSocketListener();

        WebSocket ws = client.newWebSocket(request, listener);
        return ws;
    }

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send("Knock, knock!");
            webSocket.send("Hello!");
            webSocket.send(ByteString.decodeHex("deadbeef"));
            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye!");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            System.out.println("Receiving: " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            System.out.println("Receiving: " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            System.out.println("Closing: " + code + " " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            t.printStackTrace();
        }

    }

    public RemoteWebDriver getDriver() {
        return this.driver;
    }
}
