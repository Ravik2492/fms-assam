package com.example.master.service;

import com.example.master.dtobj.AwcCsvRow;
import com.example.master.dtobj.DemandDetailDto;
import com.example.master.dtobj.DemandRequestDto;
import com.example.master.entity.AwcCenter;
import com.example.master.repository.AwcCenterRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class ExcelService {


    @Autowired
    private AwcCenterRepository awcRepo;

    public Map<String, Object> processCsv(MultipartFile file) {

        Map<String, Object> response = new HashMap<>();
        List<AwcCsvRow> rows = new ArrayList<>();
        Map<Long, String> projectMap = new LinkedHashMap<>();
        Map<Long, String> districtMap = new LinkedHashMap<>();
        Map<Long, String> sectorMap = new LinkedHashMap<>();


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean skipHeader = true;

            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] parts = line.split("\t"); // tab-separated
                AwcCsvRow row = new AwcCsvRow();
                row.setCenterId(parts.length > 0 ? parts[0].trim() : null);
                AwcCenter center = awcRepo.findByCenterId(row.getCenterId()).orElseThrow(() -> new RuntimeException("No AWC center found with Center ID: "+row.getCenterId()));;
                row.setId(center.getId());
                row.setCenterName(center.getCenterName());
                // Populate distinct maps
                projectMap.putIfAbsent(center.getProject().getId(), center.getProject().getProjectName());
                districtMap.putIfAbsent(center.getDistrict().getId(), center.getDistrict().getDistrictName());
                sectorMap.putIfAbsent(center.getSector().getId(), center.getSector().getSectorName());
                //row.setCenterName(parts.length > 1 ? parts[1].trim() : null);
                row.setNutriBarCount(parts.length > 2 && !parts[2].isBlank() ? Integer.valueOf(parts[2].trim()) : 0);
                row.setRegisteredChildren(parts.length > 3 && !parts[3].isBlank() ? Integer.valueOf(parts[3].trim()) : 0);

                rows.add(row);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }
        response.put("awc_rows", rows);
        response.put("projects", projectMap);
        response.put("districts", districtMap);
        response.put("sectors", sectorMap);

        return response;
    }

    public static DemandRequestDto parseSingleDemand(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1); // Assuming row 0 is header

            DemandRequestDto dto = new DemandRequestDto();
            dto.setFromDate(row.getCell(0).getLocalDateTimeCellValue().toLocalDate());
            dto.setToDate(row.getCell(1).getLocalDateTimeCellValue().toLocalDate());
            dto.setSupplierId(row.getCell(2).getStringCellValue());
            dto.setCreatedBy(row.getCell(3).getStringCellValue());

            // Parse district/project/sector IDs as comma-separated strings
            dto.setDistrictIds(parseIdList(row.getCell(4).getStringCellValue()));
            dto.setProjectIds(parseIdList(row.getCell(5).getStringCellValue()));
            dto.setSectorIds(parseIdList(row.getCell(6).getStringCellValue()));

            // Parse AWC details (assuming one AWC per row for now)
            DemandDetailDto detail = new DemandDetailDto();
            detail.setAwcId(Long.parseLong(row.getCell(7).getStringCellValue()));
            detail.setNutritionBarCount((int) row.getCell(8).getNumericCellValue());
            detail.setNutritionBarPackageType(row.getCell(9).getStringCellValue());
            detail.setRegisteredChildren((int) row.getCell(10).getNumericCellValue());

            dto.setAwcDetails(List.of(detail));
            return dto;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }
    }

    private static List<Long> parseIdList(String cellValue) {
        return Arrays.stream(cellValue.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();
    }

}
