# PhotoSeshBooking
PhotoSeshBooking – A service for the busy photographer, to keep track of customers and bookings. 
RESTful Api Server, Java 8, Play Framework, PostgreSql, Hibernate.
<br>
<i>
[Back End Engineer available for hire @ San Francisco Bay Area](https://www.linkedin.com/in/yaelwb) 
</i>
<br>
<br>
[Booking flow chart diagram](Photoshoot - flow chart.png)
<br>
Main functionality:
<br>
[app/services/BookingServiceImpl.java](app/services/BookingServiceImpl.java)
<br>
[app/services/CustomerServiceImpl.java](app/services/CustomerServiceImpl.java)
<br>
<ul>
     <li><b>POST    /customers </b>Create a customer</li>
     <li><b>GET     /customers </b>View all customers - with filters</li>
     <li><b>GET     /customers/byId </b>View a customer by id</li>
     <li><b>GET     /customers/byName </b>View a customer by name</li>
     <li><b>PUT     /customers </b>Update a customer</li>
     <li><b>DELETE  /customers </b>Delete a customer</li>
     <li><b>DELETE  /customers/DeleteAll </b>Delete all customers</li>
     <li><b>POST    /bookings </b>Create a booking</li>
     <li><b>GET     /bookings </b>View all bookings - with filters</li>
     <li><b>GET     /bookings/byId </b>View a booking by id</li>
     <li><b>GET     /bookings/byNumTodo </b>View all bookings with number of images to be processed within a given range</li>
     <li><b>PUT     /bookings </b>Update a booking. directed to the correct flow by statuses of original and updated bookings</li>
     <li><b>DELETE  /bookings </b>Delete a booking</li>
     <li><b>DELETE  /bookings/DeleteAll </b>Delete all bookings</li>
</ul>
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
               <li>app/services/CustomerService.java - support for CustomerController</li>
               <li>app/services/CustomerServiceImpl.java</li>
          </ul>
     </li>
     <li>
          <b>Booking:</b>
          <ul>
               <li>model/Booking: id, customer id, status id <br>
                    Event: date, location, type, duration, price, amount paid key attendees <br>
                    Preparation: requirements, equipment, camera settings, optimal lighting spots <br>
                    Editing: number of pictures taken, number of pictures selected, number of pictures processed, review notes
               </li>
               <li>app/controllers/BookingController.java - create, get (all, by id, by filters), delete, update</li>
               <li>app/services/BookingService.java - support for BookingController</li>
               <li>app/services/BookingServiceImpl.java</li>
          </ul>
     </li>
     <li>
          <b>Utilities:</b>
          <ul>
               <li>utilities/ActionAuthenticator.java - basic authentication</li>
               <li>utilities/Parse.java - validate name, phone, email strings</li>
               <li>utilities/RequestUtil.java - get parameters from query request, paginate results</li>
               <li>utilities/StatusUtil.java - cache for Status, state, status id - to reduce db calls.
                    <br>Also assists the state machine via stateChangeExists(), stateChanges: mapping State->Set of States</li>
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
