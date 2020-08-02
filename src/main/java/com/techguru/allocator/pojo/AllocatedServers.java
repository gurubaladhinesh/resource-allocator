package com.techguru.allocator.pojo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * AllocatedServers - POJO class for list of servers allocated in a region, corresponding total CPUs and their total price
 */
@Getter
@Setter
@Builder
public class AllocatedServers {

    private List<Map<String, Integer>> allocatedServersList;

    private Integer totalCpus;

    private Double totalCost;

}
