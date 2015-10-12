# PhotoSeshBooking - work in progress
PhotoSeshBooking – A service for the busy photographer, to keep track of customers and bookings. 
RESTful Api Server, Java 8, Play Framework, PostgreSql, Hibernate.
<ul>
<li>
Schema: conf/schema.sql - Customer, Status, Booking
</li>
<li>
Customer:
model/Customer - First name, last name, email, phone, pay method, balnce
app/controllers/CustomerController.java - create, get (all, by id, by name), update, delete
</li>
<li>
Booking: TODO
</li>
<li>
Utilities:
utilities/ActionAuthenticator.java - basic authentication
utilities/Parse.java - validate name, phone, email strings
utilities/RequestUtil.java - get parameters from query request, paginate results
</li>
<li>
Enumerates:
Status
PayMethod
</li>
<li>
Settings:
conf/routes
conf/application.conf
conf/META-INF/persistence.xml
build.sbt
</li>
</ul>
