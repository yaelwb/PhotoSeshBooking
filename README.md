# PhotoSeshBooking - work in progress
PhotoSeshBooking – A service for the busy photographer, to keep track of customers and bookings. 
RESTful Api Server, Java 8, Play Framework, PostgreSql, Hibernate.
<ul>

  <li>
    <b>Schema: </b>
    <ul>
    <li>conf/schema.sql - Customer, Status, Booking</li>
    </ul>
  </li>
  <li>
    <b>Customer:</b>
    <ul>
    <li>model/Customer - First name, last name, email, phone, pay method, balance</li>
    <li>app/controllers/CustomerController.java - create, get (all, by id, by name), update, delete</li>
    </ul>
  </li>
  <li>
    <b>Booking:</b>
    <ul>
        <li>model/Booking: id, customer id, status id <br>
        Event: date, location, type, duration, price, key attendees <br>
        Preparation: requirements, equipment, camera settings, optimal lighting spots <br>
        Editing: num_pics, num_selected, num_processed
        </li>
        <li>app/controllers/BookingController.java - create</li>
        </ul>
  </li>
  <li>
    <b>Utilities:</b>
    <ul>
    <li>utilities/ActionAuthenticator.java - basic authentication</li>
    <li>utilities/Parse.java - validate name, phone, email strings</li>
    <li>utilities/RequestUtil.java - get parameters from query request, paginate results</li>
    <li>utilities/StatusUtil.java - cache for Status, state, status id - to reduce db calls</li>
    </ul>
  </li>
  <li>
    <b>Enumerates:</b>
    <ul>
    <li>Status</li>
    <li>PayMethod</li>
    </ul>
  </li>
  <li>
    <b>Settings:</b>
    <ul>
    <li>conf/routes</li>
    <li>conf/application.conf</li>
    <li>conf/META-INF/persistence.xml</li>
    <li>build.sbt</li>
    </ul>
  </li>
</ul>
