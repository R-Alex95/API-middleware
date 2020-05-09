package org.jboss.quickstarts.wfk.guestbooking;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.booking.BookingRestService;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerInfo;
import org.jboss.quickstarts.wfk.customer.CustomerRegistrationTest;
import org.jboss.quickstarts.wfk.customer.CustomerRestService;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiInfo;
import org.jboss.quickstarts.wfk.taxi.TaxiRegistrationTest;
import org.jboss.quickstarts.wfk.taxi.TaxiRestService;
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

@RunWith(Arquillian.class)
// Run command : mvn clean test -Parq-wildfly-remote
public class GuestBookingTest {

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
    CustomerRestService customerRestService;
    @Inject
    TaxiRestService taxiRestService;

    @Inject
    BookingRestService bookingRestService;

    @Inject
    GuestBookingRestService guestBookingRestService;

    @Inject
    @Named("logger") Logger log;

    static Taxi taxi;
    static Customer customer;
    static Booking booking;

    //Set millis 498484800000 from 1985-10-10T12:00:00.000Z
    private Date pastDate = new Date(498484800000L);

    //Set to a future date : 18/12/19 7:33 PM in millis
    private Date futureDate = new Date(1576697580000L);

    @Test
    @InSequence(1)
    public void testRegister() throws Exception {
        GuestBookingTest.taxi = persistsTaxi();
        GuestBooking guestBooking = createGuestBookingInstance(createDefaultCustomerInfo(), GuestBookingTest.taxi.getId(),  futureDate);
        Response response = guestBookingRestService.createGuestBooking(guestBooking);
        GuestBookingTest.booking = (Booking) response.getEntity();
        GuestBookingTest.customer = booking.getCustomer();

        assertEquals("Unexpected response status", 201, response.getStatus());
        log.info(" New contact was persisted and returned status " + response.getStatus());
    }

    @Test
    @InSequence(2)
    public void testInvalidRegister() throws Exception {
        GuestBooking guestBooking = createGuestBookingInstance(createDefaultCustomerInfo(), GuestBookingTest.taxi.getId(),  futureDate);
        try {
            Response response = guestBookingRestService.createGuestBooking(guestBooking);
        } catch (RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.INTERNAL_SERVER_ERROR, e.getStatus());
        }
    }


    private GuestBooking createGuestBookingInstance(CustomerInfo customerInfo, long taxi_id, Date bookingDate) {
        return new GuestBooking(customerInfo,taxi_id,bookingDate);
    }

    private CustomerInfo createDefaultCustomerInfo() {
        return new CustomerInfo("Random Name", "testemail1995@hotmail.com", "01234567898");
    }

    private CustomerInfo createCustomerInstance(String name, String email, String phone) {
        CustomerInfo customer= new CustomerInfo();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
        return customer;
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