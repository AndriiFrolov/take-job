package org.example.formhandlers;

import org.example.BaseTestSetup;
import org.example.model.VisaFormTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.example.enums.Section4FormParameterEnum.*;
import static org.mockito.Mockito.verify;


class Section4VisaFormHandlerTest extends BaseTestSetup {

    static String path_extend_studium = Section4VisaFormHandler.class.getClassLoader().getResource("section4Handler_extend_study_2023-02-03_10:23:40.html").getPath();
    static String url_for_service_extend_studium = "file:".concat(path_extend_studium);
    static String path_apply_bluecard = Section4VisaFormHandler.class.getClassLoader().getResource("section4Handler_apply_bluecard_visa_exists_2023-01-12_00:10:29.html").getPath();
    static String url_for_service_apply_for_bluecard = "file:".concat(path_apply_bluecard);

    static String path_new = Section4VisaFormHandler.class.getClassLoader().getResource("Section4VisaFormHandler_extend_visa_2023-03-01_14_11_31.html").getPath();
    static String url_new = "file:".concat(path_new);

    VisaFormTO visaForm_apply_for_bluecard_with_residencePermit = new VisaFormTO(true, residencePermitId, "Apply for a residence title", "Economic activity", "EU Blue Card / Blaue Karte EU (sect. 18b para. 2)");


    VisaFormTO visaForm_extend_berufsausbildung = new VisaFormTO(null, residencePermitId, "Extend a residence title", "Educational purposes", "Residence permit for vocational training (sect. 16a)");

    Section4VisaFormHandler formFiller = new Section4VisaFormHandler(personalInfoFormTO, visaForm_apply_for_bluecard_with_residencePermit, driver);

    static List<String> getPathUrls() {
        return List.of(url_for_service_apply_for_bluecard, url_for_service_extend_studium);
    }

    @ParameterizedTest
    @MethodSource("getPathUrls")
    void ASSERT_THAT_firstName_is_entered_WHEN_enterFirstName_is_called(String url) {
        // GIVEN
        driver.get(url);

        // WHEN
        formFiller.enterFirstName();

        // THEN
        String elementId = FIRSTNAME.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Assertions.assertEquals(firstName, element.getAttribute("value"));
    }

    @ParameterizedTest
    @MethodSource("getPathUrls")
    void ASSERT_THAT_lastName_is_entered_WHEN_enterLastName_is_called(String url) {
        // GIVEN
        driver.get(url);

        // WHEN
        formFiller.enterLastName();

        // THEN
        String elementId = LASTNAME.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Assertions.assertEquals(lastName, element.getAttribute("value"));
    }

    @ParameterizedTest
    @MethodSource("getPathUrls")
    void ASSERT_THAT_email_is_entered_WHEN_enterEmail_is_called(String url) {
        // GIVEN
        driver.get(url);

        // WHEN
        formFiller.enterEmail();

        // THEN
        String elementId = EMAIL.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Assertions.assertEquals(email, element.getAttribute("value"));

    }

    @ParameterizedTest
    @MethodSource("getPathUrls")
    void ASSERT_THAT_birthdate_is_entered_WHEN_enterBirthdate_is_called(String url){
        // GIVEN
        driver.get(url);

        // WHEN
        formFiller.enterBirthdate();

        // THEN
        String elementId = BIRTHDATE.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Assertions.assertEquals(birthdate, element.getAttribute("value"));
    }

    @Test
    void ASSERT_THAT_residencePermit_is_selected_true_WHEN_selectResidencePermit_is_called_GIVEN_residencePermit_exist_AND_apply_to_visa() {
        // GIVEN
        driver.get(url_for_service_apply_for_bluecard);

        // WHEN
        formFiller.selectResidencePermit();

        // THEN
        String elementId = RESIDENCE_PERMIT.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Select select = new Select(element);
        WebElement option = select.getFirstSelectedOption();
        String actualValue = option.getText();
        Assertions.assertEquals("Ja", actualValue);
    }

    @Test
    void ASSERT_THAT_residencePermit_is_selected_false_WHEN_selectResidencePermit_is_called_GIVEN_residencePermit_not_exist() {
        // GIVEN
        driver.get(url_for_service_apply_for_bluecard);
        Section4VisaFormHandler formFiller = new Section4VisaFormHandler(personalInfoFormTO, visaForm_apply_for_bluecard_without_residencePermit, driver);

        // WHEN
        formFiller.selectResidencePermit();

        // THEN
        String elementId = RESIDENCE_PERMIT.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Select select = new Select(element);
        WebElement option = select.getFirstSelectedOption();
        String actualValue = option.getText();
        Assertions.assertEquals("Nein", actualValue);
    }

