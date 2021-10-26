package com.assignment.automation.Test;

import com.assignment.automation.ObjectRepository.*;
import com.assignment.automation.Utilities.FileHelper;
import com.assignment.automation.Utilities.PageLoader;
import com.assignment.automation.Utilities.Vehicle;
import com.assignment.automation.Utilities.VehicleMapper;
import com.assignment.automation.factory.WebDriverFactory;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
//import com.aventstack.extentreports.reporter.ExtentReporter;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test automation suite which does following:
 *
 * 1. Reads given input file: car_input.txt
 * 2. Extracts vehicle registration numbers based on pattern(s).
 * 3. Each number extracted from input file is fed to https://cartaxcheck.co.uk/
 *          (Peform Free Car Check)
 * 4. Compare the output returned by https://cartaxcheck.co.uk/ with given car_output.txt
 * 5. Highlight/fail the test for any mismatches.
 *
 **/
public class AutomationTest {

    private WebDriver webDriver;

    private PageLoader page;

    private CarTaxCheckPage carTaxCheckPage;

    private CarTaxDetailsPage detailsPage;

    @Before
    public void setup() {
        if (webDriver == null) {
            webDriver = WebDriverFactory.create();
        }

        if (page == null) {
            page = new PageLoader(webDriver);
        }       
       
    }

    /**
     * Verifies each registration number available in input txt files, by using cartaxcheck.com website
     * and asserts the values.
     *
     * @throws Exception
     */
    @Test
    public void checkVehicleCarTax() throws Exception {
    	
    	ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("target\\extent.html");
    	   ExtentReports extent = new ExtentReports();
           extent.attachReporter(htmlReporter);
        ExtentTest test = extent.createTest("AutomationTest", "Validate Car Tax with portal : https://cartaxcheck.co.uk/ p");   
    	
        //Load car input file and read vehicle registration numbers
        List<String> vehicleRegistrationNumbers = FileHelper.getVehicleRegistrationNumbers();

        //Load expected car output file and create a map of vehicles.
        Map<String, Vehicle> expectedVehicles = FileHelper.getExpectedVehicleData();

        vehicleRegistrationNumbers.forEach(registrationNumber -> {
            //Loading Car Tax Check Page
            carTaxCheckPage = page.load(CarTaxCheckPage.class);
            assertTrue(carTaxCheckPage.isDisplayed());
            carTaxCheckPage.enterRegistrationNumber(registrationNumber);
            carTaxCheckPage.clickOnFreeCarCheckButton();
            test.log(Status.PASS, "Loading Car Tax Check Page");
            //Check that registration number
            String actualRegistrationNumber = carTaxCheckPage.getRegistrationNumber().replaceAll("\\s", StringUtils.EMPTY);
            String expectedRegistrationNumber = registrationNumber.replaceAll("\\s", StringUtils.EMPTY);
            assertThat(actualRegistrationNumber, is(equalTo(expectedRegistrationNumber)));
            test.log(Status.PASS, "Check registration number: "+actualRegistrationNumber);
            
            //Loading Car Details Page
            detailsPage = page.init(CarTaxDetailsPage.class);
            assertTrue(detailsPage.isDisplayed());
            test.log(Status.PASS, "Loading Car Details Page");
            
            //Verify car details with expected values from car_output.txt
            Vehicle actualVehicle = VehicleMapper.mapToActualVehicle(detailsPage);
            Vehicle expectedVehicle = expectedVehicles.get(actualVehicle.getRegistration());
            assertThat(actualVehicle, samePropertyValuesAs(expectedVehicle));
            test.log(Status.PASS, "Successfully Verified car details with expected values from car_output.txt");
            
            test.info("AutomationTest Executed Successfully");
            extent.flush();
        });

    }
}
