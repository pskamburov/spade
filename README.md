  Data Mining Algorithms
  --------------------------------------------------------------
  *Data mining, 2017*

Sequential pattern mining
=========================

Introduction
------------

Sequential pattern mining is a topic of data mining concerned with
finding statistically relevant patterns between data examples where the
values are delivered in a sequence. The goal is to able to
understand the data and make some decisions based on the analysis. More
precisely sequential pattern mining consists of discovering interesting
subsequences in a set of sequences, where the value of a subsequence can
be measured in terms of various criteria such as its occurrence
frequency, length and many others.

Real-life application
---------------------

The frequent pattern mining and the following association rule mining
have many real-life applications:

-   Famous story about "beer and diapers" - An analysis of 1.2 million
    market baskets from about 25 Osco Drug stores showed that between
    5:00 and 7:00 p.m. that consumers bought beer and diapers.

When you know the facts, you can find it reasonable -- when women have a
baby, they stay at home taking care of the baby, while men are supposed
to do the shopping. So, it makes sense, but before you know the facts,
it is hard to see this relation.

Any kind of shops could apply these algorithms and find some patterns. A
bookstore could discover that people who buy a specific book, have a
very good chance of buying another specific book.

-   Web sites and search engines - The sequential pattern mining could
    be used to analyze web click logs, so that user's interaction with
    the web site could be improved. Another great example are the search
    engines that are used millions and even billions times a day. The
    analyzed data could be used for many, many purposes.

-   Biology and medicine - Medical files could be analyzed to find a
    relation between symptoms and diseases. Also in biology, some DNA
    sequences or some genetic conditions could be linked to some
    patterns.

General approach
----------------

The approach of the patterns mining algorithms generally looks like
this:

*Given*: Sequences of items(database)

Algorithm steps:

1.  Generate candidates

2.  Check how frequent are these candidates in the sequences of
    items(database)

Usually these steps are done many times, iterating over the length of
the generated sequences. The iterations are done until some condition is
reached, or there are no more candidates.

SPADE
-----

**SPADE** (**S**equential **PA**ttern **D**iscovery using
**E**quivalence classes) algorithm is developed by Mohammed J. Zaki in
2001. \[3\]

SPADE uses a vertical id-list database format. Id-lists associate each
sequence with a list of objects in which it occurs. Then, frequent
sequences can be found efficiently using intersections on id-lists.
Thanks to this strategy the number of databases scans are reduced, and
therefore the execution time is faster.

### Spade -- steps by steps

The first step of SPADE is to compute the frequencies of 1-sequences,
which are sequences with only one item. This is done in a single
database scan. The second step consists of counting 2-sequences.
Subsequent n-sequences can then be formed by joining (n-1)-sequences
using their id-lists. The algorithm stops when no frequent sequences can
be found anymore or some other condition is reached. The algorithm can
use a breadth-first or a depth-first search method for finding new
sequences.

![](media/generation.png?raw=true "Patterns generation")

### Spade -- Implementations

Three modifications are implemented for this project. The
implementations follow the same steps:

1.  Get input files with sequences

2.  Generate new patterns(candidates)

3.  Prune non-frequent patterns

#### File implementation

1.  Load the input -- iterate over input files which contain sequences.
    Create an Id-list for each item in the sequences. The id-list for
    each item is represented by a file. An id-list contains the
    occurrences for each item (the sequence id and the element id).

2.  Generating new patterns -- using the already generated id-lists in
    step 1, we start generating new patterns with length 2, and save
    their id-lists in files. The id-lists are created by joining the
    already created id-lists.

3.  Pruning - after all patterns with length 2 are generated, some of
    them are removed -- the ones that are not frequent, meaning they do
    not occur so often (less than the defined minimum support).

> Then step 2 is executed again, but this time generating the patterns
> with length 3. To create the id-lists, we use the already created
> id-lists of the patterns with length 2, and the id-lists of frequent
> items generated in step 1. Then step 3 is executed again. And so on.

#### Partially In-memory implementation

1.  Load the input - Iterate over input files which contain sequences.
    Create an Id-list for each item in the sequences. Only this time the
    frequent items id-lists are stored in-memory.

