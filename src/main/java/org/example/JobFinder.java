package org.example;

import com.google.common.annotations.VisibleForTesting;

import org.example.exceptions.FormValidationFailed;

import org.example.model.Job;
import org.example.model.JobMatcher;
import org.example.model.Settings;
import org.example.utils.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.Config.FORM_REFRESH_PERIOD_IN_SECONDS;
import static org.example.Config.TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS;
import static org.example.utils.DriverUtils.initDriver;
import static org.example.utils.DriverUtils.resetDriverGracefully;
import static org.example.utils.IoUtils.savePage;

public class JobFinder {

    public static final String NEW_JOBS_URL = "https://lcx-jobboard.lionbridge.com/new-jobs";
    public static int searchCount = 0;
    private final Logger logger = LoggerFactory.getLogger(JobFinder.class);
    private final Timer timer = new Timer(true);
    private WebDriver driver;
    private Settings settings;
    private Set<Job> jobsFound = new HashSet<>();

    public JobFinder(Settings settings) {
        this.settings = settings;
    }

    public void startScanning() {
        Config.getPropValues();
        logger.info("--------- Settings -------");
        logger.info("---minTotalPrice (EUR) " + settings.getMinTotalPrice());
        logger.info("---deadlineHoursFromNow (days) " + settings.getDeadlineHoursFromNow());
        logger.info("---maxVolume (words) " + settings.getMaxVolume());
        logger.info("--------------------------");

        if (driver == null) {
            driver = initDriver();
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::run, 0, FORM_REFRESH_PERIOD_IN_SECONDS, TimeUnit.SECONDS);
        logger.info("Scheduled the task at rate: {} ", FORM_REFRESH_PERIOD_IN_SECONDS);

    }

    protected void run() {

        try {
            openNewJobs();

            logger.info("End of process");
            driver.quit();
            timer.cancel();
        } catch (Exception e) {
            logger.error("Exception occurred during the process, quitting.", e);
            String fileName = getClass().getSimpleName();
            savePage(driver, fileName, "exception");
            logger.info("page is saved");
            try {
                resetDriverGracefully(driver);
                driver = initDriver();
            } catch (Exception e2) {
                logger.error("Exception occurred during the process, driver initializing", e2);
            }
        }

    }

