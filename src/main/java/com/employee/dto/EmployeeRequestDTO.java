package com.employee.dto;

import com.employee.enums.BloodGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeRequestDTO {

    @NotBlank(message = "Name is Required")
    private String empName;

    @NotBlank(message = "Email is Required")
    private String empEmail;

    @NotBlank(message = "Contact No is Required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String contactNo;

    @NotBlank(message = "Address is Required")
    private String address;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    private BigDecimal salary;

    @NotNull(message = "BirthDate is Required")
    @Past(message = "Birth date must be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MMM-yyyy")
    private LocalDate birthDate;

    @NotNull(message = "Blood Group is required")
    private BloodGroup bloodGroup;
}
