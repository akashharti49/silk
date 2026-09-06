package com.gld.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Component
public class Silk {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Size(min = 3, max = 30, message = "* Enter between 3~30 characters")
    private String name;

    @NotNull(message = "* Enter Proper Value")
    @DecimalMax(value = "100000", message = "* Enter below 1 lakh rs")
    @DecimalMin(value = "500", message = "* Enter above 500rs")
    private double price;

    @NotNull(message = "* Enter Proper Value")
    @DecimalMax(value = "100000", message = "* Enter below 1 lakh rs")
    @DecimalMin(value = "500", message = "* Enter above 500rs")
    private double dummyprice;

    @NotNull(message = "* Enter Proper Value")
    @Min(value = 0, message = "* Should be atleast One")
    @Max(value = 100, message = "* Maximum 100 is allowed")
    private int stock;

    @Size(min = 15, max = 200, message = "* Enter between 15~200 characters")
    private String description;

    @ElementCollection
    private List<String> imageLinks;

    @NotEmpty(message = "* Enter Something")
    private String silkType;

    @NotEmpty(message = "* Enter Something")
    private String fabric;
    
    @NotEmpty(message = "* Enter Something")
    private String color;

    @NotEmpty(message = "* Enter Something")
    private String weavingType;

    @NotEmpty(message = "* Enter Something")
    private String origin;

    @NotEmpty(message = "* Enter Something")
    private String design;

    private String finish;

    private String weight;

    private String width;
    
    private boolean published;
    
    

}
