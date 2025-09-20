package com.example.master.service;

import com.example.master.config.TokenHelper;
import com.example.master.dtobj.Role;
import com.example.master.entity.AwcCenter;
import com.example.master.entity.UserMetadata;
import com.example.master.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    /*@Autowired
    private AwcCenterRepository awcRepo;
    @Autowired
    private SectorRepository sectorRepo;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private DemandRequestRepository demandRepo;
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
            AwcCenter center = awcRepo.findById(awcId).orElseThrow(() -> new RuntimeException("No AWC center found"));
            List<DemandRequest> demands = demandRepo.findByAwcCenterId(awcId);
            Long totalDemand = Long.valueOf(demands.size());
            Long totalRequestedPackets = 0l;
            Long totalDistributePackets = 0l;
            Long totalAvailablePackets = 0l;
            for (DemandRequest demand : demands) {
                if (!demand.getBatches().isEmpty()) {
                    Batch batch = demand.getBatches().get(0);
                    List<Dispatch> dispatches = batch.getDispatches().stream()
                            .filter(dispatch -> dispatch.getAwcCenterName() != null &&
                                    dispatch.getAwcCenterName().equals(center.getCenterName()))
                            .toList();
                    for (Dispatch dispatch : dispatches) {
                        totalRequestedPackets += dispatch.getRequestedPackets();
                        totalDistributePackets += dispatch.getPacketsToDispatch();
                        totalAvailablePackets += dispatch.getRemainingPacketCount().longValue();
                    }
                }
            }
            counts.put("total_demand", totalDemand);
            counts.put("total_requested_packets", totalRequestedPackets);
            counts.put("total_distribute_packets", totalDistributePackets);
            counts.put("total_available_packets", totalAvailablePackets);

        } else {
            counts.put("awcs", awcRepo.count());
            counts.put("sectors", sectorRepo.count());
            counts.put("projects", projectRepo.count());
            counts.put("demands", demandRepo.count());
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
    }*/
}

