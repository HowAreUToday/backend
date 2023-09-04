package com.howareyoutoday.model;

import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Chat {
    @Id
    private int ID;
    private String Message;
    private String ChatDate;
    private Boolean Who;
}