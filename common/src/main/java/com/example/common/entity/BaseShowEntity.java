package com.example.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class BaseShowEntity extends BaseEntity {

    @Column(nullable = false)
    private boolean show = true;

    public void deleteShow() {
        this.show = false;
    }

    public void depositShow() {
        this.show = true;
    }
}
