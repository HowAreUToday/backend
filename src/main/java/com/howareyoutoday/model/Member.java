package com.howareyoutoday.model;

import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
    @Id
    private int ID;
    private String Email;
    private String Name;
    private String Age;
    private String Gender;
    private String ImgPath;
    private String Memo;
    private String Token;
}