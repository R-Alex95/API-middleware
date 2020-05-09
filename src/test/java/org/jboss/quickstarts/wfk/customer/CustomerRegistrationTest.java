package org.jboss.quickstarts.wfk.customer;

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
import java.util.Date;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/*
 Run commands :
 mvn clean test -Parq-wildfly-remote
 OR for individual test
 mvn -Dtest= org.jboss.quickstarts.wfk.customer.CustomerRegistrationTest test -Parq-wildfly-remote
 */

@RunWith(Arquillian.class)
public class CustomerRegistrationTest {

    @Deployment
    public static Archive<?> createTestArchive() {
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
    CustomerRestService customerRestService;

    @Inject
    @Named("logger")
    Logger log;

    static Customer customer;

    @Test
    @InSequence(1)
    public void testRegister() throws Exception {
        CustomerInfo customer= createCustomerInstance("Jack Deee", "jack@mailinator.com", "04563426546");
        Response response = customerRestService.createCustomer(customer);
        CustomerRegistrationTest.customer = (Customer) response.getEntity();
        assertEquals("Unexpected response status", 201, response.getStatus());
        log.info(" New customer was persisted and returned status " + response.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(2)
    public void testInvalidRegister() {
        CustomerInfo customer= createCustomerInstance("", "", "");
        try {
            customerRestService.createCustomer(customer);
            fail("Expected a RestServiceException to be thrown");
        } catch(RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
            assertEquals("Unexpected response body", 3, e.getReasons().size());
            log.info("Invalid customerRegister attempt failed with return code " + e.getStatus());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(3)
    public void testDuplicateEmail() throws Exception {

        // Register a different user with the same email
        CustomerInfo anotherCustomer = createCustomerInstance("Alex RaN", "jack@mailinator.com", "04563426547");

        try {
            customerRestService.createCustomer(anotherCustomer);
            fail("Expected a RestServiceException to be thrown");
        } catch(RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
            assertTrue("Unexpected error. Should be Unique email violation", e.getCause() instanceof UniqueCustomerEmailException);
            assertEquals("Unexpected response body", 1, e.getReasons().size());
            log.info("Duplicate customerregister attempt failed with return code " + e.getStatus());
        }
    }

    @Test
    @InSequence(4)
    public void testGetById() throws Exception {
        Response response = customerRestService.retrieveCustomerById(CustomerRegistrationTest.customer.getId());
        assertEquals("Unexpected response status", 200, response.getStatus());
        assertEquals("Customer fetched is not the same", CustomerRegistrationTest.customer, response.getEntity());
        log.info("getCustomerById was successful and got the following object " + response.getEntity().toString());
    }

    @Test
    @InSequence(5)
    public void testGetByRegNo() throws Exception {
        Response response = customerRestService.retrieveCustomersByEmail(CustomerRegistrationTest.customer.getEmail());
        assertEquals("Unexpected response status", 200, response.getStatus());
        assertEquals("Customer fetched is not the same", CustomerRegistrationTest.customer, response.getEntity());
        log.info("getCustomerByRegNo was successful and got the following object " + response.getEntity().toString());
    }

    @Test
    @InSequence(6)
    public void testDelete() throws Exception {
        Response response = customerRestService.deleteCustomer(CustomerRegistrationTest.customer.getId());
        assertEquals("Unexpected response status", 204, response.getStatus());
        log.info("Deletion successful " + response.getStatusInfo());
    }

    /**
     * <p>A utility method to construct a {@link org.jboss.quickstarts.wfk.customer.Customer Customer} object for use in
     * testing. This object is not persisted.</p>
     *
     * @param name      The name of the Customer being created
     * @param email     The email address of the Customer being created
     * @param phone     The phone number of the Customer being created
     * @return The Customer object create
     */
    public static CustomerInfo createCustomerInstance(String name, String email, String phone) {
        CustomerInfo customer= new CustomerInfo();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
        return customer;
    }
}
