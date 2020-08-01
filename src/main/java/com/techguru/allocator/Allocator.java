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
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Allocator {
    private static final Logger logger = LoggerFactory.getLogger(Allocator.class);

    private InputStream serverTypesJsonInputStream;
    private InputStream regionCostPerHourJsonInputStream;
    private Map<String, Integer> serverTypes;
    private Map<String, LinkedHashMap<String, Double>> regionCostPerHourPerCpu;

    private Allocator() {
    }

    public Allocator(InputStream serverTypesJsonInputStream, InputStream regionCostPerHourJsonInputStream) throws AllocatorException {
        this.serverTypesJsonInputStream = serverTypesJsonInputStream;
        this.regionCostPerHourJsonInputStream = regionCostPerHourJsonInputStream;
        this.load();
    }

    public String getCosts(Integer hours, Integer cpus, Double price) throws AllocatorException {
        logger.info("Allocating {} cpus for {} hours at price ${}", cpus, hours, price);
        Gson gson = new Gson();
        List<Result> resultList = new ArrayList<>();
        if (hours == null) throw new AllocatorException("Hours cannot be null");
        if (cpus == null && price == null) throw new AllocatorException("Cpus and Price cannot be null");

        for (Map.Entry<String, LinkedHashMap<String, Double>> e1 : this.regionCostPerHourPerCpu.entrySet()) { // for each region
            String region = e1.getKey();
            LinkedHashMap<String, Double> costPerHourMap = e1.getValue();
            Result result = new Result();
            result.setRegion(region);
            AllocatedServers allocatedServers;
            if (cpus != null && price == null)
                allocatedServers = allocateServersByCpus(costPerHourMap, hours, cpus);
            else if (price != null && cpus == null)
                allocatedServers = allocatedServersByPrice(costPerHourMap, hours, price);
            else
                allocatedServers = allocatedServersByCpusAndPrice(costPerHourMap, hours, cpus, price);
            result.setTotalCpus(allocatedServers.getTotalCpus());
            result.setTotalCost(dollarValue(allocatedServers.getTotalCost()));
            result.setServers(allocatedServers.getAllocatedServersList());
            resultList.add(result);
        }
        return gson.toJson(resultList);
    }

    private void load() throws AllocatorException {
        try {
            Gson gson = new Gson();
            String serverTypesJson = new String(serverTypesJsonInputStream.readAllBytes());
            String regionCostPerHourJson = new String(regionCostPerHourJsonInputStream.readAllBytes());
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
        } catch (IOException e) {
            String message = "Exception while creating allocator";
            logger.error(message, e);
            throw new AllocatorException(message, e);
        }
    }

    private double round(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }

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

    private String dollarValue(Double value) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }
}
