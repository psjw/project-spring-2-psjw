package com.psjw.thisbox.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@Getter
public class Role {
    @Id
    @GeneratedValue
    Long id;

    private Long userId;

    private String name;




    public Role(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public Role(String name) {
        this(null, name);
    }
}
