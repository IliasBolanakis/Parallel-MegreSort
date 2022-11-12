package org.ilbolan;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Comparator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * NOTE: MORE TESTS TO BE DONE WITH COMPARATORS
 * @author Ilias Bolanakis
 */
public class SortingTests {

    private static Integer[] data;

    /**
     * Instantiate Data
     */
    @BeforeAll
    static void runBeforeAll(){
        data = new Integer[1000000];
    }

    /**
     * Randomly assign values data before each test
     */
    @BeforeEach
    void runBeforeEach(){
        Random r = new Random();
        for(int i = 0; i < data.length; i++){
            data[i] = r.nextInt();
        }
        // make sure data is not sorted from prev tests
        System.out.println("Data sorted: " + isSorted(data,Comparator.naturalOrder()));
    }

    /**
     * Tests without comparator
     */
    @DisplayName("Natural Order Sort")
    @RepeatedTest(value=10, name="{displayName}: repetition {currentRepetition}/{totalRepetitions}")
    @Execution(ExecutionMode.CONCURRENT)
    void test_naturalOrder(){
        ParallelSort.sort(data);
        assertTrue(isSorted(data, Comparator.naturalOrder()));
    }

    /**
     * Tests with reverse order comparator
     */
    @DisplayName("Reverse Order Sort")
    @RepeatedTest(value=10, name="{displayName}: repetition {currentRepetition}/{totalRepetitions}")
    @Execution(ExecutionMode.CONCURRENT)
    void test_reverseOrder(){
        ParallelSort.sort(data,Comparator.reverseOrder());
        assertTrue(isSorted(data, Comparator.reverseOrder()));
    }

    boolean isSorted(Comparable[] array, Comparator comp) {
        for (int i = 0; i < array.length - 1; ++i) {
            if (comp.compare(array[i], array[i+1]) > 0)
                return false;
        }
        return true;
    }
}
