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
        this.statusId = StatusUtil.getStatusId(State.CREATED.toString());
    }

    @Id
    @Column(name="id")
    @SequenceGenerator(name="booking_id_seq",
            sequenceName="booking_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_id_seq")
    private Long id;

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

    @Column(name = "key_attendees")
    private String key_attendees;

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

    @Override
    public String toString() {
        return "Booking{" +
                "customerId=" + customerId +
                ", status=" + status +
                ", eventDate=" + eventDate +
                ", location='" + location + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
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

    public String getKey_attendees() {
        return key_attendees;
    }

    public void setKey_attendees(String key_attendees) {
        this.key_attendees = key_attendees;
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
}
