# Nuber - Ride-Sharing Simulator

A multi-threaded Java simulation of a ride-sharing service (Uber clone) that demonstrates concurrent programming concepts, thread synchronization, and distributed job processing across multiple regions.

## Overview

Nuber is a command-line based simulator that models a ride-sharing platform where passengers request rides and drivers fulfill those requests across multiple geographic regions. The system handles concurrent bookings, driver allocation, and region-specific capacity constraints using Java's concurrency utilities.

## Features

- **Multi-Region Support** - Multiple independent regions operating concurrently
- **Thread-Safe Operations** - Concurrent booking processing with proper synchronization
- **Dynamic Driver Allocation** - Shared driver pool across all regions
- **Capacity Management** - Configurable maximum simultaneous jobs per region
- **Asynchronous Processing** - Non-blocking booking system using Future/Callable pattern
- **Real-Time Monitoring** - Live tracking of active and pending bookings
- **Graceful Shutdown** - Completes existing bookings before terminating

## System Architecture

### Core Components

#### 1. NuberDispatch
The central controller that manages the entire system:
- Maintains a thread-safe queue of available drivers
- Creates and manages multiple NuberRegion instances
- Coordinates driver allocation across regions
- Tracks pending bookings awaiting drivers
- Handles system-wide shutdown

**Key Design Decisions:**
- Uses `BlockingQueue<Driver>` for thread-safe driver management
- Uses `AtomicInteger` for lock-free pending booking counter
- Implements `ExecutorService` for region thread pool management

#### 2. NuberRegion
Independent regional processing units that handle bookings:
- Processes bookings up to a maximum simultaneous job limit
- Queues excess bookings until capacity is available
- Operates as a `Runnable` in its own thread
- Manages its own thread pool for booking execution

**Key Design Decisions:**
- Each region has its own `ExecutorService` with fixed thread pool
- Uses `BlockingQueue<Passenger>` for pending passenger queue
- Uses `AtomicInteger` to track active bookings without locks

#### 3. Booking
Represents a single ride request (implements `Callable<BookingResult>`):
- Generates unique, sequential booking IDs (thread-safe)
- Requests driver from dispatch
- Coordinates pickup and dropoff
- Tracks total trip duration
- Returns driver to available pool after completion

**Lifecycle:**
1. Created with passenger and dispatch reference
2. Waits for available driver from dispatch
3. Driver picks up passenger (simulated delay)
4. Driver drives to destination (simulated delay)
5. Returns `BookingResult` with trip information

#### 4. Driver & Passenger
- **Driver**: Picks up passengers and drives them to destinations
- **Passenger**: Requests rides with random travel times
- Both extend `Person` abstract class with name and maxSleep properties

#### 5. BookingResult
Immutable result object containing:
- Booking ID
- Passenger reference
- Driver reference
- Total trip duration (milliseconds)

## Concurrency Features

### Thread Safety Mechanisms

1. **BlockingQueue** - Thread-safe driver queue and passenger queues
   - Automatic blocking when queue is empty/full
   - No explicit synchronization needed

2. **AtomicInteger** - Lock-free counters
   - Booking ID generation
   - Active booking tracking
   - Pending booking counting

3. **ExecutorService** - Managed thread pools
   - Region-level thread pools for bookings
   - Dispatch-level thread pool for regions
   - Graceful shutdown support

4. **Future/Callable Pattern** - Asynchronous results
   - Non-blocking booking submission
   - Retrievable results when complete
   - Exception handling support

### Synchronization Strategy

The system avoids explicit locks (`synchronized`) by using Java's concurrent collections and atomic operations:
- Driver allocation: `BlockingQueue.take()` blocks until driver available
- Booking counters: `AtomicInteger` provides atomic increment/decrement
- Region capacity: Atomic counter prevents over-allocation

## Building and Running

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Java IDE (Eclipse, IntelliJ IDEA, or NetBeans) or command-line tools

## Example Output

\`\`\`
Creating booking: 1:null:P-Bryan
Creating booking: 2:null:P-Olivia
Creating booking: 3:null:P-Vincent

1:null:P-Bryan: Waitng for available driver
1:D-Kenneth:P-Bryan: Starting, on way to passenger
1:D-Kenneth:P-Bryan: Collected passenger, on way to destination

Active bookings: 8, pending: 5
Active bookings: 6, pending: 3
Active bookings: 4, pending: 1

1:D-Kenneth:P-Bryan: Drop off, driver is free now
2:D-Debra:P-Olivia: Starting, on way to passenger

Active bookings: 0, pending: 0
Simulation complete in 4523ms
\`\`\`

## Performance Considerations

### Scalability
- **Horizontal**: Add more regions for geographic distribution
- **Vertical**: Increase `maxSimultaneousJobs` per region
- **Driver Pool**: Scale driver count independently

### Optimizations
- Lock-free atomic operations reduce contention
- `BlockingQueue` provides efficient wait/notify
- Thread pools prevent thread creation overhead

## Testing

### Unit Testing Approach
1. Test individual driver pickup/dropoff
2. Test booking lifecycle
3. Test dispatch driver allocation
4. Test region capacity limits
5. Test shutdown behavior

### Integration Testing
Run various simulation scenarios:
- Single driver, multiple passengers (stress test)
- Multiple drivers, single passenger (resource abundance)
- Balanced load (drivers ≈ passengers)
- Multiple regions with different capacities

## Troubleshooting

### Common Issues

**Deadlock**: If simulation hangs
- Check driver queue is being replenished after trips
- Verify regions are processing pending queue
- Ensure shutdown is called properly

**Memory Issues**: If OutOfMemoryError occurs
- Reduce passenger count
- Reduce maxSleep to speed up simulation
- Check for driver/booking leaks

**Incorrect Booking Counts**: If pending count is wrong
- Verify `AtomicInteger` increment/decrement pairs
- Check driver is returned to queue after trip
- Ensure booking counter decrements when driver allocated

## Future Enhancements

- [ ] Geographic distance calculations
- [ ] Real-time visualization dashboard
- [ ] Persistent storage for booking history
- [ ] RESTful API for external integration

---

**Note**: This simulator uses random delays to simulate real-world driving times. Actual execution time varies based on `maxSleep` parameter and number of bookings.
