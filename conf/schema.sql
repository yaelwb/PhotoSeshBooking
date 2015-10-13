CREATE TABLE customer
(         
	id BIGSERIAL PRIMARY KEY NOT NULL,   
	first_name VARCHAR(30) NOT NULL, 
	last_name VARCHAR(30) NOT NULL,  
	email VARCHAR(30) NOT NULL, 
	phone VARCHAR(30) NOT NULL, 
	pay_method VARCHAR(30) NOT NULL,
	balance NUMERIC NOT NULL
);

ALTER SEQUENCE customer_id_seq RESTART WITH 41000;

CREATE TABLE status
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    state VARCHAR(40) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE booking 
(         
	id BIGSERIAL PRIMARY KEY NOT NULL, 
	customer_id BIGINT NOT NULL,
	event_date TIMESTAMP,
	location VARCHAR(60), 
	event_type VARCHAR(30),
	duration NUMERIC,
	price NUMERIC,
 	key_attendees VARCHAR(255),
	requirements VARCHAR(255), 
	equipment VARCHAR(255), 
	camera_settings VARCHAR(255),
	optimal_lighting_spots VARCHAR(255),
	status_id BIGINT NOT NULL,
	num_pics INT,
	num_selected INT,
	num_processed INT,
	FOREIGN KEY ( customer_id ) REFERENCES customer ( id ),
	FOREIGN KEY ( status_id ) REFERENCES status ( id )
);

INSERT INTO status (state, description) VALUES
('CREATED', 'Customer contacted with a request for a photoshoot, initial details given');

INSERT INTO status (state, description) VALUES
('BOOKED', 'Both parties agreed on a photoshoot: event type, date, location, and amount are set - can be altered later');

INSERT INTO status (state, description) VALUES
('DOWNPAYMENT', 'Initial payment is collected');

INSERT INTO status (state, description) VALUES
('PREPARATION', 'Gathering requirements and key attendees, inspecting the location and making decisions regarding optimal lighting, equipment and camera settings');

INSERT INTO status (state, description) VALUES
('PHOTOSHOOT', 'Day of the event - from arriving at the location till uploading/backup all the images');

INSERT INTO status (state, description) VALUES
('PAYMENT', 'Payment is collected, prior to sending the final images');

INSERT INTO status (state, description) VALUES
('SELECTIONS', 'Making selects of images to be processed/edited');

INSERT INTO status (state, description) VALUES
('EDITING', 'Lightroom CC/Photoshop edits - depend on requirement and level of processing required');

INSERT INTO status (state, description) VALUES
('REVIEW', 'Processed images sent to customer for review. Can lead back to EDITING state');

INSERT INTO status (state, description) VALUES
('COMPLETE', 'Customer approved all edits, and balance is paid in full');

INSERT INTO status (state, description) VALUES
('CANCELED', 'Either party had to cancel - date is cleared, amount and customers balance might be affected');

INSERT INTO status (state, description) VALUES
('POSTPONED', 'Either party had to postpone - date, amount, balance might be affected, as well as camera/light settings');