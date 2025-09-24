package com.example.master.service;

import com.example.master.config.TokenHelper;
import com.example.master.dtobj.DistrictBreakdownDto;
import com.example.master.dtobj.Role;
import com.example.master.entity.AwcCenterr;
import com.example.master.entity.District;
import com.example.master.entity.UserMetadata;
import com.example.master.model.Demand;
import com.example.master.model.DemandCdpoDetail;
import com.example.master.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private AwcCenterRepository awcRepo;
    @Autowired
    private SectorRepository sectorRepo;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private DemandRepository demandRepo;
    @Autowired
    private DistrictRepository districtRepo;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    public Map<String, Long> getTotalCounts() {

        Map<String, Long> counts = new HashMap<>();

        String role = TokenHelper.getRole().get().name();
        if(role.equals(Role.AWC.name())) {
            //KcUser user = keycloakAdminService.getUserDetails(username, LookupType.USERNAME);
            //Optional<AwcCenter> awc = awcRepo.findByCenterId(user.getId());// or use username
            String username = TokenHelper.getUsername();
            UserMetadata metadata = userMetadataRepository.findById(username)
                    .orElseThrow(() -> new RuntimeException("No AWC center assigned to this user."));
            Long awcId = Long.valueOf(metadata.getAwc());
            AwcCenterr center = awcRepo.findById(awcId).orElseThrow(() -> new RuntimeException("No AWC center found"));
            List<Demand> demands = demandRepo.findAll();//findByAwcCenterId(awcId);
            Long totalDemand = Long.valueOf(demands.size());
            Long totalRequestedPackets = 0l;
            Long totalDistributePackets = 0l;
            Long totalAvailablePackets = 0l;
            for (Demand demand : demands) {
                if (!demand.getCdpoDetails().isEmpty()) {
                    for (DemandCdpoDetail dispatch : demand.getCdpoDetails()) {
                        totalRequestedPackets += dispatch.getQuantity();
                        //totalDistributePackets += dispatch.getPacketsToDispatch();//need to remove but first check if anything causes problems
                        //totalDistributePackets += dispatch.getDistributions().stream().mapToInt(PacketDistribution::getDistributedPacketCount).sum();
                        //totalAvailablePackets += dispatch.getRemainingPacketCount().longValue();
                    }
                }
            }
            counts.put("total_demand", totalDemand);
            counts.put("total_requested_packets", totalRequestedPackets);
            counts.put("total_distribute_packets", totalDistributePackets);
            counts.put("total_available_packets", totalAvailablePackets);

        } else {
            counts.put("awcs", awcRepo.count());
            counts.put("districts", districtRepo.count());
            counts.put("sectors", sectorRepo.count());
            counts.put("projects", projectRepo.count());
            counts.put("demands", demandRepo.count());
            Long totalRequestedPackets = 0l;
            Long totalDistributePackets = 0l;
            Long totalAvailablePackets = 0l;
            for (Demand demand : demandRepo.findAll()) {
                if (!demand.getCdpoDetails().isEmpty()) {
                    //Batch batch = demand.getBatches().get(0);
                    for (DemandCdpoDetail detail : demand.getCdpoDetails()) {
                        totalRequestedPackets += detail.getQuantity();
                        //totalDistributePackets += dispatch.getPacketsToDispatch();//need to remove but first check if anything causes problems
                        //totalDistributePackets += dispatch.getDistributions().stream().mapToInt(PacketDistribution::getDistributedPacketCount).sum();
                        //totalAvailablePackets += dispatch.getRemainingPacketCount().longValue();
                    }
                }
            }
            counts.put("total_requested_packets", totalRequestedPackets);
            counts.put("total_distribute_packets", totalDistributePackets);
            counts.put("total_available_packets", totalAvailablePackets);
        }
        return counts;
    }

    public List<DistrictBreakdownDto> getDistrictBreakdown() {
        List<District> districts = districtRepo.findAll();
        List<DistrictBreakdownDto> breakdown = new ArrayList<>();

        for (District district : districts) {
            Long projectCount = projectRepo.countByDistrictId(district.getId());
            Long sectorCount = sectorRepo.countByDistrictId(district.getId());

            breakdown.add(new DistrictBreakdownDto(
                    district.getId(),
                    district.getDistrictName(),
                    projectCount,
                    sectorCount
            ));
        }

        return breakdown;
    }
}

