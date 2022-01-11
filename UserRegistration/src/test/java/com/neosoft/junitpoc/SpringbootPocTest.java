package com.neosoft.junitpoc;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neosoft.controller.UserController;
import com.neosoft.model.User;
import com.neosoft.repository.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@WebMvcTest(value=UserController.class)
public class SpringbootPocTest {
    
	@Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;  
    
    @MockBean
    UserRepository userRepository;
    
   User RECORD_1 = new User(1,"Kartikey", "bindal","pune", "403839",new Date(1995-8-15),new Date(06-19-2000));
   User RECORD_2 = new User(2,"Abhi", "agnihotri","delhi","404738",new Date(2011-06-26),new Date(2024-06-30));
   User RECORD_3 = new User(3,"Anand", "shinde","kanpur","4048399", new Date(1996-03-30),new Date(2009-03-15));
    
    @Test
	void contextLoads() {
	}
    
    @Test
    public void getAllRecords_success() throws Exception {
        List<User> records = new ArrayList<>
        		(Arrays.asList(RECORD_1, RECORD_2, RECORD_3));
        
        Mockito.when(userRepository.findAll()).thenReturn(records);
        //When findAll called then ready with records  (No DB calls) 
        mockMvc.perform(MockMvcRequestBuilders
                .get("/user")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) //200
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].firstName", is("Kartikey")));
    }
    
    @Test
    public void getUserById_success() throws Exception {
        Mockito.when(userRepository.findById(RECORD_1.getUserId()))
        				.thenReturn(java.util.Optional.of(RECORD_1));

        mockMvc.perform(MockMvcRequestBuilders
                .get("/user/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.surname", is("bindal")));
    }
    
    @Test
    public void createRecord_success() throws Exception {
        User record = User.builder()
                .firstName("Abhi")
                .surname("Bindal")
                .address("pune")
                .pinCode(403839)
                .dob(new Date(1998-9-12))
                .doj(new Date(2009-12-10))
                .build();

        Mockito.when(userRepository.save(record)).thenReturn(record);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(record));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("Abhi")));
        }
    
    @Test
    public void updateUser_success() throws Exception {
        User updatedRecord = User.builder()
        	 .firstName("Abhi")
                .surname("agnihotri")
                .address("usa")
                .pinCode(404839)
                .dob(new Date(1939-10-24))
                .doj(new Date(2000-6-17))
                .build();

        Mockito.when(userRepository.findById(RECORD_1.getUserId()))
        .thenReturn(Optional.of(RECORD_1));
        
        Mockito.when(userRepository.save(updatedRecord)).thenReturn(updatedRecord);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.surname", is("agnihotri")));
    }
    
    @Test
    public void updateUser_nullId() throws Exception {
        User updatedRecord = User.builder()
        	 .firstName("Anand")
                .surname("khanna")
                .address("delhi")
                .pinCode(400242)
                .dob(new Date(1999-8-26))
                .doj(new Date(2000-10-15))
                .build();

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
        		.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                    assertTrue(result.getResolvedException() 
                    		instanceof InvalidRequestException))
                .andExpect(result ->
                	assertEquals("User or ID must not be null!", 
            		result.getResolvedException().getMessage()));
        }
    
    @Test
    public void updateUser_recordNotFound() throws Exception {
        User updatedRecord = User.builder()
        		.firstName("Anand")
                .surname("kapoor")
                .address("uk")
                .pinCode(146318)
                .dob(new Date(1997-10-24))
                .doj(new Date(2000-12-26))
                .build();        

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                    assertTrue(result.getResolvedException() 
                    		instanceof InvalidRequestException))
        .andExpect(result ->
            assertEquals("User with ID 5 does not exist.", 
            		result.getResolvedException().getMessage()));
    }
    
    @Test
    public void deleteUserById_success() throws Exception {
        
    	Mockito.when(userRepository.findById(RECORD_2.getUserId()))
        .thenReturn(Optional.of(RECORD_2));
    	
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/user/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());              
    }

    @Test
    public void deleteUserById_notFound() throws Exception {       

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/user/5")
                .contentType(MediaType.APPLICATION_JSON))
        		.andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() 
                        		instanceof InvalidRequestException))
                .andExpect(result ->
                assertEquals("User with ID 5 does not exist.", 
                		result.getResolvedException().getMessage()));
    }
}

    