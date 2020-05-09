package org.jboss.quickstarts.wfk.booking;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerInfo;
import org.jboss.quickstarts.wfk.customer.CustomerRegistrationTest;
import org.jboss.quickstarts.wfk.customer.CustomerRestService;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiInfo;
import org.jboss.quickstarts.wfk.taxi.TaxiRegistrationTest;
import org.jboss.quickstarts.wfk.taxi.TaxiRestService;

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
import java.util.Calendar;
import java.sql.Date;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/*
 mvn -Dtest=org.jboss.quickstarts.wfk.booking.BookingRegistrationTest test -Parq-wildfly-remote

 Run command : mvn clean test -Parq-wildfly-remote
 */
@RunWith(Arquillian.class)
public class BookingRegistrationTest {

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
    private CustomerRestService customerRestService;

    @Inject
    private BookingRestService bookingRestService;

    @Inject
    private TaxiRestService taxiRestService;

    @Inject
    @Named("logger")
    Logger log;

    static Customer cust;

    static Taxi taxi;

    static Booking booking;

    //Set millis 498484800000 from 1985-10-10T12:00:00.000Z
    private Date pastDate = new Date(498484800000L);

    //Set to a future date : 12/18/19 7:33 PM in millis
    private Date futureDate = new Date(1576697580000L);


    @Test
    @InSequence(1)
    public void testRegister() throws Exception {
        Customer customer = persistsCustomer();
        BookingRegistrationTest.cust = customer;
        Taxi taxi = persistsTaxi();
        BookingRegistrationTest.taxi = taxi;

        BookingInfo booking = createBookingInfoInstance(customer, taxi, futureDate);
        Response response = bookingRestService.createBooking(booking);

        BookingRegistrationTest.booking = (Booking) response.getEntity();
        assertEquals("Unexpected response status", 201, response.getStatus());
        log.info(" New booking was persisted and returned status " + response.getStatus());
    }

    @Test
    @InSequence(2)
    public void testInvalidRegisterCustomerNotFound() {
        // Create a customer with a ID belonging to a persisted taxi so no customer can be found with that ID
        BookingInfo booking = new BookingInfo(600,10010, futureDate);

        try {
            bookingRestService.createBooking(booking);
            fail("Expected a RestServiceException to be thrown");
        } catch (RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
            for (String reason: e.getReasons().values()){
                log.info(reason);
            }
            log.info("Invalid booking register attempt failed with return code " + e.getStatus());
        }
    }

    @Test
    @InSequence(3)
    public void testInvalidRegisterTaxiNotFound() {
        // Create a taxi with a ID belonging to a persisted customer
        BookingInfo booking = new BookingInfo(BookingRegistrationTest.cust.getId(),10001,futureDate);

        try {
            bookingRestService.createBooking(booking);
            fail("Expected a RestServiceException to be thrown");
        } catch (RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
            log.info("Invalid booking register attempt failed with return code " + e.getStatus());
        }
    }

