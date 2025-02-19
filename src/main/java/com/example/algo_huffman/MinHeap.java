package com.example.algo_huffman;

public class MinHeap {
    private Node[] heap; // Array to store the elements of the heap
    private int size;        // Current number of elements in the heap
    private int capacity;     // Maximum size of the heap

    // Constructor to initialize the heap with a maximum size
    public MinHeap(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        heap = new Node[capacity + 1];
        Node dummy = new Node(0); // A dummy node with minimum frequency, used as a sentinel
        heap[0] = dummy;
    }

    // Function to maintain the heap property starting from a given position
    private void minHeapify(int position) {
        if (!isLeaf(position)) { // Check if the current node is not a leaf node
            boolean hasLeftChild = leftChild(position) <= size && heap[leftChild(position)] != null;
            boolean hasRightChild = rightChild(position) <= size && heap[rightChild(position)] != null;

            if (hasLeftChild) {
                int smallestChildPos = leftChild(position);
                // Check if the right child is smaller than the left child
                if (hasRightChild && heap[rightChild(position)].getFreq() < heap[leftChild(position)].getFreq()) {
                    smallestChildPos = rightChild(position);
                }
                // Swap with the smaller child and continue heapifying if necessary
                if (heap[position].getFreq() > heap[smallestChildPos].getFreq()) {
                    swap(position, smallestChildPos);
                    minHeapify(smallestChildPos);
                }
            }
        }
    }

    // Function to insert a new element into the heap
    public void insert(Node element) {
        if (size >= capacity) {
            return; // Do nothing if the heap is already full
        }

        heap[++size] = element; // Place the element at the end of the heap
        int current = size;
        // Adjust the position of the newly added element to maintain the heap property
        while (heap[current].getFreq() < heap[parent(current)].getFreq()) {
            swap(current, parent(current));
            current = parent(current);
        }
    }

    // Function to remove and return the minimum element from the heap
    public Node remove() {
        Node min = heap[1]; // The root of the heap, which is the minimum element
        heap[1] = heap[size--]; // Replace the root with the last element and decrease the size
        minHeapify(1); // Restore the heap property
        return min;
    }

    // Getter to obtain the current size of the heap
    public int getSize() {
        return size;
    }

    // Helper function to get the index of the parent of a given node
    private int parent(int position) {
        return position / 2;
    }

    // Helper function to get the index of the left child of a given node
    private int leftChild(int position) {
        return (2 * position);
    }

    // Helper function to get the index of the right child of a given node
    private int rightChild(int position) {
        return (2 * position) + 1;
    }

    // Helper function to check if a given node is a leaf
    private boolean isLeaf(int position) {
        return position > (size / 2) && position <= size;
    }

    // Helper function to swap two elements in the heap
    private void swap(int firstPosition, int secondPosition) {
        Node temp;
        temp = heap[firstPosition];
        heap[firstPosition] = heap[secondPosition];
        heap[secondPosition] = temp;
    }
}