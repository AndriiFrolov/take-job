package org.example.auslanderbehorde.formfiller.business;

import org.example.auslanderbehorde.formfiller.enums.EconomicActivityVisaDeEnum;
import org.example.auslanderbehorde.formfiller.model.FormInputs;
import org.example.auslanderbehorde.formfiller.model.Section4FormInputs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminFinderTest {

    static ChromeDriver driver;
    FormInputs formInputs = new FormInputs("163", "1", "2", EconomicActivityVisaDeEnum.BLUECARD);
    String firstName = "firstName";
    String lastName = "lastname";
    String email = "yilmazn.aslan@gmail.com";
    String birthdate = "12.03.1993";
    String residencePermitId = "ABCSD12333";
    Section4FormInputs section4FormInputs = new Section4FormInputs(firstName, lastName, email, birthdate, false, Optional.of(residencePermitId));


    @BeforeAll
    static void initDriver() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
    }

    //@AfterAll
    static void quitDriver() {
        driver.quit();
    }

    @Test
    void ASSERT_THAT_everythings_is_fine_WHEN_run_is_called() {
        // GIVEN
        //driver.get(url_DE);


        TerminFinder terminFinder = spy(new TerminFinder(section4FormInputs, formInputs, driver));

        doThrow(new NoSuchSessionException("")).when(terminFinder).run();

        // WHEN
        terminFinder.run();

        // THEN
       // verify(terminFinder).

    }

    @Test
    void run() {
    }
}