    @Test
    void ASSERT_THAT_residencePermitId_is_entered_true_WHEN_selectResidencePermit_is_called_GIVEN_residencePermit_exist_AND_apply_for_visa(){
        // GIVEN
        driver.get(url_for_service_apply_for_bluecard);
        Section4VisaFormHandler formFiller = new Section4VisaFormHandler(personalInfoFormTO, visaForm_apply_for_bluecard_with_residencePermit, driver);

        // WHEN
        formFiller.enterResidencePermitId(RESIDENCE_PERMIT_NUMBER.getName());

        // THEN
        String elementId = RESIDENCE_PERMIT_NUMBER.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Assertions.assertEquals(residencePermitId, element.getAttribute("value"));
    }

    @Test
    void ASSERT_THAT_residencePermitId_is_entered_true_WHEN_selectResidencePermit_is_called_GIVEN_residencePermit_exist_AND_extend_visa() {
        // GIVEN
        driver.get(url_for_service_extend_studium);
        Section4VisaFormHandler formFiller = new Section4VisaFormHandler(personalInfoFormTO, visaForm_extend_berufsausbildung, driver);

        // WHEN
        formFiller.enterResidencePermitId(RESIDENCE_PERMIT_NUMBER_EXTENSION.getName());

        // THEN
        String elementId = RESIDENCE_PERMIT_NUMBER_EXTENSION.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Assertions.assertEquals(residencePermitId, element.getAttribute("value"));
    }

    @Test
    void ASSERT_THAT_residencePermitId_is_entered_true_WHEN_selectResidencePermit_is_called_GIVEN_service_is_Aufenthaltserlaubnis_für_eine_Berufsausbildung() {
        // GIVEN
        driver.get(url_for_service_extend_studium);
        Section4VisaFormHandler formFiller = new Section4VisaFormHandler(personalInfoFormTO, visaForm_extend_berufsausbildung, driver);

        // WHEN
        formFiller.enterResidencePermitId(RESIDENCE_PERMIT_NUMBER_EXTENSION.getName());

        // THEN
        String elementId = RESIDENCE_PERMIT_NUMBER_EXTENSION.getId();
        WebElement element = driver.findElement(By.id(elementId));
        Assertions.assertEquals(residencePermitId, element.getAttribute("value"));
    }

    @Test
    void ASSERT_THAT_form_is_filled_and_sent_WHEN_sendForm_is_called_GIVEN_form_is_filled_GIVEN_THAT_service_is_apply_for_bluecard(){
        // GIVEN
        driver.get(url_for_service_apply_for_bluecard);

        // WHEN
        Section4VisaFormHandler spiedFormFiller = Mockito.spy(new Section4VisaFormHandler(personalInfoFormTO, visaForm_apply_for_bluecard_with_residencePermit, driver));
        spiedFormFiller.fillAndSendForm();

        // THEN
        verify(spiedFormFiller).fillForm();
        verify(spiedFormFiller).sendForm();
    }

    @Test
    void ASSERT_THAT_form_is_filled_and_sent_WHEN_sendForm_is_called_GIVEN_form_is_filled_AND_visa_type_is_extension(){
        // GIVEN
        driver.get(url_for_service_extend_studium);

        // WHEN
        Section4VisaFormHandler spiedFormFiller = Mockito.spy(new Section4VisaFormHandler(personalInfoFormTO, visaForm_extend_berufsausbildung, driver));
        spiedFormFiller.fillAndSendForm();

        // THEN
        verify(spiedFormFiller).fillForm();
        verify(spiedFormFiller).sendForm();
    }

    @Test
    void ASSERT_THAT_form_is_filled_and_sent_WHEN_sendForm_is_called_GIVEN_form_is_filled_AND_visa_type_is_extension_() {
        // GIVEN
        driver.get(url_new);

        // WHEN
        Section4VisaFormHandler spiedFormFiller = Mockito.spy(new Section4VisaFormHandler(personalInfoFormTO, visaForm_extend_berufsausbildung, driver));
        spiedFormFiller.fillAndSendForm();

        // THEN
        verify(spiedFormFiller).fillForm();
        verify(spiedFormFiller).sendForm();
    }

}