    @Test
    @InSequence(3)
    public void testInvalidRegisterInvalidDate() {
        BookingInfo booking = new BookingInfo(BookingRegistrationTest.cust.getId(),BookingRegistrationTest.taxi.getId(),pastDate);
        try {
            bookingRestService.createBooking(booking);
            fail("Expected a RestServiceException to be thrown");
        } catch (RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
            for (String reason: e.getReasons().values()){
                log.info(reason);
            }
            log.info("Invalid booking register attempt failed with return code " + e.getStatus());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(4)
    public void testDuplicateBooking() throws Exception {
        BookingInfo anotherBooking = createBookingInfoInstance(BookingRegistrationTest.cust,taxi,futureDate);

        try {
            bookingRestService.createBooking(anotherBooking);
            fail("Expected a RestServiceException to be thrown");
        } catch (RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
            assertTrue("Unexpected error. Should be Unique email violation", e.getCause() instanceof UniqueBookingException);
            for (String reason: e.getReasons().values()){
                log.info(reason);
            }
            log.info("Duplicate booking register attempt failed with return code " + e.getStatus());
        }
    }

    @Test
    @InSequence(5)
    public void testGetById() throws Exception {
        Response response = bookingRestService.retrieveBookingByCustomerId(BookingRegistrationTest.cust.getId());
        assertEquals("Unexpected response status", 200, response.getStatus());
        List<Booking> bookings = (List<Booking>) response.getEntity();
        assertEquals("Booking fetched is not the same", BookingRegistrationTest.booking,bookings.get(0) );
        log.info("getCustomerById was successful and got the following object " + response.getEntity().toString());
    }

    @Test
    @InSequence(5)
    public void testGetAll() throws Exception {
        // since you have 1 booking check that one
        Response response = bookingRestService.retrieveAllBookings();
        assertEquals("Unexpected response status", 200, response.getStatus());
        List<Booking> bookings = (List<Booking>) response.getEntity();
        assertEquals("Booking fetched is not the same", BookingRegistrationTest.booking,bookings.get(0) );
        log.info("getCustomerById was successful and got the following object " + response.getEntity().toString());
    }

    @Test
    @InSequence(6)
    public void testDelete() throws Exception {
        Response response = bookingRestService.deleteBooking(BookingRegistrationTest.booking.getId());
        assertEquals("Unexpected response status", 204, response.getStatus());
        log.info("Deletion successful " + response.getStatusInfo());
    }

    // TESTING CASCADE DELETION

    @Test
    @InSequence(7)
    public void testDeleteCustomer() throws Exception {
        BookingInfo booking = createBookingInfoInstance(BookingRegistrationTest.cust, BookingRegistrationTest.taxi, futureDate);
        Response response = bookingRestService.createBooking(booking);
        BookingRegistrationTest.booking = (Booking) response.getEntity();
        assertEquals("Unexpected response status", 201, response.getStatus());

        Response responseCustDelete = customerRestService.deleteCustomer(BookingRegistrationTest.cust.getId());
        assertEquals("Unexpected response status", 204, responseCustDelete.getStatus());
        log.info("Customer's deletion was successful " + response.getStatusInfo());
        try {
            Response responseBookingDelete = bookingRestService.deleteBooking(BookingRegistrationTest.booking.getId());
            fail();
        } catch(RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
            for (String s:e.getReasons().values()) {
                log.info(s);
            }
        }
    }

    @Test
    @InSequence(8)
    public void testDeleteTaxi() throws Exception {
        BookingInfo booking = createBookingInfoInstance(this.persistsCustomer(), BookingRegistrationTest.taxi, futureDate);
        Response response = bookingRestService.createBooking(booking);
        BookingRegistrationTest.booking = (Booking) response.getEntity();
        assertEquals("Unexpected response status", 201, response.getStatus());

        Response responseTaxiDelete = taxiRestService.deleteTaxi(BookingRegistrationTest.taxi.getId());
        assertEquals("Unexpected response status", 204, responseTaxiDelete.getStatus());
        log.info("Customer's deletion was successful " + response.getStatusInfo());

        try {
            Response responseBookingDelete = bookingRestService.deleteBooking(BookingRegistrationTest.booking.getId());
            fail();
        } catch(RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
            for (String s:e.getReasons().values()) {
                log.info(s);
            }
        }
    }

    /**
     * <p>A utility method to construct a {@link org.jboss.quickstarts.wfk.booking.Booking Booking} object for use in
     * testing. This object is not persisted.</p>
     *
     * @return The Booking object create
     */
    public static BookingInfo createBookingInfoInstance() {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2019);
        cal.set(Calendar.MONTH, 12);
        cal.set(Calendar.DATE, 3);

        /*
         * Creating a bookingInfo from already persisted Customer & Taxi objects.
         * These objects are taken from the import.sql file
         */

        BookingInfo bookingInfo = new BookingInfo(10002,10010,cal.getTime());

        return bookingInfo;
    }

    public static BookingInfo createBookingInfoInstance(Customer customer, Taxi taxi, Date date) {
        return new BookingInfo(customer.getId(),taxi.getId(),date);
    }

    public Customer persistsCustomer() {
        CustomerInfo customer = CustomerRegistrationTest.createCustomerInstance("Jack Deee", "jack@mailinator.com", "04563426546");
        Response response = customerRestService.createCustomer(customer);
        assertEquals("Unexpected response status", 201, response.getStatus());
        return (Customer)response.getEntity();
    }

    public Taxi persistsTaxi() {
        TaxiInfo taxi = TaxiRegistrationTest.createDefaultTaxiInstance();
        Response response = taxiRestService.createTaxi(taxi);
        assertEquals("Unexpected response status", 201, response.getStatus());
        return (Taxi)response.getEntity();
    }
}
