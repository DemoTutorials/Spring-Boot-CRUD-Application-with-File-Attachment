package com.employee.repository;

import com.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    boolean existsByEmpEmailOrContactNoAndIsDeletedFalse(String empEmail, String contactNo);

    List<Employee> findAllByIsDeletedFalse();

    Optional<Employee> findByIdAndIsDeletedFalse(Long id);
}
