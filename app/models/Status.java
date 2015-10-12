package models;

import javax.persistence.*;

/**
 * Created by yael on 10/11/15.
 */
@Entity
@Table(name = "status")
public class Status {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="status_id_seq",
            sequenceName="status_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "status_id_seq")
    private Long id;

    @Column(name="state")
    private String state;

    @Column(name="description")
    private String description;

    public Status(String state, String description) {
        this.state = state;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

}
