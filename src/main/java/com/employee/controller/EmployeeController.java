package com.employee.controller;

import com.employee.dto.EmployeeRequestDTO;
import com.employee.dto.EmployeeResponseDTO;
import com.employee.dto.FileDto;
import com.employee.service.EmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> save(@Valid @RequestBody EmployeeRequestDTO employeeRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(employeeRequestDTO));
    }

    // GET ALL Employees
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAll(){
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.getAll());
    }

    // GET By ID
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.getById(id));
    }

    // UPDATE By ID
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable Long id,@Valid @RequestBody EmployeeRequestDTO employeeRequestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.update(id,employeeRequestDTO));
    }

    // DELETE By ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Specific Fields Update By ID
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> patchUpdate(@PathVariable Long id, @RequestBody Map<String,Object> updates){
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.patchUpdate(id,updates));
    }

    // Upload-File By ID If it is Exists
    @PostMapping("/upload-file/{id}")
    public ResponseEntity<FileDto> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file){
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.uploadFile(id,file));
    }

    // // Upload-File By ID If it is Exists with Specific File-Size
    @PostMapping("/upload-file/file-size/{id}")
    public ResponseEntity<?> uploadFileWithFileSize(@PathVariable Long id, @RequestParam("file") MultipartFile file){
        long maxFileSize = 1*1024*1024;
        if(file.getSize()>maxFileSize){
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File Size is exceed than 1MB... Please choose smaller size file");
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.uploadFile(id,file));
    }

    // // Upload-File By ID If it is Exists with Specific File-Size And File-Extension
    @PostMapping("/upload-file/file-size/file-extension/{id}")
    public ResponseEntity<?> uploadFileWithFileSizeAndFileExtension(@PathVariable Long id, @RequestParam("file") MultipartFile file){
        long maxFileSize = 1*1024*1024;
        if(file.getSize()>maxFileSize){
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File Size is exceed than 1MB... Please choose smaller size file");
        }
        String originalFilename = file.getOriginalFilename();
        if(originalFilename==null || !isValidExtension(originalFilename)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File Extension is Invalid... Allowed only .JPG |.PDF |.JPEG File Extensions...");
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.uploadFile(id,file));
    }

    private boolean isValidExtension(String fileName) {
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".pdf") |
                lowerCase.endsWith(".jpg") |
                lowerCase.endsWith(".jpeg");
    }

    // Upload-Employee JSON OR File
    @PostMapping(value = "/upload-json-with-file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeRequestDTO> uploadJsonWithFile(@RequestPart("employees") String employeeJson, @RequestPart(value ="file",required = false) MultipartFile file){
        ObjectMapper objectMapper=new ObjectMapper().registerModule(new JavaTimeModule());
        EmployeeRequestDTO employeeRequestDTO;
        try {
             employeeRequestDTO = objectMapper.readValue(employeeJson, EmployeeRequestDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.uploadJsonWithFile(employeeRequestDTO,file));
    }

    // Download-File By ID If it is Exists
    @GetMapping("/download-file/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id){
        FileDto fileDto=employeeService.downloadFile(id);
        return ResponseEntity
                .ok().
                contentType(MediaType.parseMediaType(fileDto.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; fileName=\""+fileDto.getFileName()+"\"")
                .body(fileDto.getFileData());
    }

    // Download-File By ID If it is Exists and Download into the Specific Directory Location
    @GetMapping("/download-file/file-location/{id}")
    public ResponseEntity<?> downloadFileSpecificLocation(@PathVariable Long id, @RequestParam("fileLocation") String fileLocation){
        FileDto fileDto=employeeService.downloadFile(id);
        try {
            Path directoryPath = Paths.get(fileLocation);
            if(!Files.exists(directoryPath))
                Files.createDirectories(directoryPath);
            String originalFileName = fileDto.getFileName();
            String fileName=originalFileName;
            String baseName=originalFileName;
            String extension="";

            int dotIndex = originalFileName.lastIndexOf(".");
            if(dotIndex!=-1){
                 baseName = originalFileName.substring(0, dotIndex);
                extension = originalFileName.substring(dotIndex);
            }

            Path filePath = directoryPath.resolve(fileName);
            int counter=1;
            while(Files.exists(filePath)){
                fileName=baseName+"("+counter+")"+extension;
                filePath = directoryPath.resolve(fileName);
                counter++;
            }
            Files.write(filePath,fileDto.getFileData());
            return ResponseEntity.status(HttpStatus.OK).body("Successfully Save File:- "+filePath.toString());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to Download File"+e.getMessage());
        }
    }

}
