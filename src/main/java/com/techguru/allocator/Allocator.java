package com.techguru.allocator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techguru.allocator.exception.AllocatorException;
import com.techguru.allocator.pojo.AllocatedServers;
import com.techguru.allocator.pojo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.techguru.allocator.constants.AllocatorConstants.*;
import static com.techguru.allocator.util.AllocatorUtils.*;

/**
 * Allocator - Master class which allocates server resources
 */
public class Allocator {
    private static final Logger logger = LoggerFactory.getLogger(Allocator.class);

    private Map<String, Integer> serverTypes;
    private Map<String, LinkedHashMap<String, Double>> regionCostPerHourPerCpu;

    private Allocator() {
    }

    /**
     * Instantiates a new Allocator.
     *
     * @param serverTypesJsonInputStream       the server types json input stream
     * @param regionCostPerHourJsonInputStream the region cost per hour json input stream
     * @throws AllocatorException the allocator exception
     */
    public Allocator(InputStream serverTypesJsonInputStream, InputStream regionCostPerHourJsonInputStream) throws AllocatorException {
        this.load(serverTypesJsonInputStream, regionCostPerHourJsonInputStream);
    }

    /**
     * Instantiates a new Allocator.
     *
     * @param serverTypesJson       the server types json
     * @param regionCostPerHourJson the region cost per hour json
     * @throws AllocatorException the allocator exception
     */
    public Allocator(String serverTypesJson, String regionCostPerHourJson) throws AllocatorException {
        this.load(serverTypesJson, regionCostPerHourJson);
    }

    /**
     * Validation: 1. If input is a json 2. Input serverTypes json contains all server types contained in regionCostPerHour json
     * Calculation: Created a new map similar to regionCostPerHour json where srever-types in each region are sorted based on 'cost per hour per CPU'
     * Initialization: Assign the calculated regionCostPerHourPerCpu json and serverTypes json to object fields
     */
    private void load(String serverTypesJson, String regionCostPerHourJson) throws AllocatorException {
        Gson gson = new Gson();

        //Validation - Check if input jsons are valid
        validate(serverTypesJson);
        validate(regionCostPerHourJson);

        this.serverTypes = gson.fromJson(serverTypesJson, new TypeToken<Map<String, Integer>>() {
        }.getType());
        Map<String, Map<String, Double>> regionCostPerHour = gson.fromJson(regionCostPerHourJson, new TypeToken<Map<String, Map<String, Double>>>() {
        }.getType());

        //Validation - All server types in each region should be available in server types json
        for (Map.Entry<String, Map<String, Double>> e1 : regionCostPerHour.entrySet()) {
            String region = e1.getKey();
            Map<String, Double> costPerHourMap = e1.getValue();
            for (Map.Entry<String, Double> e2 : costPerHourMap.entrySet()) {
                String serverType = e2.getKey();
                if (!this.serverTypes.containsKey(serverType)) {
                    String message = "Invalid server type " + serverType + " in region " + region;
                    throw new AllocatorException(message);
                }
            }
        }

        this.regionCostPerHourPerCpu = new LinkedHashMap<>();
        regionCostPerHour.forEach((region, costPerHourMap) -> {
            LinkedHashMap<String, Double> costPerHourPerCpu = costPerHourMap.entrySet().stream().sorted(Comparator.comparingDouble(e -> e.getValue() / this.serverTypes.get(e.getKey()))
            ).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            this.regionCostPerHourPerCpu.put(region, costPerHourPerCpu);
        });
    }

    private void load(InputStream serverTypesJsonInputStream, InputStream regionCostPerHourJsonInputStream) throws AllocatorException {
        try {
            String serverTypesJson = new String(serverTypesJsonInputStream.readAllBytes());
            String regionCostPerHourJson = new String(regionCostPerHourJsonInputStream.readAllBytes());
            load(serverTypesJson, regionCostPerHourJson);
        } catch (IOException e) {
            String message = "Exception while creating allocator";
            throw new AllocatorException(message, e);
        }
    }

    /**
     * Allocates server resources based on the user input parameters
     *
     * @param hours the required # of hours of resources
     * @param cpus  the required # of cpus
     * @param price the maximum price allowed for the allocation
     * @return the costs, cpus if allocated for each region
     * @throws AllocatorException the allocator exception
     */
    public String getCosts(Integer hours, Integer cpus, Double price) throws AllocatorException {
        Gson gson = new Gson();
        List<Result> resultList = new ArrayList<>();
        if (hours == null) throw new AllocatorException("Hours cannot be null");
        if (cpus == null && price == null) throw new AllocatorException("Cpus and Price cannot be null");

        if (cpus != null && price == null) logger.info(LOG_ALLOCATE_BYCPUS, cpus, hours);
        else if (cpus == null) logger.info(LOG_ALLOCATE_BYPRICE, hours, price);
        else logger.info(LOG_ALLOCATE_BYCPUS_BYPRICE, cpus, hours, price);

        for (Map.Entry<String, LinkedHashMap<String, Double>> e1 : this.regionCostPerHourPerCpu.entrySet()) { // for each region
            String region = e1.getKey();
            LinkedHashMap<String, Double> costPerHourMap = e1.getValue();
            AllocatedServers allocatedServers;
            if (cpus != null && price == null)
                allocatedServers = allocateServersByCpus(costPerHourMap, hours, cpus);
            else if (cpus == null)
                allocatedServers = allocatedServersByPrice(costPerHourMap, hours, price);
            else
                allocatedServers = allocatedServersByCpusAndPrice(costPerHourMap, hours, cpus, price);
            Result result = Result.builder().region(region).totalCpus(allocatedServers.getTotalCpus()).totalCost(dollarValue(allocatedServers.getTotalCost())).servers(allocatedServers.getAllocatedServersList()).build();
            resultList.add(result);
        }
        return gson.toJson(resultList);
    }

