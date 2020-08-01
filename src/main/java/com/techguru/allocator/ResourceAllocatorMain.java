package com.techguru.allocator;

import com.techguru.allocator.exception.AllocatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ResourceAllocatorMain {

    private static Logger logger = LoggerFactory.getLogger(ResourceAllocatorMain.class);

    public static void main(String[] args) throws AllocatorException {
        try (InputStream serverTypesInputStream = Objects.requireNonNull(Allocator.class.getClassLoader().getResource("server.types.json")).openStream();
             InputStream regionCostPerHourInputStream = Objects.requireNonNull(Allocator.class.getClassLoader().getResource("region.cost.per.hour.json")).openStream()
        ) {
            Allocator allocator = new Allocator(serverTypesInputStream, regionCostPerHourInputStream);
            logger.info(allocator.getCosts(24, 115, null));
            logger.info(allocator.getCosts(8, null, 29.0));
            logger.info(allocator.getCosts(7, 214, 95.0));
        } catch (IOException e) {
            String message = "Exception in processing one of the inputs";
            logger.error(message, e);
            throw new AllocatorException(message, e);
        }
    }
}
