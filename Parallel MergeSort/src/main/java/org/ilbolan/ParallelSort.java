package org.ilbolan;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Sorting class that encapsulates two static sorting methods:
 *      1-> The first one uses no comparator so sorting is done is natural order
 *      2-> The second takes a comparator parameter for more flexible comparisons
 * @author Ilias Bolanakis
 * NOTE : Reflection is probably slowing the algoritm down / Find better solution !
 */
public class ParallelSort{

    private ParallelSort(){} // private constructor (no point in creating instances)

    /**
     * Sorting method that sorts in natural order
     * @param array Array to be sorted
     */
    public static void sort(Object[] array){

        if(array == null) // Check is array is null
            throw new NullPointerException("Array is null");

        if(array.length == 0) // If array = empty return (IndexOutOfBoundsException)
            return;

        Class<?> arrayClass = array[0].getClass(); //Check if arrays' elements implement Comparable
        if (! Comparable.class.isAssignableFrom(arrayClass))
            throw new IllegalArgumentException("Arrays' elements don't implement Comparable");

        // create pool and execute task
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(new MergeSortTask(array, 0, array.length-1, null));
        pool.shutdown();
    }

    /**
     * Sorting method with custom Comparator
     * @param array Array to be sorted
     * @param comp Comparator to be used
     */
    public static void sort(Object[] array ,Comparator comp){

        //create pool and execute task
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(new MergeSortTask<>(array, 0, array.length-1, comp));
        pool.shutdown();
    }

    /**
     * Private class encapsulated in ParallelSort
     * @param <T> is used as a generic to accept arrays of any object type
     * Extends RecursiveAction to use ForkJoin FW
     */
    private static class MergeSortTask<T extends Comparable<? super T>> extends RecursiveAction {

        T[] A;

        int low;

        int high;

        /**
         * {@summary  "THRESHOLD value is arbitrary and must be reexamined"}
         */
        final int THRESHOLD = 50000; // min size to introduce new thread

        Comparator<T> comp;

        public MergeSortTask(Object[] A, int low, int high, Comparator<T> comp) {
            this.A = (T[]) A;
            this.low = low;
            this.high = high;
            this.comp = comp == null? Comparator.naturalOrder() : comp;
        }

        protected void compute() {
            // If array size is small there is no need for more threads
            if(high-low <= THRESHOLD) {
                mergeSort(A,low,high);
            } else {
                int mid = low/2 + high/2;

                // split array into two equally sized halves
                MergeSortTask<T> leftSort = new MergeSortTask<>(A, low, mid, comp);
                MergeSortTask<T> rightSort = new MergeSortTask<>(A, mid+1, high, comp);
                invokeAll(leftSort,rightSort);

                // when sorted merge them back together
                if(leftSort.isDone() && rightSort.isDone())
                    merge(A,low,mid,high);
            }
        }

        /**
         * Typical Merge Sort function
         * @param A Array to be sorted
         * @param low Start point to take into consideration
         * @param high End point to take into consideration
         */
        private void mergeSort(T[] A, int low, int high){
            if(low<high){
                int middle = low/2 + high/2 ;
                mergeSort(A, low, middle);
                mergeSort(A, middle+1, high);
                merge(A, low, middle, high);
            }
        }

        /**
         * Typical Merging function
         * @param A Array to be sorted
         * @param low Start point to take into consideration
         * @param middle Middle point to take into consideration
         * @param high End point to take into consideration
         */
        private void merge(T[] A, int low, int middle, int high){

            int n1 = middle - low + 1;
            int n2 = high - middle;

            // find the type of array with reflection and create left and right sub-arrays
            T[] L = (T[]) Array.newInstance(A[0].getClass(),n1);
            T[] R = (T[]) Array.newInstance(A[0].getClass(),n2);

            int i, j;

            for(i = 0; i < n1; i++)
                L[i] = A[low+i];

            for(j = 0; j < n2; j++)
                R[j] = A[middle+j+1];

            i = 0;
            j = 0;

            int k = low;
            while (i < n1 && j < n2) {
                if (comp.compare(L[i], R[j]) < 0)
                    A[k] = L[i++];
                else
                    A[k] = R[j++];
                k++;
            }

            // Copy remaining elements of L[] if any
            while (i < n1)
                A[k++] = L[i++];

            // Copy remaining elements of R[] if any
            while (j < n2)
                A[k++] = R[j++];
        }
    }
}

