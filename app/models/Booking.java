package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.util.EnumValues;
import enums.State;
import utilities.StatusMap;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Created by yael on 10/11/15.
 */
@Entity
public class Booking {

    //region customer id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", updatable = false, insertable = false)
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
    private long statusId;

    public long getStatusId() {
        return statusId;
    }

    public void setStatusId(long statusId) {
        this.statusId = statusId;
    }

    @ManyToOne(optional=false)
    @JoinColumn(name="status_id",referencedColumnName="id", insertable = false, updatable = false)
    private Status status;

    public void setStatus(String status) {
        if(!status.isEmpty()) {
            Long id = StatusMap.getStatusId(status);
            setStatusId(id);
        }
    }

    public String getStatus() {
        Status status = StatusMap.getStatus(getStatusId());
        if(status != null) {
            return status.getState();
        }
        return null;
    }

    public String getStatusDescription() {
        Status status = StatusMap.getStatus(getStatusId());
        if(status != null) {
            return status.getDescription();
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

}
