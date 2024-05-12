package com.bsep.marketingacency.controller;

import com.bsep.marketingacency.dto.EmployeeDto;
import com.bsep.marketingacency.model.Employee;
import com.bsep.marketingacency.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@CrossOrigin(origins = "https://localhost:4200")
@RequestMapping("/employee")
public class EmployeeController {
    private Logger logger =  LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/byUserId/{userId}")
    public ResponseEntity<EmployeeDto> getEmployeeByUserId(@PathVariable Long userId) {
        Employee employee = employeeService.getEmployeeByUserId(userId);
        if (employee != null) {
            EmployeeDto employeeDto = new EmployeeDto(
                    employee.getId(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getAddress(),
                    employee.getCity(),
                    employee.getCountry(),
                    employee.getPhoneNumber(),
                    employee.getUserId()
            );
            return new ResponseEntity<>(employeeDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_EMPLOYEE_PERMISSION')")
    @PutMapping("/update")
    public ResponseEntity<EmployeeDto> updateEmployee(@RequestBody EmployeeDto employeeDto) {
        // Convert EmployeeDto to Employee object
        Employee updatedEmployee = new Employee(
                employeeDto.getId(),
                employeeDto.getFirstName(),
                employeeDto.getLastName(),
                employeeDto.getAddress(),
                employeeDto.getCity(),
                employeeDto.getCountry(),
                employeeDto.getPhoneNumber(),
                employeeDto.getUserId()
        );

        // Call the service method to update the employee
        Employee updated = employeeService.updateEmployee(updatedEmployee);

        if (updated != null) {
            return new ResponseEntity<>(employeeDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
