package org.example.auslanderbehorde.form.business;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.example.auslanderbehorde.form.exceptions.ElementNotFoundException;
import org.example.auslanderbehorde.form.exceptions.InteractionFailedException;
import org.example.auslanderbehorde.form.enums.SeleniumProcessEnum;
import org.example.auslanderbehorde.form.enums.SeleniumProcessResultEnum;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FormFillerUtils {
    private static final Logger logger = LogManager.getLogger(FormFillerUtils.class);
    static final int TIMEOUT_FOR_INTERACTING_IN_SECONDS = 10;
    static final int SLEEP_DURATION_IN_MILISECONDS = 1500;
    public static long formId;

    public static WebElement getElementById(String id, String elementDescription, WebDriver driver) throws InterruptedException, ElementNotFoundException {
        WebElement element = null;
        int i = 1;
        while (i <= TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            try {
                element = driver.findElement(By.id(id));
                logInfo(elementDescription, SeleniumProcessEnum.GETTING_BY_ID, "Successful");
                Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
                break;
            } catch (Exception e) {
                logWarn(elementDescription, SeleniumProcessEnum.GETTING_BY_XPATH.name(), SeleniumProcessResultEnum.FAILED.name(), e);
            }
            Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
            i++;
        }
        if (i > TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            logger.warn("Element: {}. Process: Getting by elementId. Result: Failed. Reason: Couldn't get elementById within timeout", elementDescription);
            throw new ElementNotFoundException(elementDescription);

        }
        return element;
    }

    public static WebElement getElementByName(String elementName, String elementDescription, WebDriver driver) throws InterruptedException, ElementNotFoundException {
        WebElement element = null;
        int i = 1;
        while (i <= TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            try {
                element = driver.findElement(By.name(elementName));
                logInfo(elementDescription, SeleniumProcessEnum.GETTING_BY_NAME, "Successful");
                Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
                break;
            } catch (Exception e) {
                logger.warn("Element: {}. Process: Getting by elementName. Result: Failed. Reason:{}", elementDescription, e.getMessage());
            }
            Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
            i++;
        }
        if (i > TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            //logWarn(elementDescription, SeleniumProcessEnum.GETTING_BY_NAME.name(), SeleniumProcessResultEnum.FAILED.name(), e);
            throw new ElementNotFoundException(elementDescription);

        }
        return element;
    }

    public static WebElement getElementByXPath(String xpath, String elementDescription, WebDriver driver) throws InterruptedException, ElementNotFoundException {
        WebElement element = null;
        int i = 0;
        while (i <= TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            try {
                element = driver.findElement(By.xpath(xpath));
                logInfo(elementDescription, SeleniumProcessEnum.GETTING_BY_XPATH, "Successful");
                Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
                break;
            } catch (Exception e) {
                logger.warn("Element: {}. Process: Getting by elementXPath. Result: Failed", elementDescription);
            }
            Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
            i++;
        }
        if (i > TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            logger.warn("Element: {}. Process: Getting by elementXPath. Result: Failed. Reason: Couldn't get the element within timeout", elementDescription);
            throw new ElementNotFoundException(elementDescription);

        }
        return element;
    }

    public static WebElement getElementByCssSelector(String cssSelector, String elementDescription, WebDriver driver) throws InterruptedException, ElementNotFoundException {
        WebElement element = null;
        int i = 0;
        while (i <= TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            try {
                element = driver.findElement(By.cssSelector(cssSelector));
                logInfo(elementDescription, SeleniumProcessEnum.GETTING_BY_CSS_SELECTOR, "Successful");
                Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
                break;
            } catch (Exception e) {
                logger.warn("Element: {}. Process: Getting by elementCssSelector. Result: Failed", elementDescription);
            }
            Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
            i++;
        }
        if (i > TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            logger.warn("Element: {}. Process: Getting by elementCssSelector. Result: Failed. Reason: Couldn't get within timeout", elementDescription);
            throw new ElementNotFoundException(cssSelector);

        }
        return element;
    }

    public static void clickToElement(WebElement element, String elementDescription) throws InteractionFailedException {
        if (element == null) {
            logger.warn("Element:{} is null, Process: Click can not be continued", elementDescription);
            return;
        }
        int i = 0;
        while (i <= TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            try {
                element.click();
                logInfo(elementDescription, SeleniumProcessEnum.CLICKING_TO_ELEMENT, "Successful");
                Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
                break;
            } catch (Exception e) {
                logger.warn("Element: {}. Process: Click, Result: Failed. Exception: ", elementDescription, e);
            }
            i++;
        }
        if (i > TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            logger.warn("Element: {}. Process: Click, Result: Failed Reason: Couldn't click within timeout", elementDescription);
            throw new InteractionFailedException("");
        }
    }

    public static void selectOption(WebElement element, String elementDescription, String optionValue) {
        if (element == null) {
            logger.warn("Element:{} is null, Process: Select can not be continued");
            return;
        }
        int i = 0;
        while (i <= TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            try {
                Select select = new Select(element);
                select.selectByValue(optionValue);
                WebElement option = select.getFirstSelectedOption();
                String selectValue = option.getText();
                logInfo(elementDescription, SeleniumProcessEnum.SELECTING_OPTION, "Successful", "value" + selectValue);
                Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
                break;
            } catch (Exception e) {
                logger.warn("Element: {}. Process: Select, Result: Failed Reason:{}", elementDescription, e.getMessage());
            }
            i++;
        }
        if (i > TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            logger.warn("Element: {}. Process: Select, Result: Failed Reason: Couldn't select within timeout", elementDescription);
        }
    }

    public static void selectOptionByIndex(WebElement element, String elementDescription) {
        if (element == null) {
            logger.warn("Element:{} is null, process can not be continued");
            return;
        }
        int i = 0;
        while (i <= TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            try {
                Select select = new Select(element);
                List<WebElement> availableHours = select.getOptions();
                isTimeslotOptionVerified(element);
                int targetHourIndex = 0;
                select.selectByIndex(targetHourIndex);
                String selectValue = availableHours.get(targetHourIndex).getText();
                logInfo(elementDescription, SeleniumProcessEnum.SELECTING_OPTION, SeleniumProcessResultEnum.SUCCESSFUL.name(), "Value: " + selectValue);
                Thread.sleep(SLEEP_DURATION_IN_MILISECONDS);
                break;
            } catch (Exception e) {
                logger.warn("Element: {}. Process: Select, Result: Failed Reason:{}", elementDescription, e.getMessage());
            }
            i++;
        }
        if (i > TIMEOUT_FOR_INTERACTING_IN_SECONDS) {
            logger.warn("Element: {}. Process: Select, Result: Failed Reason: Couldn't select within timeout", elementDescription);
        }

    }
    private static boolean isTimeslotOptionVerified(WebElement element){
        Select select = new Select(element);
        List<WebElement> availableHours = select.getOptions();
        logger.info(String.format("There are %s options", availableHours.size()));
        for (WebElement hours: availableHours) {
            logger.info(hours.getText());
        }
        if(availableHours.get(0).getText().contains("Bitte")){
            return false;
        }
        return true;
    }
    public static void saveSourceCodeToFile(String content) {
        String filePath = FormFiller.class.getResource("/").getPath();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateAsStr = dtf.format(now);
        File newTextFile = new File(filePath + "/page_" + dateAsStr + ".html");

        FileWriter fw;
        try {
            fw = new FileWriter(newTextFile);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void saveScreenshot(WebDriver driver) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateAsStr = dtf.format(now);
        File scrFile1 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String filePath = FormFiller.class.getResource("/").getPath();
        FileUtils.copyFile(scrFile1, new File(filePath + "/screenshot_" + dateAsStr + "_0.png"));

        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("scroll(0, 1000);");
        FileUtils.copyFile(scrFile1, new File(filePath + "/screenshot_" + dateAsStr + "_1.png"));

    }

    public static void logInfo(String elementDescription, SeleniumProcessEnum process, String status) {
        ThreadContext.put("formId", String.valueOf(formId));
        ThreadContext.put("elementDescription", elementDescription);
        ThreadContext.put("seleniumProcess", process.name());
        ThreadContext.put("seleniumStatus", status);
        logger.info(elementDescription + " " + process + " " + status);
        //ThreadContext.clearAll();
    }

    public static void logInfo(String elementDescription, SeleniumProcessEnum process, String status, String msg) {
        ThreadContext.put("formId", String.valueOf(formId));
        ThreadContext.put("elementDescription", elementDescription);
        ThreadContext.put("seleniumProcess", process.name());
        ThreadContext.put("seleniumStatus", status);
        logger.info(elementDescription + " " + process + " " + status + msg);
        //ThreadContext.clearAll();
    }

    public static void logWarn(String elementDescription, String process, String status, Throwable e) {
        ThreadContext.put("formId", String.valueOf(formId));
        ThreadContext.put("elementDescription", elementDescription);
        ThreadContext.put("seleniumProcess", process);
        ThreadContext.put("seleniumStatus", status);
        logger.warn(elementDescription + " " + process + " " + status, e);
        //ThreadContext.clearAll();
    }
}
