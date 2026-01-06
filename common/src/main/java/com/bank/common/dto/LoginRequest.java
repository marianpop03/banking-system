package com.bank.common.dto;
import lombok.Data;
import java.io.Serializable;

@Data
public class LoginRequest implements Serializable {
    private Long id;
    private String username;
    private String password;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}