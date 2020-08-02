package com.techguru.allocator;

import com.techguru.allocator.exception.AllocatorException;
import com.techguru.allocator.util.AllocatorUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class AllocatorTest {

    private static final String SERVER_TYPES_VALIDJSON = "server.types.json";
    private static final String SERVER_TYPES_VALIDJSON_INVALIDVALUES = "server.types.validjson.invalidvalues.json";
    private static final String REGION_COST_PERHOUR_INVALIDJSON = "region.cost.per.hour.invalidjson.txt";
    private static final String REGION_COST_PERHOUR_VALIDJSON = "region.cost.per.hour.json";

    @Test
    void getCosts_ThrowsException_IfInputIsInvalidJson() {
        InputStream serverTypesInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(SERVER_TYPES_VALIDJSON);
        InputStream regionCostPerHourInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(REGION_COST_PERHOUR_INVALIDJSON);
        assertThrows(AllocatorException.class, () -> new Allocator(serverTypesInputStream, regionCostPerHourInputStream));
    }

    @Test
    void getCosts_ThrowsException_IfInputIsValidJsonAndInvalidValuesInJson() {
        InputStream serverTypesInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(SERVER_TYPES_VALIDJSON_INVALIDVALUES);
        InputStream regionCostPerHourInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(REGION_COST_PERHOUR_VALIDJSON);
        assertThrows(AllocatorException.class, () -> new Allocator(serverTypesInputStream, regionCostPerHourInputStream));
    }

    @Test
    void getCosts_ValidJson_IfGetCostsByHoursAndCpus() {
        InputStream serverTypesInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(SERVER_TYPES_VALIDJSON);
        InputStream regionCostPerHourInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(REGION_COST_PERHOUR_VALIDJSON);
        try {
            Allocator allocator = new Allocator(serverTypesInputStream, regionCostPerHourInputStream);
            String resultJson = allocator.getCosts(24, 115, null);
            AllocatorUtils.validate(resultJson);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getCosts_ValidJson_IfGetCostsByHoursAndPrice() {
        InputStream serverTypesInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(SERVER_TYPES_VALIDJSON);
        InputStream regionCostPerHourInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(REGION_COST_PERHOUR_VALIDJSON);
        try {
            Allocator allocator = new Allocator(serverTypesInputStream, regionCostPerHourInputStream);
            String resultJson = allocator.getCosts(8, null, 29.0);
            AllocatorUtils.validate(resultJson);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getCosts_ValidJson_IfGetCostsByHoursAndCpusAndPrice() {
        InputStream serverTypesInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(SERVER_TYPES_VALIDJSON);
        InputStream regionCostPerHourInputStream = AllocatorTest.class.getClassLoader().getResourceAsStream(REGION_COST_PERHOUR_VALIDJSON);
        try {
            Allocator allocator = new Allocator(serverTypesInputStream, regionCostPerHourInputStream);
            String resultJson = allocator.getCosts(7, 214, 95.0);
            AllocatorUtils.validate(resultJson);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}