package com.czen.payroll_automation.controller;

import com.czen.payroll_automation.model.Department;
import com.czen.payroll_automation.model.Employee;
import com.czen.payroll_automation.model.JobCode;
import com.czen.payroll_automation.model.TimeEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testValidateTimeEntry_Valid() throws Exception {
        Employee employee = new Employee();
        employee.setId(1L);
        Department department = new Department();
        department.setId(1L);
        JobCode jobCode = new JobCode();
        jobCode.setId(1L);
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setEmployee(employee);
        timeEntry.setDepartment(department);
        timeEntry.setJobCode(jobCode);

        mockMvc.perform(post("/api/payroll/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeEntry)))
                .andExpect(status().isOk())
                .andExpect(content().string("Time entry is valid."));
    }

    @Test
    void testValidateTimeEntry_Invalid() throws Exception {
        Employee employee = new Employee();
        employee.setId(99L); // Non-existent employee
        Department department = new Department();
        department.setId(1L);
        JobCode jobCode = new JobCode();
        jobCode.setId(1L);
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setEmployee(employee);
        timeEntry.setDepartment(department);
        timeEntry.setJobCode(jobCode);

        mockMvc.perform(post("/api/payroll/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeEntry)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Time entry is invalid."));
    }
}