    /**
     * Allocation - Allocate servers in each region by
     *      1.  Hours
     *      2.  Cpus
     */
    private AllocatedServers allocateServersByCpus(LinkedHashMap<String, Double> costPerHourMap, Integer hours, Integer targetCpus) {
        List<Map<String, Integer>> allocatedServersList = new ArrayList<>();

        Double totalCost = 0.0;
        Integer totalCpus = 0;

        for (Map.Entry<String, Double> e2 : costPerHourMap.entrySet()) { // for each server in region
            String serverType = e2.getKey();
            Double cost = e2.getValue();
            Integer n = this.serverTypes.get(serverType);
            if (targetCpus > 0 && targetCpus >= n) {
                Integer allocated = targetCpus / n;
                Map<String, Integer> allocatedServer = new HashMap<>();
                allocatedServer.put(serverType, allocated);
                allocatedServersList.add(allocatedServer);
                targetCpus = targetCpus - (n * allocated);
                totalCost = totalCost + (allocated * cost * hours);
                totalCpus = totalCpus + (allocated * n);
            }
        }
        return AllocatedServers.builder().allocatedServersList(allocatedServersList).totalCpus(totalCpus).totalCost(round(totalCost, 2)).build();
    }

    /**
     * Allocation - Allocate servers in each region by
     *      1.  Hours
     *      2.  Price
     */
    private AllocatedServers allocatedServersByPrice(LinkedHashMap<String, Double> costPerHourMap, Integer hours, Double targetPrice) {
        List<Map<String, Integer>> allocatedServersList = new ArrayList<>();

        Double targetPricePerHour = targetPrice / hours;
        Double totalCost = 0.0;
        Integer totalCpus = 0;
        for (Map.Entry<String, Double> e2 : costPerHourMap.entrySet()) { // for each server in region
            String serverType = e2.getKey();
            Double cost = e2.getValue();
            Integer n = this.serverTypes.get(serverType);
            if (targetPricePerHour > 0 && targetPricePerHour >= cost) {
                Double quotient = targetPricePerHour / cost;
                Integer allocated = quotient.intValue();
                Map<String, Integer> allocatedServer = new HashMap<>();
                allocatedServer.put(serverType, allocated);
                allocatedServersList.add(allocatedServer);
                targetPricePerHour = targetPricePerHour - (allocated * cost);
                totalCost = totalCost + (allocated * cost * hours);
                totalCpus = totalCpus + (allocated * n);
            }
        }
        return AllocatedServers.builder().allocatedServersList(allocatedServersList).totalCpus(totalCpus).totalCost(round(totalCost, 2)).build();
    }

    /**
     * Allocation - Allocate servers in each region by
     *      1.  Hours
     *      2.  Cpus
     *      3.  Price
     */
    private AllocatedServers allocatedServersByCpusAndPrice(LinkedHashMap<String, Double> costPerHourMap, Integer hours, Integer targetCpus, Double targetPrice) {
        List<Map<String, Integer>> allocatedServersList = new ArrayList<>();

        Double targetPricePerHour = targetPrice / hours;
        Double totalCost = 0.0;
        Integer totalCpus = 0;
        for (Map.Entry<String, Double> e2 : costPerHourMap.entrySet()) { // for each server in region
            String serverType = e2.getKey();
            Double cost = e2.getValue();
            Integer n = this.serverTypes.get(serverType);
            if (targetCpus > 0 && targetCpus >= n && targetPricePerHour > 0 && targetPricePerHour >= cost) {
                Double quotient = targetPricePerHour / cost;
                Integer allocated = Math.min(targetCpus / n, quotient.intValue());
                Map<String, Integer> allocatedServer = new HashMap<>();
                allocatedServer.put(serverType, allocated);
                allocatedServersList.add(allocatedServer);
                targetPricePerHour = targetPricePerHour - (allocated * cost);
                totalCost = totalCost + (allocated * cost * hours);
                totalCpus = totalCpus + (allocated * n);
            }
        }
        return AllocatedServers.builder().allocatedServersList(allocatedServersList).totalCpus(totalCpus).totalCost(round(totalCost, 2)).build();
    }

}
