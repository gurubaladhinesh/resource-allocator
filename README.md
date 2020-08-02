# Resource Allocator

Resource Allocator is a Java library for allocating CPU resources optimally in a cloud environment based of user needs.

## Pre-requisites
1. [Java 11](https://openjdk.java.net/install/)

## Usage

```java
import com.techguru.allocator.Allocator;
class Main{
    public static void main(String[] args){
        String serverTypesJson = ""; //json of available server types
        String regionCostPerHourJson = ""; //json of cost of server-type in each region
        Allocator allocator = new Allocator(serverTypesJson, regionCostPerHourJson);
        allocator.getCosts(24, 115, null);
        allocator.getCosts(8, null, 29.0);
        allocator.getCosts(7, 214, 95.0);
    }
}
```
## I/O
Input:

    1.  String serverTypesJson - Json of all available server types
    2.  String regionCostPerHourJson - Json of cost per hour of server-type in each region

Output:
    
    Json string containing array of each resources allocated in each region along with total_cost and total_cpus
    [{"region":"us-east","totalCpus":115,"totalCost":"$243.60","servers":[{"8xlarge":7},{"xlarge":1},{"large":1}]},{"region":"us-west","totalCpus":115,"totalCost":"$228.48","servers":[{"8xlarge":7},{"large":3}]},{"region":"asia","totalCpus":115,"totalCost":"$205.68","servers":[{"8xlarge":7},{"xlarge":1},{"large":1}]}]


## Solution
1.  Calculates the 'cost per hour per CPU' for each of the server-type in each region using both inputs.
2.  Sort the input regionCostPerHourJson json for each region based on the calculated value.
3.  Allocated resources in each region based on the sorted json
