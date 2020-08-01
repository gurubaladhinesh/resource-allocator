package com.techguru.allocator.pojo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class AllocatedServers {

    private List<Map<String, Integer>> allocatedServersList;

    private Integer totalCpus;

    private Double totalCost;

}