2.  Generating new patterns -- Same as File Implementation. (only the
    frequent items are used from a Map, not from a file).

3.  Pruning -- Same as File Implementation.

#### In-memory implementation

1.  Load the input -- Same as Partially In-Memory implementation.

2.  Generating new patterns -- Using the id-lists in step 1, we start
    generating new patterns with length 2, only this implementation
    store the new patterns in-memory in a Map, not in files.

3.  Pruning -- Remove the non-frequent patterns from the Map.

### SPADE - Stats

The first step of SPADE is to compute the frequencies of 1-sequences,
which are all the items. This step is pretty much the same for the three
implementations because all the sequences should be scanned and items
should be analyzed. There is no way to pass this step, or make some huge
improvements so the execution time for this step is shown in a common
table for the three implementations:

Table 1. Execution time for frequent items

  | **Input size**                 | **Execution time in milliseconds** |
  | ------------------------------ | ---------------------------------- |
  | 100 sequences (2408 items)     | 2000                               |
  | 1000 sequences (17069 items)   | 17000                              |
  | 10000 sequences (120103 items) | 60000                              |

The next table shows us the execution time for generating new patterns.

Table 2. Execution time for generating patterns
![](media/table2.png?raw=true "Execution time for patterns")

There is additional information for the In-memory implementation --
"Seconds for output". For instance, let's look at the first row of the
table for In-memory implementation -- it takes 60 milliseconds to
process 100 sequences (2408 items), but after the 60 milliseconds the
new generated patterns are stored in Map, so to save the result from the
map to a file, it takes 5 seconds.

The performance testing was done using max heap size 2GB on a CPU Intel
Core I5-4300.

### SPADE - Findings

Looking at the performance testing table, we can make a few conclusions.
Comparing the File implementation and the Partially in-memory
implementation, there is not much a difference. We can see a bigger
difference is special cases where the frequent items are a lot, so there
will be a real benefit to store them in-memory instead of storing them
in files. But of course, there is the possibility to run out of memory.

The In-memory implementation is a lot faster than the others. This is
expected because accessing the in-memory objects is a lot faster than
reading and writing to files on the disk. Further analysis was made to
confirm that - In the example with the 1000 sequences (17069 items) the
file implementation needs 106000 milliseconds. Statistics for the
input/output file operations.

![](media/findings1.png?raw=true "IO operations")

As we can see, a lot more times we read from the files -- 40MB are read,
but the read time is only 189ms. This is because we read the files to
make the temporal joins, so once the file is opened we read a lot of
data from it. On the other hand, writing is a slower operation -the data
written on files is only 2MB, but it takes 19seconds.

Some additional statistics:

![](media/findings2.png?raw=true "IO operations")

The maximum number of times a file was opened - 1243 times. Totally
nearly 300000 openings on the files were made.

About the performance:

![](media/findings3.png?raw=true "Performance")

So, in conclusion, the file implementation should be able to run on
large data, but the execution time is not very good.

### SPADE -- Future improvements

Some ideas for improving the implementations execution time:

-   In current implementations, the id-lists are written as strings (the
    sequence id and the element id are string). To make the joins of
    id-lists faster, a better structure for the id-lists could be used
    -- binary format, some bit maps, or other.

-   Another thing to consider is parallel generation of patterns. This
    could improve the execution time a lot.

-   Some experimenting could be made with another way of generating the
    patterns. Currently we generate all possible combinations using
    Bread-First-Search method. Some better approaches could be used --
    and again working with multiple threads could be considered.

-   Clean code principles to be applied - use logger, extract parameters as a properties file or as a java system properties, etc.

Resources
=========

\[IJCSIT\] "An Effective Approach to Mine Frequent Sequential Pattern
over Uncertain dataset", Kshiti S Rana, Hiren V Mer, International
Journal of Computer Science and Information Technologies

\[DSRES\] http://www.dssresources.com/newsletters/66.php

\[SPADE\] SPADE: An Efficient Algorithm for Mining Frequent Sequences,
Mohammed J. Zaki
