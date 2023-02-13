package org.example.auslanderbehorde.formfiller.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.auslanderbehorde.formfiller.enums.ServiceType;
import org.example.auslanderbehorde.formfiller.enums.visaextension.VisaExtensionForEducationalPurposeVisaEnum_DE;
import org.example.auslanderbehorde.formfiller.model.PersonalInfoDTO;
import org.example.auslanderbehorde.formfiller.model.ResidenceTitleInfoDTO;
import org.example.auslanderbehorde.formfiller.model.Section2FormInputs;
import org.example.auslanderbehorde.formfiller.model.Section4FormInputs;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.example.auslanderbehorde.formfiller.business.DriverManager.initDriverHeadless;
import static org.example.auslanderbehorde.formfiller.enums.applyforavisa.EconomicActivityVisaDe.BLUECARD;

public class FormManager {

    public final static Logger logger = LogManager.getLogger(FormManager.class);

    public static PersonalInfoDTO createDummyPersonalInfoDTA() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = PersonalInfoDTO.class.getResourceAsStream("/personalInfoDTO.json");
        try {
            return mapper.readValue(is, PersonalInfoDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResidenceTitleInfoDTO readVisaInfo(){
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = ResidenceTitleInfoDTO.class.getResourceAsStream("/residentTitleInfoDTO.json");
        try {
            return mapper.readValue(is, ResidenceTitleInfoDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startForm(PersonalInfoDTO personalInfoDTO, ResidenceTitleInfoDTO residenceTitleInfoDTO) {
        Section2FormInputs section2FormInputs = new Section2FormInputs(
                personalInfoDTO.citizenshipValue(),
                personalInfoDTO.applicationsNumber(),
                personalInfoDTO.familyStatus(),
                residenceTitleInfoDTO.serviceType(),
                VisaExtensionForEducationalPurposeVisaEnum_DE.PURPOSE_OF_STUDYING);

        Section2FormInputs section2FormInputs_2 = new Section2FormInputs(
                personalInfoDTO.citizenshipValue(),
                personalInfoDTO.applicationsNumber(),
                personalInfoDTO.familyStatus(),
                residenceTitleInfoDTO.serviceType(),
                BLUECARD);

        Section4FormInputs section4FormInputs = new Section4FormInputs(
                personalInfoDTO.firstName(),
                personalInfoDTO.lastName(),
                personalInfoDTO.emailAddress(),
                personalInfoDTO.birthdate(),
                Optional.empty(),
                Optional.of(residenceTitleInfoDTO.residencePermitId()),
                ServiceType.EXTEND_A_RESIDENCE_TITLE);
        Section4FormInputs section4FormInputs_2 = new Section4FormInputs(
                personalInfoDTO.firstName(),
                personalInfoDTO.lastName(),
                personalInfoDTO.emailAddress(),
                personalInfoDTO.birthdate(),
                Optional.of(true),
                Optional.of(residenceTitleInfoDTO.residencePermitId()),
                ServiceType.APPLY_FOR_A_RESIDENCE_TITLE);

        if (isFormVerified(section4FormInputs)) {
            RemoteWebDriver remoteWebDriver = initDriverHeadless();
            TerminFinder terminFinder = new TerminFinder(section4FormInputs, section2FormInputs, remoteWebDriver);
            terminFinder.startScanning();
        }


        if (isFormVerified(section4FormInputs_2)) {
            //RemoteWebDriver remoteWebDriver = initDriverHeadless();
            //TerminFinder terminFinder_2 = new TerminFinder(section4FormInputs_2, section2FormInputs_2, remoteWebDriver);
            //terminFinder_2.startScanning();
        }
    }

    public static boolean isFormVerified(Section4FormInputs formInputs) {
        logger.info("Verifying form: {}", formInputs.toString());
        ServiceType serviceType = formInputs.getServiceType();
        Optional<Boolean> isResidencePermitPresent = formInputs.getIsResidencePermitPresent();
        Optional<String> residencePermitId = formInputs.getResidencePermitId();

        if (serviceType.equals(ServiceType.APPLY_FOR_A_RESIDENCE_TITLE)) {
            if (isResidencePermitPresent.isEmpty()) {
                return false;
            }

            if (isResidencePermitPresent.get() && residencePermitId.isEmpty()) {
                return false;
            }

            if (!isResidencePermitPresent.get() && residencePermitId.isPresent()) {
                return false;
            }
        }

        if (serviceType.equals(ServiceType.EXTEND_A_RESIDENCE_TITLE)) {
            if (isResidencePermitPresent.isPresent()) {
                return false;
            }

            if (residencePermitId.isEmpty()) {
                return false;
            }
        }

        return true;
    }

}