    protected void signIn() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("Starting to {}", methodName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS));
        wait.until(webDriver -> {
            try {
                String username = "aija.vitola";
                String password = "6Ru*Restidru";

                // Getting the home page
                logger.info(String.format("Entering username: %s", username));
                webDriver.findElement(By.id("Username")).sendKeys(username);
                Thread.sleep(2000);
                logger.info("Clicking Proceed button");
                webDriver.findElement(By.tagName("button")).click();
                Thread.sleep(4000);
                logger.info(String.format("Entering password: %s", password));
                webDriver.findElement(By.id("passwordInput")).sendKeys(password);
                logger.info("Clicking Submit button");
                webDriver.findElement(By.id("submitButton")).click();
                Thread.sleep(10000);
                return true;
            } catch (Exception e) {
                logger.error("Exception occurred ", e);
                return false;
            }
        });
    }


    protected void openNewJobs() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        logger.info("Starting to {}", methodName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_FOR_INTERACTING_WITH_ELEMENT_IN_SECONDS));
        wait.until(webDriver -> {
            try {

                logger.info(String.format("Getting the URL: %s", NEW_JOBS_URL));
                try {
                    webDriver.get(NEW_JOBS_URL);
                } catch (WebDriverException we) {
                    logger.info("Resetting driver");
                    DriverUtils.resetDriverGracefully(webDriver);
                    driver = DriverUtils.initDriver();
                    webDriver = driver;
                    webDriver.get(NEW_JOBS_URL);
                }

                Thread.sleep(4000);
                logger.info("Checking if Sign in is required");
                if (webDriver.findElements(By.id("Username")).size() > 0) {
                    logger.info("Sign in is required");
                    signIn();
                }
                logger.info("Checking if no jobs message shown");
                List<WebElement> noJobsMessage = webDriver.findElements(By.xpath("//div[contains(@class, 'no-jobs-message')]"));
                logger.info("No Jobs message - " + noJobsMessage.size());
                if (noJobsMessage.size() > 0) {
                    logger.info("No jobs available");
                    Thread.sleep(10000);
                    return false;
                } else {
                    logger.info("Some jobs are available?");
                    String pageSource = webDriver.getPageSource();
                    int c = 0;

                    List<WebElement> cards = driver.findElements(By.xpath("//ltx-job-card"));


                    logger.info("----------------------------------------------------");
                    logger.info("----------------------------------------------------");
                    logger.info("----------------------------------------------------");
                    logger.info("------------Found {} cards ----------", cards.size());
                    for (int i = 1; i <= cards.size(); i++) {
                        try {
                            logger.info("----- Parsing job card {}", i);
                            Job job = new Job();
                            String card = String.format("(//ltx-job-card)[%d]", i);
                            WebElement title = driver.findElement(By.xpath(card + "//div[@class='job-title']"));
                            job.setTitle(title.getText());
                            job.setAppeared(LocalDateTime.now());
                            job.setCustomer(driver.findElement(By.xpath(card + "//div[@class='customerGroupName']")).getText());
                            job.setPrice(driver.findElement(By.xpath(card + "//div[@class='job-total']//span[@class='cost'] ")).getText());
                            job.setWordsCount(driver.findElement(By.xpath(card + "//div[@class='job-total']//div[@class='details']")).getText());
                            job.setDueDate(driver.findElement(By.xpath(card + "//div[@class='job-date']")).getText());
                            jobsFound.add(job);
                            boolean jobMatches = new JobMatcher().isJobMatches(job, settings);
                            if (jobMatches) {
                                logger.info("!!!!! JOB MATCHES! {}", job.getTitle());
                                title.click();
                                Thread.sleep(2000);
                                driver.findElement(By.xpath("(//button[@id='accept-btn'])[1]")).click();
                                logger.info("ACCEPTED!");
                                int f = 0;
                            } else {
                                logger.info("JOB NOT MATCHES {}", job.getTitle());
                            }

                        } catch (Exception e) {
                            logger.info("Error while parsing job card. Perhaps someone accepted it", e);
                        }

                    }

                    printJobTable(jobsFound);
                    return false;


                    //Card - //ltx-job-card
                    //div[@class='customerGroupName'] - customer
                    //ltx-job-card//div[@class='job-title'] - job title
                    //ltx-job-card//div[@class='job-date'] - job date
                    //ltx-job-card//div[@class='job-total']//span[@class="cost"] - price
                    //ltx-job-card//div[@class='job-total']//div[@class="details"]
                }

                //return true;
            } catch (Exception e) {
                logger.error("Exception occurred ", e);
                return false;
            }
        });
    }

    public static void printJobTable(Set<Job> jobs) {
        int columnWidth = 25; // Adjust this based on your needs
        String horizontalLine = "+-" + "-".repeat(13) + "-+-" + "-".repeat(8) + "-+-" + "-".repeat(13) + "-+-" + "-".repeat(60) + "-+";

        // Print top border
        System.out.println(horizontalLine);

        // Print table header
        System.out.printf("| %-13s | %-8s | %-13s | %-63s |%n",
                "Appeared", "Price", "Due Date", "Title");

        // Print header separator
        System.out.println(horizontalLine);


        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

        for (Job job : jobs) {
            String appearedFormatted = job.getAppeared().format(outputFormatter);
            String dueDateFormatted = job.getDueDate().format(outputFormatter);

            // Remove newline characters from the price
            String price = job.getPrice().replace("\n", "");

            // Shorten title if necessary
            String title = job.getTitle().length() > 60 ? job.getTitle().substring(0, 57) + "..." : job.getTitle();

            // Print job details with borders
            System.out.printf("| %-13s | %-8s | %-13s | %-43s |%n",
                    appearedFormatted, price, dueDateFormatted, title);

            // Print separator
            System.out.println(horizontalLine);
        }

        // Print bottom border
        System.out.println(horizontalLine);
    }



}
