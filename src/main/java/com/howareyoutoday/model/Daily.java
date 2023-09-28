package com.howareyoutoday.model;

import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// @Entity
// @Table(name = "AcntBck", schema = "Management")
public class Daily {
    @Id
    private int ID;
    private String Day;
    private String Text;
    private Integer Emotion;
}

