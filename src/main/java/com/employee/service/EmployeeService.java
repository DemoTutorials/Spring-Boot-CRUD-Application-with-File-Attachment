package com.employee.service;

import com.employee.dto.EmployeeRequestDTO;
import com.employee.dto.EmployeeResponseDTO;
import com.employee.dto.FileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    EmployeeResponseDTO create(EmployeeRequestDTO employeeRequestDTO);

    List<EmployeeResponseDTO> getAll();

    EmployeeResponseDTO getById(Long id);

    EmployeeResponseDTO update(Long id, EmployeeRequestDTO employeeRequestDTO);

    void delete(Long id);

    EmployeeResponseDTO patchUpdate(Long id, Map<String, Object> updates);

    FileDto uploadFile(Long id, MultipartFile file);

    FileDto downloadFile(Long id);

    EmployeeRequestDTO uploadJsonWithFile(EmployeeRequestDTO employeeRequestDTO, MultipartFile file);
}
