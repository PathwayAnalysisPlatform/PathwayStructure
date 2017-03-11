# Query of the Protein interaction graph

* Case 1: Get the graph for the whole proteome.
    - The proteome is the list of curated human proteins in Uniprot / Swiss prot.
* Case 2: Get the graph for a set of proteins.

# Graph characteristics

* The number of vertices does not change during the execution of the program.
* It is possible to map from Uniprot Ids to a number.
* The number of vertices is never higher than 20168. That is the number of proteins annotated in Reactome.
* Each edge has to have a type
* Each node is identified by a string, the Uniprot Id
* The IDs of proteins are of fixed length. The characters are simple letters and numbers, so they can be stored in a byte[].
    - 21 a length of 10
    - 20100 a length of 6


# Graph operations needed

### Case 1

* Add node: All the nodes will be added at once
* Add edge: Edges will be added with random access
* List all neighbors of a node
* List all edges of the graph
* Tell the number of edges
* Tell the number of vertices

### Case 2 
* Add node: All the nodes will be added at once
* Add edge: Edges will be added with random access
* Check if a node is present
* List all neighbors of a node
* List all edges of the graph
* Tell the number of edges
* Tell the number of vertices

# Implementation considerations

- The number of vertex fits in a "byte" primitive type. For a short the range of values are (-32,768,32,767]. 
- The Uniprot Ids can be stored in a byte[] array. 
- The map for the keys and numbers is a bidirectional HashMap:
    - From Uniprot Id to number a HashMap
    - From Number to Uniprot Id an array: byte[][]

- The graph is implemented as an Adjacency List.
    - The graph uses vertex numbers, not the protein ids.
    - The adjacency list is an array of lists of shorts: LinkedList<short>[] group = (LinkedList<short>[])new LinkedList[4];
    Every neighbor is stored as a short int, which occupies less space than another string.
    - This is because shorts occupy 2 bytes of memory and the String 36 bytes.
    - For the adjacency list of vertices. The main list should be an array or an ArrayList if it is not possible.
    - The ArrayList is selected because the random access for an element is faster than LinkedList: O(1) vs O(n/4) average.
    - The ArrayList is slower to add or delete elements: O(1) amortized, but O(n) worst-case vs O(1). But the list is fixed size, then the initial size of the ArrayList is the right one. Then each vertex will be added only once and the list will never be resized.
    - The initial size will accomodate 33% more capacity. 
    - The initial load
    - The space needed for the references in the ArrayList is smaller.
    - The list of neighbors of a vertex is implemented as a LinkedList since the number of neighbors is not known. It takes O(1) to add new elements and O(n) to traverse. There is never resizing because of initial size overflow.
    * The heap memory has to be considered. Usually objects are allocated in 8 bytes blocks. Then objects have sizes multiples of 8.

# Stages of the program

* Initialization:
    - Read the configuration
* Input: 
    - Get proteome list from Uniprot or,
    - Read the protein list
* Gather interactions / create graph
    - Query Neo4j for all the reactions
    - Iterate over each reactions:
        - Extract Protein interactions
        - Add them to the graph
* Output the graph

# Future improvements:
- Read proteome list from reactome services or uniprot online services. Currently it reads the list downloaded manually from Uniprot.