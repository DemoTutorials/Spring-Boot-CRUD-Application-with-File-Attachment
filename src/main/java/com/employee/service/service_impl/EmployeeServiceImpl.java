package com.employee.service.service_impl;

import com.employee.dto.EmployeeRequestDTO;
import com.employee.dto.EmployeeResponseDTO;
import com.employee.dto.FileDto;
import com.employee.entity.Employee;
import com.employee.enums.BloodGroup;
import com.employee.exception.custom_exception.EmployeeAlreadyExistsException;
import com.employee.exception.custom_exception.EmployeeNotFoundException;
import com.employee.repository.EmployeeRepository;
import com.employee.service.EmployeeService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final String EMPLOYEE_NOT_FOUND_MSG = "Employee Not Found with ID: ";
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public EmployeeResponseDTO create(EmployeeRequestDTO employeeRequestDTO) {

        boolean exists = employeeRepository.existsByEmpEmailOrContactNoAndIsDeletedFalse(employeeRequestDTO.getEmpEmail(),employeeRequestDTO.getContactNo());
        if(exists){
            throw new EmployeeAlreadyExistsException("An employee already exists with the provided Email ID:- "+employeeRequestDTO.getEmpEmail()+" or Contact No.:-" +employeeRequestDTO.getContactNo());
        }
        Employee employee = modelMapper.map(employeeRequestDTO, Employee.class);
        Employee savedEmployee = employeeRepository.save(employee);
        return modelMapper.map(savedEmployee, EmployeeResponseDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EmployeeResponseDTO> getAll() {
        List<Employee> employees = employeeRepository.findAllByIsDeletedFalse();
        if(employees.isEmpty()){
            throw new EmployeeNotFoundException("Employees Not Found!...");
        }
        return employees.stream().map(employee -> modelMapper.map(employee, EmployeeResponseDTO.class)).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public EmployeeResponseDTO getById(Long id) {
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_MSG + id));
        return modelMapper.map(employee, EmployeeResponseDTO.class);
    }

    @Transactional(readOnly = false)
    @Override
    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO employeeRequestDTO) {
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_MSG + id));
        modelMapper.map(employeeRequestDTO,employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        return modelMapper.map(updatedEmployee, EmployeeResponseDTO.class);
    }

    @Transactional(readOnly = false)
    @Override
    public void delete(Long id) {
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_MSG + id));
        employee.setIsActive(false);
        employee.setIsDeleted(true);
        employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    @Override
    public EmployeeResponseDTO patchUpdate(Long id, Map<String, Object> updates) {
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_MSG + id));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        updates.forEach((field,value)->{
            try {
                updateField(employee, field, value, dateFormatter);
            } catch (IllegalArgumentException e) {
                throw e;                    // rethrow known validation errors
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to update field '" + field + "': " + e.getMessage(), e);
            }
        });
        Employee updatedEmployee = employeeRepository.save(employee);
        return modelMapper.map(updatedEmployee, EmployeeResponseDTO.class);
    }

    @Override
    public FileDto uploadFile(Long id, MultipartFile file) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_MSG + id));
        try {
            employee.setFileName(file.getOriginalFilename());
            employee.setFileType(file.getContentType());
            employee.setFileData(file.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file content", e);
        }
        Employee updateEmployee = employeeRepository.save(employee);
        return new FileDto(updateEmployee.getFileName(),updateEmployee.getFileType(),updateEmployee.getFileData());
    }

    @Transactional(readOnly = false)
    @Override
    public FileDto downloadFile(Long id) {
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND_MSG + id));
        if (employee.getFileData() == null) {
            throw new IllegalStateException("No file attached to this employee");
        }
        return new FileDto(employee.getFileName(), employee.getFileType(), employee.getFileData());
    }

    @Override
    public EmployeeRequestDTO uploadJsonWithFile(EmployeeRequestDTO employeeRequestDTO, MultipartFile file) {
        Employee employee = modelMapper.map(employeeRequestDTO, Employee.class);
        try {
            if(file!=null && !file.isEmpty()){
                employee.setFileName(file.getOriginalFilename());
                employee.setFileType(file.getContentType());
                employee.setFileData(file.getBytes());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to process uploaded file", e);
        }
        Employee updatedEmployee = employeeRepository.save(employee);
        return modelMapper.map(updatedEmployee, EmployeeRequestDTO.class);
    }

    private void updateField(Employee employee, String field, Object value, DateTimeFormatter fmt) {
        switch (field) {
            case "empName" -> employee.setEmpName((String) value);
            case "empEmail" -> employee.setEmpEmail((String) value);
            case "contactNo" -> employee.setContactNo((String) value);
            case "address" -> employee.setAddress((String) value);

            case "salary"     -> setSalary(employee, value);
            case "birthDate"  -> setBirthDate(employee, value, fmt);
            case "bloodGroup" -> setBloodGroup(employee, value);

            default -> throw new IllegalArgumentException("Field is not supported: " + field);
        }
    }

    private void setSalary(Employee employee, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Salary cannot be null");
        }

        if (value instanceof Number num) {
            employee.setSalary(BigDecimal.valueOf(num.doubleValue()));
            return;
        }

        if (value instanceof String str) {
            try {
                employee.setSalary(new BigDecimal(str.trim()));
                return;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid salary format: " + str);
            }
        }

        throw new IllegalArgumentException("Salary must be number or numeric string, got: " + value.getClass().getSimpleName());
    }

    private void setBirthDate(Employee employee, Object value, DateTimeFormatter fmt) {
        if (value instanceof String str) {
            try {
                employee.setBirthDate(LocalDate.parse(str.trim(), fmt));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid birthDate format. Expected dd-MMM-yyyy, got: " + str);
            }
        } else {
            throw new IllegalArgumentException("birthDate must be a string in format dd-MMM-yyyy");
        }
    }

    private void setBloodGroup(Employee employee, Object value) {
        if (value instanceof BloodGroup bg) {
            employee.setBloodGroup(bg);
        } else if (value instanceof String str) {
            employee.setBloodGroup(BloodGroup.fromString(str.trim()));
        } else {
            throw new IllegalArgumentException(
                    "bloodGroup must be BloodGroup enum or valid string, got: " +
                            (value != null ? value.getClass().getSimpleName() : "null")
            );
        }
    }
}
