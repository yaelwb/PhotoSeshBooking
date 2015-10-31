package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import enums.State;
import utilities.StatusUtil;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Created by yael on 10/11/15.
 */
@Entity
@Table(name = "booking")
public class Booking {

    public Booking() {}

    public Booking(Long customerId) {
        this.customerId = customerId;
    }

    @Id
    @Column(name="id")
    @SequenceGenerator(name="booking_id_seq",
            sequenceName="booking_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_id_seq")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    //region customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", updatable = false, insertable = false)
    private Customer customer;

    @Column(name = "customer_id")
    private Long customerId;

    @JsonBackReference(value = "booking-customer_id")
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    //endregion

    //region status
    @Column(name = "status_id", nullable = false)
    private Long statusId;

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    @ManyToOne(optional=false)
    @JoinColumn(name="status_id",referencedColumnName="id", insertable = false, updatable = false)
    private Status status;

    public void setStatus(String status) {
        if(!status.isEmpty()) {
            Long id = StatusUtil.getStatusId(status);
            setStatusId(id);
        }
    }

    public String getStatus() {
        Status status = StatusUtil.getStatus(getStatusId());
        if(status != null) {
            return status.getState();
        }
        return null;
    }
    //endregion


    @Column(name = "event_date")
    private Timestamp eventDate;

    @Column
    private String location;

    @Column(name = "event_type")
    private String eventType;

    @Column
    private BigDecimal duration;

    @Column
    private BigDecimal price;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "key_attendees")
    private String keyAttendees;

    @Column
    private String requirements;

    @Column
    private String equipment;

    @Column(name = "camera_settings")
    private String cameraSettings;

    @Column(name = "optimal_lighting_spots")
    private String optimalLightingSpots;

    @Column(name = "num_pics")
    private int numPics;

    @Column(name = "num_selected")
    private int numSelected;

    @Column(name = "num_processed")
    private int numProcessed;

    @Column(name = "review_notes")
    private String reviewNotes;

    @Override
    public String toString() {
        return "id: " + id +
                ", customer id: " + customerId +
                ", status id: " + statusId +
                ", status: " + this.getStatus() +
                ((eventDate == null)? "" : ", event date: " + eventDate) +
                ((location == null)? "" : ", location: " + location) +
                ((eventType == null)? "" : ", event type: " + eventType) +
                ((duration == null)? "" : ", duration: " + duration) +
                ((price == null)? "" : ", price: " + price) +
                ((amountPaid == null)? "" : ", amount paid: " + amountPaid) +
                ((keyAttendees == null)? "" : ", key attendees: " + keyAttendees) +
                ((requirements == null)? "" : ", requirements: " + requirements) +
                ((equipment == null)? "" : ", equipment: " + equipment) +
                ((cameraSettings == null)? "" : ", camera settings: " + cameraSettings) +
                ((optimalLightingSpots == null)? "" : ", optimal lighting spots: " + optimalLightingSpots) +
                ((numPics <= 0)? "" : ", num pics: " + numPics) +
                ((numSelected <= 0)? "" : ", num selected: " + numSelected) +
                ((numProcessed <= 0)? "" : ", num processed: " + numProcessed) +
                ((requirements == null)? "" : ", review notes: " + reviewNotes);
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public BigDecimal getDuration() {
        return duration;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getKeyAttendees() {
        return keyAttendees;
    }

    public void setKeyAttendees(String keyAttendees) {
        this.keyAttendees = keyAttendees;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getCameraSettings() {
        return cameraSettings;
    }

    public void setCameraSettings(String cameraSettings) {
        this.cameraSettings = cameraSettings;
    }

    public String getOptimalLightingSpots() {
        return optimalLightingSpots;
    }

    public void setOptimalLightingSpots(String optimalLightingSpots) {
        this.optimalLightingSpots = optimalLightingSpots;
    }

    public int getNumPics() {
        return numPics;
    }

    public void setNumPics(int numPics) {
        this.numPics = numPics;
    }

    public int getNumSelected() {
        return numSelected;
    }

    public void setNumSelected(int numSelected) {
        this.numSelected = numSelected;
    }

    public int getNumProcessed() {
        return numProcessed;
    }

    public void setNumProcessed(int numProcessed) {
        this.numProcessed = numProcessed;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }
}
