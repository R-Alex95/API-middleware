package org.jboss.quickstarts.wfk.taxi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
// Run command : mvn clean test -Parq-wildfly-remote

// mvn -Dtest=org.jboss.quickstarts.wfk.taxi.TaxiRegistrationTest test -Parq-wildfly-remote
public class TaxiRegistrationTest {
    /**
     * <p>Compiles an Archive using Shrinkwrap, containing those external dependencies necessary to run the tests.</p>
     *
     * <p>Note: This code will be needed at the start of each Arquillian test, but should not need to be edited, except
     * to pass *.class values to .addClasses(...) which are appropriate to the functionality you are trying to test.</p>
     *
     * @return Micro test war to be deployed and executed.
     */
    @Deployment
    public static Archive<?> createTestArchive() {
        // This is currently not well tested. If you run into issues, comment line 67 (the contents of 'resolve') and
        // uncomment 65. This will build our war with all dependencies instead.
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
//                .importRuntimeAndTestDependencies()
                .resolve(
                        "io.swagger:swagger-jaxrs:1.5.16"
                ).withTransitivity().asFile();

        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addPackages(true, "org.jboss.quickstarts.wfk")
                .addAsLibraries(libs)
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("arquillian-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    TaxiRestService taxiRestService;

    @Inject
    @Named("logger")
    Logger log;

    static Taxi taxi;

    @Test
    @InSequence(1)
    public void testRegister() throws Exception {
        TaxiInfo taxi= createDefaultTaxiInstance();
        Response response = taxiRestService.createTaxi(taxi);
        TaxiRegistrationTest.taxi = (Taxi) response.getEntity();
        assertEquals("Unexpected response status", 201, response.getStatus());
        log.info(" New taxi was persisted and returned status " + response.getStatus() + " : " + this.taxi);
    }

    @Test
    @InSequence(2)
    public void testInvalidRegister() {
        TaxiInfo taxi= new TaxiInfo("", 0);
        try {
            taxiRestService.createTaxi(taxi);
            fail("Expected a RestServiceException to be thrown");
        } catch(RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
            assertEquals("Unexpected response body", 2, e.getReasons().size());
            log.info("Invalid taxiregister attempt failed with return code " + e.getStatus());
        }
    }

    @Test
    @InSequence(3)
    public void testDuplicateRegNo() throws Exception {
        try {
            taxiRestService.createTaxi(createDefaultTaxiInstance());
            fail("Expected a RestServiceException to be thrown");
        } catch(RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
            assertTrue("Unexpected error. Should be Unique Registration Number violation", e.getCause() instanceof UniqueTaxiRegNoException);
            log.info("Duplicate taxiregister attempt failed with return code " + e.getStatus());
        }
    }

    @Test
    @InSequence(4)
    public void testGetById() throws Exception {
        Response response = taxiRestService.retrieveTaxiById(TaxiRegistrationTest.taxi.getId());
        assertEquals("Unexpected response status", 200, response.getStatus());
        assertEquals("Taxi fetched is not the same", TaxiRegistrationTest.taxi, response.getEntity());
        log.info("getTaxiById was successful and got the following object " + response.getEntity().toString());
    }

    @Test
    @InSequence(5)
    public void testGetByRegNo() throws Exception {
        Response response = taxiRestService.retrieveTaxisByRegNo(TaxiRegistrationTest.taxi.getRegNo());
        assertEquals("Unexpected response status", 200, response.getStatus());
        assertEquals("Taxi fetched is not the same", TaxiRegistrationTest.taxi, response.getEntity());
        log.info("getTaxiByRegNo was successful and got the following object " + response.getEntity().toString());
    }

    @Test
    @InSequence(6)
    public void testDelete() throws Exception {
        Response response = taxiRestService.deleteTaxi(TaxiRegistrationTest.taxi.getId());
        assertEquals("Unexpected response status", 204, response.getStatus());
    }

    /**
     * An utility method to create a Taxi object with default parameters.
     *
     * @return The Taxi object created
     */
    public static TaxiInfo createDefaultTaxiInstance() {
        return new TaxiInfo("3RD4FH4",6);
    }

}