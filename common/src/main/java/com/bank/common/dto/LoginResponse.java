package com.bank.common.dto;
import lombok.Data;
import java.io.Serializable;

@Data
public class LoginResponse implements Serializable {
    private String token;
}