package com.techguru.allocator.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Result - POJO class for output of allocator.
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"region", "total_cpus", "total_cost", "servers"})
@Builder
public class Result {
    @JsonProperty("region")
    private String region;
    @JsonProperty("total_cpus")
    private Integer totalCpus;
    @JsonProperty("total_cost")
    private String totalCost;
    @JsonProperty("servers")
    private List<Map<String, Integer>> servers;

    /**
     * Gets region.
     *
     * @return the region
     */
    @JsonProperty("region")
    public String getRegion() {
        return this.region;
    }

    /**
     * Sets region.
     *
     * @param region the region
     */
    @JsonProperty("region")
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets total cpus.
     *
     * @return the total cpus
     */
    @JsonProperty("total_cpus")
    public Integer getTotalCpus() {
        return this.totalCpus;
    }

    /**
     * Sets total cpus.
     *
     * @param totalCpus the total cpus
     */
    @JsonProperty("total_cpus")
    public void setTotalCpus(Integer totalCpus) {
        this.totalCpus = totalCpus;
    }

    /**
     * Gets total cost.
     *
     * @return the total cost
     */
    @JsonProperty("total_cost")
    public String getTotalCost() {
        return this.totalCost;
    }

    /**
     * Sets total cost.
     *
     * @param totalCost the total cost
     */
    @JsonProperty("total_cost")
    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Gets servers.
     *
     * @return the servers
     */
    @JsonProperty("servers")
    public List<Map<String, Integer>> getServers() {
        return this.servers;
    }

    /**
     * Sets servers.
     *
     * @param servers the servers
     */
    @JsonProperty("servers")
    public void setServers(List<Map<String, Integer>> servers) {
        this.servers = servers;
    }

}
